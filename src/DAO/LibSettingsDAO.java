package DAO;

import Util.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LibSettingsDAO {

    public Map<String,Object> getLatest() throws Exception {
        String sql = "SELECT SettingID, DefaultDueDays, LateFeePerDay, MaxBooksPerPatron, DepositMultiplier, UpdatedAt, UpdatedByUserID " +
                     "FROM LIB_SETTINGS ORDER BY SettingID DESC LIMIT 1";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            Map<String,Object> m = new HashMap<>();
            m.put("SettingID", rs.getLong("SettingID"));
            m.put("DefaultDueDays", rs.getInt("DefaultDueDays"));
            m.put("LateFeePerDay", rs.getBigDecimal("LateFeePerDay"));
            m.put("MaxBooksPerPatron", rs.getInt("MaxBooksPerPatron"));
            m.put("DepositMultiplier", rs.getBigDecimal("DepositMultiplier"));
            m.put("UpdatedAt", rs.getTimestamp("UpdatedAt"));
            m.put("UpdatedByUserID", rs.getObject("UpdatedByUserID"));
            return m;
        }
    }

    public long insertNew(int defaultDueDays, BigDecimal lateFeePerDay, int maxBooks, BigDecimal depositMultiplier, Long userId) throws Exception {
        String sql = "INSERT INTO LIB_SETTINGS(DefaultDueDays, LateFeePerDay, MaxBooksPerPatron, DepositMultiplier, UpdatedAt, UpdatedByUserID) " +
                     "VALUES (?,?,?,?, NOW(), ?)";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, defaultDueDays);
            ps.setBigDecimal(2, lateFeePerDay);
            ps.setInt(3, maxBooks);
            ps.setBigDecimal(4, depositMultiplier);
            if (userId == null) ps.setNull(5, Types.BIGINT); else ps.setLong(5, userId);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) { gk.next(); return gk.getLong(1); }
        }
    }
}

