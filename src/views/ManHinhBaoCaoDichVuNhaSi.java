package views;

import controllers.DieuKhienBaoCaoNgay;

public class ManHinhBaoCaoDichVuNhaSi extends ManHinhBaoCaoTheoNgay {
    public ManHinhBaoCaoDichVuNhaSi() {
        super("Thống Kê Dịch Vụ & Hiệu Suất Nha Sĩ",
                "Đếm hồ sơ khám theo nha sĩ, dịch vụ đã lập hóa đơn và doanh thu trong khoảng ngày chọn",
                new String[]{"Kỳ", "Mã nha sĩ", "Nha sĩ", "Mã dịch vụ", "Dịch vụ", "Số hồ sơ", "Số lượng", "Doanh thu"},
                DieuKhienBaoCaoNgay.DICH_VU_NHA_SI);
    }
}
