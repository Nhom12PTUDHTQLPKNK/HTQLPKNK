package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng HoSoBenhAn
 */
public class HoSoBenhAn {
    private String maBenhAn;
    private String maKhachHang;
    private String maNhanVien;
    private Date ngayKham;
    private String ketQuaKham;
    private String chuanDoan;
    private String trangThai;

    public HoSoBenhAn() {}

    public HoSoBenhAn(String maBenhAn, String maKhachHang, String maNhanVien, Date ngayKham, String ketQuaKham, String chuanDoan, String trangThai) {
        this.maBenhAn = maBenhAn;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.ngayKham = ngayKham;
        this.ketQuaKham = ketQuaKham;
        this.chuanDoan = chuanDoan;
        this.trangThai = trangThai;
    }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayKham() { return ngayKham; }
    public void setNgayKham(Date ngayKham) { this.ngayKham = ngayKham; }

    public String getKetQuaKham() { return ketQuaKham; }
    public void setKetQuaKham(String ketQuaKham) { this.ketQuaKham = ketQuaKham; }

    public String getChuanDoan() { return chuanDoan; }
    public void setChuanDoan(String chuanDoan) { this.chuanDoan = chuanDoan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "HoSoBenhAn{" +
                "maBenhAn='" + maBenhAn + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", ngayKham=" + ngayKham +
                ", chuanDoan='" + chuanDoan + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
