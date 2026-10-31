package models.dao;

import Audit.AuditLog;
import models.entity.KhuyenMai;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO {

    public boolean insert(KhuyenMai km) throws SQLException {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, km.getMaKhuyenMai());
            stmt.setString(2, km.getTenKhuyenMai());
            stmt.setDouble(3, km.getGiaTriGiam());
            stmt.setDate(4, (Date) km.getNgayBatDau());
            stmt.setDate(5, (Date) km.getNgayKetThuc());
            stmt.setBoolean(6, km.getTrangThai() != null ? km.getTrangThai() : true);
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Khuyến mãi " + km.getMaKhuyenMai());
            return saved;
        }
    }

    public boolean update(KhuyenMai km) throws SQLException {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, giaTriGiam = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, km.getTenKhuyenMai());
            stmt.setDouble(2, km.getGiaTriGiam());
            stmt.setDate(3, (Date) km.getNgayBatDau());
            stmt.setDate(4, (Date) km.getNgayKetThuc());
            stmt.setBoolean(5, km.getTrangThai() != null ? km.getTrangThai() : true);
            stmt.setString(6, km.getMaKhuyenMai());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Khuyến mãi " + km.getMaKhuyenMai());
            return updated;
        }
    }

    public boolean delete(String maKhuyenMai) throws SQLException {
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maKhuyenMai);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Khuyến mãi " + maKhuyenMai);
            return deleted;
        }
    }

    public List<KhuyenMai> getAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY maKhuyenMai";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapKhuyenMai(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<KhuyenMai> search(String keyword) {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE maKhuyenMai LIKE ? OR tenKhuyenMai LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapKhuyenMai(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private KhuyenMai mapKhuyenMai(ResultSet rs) throws SQLException {
        KhuyenMai km = new KhuyenMai();
        km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
        km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
        km.setGiaTriGiam(rs.getDouble("giaTriGiam"));
        km.setNgayBatDau(rs.getDate("ngayBatDau"));
        km.setNgayKetThuc(rs.getDate("ngayKetThuc"));
        km.setTrangThai(rs.getBoolean("trangThai"));
        return km;
    }
}