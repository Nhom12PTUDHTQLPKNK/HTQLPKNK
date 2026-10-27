package models.entity;

import java.sql.Date;

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
    private double daThanhToan;
    private double soTienConLai;
    private String ghiChu;

    public PhieuThanhToan() {}

    public PhieuThanhToan(String maPhieu, String maHoaDon, String maKhachHang, String maNhanVien, Date ngayThanhToan, String hinhThucThanhToan, double daThanhToan, double soTienConLai, String ghiChu) {
        this.maPhieu = maPhieu;
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.ngayThanhToan = ngayThanhToan;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.daThanhToan = daThanhToan;
        this.soTienConLai = soTienConLai;
        this.ghiChu = ghiChu;
    }

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

    public double getDaThanhToan() { return daThanhToan; }
    public void setDaThanhToan(double daThanhToan) { this.daThanhToan = daThanhToan; }

    public double getSoTienConLai() { return soTienConLai; }
    public void setSoTienConLai(double soTienConLai) { this.soTienConLai = soTienConLai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return "PhieuThanhToan{" +
                "maPhieu='" + maPhieu + '\'' +
                ", maHoaDon='" + maHoaDon + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", ngayThanhToan=" + ngayThanhToan +
                ", hinhThucThanhToan='" + hinhThucThanhToan + '\'' +
                ", daThanhToan=" + daThanhToan +
                ", soTienConLai=" + soTienConLai +
                '}';
    }
}
