package views;

import java.time.LocalDate;

public class RevenueDebtReportPanel extends SqlDateReportPanel {
    public RevenueDebtReportPanel() {
        super("Doanh Thu, Chi Phí & Công Nợ",
                "Tổng hợp hóa đơn và phiếu thanh toán theo ngày/tháng/năm. CSDL hiện chưa có bảng ghi nhận chi phí hoạt động.",
                new String[]{"Kỳ", "Số hóa đơn", "Doanh thu", "Đã thu", "Công nợ", "Chi phí"});
    }

    @Override
    protected ReportResult fetchReport(LocalDate from, LocalDate to, Period period) throws Exception {
        String key = periodExpression("H.ngayLap", period);
        String sql = "SELECT " + key + " AS ky, COUNT(*) AS soHoaDon, "
                + "SUM(COALESCE(T.tongTien, 0)) AS doanhThu, "
                + "SUM(COALESCE(P.daThu, 0)) AS daThu, "
                + "SUM(CASE WHEN COALESCE(T.tongTien, 0) - COALESCE(P.daThu, 0) > 0 "
                + "THEN COALESCE(T.tongTien, 0) - COALESCE(P.daThu, 0) ELSE 0 END) AS congNo, "
                + "CAST(NULL AS DECIMAL(18,2)) AS chiPhi "
                + "FROM HoaDon H "
                + "LEFT JOIN (SELECT maHoaDon, SUM(CAST(soLuong AS DECIMAL(18,2)) * donGia) AS tongTien "
                + "FROM ChiTietHoaDon GROUP BY maHoaDon) T ON H.maHoaDon = T.maHoaDon "
                + "LEFT JOIN (SELECT maHoaDon, SUM(daThanhToan) AS daThu "
                + "FROM PhieuThanhToan GROUP BY maHoaDon) P ON H.maHoaDon = P.maHoaDon "
                + "WHERE H.ngayLap >= ? AND H.ngayLap <= ? "
                + "GROUP BY " + key + " ORDER BY MIN(H.ngayLap)";
        return query(sql, from, to, 6);
    }
}
