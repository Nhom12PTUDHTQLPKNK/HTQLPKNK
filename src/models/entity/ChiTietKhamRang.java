
package models.entity;

public class ChiTietKhamRang {

    private int maChiTietKham;
    private String maBenhAn;
    private int maRang;
    private String tinhTrang;
    private String mucDo;
    private String trieuChung;
    private String chanDoan;
    private String ghiChu;

    // Constructor mặc định
    public ChiTietKhamRang() {
    }

    // Constructor đầy đủ
    public ChiTietKhamRang(int maChiTietKham, String maBenhAn,
            int maRang, String tinhTrang, String mucDo,
            String trieuChung, String chanDoan, String ghiChu) {

        this.maChiTietKham = maChiTietKham;
        this.maBenhAn = maBenhAn;
        this.maRang = maRang;
        this.tinhTrang = tinhTrang;
        this.mucDo = mucDo;
        this.trieuChung = trieuChung;
        this.chanDoan = chanDoan;
        this.ghiChu = ghiChu;
    }

    // Constructor thêm mới
    public ChiTietKhamRang(String maBenhAn, int maRang,
            String tinhTrang, String mucDo,
            String trieuChung, String chanDoan, String ghiChu) {

        this.maBenhAn = maBenhAn;
        this.maRang = maRang;
        this.tinhTrang = tinhTrang;
        this.mucDo = mucDo;
        this.trieuChung = trieuChung;
        this.chanDoan = chanDoan;
        this.ghiChu = ghiChu;
    }

    public int getMaChiTietKham() {
        return maChiTietKham;
    }

    public void setMaChiTietKham(int maChiTietKham) {
        this.maChiTietKham = maChiTietKham;
    }

    public String getMaBenhAn() {
        return maBenhAn;
    }

    public void setMaBenhAn(String maBenhAn) {
        this.maBenhAn = maBenhAn;
    }

    public int getMaRang() {
        return maRang;
    }

    public void setMaRang(int maRang) {
        this.maRang = maRang;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public String getMucDo() {
        return mucDo;
    }

    public void setMucDo(String mucDo) {
        this.mucDo = mucDo;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getChanDoan() {
        return chanDoan;
    }

    public void setChanDoan(String chanDoan) {
        this.chanDoan = chanDoan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "ChiTietKhamRang{" +
                "maChiTietKham=" + maChiTietKham +
                ", maBenhAn='" + maBenhAn + '\'' +
                ", maRang=" + maRang +
                ", tinhTrang='" + tinhTrang + '\'' +
                ", mucDo='" + mucDo + '\'' +
                ", chanDoan='" + chanDoan + '\'' +
                '}';
    }
}
