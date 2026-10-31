package views;

public class PromotionSearchPanel extends SqlSearchPanel {
    public PromotionSearchPanel() {
        super("Tra Cứu Chương Trình Khuyến Mãi",
                "Tra cứu chương trình, mức giảm, thời gian áp dụng và trạng thái",
                new String[]{"Mã KM", "Tên chương trình", "Mức giảm", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"},
                "SELECT maKhuyenMai, tenKhuyenMai, "
                + "CONVERT(VARCHAR(30), giaTriGiam) + '%' AS mucGiam, "
                + "CONVERT(VARCHAR(10), ngayBatDau, 103) AS ngayBatDau, "
                + "CONVERT(VARCHAR(10), ngayKetThuc, 103) AS ngayKetThuc, "
                + "CASE WHEN trangThai = 1 THEN N'Đang áp dụng' ELSE N'Ngừng áp dụng' END AS trangThai "
                + "FROM KhuyenMai",
                "Tra cứu khuyến mãi", "maKhuyenMai", "tenKhuyenMai", "trangThai");
    }
}
