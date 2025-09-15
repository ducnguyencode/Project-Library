// Model/SystemUser.java
package Model;

import java.util.Date;

public class SystemUser {
    public long   userId;
    public String username;
    public String passwordHash;   // để verify khi cần
    public String role;           // ADMIN | LIBRARIAN
    public boolean isActive;
    public Long  staffSystemId;   // nullable
    public Date  lastLogin;       // null nếu chưa login lần nào
}
