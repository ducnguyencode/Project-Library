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
	public BookCopy() {
		super();
	}
	public BookCopy(long copyId, long bookId, int copySeq, String callNumber, boolean isAvailable, String status,
			String shelfLocation) {
		super();
		this.copyId = copyId;
		this.bookId = bookId;
		this.copySeq = copySeq;
		this.callNumber = callNumber;
		this.isAvailable = isAvailable;
		this.status = status;
		this.shelfLocation = shelfLocation;
	}
	public long getCopyId() {
		return copyId;
	}
	public void setCopyId(long copyId) {
		this.copyId = copyId;
	}
	public long getBookId() {
		return bookId;
	}
	public void setBookId(long bookId) {
		this.bookId = bookId;
	}
	public int getCopySeq() {
		return copySeq;
	}
	public void setCopySeq(int copySeq) {
		this.copySeq = copySeq;
	}
	public String getCallNumber() {
		return callNumber;
	}
	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getShelfLocation() {
		return shelfLocation;
	}
	public void setShelfLocation(String shelfLocation) {
		this.shelfLocation = shelfLocation;
	}
	@Override
	public String toString() {
		return "BookCopy [copyId=" + copyId + ", bookId=" + bookId + ", copySeq=" + copySeq + ", callNumber="
				+ callNumber + ", isAvailable=" + isAvailable + ", status=" + status + ", shelfLocation="
				+ shelfLocation + "]";
	}
    
}
