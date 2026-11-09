package models.dao;

import controllers.KiemSoatTruyCap;
import controllers.NhatKyKiemToan;
import sql.DBConnection;
import java.sql.*;
import java.util.*;

/** Quản lý chức vụ và các quyền màn hình được gán cho từng chức vụ. */
public class ChucVuDAO {
    public List<String> getNames() throws SQLException {
        List<String> names = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement("SELECT tenChucVu FROM ChucVu WHERE trangThai=1 ORDER BY tenChucVu"); ResultSet r = p.executeQuery()) {
            while (r.next()) { String name=r.getString(1); if(!isAdminRole(name)) names.add(name); }
        }
        return names;
    }

    public List<Object[]> getAll() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT cv.tenChucVu, cv.moTa, cv.trangThai, COUNT(cp.maQuyen) soQuyen FROM ChucVu cv LEFT JOIN ChucVuQuyen cp ON cp.tenChucVu=cv.tenChucVu GROUP BY cv.tenChucVu,cv.moTa,cv.trangThai ORDER BY cv.tenChucVu";
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            while(r.next()) rows.add(new Object[]{r.getString(1),r.getString(2),r.getBoolean(3),r.getInt(4)});
        }
        return rows;
    }

    public Set<String> getPermissions(String name) throws SQLException {
        Set<String> result=new HashSet<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT maQuyen FROM ChucVuQuyen WHERE tenChucVu=?")) {
            p.setString(1,name); try(ResultSet r=p.executeQuery()){while(r.next()) result.add(r.getString(1));}
        }
        return result;
    }

    public boolean save(String oldName,String name,String description,Set<String> permissions) throws SQLException {
        requireAdmin();
        if(name==null || name.trim().isEmpty()) throw new SQLException("Tên chức vụ không được để trống.");
        if(isAdminRole(name) || isAdminRole(oldName)) throw new SQLException("Chức vụ Admin là cố định, không thể thêm hoặc chỉnh sửa.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if(oldName==null) {
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO ChucVu(tenChucVu,moTa,trangThai) VALUES(?,?,1)")){p.setString(1,name.trim());p.setString(2,description);p.executeUpdate();}
                } else {
                    if(!oldName.equals(name.trim())) {
                        try(PreparedStatement p=c.prepareStatement("INSERT INTO ChucVu(tenChucVu,moTa,trangThai) VALUES(?,?,1)")){p.setString(1,name.trim());p.setString(2,description);p.executeUpdate();}
                        try(PreparedStatement p=c.prepareStatement("UPDATE NhanVien SET chucVu=? WHERE chucVu=?")){p.setString(1,name.trim());p.setString(2,oldName);p.executeUpdate();}
                        try(PreparedStatement p=c.prepareStatement("UPDATE TaiKhoan SET quyen=? WHERE quyen=?")){p.setString(1,name.trim());p.setString(2,oldName);p.executeUpdate();}
                        try(PreparedStatement p=c.prepareStatement("DELETE FROM ChucVu WHERE tenChucVu=?")){p.setString(1,oldName);p.executeUpdate();}
                    } else {
                        try(PreparedStatement p=c.prepareStatement("UPDATE ChucVu SET moTa=? WHERE tenChucVu=?")){p.setString(1,description);p.setString(2,oldName);p.executeUpdate();}
                    }
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM ChucVuQuyen WHERE tenChucVu=?")){p.setString(1,name.trim());p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO ChucVuQuyen(tenChucVu,maQuyen) VALUES(?,?)")) {
                    for(String permission:permissions){p.setString(1,name.trim());p.setString(2,permission);p.addBatch();} p.executeBatch();
                }
                c.commit();
                KiemSoatTruyCap.invalidatePermissionCache();
                NhatKyKiemToan.event(oldName==null?"CREATE":"UPDATE","Chức vụ "+name.trim()); return true;
            } catch(SQLException ex){c.rollback();throw ex;} finally{c.setAutoCommit(true);}
        }
    }

    public boolean delete(String name) throws SQLException {
        requireAdmin();
        if(isAdminRole(name)) throw new SQLException("Chức vụ Admin là cố định, không thể xóa.");
        try(Connection c=DBConnection.getConnection()) {
            try(PreparedStatement p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM NhanVien WHERE chucVu=?)+(SELECT COUNT(*) FROM TaiKhoan WHERE quyen=?)")){p.setString(1,name);p.setString(2,name);try(ResultSet r=p.executeQuery()){r.next();if(r.getInt(1)>0)throw new SQLException("Không thể xóa chức vụ đang được nhân viên hoặc tài khoản sử dụng.");}}
            try(PreparedStatement p=c.prepareStatement("DELETE FROM ChucVu WHERE tenChucVu=?")){p.setString(1,name);boolean deleted=p.executeUpdate()>0;if(deleted){KiemSoatTruyCap.invalidatePermissionCache();NhatKyKiemToan.event("DELETE","Chức vụ "+name);}return deleted;}
        }
    }
    private static boolean isAdminRole(String name) { return name != null && "admin".equalsIgnoreCase(name.trim()); }
    private static void requireAdmin() throws SQLException {if(!KiemSoatTruyCap.isAdministrator())throw new SQLException("Chỉ quản trị viên được quản lý chức vụ.");}
}
