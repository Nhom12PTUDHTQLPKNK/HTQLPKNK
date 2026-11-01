package models.dao;

import Audit.AuditLog;
import security.AccessControl;
import sql.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** SQL-backed, transactional maintenance for employee and account records. */
public class PersonnelAccountDAO {
    public List<Object[]> getAll() throws SQLException {
        requireAdmin();
        String sql = "SELECT n.maNhanVien,n.tenNhanVien,t.tenDN,t.quyen,n.sdt,n.mucLuong, " +
                "CASE WHEN t.tenDN IS NULL THEN N'Chưa cấp tài khoản' WHEN t.trangThai=1 THEN N'Hoạt động' ELSE N'Tạm khóa' END trangThai " +
                "FROM NhanVien n LEFT JOIN TaiKhoan t ON t.maNhanVien=n.maNhanVien ORDER BY n.maNhanVien";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) rows.add(new Object[]{r.getString("maNhanVien"),r.getString("tenNhanVien"),r.getString("tenDN"),
                    normalizeRole(r.getString("quyen")),r.getString("sdt"),r.getObject("mucLuong"),r.getString("trangThai")});
        }
        return rows;
    }

    public boolean save(String id, String name, String username, String password, String role,
                        String phone, double salary, boolean active, boolean editing, String oldUsername) throws SQLException {
        requireAdmin();
        if (id==null || id.trim().isEmpty() || name==null || name.trim().isEmpty() || username==null || username.trim().isEmpty())
            throw new SQLException("Mã nhân viên, họ tên và tên đăng nhập là bắt buộc.");
        try (Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (editing) {
                    try (PreparedStatement p=c.prepareStatement("UPDATE NhanVien SET tenNhanVien=?,chucVu=?,sdt=?,mucLuong=? WHERE maNhanVien=?")) {
                        p.setString(1,name.trim()); p.setString(2,role); p.setString(3,emptyToNull(phone)); p.setDouble(4,salary); p.setString(5,id.trim());
                        if (p.executeUpdate()==0) throw new SQLException("Không tìm thấy nhân viên trong SQL Server.");
                    }
                } else {
                    try (PreparedStatement p=c.prepareStatement("INSERT INTO NhanVien(maNhanVien,tenNhanVien,chucVu,sdt,mucLuong) VALUES(?,?,?,?,?)")) {
                        p.setString(1,id.trim()); p.setString(2,name.trim()); p.setString(3,role); p.setString(4,emptyToNull(phone)); p.setDouble(5,salary); p.executeUpdate();
                    }
                }
                boolean accountExists=false, protectedAccount=false;
                if (editing && oldUsername!=null && !oldUsername.trim().isEmpty()) {
                    try (PreparedStatement p=c.prepareStatement("SELECT quyen FROM TaiKhoan WHERE tenDN=? AND maNhanVien=?")) {
                        p.setString(1,oldUsername); p.setString(2,id); try(ResultSet r=p.executeQuery()){
                            accountExists=r.next();
                            if(accountExists) protectedAccount=AccessControl.isProtectedAdministrator(oldUsername,r.getString("quyen"));
                        }
                    }
                }
                if (protectedAccount && (!oldUsername.equalsIgnoreCase(username.trim())
                        || !AccessControl.isProtectedAdministrator(username,role) || !active))
                    throw new SQLException("Không được đổi tên, hạ quyền hoặc khóa tài khoản admin/quản lý.");
                if (accountExists) {
                    if (password==null || password.isEmpty()) {
                        try (PreparedStatement p=c.prepareStatement("UPDATE TaiKhoan SET tenDN=?,quyen=?,trangThai=? WHERE tenDN=? AND maNhanVien=?")) {
                            p.setString(1,username.trim()); p.setString(2,role); p.setBoolean(3,active); p.setString(4,oldUsername); p.setString(5,id); p.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement p=c.prepareStatement("UPDATE TaiKhoan SET tenDN=?,matKhau=?,quyen=?,trangThai=? WHERE tenDN=? AND maNhanVien=?")) {
                            p.setString(1,username.trim()); p.setString(2,password); p.setString(3,role); p.setBoolean(4,active); p.setString(5,oldUsername); p.setString(6,id); p.executeUpdate();
                        }
                    }
                } else {
                    if (password==null || password.trim().isEmpty()) throw new SQLException("Nhân viên chưa có tài khoản: cần nhập mật khẩu để cấp tài khoản.");
                    try (PreparedStatement p=c.prepareStatement("INSERT INTO TaiKhoan(tenDN,matKhau,quyen,trangThai,maNhanVien) VALUES(?,?,?,?,?)")) {
                        p.setString(1,username.trim()); p.setString(2,password); p.setString(3,role); p.setBoolean(4,active); p.setString(5,id.trim()); p.executeUpdate();
                    }
                }
                c.commit();
                AuditLog.event(editing ? "UPDATE" : "CREATE", "Nhân sự/tài khoản mã " + id + ", user " + username);
                return true;
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public boolean delete(String id, String username) throws SQLException {
        requireAdmin();
        try (Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String linkedUsername = null;
                try(PreparedStatement p=c.prepareStatement("SELECT tenDN,quyen FROM TaiKhoan WHERE maNhanVien=?")) {
                    p.setString(1,id); try(ResultSet r=p.executeQuery()) {
                        if(r.next()) { linkedUsername=r.getString("tenDN");
                            if (AccessControl.isCurrentAccount(linkedUsername)) throw new SQLException("Không thể tự xóa tài khoản đang đăng nhập.");
                            if (AccessControl.isProtectedAdministrator(linkedUsername,r.getString("quyen"))) throw new SQLException("Không thể xóa tài khoản admin/quản lý.");
                        }
                    }
                }
                if(linkedUsername!=null) try(PreparedStatement p=c.prepareStatement("DELETE FROM TaiKhoan WHERE tenDN=? AND maNhanVien=?")) { p.setString(1,linkedUsername); p.setString(2,id); p.executeUpdate(); }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM NhanVien WHERE maNhanVien=?")) { p.setString(1,id); if(p.executeUpdate()==0) throw new SQLException("Không tìm thấy nhân viên cần xóa."); }
                c.commit(); AuditLog.event("DELETE", "Nhân sự/tài khoản mã " + id + ", user " + username); return true;
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    private static void requireAdmin() throws SQLException { if(!AccessControl.isAdministrator()) throw new SQLException("Chỉ quản trị viên mới được quản lý nhân sự và tài khoản."); }
    private static String emptyToNull(String s) { return s==null || s.trim().isEmpty() ? null : s.trim(); }
    private static String normalizeRole(String role) {
        if(role==null) return "Lễ tân";
        String v=role.trim().toUpperCase();
        if(v.equals("ADMIN")||v.equals("QUAN_LY")||role.contains("Quản lý")) return "Admin";
        if(v.equals("NHA_SI")||role.contains("Nha sĩ")||role.contains("Bác sĩ")) return "Nha sĩ";
        if(v.equals("Y_TA")||role.contains("Y tá")||role.contains("Điều dưỡng")) return "Y tá";
        return "Lễ tân";
    }
}
