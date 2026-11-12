package models.dao;

/** Query definitions for date based clinic reports. */
public final class BaoCaoPhongKhamDAO {
    private BaoCaoPhongKhamDAO() {}

    public static String periodExpression(String column, String period) {
        if ("MONTH".equals(period)) return "CONVERT(CHAR(7), " + column + ", 126)";
        if ("YEAR".equals(period)) return "CONVERT(CHAR(4), YEAR(" + column + "))";
        return "CONVERT(VARCHAR(10), " + column + ", 103)";
    }

    public static String revenueDebtQuery(String key) {
        return "SELECT " + key + " AS ky, COUNT(*) AS soHoaDon, "
                + "SUM(COALESCE(T.tongTien, 0)) AS doanhThu, SUM(COALESCE(P.daThu, 0)) AS daThu, "
                + "SUM(CASE WHEN COALESCE(T.tongTien, 0) - COALESCE(P.daThu, 0) > 0 "
                + "THEN COALESCE(T.tongTien, 0) - COALESCE(P.daThu, 0) ELSE 0 END) AS congNo, "
                + "CAST(NULL AS DECIMAL(18,2)) AS chiPhi FROM HoaDon H "
                + "LEFT JOIN (SELECT maHoaDon, SUM(CAST(soLuong AS DECIMAL(18,2)) * donGia) AS tongTien "
                + "FROM ChiTietHoaDon GROUP BY maHoaDon) T ON H.maHoaDon = T.maHoaDon "
                + "LEFT JOIN (SELECT maHoaDon, SUM(daThanhToan) AS daThu FROM PhieuThanhToan GROUP BY maHoaDon) P "
                + "ON H.maHoaDon = P.maHoaDon WHERE H.ngayLap >= ? AND H.ngayLap < DATEADD(DAY, 1, ?) "
                + "GROUP BY " + key + " ORDER BY MIN(H.ngayLap)";
    }

    public static String serviceDentistQuery(String key) {
        return "SELECT " + key + " AS ky, N.maNhanVien, N.tenNhanVien, D.maDichVu, "
                + "COALESCE(D.tenDichVu, N'Chưa ghi nhận dịch vụ') AS tenDichVu, "
                + "COUNT(DISTINCT BA.maBenhAn) AS soHoSo, SUM(COALESCE(CT.soLuong, 0)) AS soLuong, "
                + "SUM(COALESCE(CT.soLuong, 0) * COALESCE(CT.donGia, 0)) AS doanhThu "
                + "FROM HoSoBenhAn BA INNER JOIN NhanVien N ON BA.maNhanVien = N.maNhanVien "
                + "LEFT JOIN HoaDon H ON H.maBenhAn = BA.maBenhAn "
                + "LEFT JOIN ChiTietHoaDon CT ON CT.maHoaDon = H.maHoaDon "
                + "LEFT JOIN DichVu D ON D.maDichVu = CT.maDichVu "
                + "WHERE BA.ngayKham >= ? AND BA.ngayKham < DATEADD(DAY, 1, ?) "
                + "AND (N.chucVu LIKE N'%Bác sĩ%' OR N.chucVu LIKE N'%Nha sĩ%') "
                + "GROUP BY " + key + ", N.maNhanVien, N.tenNhanVien, D.maDichVu, D.tenDichVu "
                + "ORDER BY MIN(BA.ngayKham), N.tenNhanVien, D.tenDichVu";
    }
}
