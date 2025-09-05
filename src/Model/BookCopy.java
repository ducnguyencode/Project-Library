// Models/BookCopy.java
// POJO cho bảng BOOK_COPIES
package Model;

public class BookCopy {
    public long   copyId;
    public long   bookId;
    public int    copySeq;
    public String callNumber;     // XX-XX-NNN
    public boolean isAvailable;   // 1 = trong thư viện
    public String status;         // AVAILABLE|CHECKED_OUT|LOST|DAMAGED|RETIRED
    public String shelfLocation;  // vị trí kệ
}
