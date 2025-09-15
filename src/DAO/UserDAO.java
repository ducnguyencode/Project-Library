// DAO/UserDAO.java
package DAO;

import Model.SystemUser;
import Util.DB;
import Util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    /** Tạo admin mặc định nếu chưa có. Gọi trong App.main() */
    public void ensureAdmin(String username, String defaultPassword, Long staffSystemId) throws Exception {
        String chk = "SELECT UserID FROM SYSTEM_USERS WHERE Username=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(chk)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return; // đã có
            }
        }
        String ins = "INSERT INTO SYSTEM_USERS(Username,PasswordHash,Role,IsActive,StaffSystemID,LastLogin) " +
                     "VALUES (?,?,?,?,?,NULL)";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(ins)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(defaultPassword));
            ps.setString(3, "ADMIN");
            ps.setBoolean(4, true);
            if (staffSystemId == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, staffSystemId);
            ps.executeUpdate();
        }
    }

    /** Đăng nhập: trả SystemUser nếu hợp lệ & active, ngược lại trả null */
    public SystemUser login(String username, String plainPassword) throws Exception {
        String sql = "SELECT UserID, Username, PasswordHash, Role, IsActive, StaffSystemID, LastLogin " +
                     "FROM SYSTEM_USERS WHERE Username=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                boolean active = rs.getBoolean("IsActive");
                String  hash   = rs.getString("PasswordHash");
                if (!active || !PasswordUtil.verify(plainPassword, hash)) return null;

                SystemUser u = new SystemUser();
                u.userId         = rs.getLong("UserID");
                u.username       = rs.getString("Username");
                u.passwordHash   = hash;
                u.role           = rs.getString("Role");
                u.isActive       = active;
                long s = rs.getLong("StaffSystemID");
                u.staffSystemId  = rs.wasNull() ? null : s;
                Timestamp ts = rs.getTimestamp("LastLogin");
                u.lastLogin = ts == null ? null : new java.util.Date(ts.getTime());
                return u;
            }
        }
    }

    /**
     * Đổi mật khẩu.
     * - Nếu oldPlain == null hoặc rỗng: BỎ QUA bước verify (dùng cho first login – user đã được authenticate).
     * - Ngược lại: verify oldPlain với PasswordHash trước khi update.
     * @return true nếu đổi thành công, false nếu verify thất bại
     */
    public boolean changePassword(long userId, String oldPlain, String newPlain) throws Exception {
        String curHash;
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("SELECT PasswordHash FROM SYSTEM_USERS WHERE UserID=?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                curHash = rs.getString(1);
            }
        }

        if (oldPlain != null && !oldPlain.isEmpty()) {
            if (!PasswordUtil.verify(oldPlain, curHash)) return false;
        }

        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("UPDATE SYSTEM_USERS SET PasswordHash=? WHERE UserID=?")) {
            ps.setString(1, PasswordUtil.hash(newPlain));
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
        return true;
    }

    /** cập nhật LastLogin = NOW() sau khi đăng nhập (và đổi mật khẩu nếu first login) */
    public void updateLastLogin(long userId) throws Exception {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement("UPDATE SYSTEM_USERS SET LastLogin=NOW() WHERE UserID=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
    
 // DAO/UserDAO.java (bổ sung)
    public long createForEmployee(long staffSystemId, String username, String tempPassword, String role) throws Exception {
        try (Connection c = DB.get()) {
            try (PreparedStatement t = c.prepareStatement("SELECT 1 FROM SYSTEM_USERS WHERE Username=? LIMIT 1")) {
                t.setString(1, username);
                try (ResultSet r = t.executeQuery()) {
                    if (r.next()) throw new Exception("Username already exists");
                }
            }
            String hash = PasswordUtil.hash(tempPassword);
            String sql = "INSERT INTO SYSTEM_USERS(Username,PasswordHash,Role,IsActive,StaffSystemID) VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, hash);
                ps.setString(3, role);
                ps.setInt   (4, 1);
                ps.setLong  (5, staffSystemId);
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getLong(1); }
            }
        }
    }

}
