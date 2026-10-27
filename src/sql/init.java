package sql;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class init {

    private static final String URL = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";
    private static void initDefaultAdmin() {
        String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDN = 'admin'";
        String insertNv = "IF NOT EXISTS (SELECT 1 FROM NhanVien WHERE maNhanVien = 'NV-004') " +
                          "INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu) VALUES ('NV-004', N'BS. Phạm Quốc Dũng', N'Quản lý')";
        String insertTk = "INSERT INTO TaiKhoan (tenDN, matKhau, quyen, maNhanVien, trangThai) VALUES ('admin', '123456', 'QUAN_LY', 'NV-004', 1)";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertNv);
                stmt.executeUpdate(insertTk);
                System.out.println(">>> Đã khởi tạo tài khoản admin mặc định (admin / 123456)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        initDatabase("phongkhamnhaakhoainit.sql");
    }

    public static void initDatabase(String resourceName) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            InputStream inputStream = init.class.getResourceAsStream(resourceName);
            if (inputStream == null) {
                System.err.println("Không tìm thấy file SQL: " + resourceName);
                return;
            }

            String sqlScript;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                sqlScript = reader.lines().collect(Collectors.joining("\n"));
            }

            String[] sqlStatements = sqlScript.split("(?i)\\bGO\\b");

            for (String sql : sqlStatements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    stmt.execute(trimmedSql);
                }
            }

            System.out.println(">>> Khởi tạo cơ sở dữ liệu SQL Server thành công!");

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi khởi tạo cơ sở dữ liệu:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi đọc file script:");
            e.printStackTrace();
        }
        initDefaultAdmin();
    }
}