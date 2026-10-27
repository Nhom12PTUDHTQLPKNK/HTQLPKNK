package models.dao;

import models.entity.LichLamViec;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichLamViecDAO {

    public List<LichLamViec> getLichLamViecByNhanVien(String maNhanVien) {
        List<LichLamViec> list = new ArrayList<>();
        String sql = "SELECT * FROM LichLamViec WHERE maNhanVien = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LichLamViec llv = new LichLamViec(
                    rs.getString("maLichLamViec"),
                    rs.getString("maNhanVien"),
                    rs.getDate("ngayLamViec"),
                    rs.getString("gioBatDau"),
                    rs.getString("gioKetThuc"),
                    rs.getString("trangThai")
                );
                list.add(llv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}