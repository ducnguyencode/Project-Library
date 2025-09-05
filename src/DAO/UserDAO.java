// DAO/UserDAO.java
package DAO;

import Util.DB;
import Util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    /** Đăng nhập: trả về UserID nếu ok; ngược lại null. */
    public Long login(String username, String password) throws Exception {
        String sql = "SELECT UserID, PasswordHash, Role FROM SYSTEM_USERS " +
                     "WHERE Username=? AND IsActive=1 LIMIT 1";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                long userId = rs.getLong("UserID");
                String hash = rs.getString("PasswordHash");
                String role = rs.getString("Role");

                if (PasswordUtil.verify(password, hash)) {
                    Util.Session.currentUserId = userId;
                    Util.Session.currentUsername = username;
                    Util.Session.currentRole = role;
                    return userId;
                }
                return null;
            }
        }
    }

    /** Tạo admin nếu chưa tồn tại (dùng cho bootstrap lần đầu). */
    public void ensureAdmin(String username, String plainPassword, Long staffSystemId) throws Exception {
        try (Connection c = DB.get()) {
            // đã có user chưa?
            try (PreparedStatement chk = c.prepareStatement(
                    "SELECT 1 FROM SYSTEM_USERS WHERE Username=? LIMIT 1")) {
                chk.setString(1, username);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) return; // đã có, bỏ qua
                }
            }
            String hash = Util.PasswordUtil.hash(plainPassword);
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO SYSTEM_USERS(Username,PasswordHash,Role,IsActive,StaffSystemID,LastLogin) " +
                    "VALUES(?,?, 'ADMIN',1, ?, NOW())")) {
                ins.setString(1, username);
                ins.setString(2, hash);
                if (staffSystemId == null) ins.setNull(3, Types.BIGINT); else ins.setLong(3, staffSystemId);
                ins.executeUpdate();
            }
        }
    }
}
