package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import models.dao.LichHenDAO;
import models.entity.LichHen;

/**
 * Controller quản lý lịch hẹn và chống trùng lịch trực tiếp với CSDL
 */
public class AppointmentController {
    private LichHenDAO lichHenDAO = new LichHenDAO();

    /**
     * Kiểm tra tự động chống trùng lịch khám của Nha sĩ
     */
    public boolean checkOverlappingAppointment(String maNhanVien, Date ngayHen) {
        if (maNhanVien == null || ngayHen == null) {
            return true;
        }
        return lichHenDAO.checkOverlap(maNhanVien, ngayHen);
    }

    /**
     * Tạo lịch hẹn mới và lưu xuống CSDL
     */
    public boolean createAppointment(LichHen lichHen) {
        if (lichHen == null || lichHen.getMaKhachHang() == null || lichHen.getMaNhanVien() == null || lichHen.getNgayHen() == null) {
            System.err.println("Lỗi: Dữ liệu lịch hẹn không đầy đủ!");
            return false;
        }

        if (checkOverlappingAppointment(lichHen.getMaNhanVien(), lichHen.getNgayHen())) {
            System.err.println("Nha sĩ đã có lịch hẹn trùng khung giờ/ngày này!");
            return false;
        }

        lichHen.setTrangThai("Đã xác nhận");
        return lichHenDAO.insert(lichHen);
    }
}