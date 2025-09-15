// Model/Employee.java
package Model;

public class Employee {
    private Long   systemId;    // SystemID trong DB, có thể null khi thêm mới
    private String employeeID;  // mã hiển thị (E001…)
    private String name;
    private String address;
    private String phoneNum;    // giữ đúng tên getter/setter đang dùng ở UI

    public Employee() {}

    public Employee(String employeeID, String name, String address, String phoneNum) {
        this.employeeID = employeeID;
        this.name       = name;
        this.address    = address;
        this.phoneNum   = phoneNum;
    }

    public Long   getSystemId()         { return systemId; }
    public void   setSystemId(Long id)  { this.systemId = id; }

    public String getEmployeeID()       { return employeeID; }
    public void   setEmployeeID(String s){ this.employeeID = s; }

    public String getName()             { return name; }
    public void   setName(String s)     { this.name = s; }

    public String getAddress()          { return address; }
    public void   setAddress(String s)  { this.address = s; }

    public String getPhoneNum()         { return phoneNum; }
    public void   setPhoneNum(String s) { this.phoneNum = s; }
}
