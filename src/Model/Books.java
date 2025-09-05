
package Model;

import java.math.BigDecimal;

public class Books {
    public long   bookId;        
    public String isbn;         
    public String title;         
    public String author;        
    public int    subjectCode;   
    public int    subjectSeq;    
    public String description;   
    public BigDecimal price;     
    public String imageURL;
	public Books() {
		super();
	}
	public Books(long bookId, String isbn, String title, String author, int subjectCode, int subjectSeq,
			String description, BigDecimal price, String imageURL) {
		super();
		this.bookId = bookId;
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.subjectCode = subjectCode;
		this.subjectSeq = subjectSeq;
		this.description = description;
		this.price = price;
		this.imageURL = imageURL;
	}
	public long getBookId() {
		return bookId;
	}
	public void setBookId(long bookId) {
		this.bookId = bookId;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
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
	public int getSubjectCode() {
		return subjectCode;
	}
	public void setSubjectCode(int subjectCode) {
		this.subjectCode = subjectCode;
	}
	public int getSubjectSeq() {
		return subjectSeq;
	}
	public void setSubjectSeq(int subjectSeq) {
		this.subjectSeq = subjectSeq;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	@Override
	public String toString() {
		return "Books [bookId=" + bookId + ", isbn=" + isbn + ", title=" + title + ", author=" + author
				+ ", subjectCode=" + subjectCode + ", subjectSeq=" + subjectSeq + ", description=" + description
				+ ", price=" + price + ", imageURL=" + imageURL + "]";
	}  
    
}
