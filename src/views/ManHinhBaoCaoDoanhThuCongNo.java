package views;

import java.time.LocalDate;
import models.dao.BaoCaoPhongKhamDAO;

public class ManHinhBaoCaoDoanhThuCongNo extends ManHinhBaoCaoTheoNgay {
    public ManHinhBaoCaoDoanhThuCongNo() {
        super("Doanh Thu, Chi Phí & Công Nợ",
                "Tổng hợp hóa đơn và phiếu thanh toán theo ngày/tháng/năm. CSDL hiện chưa có bảng ghi nhận chi phí hoạt động.",
                new String[]{"Kỳ", "Số hóa đơn", "Doanh thu", "Đã thu", "Công nợ", "Chi phí"});
    }

    @Override
    protected KetQuaBaoCao fetchReport(LocalDate from, LocalDate to, ChuKyBaoCao period) throws Exception {
        String key = periodExpression("H.ngayLap", period);
        return query(BaoCaoPhongKhamDAO.revenueDebtQuery(key), from, to, 6);
    }
}
