package models.entity;

import java.util.Date;

/**
 * Entity class cho bảng KhuyenMai
 */
public class KhuyenMai {
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private Double giaTriGiam;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Boolean trangThai;

    public KhuyenMai() {}

    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }

    public String getTenKhuyenMai() { return tenKhuyenMai; }
    public void setTenKhuyenMai(String tenKhuyenMai) { this.tenKhuyenMai = tenKhuyenMai; }

    public Double getGiaTriGiam() { return giaTriGiam; }
    public void setGiaTriGiam(Double giaTriGiam) { this.giaTriGiam = giaTriGiam; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "KhuyenMai{" +
                "maKhuyenMai='" + maKhuyenMai + '\'' +
                ", tenKhuyenMai='" + tenKhuyenMai + '\'' +
                ", giaTriGiam=" + giaTriGiam +
                ", trangThai=" + trangThai +
                '}';
    }
}