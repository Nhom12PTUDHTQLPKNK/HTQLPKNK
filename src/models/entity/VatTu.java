package models.entity;

import java.util.Date;

/**
 * Entity class cho bảng VatTu
 */
public class VatTu {
    private String maVatTu;
    private String tenVatTu;
    private String donViTinh;
    private Integer soLuongTon;
    private Date ngaySuDung;

    public VatTu() {}

    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String maVatTu) { this.maVatTu = maVatTu; }

    public String getTenVatTu() { return tenVatTu; }
    public void setTenVatTu(String tenVatTu) { this.tenVatTu = tenVatTu; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Date getNgaySuDung() { return ngaySuDung; }
    public void setNgaySuDung(Date ngaySuDung) { this.ngaySuDung = ngaySuDung; }

    @Override
    public String toString() {
        return "VatTu{" +
                "maVatTu='" + maVatTu + '\'' +
                ", tenVatTu='" + tenVatTu + '\'' +
                ", soLuongTon=" + soLuongTon +
                ", donViTinh='" + donViTinh + '\'' +
                '}';
    }
}