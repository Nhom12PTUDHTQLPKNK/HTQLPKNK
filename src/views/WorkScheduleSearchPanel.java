package views;

public class WorkScheduleSearchPanel extends SqlSearchPanel {
    public WorkScheduleSearchPanel() {
        super("Tra Cứu Lịch Làm Việc",
                "Tra cứu lịch phân ca theo nhân viên, ngày trực và trạng thái",
                new String[]{"Mã lịch", "Mã nhân viên", "Tên nhân viên", "Chức vụ", "Ngày trực", "Ca làm", "Trạng thái"},
                "SELECT L.maLichLamViec, N.maNhanVien, N.tenNhanVien, N.chucVu, "
                + "CONVERT(VARCHAR(10), L.ngayLamViec, 103) AS ngayLamViec, "
                + "COALESCE(L.gioBatDau, '') + CASE WHEN L.gioKetThuc IS NULL THEN '' ELSE ' - ' + L.gioKetThuc END AS caLam, "
                + "L.trangThai FROM LichLamViec L "
                + "LEFT JOIN NhanVien N ON L.maNhanVien = N.maNhanVien",
                "Tra cứu lịch làm việc", "maLichLamViec", "maNhanVien", "tenNhanVien", "chucVu", "trangThai");
    }
}
