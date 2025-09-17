package DAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Check-out logic (no ticket table; write directly to BORROW_RECORDS).
 */
public class LoanTicketDAO {

    private final Connection conn;

    public LoanTicketDAO(Connection conn) {
        this.conn = conn;
    }

    public static class CheckoutResult {
        public long patronSysId;
        public int  itemCount;
        public Date issueDate;
        public Date dueDate;
        public List<String> checked = new ArrayList<>();
        public List<String> skipped = new ArrayList<>();
    }

    /**
     * Check-out many books by call numbers.
     * - Validate Patron by PatronID (active)
     * - For each call number:
     *   * lock copy row
     *   * skip if not found / not available
     *   * insert BORROW_RECORDS (CURDATE, DATE_ADD(..., ? DAY))
     *   * set BOOK_COPIES.IsAvailable = 0
     */
    public CheckoutResult checkoutMany(List<String> callNumbers, String patronId, long userId) throws Exception {
        if (callNumbers == null || callNumbers.isEmpty()) throw new IllegalArgumentException("Empty callNumbers");
        if (patronId == null || patronId.isBlank()) throw new IllegalArgumentException("Empty patronId");

        CheckoutResult out = new CheckoutResult();

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // 1) patronSysId
            Long patronSysId = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT PatronSysID FROM patrons WHERE PatronID = ? AND IsActive = 1")) {
                ps.setString(1, patronId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) patronSysId = rs.getLong(1);
                }
            }
            if (patronSysId == null) throw new Exception("Patron not found or inactive: " + patronId);
            out.patronSysId = patronSysId;

            // 2) default due days, late fee per day and deposit multiplier (snapshot)
            int dueDays = 5;
            BigDecimal lateFeePerDay = BigDecimal.valueOf(0.10);
            BigDecimal depositMultiplier = BigDecimal.ONE;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT DefaultDueDays, LateFeePerDay, DepositMultiplier FROM lib_settings ORDER BY SettingID DESC LIMIT 1")) {
                if (rs.next()) {
                    int v = rs.getInt(1); if (v > 0) dueDays = v;
                    BigDecimal lf = rs.getBigDecimal(2); if (lf != null) lateFeePerDay = lf;
                    BigDecimal dm = rs.getBigDecimal(3); if (dm != null) depositMultiplier = dm;
                }
            } catch (SQLException ignore) { /* fallback values */ }

            // Set result dates by application clock (matching SQL CURDATE logic)
            out.issueDate = Date.valueOf(LocalDate.now());
            out.dueDate   = Date.valueOf(LocalDate.now().plusDays(dueDays));

            // Create loan slip (snapshot settings) — will be removed if no items checked out
            long slipId;
            String insSlip = "INSERT INTO loan_slips (PatronSysID, IssueDate, DueDaysUsed, LateFeePerDayUsed, DepositMultiplierUsed, CheckoutByUserID, Status) " +
                             "VALUES (?, CURDATE(), ?, ?, ?, ?, 'OPEN')";
            try (PreparedStatement psSlip = conn.prepareStatement(insSlip, Statement.RETURN_GENERATED_KEYS)) {
                psSlip.setLong(1, patronSysId);
                psSlip.setInt(2, dueDays);
                psSlip.setBigDecimal(3, lateFeePerDay);
                psSlip.setBigDecimal(4, depositMultiplier);
                psSlip.setLong(5, userId);
                psSlip.executeUpdate();
                try (ResultSet gk = psSlip.getGeneratedKeys()) { gk.next(); slipId = gk.getLong(1); }
            }

            // Prepared SQLs
            String qCopy = "SELECT bc.CopyID, bc.IsAvailable, b.Price FROM book_copies bc JOIN books b ON bc.BookID=b.BookID WHERE bc.CallNumber = ? FOR UPDATE";
            String insBr = "INSERT INTO borrow_records (SlipID, CopyID, DueDate, LateFee, DepositAmount) VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL ? DAY), 0, ?)";
            String upCopy = "UPDATE book_copies SET IsAvailable = 0 WHERE CopyID = ?";

            try (PreparedStatement psCopy = conn.prepareStatement(qCopy);
                 PreparedStatement psIns  = conn.prepareStatement(insBr);
                 PreparedStatement psUp   = conn.prepareStatement(upCopy)) {

                for (String call : callNumbers) {
                    if (call == null || call.isBlank()) continue;

                    // lock copy row and get price
                    Long copyId = null; boolean avail = false; BigDecimal price = BigDecimal.ZERO;
                    psCopy.setString(1, call.trim());
                    try (ResultSet rs = psCopy.executeQuery()) {
                        if (rs.next()) {
                            copyId = rs.getLong("CopyID");
                            avail  = rs.getBoolean("IsAvailable");
                            price  = rs.getBigDecimal("Price");
                            if (price == null) price = BigDecimal.ZERO;
                        }
                    }

                    if (copyId == null) { out.skipped.add(call + " (not found)"); continue; }
                    if (!avail)         { out.skipped.add(call + " (not available)"); continue; }

                    // compute deposit snapshot
                    BigDecimal deposit = price.multiply(depositMultiplier);

                    // insert borrow record
                    psIns.setLong(1, slipId);
                    psIns.setLong(2, copyId);
                    psIns.setInt(3, dueDays);
                    psIns.setBigDecimal(4, deposit);
                    psIns.executeUpdate();

                    // mark copy un-available
                    psUp.setLong(1, copyId);
                    psUp.executeUpdate();

                    out.checked.add(call);
                    out.itemCount++;
                }
            }

            // If nothing was checked out, remove the created slip
            if (out.itemCount == 0) {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM loan_slips WHERE SlipID = ?")) {
                    del.setLong(1, slipId); del.executeUpdate();
                } catch (SQLException ignore) {}
            }

            conn.commit();
            return out;
        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(oldAuto); } catch (SQLException ignore) {}
        }
    }

    public static class CheckinResult {
        public int itemCount;
        public BigDecimal totalLateFees = BigDecimal.ZERO;
        public List<String> checked = new ArrayList<>();
        public List<String> skipped = new ArrayList<>();
        public Date checkinDate;
    }

    /**
     * Check-in many books by call numbers.
     * - For each call number: find open borrow record, lock row, set ReturnDate=CURDATE(), compute LateFee,
     *   set CheckinByUserID and mark book copy available.
     */
    public CheckinResult checkinMany(List<String> callNumbers, long userId) throws Exception {
        if (callNumbers == null || callNumbers.isEmpty()) throw new IllegalArgumentException("Empty callNumbers");

        CheckinResult out = new CheckinResult();

        boolean oldAuto = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // 1) late fee per day from settings (fallback 0.10)
            BigDecimal lateFeePerDay = BigDecimal.valueOf(0.10);
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT LateFeePerDay FROM lib_settings ORDER BY SettingID DESC LIMIT 1")) {
                if (rs.next()) {
                    BigDecimal v = rs.getBigDecimal(1);
                    if (v != null) lateFeePerDay = v;
                }
            } catch (SQLException ignore) { /* fallback */ }

            out.checkinDate = Date.valueOf(LocalDate.now());

            String qOpen = "SELECT br.RecordID, br.CopyID, br.DueDate " +
                           "FROM borrow_records br JOIN book_copies bc ON br.CopyID=bc.CopyID " +
                           "WHERE bc.CallNumber = ? AND br.ReturnDate IS NULL FOR UPDATE";
            String updBr = "UPDATE borrow_records SET ReturnDate=CURDATE(), LateFee=?, CheckinByUserID=? WHERE RecordID=?";
            String updCopy = "UPDATE book_copies SET IsAvailable = 1 WHERE CopyID = ?";

            try (PreparedStatement psSel = conn.prepareStatement(qOpen);
                 PreparedStatement psUpdBr = conn.prepareStatement(updBr);
                 PreparedStatement psUpdCopy = conn.prepareStatement(updCopy)) {

                for (String call : callNumbers) {
                    if (call == null || call.isBlank()) continue;

                    psSel.setString(1, call.trim());
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) { out.skipped.add(call + " (not found/open)"); continue; }
                        long recordId = rs.getLong("RecordID");
                        long copyId = rs.getLong("CopyID");
                        Date due = rs.getDate("DueDate");

                        long daysLate = 0L;
                        if (due != null) {
                            long diff = (System.currentTimeMillis() - due.getTime()) / (24L*60*60*1000);
                            if (diff > 0) daysLate = diff;
                        }

                        BigDecimal late = lateFeePerDay.multiply(BigDecimal.valueOf(daysLate));
                        if (late.compareTo(BigDecimal.ZERO) < 0) late = BigDecimal.ZERO;

                        psUpdBr.setBigDecimal(1, late);
                        psUpdBr.setLong(2, userId);
                        psUpdBr.setLong(3, recordId);
                        psUpdBr.executeUpdate();

                        psUpdCopy.setLong(1, copyId);
                        psUpdCopy.executeUpdate();

                        out.checked.add(call);
                        out.itemCount++;
                        out.totalLateFees = out.totalLateFees.add(late);
                    }
                }
            }

            conn.commit();
            return out;
        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignore) {}
            throw ex;
        } finally {
            try { conn.setAutoCommit(oldAuto); } catch (SQLException ignore) {}
        }
    }
}
