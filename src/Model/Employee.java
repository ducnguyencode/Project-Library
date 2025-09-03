package Model;

public class Employee {
	private String employeeID;
	private String name;
	private String address;
	private String phoneNum;
	private String department;
	public Employee() {
		super();
	}
	public Employee(String employeeID, String name, String address, String phoneNum, String department) {
		super();
		this.employeeID = employeeID;
		this.name = name;
		this.address = address;
		this.phoneNum = phoneNum;
		this.department = department;
	}
	public String getEmployeeID() {
		return employeeID;
	}
	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	@Override
	public String toString() {
		return "Employee [employeeID=" + employeeID + ", name=" + name + ", address=" + address + ", phoneNum="
				+ phoneNum + ", department=" + department + "]";
	}
	
}
