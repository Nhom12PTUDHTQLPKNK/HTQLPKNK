package controllers;

import models.dao.KhachHangDAO;
import models.entity.KhachHang;
import java.sql.SQLException;
import java.util.List;


public class PatientController {
    private KhachHangDAO khachHangDAO = new KhachHangDAO();

 
    public boolean addPatient(KhachHang khachHang) throws SQLException {
        if (khachHang == null) return false;
        
        if (khachHang.getTenKH() == null || khachHang.getTenKH().trim().isEmpty()) {
            System.err.println("Lỗi: Tên bệnh nhân không được để trống!");
            return false;
        }
        
        if (khachHang.getSdt() == null || khachHang.getSdt().trim().isEmpty()) {
            System.err.println("Lỗi: Số điện thoại không được để trống!");
            return false;
        }

        return khachHangDAO.insert(khachHang);
    }

 
    public KhachHang findPatient(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return khachHangDAO.findByPhoneOrId(keyword);
    }

    public boolean updatePatient(KhachHang patient) throws SQLException {
        return patient != null && khachHangDAO.update(patient);
    }

    public boolean deletePatient(String patientId) throws SQLException {
        return patientId != null && khachHangDAO.delete(patientId);
    }

    public List<KhachHang> getPatients() throws SQLException {
        return khachHangDAO.findAll();
    }
}
