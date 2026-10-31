package models.entity;

/**
 * Entity class cho bảng THUOC
 */
public class Thuoc {
    private String maThuoc;
    private String tenThuoc;
    private String hoatChatChinh;
    private String maNhomThuoc;
    private String maDangBaoChe;
    private String maDuongDung;
    private String nhaSanXuat;
    private String nuocSanXuat;
    private String quyCachDongGoi;
    private String moTa;
    private String hinhAnh;
    private Boolean trangThai;

    public Thuoc() {}

    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getHoatChatChinh() { return hoatChatChinh; }
    public void setHoatChatChinh(String hoatChatChinh) { this.hoatChatChinh = hoatChatChinh; }

    public String getMaNhomThuoc() { return maNhomThuoc; }
    public void setMaNhomThuoc(String maNhomThuoc) { this.maNhomThuoc = maNhomThuoc; }

    public String getMaDangBaoChe() { return maDangBaoChe; }
    public void setMaDangBaoChe(String maDangBaoChe) { this.maDangBaoChe = maDangBaoChe; }

    public String getMaDuongDung() { return maDuongDung; }
    public void setMaDuongDung(String maDuongDung) { this.maDuongDung = maDuongDung; }

    public String getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(String nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

    public String getNuocSanXuat() { return nuocSanXuat; }
    public void setNuocSanXuat(String nuocSanXuat) { this.nuocSanXuat = nuocSanXuat; }

    public String getQuyCachDongGoi() { return quyCachDongGoi; }
    public void setQuyCachDongGoi(String quyCachDongGoi) { this.quyCachDongGoi = quyCachDongGoi; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "Thuoc{" +
                "maThuoc='" + maThuoc + '\'' +
                ", tenThuoc='" + tenThuoc + '\'' +
                ", hoatChatChinh='" + hoatChatChinh + '\'' +
                ", nhaSanXuat='" + nhaSanXuat + '\'' +
                '}';
    }
}