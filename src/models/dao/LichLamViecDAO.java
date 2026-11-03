package models.dao;

import controllers.NhatKyKiemToan;
import models.entity.LichLamViec;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichLamViecDAO {

    public String generateNextId() {
        String sql = "SELECT TOP 1 maLichLamViec FROM LichLamViec ORDER BY maLichLamViec DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString("maLichLamViec"); 
                if (lastId != null && lastId.startsWith("LLV")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(3)) + 1;
                        return String.format("LLV%03d", num);
                    } catch (NumberFormatException e) {
                       
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "LLV00"; 
    }

    // Thêm lịch làm việc
    public boolean insert(LichLamViec llv) throws SQLException {
        String sql = "INSERT INTO LichLamViec (maLichLamViec, maNhanVien, ngayLamViec, gioBatDau, gioKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, llv.getMaLichLamViec());
            stmt.setString(2, llv.getMaNhanVien());
            stmt.setDate(3, llv.getNgayLamViec());
            stmt.setString(4, llv.getGioBatDau());
            stmt.setString(5, llv.getGioKetThuc());
            stmt.setString(6, llv.getTrangThai());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) NhatKyKiemToan.event("CREATE", "Lịch làm việc " + llv.getMaLichLamViec());
            return saved;
        }
    }

    // Cập nhật lịch làm việc
    public boolean update(LichLamViec llv) throws SQLException {
        String sql = "UPDATE LichLamViec SET maNhanVien = ?, ngayLamViec = ?, gioBatDau = ?, gioKetThuc = ?, trangThai = ? WHERE maLichLamViec = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, llv.getMaNhanVien());
            stmt.setDate(2, llv.getNgayLamViec());
            stmt.setString(3, llv.getGioBatDau());
            stmt.setString(4, llv.getGioKetThuc());
            stmt.setString(5, llv.getTrangThai());
            stmt.setString(6, llv.getMaLichLamViec());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event("UPDATE", "Lịch làm việc " + llv.getMaLichLamViec());
            return updated;
        }
    }

    // Xóa lịch làm việc
    public boolean delete(String maLichLamViec) throws SQLException {
        String sql = "DELETE FROM LichLamViec WHERE maLichLamViec = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maLichLamViec);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) NhatKyKiemToan.event("DELETE", "Lịch làm việc " + maLichLamViec);
            return deleted;
        }
    }

    // Lấy danh sách toàn bộ lịch làm việc
    public List<LichLamViec> getAllLichLamViec() {
        List<LichLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM LichLamViec ORDER BY ngayLamViec DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapLichLamViec(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy lịch làm việc theo mã nhân viên
    public List<LichLamViec> getLichLamViecByNhanVien(String maNhanVien) {
        List<LichLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM LichLamViec WHERE maNhanVien = ? ORDER BY ngayLamViec DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapLichLamViec(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private LichLamViec mapLichLamViec(ResultSet rs) throws SQLException {
        return new LichLamViec(
            rs.getString("maLichLamViec"),
            rs.getString("maNhanVien"),
            rs.getDate("ngayLamViec"),
            rs.getString("gioBatDau"),
            rs.getString("gioKetThuc"),
            rs.getString("trangThai")
        );
    }
}