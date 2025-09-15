// Model/Patron.java
package Model;

public class Patron {
    public long   patronSysId;  // PK
    public String patronId;     // mã độc giả hiển thị (unique)
    public String name;
    public String address;
    public String phone;
    public String email;
    public boolean isActive;
}
