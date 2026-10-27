package models.dao;

import models.entity.HoaDon;
import sql.DBConnection;

import java.sql.*;

public class HoaDonDAO {

    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (maHoaDon, maBenhAn, maKhachHang, maNhanVien, maKhuyenMai, ngayLap) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setString(2, hd.getMaBenhAn());
            stmt.setString(3, hd.getMaKhachHang());
            stmt.setString(4, hd.getMaNhanVien());
            stmt.setString(5, hd.getMaKhuyenMai());
            stmt.setDate(6, hd.getNgayLap());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}