package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng VatTu
 */
public class VatTu {
    private String maVatTu;
    private String tenVatTu;
    private String donViTinh;
    private int soLuongTon;
    private Date ngaySuDung;

    public VatTu() {}

    public VatTu(String maVatTu, String tenVatTu, String donViTinh, int soLuongTon, Date ngaySuDung) {
        this.maVatTu = maVatTu;
        this.tenVatTu = tenVatTu;
        this.donViTinh = donViTinh;
        this.soLuongTon = soLuongTon;
        this.ngaySuDung = ngaySuDung;
    }

    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String maVatTu) { this.maVatTu = maVatTu; }

    public String getTenVatTu() { return tenVatTu; }
    public void setTenVatTu(String tenVatTu) { this.tenVatTu = tenVatTu; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public Date getNgaySuDung() { return ngaySuDung; }
    public void setNgaySuDung(Date ngaySuDung) { this.ngaySuDung = ngaySuDung; }

    @Override
    public String toString() {
        return "VatTu{" +
                "maVatTu='" + maVatTu + '\'' +
                ", tenVatTu='" + tenVatTu + '\'' +
                ", donViTinh='" + donViTinh + '\'' +
                ", soLuongTon=" + soLuongTon +
                ", ngaySuDung=" + ngaySuDung +
                '}';
    }
}
