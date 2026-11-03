package models.dao;

import sql.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Read-only reference data access for UI selection and catalog screens. */
public class DuLieuThamChieuDAO {
    public List<Object[]> getTeeth() throws Exception {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT MaRang, TenRang, Ham, ViTri, PhanLoai, MoTaChung FROM RANG ORDER BY MaRang ASC";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new Object[]{r.getInt("MaRang"), r.getString("TenRang"), r.getString("Ham"),
                    r.getString("ViTri"), r.getString("PhanLoai"), r.getString("MoTaChung")});
        }
        return rows;
    }

    public String[] getStaffInfo(String employeeId) throws Exception {
        String sql = "SELECT tenNhanVien, chucVu FROM NhanVien WHERE maNhanVien = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, employeeId);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return new String[]{r.getString("tenNhanVien"), r.getString("chucVu")};
            }
        }
        return new String[]{employeeId, "Nhân viên"};
    }

    public List<String[]> getShiftTimes() throws Exception {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT DISTINCT gioBatDau, gioKetThuc FROM LichLamViec WHERE gioBatDau IS NOT NULL AND gioKetThuc IS NOT NULL";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new String[]{r.getString("gioBatDau"), r.getString("gioKetThuc")});
        }
        return rows;
    }
}
