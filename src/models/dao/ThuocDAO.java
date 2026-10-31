package models.dao;

import Audit.AuditLog;
import models.entity.Thuoc;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThuocDAO {

    public boolean insert(Thuoc t) throws SQLException {
        String sql = "INSERT INTO Thuoc (maThuoc, tenThuoc, hoatChatChinh, maNhomThuoc, maDangBaoChe, maDuongDung, nhaSanXuat, nuocSanXuat, quyCachDongGoi, moTa, hinhAnh, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getMaThuoc());
            stmt.setString(2, t.getTenThuoc());
            stmt.setString(3, t.getHoatChatChinh());
            stmt.setString(4, t.getMaNhomThuoc());
            stmt.setString(5, t.getMaDangBaoChe());
            stmt.setString(6, t.getMaDuongDung());
            stmt.setString(7, t.getNhaSanXuat());
            stmt.setString(8, t.getNuocSanXuat());
            stmt.setString(9, t.getQuyCachDongGoi());
            stmt.setString(10, t.getMoTa());
            stmt.setString(11, t.getHinhAnh());
            stmt.setBoolean(12, t.getTrangThai() != null ? t.getTrangThai() : true);
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Thuốc " + t.getMaThuoc());
            return saved;
        }
    }

    public boolean update(Thuoc t) throws SQLException {
        String sql = "UPDATE Thuoc SET tenThuoc = ?, hoatChatChinh = ?, maNhomThuoc = ?, maDangBaoChe = ?, maDuongDung = ?, nhaSanXuat = ?, nuocSanXuat = ?, quyCachDongGoi = ?, moTa = ?, hinhAnh = ?, trangThai = ? WHERE maThuoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getTenThuoc());
            stmt.setString(2, t.getHoatChatChinh());
            stmt.setString(3, t.getMaNhomThuoc());
            stmt.setString(4, t.getMaDangBaoChe());
            stmt.setString(5, t.getMaDuongDung());
            stmt.setString(6, t.getNhaSanXuat());
            stmt.setString(7, t.getNuocSanXuat());
            stmt.setString(8, t.getQuyCachDongGoi());
            stmt.setString(9, t.getMoTa());
            stmt.setString(10, t.getHinhAnh());
            stmt.setBoolean(11, t.getTrangThai() != null ? t.getTrangThai() : true);
            stmt.setString(12, t.getMaThuoc());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Thuốc " + t.getMaThuoc());
            return updated;
        }
    }

    public boolean delete(String maThuoc) throws SQLException {
        String sql = "DELETE FROM Thuoc WHERE maThuoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maThuoc);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Thuốc " + maThuoc);
            return deleted;
        }
    }

    public List<Thuoc> getAllThuoc() {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM Thuoc ORDER BY maThuoc";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapThuoc(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Thuoc> search(String keyword) {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM Thuoc WHERE maThuoc LIKE ? OR tenThuoc LIKE ? OR hoatChatChinh LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapThuoc(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Thuoc mapThuoc(ResultSet rs) throws SQLException {
        Thuoc t = new Thuoc();
        t.setMaThuoc(rs.getString("maThuoc"));
        t.setTenThuoc(rs.getString("tenThuoc"));
        t.setHoatChatChinh(rs.getString("hoatChatChinh"));
        t.setMaNhomThuoc(rs.getString("maNhomThuoc"));
        t.setMaDangBaoChe(rs.getString("maDangBaoChe"));
        t.setMaDuongDung(rs.getString("maDuongDung"));
        t.setNhaSanXuat(rs.getString("nhaSanXuat"));
        t.setNuocSanXuat(rs.getString("nuocSanXuat"));
        t.setQuyCachDongGoi(rs.getString("quyCachDongGoi"));
        t.setMoTa(rs.getString("moTa"));
        t.setHinhAnh(rs.getString("hinhAnh"));
        t.setTrangThai(rs.getBoolean("trangThai"));
        return t;
    }
}