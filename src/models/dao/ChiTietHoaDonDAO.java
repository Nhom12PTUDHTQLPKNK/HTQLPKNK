package models.dao;

import Audit.AuditLog;
import models.entity.ChiTietHoaDon;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietHoaDonDAO {

    public boolean insert(ChiTietHoaDon cthd) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, maVatTu, soLuong, donGia) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cthd.getMaHoaDon());
            stmt.setString(2, cthd.getMaDichVu());
            stmt.setString(3, cthd.getMaVatTu());
            stmt.setInt(4, cthd.getSoLuong());
            stmt.setDouble(5, cthd.getDonGia());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Chi tiết hóa đơn " + cthd.getMaHoaDon());
            return saved;
        }
    }

    public List<ChiTietHoaDon> getByMaHoaDon(String maHoaDon) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHoaDon);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietHoaDon(
                        rs.getString("maHoaDon"),
                        rs.getString("maDichVu"),
                        rs.getString("maVatTu"),
                        rs.getInt("soLuong"),
                        rs.getDouble("donGia")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(String maHoaDon, String maDichVu) throws SQLException {
        String sql = "DELETE FROM ChiTietHoaDon WHERE maHoaDon = ? AND maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maHoaDon);
            stmt.setString(2, maDichVu);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Chi tiết hóa đơn " + maHoaDon);
            return deleted;
        }
    }
}