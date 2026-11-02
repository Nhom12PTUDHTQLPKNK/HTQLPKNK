
package models.dao;

import controllers.AuditLog;
import models.entity.ChiTietKhamRang;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChiTietKhamRangDAO {

    // ================= THÊM RĂNG ĐÃ KHÁM =================
    public boolean insert(ChiTietKhamRang ct) throws SQLException {

        String sql = "INSERT INTO ChiTietKhamRang "
                + "(maBenhAn, maRang, tinhTrang, mucDo, "
                + "trieuChung, chanDoan, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ct.getMaBenhAn());
            stmt.setInt(2, ct.getMaRang());
            stmt.setString(3, ct.getTinhTrang());
            stmt.setString(4, ct.getMucDo());
            stmt.setString(5, ct.getTrieuChung());
            stmt.setString(6, ct.getChanDoan());
            stmt.setString(7, ct.getGhiChu());

            boolean saved = stmt.executeUpdate() > 0;

            if (saved) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ct.setMaChiTietKham(rs.getInt(1));
                    }
                }

                AuditLog.event("CREATE",
                        "Thêm thông tin khám răng " + ct.getMaRang()
                        + " - Bệnh án " + ct.getMaBenhAn());
            }

            return saved;
        }
    }

    // ================= CẬP NHẬT RĂNG ĐÃ KHÁM =================
    public boolean update(ChiTietKhamRang ct) throws SQLException {

        String sql = "UPDATE ChiTietKhamRang SET "
                + "maRang = ?, tinhTrang = ?, mucDo = ?, "
                + "trieuChung = ?, chanDoan = ?, ghiChu = ? "
                + "WHERE maChiTietKham = ? AND maBenhAn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ct.getMaRang());
            stmt.setString(2, ct.getTinhTrang());
            stmt.setString(3, ct.getMucDo());
            stmt.setString(4, ct.getTrieuChung());
            stmt.setString(5, ct.getChanDoan());
            stmt.setString(6, ct.getGhiChu());
            stmt.setInt(7, ct.getMaChiTietKham());
            stmt.setString(8, ct.getMaBenhAn());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                AuditLog.event("UPDATE",
                        "Cập nhật thông tin khám răng "
                        + ct.getMaRang());
            }

            return updated;
        }
    }

    // ================= XÓA RĂNG ĐÃ KHÁM =================
    public boolean delete(int maChiTietKham) throws SQLException {

        String sql = "DELETE FROM ChiTietKhamRang "
                + "WHERE maChiTietKham = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maChiTietKham);

            boolean deleted = stmt.executeUpdate() > 0;

            if (deleted) {
                AuditLog.event("DELETE",
                        "Xóa chi tiết khám răng " + maChiTietKham);
            }

            return deleted;
        }
    }

    // ================= TÌM THEO MÃ CHI TIẾT =================
    public ChiTietKhamRang findByMaChiTietKham(
            int maChiTietKham) throws SQLException {

        String sql = "SELECT * FROM ChiTietKhamRang "
                + "WHERE maChiTietKham = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maChiTietKham);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapChiTietKhamRang(rs);
                }
            }
        }

        return null;
    }

    // ================= LẤY DANH SÁCH THEO BỆNH ÁN =================
    public List<ChiTietKhamRang> findByMaBenhAn(
            String maBenhAn) throws SQLException {

        List<ChiTietKhamRang> list = new ArrayList<>();

        String sql = "SELECT * FROM ChiTietKhamRang "
                + "WHERE maBenhAn = ? ORDER BY maRang";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maBenhAn);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChiTietKhamRang(rs));
                }
            }
        }

        return list;
    }
    
    public static String loadTeethFromDatabase(int code) {
        String tenRang = null;
        String query = "SELECT TenRang FROM RANG WHERE MaRang = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tenRang = rs.getString("TenRang");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tenRang;
    }

    // ================= ÁNH XẠ RESULTSET =================
    private ChiTietKhamRang mapChiTietKhamRang(
            ResultSet rs) throws SQLException {

        ChiTietKhamRang ct = new ChiTietKhamRang();

        ct.setMaChiTietKham(rs.getInt("maChiTietKham"));
        ct.setMaBenhAn(rs.getString("maBenhAn"));
        ct.setMaRang(rs.getInt("maRang"));
        ct.setTinhTrang(rs.getString("tinhTrang"));
        ct.setMucDo(rs.getString("mucDo"));
        ct.setTrieuChung(rs.getString("trieuChung"));
        ct.setChanDoan(rs.getString("chanDoan"));
        ct.setGhiChu(rs.getString("ghiChu"));

        return ct;
    }
}
