package views;

import models.dao.CauTruyVanTimKiemDAO;

public class ManHinhTraCuuLichLamViec extends ManHinhTraCuuDuLieu {
    public ManHinhTraCuuLichLamViec() {
        super("Tra Cứu Lịch Làm Việc",
                "Tra cứu lịch phân ca theo nhân viên, ngày trực và trạng thái",
                new String[]{"Mã lịch", "Mã nhân viên", "Tên nhân viên", "Chức vụ", "Ngày trực", "Ca làm", "Trạng thái"},
                CauTruyVanTimKiemDAO.workScheduleLookup(),
                "Tra cứu lịch làm việc",
                new String[]{"maLichLamViec", "maNhanVien"},
                "ngayLamViecFilter");
    }
}
