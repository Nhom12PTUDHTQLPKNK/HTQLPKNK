package models.dao;

import models.entity.Account;
import models.entity.Role;
import sql.DBConnection;

import java.sql.*;

public class TaiKhoanDAO {

    /**
     * Tìm thông tin tài khoản dựa trên tên đăng nhập
     */
    public Account findByUsername(String username) {
        String sql = "SELECT * FROM TaiKhoan WHERE tenDN = ? AND trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String pass = rs.getString("matKhau");
                String quyenStr = rs.getString("quyen");
                Role role = Role.fromString(quyenStr);
                
                // Trả về đối tượng Account (tên đăng nhập, mật khẩu, tên hiển thị tạm thời, phân quyền)
                return new Account(username, pass, rs.getString("maNhanVien"), role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}