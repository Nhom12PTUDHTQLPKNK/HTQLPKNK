package models.dao;

import Audit.AuditLog;
import models.entity.KeHoachDieuTri;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KeHoachDieuTriDAO {

    public boolean insert(KeHoachDieuTri khdt) throws SQLException {
        String sql = "INSERT INTO KeHoachDieuTri (maKeHoach, maBenhAn, maNhanVien, maDichVu, ngayLap, ghiChu, soLankham, thoiGianDuKien, tongChiPhiDuKien, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, khdt.getMaKeHoach());
            stmt.setString(2, khdt.getMaBenhAn());
            stmt.setString(3, khdt.getMaNhanVien());
            stmt.setString(4, khdt.getMaDichVu());
            stmt.setDate(5, khdt.getNgayLap() != null ? new java.sql.Date(khdt.getNgayLap().getTime()) : null);
            stmt.setString(6, khdt.getGhiChu());
            stmt.setInt(7, khdt.getSoLankham() != null ? khdt.getSoLankham() : 0);
            stmt.setString(8, khdt.getThoiGianDuKien());
            stmt.setDouble(9, khdt.getTongChiPhiDuKien() != null ? khdt.getTongChiPhiDuKien() : 0.0);
            stmt.setString(10, khdt.getTrangThai());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Kế hoạch điều trị " + khdt.getMaKeHoach());
            return saved;
        }
    }

    public List<KeHoachDieuTri> getByBenhAn(String maBenhAn) {
        List<KeHoachDieuTri> list = new ArrayList<>();
        String sql = "SELECT * FROM KeHoachDieuTri WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBenhAn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    KeHoachDieuTri k = new KeHoachDieuTri();
                    k.setMaKeHoach(rs.getString("maKeHoach"));
                    k.setMaBenhAn(rs.getString("maBenhAn"));
                    k.setMaNhanVien(rs.getString("maNhanVien"));
                    k.setMaDichVu(rs.getString("maDichVu"));
                    k.setNgayLap(rs.getDate("ngayLap"));
                    k.setGhiChu(rs.getString("ghiChu"));
                    k.setSoLankham(rs.getInt("soLankham"));
                    k.setThoiGianDuKien(rs.getString("thoiGianDuKien"));
                    k.setTongChiPhiDuKien(rs.getDouble("tongChiPhiDuKien"));
                    k.setTrangThai(rs.getString("trangThai"));
                    list.add(k);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}