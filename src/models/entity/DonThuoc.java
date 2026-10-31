package models.entity;

import java.util.Date;

/**
 * Entity class cho bảng DonThuoc
 */
public class DonThuoc {
    private String maDonThuoc;
    private String maBenhAn;
    private String maNhanVien;
    private String maKhachHang;
    private String maThuoc;
    private String tenThuoc;
    private Date ngayKe;
    private String ghiChu;
    private int soLuong;
    private String cachDung;

    public DonThuoc() {}

    public DonThuoc(String maDonThuoc, String maBenhAn, String maNhanVien, String maKhachHang, String maThuoc, String tenThuoc, Date ngayKe, String ghiChu, int soLuong, String cachDung) {
        this.maDonThuoc = maDonThuoc;
        this.maBenhAn = maBenhAn;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.ngayKe = ngayKe;
        this.ghiChu = ghiChu;
        this.soLuong = soLuong;
        this.cachDung = cachDung;
    }

    public String getMaDonThuoc() { return maDonThuoc; }
    public void setMaDonThuoc(String maDonThuoc) { this.maDonThuoc = maDonThuoc; }

    public String getMaBenhAn() { return maBenhAn; }
    public void setMaBenhAn(String maBenhAn) { this.maBenhAn = maBenhAn; }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) { this.maNhanVien = maNhanVien; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public Date getNgayKe() { return ngayKe; }
    public void setNgayKe(Date ngayKe) { this.ngayKe = ngayKe; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getCachDung() { return cachDung; }
    public void setCachDung(String cachDung) { this.cachDung = cachDung; }

    @Override
    public String toString() {
        return "DonThuoc{" +
                "maDonThuoc='" + maDonThuoc + '\'' +
                ", tenThuoc='" + tenThuoc + '\'' +
                ", soLuong=" + soLuong +
                '}';
    }
}