package models.dao;

import models.entity.DichVu;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVu> getAllDichVu() {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                DichVu dv = new DichVu(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getDouble("donGia"),
                    rs.getString("moTa")
                );
                list.add(dv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}