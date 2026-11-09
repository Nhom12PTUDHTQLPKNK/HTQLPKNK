package models.dao;

import controllers.NhatKyKiemToan;
import models.entity.NhanVien;
import controllers.KiemSoatTruyCap;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    public boolean insert(NhanVien nv) throws SQLException {
        if ("admin".equalsIgnoreCase(nv.getChucVu() == null ? "" : nv.getChucVu().trim()))
            throw new SQLException("Không thể gán chức vụ Admin cho nhân viên.");
        String sql = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, ngaySinh, sdt, chucVu, lichLamViec, trinhDo, mucLuong, hinhAnh) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nv.getMaNhanVien());
            stmt.setString(2, nv.getTenNhanVien());
            stmt.setDate(3, (Date) nv.getNgaySinh());
            stmt.setString(4, nv.getSdt());
            stmt.setString(5, nv.getChucVu());
            stmt.setString(6, nv.getLichLamViec());
            stmt.setString(7, nv.getTrinhDo());
            stmt.setDouble(8, nv.getMucLuong() != null ? nv.getMucLuong() : 0.0);
            stmt.setString(9, nv.getHinhAnh());
            boolean saved = stmt.executeUpdate() > 0;
            if (saved) NhatKyKiemToan.event("CREATE", "Nhân viên " + nv.getMaNhanVien());
            return saved;
        }
    }

    public boolean update(NhanVien nv) throws SQLException {
        if ("admin".equalsIgnoreCase(nv.getChucVu() == null ? "" : nv.getChucVu().trim()))
            throw new SQLException("Không thể gán chức vụ Admin cho nhân viên.");
        if (isFixedAdminEmployee(nv.getMaNhanVien())) throw new SQLException("Nhân sự gắn với tài khoản Admin được bảo vệ và không thể chỉnh sửa.");
        String sql = "UPDATE NhanVien SET tenNhanVien = ?, ngaySinh = ?, sdt = ?, chucVu = ?, lichLamViec = ?, trinhDo = ?, mucLuong = ?, hinhAnh = ? WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nv.getTenNhanVien());
            stmt.setDate(2, (Date) nv.getNgaySinh());
            stmt.setString(3, nv.getSdt());
            stmt.setString(4, nv.getChucVu());
            stmt.setString(5, nv.getLichLamViec());
            stmt.setString(6, nv.getTrinhDo());
            stmt.setDouble(7, nv.getMucLuong() != null ? nv.getMucLuong() : 0.0);
            stmt.setString(8, nv.getHinhAnh());
            stmt.setString(9, nv.getMaNhanVien());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event("UPDATE", "Nhân viên " + nv.getMaNhanVien());
            return updated;
        }
    }

    public boolean delete(String maNhanVien) throws SQLException {
        if (isFixedAdminEmployee(maNhanVien)) throw new SQLException("Nhân sự gắn với tài khoản Admin được bảo vệ và không thể xóa.");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement lookup = conn.prepareStatement("SELECT tenDN,quyen FROM TaiKhoan WHERE maNhanVien=?")) {
            lookup.setString(1, maNhanVien);
            try (ResultSet rs = lookup.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("tenDN");
                    if (!KiemSoatTruyCap.isAdministrator()) throw new SQLException("Chỉ admin mới được xóa nhân viên có tài khoản.");
                    if (KiemSoatTruyCap.isCurrentAccount(username)) throw new SQLException("Không thể tự xóa tài khoản đang đăng nhập.");
                    if (KiemSoatTruyCap.isProtectedAdministrator(username, rs.getString("quyen"))) throw new SQLException("Không thể xóa tài khoản admin/quản lý.");
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM NhanVien WHERE maNhanVien = ?")) {
            stmt.setString(1, maNhanVien);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) NhatKyKiemToan.event("DELETE", "Nhân viên " + maNhanVien);
            return deleted;
            }
        }
    }

    private boolean isFixedAdminEmployee(String employeeId) throws SQLException {
        if (employeeId == null) return false;
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE maNhanVien=? AND LOWER(tenDN)='admin'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        }
    }

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien ORDER BY maNhanVien";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapNhanVien(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<NhanVien> search(String keyword) {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien WHERE maNhanVien LIKE ? OR tenNhanVien LIKE ? OR sdt LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public NhanVien findById(String maNhanVien) {
        String sql = "SELECT * FROM NhanVien WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getAllChucVu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT tenChucVu FROM ChucVu WHERE trangThai = 1 AND UPPER(LTRIM(RTRIM(tenChucVu))) <> 'ADMIN' ORDER BY tenChucVu";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("tenChucVu"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private NhanVien mapNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien(
            rs.getString("maNhanVien"),
            rs.getString("tenNhanVien"),
            rs.getDate("ngaySinh"),
            rs.getString("sdt"),
            rs.getString("chucVu"),
            rs.getString("lichLamViec"),
            rs.getString("trinhDo"),
            rs.getDouble("mucLuong")
        );
        nv.setHinhAnh(rs.getString("hinhAnh"));
        return nv;
    }
}
