package models.dao;

import models.entity.NhanVien;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NhanVien nv = new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getDate("ngaySinh"),
                    rs.getString("chucVu"),
                    rs.getString("lichLamViec"),
                    rs.getString("trinhDo"),
                    rs.getDouble("mucLuong")
                );
                list.add(nv);
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
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getDate("ngaySinh"),
                    rs.getString("chucVu"),
                    rs.getString("lichLamViec"),
                    rs.getString("trinhDo"),
                    rs.getDouble("mucLuong")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}