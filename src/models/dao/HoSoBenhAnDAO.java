package models.dao;

import Audit.AuditLog;
import models.entity.HoSoBenhAn;
import sql.DBConnection;

import java.sql.*;

public class HoSoBenhAnDAO {

    public boolean insert(HoSoBenhAn hs) {
        String sql = "INSERT INTO HoSoBenhAn (maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hs.getMaBenhAn());
            stmt.setString(2, hs.getMaKhachHang());
            stmt.setString(3, hs.getMaNhanVien());
            stmt.setDate(4, hs.getNgayKham());
            stmt.setString(5, hs.getKetQuaKham());
            stmt.setString(6, hs.getChuanDoan());
            stmt.setString(7, hs.getTrangThai());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Bệnh án " + hs.getMaBenhAn());
            return saved;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(String maBenhAn, String trangThaiMoi) {
        String sql = "UPDATE HoSoBenhAn SET trangThai = ? WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThaiMoi);
            stmt.setString(2, maBenhAn);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Trạng thái bệnh án " + maBenhAn);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
