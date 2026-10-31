package models.dao;

import Audit.AuditLog;
import models.entity.PhieuThanhToan;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhieuThanhToanDAO {

    public boolean insert(PhieuThanhToan ptt) {
        String sql = "INSERT INTO PhieuThanhToan (maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ptt.getMaPhieu());
            stmt.setString(2, ptt.getMaHoaDon());
            stmt.setString(3, ptt.getMaKhachHang());
            stmt.setString(4, ptt.getMaNhanVien());
            stmt.setDate(5, ptt.getNgayThanhToan() != null ? new java.sql.Date(ptt.getNgayThanhToan().getTime()) : null);
            stmt.setString(6, ptt.getHinhThucThanhToan());
            stmt.setDouble(7, ptt.getDaThanhToan() != null ? ptt.getDaThanhToan() : 0.0);
            stmt.setDouble(8, ptt.getSoTienConLai() != null ? ptt.getSoTienConLai() : 0.0);
            stmt.setString(9, ptt.getGhiChu());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("PAYMENT", "Phiếu thanh toán " + ptt.getMaPhieu());
            return saved;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<PhieuThanhToan> getAll() {
        List<PhieuThanhToan> list = new ArrayList<>();
        String sql = "SELECT * FROM PhieuThanhToan ORDER BY ngayThanhToan DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PhieuThanhToan ptt = new PhieuThanhToan();
                ptt.setMaPhieu(rs.getString("maPhieu"));
                ptt.setMaHoaDon(rs.getString("maHoaDon"));
                ptt.setMaKhachHang(rs.getString("maKhachHang"));
                ptt.setMaNhanVien(rs.getString("maNhanVien"));
                ptt.setNgayThanhToan(rs.getDate("ngayThanhToan"));
                ptt.setHinhThucThanhToan(rs.getString("hinhThucThanhToan"));
                ptt.setDaThanhToan(rs.getDouble("daThanhToan"));
                ptt.setSoTienConLai(rs.getDouble("soTienConLai"));
                ptt.setGhiChu(rs.getString("ghiChu"));
                list.add(ptt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}