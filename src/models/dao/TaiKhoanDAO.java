package models.dao;

import models.entity.Account;
import models.entity.Role;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    /**
     * Tìm thông tin tài khoản linh hoạt (Hỗ trợ tìm theo tenDN, maNhanVien hoặc tenNhanVien)
     */
    public Account findByUsername(String input) {
        String sql = "SELECT t.* FROM TaiKhoan t " +
                     "LEFT JOIN NhanVien n ON t.maNhanVien = n.maNhanVien " +
                     "WHERE t.tenDN = ? OR t.maNhanVien = ? OR n.tenNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, input);
            stmt.setString(2, input);
            stmt.setString(3, input);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("tenDN");
                String pass = rs.getString("matKhau");
                String quyenStr = rs.getString("quyen");
                Role role = Role.fromString(quyenStr);
                
                return new Account(username, pass, rs.getString("maNhanVien"), role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object[]> getAllAccounts() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT tenDN, maNhanVien, quyen, trangThai FROM TaiKhoan";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String u = rs.getString("tenDN");
                String id = rs.getString("maNhanVien");
                String r = rs.getString("quyen");
                boolean statusBit = rs.getBoolean("trangThai");
                String s = statusBit ? "Hoạt động" : "Tạm khóa";
                list.add(new Object[]{u, id != null ? id : "", r, s});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertAccount(String tenDN, String matKhau, String quyen, boolean trangThai, String maNhanVien) {
        String sql = "INSERT INTO TaiKhoan (tenDN, matKhau, quyen, trangThai, maNhanVien) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDN);
            stmt.setString(2, (matKhau == null || matKhau.trim().isEmpty()) ? "123456" : matKhau);
            stmt.setString(3, quyen);
            stmt.setBoolean(4, trangThai);
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, maNhanVien);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAccount(String tenDN, String matKhau, String quyen, boolean trangThai, String maNhanVien, boolean updatePassword) {
        String sql;
        if (updatePassword) {
            sql = "UPDATE TaiKhoan SET matKhau = ?, quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ?";
        } else {
            sql = "UPDATE TaiKhoan SET quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (updatePassword) {
                stmt.setString(1, matKhau);
                stmt.setString(2, quyen);
                stmt.setBoolean(3, trangThai);
                if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                    stmt.setNull(4, Types.VARCHAR);
                } else {
                    stmt.setString(4, maNhanVien);
                }
                stmt.setString(5, tenDN);
            } else {
                stmt.setString(1, quyen);
                stmt.setBoolean(2, trangThai);
                if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                    stmt.setNull(3, Types.VARCHAR);
                } else {
                    stmt.setString(3, maNhanVien);
                }
                stmt.setString(4, tenDN);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAccount(String tenDN) {
        String sql = "DELETE FROM TaiKhoan WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDN);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updatePassword(String tenDN, String newMatKhau) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newMatKhau);
            stmt.setString(2, tenDN);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}