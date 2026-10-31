package models.dao;

import Audit.AuditLog;
import models.entity.KhachHang;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public boolean insert(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (maKhachHang, tenKH, gioiTinh, diaChi, ngaySinh, sdt, tienSuBenh, diemTichLuy) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKhachHang());
            stmt.setString(2, kh.getTenKH());
            stmt.setString(3, kh.getGioiTinh());
            stmt.setString(4, kh.getDiaChi());
            stmt.setDate(5, (Date) kh.getNgaySinh());
            stmt.setString(6, kh.getSdt());
            stmt.setString(7, kh.getTienSuBenh());
            stmt.setInt(8, kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0);
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Khách hàng " + kh.getMaKhachHang());
            return saved;
        }
    }

    public boolean update(KhachHang kh) throws SQLException {
        String sql = "UPDATE KhachHang SET tenKH = ?, gioiTinh = ?, diaChi = ?, ngaySinh = ?, sdt = ?, tienSuBenh = ?, diemTichLuy = ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kh.getTenKH());
            stmt.setString(2, kh.getGioiTinh());
            stmt.setString(3, kh.getDiaChi());
            stmt.setDate(4, (Date) kh.getNgaySinh());
            stmt.setString(5, kh.getSdt());
            stmt.setString(6, kh.getTienSuBenh());
            stmt.setInt(7, kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0);
            stmt.setString(8, kh.getMaKhachHang());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Khách hàng " + kh.getMaKhachHang());
            return updated;
        }
    }

    public boolean delete(String maKhachHang) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maKhachHang);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Khách hàng " + maKhachHang);
            return deleted;
        }
    }

    public List<KhachHang> findAll() throws SQLException {
        String sql = "SELECT maKhachHang, tenKH, gioiTinh, diaChi, ngaySinh, sdt, tienSuBenh, diemTichLuy " +
                     "FROM KhachHang ORDER BY maKhachHang";
        List<KhachHang> customers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        }
        return customers;
    }

    public KhachHang findByPhoneOrId(String keyword) throws SQLException {
        String sql = "SELECT maKhachHang, tenKH, gioiTinh, diaChi, ngaySinh, sdt, tienSuBenh, diemTichLuy " +
                     "FROM KhachHang WHERE maKhachHang = ? OR sdt = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapCustomer(rs) : null;
            }
        }
    }

    private KhachHang mapCustomer(ResultSet rs) throws SQLException {
        return new KhachHang(
                rs.getString("maKhachHang"),
                rs.getString("tenKH"),
                rs.getString("gioiTinh"),
                rs.getString("diaChi"),
                rs.getDate("ngaySinh"),
                rs.getString("sdt"),
                rs.getString("tienSuBenh"),
                rs.getInt("diemTichLuy")
        );
    }
}