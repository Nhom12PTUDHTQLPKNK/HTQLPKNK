package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Cấu hình URL kết nối SQL Server (đã chỉ định database QuanLyPhongKhamNhaKhoa)
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamNhaKhoa;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}