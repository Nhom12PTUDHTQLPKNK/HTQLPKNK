package views;

public class AppointmentSearchPanel extends SqlSearchPanel {
    public AppointmentSearchPanel() {
        super("Tra Cứu Lịch Hẹn Khám",
                "Nhập mã lịch, mã hoặc tên khách hàng, số điện thoại để tra cứu lịch hẹn",
                new String[]{"Mã lịch hẹn", "Mã KH", "Tên khách hàng", "Số điện thoại", "Nha sĩ phụ trách", "Ngày hẹn", "Giờ hẹn", "Trạng thái", "Ghi chú"},
                "SELECT L.maLichHen, K.maKhachHang, K.tenKH, K.sdt, N.tenNhanVien, "
                + "CONVERT(VARCHAR(10), L.ngayHen, 103) AS ngayHen, L.gioKham, L.trangThai, L.ghiChu "
                + "FROM LichHen L "
                + "LEFT JOIN KhachHang K ON L.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N ON L.maNhanVien = N.maNhanVien",
                "Danh sách tra cứu lịch hẹn", "maLichHen", "maKhachHang", "tenKH", "sdt", "tenNhanVien", "trangThai");
    }
}
