package models.dao;

import models.entity.KhachHang;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {

    public boolean insert(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (maKhachHang, tenKH, gioiTinh, ngaySinh, sdt, tienSuBenh, diemTichLuy) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, kh.getMaKhachHang());
            stmt.setString(2, kh.getTenKH());
            stmt.setString(3, kh.getGioiTinh());
            stmt.setDate(4, kh.getNgaySinh());
            stmt.setString(5, kh.getSdt());
            stmt.setString(6, kh.getTienSuBenh());
            stmt.setInt(7, kh.getDiemTichLuy());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public KhachHang findByPhoneOrId(String keyword) {
        String sql = "SELECT * FROM KhachHang WHERE maKhachHang = ? OR sdt = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, keyword);
            stmt.setString(2, keyword);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("tenKH"),
                    rs.getString("gioiTinh"),
                    rs.getDate("ngaySinh"),
                    rs.getString("sdt"),
                    rs.getString("tienSuBenh"),
                    rs.getInt("diemTichLuy")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}