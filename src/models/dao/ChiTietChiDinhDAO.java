package models.dao;

import controllers.AuditLog;
import models.entity.ChiTietChiDinh;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietChiDinhDAO {

    // ================= THÊM CHI TIẾT CHỈ ĐỊNH =================
    public boolean insert(ChiTietChiDinh ctd) throws SQLException {
        String sql = "INSERT INTO ChiTietChiDinh (maBenhAn, maDichVu, maRang, soLuong, donVi, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ctd.getMaBenhAn());
            stmt.setString(2, ctd.getMaDichVu());

            if (ctd.getMaRang() != null) {
                stmt.setInt(3, ctd.getMaRang());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setInt(4, ctd.getSoLuong());
            stmt.setString(5, ctd.getDonVi());
            stmt.setString(6, ctd.getGhiChu());

            boolean saved = stmt.executeUpdate() > 0;

            if (saved) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ctd.setMaChiDinh(rs.getInt(1));
                    }
                }

                AuditLog.event("CREATE",
                        "Thêm chỉ định dịch vụ " + ctd.getMaDichVu()
                        + " - Bệnh án " + ctd.getMaBenhAn());
            }

            return saved;
        }
    }

    // ================= CẬP NHẬT CHI TIẾT CHỈ ĐỊNH =================
    public boolean update(ChiTietChiDinh ctd) throws SQLException {
        String sql = "UPDATE ChiTietChiDinh SET maBenhAn = ?, maDichVu = ?, maRang = ?, soLuong = ?, donVi = ?, ghiChu = ? WHERE maChiDinh = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ctd.getMaBenhAn());
            stmt.setString(2, ctd.getMaDichVu());

            if (ctd.getMaRang() != null) {
                stmt.setInt(3, ctd.getMaRang());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setInt(4, ctd.getSoLuong());
            stmt.setString(5, ctd.getDonVi());
            stmt.setString(6, ctd.getGhiChu());
            stmt.setInt(7, ctd.getMaChiDinh());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                AuditLog.event("UPDATE",
                        "Cập nhật chi tiết chỉ định mã: " + ctd.getMaChiDinh());
            }

            return updated;
        }
    }

    // ================= XÓA CHI TIẾT CHỈ ĐỊNH =================
    public boolean delete(int maChiDinh) throws SQLException {
        String sql = "DELETE FROM ChiTietChiDinh WHERE maChiDinh = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maChiDinh);

            boolean deleted = stmt.executeUpdate() > 0;

            if (deleted) {
                AuditLog.event("DELETE",
                        "Xóa chi tiết chỉ định mã: " + maChiDinh);
            }

            return deleted;
        }
    }

    // ================= TÌM THEO MÃ CHI TIẾT CHỈ ĐỊNH =================
    public ChiTietChiDinh findByMaChiDinh(int maChiDinh) throws SQLException {
        String sql = "SELECT * FROM ChiTietChiDinh WHERE maChiDinh = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maChiDinh);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapChiTietChiDinh(rs);
                }
            }
        }
        return null;
    }

    // ================= LẤY DANH SÁCH THEO BỆNH ÁN =================
    public List<ChiTietChiDinh> findByMaBenhAn(String maBenhAn) throws SQLException {
        List<ChiTietChiDinh> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietChiDinh WHERE maBenhAn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maBenhAn);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChiTietChiDinh(rs));
                }
            }
        }
        return list;
    }

    // ================= LẤY TẤT CẢ DANH SÁCH =================
    public List<ChiTietChiDinh> getAll() throws SQLException {
        List<ChiTietChiDinh> list = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietChiDinh";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapChiTietChiDinh(rs));
            }
        }
        return list;
    }

    // ================= ÁNH XẠ RESULTSET =================
    private ChiTietChiDinh mapChiTietChiDinh(ResultSet rs) throws SQLException {
        ChiTietChiDinh ctd = new ChiTietChiDinh();

        ctd.setMaChiDinh(rs.getInt("maChiDinh"));
        ctd.setMaBenhAn(rs.getString("maBenhAn"));
        ctd.setMaDichVu(rs.getString("maDichVu"));

        int maRang = rs.getInt("maRang");
        ctd.setMaRang(rs.wasNull() ? null : maRang);

        ctd.setSoLuong(rs.getInt("soLuong"));
        ctd.setDonVi(rs.getString("donVi"));
        ctd.setGhiChu(rs.getString("ghiChu"));

        return ctd;
    }
}