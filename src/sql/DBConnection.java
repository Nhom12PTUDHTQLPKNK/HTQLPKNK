package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Có thể cấu hình SQL Server theo môi trường chạy mà không cần sửa mã nguồn.
    private static final String URL = setting("clinic.db.url", "CLINIC_DB_URL",
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamNhaKhoa;encrypt=true;trustServerCertificate=true;loginTimeout=5;");
    private static final String USER = setting("clinic.db.user", "CLINIC_DB_USER", "sa");
    private static final String PASSWORD = setting("clinic.db.password", "CLINIC_DB_PASSWORD", "sapassword");

    private static String setting(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.trim().isEmpty()) value = System.getenv(environment);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
