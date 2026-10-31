package models.entity;

/**
 * Entity class cho bảng ChiTietHoaDon (Composite Key: maHoaDon, maDichVu, maVatTu)
 */
public class ChiTietHoaDon {
    private String maHoaDon;
    private String maDichVu;
    private String maVatTu;
    private int soLuong;
    private double donGia;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maHoaDon, String maDichVu, String maVatTu, int soLuong, double donGia) {
        this.maHoaDon = maHoaDon;
        this.maDichVu = maDichVu;
        this.maVatTu = maVatTu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getMaVatTu() { return maVatTu; }
    public void setMaVatTu(String maVatTu) { this.maVatTu = maVatTu; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getThanhTien() { return soLuong * donGia; }

    @Override
    public String toString() {
        return "ChiTietHoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", maDichVu='" + maDichVu + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                '}';
    }
}