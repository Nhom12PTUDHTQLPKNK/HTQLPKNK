package models.dao;

import models.entity.NhanVien;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // Thêm nhân viên mới
    public boolean insert(NhanVien nv) throws SQLException {
        String sql = "INSERT INTO NhanVien (maNhanVien, tenNhanVien, ngaySinh, sdt, chucVu, lichLamViec, trinhDo, mucLuong) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nv.getMaNhanVien());
            stmt.setString(2, nv.getTenNhanVien());
            stmt.setDate(3, nv.getNgaySinh());
            stmt.setString(4, nv.getSdt());
            stmt.setString(5, nv.getChucVu());
            stmt.setString(6, nv.getLichLamViec());
            stmt.setString(7, nv.getTrinhDo());
            stmt.setDouble(8, nv.getMucLuong());
            return stmt.executeUpdate() > 0;
        }
    }

    // Cập nhật thông tin nhân viên
    public boolean update(NhanVien nv) throws SQLException {
        String sql = "UPDATE NhanVien SET tenNhanVien = ?, ngaySinh = ?, sdt = ?, chucVu = ?, lichLamViec = ?, trinhDo = ?, mucLuong = ? WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nv.getTenNhanVien());
            stmt.setDate(2, nv.getNgaySinh());
            stmt.setString(3, nv.getSdt());
            stmt.setString(4, nv.getChucVu());
            stmt.setString(5, nv.getLichLamViec());
            stmt.setString(6, nv.getTrinhDo());
            stmt.setDouble(7, nv.getMucLuong());
            stmt.setString(8, nv.getMaNhanVien());
            return stmt.executeUpdate() > 0;
        }
    }

    // Xóa nhân viên theo mã
    public boolean delete(String maNhanVien) throws SQLException {
        String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            return stmt.executeUpdate() > 0;
        }
    }

    // Lấy toàn bộ danh sách nhân viên
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

    // Tìm kiếm nhân viên theo mã, tên hoặc số điện thoại
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

    // Tìm chi tiết nhân viên theo mã
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

    // Lấy danh sách chức vụ duy nhất hiện có trong CSDL (dùng đổ vào JComboBox nếu cần)
    public List<String> getAllChucVu() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT chucVu FROM NhanVien WHERE chucVu IS NOT NULL AND chucVu <> ''";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("chucVu"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ánh xạ dữ liệu từ ResultSet sang đối tượng NhanVien
    private NhanVien mapNhanVien(ResultSet rs) throws SQLException {
        return new NhanVien(
            rs.getString("maNhanVien"),
            rs.getString("tenNhanVien"),
            rs.getDate("ngaySinh"),
            rs.getString("sdt"),
            rs.getString("chucVu"),
            rs.getString("lichLamViec"),
            rs.getDouble("mucLuong")
        );
    }
}