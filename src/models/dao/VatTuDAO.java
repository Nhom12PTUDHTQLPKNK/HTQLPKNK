package models.dao;

import Audit.AuditLog;
import models.entity.VatTu;
import sql.DBConnection;

import java.sql.*;

public class VatTuDAO {

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
}
