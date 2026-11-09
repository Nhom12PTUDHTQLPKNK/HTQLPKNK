package models.dao;

import sql.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy vấn dữ liệu cho bảng tổng hợp quy trình lâm sàng. */
public class QuyTrinhLamSangDAO {
    public List<Object[]> getRows(String workflow) throws SQLException {
        String sql = queryFor(workflow);
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            int columnCount = result.getMetaData().getColumnCount();
            while (result.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) row[i] = result.getObject(i + 1);
                rows.add(row);
            }
        }
        return rows;
    }

    private String queryFor(String workflow) {
        if ("TREATMENT_PLAN".equals(workflow))
            return "SELECT maKeHoach, maBenhAn, maNhanVien, maDichVu, ngayLap, soLankham, thoiGianDuKien, tongChiPhiDuKien, trangThai FROM KeHoachDieuTri ORDER BY ngayLap DESC";
        if ("PRESCRIPTION".equals(workflow))
            return "SELECT maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, soLuong, cachDung FROM DonThuoc ORDER BY ngayKe DESC";
        if ("FOLLOW_UP".equals(workflow))
            return "SELECT lh.maLichHen, lh.maKhachHang, kh.tenKH, lh.maNhanVien, nv.tenNhanVien, lh.ngayHen, lh.gioKham, lh.trangThai, lh.ghiChu FROM LichHen lh LEFT JOIN KhachHang kh ON kh.maKhachHang = lh.maKhachHang LEFT JOIN NhanVien nv ON nv.maNhanVien = lh.maNhanVien WHERE lh.ghiChu LIKE N'%Tái khám%' ORDER BY lh.ngayHen DESC";
        throw new IllegalArgumentException("Quy trình không được hỗ trợ: " + workflow);
    }
}
