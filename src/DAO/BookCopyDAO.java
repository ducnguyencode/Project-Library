// DAO/BookCopyDAO.java
package DAO;

import Model.BookCopy;
import Util.DB;

import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class BookCopyDAO {

    /** Bỏ dấu + lấy 2 chữ cái đầu (A-Z). Nếu thiếu thì pad 'X' */
    private static String twoAsciiLetters(String s){
        if (s == null) s = "";
        String no = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}","")              // bỏ dấu
                .replaceAll("[^A-Za-z]"," ");          // chỉ giữ chữ
        StringBuilder out = new StringBuilder(2);
        for (int i = 0; i < no.length() && out.length() < 2; i++){
            char ch = no.charAt(i);
            if (Character.isLetter(ch)) out.append(Character.toUpperCase(ch));
        }
        while (out.length() < 2) out.append('X');
        return out.substring(0,2);
    }

    /** Tạo 1 bản sao tự động. */
    public BookCopy insertAuto(long bookId) throws Exception {
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                String title, author;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT Title, Author FROM BOOKS WHERE BookID=? FOR UPDATE")) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Book not found");
                        title = rs.getString(1);
                        author = rs.getString(2);
                    }
                }

                int nextSeq;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(MAX(CopySeq),0)+1 FROM BOOK_COPIES WHERE BookID=? FOR UPDATE")) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); nextSeq = rs.getInt(1); }
                }

                String call = twoAsciiLetters(title) + "-" + twoAsciiLetters(author) + "-" +
                              String.format("%03d", nextSeq);

                long newId;
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO BOOK_COPIES(BookID, CopySeq, CallNumber, IsAvailable, Status) " +
                        "VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ins.setLong(1, bookId);
                    ins.setInt(2, nextSeq);
                    ins.setString(3, call);
                    ins.setBoolean(4, true);
                    ins.setString(5, "AVAILABLE");
                    ins.executeUpdate();
                    try (ResultSet gk = ins.getGeneratedKeys()) { gk.next(); newId = gk.getLong(1); }
                }

                c.commit();
                BookCopy cp = new BookCopy();
                cp.copyId = newId; cp.bookId = bookId; cp.copySeq = nextSeq;
                cp.callNumber = call; cp.isAvailable = true; cp.status = "AVAILABLE";
                return cp;
            } catch (Exception ex) {
                c.rollback(); throw ex;
            } finally { c.setAutoCommit(true); }
        }
    }

    /** Tạo N bản sao tự động, trả về số bản tạo được. */
    public int insertMany(long bookId, int qty) throws Exception {
        if (qty <= 0) return 0;
        try (Connection c = DB.get()) {
            c.setAutoCommit(false);
            try {
                String title, author;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT Title, Author FROM BOOKS WHERE BookID=? FOR UPDATE")) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Book not found");
                        title = rs.getString(1);
                        author = rs.getString(2);
                    }
                }

                int base;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(MAX(CopySeq),0) FROM BOOK_COPIES WHERE BookID=? FOR UPDATE")) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); base = rs.getInt(1); }
                }

                String t2 = twoAsciiLetters(title), a2 = twoAsciiLetters(author);
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO BOOK_COPIES(BookID, CopySeq, CallNumber, IsAvailable, Status) " +
                        "VALUES(?,?,?,?,?)")) {
                    for (int i = 1; i <= qty; i++) {
                        int seq = base + i;
                        String call = t2 + "-" + a2 + "-" + String.format("%03d", seq);
                        ins.setLong(1, bookId);
                        ins.setInt(2, seq);
                        ins.setString(3, call);
                        ins.setBoolean(4, true);
                        ins.setString(5, "AVAILABLE");
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }

                c.commit();
                return qty;
            } catch (Exception ex) {
                c.rollback(); throw ex;
            } finally { c.setAutoCommit(true); }
        }
    }

    /** Đổi trạng thái bản sao. AVAILABLE => IsAvailable=1, khác => 0. */
    public void changeStatus(long copyId, String newStatus) throws Exception {
        String sql = "UPDATE BOOK_COPIES " +
                     "SET Status=?, IsAvailable=(?='AVAILABLE') " +
                     "WHERE CopyID=?";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setLong(3, copyId);
            ps.executeUpdate();
        }
    }

    /** Xóa nếu không có borrow mở (ReturnDate IS NULL). */
    public boolean deleteIfNoOpenLoan(long copyId) throws Exception {
        String chk = "SELECT COUNT(*) FROM BORROW_RECORDS WHERE CopyID=? AND ReturnDate IS NULL";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(chk)) {
            ps.setLong(1, copyId);
            try (ResultSet rs = ps.executeQuery()) { rs.next();
                if (rs.getInt(1) > 0) return false;
            }
        }
        try (Connection c = DB.get();
             PreparedStatement del = c.prepareStatement("DELETE FROM BOOK_COPIES WHERE CopyID=?")) {
            del.setLong(1, copyId);
            del.executeUpdate();
            return true;
        }
    }

    public List<BookCopy> findByBook(long bookId) throws Exception {
        String sql = "SELECT CopyID,BookID,CopySeq,CallNumber,IsAvailable,Status,ShelfLocation " +
                     "FROM BOOK_COPIES WHERE BookID=? ORDER BY CopySeq";
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                List<BookCopy> list = new ArrayList<>();
                while (rs.next()) {
                    BookCopy cp = new BookCopy();
                    cp.copyId = rs.getLong(1);
                    cp.bookId = rs.getLong(2);
                    cp.copySeq = rs.getInt(3);
                    cp.callNumber = rs.getString(4);
                    cp.isAvailable = rs.getBoolean(5);
                    cp.status = rs.getString(6);
                    cp.shelfLocation = rs.getString(7);
                    list.add(cp);
                }
                return list;
            }
        }
    }
}
