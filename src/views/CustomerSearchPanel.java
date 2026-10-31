package views;

public class CustomerSearchPanel extends SqlSearchPanel {
    public CustomerSearchPanel() {
        super("Tra Cứu Khách Hàng & Điểm Tích Lũy",
                "Nhập mã khách hàng, tên hoặc số điện thoại để tra cứu thông tin thành viên",
                new String[]{"Mã KH", "Họ tên", "Số điện thoại", "Địa chỉ", "Điểm tích lũy", "Hạng thành viên"},
                "SELECT maKhachHang, tenKH, sdt, diaChi, COALESCE(diemTichLuy, 0) AS diemTichLuy, "
                + "CASE WHEN COALESCE(diemTichLuy, 0) >= 500 THEN N'VIP Kim Cương' "
                + "WHEN COALESCE(diemTichLuy, 0) >= 300 THEN N'VIP' "
                + "WHEN COALESCE(diemTichLuy, 0) >= 100 THEN N'Thân thiết' ELSE N'Thành viên' END AS hangThanhVien "
                + "FROM KhachHang",
                "Tra cứu khách hàng và điểm tích lũy", "maKhachHang", "tenKH", "sdt");
    }
}
