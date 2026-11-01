package views;

/** Tra cứu danh mục khuyến mãi từ bảng KhuyenMai. */
public class PromotionPanel extends SqlSearchPanel {
    public PromotionPanel() {
        super("Danh Mục: Chương Trình Khuyến Mãi",
                "Danh sách ưu đãi, mức giảm và thời gian áp dụng trong SQL Server",
                new String[]{"Mã KM", "Tên chương trình", "Mức giảm (%)", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"},
                "SELECT maKhuyenMai, tenKhuyenMai, giaTriGiam, CONVERT(VARCHAR(10), ngayBatDau, 103) AS ngayBatDau, "
                        + "CONVERT(VARCHAR(10), ngayKetThuc, 103) AS ngayKetThuc, "
                        + "CASE WHEN trangThai = 1 THEN N'Đang áp dụng' ELSE N'Ngừng áp dụng' END AS trangThai FROM KhuyenMai",
                "Danh sách khuyến mãi", "maKhuyenMai", "tenKhuyenMai", "trangThai");
    }
}
