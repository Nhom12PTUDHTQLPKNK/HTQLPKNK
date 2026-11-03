package models.dao;

import controllers.NhatKyKiemToan;
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
            if (saved) NhatKyKiemToan.event("CREATE", "Kế hoạch điều trị " + khdt.getMaKeHoach());
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
 // Lấy thông tin tổng quan và chi tiết kế hoạch điều trị theo mã bệnh án / mã lịch hẹn
    public List<Object[]> getChiTietKeHoachByBenhAn(String maBenhAn) {
        List<Object[]> list = new ArrayList<>();
        String sql = 
            "SELECT ctd.maRang, r.tinhTrang, r.chanDoan, dv.tenDichVu, ctd.soLuong, dv.donGia, (ctd.soLuong * dv.donGia) AS thanhTien " +
            "FROM ChiTietChiDinh ctd " +
            "JOIN HoSoBenhAn ba ON ctd.maBenhAn = ba.maBenhAn " +
            "LEFT JOIN ChiTietKhamRang r ON ba.maBenhAn = r.maBenhAn AND ctd.maRang = r.maRang " +
            "JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu " +
            "WHERE ba.maBenhAn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBenhAn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        false,
                        rs.getInt("maRang"),
                        rs.getString("tinhTrang"),
                        rs.getString("chanDoan"),
                        rs.getString("tenDichVu")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}