package models.dao;

import models.entity.LichHen;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LichHenDAO {

    /**
     * Lấy toàn bộ danh sách lịch hẹn từ CSDL bằng cách JOIN các bảng:
     * LichHen JOIN KhachHang JOIN NhanVien JOIN DichVu
     * Chỉ lấy chính xác 2 trạng thái: 'Chờ xác nhận' và 'Đã xác nhận'
     */
    public List<Object[]> getAllAppointments() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT L.maLichHen, K.tenKH, K.sdt, " +
                     "ISNULL(D.tenDichVu, L.ghiChu) AS tenDichVu, " +
                     "N.tenNhanVien AS nhaSi, " +
                     "CONVERT(VARCHAR(10), L.ngayHen, 103) AS ngayHen, " +
                     "L.gioKham, L.trangThai " +
                     "FROM LichHen L " +
                     "INNER JOIN KhachHang K ON L.maKhachHang = K.maKhachHang " +
                     "LEFT JOIN NhanVien N ON L.maNhanVien = N.maNhanVien " +
                     "LEFT JOIN DichVu D ON L.ghiChu = D.maDichVu OR L.ghiChu = D.tenDichVu " +
                     "WHERE L.trangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                     "ORDER BY L.ngayHen DESC, L.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String gioKham = rs.getString("gioKham");
                if (gioKham == null || gioKham.trim().isEmpty()) {
                    gioKham = "";
                }

                list.add(new Object[]{
                    rs.getString("maLichHen"),
                    rs.getString("tenKH"),
                    rs.getString("sdt"),
                    rs.getString("tenDichVu"),
                    rs.getString("nhaSi"),
                    rs.getString("ngayHen"),
                    gioKham,
                    rs.getString("trangThai")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkOverlap(String maNhanVien, Date ngayHen, String gioKham) {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE maNhanVien = ? AND ngayHen = ? AND gioKham = ? AND ISNULL(trangThai, N'') <> N'Đã hủy'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            ps.setDate(2, new java.sql.Date(ngayHen.getTime()));
            ps.setString(3, gioKham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(LichHen lichHen) throws SQLException {
        String sql = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVien, ngayHen, gioKham, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lichHen.getMaLichHen());
            ps.setString(2, lichHen.getMaKhachHang());
            ps.setString(3, lichHen.getMaNhanVien());
            ps.setDate(4, new java.sql.Date(lichHen.getNgayHen().getTime()));
            ps.setString(5, lichHen.getGioKham());
            ps.setString(6, lichHen.getTrangThai());
            ps.setString(7, lichHen.getGhiChu());
            return ps.executeUpdate() > 0;
        }
    }

    public String findCustomerId(Connection conn, String phone) throws SQLException {
        String sql = "SELECT maKhachHang FROM KhachHang WHERE sdt = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("maKhachHang");
            }
        }
        return null;
    }

    public String createCustomer(Connection conn, String name, String phone) throws SQLException {
        String newId;
        String getIdSql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING(maKhachHang, 3, 20))), 0) + 1 FROM KhachHang WHERE maKhachHang LIKE 'KH%'";
        try (PreparedStatement ps = conn.prepareStatement(getIdSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                newId = String.format("KH%03d", rs.getInt(1));
            } else {
                newId = "KH001";
            }
        }

        String insertSql = "INSERT INTO KhachHang (maKhachHang, tenKH, sdt, diemTichLuy) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, newId);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.executeUpdate();
        }
        return newId;
    }

    public String findDoctorId(Connection conn, String doctorName) throws SQLException {
        String sql = "SELECT maNhanVien FROM NhanVien WHERE tenNhanVien = ? AND chucVu LIKE N'%Bác sĩ%'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("maNhanVien");
            }
        }
        return null;
    }

    public boolean isDuplicateAppointment(Connection conn, String doctorId, java.sql.Date appointmentDate, String appointmentTime, String appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE maNhanVien = ? AND ngayHen = ? AND gioKham = ? AND ISNULL(trangThai, N'') <> N'Đã hủy' AND maLichHen <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            ps.setDate(2, appointmentDate);
            ps.setString(3, appointmentTime);
            ps.setString(4, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean insertAppointment(Connection conn, String id, String customerId, String doctorId, java.sql.Date date, String time, String service) throws SQLException {
        String sql = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVien, ngayHen, gioKham, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, N'Chờ xác nhận', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, customerId);
            ps.setString(3, doctorId);
            ps.setDate(4, date);
            ps.setString(5, time);
            ps.setString(6, service);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAppointment(Connection conn, String id, String customerId, String doctorId, java.sql.Date date, String time, String service) throws SQLException {
        String sql = "UPDATE LichHen SET maKhachHang = ?, maNhanVien = ?, ngayHen = ?, gioKham = ?, ghiChu = ? WHERE maLichHen = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.setString(2, doctorId);
            ps.setDate(3, date);
            ps.setString(4, time);
            ps.setString(5, service);
            ps.setString(6, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật trạng thái nhanh (Dùng khi bấm nút Xác Nhận)
    public boolean updateAppointmentStatus(String id, String trangThai) {
        String sql = "UPDATE LichHen SET trangThai = ? WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelAppointment(String id) {
        String sql = "UPDATE LichHen SET trangThai = N'Đã hủy' WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}