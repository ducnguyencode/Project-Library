// DAO/PatronDAO.java
package DAO;

import Model.Patron;
import Util.DB;

import java.sql.*;
import java.util.*;

public class PatronDAO {

    // Thêm độc giả, trả về PatronSysID
    public long insert(Patron p) throws Exception {
        String sql = "INSERT INTO PATRONS(PatronID,Name,Address,Phone,Email,IsActive) VALUES(?,?,?,?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.patronId);
            ps.setString(2, p.name);
            ps.setString(3, p.address);
            ps.setString(4, p.phone);
            ps.setString(5, p.email);
            ps.setBoolean(6, p.isActive);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public Patron get(long patronSysId) throws Exception {
        String sql = "SELECT PatronSysID,PatronID,Name,Address,Phone,Email,IsActive " +
                     "FROM PATRONS WHERE PatronSysID=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, patronSysId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Patron p = new Patron();
                p.patronSysId = rs.getLong(1);
                p.patronId    = rs.getString(2);
                p.name        = rs.getString(3);
                p.address     = rs.getString(4);
                p.phone       = rs.getString(5);
                p.email       = rs.getString(6);
                p.isActive    = rs.getBoolean(7);
                return p;
            }
        }
    }

    public void update(Patron p) throws Exception {
        String sql = "UPDATE PATRONS SET PatronID=?, Name=?, Address=?, Phone=?, Email=?, IsActive=? " +
                     "WHERE PatronSysID=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.patronId);
            ps.setString(2, p.name);
            ps.setString(3, p.address);
            ps.setString(4, p.phone);
            ps.setString(5, p.email);
            ps.setBoolean(6, p.isActive);
            ps.setLong(7, p.patronSysId);
            ps.executeUpdate();
        }
    }

    /** Xóa khi KHÔNG có khoản mượn đang mở (ReturnDate IS NULL) */
    public boolean deleteIfNoOpenLoans(long patronSysId) throws Exception {
        String chk = "SELECT COUNT(*) " +
                     "FROM LOAN_SLIPS ls JOIN BORROW_RECORDS br ON br.SlipID=ls.SlipID " +
                     "WHERE ls.PatronSysID=? AND br.ReturnDate IS NULL";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(chk)) {
            ps.setLong(1, patronSysId);
            try (ResultSet rs = ps.executeQuery()) { rs.next();
                if (rs.getInt(1) > 0) return false;
            }
        }
        try (Connection c = DB.get();
             PreparedStatement del = c.prepareStatement("DELETE FROM PATRONS WHERE PatronSysID=?")) {
            del.setLong(1, patronSysId);
            del.executeUpdate();
            return true;
        }
    }

    /** Tìm kiếm + tổng quan: số khoản mượn đang mở & tổng phí trễ hạn. Giới hạn phân trang. */
    public List<Map<String,Object>> searchWithSummary(String patronIdLike, String nameLike,
                                                      int page, int pageSize) throws Exception {
        int offset = Math.max(0, (page-1)*pageSize);
        String sql =
            "SELECT p.PatronSysID, p.PatronID, p.Name, p.Phone, p.Email, p.IsActive, " +
            "       COALESCE(SUM(CASE WHEN br.ReturnDate IS NULL THEN 1 ELSE 0 END),0) AS OpenLoans, " +
            "       COALESCE(SUM(br.LateFee),0) AS TotalFees " +
            "FROM PATRONS p " +
            "LEFT JOIN LOAN_SLIPS ls ON ls.PatronSysID=p.PatronSysID " +
            "LEFT JOIN BORROW_RECORDS br ON br.SlipID=ls.SlipID " +
            "WHERE (? IS NULL OR p.PatronID LIKE CONCAT('%',?,'%')) " +
            "  AND (? IS NULL OR p.Name    LIKE CONCAT('%',?,'%')) " +
            "GROUP BY p.PatronSysID, p.PatronID, p.Name, p.Phone, p.Email, p.IsActive " +
            "ORDER BY p.PatronSysID DESC " +
            "LIMIT ?,?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            // bind trùng tham số
            ps.setString(1, patronIdLike); ps.setString(2, patronIdLike);
            ps.setString(3, nameLike);     ps.setString(4, nameLike);
            ps.setInt(5, offset);          ps.setInt(6, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("PatronSysID", rs.getLong("PatronSysID"));
                    m.put("PatronID",    rs.getString("PatronID"));
                    m.put("Name",        rs.getString("Name"));
                    m.put("Phone",       rs.getString("Phone"));
                    m.put("Email",       rs.getString("Email"));
                    m.put("IsActive",    rs.getBoolean("IsActive"));
                    m.put("OpenLoans",   rs.getInt("OpenLoans"));
                    m.put("TotalFees",   rs.getBigDecimal("TotalFees"));
                    list.add(m);
                }
                return list;
            }
        }
    }

    /** Chi tiết các lần mượn/trả của 1 độc giả (để hiện ở PatronLoansDialog). */
    public List<Map<String,Object>> borrowDetails(long patronSysId) throws Exception {
        String sql =
            "SELECT br.RecordID, bc.CallNumber, b.Title, ls.IssueDate, br.DueDate, br.ReturnDate, " +
            "       br.LateFee, br.DepositAmount, (br.ReturnDate IS NULL) AS IsOpen " +
            "FROM LOAN_SLIPS ls " +
            "JOIN BORROW_RECORDS br ON br.SlipID = ls.SlipID " +
            "JOIN BOOK_COPIES bc ON bc.CopyID = br.CopyID " +
            "JOIN BOOKS b ON b.BookID = bc.BookID " +
            "WHERE ls.PatronSysID=? " +
            "ORDER BY ls.IssueDate DESC, br.RecordID DESC";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, patronSysId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("RecordID",      rs.getLong("RecordID"));
                    m.put("CallNumber",    rs.getString("CallNumber"));
                    m.put("Title",         rs.getString("Title"));
                    m.put("IssueDate",     rs.getDate("IssueDate"));
                    m.put("DueDate",       rs.getDate("DueDate"));
                    m.put("ReturnDate",    rs.getDate("ReturnDate"));
                    m.put("LateFee",       rs.getBigDecimal("LateFee"));
                    m.put("DepositAmount", rs.getBigDecimal("DepositAmount"));
                    m.put("IsOpen",        rs.getBoolean("IsOpen"));
                    list.add(m);
                }
                return list;
            }
        }
    }
}
