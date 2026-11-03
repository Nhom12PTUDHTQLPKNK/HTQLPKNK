package models.dao;

import controllers.AuditLog;
import models.entity.HoSoBenhAn;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoSoBenhAnDAO {

    public boolean insert(HoSoBenhAn hs) throws SQLException {
        String sql = "INSERT INTO HoSoBenhAn (maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hs.getMaBenhAn());
            stmt.setString(2, hs.getMaKhachHang());
            stmt.setString(3, hs.getMaNhanVien());
            stmt.setDate(4, hs.getNgayKham() != null ? new java.sql.Date(hs.getNgayKham().getTime()) : null);
            stmt.setString(5, hs.getKetQuaKham());
            stmt.setString(6, hs.getChuanDoan());
            stmt.setString(7, hs.getTrangThai());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) AuditLog.event("CREATE", "Bệnh án " + hs.getMaBenhAn());
            return saved;
        }
    }

    public boolean update(HoSoBenhAn hs) throws SQLException {
        String sql = "UPDATE HoSoBenhAn SET maKhachHang = ?, maNhanVien = ?, ngayKham = ?, ketQuaKham = ?, chuanDoan = ?, trangThai = ? WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hs.getMaKhachHang());
            stmt.setString(2, hs.getMaNhanVien());
            stmt.setDate(3, hs.getNgayKham() != null ? new java.sql.Date(hs.getNgayKham().getTime()) : null);
            stmt.setString(4, hs.getKetQuaKham());
            stmt.setString(5, hs.getChuanDoan());
            stmt.setString(6, hs.getTrangThai());
            stmt.setString(7, hs.getMaBenhAn());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) AuditLog.event("UPDATE", "Bệnh án " + hs.getMaBenhAn());
            return updated;
        }
    }

    public boolean delete(String maBenhAn) throws SQLException {
        String sql = "DELETE FROM HoSoBenhAn WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maBenhAn);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) AuditLog.event("DELETE", "Bệnh án " + maBenhAn);
            return deleted;
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

    public List<HoSoBenhAn> getAll() {
        List<HoSoBenhAn> list = new ArrayList<>();
        String sql = "SELECT hs.*, nv.tenNhanVien FROM HoSoBenhAn hs LEFT JOIN NhanVien nv ON hs.maNhanVien = nv.maNhanVien ORDER BY hs.ngayKham DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapHoSoBenhAn(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getNextMaBenhAn() throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(INT, SUBSTRING(maBenhAn, 3, 3))), 0) + 1 "
                + "FROM HoSoBenhAn WHERE maBenhAn LIKE 'BA[0-9][0-9][0-9]'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            int next = rs.getInt(1);
            if (next > 999) throw new SQLException("Đã hết mã hồ sơ theo định dạng BA + 3 số.");
            return String.format("BA%03d", next);
        }
    }

    public boolean customerExists(String maKhachHang) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM KhachHang WHERE maKhachHang = ?")) {
            stmt.setString(1, maKhachHang);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    public List<String[]> getDentists() throws SQLException {
        List<String[]> dentists = new ArrayList<>();
        String sql = "SELECT maNhanVien, tenNhanVien FROM NhanVien "
                + "WHERE chucVu LIKE N'%nha sĩ%' OR chucVu LIKE N'%nha si%' "
                + "ORDER BY tenNhanVien";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) dentists.add(new String[]{rs.getString("maNhanVien"), rs.getString("tenNhanVien")});
        }
        return dentists;
    }

    public List<HoSoBenhAn> search(String keyword) {
        List<HoSoBenhAn> list = new ArrayList<>();
        String sql = "SELECT * FROM HoSoBenhAn WHERE maBenhAn LIKE ? OR maKhachHang LIKE ? OR chuanDoan LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHoSoBenhAn(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private HoSoBenhAn mapHoSoBenhAn(ResultSet rs) throws SQLException {
        HoSoBenhAn hs = new HoSoBenhAn();
        hs.setMaBenhAn(rs.getString("maBenhAn"));
        hs.setMaKhachHang(rs.getString("maKhachHang"));
        hs.setMaNhanVien(rs.getString("maNhanVien"));
        try { hs.setTenNhanVien(rs.getString("tenNhanVien")); } catch (SQLException ignored) { }
        hs.setNgayKham(rs.getDate("ngayKham"));
        hs.setKetQuaKham(rs.getString("ketQuaKham"));
        hs.setChuanDoan(rs.getString("chuanDoan"));
        hs.setTrangThai(rs.getString("trangThai"));
        return hs;
    }
    
    public List<Object[]> getTableDataByLichHenOrMa(String maLichHen) {
        List<Object[]> list = new ArrayList<>();
        
        String sql = "SELECT hs.maBenhAn, hs.maKhachHang, hs.maNhanVien, hs.ngayKham, hs.ketQuaKham, hs.chuanDoan, hs.trangThai " +
                     "FROM HoSoBenhAn hs " +
                     "WHERE hs.maBenhAn = ? " +
                     "OR hs.maKhachHang = ? " +
                     "OR hs.maKhachHang IN (SELECT maKhachHang FROM LichHen WHERE maLichHen = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            ps.setString(2, maLichHen);
            ps.setString(3, maLichHen);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("maBenhAn"),
                        rs.getString("maKhachHang"),
                        rs.getString("maNhanVien"),
                        rs.getDate("ngayKham"),
                        rs.getString("ketQuaKham"),
                        rs.getString("chuanDoan"),
                        rs.getString("trangThai")
                    };
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
}
