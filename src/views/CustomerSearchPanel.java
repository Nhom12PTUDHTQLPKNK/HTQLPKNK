package views;

/** Tra cứu khách hàng và điểm từ các cột hiện có trong SQL Server. */
public class CustomerSearchPanel extends SqlSearchPanel {
    public CustomerSearchPanel() {
        super("Tra Cứu Khách Hàng & Điểm Tích Lũy",
                "Tra cứu theo mã, họ tên hoặc số điện thoại; dữ liệu lấy trực tiếp từ SQL Server",
                new String[]{"Mã KH", "Họ tên", "Số điện thoại", "Ngày sinh", "Địa chỉ", "Điểm tích lũy"},
                "SELECT maKhachHang, tenKH, sdt, CONVERT(VARCHAR(10), ngaySinh, 103) AS ngaySinh, diaChi, diemTichLuy FROM KhachHang",
                "Tra cứu khách hàng và điểm tích lũy", "maKhachHang", "tenKH", "sdt");
    }
}
