package models.dao;

import models.entity.Thuoc;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThuocDAO {

    public List<Thuoc> getAllThuoc() {
        List<Thuoc> list = new ArrayList<>();
        String sql = "SELECT * FROM Thuoc";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Thuoc t = new Thuoc(
                    rs.getString("maThuoc"),
                    rs.getString("tenThuoc"),
                    rs.getString("donViTinh"),
                    rs.getDouble("donGia"),
                    rs.getInt("soLuongTon")
                );
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStock(String maThuoc, int soLuongMoi) {
        String sql = "UPDATE Thuoc SET soLuongTon = ? WHERE maThuoc = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, soLuongMoi);
            stmt.setString(2, maThuoc);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}