
package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import models.entity.LichHen;
import sql.DBConnection;

public class LichHenDAO {

    // ==================== TABLE MODEL ====================

    private DefaultTableModel createTableModel() {

        return new DefaultTableModel(
                new String[]{
                        "Mã Lịch Hẹn",
                        "Tên Khách Hàng",
                        "SĐT",
                        "Nha sĩ phụ trách",
                        "Ngày hẹn",
                        "Giờ",
                        "Trạng thái",
                        "Ghi chú"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // ==================== GET APPOINTMENTS BY STATUS ====================

    public DefaultTableModel getDanhSachLichHen(String trangThai) {

        DefaultTableModel model = createTableModel();

        String sql =
                "SELECT lh.maLichHen, kh.tenKH, kh.sdt, " +
                "nv.tenNhanVien, lh.ngayHen, lh.gioKham, " +
                "lh.trangThai, lh.ghiChu " +
                "FROM LichHen lh " +
                "LEFT JOIN KhachHang kh " +
                "ON lh.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv " +
                "ON lh.maNhanVien = nv.maNhanVien " +
                "WHERE lh.trangThai = ? " +
                "ORDER BY lh.ngayHen ASC, lh.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trangThai);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    model.addRow(new Object[]{
                            rs.getString("maLichHen"),
                            rs.getString("tenKH"),
                            rs.getString("sdt"),
                            rs.getString("tenNhanVien"),
                            rs.getDate("ngayHen"),
                            rs.getString("gioKham"),
                            rs.getString("trangThai"),
                            rs.getString("ghiChu")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    public DefaultTableModel getLichHenChoXuLy() {
        return getDanhSachLichHen("Chờ xác nhận");
    }

    public DefaultTableModel getLichHenDaXacNhan() {
        return getDanhSachLichHen("Đã xác nhận");
    }

    // ==================== GET ALL APPOINTMENTS ====================

    // Dữ liệu trả về đúng 8 cột của BookingProcessPanel:
    // 0: Mã lịch hẹn
    // 1: Tên khách hàng
    // 2: Số điện thoại
    // 3: Dịch vụ
    // 4: Nha sĩ
    // 5: Ngày hẹn
    // 6: Giờ hẹn
    // 7: Trạng thái

    public List<Object[]> getAllAppointments() {

        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT lh.maLichHen, " +
                "kh.tenKH, " +
                "kh.sdt, " +
                "lh.ghiChu, " +
                "nv.tenNhanVien, " +
                "lh.ngayHen, " +
                "lh.gioKham, " +
                "lh.trangThai " +
                "FROM LichHen lh " +
                "LEFT JOIN KhachHang kh " +
                "ON lh.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv " +
                "ON lh.maNhanVien = nv.maNhanVien " +
                "ORDER BY lh.ngayHen DESC, lh.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String ghiChu = rs.getString("ghiChu");
                String service = "";

                if (ghiChu != null) {

                    ghiChu = ghiChu.trim();

                    if (ghiChu.startsWith("Dịch vụ: ")) {
                        service = ghiChu.substring("Dịch vụ: ".length());
                    } else {
                        service = ghiChu;
                    }
                }

                String date = "";

                java.sql.Date sqlDate = rs.getDate("ngayHen");

                if (sqlDate != null) {
                    date = new SimpleDateFormat("dd/MM/yyyy")
                            .format(sqlDate);
                }

                String customerName = rs.getString("tenKH");
                String phone = rs.getString("sdt");
                String doctor = rs.getString("tenNhanVien");
                String time = rs.getString("gioKham");
                String status = rs.getString("trangThai");

                list.add(new Object[]{
                        rs.getString("maLichHen"),
                        customerName == null ? "" : customerName,
                        phone == null ? "" : phone,
                        service,
                        doctor == null ? "" : doctor,
                        date,
                        time == null ? "" : time,
                        status == null ? "" : status
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tải danh sách lịch hẹn: " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    // ==================== CONFIRM APPOINTMENT ====================

    public boolean xacNhanLichHen(String maLichHen) {

        String sql =
                "UPDATE LichHen " +
                "SET trangThai = N'Đã xác nhận' " +
                "WHERE maLichHen = ? " +
                "AND trangThai = N'Chờ xác nhận'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichHen);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== CANCEL APPOINTMENT ====================

    public boolean cancelAppointment(String maLichHen) {

        String sql =
                "UPDATE LichHen SET trangThai = N'Đã hủy' " +
                "WHERE maLichHen = ? " +
                "AND trangThai IN (" +
                "N'Chờ xác nhận', N'Đã xác nhận', N'Chờ khám')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichHen);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== FIND CUSTOMER ====================

    public String findCustomerId(
            Connection conn,
            String phone
    ) throws SQLException {

        String sql =
                "SELECT maKhachHang " +
                "FROM KhachHang WHERE sdt = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("maKhachHang");
                }
            }
        }

        return null;
    }

    // ==================== CREATE CUSTOMER ====================

    public String createCustomer(
            Connection conn,
            String name,
            String phone
    ) throws SQLException {

        String newCustomerId = "KH" + System.currentTimeMillis();

        String sql =
                "INSERT INTO KhachHang " +
                "(maKhachHang, tenKH, sdt) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newCustomerId);
            ps.setString(2, name);
            ps.setString(3, phone);

            ps.executeUpdate();
        }

        return newCustomerId;
    }

    // ==================== FIND DOCTOR ====================

    public String findDoctorId(
            Connection conn,
            String doctorName
    ) throws SQLException {

        String sql =
                "SELECT maNhanVien " +
                "FROM NhanVien " +
                "WHERE tenNhanVien = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctorName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("maNhanVien");
                }
            }
        }

        return null;
    }

    // ==================== CHECK DUPLICATE APPOINTMENT ====================

    public boolean isDuplicateAppointment(
            Connection conn,
            String maNhanVien,
            Date ngayHen,
            String gioKham,
            String excludeMaLichHen
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM LichHen " +
                "WHERE maNhanVien = ? " +
                "AND ngayHen = ? " +
                "AND gioKham = ? " +
                "AND maLichHen <> ? " +
                "AND trangThai IN (" +
                "N'Chờ xác nhận', N'Đã xác nhận', N'Chờ khám')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maNhanVien);

            ps.setDate(
                    2,
                    new java.sql.Date(ngayHen.getTime())
            );

            ps.setString(3, gioKham);

            ps.setString(
                    4,
                    excludeMaLichHen != null
                            ? excludeMaLichHen
                            : ""
            );

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // ==================== INSERT APPOINTMENT ====================

    public void insertAppointment(
            Connection conn,
            String maLichHen,
            String maKhachHang,
            String maNhanVien,
            Date ngayHen,
            String gioKham,
            String service
    ) throws SQLException {

        String sql =
                "INSERT INTO LichHen " +
                "(maLichHen, maKhachHang, maNhanVien, " +
                "ngayHen, gioKham, trangThai, ghiChu) " +
                "VALUES (?, ?, ?, ?, ?, N'Chờ xác nhận', ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichHen);
            ps.setString(2, maKhachHang);
            ps.setString(3, maNhanVien);

            ps.setDate(
                    4,
                    new java.sql.Date(ngayHen.getTime())
            );

            ps.setString(5, gioKham);
            ps.setString(6, "Dịch vụ: " + service);

            ps.executeUpdate();
        }
    }

    // ==================== UPDATE APPOINTMENT ====================

    public void updateAppointment(
            Connection conn,
            String maLichHen,
            String maKhachHang,
            String maNhanVien,
            Date ngayHen,
            String gioKham,
            String service
    ) throws SQLException {

        String sql =
                "UPDATE LichHen SET " +
                "maKhachHang = ?, " +
                "maNhanVien = ?, " +
                "ngayHen = ?, " +
                "gioKham = ?, " +
                "ghiChu = ? " +
                "WHERE maLichHen = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKhachHang);
            ps.setString(2, maNhanVien);

            ps.setDate(
                    3,
                    new java.sql.Date(ngayHen.getTime())
            );

            ps.setString(4, gioKham);
            ps.setString(5, "Dịch vụ: " + service);
            ps.setString(6, maLichHen);

            ps.executeUpdate();
        }
    }

    // ==================== CHECK OVERLAP ====================

    public boolean checkOverlap(
            String maNhanVien,
            Date ngayHen,
            String gioKham
    ) {

        try (Connection conn = DBConnection.getConnection()) {

            return isDuplicateAppointment(
                    conn,
                    maNhanVien,
                    ngayHen,
                    gioKham,
                    ""
            );

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    // ==================== INSERT ENTITY ====================

    public boolean insert(LichHen lichHen) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            insertAppointment(
                    conn,
                    lichHen.getMaLichHen(),
                    lichHen.getMaKhachHang(),
                    lichHen.getMaNhanVien(),
                    lichHen.getNgayHen(),
                    lichHen.getGioKham(),
                    lichHen.getGhiChu()
            );

            return true;
        }
    }
}
