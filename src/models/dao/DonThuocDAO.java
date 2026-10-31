package models.dao;

import Audit.AuditLog;
import models.entity.DonThuoc;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonThuocDAO {

    public boolean insert(DonThuoc dt) throws SQLException {
        String sql = "INSERT INTO DonThuoc (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, ghiChu, soLuong, cachDung) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dt.getMaDonThuoc());
            stmt.setString(2, dt.getMaBenhAn());
            stmt.setString(3, dt.getMaNhanVien());
            stmt.setString(4, dt.getMaKhachHang());
            stmt.setString(5, dt.getMaThuoc());
            stmt.setString(6, dt.getTenThuoc());
            stmt.setDate(7, dt.getNgayKe() != null ? new java.sql.Date(dt.getNgayKe().getTime()) : null);
            stmt.setString(8, dt.getGhiChu());
            stmt.setInt(9, dt.getSoLuong());
            stmt.setString(10, dt.getCachDung());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Đơn thuốc " + dt.getMaDonThuoc());
            return saved;
        }
    }

    public List<DonThuoc> getByBenhAn(String maBenhAn) {
        List<DonThuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBenhAn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DonThuoc dt = new DonThuoc(
                        rs.getString("maDonThuoc"),
                        rs.getString("maBenhAn"),
                        rs.getString("maNhanVien"),
                        rs.getString("maKhachHang"),
                        rs.getString("maThuoc"),
                        rs.getString("tenThuoc"),
                        rs.getDate("ngayKe"),
                        rs.getString("ghiChu"),
                        rs.getInt("soLuong"),
                        rs.getString("cachDung")
                    );
                    list.add(dt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}