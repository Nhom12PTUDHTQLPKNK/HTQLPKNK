package controllers;

import java.sql.SQLException;
import java.util.Date;
import models.dao.LichHenDAO;
import models.entity.LichHen;

/**
 * Controller quản lý lịch hẹn và chống trùng lịch trực tiếp với CSDL
 */
public class AppointmentController {
    private LichHenDAO lichHenDAO = new LichHenDAO();

    /**
     * Kiểm tra tự động chống trùng lịch khám của Nha sĩ theo Ngày và Giờ khám
     */
    public boolean checkOverlappingAppointment(String maNhanVien, Date ngayHen, String gioKham) {
        if (maNhanVien == null || ngayHen == null || gioKham == null) {
            return true; 
        }
        return lichHenDAO.checkOverlap(maNhanVien, ngayHen, gioKham);
    }

    /**
     * Tạo lịch hẹn mới và lưu xuống CSDL (Mặc định trạng thái là 'Chờ khám')
     */
    public boolean createAppointment(LichHen lichHen) throws SQLException {
        if (lichHen == null || lichHen.getMaKhachHang() == null || lichHen.getMaNhanVien() == null || lichHen.getNgayHen() == null || lichHen.getGioKham() == null) {
            System.err.println("Lỗi: Dữ liệu lịch hẹn không đầy đủ!");
            return false;
        }

        // Kiểm tra xem nha sĩ đã bị trùng lịch vào khung giờ đó chưa
        if (checkOverlappingAppointment(lichHen.getMaNhanVien(), lichHen.getNgayHen(), lichHen.getGioKham())) {
            System.err.println("Nha sĩ đã có lịch hẹn trùng vào khung giờ này trong ngày!");
            return false;
        }

        // Đồng bộ trạng thái ban đầu là 'Chờ khám' khớp với giao diện Panel
        lichHen.setTrangThai("Chờ khám");
        return lichHenDAO.insert(lichHen);
    }

    /**
     * Hủy lịch hẹn
     */
    public boolean cancelAppointment(String maLichHen) {
        if (maLichHen == null || maLichHen.trim().isEmpty()) {
            return false;
        }
        return lichHenDAO.cancelAppointment(maLichHen);
    }
}