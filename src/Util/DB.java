package Util;

import java.sql.Connection;
import java.sql.DriverManager;

public final class DB {
    
    private static final String URL  = "jdbc:mysql://localhost:3306/LibraryManagement?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    private DB(){}
    
    public static Connection get() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
