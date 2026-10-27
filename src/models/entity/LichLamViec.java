package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng LichLamViec
 */
public class LichLamViec {
    private String maLichLamViec;
    private String maNhanVien;
    private Date ngayLamViec;
    private String gioBatDau;
    private String gioKetThuc;
    private String trangThai;

    public LichLamViec() {}

    public LichLamViec(String maLichLamViec, String maNhanVien, Date ngayLamViec, String gioBatDau, String gioKetThuc, String trangThai) {
        this.maLichLamViec = maLichLamViec;
        this.maNhanVien = maNhanVien;
        this.ngayLamViec = ngayLamViec;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
    }

    public String getMaLichLamViec() { return maLichLamViec; }
    public void setMaLichLamViec(String maLichLamViec) { this.maLichLamViec = maLichLamViec; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public Date getNgayLamViec() { return ngayLamViec; }
    public void setNgayLamViec(Date ngayLamViec) { this.ngayLamViec = ngayLamViec; }

    public String getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(String gioBatDau) { this.gioBatDau = gioBatDau; }

    public String getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(String gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "LichLamViec{" +
                "maLichLamViec='" + maLichLamViec + '\'' +
                ", maNhanVien='" + maNhanVien + '\'' +
                ", ngayLamViec=" + ngayLamViec +
                ", gioBatDau='" + gioBatDau + '\'' +
                ", gioKetThuc='" + gioKetThuc + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
