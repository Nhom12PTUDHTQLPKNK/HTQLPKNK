package views;

import java.time.LocalDate;
import models.dao.BaoCaoKhoDAO;

public class ManHinhBaoCaoNhapXuatKho extends ManHinhBaoCaoTheoNgay {
    public ManHinhBaoCaoNhapXuatKho() {
        super("Thống Kê Nhập, Xuất & Tồn Kho",
                "Xuất thuốc thống kê từ đơn thuốc; tồn vật tư là số hiện tại. CSDL chưa lưu lịch sử nhập kho hoặc lịch sử biến động tồn.",
                new String[]{"Kỳ", "Loại số liệu", "Mã hàng", "Tên hàng", "Số lượng", "Đơn vị / ghi chú"});
    }

    @Override
    protected KetQuaBaoCao fetchReport(LocalDate from, LocalDate to, ChuKyBaoCao period) throws Exception {
        String key = periodExpression("D.ngayKe", period);
        BaoCaoKhoDAO.DuLieuBaoCao data = new BaoCaoKhoDAO().fetch(from, to, key);
        return new KetQuaBaoCao(data.rows, "Dòng xuất theo đơn: " + data.issuedRows
                + " (bao gồm snapshot tồn hiện tại; không có lịch sử nhập kho trong lược đồ hiện tại)");
    }
}
