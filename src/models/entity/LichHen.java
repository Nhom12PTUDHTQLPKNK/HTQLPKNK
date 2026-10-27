package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng LichHen
 */
public class LichHen {
    private String maLichHen;
    private String maKhachHang;
    private String maNhanVien;
    private Date ngayHen;
    private String trangThai;
    private String ghiChu;

    public LichHen() {}

    public LichHen(String maLichHen, String maKhachHang, String maNhanVien, Date ngayHen, String trangThai, String ghiChu) {
        this.maLichHen = maLichHen;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.ngayHen = ngayHen;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public String getMaLichHen() { return maLichHen; }
    public void setMaLichHen(String maLichHen) { this.maLichHen = maLichHen; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayHen() { return ngayHen; }
    public void setNgayHen(Date ngayHen) { this.ngayHen = ngayHen; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return "LichHen{" +
                "maLichHen='" + maLichHen + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", ngayHen=" + ngayHen +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
