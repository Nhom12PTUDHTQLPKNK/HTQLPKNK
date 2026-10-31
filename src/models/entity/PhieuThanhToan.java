package models.entity;

import java.util.Date;

/**
 * Entity class cho bảng PhieuThanhToan
 */
public class PhieuThanhToan {
    private String maPhieu;
    private String maHoaDon;
    private String maKhachHang;
    private String maNhanVien;
    private Date ngayThanhToan;
    private String hinhThucThanhToan;
    private Double daThanhToan;
    private Double soTienConLai;
    private String ghiChu;

    public PhieuThanhToan() {}

    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(Date ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }

    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }

    public Double getDaThanhToan() { return daThanhToan; }
    public void setDaThanhToan(Double daThanhToan) { this.daThanhToan = daThanhToan; }

    public Double getSoTienConLai() { return soTienConLai; }
    public void setSoTienConLai(Double soTienConLai) { this.soTienConLai = soTienConLai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return "PhieuThanhToan{" +
                "maPhieu='" + maPhieu + '\'' +
                ", maHoaDon='" + maHoaDon + '\'' +
                ", daThanhToan=" + daThanhToan +
                ", soTienConLai=" + soTienConLai +
                ", hinhThucThanhToan='" + hinhThucThanhToan + '\'' +
                '}';
    }
}