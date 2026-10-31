package views;

public class InventorySearchPanel extends SqlSearchPanel {
    public InventorySearchPanel() {
        super("Tra Cứu Tồn Kho Thuốc & Vật Tư",
                "Số lượng tồn lấy từ bảng VatTu; danh mục THUOC chưa có trường theo dõi số lượng tồn",
                new String[]{"Mã hàng", "Tên hàng", "Nhóm", "Số lượng tồn", "Đơn vị", "Ngày cập nhật"},
                "SELECT maVatTu AS maHang, tenVatTu AS tenHang, N'Vật tư / thuốc trong kho' AS nhom, "
                + "COALESCE(CONVERT(NVARCHAR(30), soLuongTon), N'Chưa cập nhật') AS soLuongTon, "
                + "COALESCE(donViTinh, N'') AS donViTinh, "
                + "CONVERT(VARCHAR(10), ngaySuDung, 103) AS ngayCapNhat FROM VatTu "
                + "UNION ALL "
                + "SELECT T.MaThuoc, T.TenThuoc, N'Danh mục thuốc (chưa quản lý tồn)', "
                + "N'Chưa theo dõi', N'', N'' FROM THUOC T",
                "Tra cứu tồn kho thuốc và vật tư", "maHang", "tenHang", "nhom");
    }
}
