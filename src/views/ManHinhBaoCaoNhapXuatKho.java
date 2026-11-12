package views;

import controllers.DieuKhienBaoCaoNgay;

public class ManHinhBaoCaoNhapXuatKho extends ManHinhBaoCaoTheoNgay {
    public ManHinhBaoCaoNhapXuatKho() {
        super("Thống Kê Nhập, Xuất & Tồn Kho",
                "Xuất thuốc thống kê từ đơn thuốc; tồn vật tư là số hiện tại. CSDL chưa lưu lịch sử nhập kho hoặc lịch sử biến động tồn.",
                new String[]{"Kỳ", "Loại số liệu", "Mã hàng", "Tên hàng", "Số lượng", "Đơn vị / ghi chú"},
                DieuKhienBaoCaoNgay.NHAP_XUAT_KHO);
    }
}
