package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng NhanVien
 */
public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private Date ngaySinh;
    private String chucVu;
    private String lichLamViec;
    private String trinhDo;
    private double mucLuong;

    public NhanVien() {}

    public NhanVien(String maNhanVien, String tenNhanVien, Date ngaySinh, String chucVu, String lichLamViec, String trinhDo, double mucLuong) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.ngaySinh = ngaySinh;
        this.chucVu = chucVu;
        this.lichLamViec = lichLamViec;
        this.trinhDo = trinhDo;
        this.mucLuong = mucLuong;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getLichLamViec() { return lichLamViec; }
    public void setLichLamViec(String lichLamViec) { this.lichLamViec = lichLamViec; }

    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }

    public double getMucLuong() { return mucLuong; }
    public void setMucLuong(double mucLuong) { this.mucLuong = mucLuong; }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", tenNhanVien='" + tenNhanVien + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", chucVu='" + chucVu + '\'' +
                ", trinhDo='" + trinhDo + '\'' +
                ", mucLuong=" + mucLuong +
                '}';
    }
}
