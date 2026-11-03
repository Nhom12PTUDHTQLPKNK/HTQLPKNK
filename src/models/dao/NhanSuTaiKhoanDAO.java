package models.dao;

import controllers.NhatKyKiemToan;
import controllers.KiemSoatTruyCap;
import sql.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** SQL-backed, transactional maintenance for employee and account records. */
public class NhanSuTaiKhoanDAO {
    public List<Object[]> getAll() throws SQLException {
        String sql = "SELECT n.maNhanVien, n.tenNhanVien, t.tenDN, t.quyen, n.sdt, n.mucLuong, " +
                "CASE WHEN t.tenDN IS NULL THEN N'Chưa cấp tài khoản' WHEN t.trangThai=1 THEN N'Hoạt động' ELSE N'Tạm khóa' END trangThai " +
                "FROM NhanVien n LEFT JOIN TaiKhoan t ON t.maNhanVien=n.maNhanVien ORDER BY n.maNhanVien";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while(r.next()) {
                rows.add(new Object[]{
                    r.getString("maNhanVien"),
                    r.getString("tenNhanVien"),
                    r.getString("tenDN"),
                    normalizeRole(r.getString("quyen")),
                    r.getString("sdt"),
                    r.getObject("mucLuong"),
                    r.getString("trangThai")
                });
            }
        }
        return rows;
    }

    public boolean save(String id, String name, String username, String password, String role,
                        String phone, double salary, boolean active, boolean editing, String oldUsername) throws SQLException {
        if (id == null || id.trim().isEmpty() || name == null || name.trim().isEmpty() || username == null || username.trim().isEmpty())
            throw new SQLException("Mã nhân viên, họ tên và tên đăng nhập là bắt buộc.");
            
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. Kiểm tra xem nhân viên đã tồn tại chưa để Insert hoặc Update bảng NhanVien
                boolean employeeExists = false;
                try (PreparedStatement checkEmp = c.prepareStatement("SELECT COUNT(*) FROM NhanVien WHERE maNhanVien = ?")) {
                    checkEmp.setString(1, id.trim());
                    try (ResultSet rs = checkEmp.executeQuery()) {
                        if (rs.next()) employeeExists = rs.getInt(1) > 0;
                    }
                }

                if (employeeExists) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE NhanVien SET tenNhanVien = ?, chucVu = ?, sdt = ?, mucLuong = ? WHERE maNhanVien = ?")) {
                        p.setString(1, name.trim()); 
                        p.setString(2, role); 
                        p.setString(3, emptyToNull(phone)); 
                        p.setDouble(4, salary); 
                        p.setString(5, id.trim());
                        p.executeUpdate();
                    }
                } else {
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu, sdt, mucLuong) VALUES (?, ?, ?, ?, ?)")) {
                        p.setString(1, id.trim()); 
                        p.setString(2, name.trim()); 
                        p.setString(3, role); 
                        p.setString(4, emptyToNull(phone)); 
                        p.setDouble(5, salary); 
                        p.executeUpdate();
                    }
                }

                // 2. Kiểm tra xem tài khoản đã tồn tại trong bảng TaiKhoan chưa
                boolean accountExists = false;
                String queryUser = (editing && oldUsername != null && !oldUsername.trim().isEmpty()) ? oldUsername : username.trim();
                
                try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM TaiKhoan WHERE tenDN = ? OR maNhanVien = ?")) {
                    p.setString(1, queryUser);
                    p.setString(2, id.trim());
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) accountExists = r.getInt(1) > 0;
                    }
                }

                // 3. Thực hiện Insert hoặc Update bảng TaiKhoan
                if (accountExists) {
                    if (password == null || password.isEmpty()) {
                        try (PreparedStatement p = c.prepareStatement("UPDATE TaiKhoan SET tenDN = ?, quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ? OR maNhanVien = ?")) {
                            p.setString(1, username.trim()); 
                            p.setString(2, role); 
                            p.setBoolean(3, active); 
                            p.setString(4, id.trim());
                            p.setString(5, queryUser);
                            p.setString(6, id.trim());
                            p.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement p = c.prepareStatement("UPDATE TaiKhoan SET tenDN = ?, matKhau = ?, quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ? OR maNhanVien = ?")) {
                            p.setString(1, username.trim()); 
                            p.setString(2, password); 
                            p.setString(3, role); 
                            p.setBoolean(4, active); 
                            p.setString(5, id.trim());
                            p.setString(6, queryUser);
                            p.setString(7, id.trim());
                            p.executeUpdate();
                        }
                    }
                } else {
                    String passToUse = (password == null || password.trim().isEmpty()) ? "123456" : password;
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO TaiKhoan (tenDN, matKhau, quyen, trangThai, maNhanVien) VALUES (?, ?, ?, ?, ?)")) {
                        p.setString(1, username.trim()); 
                        p.setString(2, passToUse); 
                        p.setString(3, role); 
                        p.setBoolean(4, active); 
                        p.setString(5, id.trim());
                        p.executeUpdate();
                    }
                }

                c.commit();
                NhatKyKiemToan.event(editing ? "UPDATE" : "CREATE", "Nhân sự/tài khoản mã " + id + ", user " + username);
                return true;
            } catch (SQLException ex) { 
                c.rollback(); 
                throw ex; 
            } finally { 
                c.setAutoCommit(true); 
            }
        }
    }

    public boolean delete(String id, String username) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (username != null) {
                    try (PreparedStatement p = c.prepareStatement("DELETE FROM TaiKhoan WHERE tenDN = ? OR maNhanVien = ?")) { 
                        p.setString(1, username); 
                        p.setString(2, id);
                        p.executeUpdate(); 
                    }
                }
                try (PreparedStatement p = c.prepareStatement("DELETE FROM NhanVien WHERE maNhanVien = ?")) { 
                    p.setString(1, id); 
                    p.executeUpdate(); 
                }
                c.commit(); 
                NhatKyKiemToan.event("DELETE", "Nhân sự/tài khoản mã " + id + ", user " + username); 
                return true;
            } catch (SQLException ex) { 
                c.rollback(); 
                throw ex; 
            } finally { 
                c.setAutoCommit(true); 
            }
        }
    }

    private static String emptyToNull(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    
    private static String normalizeRole(String role) {
        if (role == null) return "Lễ tân";
        String v = role.trim().toUpperCase();
        if (v.equals("ADMIN") || v.equals("QUAN_LY") || role.contains("Quản lý")) return "Admin";
        if (v.equals("NHA_SI") || role.contains("Nha sĩ") || role.contains("Bác sĩ")) return "Nha sĩ";
        if (v.equals("Y_TA") || role.contains("Y tá") || role.contains("Điều dưỡng")) return "Y tá";
        return "Lễ tân";
    }
}