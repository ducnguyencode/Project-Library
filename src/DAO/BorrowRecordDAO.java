package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class BorrowRecordDAO {
    private final Connection conn;

    public static class CheckoutResult {
        public final int itemCount;
        public CheckoutResult(int itemCount) { this.itemCount = itemCount; }
    }

    public BorrowRecordDAO(Connection conn) {
        this.conn = conn;
    }

    private long getEmployeeSysId(String employeeId) throws Exception {
        String sql = "SELECT SystemID FROM employees WHERE EmployeeID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Employee not found: " + employeeId);
                return rs.getLong(1);
            }
        }
    }

    public CheckoutResult checkOutMany(List<String> callNumbers, String employeeId, Integer userId) throws Exception {
        conn.setAutoCommit(false);
        try {
            long empSysId = getEmployeeSysId(employeeId);
            int ok = 0;

            String qCopy = "SELECT CopyID, IsAvailable FROM book_copies WHERE CallNumber = ? FOR UPDATE";
            String ins = "INSERT INTO borrow_records (CopyID, EmployeeSysID, IssueDate, DueDate, CheckoutByUserID) "
                       + "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY), ?)";
            String upd = "UPDATE book_copies SET IsAvailable = 0 WHERE CopyID = ?";

            try (PreparedStatement psSel = conn.prepareStatement(qCopy);
                 PreparedStatement psIns = conn.prepareStatement(ins);
                 PreparedStatement psUpd = conn.prepareStatement(upd)) {

                for (String call : callNumbers) {
                    psSel.setString(1, call);
                    try (ResultSet rs = psSel.executeQuery()) {
                        if (!rs.next()) throw new Exception("Copy not found: " + call);
                        long copyId = rs.getLong("CopyID");
                        boolean available = rs.getBoolean("IsAvailable");
                        if (!available) throw new Exception("Copy not available: " + call);

                        psIns.setLong(1, copyId);
                        psIns.setLong(2, empSysId);
                        if (userId == null) psIns.setNull(3, java.sql.Types.INTEGER);
                        else psIns.setInt(3, userId);
                        psIns.executeUpdate();

                        psUpd.setLong(1, copyId);
                        psUpd.executeUpdate();

                        ok++;
                    }
                }
            }

            conn.commit();
            return new CheckoutResult(ok);
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
