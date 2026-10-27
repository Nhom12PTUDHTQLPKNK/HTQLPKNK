package models.dao;

import models.entity.LichHen;
import sql.DBConnection;

import java.sql.*;

public class LichHenDAO {

    public boolean insert(LichHen lh) {
        String sql = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVien, ngayHen, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lh.getMaLichHen());
            stmt.setString(2, lh.getMaKhachHang());
            stmt.setString(3, lh.getMaNhanVien());
            stmt.setDate(4, lh.getNgayHen());
            stmt.setString(5, lh.getTrangThai());
            stmt.setString(6, lh.getGhiChu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOverlap(String maNhanVien, Date ngayHen) {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE maNhanVien = ? AND ngayHen = ? AND trangThai != N'Đã hủy'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            stmt.setDate(2, ngayHen);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}