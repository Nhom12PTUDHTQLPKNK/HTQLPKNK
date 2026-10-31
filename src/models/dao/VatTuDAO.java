package models.dao;

import Audit.AuditLog;
import models.entity.VatTu;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VatTuDAO {

    public boolean insert(VatTu vt) throws SQLException {
        String sql = "INSERT INTO VatTu (maVatTu, tenVatTu, donViTinh, soLuongTon, ngaySuDung) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vt.getMaVatTu());
            stmt.setString(2, vt.getTenVatTu());
            stmt.setString(3, vt.getDonViTinh());
            stmt.setInt(4, vt.getSoLuongTon() != null ? vt.getSoLuongTon() : 0);
            stmt.setDate(5, (Date) vt.getNgaySuDung());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Vật tư " + vt.getMaVatTu());
            return saved;
        }
    }

    public boolean update(VatTu vt) throws SQLException {
        String sql = "UPDATE VatTu SET tenVatTu = ?, donViTinh = ?, soLuongTon = ?, ngaySuDung = ? WHERE maVatTu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vt.getTenVatTu());
            stmt.setString(2, vt.getDonViTinh());
            stmt.setInt(3, vt.getSoLuongTon() != null ? vt.getSoLuongTon() : 0);
            stmt.setDate(4, (Date) vt.getNgaySuDung());
            stmt.setString(5, vt.getMaVatTu());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Vật tư " + vt.getMaVatTu());
            return updated;
        }
    }

    public boolean delete(String maVatTu) throws SQLException {
        String sql = "DELETE FROM VatTu WHERE maVatTu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maVatTu);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Vật tư " + maVatTu);
            return deleted;
        }
    }

    public boolean deductInventory(String maVatTu, int soLuongDung) {
        String sql = "UPDATE VatTu SET soLuongTon = soLuongTon - ? WHERE maVatTu = ? AND soLuongTon >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soLuongDung);
            stmt.setString(2, maVatTu);
            stmt.setInt(3, soLuongDung);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Trừ tồn vật tư " + maVatTu + ", số lượng " + soLuongDung);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<VatTu> getAll() {
        List<VatTu> list = new ArrayList<>();
        String sql = "SELECT * FROM VatTu ORDER BY maVatTu";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapVatTu(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<VatTu> search(String keyword) {
        List<VatTu> list = new ArrayList<>();
        String sql = "SELECT * FROM VatTu WHERE maVatTu LIKE ? OR tenVatTu LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVatTu(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private VatTu mapVatTu(ResultSet rs) throws SQLException {
        VatTu vt = new VatTu();
        vt.setMaVatTu(rs.getString("maVatTu"));
        vt.setTenVatTu(rs.getString("tenVatTu"));
        vt.setDonViTinh(rs.getString("donViTinh"));
        vt.setSoLuongTon(rs.getInt("soLuongTon"));
        vt.setNgaySuDung(rs.getDate("ngaySuDung"));
        return vt;
    }
}