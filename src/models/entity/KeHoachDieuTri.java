package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng KeHoachDieuTri
 */
public class KeHoachDieuTri {
    private String maKeHoach;
    private String maBenhAn;
    private String maNhanVien;
    private String maDichVu;
    private Date ngayLap;
    private String ghiChu;
    private int soLankham;
    private String thoiGianDuKien;
    private double tongChiPhiDuKien;
    private String trangThai;

    public KeHoachDieuTri() {}

    public KeHoachDieuTri(String maKeHoach, String maBenhAn, String maNhanVien, String maDichVu, Date ngayLap, String ghiChu, int soLankham, String thoiGianDuKien, double tongChiPhiDuKien, String trangThai) {
        this.maKeHoach = maKeHoach;
        this.maBenhAn = maBenhAn;
        this.maNhanVien = maNhanVien;
        this.maDichVu = maDichVu;
        this.ngayLap = ngayLap;
        this.ghiChu = ghiChu;
        this.soLankham = soLankham;
        this.thoiGianDuKien = thoiGianDuKien;
        this.tongChiPhiDuKien = tongChiPhiDuKien;
        this.trangThai = trangThai;
    }

    public String getMaKeHoach() { return maKeHoach; }
    public void setMaKeHoach(String maKeHoach) { this.maKeHoach = maKeHoach; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getSoLankham() { return soLankham; }
    public void setSoLankham(int soLankham) { this.soLankham = soLankham; }

    public String getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(String thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public double getTongChiPhiDuKien() { return tongChiPhiDuKien; }
    public void setTongChiPhiDuKien(double tongChiPhiDuKien) { this.tongChiPhiDuKien = tongChiPhiDuKien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "KeHoachDieuTri{" +
                "maKeHoach='" + maKeHoach + '\'' +
                ", maBenhAn='" + maBenhAn + '\'' +
                ", maDichVu='" + maDichVu + '\'' +
                ", ngayLap=" + ngayLap +
                ", soLankham=" + soLankham +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
