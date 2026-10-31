package models.dao;

import Audit.AuditLog;
import models.entity.HoSoBenhAn;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoSoBenhAnDAO {

    public boolean insert(HoSoBenhAn hs) throws SQLException {
        String sql = "INSERT INTO HoSoBenhAn (maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hs.getMaBenhAn());
            stmt.setString(2, hs.getMaKhachHang());
            stmt.setString(3, hs.getMaNhanVien());
            stmt.setDate(4, hs.getNgayKham() != null ? new java.sql.Date(hs.getNgayKham().getTime()) : null);
            stmt.setString(5, hs.getKetQuaKham());
            stmt.setString(6, hs.getChuanDoan());
            stmt.setString(7, hs.getTrangThai());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Bệnh án " + hs.getMaBenhAn());
            return saved;
        }
    }

    public boolean update(HoSoBenhAn hs) throws SQLException {
        String sql = "UPDATE HoSoBenhAn SET maKhachHang = ?, maNhanVien = ?, ngayKham = ?, ketQuaKham = ?, chuanDoan = ?, trangThai = ? WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hs.getMaKhachHang());
            stmt.setString(2, hs.getMaNhanVien());
            stmt.setDate(3, hs.getNgayKham() != null ? new java.sql.Date(hs.getNgayKham().getTime()) : null);
            stmt.setString(4, hs.getKetQuaKham());
            stmt.setString(5, hs.getChuanDoan());
            stmt.setString(6, hs.getTrangThai());
            stmt.setString(7, hs.getMaBenhAn());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Bệnh án " + hs.getMaBenhAn());
            return updated;
        }
    }

    public boolean delete(String maBenhAn) throws SQLException {
        String sql = "DELETE FROM HoSoBenhAn WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBenhAn);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Bệnh án " + maBenhAn);
            return deleted;
        }
    }

    public boolean updateStatus(String maBenhAn, String trangThaiMoi) {
        String sql = "UPDATE HoSoBenhAn SET trangThai = ? WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThaiMoi);
            stmt.setString(2, maBenhAn);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Trạng thái bệnh án " + maBenhAn);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<HoSoBenhAn> getAll() {
        List<HoSoBenhAn> list = new ArrayList<>();
        String sql = "SELECT * FROM HoSoBenhAn ORDER BY ngayKham DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapHoSoBenhAn(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<HoSoBenhAn> search(String keyword) {
        List<HoSoBenhAn> list = new ArrayList<>();
        String sql = "SELECT * FROM HoSoBenhAn WHERE maBenhAn LIKE ? OR maKhachHang LIKE ? OR chuanDoan LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHoSoBenhAn(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HoSoBenhAn mapHoSoBenhAn(ResultSet rs) throws SQLException {
        HoSoBenhAn hs = new HoSoBenhAn();
        hs.setMaBenhAn(rs.getString("maBenhAn"));
        hs.setMaKhachHang(rs.getString("maKhachHang"));
        hs.setMaNhanVien(rs.getString("maNhanVien"));
        hs.setNgayKham(rs.getDate("ngayKham"));
        hs.setKetQuaKham(rs.getString("ketQuaKham"));
        hs.setChuanDoan(rs.getString("chuanDoan"));
        hs.setTrangThai(rs.getString("trangThai"));
        return hs;
    }
}