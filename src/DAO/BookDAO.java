package DAO;

import Model.Books;
import Util.DB;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class BookDAO {

    /** Thêm đầu sách theo Hướng A (không dùng SP)
     *  - Tính SubjectSeq kế tiếp (FOR UPDATE để tránh tranh chấp)
     *  - Sinh ISBN = NNN-NNNN (SubjectCode-SubjectSeq)
     */
    public long insertAuto(Books b) throws Exception {
        if (b == null) throw new IllegalArgumentException("Book is null");
        if (b.subjectCode <= 0) throw new IllegalArgumentException("Please choose a Subject");

        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                // 1) SubjectSeq kế tiếp trong chủ đề
                int nextSeq;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(MAX(SubjectSeq),0)+1 FROM BOOKS WHERE SubjectCode=? FOR UPDATE")) {
                    ps.setInt(1, b.subjectCode);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); nextSeq = rs.getInt(1); }
                }

                // 2) ISBN theo format NNN-NNNN
                String isbn = String.format("%03d-%04d", b.subjectCode, nextSeq);

                // 3) Insert BOOKS
                String sql = "INSERT INTO BOOKS(ISBN,Title,Author,SubjectCode,SubjectSeq,Description,Price,ImageURL) " +
                             "VALUES(?,?,?,?,?,?,?,?)";
                long id;
                try (PreparedStatement ins = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ins.setString(1, isbn);
                    ins.setString(2, b.title);
                    ins.setString(3, b.author);
                    ins.setInt   (4, b.subjectCode);
                    ins.setInt   (5, nextSeq);
                    ins.setString(6, b.description);
                    ins.setBigDecimal(7, b.price == null ? BigDecimal.ZERO : b.price);
                    ins.setString(8, b.imageURL);
                    ins.executeUpdate();
                    try (ResultSet gk = ins.getGeneratedKeys()) {
                        gk.next(); id = gk.getLong(1);
                    }
                }

                c.commit();
                return id;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Cập nhật đầu sách; nếu đổi Title/Author => đổi CallNumber cho toàn bộ bản sao
     *  CallNumber = XX-XX-NNN (2 ký tự đầu Title - 2 ký tự đầu Author - LPAD(CopySeq,3,'0'))
     */
    public void update(Books b, boolean renameCallNumbers) throws Exception {
        String sql = "UPDATE BOOKS SET Title=?,Author=?,SubjectCode=?,Description=?,Price=?,ImageURL=? WHERE BookID=?";
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, b.title);
                ps.setString(2, b.author);
                ps.setInt   (3, b.subjectCode);
                ps.setString(4, b.description);
                ps.setBigDecimal(5, b.price);
                ps.setString(6, b.imageURL);
                ps.setLong  (7, b.bookId);
                ps.executeUpdate();
            }

            if (renameCallNumbers) {
                // Đổi prefix theo Title/Author mới
                String up = "UPDATE BOOK_COPIES c " +
                            "SET c.CallNumber = CONCAT( UPPER(LEFT(?,2)),'-', UPPER(LEFT(?,2)),'-', LPAD(c.CopySeq,3,'0') ) " +
                            "WHERE c.BookID = ?";
                try (PreparedStatement p2 = c.prepareStatement(up)) {
                    p2.setString(1, b.title);
                    p2.setString(2, b.author);
                    p2.setLong  (3, b.bookId);
                    p2.executeUpdate();
                }
            }

            c.commit();
        }
    }

    /** Xóa nếu không còn bản sao */
    public boolean deleteIfNoCopies(long bookId) throws Exception {
        String countSql = "SELECT COUNT(*) FROM BOOK_COPIES WHERE BookID=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(countSql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return false;
            }
        }
        try (Connection c = DB.get();
             PreparedStatement del = c.prepareStatement("DELETE FROM BOOKS WHERE BookID=?")) {
            del.setLong(1, bookId);
            del.executeUpdate();
            return true;
        }
    }

    /**
     * Xóa đầu sách nếu CHƯA TỪNG được phát hành (chưa có bản ghi trong BORROW_RECORDS).
     * - Nếu chưa từng phát hành: xóa toàn bộ BOOK_COPIES thuộc sách này rồi xóa BOOKS.
     * - Nếu đã từng phát hành (có lịch sử mượn) -> trả về false, không xóa.
     */
    public boolean deleteIfNeverIssued(long bookId) throws Exception {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                // 1) Kiểm tra đã từng có borrow record nào cho các bản sao của bookId chưa
                String chk = "SELECT COUNT(*) " +
                             "FROM BORROW_RECORDS br JOIN BOOK_COPIES c ON c.CopyID = br.CopyID " +
                             "WHERE c.BookID = ?";
                int cnt;
                try (PreparedStatement ps = c.prepareStatement(chk)) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); cnt = rs.getInt(1); }
                }
                if (cnt > 0) { c.rollback(); return false; }

                // 2) Xóa toàn bộ bản sao (nếu có)
                try (PreparedStatement delC = c.prepareStatement("DELETE FROM BOOK_COPIES WHERE BookID=?")) {
                    delC.setLong(1, bookId);
                    delC.executeUpdate();
                }
                // 3) Xóa đầu sách
                try (PreparedStatement delB = c.prepareStatement("DELETE FROM BOOKS WHERE BookID=?")) {
                    delB.setLong(1, bookId);
                    delB.executeUpdate();
                }

                c.commit();
                return true;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }


 // Tìm kiếm + số bản sao (total/available), phân trang
    public List<Map<String,Object>> searchWithAvailability(
            String isbn, String author, String title,
            int page, int pageSize) throws Exception {

        String sql =
            "SELECT b.BookID, b.ISBN, b.Title, b.Author, b.Price, b.ImageURL, " +
            "       COUNT(c.CopyID) AS TotalCopies, " +
            "       COALESCE(SUM(CASE WHEN c.IsAvailable=1 AND c.Status='AVAILABLE' THEN 1 ELSE 0 END),0) AS AvailableCopies " +
            "FROM BOOKS b " +
            "LEFT JOIN BOOK_COPIES c ON c.BookID = b.BookID " +
            "WHERE (? IS NULL OR b.ISBN   LIKE CONCAT('%',?,'%')) " +
            "  AND (? IS NULL OR b.Author LIKE CONCAT('%',?,'%')) " +
            "  AND (? IS NULL OR b.Title  LIKE CONCAT('%',?,'%')) " +
            "GROUP BY b.BookID, b.ISBN, b.Title, b.Author, b.Price, b.ImageURL " +
            "ORDER BY b.Title " +
            "LIMIT ? OFFSET ?";

        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, isbn);   ps.setString(2, isbn);
            ps.setString(3, author); ps.setString(4, author);
            ps.setString(5, title);  ps.setString(6, title);
            ps.setInt(7, pageSize);
            ps.setInt(8, (page-1) * pageSize);

            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("BookID",          rs.getLong("BookID"));
                    m.put("ISBN",            rs.getString("ISBN"));
                    m.put("Title",           rs.getString("Title"));
                    m.put("Author",          rs.getString("Author"));
                    m.put("Price",           rs.getBigDecimal("Price"));
                    m.put("TotalCopies",     rs.getInt("TotalCopies"));
                    m.put("AvailableCopies", rs.getInt("AvailableCopies"));
                    m.put("ImageURL", rs.getString("ImageURL"));
                    out.add(m);
                }
            }
            return out;
        }
    }


    /** Tìm theo CallNumber (prefix) + đếm số bản sao tổng/đang rảnh, phân trang */
    public List<Map<String,Object>> searchByCallNumberWithAvailability(String callPrefix,
                                                                       int page, int pageSize) throws Exception {
        String sql =
            "SELECT b.BookID, b.ISBN, b.Title, b.Author, b.Price, b.ImageURL, " +
            "       COUNT(*) AS TotalCopies, " +
            "       SUM(c.IsAvailable=1 AND c.Status='AVAILABLE') AS AvailableCopies " +
            "FROM BOOKS b JOIN BOOK_COPIES c ON c.BookID = b.BookID " +
            "WHERE (? IS NULL OR c.CallNumber LIKE CONCAT(?, '%')) " +
            "GROUP BY b.BookID, b.ISBN, b.Title, b.Author, b.Price, b.ImageURL " +
            "ORDER BY b.Title " +
            "LIMIT ? OFFSET ?";

        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, callPrefix);
            ps.setString(2, callPrefix);
            ps.setInt(3, pageSize);
            ps.setInt(4, (page - 1) * pageSize);

            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("BookID",          rs.getLong("BookID"));
                    m.put("ISBN",            rs.getString("ISBN"));
                    m.put("Title",           rs.getString("Title"));
                    m.put("Author",          rs.getString("Author"));
                    m.put("Price",           rs.getBigDecimal("Price"));
                    m.put("TotalCopies",     rs.getInt("TotalCopies"));
                    m.put("AvailableCopies", rs.getInt("AvailableCopies"));
                    m.put("ImageURL", rs.getString("ImageURL"));
                    out.add(m);
                }
            }
            return out;
        }
    }

    /** Lấy 1 book theo ID (để BookDialog load khi edit) */
    public Books getById(long bookId) throws Exception {
        String sql = "SELECT BookID, ISBN, Title, Author, SubjectCode, Description, Price, ImageURL " +
                     "FROM BOOKS WHERE BookID=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Books b = new Books();
                b.bookId      = rs.getLong("BookID");
                b.isbn        = rs.getString("ISBN");
                b.title       = rs.getString("Title");
                b.author      = rs.getString("Author");
                b.subjectCode = rs.getInt("SubjectCode");
                b.description = rs.getString("Description");
                b.price       = rs.getBigDecimal("Price");
                b.imageURL    = rs.getString("ImageURL");
                return b;
            }
        }
    }

    /** Header info for Book detail */
    public Map<String,Object> getHeader(long bookId) throws Exception {
        String sql = "SELECT BookID, ISBN, Title, Author FROM BOOKS WHERE BookID=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("BookID", rs.getLong("BookID"));
                m.put("ISBN", rs.getString("ISBN"));
                m.put("Title", rs.getString("Title"));
                m.put("Author", rs.getString("Author"));
                return m;
            }
        }
    }

    /**
     * Copy status rows for Book detail (includes current borrower if out)
     */
    public List<Map<String,Object>> listCopyStatus(long bookId) throws Exception {
        String sql = "SELECT c.CopyID, c.CallNumber, c.Status, c.IsAvailable, " +
                     "       ls.IssueDate, br.DueDate, p.PatronID, p.Name AS PatronName " +
                     "FROM BOOK_COPIES c " +
                     "LEFT JOIN BORROW_RECORDS br ON br.CopyID = c.CopyID AND br.ReturnDate IS NULL " +
                     "LEFT JOIN LOAN_SLIPS ls ON ls.SlipID = br.SlipID " +
                     "LEFT JOIN PATRONS p ON p.PatronSysID = ls.PatronSysID " +
                     "WHERE c.BookID=? ORDER BY c.CopySeq";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            List<Map<String,Object>> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("CopyID", rs.getLong("CopyID"));
                    m.put("CallNumber", rs.getString("CallNumber"));
                    m.put("Status", rs.getString("Status"));
                    m.put("IsAvailable", rs.getBoolean("IsAvailable"));
                    m.put("IssueDate", rs.getTimestamp("IssueDate"));
                    m.put("DueDate", rs.getTimestamp("DueDate"));
                    m.put("PatronID", rs.getString("PatronID"));
                    m.put("PatronName", rs.getString("PatronName"));
                    out.add(m);
                }
            }
            return out;
        }
    }
}
