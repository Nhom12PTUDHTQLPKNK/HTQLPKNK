package views;

import java.time.LocalDate;

public class ServiceDentistReportPanel extends SqlDateReportPanel {
    public ServiceDentistReportPanel() {
        super("Thống Kê Dịch Vụ & Hiệu Suất Nha Sĩ",
                "Đếm hồ sơ khám theo nha sĩ, dịch vụ đã lập hóa đơn và doanh thu trong khoảng ngày chọn",
                new String[]{"Kỳ", "Mã nha sĩ", "Nha sĩ", "Mã dịch vụ", "Dịch vụ", "Số hồ sơ", "Số lượng", "Doanh thu"});
    }

    @Override
    protected ReportResult fetchReport(LocalDate from, LocalDate to, Period period) throws Exception {
        String key = periodExpression("BA.ngayKham", period);
        String sql = "SELECT " + key + " AS ky, N.maNhanVien, N.tenNhanVien, D.maDichVu, "
                + "COALESCE(D.tenDichVu, N'Chưa ghi nhận dịch vụ') AS tenDichVu, "
                + "COUNT(DISTINCT BA.maBenhAn) AS soHoSo, "
                + "SUM(COALESCE(CT.soLuong, 0)) AS soLuong, "
                + "SUM(COALESCE(CT.soLuong, 0) * COALESCE(CT.donGia, 0)) AS doanhThu "
                + "FROM HoSoBenhAn BA "
                + "INNER JOIN NhanVien N ON BA.maNhanVien = N.maNhanVien "
                + "LEFT JOIN HoaDon H ON H.maBenhAn = BA.maBenhAn "
                + "LEFT JOIN ChiTietHoaDon CT ON CT.maHoaDon = H.maHoaDon "
                + "LEFT JOIN DichVu D ON D.maDichVu = CT.maDichVu "
                + "WHERE BA.ngayKham >= ? AND BA.ngayKham <= ? "
                + "AND (N.chucVu LIKE N'%Bác sĩ%' OR N.chucVu LIKE N'%Nha sĩ%') "
                + "GROUP BY " + key + ", N.maNhanVien, N.tenNhanVien, D.maDichVu, D.tenDichVu "
                + "ORDER BY MIN(BA.ngayKham), N.tenNhanVien, D.tenDichVu";
        return query(sql, from, to, 8);
    }
}
