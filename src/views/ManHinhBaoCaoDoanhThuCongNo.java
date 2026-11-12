package views;

import controllers.DieuKhienBaoCaoNgay;

public class ManHinhBaoCaoDoanhThuCongNo extends ManHinhBaoCaoTheoNgay {
    public ManHinhBaoCaoDoanhThuCongNo() {
        super("Doanh Thu, Chi Phí & Công Nợ",
                "Tổng hợp hóa đơn và phiếu thanh toán theo ngày/tháng/năm. CSDL hiện chưa có bảng ghi nhận chi phí hoạt động.",
                new String[]{"Kỳ", "Số hóa đơn", "Doanh thu", "Đã thu", "Công nợ", "Chi phí"},
                DieuKhienBaoCaoNgay.DOANH_THU);
    }

    @Override
    protected int chartValueColumn(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if ("Doanh thu".equalsIgnoreCase(headers[i])) return i;
        }
        return -1;
    }

}
