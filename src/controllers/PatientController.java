package controllers;

import models.dao.KhachHangDAO;
import models.entity.KhachHang;


public class PatientController {
    private KhachHangDAO khachHangDAO = new KhachHangDAO();

 
    public boolean addPatient(KhachHang khachHang) {
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

 
    public KhachHang findPatient(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        return khachHangDAO.findByPhoneOrId(keyword);
    }
}