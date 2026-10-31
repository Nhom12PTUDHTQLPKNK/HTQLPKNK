package models.entity;

/**
 * Entity class cho bảng DichVu
 */
public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private String loaiDieuTri;
    private Double donGia;
    private String donViTinh;
    private String moTa;

    public DichVu() {}

    public DichVu(String maDichVu, String tenDichVu, String loaiDieuTri, Double donGia, String donViTinh, String moTa) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.loaiDieuTri = loaiDieuTri;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.moTa = moTa;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public String getLoaiDieuTri() { return loaiDieuTri; }
    public void setLoaiDieuTri(String loaiDieuTri) { this.loaiDieuTri = loaiDieuTri; }

    public Double getDonGia() { return donGia; }
    public void setDonGia(Double donGia) { this.donGia = donGia; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    @Override
    public String toString() {
        return "DichVu{" +
                "maDichVu='" + maDichVu + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", loaiDieuTri='" + loaiDieuTri + '\'' +
                ", donGia=" + donGia +
                ", donViTinh='" + donViTinh + '\'' +
                '}';
    }
}