package DAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

            // 2) default due days (fallback 5)
            int dueDays = 5;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT DefaultDueDays FROM lib_settings ORDER BY SettingID DESC LIMIT 1")) {
                if (rs.next()) dueDays = rs.getInt(1);
                if (dueDays <= 0) dueDays = 5;
            } catch (SQLException ignore) { /* fallback 5 */ }

            // Set result dates by application clock (matching SQL CURDATE logic)
            out.issueDate = Date.valueOf(LocalDate.now());
            out.dueDate   = Date.valueOf(LocalDate.now().plusDays(dueDays));

            // Prepared SQLs
            String qCopy = "SELECT CopyID, IsAvailable FROM book_copies WHERE CallNumber = ? FOR UPDATE";
            String insBr = "INSERT INTO borrow_records " +
                    "(CopyID, PatronSysID, IssueDate, DueDate, LateFee, CheckoutByUserID) " +
                    "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 0, ?)";
            String upCopy = "UPDATE book_copies SET IsAvailable = 0 WHERE CopyID = ?";

            try (PreparedStatement psCopy = conn.prepareStatement(qCopy);
                 PreparedStatement psIns  = conn.prepareStatement(insBr);
                 PreparedStatement psUp   = conn.prepareStatement(upCopy)) {

                for (String call : callNumbers) {
                    if (call == null || call.isBlank()) continue;

                    // lock copy row
                    Long copyId = null; boolean avail = false;
                    psCopy.setString(1, call.trim());
                    try (ResultSet rs = psCopy.executeQuery()) {
                        if (rs.next()) {
                            copyId = rs.getLong("CopyID");
                            avail  = rs.getBoolean("IsAvailable");
                        }
                    }

                    if (copyId == null) { out.skipped.add(call + " (not found)"); continue; }
                    if (!avail)         { out.skipped.add(call + " (not available)"); continue; }

                    // insert borrow record
                    psIns.setLong(1, copyId);
                    psIns.setLong(2, patronSysId);
                    psIns.setInt(3, dueDays);
                    psIns.setLong(4, userId);
                    psIns.executeUpdate();

                    // mark copy un-available
                    psUp.setLong(1, copyId);
                    psUp.executeUpdate();

                    out.checked.add(call);
                    out.itemCount++;
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
