
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

    // Lưu đường dẫn hình ảnh JPG
    private String hinhAnh;

    // Constructor mặc định
    public DichVu() {
    }

    // Constructor không có hình ảnh
    public DichVu(String maDichVu, String tenDichVu,
                  String loaiDieuTri, Double donGia,
                  String donViTinh, String moTa) {

        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.loaiDieuTri = loaiDieuTri;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.moTa = moTa;
    }

    // Constructor có hình ảnh
    public DichVu(String maDichVu, String tenDichVu,
                  String loaiDieuTri, Double donGia,
                  String donViTinh, String moTa,
                  String hinhAnh) {

        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.loaiDieuTri = loaiDieuTri;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
    }

    // Getter và Setter
    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getLoaiDieuTri() {
        return loaiDieuTri;
    }

    public void setLoaiDieuTri(String loaiDieuTri) {
        this.loaiDieuTri = loaiDieuTri;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    // Getter hình ảnh
    public String getHinhAnh() {
        return hinhAnh;
    }

    // Setter hình ảnh
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    @Override
    public String toString() {
        return "DichVu{" +
                "maDichVu='" + maDichVu + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", loaiDieuTri='" + loaiDieuTri + '\'' +
                ", donGia=" + donGia +
                ", donViTinh='" + donViTinh + '\'' +
                ", moTa='" + moTa + '\'' +
                ", hinhAnh='" + hinhAnh + '\'' +
                '}';
    }
}
