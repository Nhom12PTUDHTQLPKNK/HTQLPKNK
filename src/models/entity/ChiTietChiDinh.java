
package models.entity;

public class ChiTietChiDinh {

    private int maChiDinh;
    private String maBenhAn;
    private String maDichVu;
    private Integer maRang;
    private int soLuong;
    private String donVi;
    private String ghiChu;

    // Constructor mặc định
    public ChiTietChiDinh() {
    }

    // Constructor đầy đủ
    public ChiTietChiDinh(int maChiDinh, String maBenhAn,
            String maDichVu, Integer maRang,
            int soLuong, String donVi, String ghiChu) {

        this.maChiDinh = maChiDinh;
        this.maBenhAn = maBenhAn;
        this.maDichVu = maDichVu;
        this.maRang = maRang;
        this.soLuong = soLuong;
        this.donVi = donVi;
        this.ghiChu = ghiChu;
    }

    // Constructor thêm mới
    public ChiTietChiDinh(String maBenhAn, String maDichVu,
            Integer maRang, int soLuong,
            String donVi, String ghiChu) {

        this.maBenhAn = maBenhAn;
        this.maDichVu = maDichVu;
        this.maRang = maRang;
        this.soLuong = soLuong;
        this.donVi = donVi;
        this.ghiChu = ghiChu;
    }

    public int getMaChiDinh() {
        return maChiDinh;
    }

    public void setMaChiDinh(int maChiDinh) {
        this.maChiDinh = maChiDinh;
    }

    public String getMaBenhAn() {
        return maBenhAn;
    }

    public void setMaBenhAn(String maBenhAn) {
        this.maBenhAn = maBenhAn;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public Integer getMaRang() {
        return maRang;
    }

    public void setMaRang(Integer maRang) {
        this.maRang = maRang;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "ChiTietChiDinh{" +
                "maChiDinh=" + maChiDinh +
                ", maBenhAn='" + maBenhAn + '\'' +
                ", maDichVu='" + maDichVu + '\'' +
                ", maRang=" + maRang +
                ", soLuong=" + soLuong +
                ", donVi='" + donVi + '\'' +
                '}';
    }
}
