package models.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import sql.DBConnection;

public class DonThuocDienTuDAO {

    // Lấy thông tin chi tiết bệnh án, khách hàng, kế hoạch điều trị theo mã bệnh án
    public ResultSet getPrescriptionHeaderInfo(String maBenhAn) throws SQLException {
        String query = "SELECT ba.maBenhAn, kh.maKhachHang, kh.tenKH, kh.ngaySinh, kh.gioiTinh, kh.sdt, " +
                       "kh.tienSuBenh, kh.diemTichLuy, kh.ngaySinh, " +
                       "khd.maKeHoach, dv.tenDichVu, nv.maNhanVien, nv.tenNhanVien, " +
                       "khd.ngayLap, khd.trangThai, khd.tongChiPhiDuKien " +
                       "FROM HoSoBenhAn ba " +
                       "JOIN KhachHang kh ON ba.maKhachHang = kh.maKhachHang " +
                       "JOIN NhanVien nv ON ba.maNhanVien = nv.maNhanVien " +
                       "LEFT JOIN KeHoachDieuTri khd ON ba.maBenhAn = khd.maBenhAn " +
                       "LEFT JOIN DichVu dv ON khd.maDichVu = dv.maDichVu " +
                       "WHERE ba.maBenhAn = ?";
        Connection conn = DBConnection.getConnection(); // Giả định lớp kết nối CSDL của bạn
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maBenhAn);
        return pst.executeQuery();
    }

    // Lấy danh sách thuốc đã kê cho đơn thuốc
    public List<Object[]> getPrescriptionDetails(String maBenhAn) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String query = "SELECT dt.maThuoc, t.TenThuoc, dbc.TenDangBaoChe, ld.LieuDung, ld.TanSuat, " +
                       "ld.ThoiGianSuDung, dt.soLuong, t.QuyCachDongGoi, dt.cachDung, dt.ghiChu " +
                       "FROM DonThuoc dt " +
                       "JOIN THUOC t ON dt.maThuoc = t.MaThuoc " +
                       "JOIN DANGBAOCHE dbc ON t.MaDangBaoChe = dbc.MaDangBaoChe " +
                       "LEFT JOIN THUOCLIEUDUNG ld ON t.MaThuoc = ld.MaThuoc " +
                       "WHERE dt.maBenhAn = ?";
        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maBenhAn);
        ResultSet rs = pst.executeQuery();
        int stt = 1;
        while (rs.next()) {
            list.add(new Object[]{
                stt++,
                rs.getString("maThuoc"),
                rs.getString("TenThuoc"),
                rs.getString("TenDangBaoChe"),
                rs.getString("LieuDung"),
                rs.getString("TanSuat"),
                rs.getString("ThoiGianSuDung"),
                rs.getInt("soLuong"),
                rs.getString("QuyCachDongGoi"), // Đơn vị tính/Quy cách
                rs.getString("cachDung")
            });
        }
        return list;
    }

    // Lưu hoặc cập nhật đơn thuốc vào CSDL SQL Server
    public void savePrescription(String maDonThuoc, String maBenhAn, String maNhanVien, 
                                 String maKhachHang, String maThuoc, String tenThuoc, 
                                 int soLuong, String cachDung, String ghiChu) throws SQLException {
        String query = "INSERT INTO DonThuoc (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, soLuong, cachDung, ghiChu) " +
                       "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, maDonThuoc);
        pst.setString(2, maBenhAn);
        pst.setString(3, maNhanVien);
        pst.setString(4, maKhachHang);
        pst.setString(5, maThuoc);
        pst.setString(6, tenThuoc);
        pst.setInt(7, soLuong);
        pst.setString(8, cachDung);
        pst.setString(9, ghiChu);
        pst.executeUpdate();
    }
}