package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng HoaDon
 */
public class HoaDon {
    private String maHoaDon;
    private String maBenhAn;
    private String maKhachHang;
    private String maNhanVien;
    private String maKhuyenMai;
    private Date ngayLap;

    public HoaDon() {}

    public HoaDon(String maHoaDon, String maBenhAn, String maKhachHang, String maNhanVien, String maKhuyenMai, Date ngayLap) {
        this.maHoaDon = maHoaDon;
        this.maBenhAn = maBenhAn;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.maKhuyenMai = maKhuyenMai;
        this.ngayLap = ngayLap;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", maBenhAn='" + maBenhAn + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", ngayLap=" + ngayLap +
                '}';
    }
}
