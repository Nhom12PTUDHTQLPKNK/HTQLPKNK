package models.dao;

/** SQL source queries used by reusable search panels. */
public final class CauTruyVanTimKiemDAO {
    private CauTruyVanTimKiemDAO() {}

    public static String invoiceLookup() {
        return "SELECT H.maHoaDon, K.maKhachHang, K.tenKH, N.tenNhanVien, KM.tenKhuyenMai, "
                + "CONVERT(VARCHAR(10), H.ngayLap, 103) AS ngayLap, "
                + "CAST(COALESCE(CT.tongTien, 0) AS DECIMAL(18, 0)) AS tongTien "
                + "FROM HoaDon H LEFT JOIN KhachHang K ON H.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N ON H.maNhanVien = N.maNhanVien "
                + "LEFT JOIN KhuyenMai KM ON H.maKhuyenMai = KM.maKhuyenMai "
                + "LEFT JOIN (SELECT maHoaDon, SUM(CAST(soLuong AS DECIMAL(18, 2)) * donGia) AS tongTien "
                + "FROM ChiTietHoaDon GROUP BY maHoaDon) CT ON H.maHoaDon = CT.maHoaDon";
    }

    public static String paymentLookup() {
        return "SELECT P.maPhieu, P.maHoaDon, K.maKhachHang, K.tenKH, N.tenNhanVien, "
                + "CONVERT(VARCHAR(10), P.ngayThanhToan, 103) AS ngayThanhToan, "
                + "P.hinhThucThanhToan, P.daThanhToan, P.soTienConLai, P.ghiChu "
                + "FROM PhieuThanhToan P LEFT JOIN KhachHang K ON P.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N ON P.maNhanVien = N.maNhanVien";
    }

    public static String promotionLookup() {
        return "SELECT maKhuyenMai, tenKhuyenMai, CONVERT(VARCHAR(30), giaTriGiam) + '%' AS [Mức giảm], "
                + "CONVERT(VARCHAR(10), ngayBatDau, 103) AS [Ngày bắt đầu], "
                + "CONVERT(VARCHAR(10), ngayKetThuc, 103) AS [Ngày kết thúc], "
                + "CASE WHEN trangThai = 1 THEN N'Đang áp dụng' ELSE N'Ngừng áp dụng' END AS [Trạng thái] "
                + "FROM KhuyenMai";
    }

    public static String workScheduleLookup() {
        return "SELECT L.maLichLamViec, N.maNhanVien, N.tenNhanVien, N.chucVu, "
                + "CONVERT(VARCHAR(10), L.ngayLamViec, 103) AS ngayLamViec, "
                + "COALESCE(L.gioBatDau, '') + CASE WHEN L.gioKetThuc IS NULL THEN '' ELSE ' - ' + L.gioKetThuc END AS caLam, "
                + "L.trangThai, CAST(L.ngayLamViec AS DATE) AS ngayLamViecFilter "
                + "FROM LichLamViec L LEFT JOIN NhanVien N ON L.maNhanVien = N.maNhanVien";
    }
}
