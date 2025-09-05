// Models/Books.java
// POJO cho bảng BOOKS theo ERD v3
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
}
