package models.entity;

import java.util.Date;

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
    private Integer soLankham;
    private String thoiGianDuKien;
    private Double tongChiPhiDuKien;
    private String trangThai;

    public KeHoachDieuTri() {}

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

    public Integer getSoLankham() { return soLankham; }
    public void setSoLankham(Integer soLankham) { this.soLankham = soLankham; }

    public String getThoiGianDuKien() { return thoiGianDuKien; }
    public void setThoiGianDuKien(String thoiGianDuKien) { this.thoiGianDuKien = thoiGianDuKien; }

    public Double getTongChiPhiDuKien() { return tongChiPhiDuKien; }
    public void setTongChiPhiDuKien(Double tongChiPhiDuKien) { this.tongChiPhiDuKien = tongChiPhiDuKien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "KeHoachDieuTri{" +
                "maKeHoach='" + maKeHoach + '\'' +
                ", maBenhAn='" + maBenhAn + '\'' +
                ", tongChiPhiDuKien=" + tongChiPhiDuKien +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}