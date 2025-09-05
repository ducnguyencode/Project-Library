// DAO/SubjectDAO.java
package DAO;

import Model.Subject;
import Util.DB;
import java.sql.*;
import java.util.*;

public class SubjectDAO {
    public List<Subject> findAll() throws Exception {
        String sql = "SELECT SubjectCode, SubjectName FROM SUBJECTS ORDER BY SubjectName";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Subject> list = new ArrayList<>();
            while (rs.next()) {
                Subject s = new Subject();
                s.subjectCode = rs.getInt(1);
                s.subjectName = rs.getString(2);
                list.add(s);
            }
            return list;
        }
    }
    
 // DAO/SubjectDAO.java (bổ sung)
    public Subject insert(String name) throws Exception {
        try (Connection c = DB.get()) {
        	
            c.setAutoCommit(false);
            int next;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COALESCE(MAX(SubjectCode),0)+1 FROM SUBJECTS FOR UPDATE")) {
                try (ResultSet rs = ps.executeQuery()) { rs.next(); next = rs.getInt(1); }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO SUBJECTS(SubjectCode, SubjectName) VALUES (?,?)")) {
                ins.setInt(1, next);
                ins.setString(2, name);
                ins.executeUpdate();
            }
            c.commit();

            Subject s = new Subject();
            s.subjectCode = next;
            s.subjectName = name;
            return s;
        }
    }

}
