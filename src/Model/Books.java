package Model;

public class Books {
	private String callNumber;
	private String ISBN;
	private String title;
	private String author;
	private boolean isAvailable;
	
	public Books() {
		super();
	}
	public Books(String callNumber, String iSBN, String title, String author, boolean isAvailable) {
		super();
		this.callNumber = callNumber;
		ISBN = iSBN;
		this.title = title;
		this.author = author;
		this.isAvailable = isAvailable;
	}
	public String getCallNumber() {
		return callNumber;
	}
	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}
	public String getISBN() {
		return ISBN;
	}
	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	@Override
	public String toString() {
		return "Books [callNumber=" + callNumber + ", ISBN=" + ISBN + ", title=" + title + ", author=" + author
				+ ", isAvailable=" + isAvailable + "]";
	}
	
}
