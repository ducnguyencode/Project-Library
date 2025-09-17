package DAO;

import Util.DB;

import java.sql.*;
import java.util.*;

/**
 * Read-only DAO for borrowing history and open loans.
 * Schema used (joins):
 *   BORROW_RECORDS br
 *   JOIN LOAN_SLIPS ls    ON br.SlipID = ls.SlipID
 *   JOIN BOOK_COPIES bc   ON br.CopyID = bc.CopyID
 *   JOIN BOOKS b          ON bc.BookID = b.BookID
 *   JOIN PATRONS p        ON ls.PatronSysID = p.PatronSysID
 */
public class BorrowRecordDAO {

    public List<Map<String,Object>> listOpenByPatron(String patronId) throws Exception {
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.LateFee, br.ReturnDate " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE p.PatronID = ? AND br.ReturnDate IS NULL " +
                     "ORDER BY br.DueDate";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patronId);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(row(rs));
            }
            return out;
        }
    }

    public List<Map<String,Object>> listHistoryByPatron(String patronId, int limit, int offset) throws Exception {
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.ReturnDate, br.LateFee " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE p.PatronID = ? " +
                     "ORDER BY COALESCE(br.ReturnDate, br.DueDate) DESC, br.RecordID DESC " +
                     "LIMIT ? OFFSET ?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, patronId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(row(rs));
            }
            return out;
        }
    }

    public List<Map<String,Object>> listHistoryByCopy(String callNumber, int limit, int offset) throws Exception {
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.ReturnDate, br.LateFee " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE bc.CallNumber = ? " +
                     "ORDER BY br.RecordID DESC " +
                     "LIMIT ? OFFSET ?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, callNumber);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(row(rs));
            }
            return out;
        }
    }

    public List<Map<String,Object>> listOverdues() throws Exception {
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.ReturnDate, br.LateFee " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE br.ReturnDate IS NULL AND br.DueDate < CURDATE() " +
                     "ORDER BY br.DueDate";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(row(rs));
            }
            return out;
        }
    }

    /** Search open loans with flexible filters and pagination. Any filter can be null. */
    public List<Map<String,Object>> searchOpenLoans(String callLike, String patronLike, String titleLike,
                                                    boolean overdueOnly, int page, int pageSize) throws Exception {
        int offset = Math.max(0, (page-1)*pageSize);
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.ReturnDate, br.LateFee " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE br.ReturnDate IS NULL " +
                     "  AND (? IS NULL OR bc.CallNumber LIKE CONCAT('%',?,'%')) " +
                     "  AND (? IS NULL OR p.PatronID  LIKE CONCAT('%',?,'%') OR p.Name LIKE CONCAT('%',?,'%')) " +
                     "  AND (? IS NULL OR b.Title    LIKE CONCAT('%',?,'%')) " +
                     (overdueOnly ? "  AND br.DueDate < CURDATE() " : "") +
                     "ORDER BY br.DueDate, br.RecordID " +
                     "LIMIT ? OFFSET ?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i=1;
            ps.setString(i++, callLike); ps.setString(i++, callLike);
            ps.setString(i++, patronLike); ps.setString(i++, patronLike); ps.setString(i++, patronLike);
            ps.setString(i++, titleLike);  ps.setString(i++, titleLike);
            ps.setInt(i++, pageSize); ps.setInt(i++, offset);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(row(rs)); }
            return out;
        }
    }

    private Map<String,Object> row(ResultSet rs) throws SQLException {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("RecordID", rs.getLong("RecordID"));
        m.put("SlipID", rs.getLong("SlipID"));
        m.put("PatronID", rs.getString("PatronID"));
        m.put("PatronName", rs.getString("PatronName"));
        m.put("CallNumber", rs.getString("CallNumber"));
        m.put("Title", rs.getString("Title"));
        m.put("Author", rs.getString("Author"));
        m.put("IssueDate", rs.getTimestamp("IssueDate"));
        m.put("DueDate", rs.getTimestamp("DueDate"));
        m.put("ReturnDate", rs.getTimestamp("ReturnDate"));
        m.put("LateFee", rs.getBigDecimal("LateFee"));
        return m;
    }

    /** Loan history with filters and IssueDate range (inclusive). */
    public List<Map<String,Object>> searchLoanHistory(String callLike, String patronLike, String titleLike,
                                                      java.sql.Date from, java.sql.Date to,
                                                      int page, int pageSize) throws Exception {
        int offset = Math.max(0, (page-1)*pageSize);
        String sql = "SELECT br.RecordID, ls.SlipID, p.PatronID, p.Name AS PatronName, " +
                     "       bc.CallNumber, b.Title, b.Author, ls.IssueDate, br.DueDate, br.ReturnDate, br.LateFee " +
                     "FROM BORROW_RECORDS br " +
                     "JOIN LOAN_SLIPS ls  ON br.SlipID = ls.SlipID " +
                     "JOIN BOOK_COPIES bc ON br.CopyID = bc.CopyID " +
                     "JOIN BOOKS b       ON bc.BookID = b.BookID " +
                     "JOIN PATRONS p     ON ls.PatronSysID = p.PatronSysID " +
                     "WHERE (? IS NULL OR bc.CallNumber LIKE CONCAT('%',?,'%')) " +
                     "  AND (? IS NULL OR p.PatronID  LIKE CONCAT('%',?,'%') OR p.Name LIKE CONCAT('%',?,'%')) " +
                     "  AND (? IS NULL OR b.Title    LIKE CONCAT('%',?,'%')) " +
                     "  AND (? IS NULL OR ls.IssueDate >= ?) " +
                     "  AND (? IS NULL OR ls.IssueDate <= ?) " +
                     "ORDER BY ls.IssueDate DESC, br.RecordID DESC " +
                     "LIMIT ? OFFSET ?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i=1;
            ps.setString(i++, callLike); ps.setString(i++, callLike);
            ps.setString(i++, patronLike); ps.setString(i++, patronLike); ps.setString(i++, patronLike);
            ps.setString(i++, titleLike);  ps.setString(i++, titleLike);
            ps.setDate(i++, from); ps.setDate(i++, from);
            ps.setDate(i++, to);   ps.setDate(i++, to);
            ps.setInt(i++, pageSize); ps.setInt(i++, offset);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) out.add(row(rs)); }
            return out;
        }
    }
}

