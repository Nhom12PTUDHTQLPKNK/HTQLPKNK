
package models.dao;

import controllers.NhatKyKiemToan;
import models.entity.DichVu;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    // ================= THÊM DỊCH VỤ =================
    public boolean insert(DichVu dv) throws SQLException {

        String sql = "INSERT INTO DichVu "
                + "(maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh, moTa, hinhAnh) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dv.getMaDichVu());
            stmt.setString(2, dv.getTenDichVu());
            stmt.setString(3, dv.getLoaiDieuTri());

            if (dv.getDonGia() != null) {
                stmt.setDouble(4, dv.getDonGia());
            } else {
                stmt.setNull(4, Types.DOUBLE);
            }

            stmt.setString(5, dv.getDonViTinh());
            stmt.setString(6, dv.getMoTa());

            // Lưu đường dẫn hình ảnh
            if (dv.getHinhAnh() != null && !dv.getHinhAnh().isEmpty()) {
                stmt.setString(7, dv.getHinhAnh());
            } else {
                stmt.setNull(7, Types.NVARCHAR);
            }

            boolean saved = stmt.executeUpdate() > 0;

            if (saved) {
                NhatKyKiemToan.event("CREATE", "Dịch vụ " + dv.getMaDichVu());
            }

            return saved;
        }
    }

    // ================= CẬP NHẬT DỊCH VỤ =================
    public boolean update(DichVu dv) throws SQLException {

        String sql = "UPDATE DichVu SET "
                + "tenDichVu = ?, "
                + "loaiDieuTri = ?, "
                + "donGia = ?, "
                + "donViTinh = ?, "
                + "moTa = ?, "
                + "hinhAnh = ? "
                + "WHERE maDichVu = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dv.getTenDichVu());
            stmt.setString(2, dv.getLoaiDieuTri());

            if (dv.getDonGia() != null) {
                stmt.setDouble(3, dv.getDonGia());
            } else {
                stmt.setNull(3, Types.DOUBLE);
            }

            stmt.setString(4, dv.getDonViTinh());
            stmt.setString(5, dv.getMoTa());

            if (dv.getHinhAnh() != null && !dv.getHinhAnh().isEmpty()) {
                stmt.setString(6, dv.getHinhAnh());
            } else {
                stmt.setNull(6, Types.NVARCHAR);
            }

            stmt.setString(7, dv.getMaDichVu());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                NhatKyKiemToan.event("UPDATE", "Dịch vụ " + dv.getMaDichVu());
            }

            return updated;
        }
    }

    // ================= XÓA DỊCH VỤ =================
    public boolean delete(String maDichVu) throws SQLException {

        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maDichVu);

            boolean deleted = stmt.executeUpdate() > 0;

            if (deleted) {
                NhatKyKiemToan.event("DELETE", "Dịch vụ " + maDichVu);
            }

            return deleted;
        }
    }

    // ================= LẤY TẤT CẢ DỊCH VỤ =================
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
            System.err.println("Lỗi lấy danh sách dịch vụ: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // ================= TÌM KIẾM DỊCH VỤ =================
    public List<DichVu> search(String keyword) {

        List<DichVu> list = new ArrayList<>();

        String sql = "SELECT * FROM DichVu "
                + "WHERE maDichVu LIKE ? "
                + "OR tenDichVu LIKE ? "
                + "OR loaiDieuTri LIKE ? "
                + "ORDER BY maDichVu";

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
            System.err.println("Lỗi tìm kiếm dịch vụ: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // ================= ÁNH XẠ SQL SANG ENTITY =================
    private DichVu mapDichVu(ResultSet rs) throws SQLException {

        DichVu dv = new DichVu();

        dv.setMaDichVu(rs.getString("maDichVu"));
        dv.setTenDichVu(rs.getString("tenDichVu"));
        dv.setLoaiDieuTri(rs.getString("loaiDieuTri"));

        double donGia = rs.getDouble("donGia");
        dv.setDonGia(rs.wasNull() ? null : donGia);

        dv.setDonViTinh(rs.getString("donViTinh"));
        dv.setMoTa(rs.getString("moTa"));

        // Đọc đường dẫn hình ảnh từ SQL
        dv.setHinhAnh(rs.getString("hinhAnh"));

        return dv;
    }
    
    public List<String> getDanhSachLoaiDieuTri() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT tenLoaiDieuTri FROM dbo.DanhMucLoaiDieuTri WHERE trangThai = 1 ORDER BY tenLoaiDieuTri";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String loaiDieuTri = rs.getString("tenLoaiDieuTri");
                if (loaiDieuTri != null && !loaiDieuTri.trim().isEmpty()) {
                    list.add(loaiDieuTri);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            list.addAll(java.util.Arrays.asList("Nha khoa tổng quát", "Thẩm mỹ", "Chỉnh nha", "Tiểu phẫu", "Phục hình sứ"));
        }
        return list;
    }
}
