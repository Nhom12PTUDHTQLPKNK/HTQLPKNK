package models.dao;

import Audit.AuditLog;
import models.entity.DichVu;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public boolean insert(DichVu dv) throws SQLException {
        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh, moTa) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dv.getMaDichVu());
            stmt.setString(2, dv.getTenDichVu());
            stmt.setString(3, dv.getLoaiDieuTri());
            stmt.setDouble(4, dv.getDonGia());
            stmt.setString(5, dv.getDonViTinh());
            stmt.setString(6, dv.getMoTa());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Dịch vụ " + dv.getMaDichVu());
            return saved;
        }
    }

    public boolean update(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET tenDichVu = ?, loaiDieuTri = ?, donGia = ?, donViTinh = ?, moTa = ? WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dv.getTenDichVu());
            stmt.setString(2, dv.getLoaiDieuTri());
            stmt.setDouble(3, dv.getDonGia());
            stmt.setString(4, dv.getDonViTinh());
            stmt.setString(5, dv.getMoTa());
            stmt.setString(6, dv.getMaDichVu());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Dịch vụ " + dv.getMaDichVu());
            return updated;
        }
    }

    public boolean delete(String maDichVu) throws SQLException {
        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maDichVu);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Dịch vụ " + maDichVu);
            return deleted;
        }
    }

    public List<DichVu> getAllDichVu() {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu ORDER BY maDichVu";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapDichVu(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DichVu> search(String keyword) {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu WHERE maDichVu LIKE ? OR tenDichVu LIKE ? OR loaiDieuTri LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDichVu(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private DichVu mapDichVu(ResultSet rs) throws SQLException {
        return new DichVu(
            rs.getString("maDichVu"),
            rs.getString("tenDichVu"),
            rs.getString("loaiDieuTri"),
            rs.getDouble("donGia"),
            rs.getString("donViTinh"),
            rs.getString("moTa")
        );
    }
}