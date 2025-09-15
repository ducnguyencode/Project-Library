// DAO/EmployeeDAO.java
package DAO;

import Model.Employee;
import Util.DB;

import java.sql.*;
import java.util.*;

public class EmployeeDAO {

  public List<Map<String,Object>> search(String empId, String name, int page, int pageSize) throws Exception {
    String sql = """
      SELECT SystemID, EmployeeID, Name, Address, Phone
      FROM EMPLOYEES
      WHERE (? IS NULL OR EmployeeID LIKE CONCAT('%',?,'%'))
        AND (? IS NULL OR Name      LIKE CONCAT('%',?,'%'))
      ORDER BY Name
      LIMIT ? OFFSET ?""";
    try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, empId); ps.setString(2, empId);
      ps.setString(3, name ); ps.setString(4, name );
      ps.setInt(5, pageSize);
      ps.setInt(6, (page-1)*pageSize);
      try (ResultSet rs = ps.executeQuery()) {
        List<Map<String,Object>> list = new ArrayList<>();
        while (rs.next()) {
          Map<String,Object> m = new LinkedHashMap<>();
          m.put("SystemID",   rs.getLong("SystemID"));
          m.put("EmployeeID", rs.getString("EmployeeID"));
          m.put("Name",       rs.getString("Name"));
          m.put("Address",    rs.getString("Address"));
          m.put("Phone",      rs.getString("Phone"));
          list.add(m);
        }
        return list;
      }
    }
  }

  public long insertAuto(Employee e) throws Exception {
    String sql = "INSERT INTO EMPLOYEES(EmployeeID,Name,Address,Phone) VALUES(?,?,?,?)";
    try (Connection c = DB.get();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, e.getEmployeeID());
      ps.setString(2, e.getName());
      ps.setString(3, e.getAddress());
      ps.setString(4, e.getPhoneNum());
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getLong(1); }
    }
  }

  public Employee get(long systemId) throws Exception {
    String sql = "SELECT SystemID,EmployeeID,Name,Address,Phone FROM EMPLOYEES WHERE SystemID=?";
    try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, systemId);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        Employee e = new Employee();
        e.setSystemId(rs.getLong("SystemID"));
        e.setEmployeeID(rs.getString("EmployeeID"));
        e.setName(rs.getString("Name"));
        e.setAddress(rs.getString("Address"));
        e.setPhoneNum(rs.getString("Phone"));
        return e;
      }
    }
  }

  public void update(Employee e) throws Exception {
    String sql = "UPDATE EMPLOYEES SET EmployeeID=?, Name=?, Address=?, Phone=? WHERE SystemID=?";
    try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, e.getEmployeeID());
      ps.setString(2, e.getName());
      ps.setString(3, e.getAddress());
      ps.setString(4, e.getPhoneNum());
      ps.setLong  (5, e.getSystemId());
      ps.executeUpdate();
    }
  }

  /** Chỉ xóa nếu KHÔNG có user hệ thống tham chiếu (SYSTEM_USERS.StaffSystemID) */
  public boolean deleteIfNoUser(long systemId) throws Exception {
    String chk = "SELECT 1 FROM SYSTEM_USERS WHERE StaffSystemID=? LIMIT 1";
    try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(chk)) {
      ps.setLong(1, systemId);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return false; }
    }
    try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(
            "DELETE FROM EMPLOYEES WHERE SystemID=?")) {
      ps.setLong(1, systemId);
      ps.executeUpdate();
      return true;
    }
  }
}
