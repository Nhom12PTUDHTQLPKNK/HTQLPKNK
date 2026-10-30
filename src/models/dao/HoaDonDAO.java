package models.dao;

import Audit.AuditLog;
import models.entity.HoaDon;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    public boolean insert(HoaDon hd) throws SQLException {
        String sql = "INSERT INTO HoaDon (maHoaDon, maBenhAn, maKhachHang, maNhanVien, maKhuyenMai, ngayLap) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setString(2, hd.getMaBenhAn());
            stmt.setString(3, hd.getMaKhachHang());
            stmt.setString(4, hd.getMaNhanVien());
            stmt.setString(5, hd.getMaKhuyenMai());
            stmt.setDate(6, hd.getNgayLap());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Hóa đơn " + hd.getMaHoaDon());
            return saved;
        }
    }

    public boolean update(HoaDon hd) throws SQLException {
        String sql = "UPDATE HoaDon SET maBenhAn = ?, maKhachHang = ?, maNhanVien = ?, maKhuyenMai = ?, ngayLap = ? WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hd.getMaBenhAn());
            stmt.setString(2, hd.getMaKhachHang());
            stmt.setString(3, hd.getMaNhanVien());
            stmt.setString(4, hd.getMaKhuyenMai());
            stmt.setDate(5, hd.getNgayLap());
            stmt.setString(6, hd.getMaHoaDon());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Hóa đơn " + hd.getMaHoaDon());
            return updated;
        }
    }

    public boolean delete(String maHoaDon) throws SQLException {
        String sql = "DELETE FROM HoaDon WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHoaDon);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Hóa đơn " + maHoaDon);
            return deleted;
        }
    }

    public List<HoaDon> getAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon ORDER BY ngayLap DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapHoaDon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HoaDon> search(String keyword) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM HoaDon WHERE maHoaDon LIKE ? OR maKhachHang LIKE ? OR maNhanVien LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHoaDon(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HoaDon mapHoaDon(ResultSet rs) throws SQLException {
        return new HoaDon(
            rs.getString("maHoaDon"),
            rs.getString("maBenhAn"),
            rs.getString("maKhachHang"),
            rs.getString("maNhanVien"),
            rs.getString("maKhuyenMai"),
            rs.getDate("ngayLap")
        );
    }
}