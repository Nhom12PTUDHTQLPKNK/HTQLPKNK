package models.dao;

import Audit.AuditLog;
import models.entity.LichHen;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichLamViecDAO {

    // Thêm lịch hẹn mới
    public boolean insert(LichHen lh) throws SQLException {
        String sql = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVien, ngayHen, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lh.getMaLichHen());
            stmt.setString(2, lh.getMaKhachHang());
            stmt.setString(3, lh.getMaNhanVien());
            stmt.setDate(4, lh.getNgayHen());
            stmt.setString(5, lh.getTrangThai());
            stmt.setString(6, lh.getGhiChu());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Lịch hẹn " + lh.getMaLichHen());
            return saved;
        }
    }

    // Cập nhật thông tin lịch hẹn
    public boolean update(LichHen lh) throws SQLException {
        String sql = "UPDATE LichHen SET maKhachHang = ?, maNhanVien = ?, ngayHen = ?, trangThai = ?, ghiChu = ? WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lh.getMaKhachHang());
            stmt.setString(2, lh.getMaNhanVien());
            stmt.setDate(3, lh.getNgayHen());
            stmt.setString(4, lh.getTrangThai());
            stmt.setString(5, lh.getGhiChu());
            stmt.setString(6, lh.getMaLichHen());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Lịch hẹn " + lh.getMaLichHen());
            return updated;
        }
    }

    // Xóa lịch hẹn
    public boolean delete(String maLichHen) throws SQLException {
        String sql = "DELETE FROM LichHen WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maLichHen);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Lịch hẹn " + maLichHen);
            return deleted;
        }
    }

    // Lấy toàn bộ danh sách lịch hẹn
    public List<LichHen> getAllLichHen() {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT * FROM LichHen ORDER BY ngayHen DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapLichHen(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tìm kiếm lịch hẹn theo mã lịch hẹn hoặc mã khách hàng
    public List<LichHen> search(String keyword) {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT * FROM LichHen WHERE maLichHen LIKE ? OR maKhachHang LIKE ? OR trangThai LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLichHen(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private LichHen mapLichHen(ResultSet rs) throws SQLException {
        return new LichHen(
            rs.getString("maLichHen"),
            rs.getString("maKhachHang"),
            rs.getString("maNhanVien"),
            rs.getDate("ngayHen"),
            rs.getString("trangThai"),
            rs.getString("ghiChu")
        );
    }
}