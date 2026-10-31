package models.entity;

import java.util.Date;

/**
 * Entity class cho bảng NhanVien
 */
public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private Date ngaySinh;
    private String sdt;
    private String chucVu;
    private String lichLamViec;
    private String trinhDo;
    private Double mucLuong;

    public NhanVien() {}

    public NhanVien(String maNhanVien, String tenNhanVien, Date ngaySinh, String sdt, String chucVu, String lichLamViec, String trinhDo, Double mucLuong) {
        this.maNhanVien = maNhanVien;
        this.tenNhanVien = tenNhanVien;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt;
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

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String getLichLamViec() { return lichLamViec; }
    public void setLichLamViec(String lichLamViec) { this.lichLamViec = lichLamViec; }

    public String getTrinhDo() { return trinhDo; }
    public void setTrinhDo(String trinhDo) { this.trinhDo = trinhDo; }

    public Double getMucLuong() { return mucLuong; }
    public void setMucLuong(Double mucLuong) { this.mucLuong = mucLuong; }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", tenNhanVien='" + tenNhanVien + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", sdt='" + sdt + '\'' +
                '}';
    }
}