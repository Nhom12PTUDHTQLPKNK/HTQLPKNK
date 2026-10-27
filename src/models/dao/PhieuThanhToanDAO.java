package models.dao;

import models.entity.PhieuThanhToan;
import sql.DBConnection;

import java.sql.*;

public class PhieuThanhToanDAO {

    public boolean insert(PhieuThanhToan ptt) {
        String sql = "INSERT INTO PhieuThanhToan (maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ptt.getMaPhieu());
            stmt.setString(2, ptt.getMaHoaDon());
            stmt.setString(3, ptt.getMaKhachHang());
            stmt.setString(4, ptt.getMaNhanVien());
            stmt.setDate(5, ptt.getNgayThanhToan());
            stmt.setString(6, ptt.getHinhThucThanhToan());
            stmt.setDouble(7, ptt.getDaThanhToan());
            stmt.setDouble(8, ptt.getSoTienConLai());
            stmt.setString(9, ptt.getGhiChu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}