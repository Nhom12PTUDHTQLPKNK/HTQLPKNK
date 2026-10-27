package models.entity;

import java.sql.Date;

/**
 * Entity class cho bảng KhachHang
 */
public class KhachHang {
    private String maKhachHang;
    private String tenKH;
    private String gioiTinh;
    private Date ngaySinh;
    private String sdt;
    private String tienSuBenh;
    private int diemTichLuy;

    public KhachHang() {}

    public KhachHang(String maKhachHang, String tenKH, String gioiTinh, Date ngaySinh, String sdt, String tienSuBenh, int diemTichLuy) {
        this.maKhachHang = maKhachHang;
        this.tenKH = tenKH;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.sdt = sdt;
        this.tienSuBenh = tienSuBenh;
        this.diemTichLuy = diemTichLuy;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getTienSuBenh() { return tienSuBenh; }
    public void setTienSuBenh(String tienSuBenh) { this.tienSuBenh = tienSuBenh; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", tenKH='" + tenKH + '\'' +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", sdt='" + sdt + '\'' +
                ", diemTichLuy=" + diemTichLuy +
                '}';
    }
}
