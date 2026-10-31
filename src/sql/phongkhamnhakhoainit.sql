-- ============================================================================
-- KỊCH BẢN KHỞI TẠO CƠ SỞ DỮ LIỆU HOÀN CHỈNH (RESET & TẠO MỚI 100%)
-- Hệ thống: Quản Lý Phòng Khám Nha Khoa Smile Dental
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PHẦN 1: TẠO DATABASE VÀ LÀM SẠCH BẢNG CŨ (NẾU ĐÃ TỒN TẠI)
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyPhongKhamNhaKhoa')
BEGIN
    CREATE DATABASE QuanLyPhongKhamNhaKhoa;
END
GO

USE QuanLyPhongKhamNhaKhoa;

GO

-- Xóa bảng theo đúng thứ tự từ Bảng Con (có FK) đến Bảng Cha (Độc lập)
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('PhieuThanhToan', 'U') IS NOT NULL DROP TABLE PhieuThanhToan;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('DonThuoc', 'U') IS NOT NULL DROP TABLE DonThuoc;
IF OBJECT_ID('THUOCLUUY', 'U') IS NOT NULL DROP TABLE THUOCLUUY;
IF OBJECT_ID('THUOCCHIDINH', 'U') IS NOT NULL DROP TABLE THUOCCHIDINH;
IF OBJECT_ID('THUOCLIEUDUNG', 'U') IS NOT NULL DROP TABLE THUOCLIEUDUNG;
IF OBJECT_ID('THUOC', 'U') IS NOT NULL DROP TABLE THUOC;
IF OBJECT_ID('DUONGDUNG', 'U') IS NOT NULL DROP TABLE DUONGDUNG;
IF OBJECT_ID('DANGBAOCHE', 'U') IS NOT NULL DROP TABLE DANGBAOCHE;
IF OBJECT_ID('NHOMTHUOC', 'U') IS NOT NULL DROP TABLE NHOMTHUOC;
IF OBJECT_ID('KeHoachDieuTri', 'U') IS NOT NULL DROP TABLE KeHoachDieuTri;
IF OBJECT_ID('HoSoBenhAn', 'U') IS NOT NULL DROP TABLE HoSoBenhAn;
IF OBJECT_ID('LichHen', 'U') IS NOT NULL DROP TABLE LichHen;
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('LichLamViec', 'U') IS NOT NULL DROP TABLE LichLamViec;
IF OBJECT_ID('RANG', 'U') IS NOT NULL DROP TABLE RANG;
IF OBJECT_ID('KhuyenMai', 'U') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('VatTu', 'U') IS NOT NULL DROP TABLE VatTu;
IF OBJECT_ID('DichVu', 'U') IS NOT NULL DROP TABLE DichVu;
IF OBJECT_ID('NhanVien', 'U') IS NOT NULL DROP TABLE NhanVien;
IF OBJECT_ID('KhachHang', 'U') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('AuditLog', 'U') IS NOT NULL DROP TABLE AuditLog;
GO

-- ----------------------------------------------------------------------------
-- PHẦN 2: TẠO CÁC BẢNG ĐỘC LẬP (BẢNG CHA)
-- ----------------------------------------------------------------------------

-- 1. Bảng Khách Hàng
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    tenKH NVARCHAR(100) NOT NULL,
    gioiTinh NVARCHAR(10),
    diaChi NVARCHAR(255),
    ngaySinh DATE,
    sdt VARCHAR(15),
    tienSuBenh NVARCHAR(MAX),
    diemTichLuy INT DEFAULT 0
);

-- 2. Bảng Nhân Viên
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY,
    tenNhanVien NVARCHAR(100) NOT NULL,
    ngaySinh DATE,
    sdt VARCHAR(15),
    chucVu NVARCHAR(50),
    lichLamViec VARCHAR(20),
    trinhDo NVARCHAR(50),
    mucLuong FLOAT
);

-- 3. Bảng Dịch Vụ
CREATE TABLE DichVu (
    maDichVu VARCHAR(20) PRIMARY KEY,
    tenDichVu NVARCHAR(100) NOT NULL,
    loaiDieuTri NVARCHAR(50),
    donGia FLOAT,
    donViTinh NVARCHAR(20),
    moTa NVARCHAR(MAX)
);

-- 4. Bảng Vật Tư
CREATE TABLE VatTu (
    maVatTu VARCHAR(20) PRIMARY KEY,
    tenVatTu NVARCHAR(100) NOT NULL,
    donViTinh NVARCHAR(20),
    soLuongTon INT,
    ngaySuDung DATE
);

-- 5. Bảng Khuyến Mãi
CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai NVARCHAR(100),
    giaTriGiam FLOAT,
    ngayBatDau DATE,
    ngayKetThuc DATE,
    trangThai BIT
);

-- 6. Bảng Nhóm Thuốc
CREATE TABLE NHOMTHUOC (
    MaNhomThuoc VARCHAR(10) PRIMARY KEY,
    TenNhomThuoc NVARCHAR(100) NOT NULL UNIQUE,
    MoTa NVARCHAR(500)
);

-- 7. Bảng Dạng Bảo Chế
CREATE TABLE DANGBAOCHE (
    MaDangBaoChe VARCHAR(10) PRIMARY KEY,
    TenDangBaoChe NVARCHAR(100) NOT NULL UNIQUE,
    MoTa NVARCHAR(255)
);

-- 8. Bảng Đường Dùng
CREATE TABLE DUONGDUNG (
    MaDuongDung VARCHAR(10) PRIMARY KEY,
    TenDuongDung NVARCHAR(100) NOT NULL UNIQUE,
    MoTa NVARCHAR(255)
);

-- 9. Bảng Răng (Sơ đồ răng FDI)
CREATE TABLE RANG (
    MaRang INT PRIMARY KEY,
    TenRang NVARCHAR(100) NOT NULL,
    Ham NVARCHAR(50) NOT NULL,
    ViTri NVARCHAR(50) NOT NULL,
    PhanLoai NVARCHAR(100) NOT NULL,
    MoTaChung NVARCHAR(500) NULL,
    GhiChu NVARCHAR(500) NULL
);

SELECT Ham, COUNT(*) AS SoLuongRang
FROM RANG
GROUP BY Ham;


SELECT COUNT(*) AS TongSoRang
FROM RANG;


-- 10. Nhật ký hệ thống (Audit Log)
CREATE TABLE dbo.AuditLog (
    AuditId BIGINT IDENTITY(1,1) PRIMARY KEY,
    OccurredAt DATETIME2 NOT NULL,
    SessionId VARCHAR(36) NOT NULL,
    Username NVARCHAR(100) NOT NULL,
    UserRole NVARCHAR(100) NOT NULL,
    ActionName NVARCHAR(80) NOT NULL,
    Detail NVARCHAR(MAX) NOT NULL,
    DurationSeconds BIGINT NOT NULL
);
GO

-- ----------------------------------------------------------------------------
-- PHẦN 3: TẠO CÁC BẢNG PHỤ THUỘC (BẢNG CON CÓ KHÓA NGOẠI)
-- ----------------------------------------------------------------------------

-- 11. Bảng Lịch Làm Việc
CREATE TABLE LichLamViec (
    maLichLamViec VARCHAR(20) PRIMARY KEY,
    maNhanVien VARCHAR(20),
    ngayLamViec DATE,
    gioBatDau VARCHAR(10),
    gioKetThuc VARCHAR(10),
    trangThai NVARCHAR(50),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien) ON DELETE SET NULL
);

-- 12. Bảng Lịch Hẹn (Đã bổ sung cột gioKham)
CREATE TABLE LichHen (
    maLichHen VARCHAR(20) PRIMARY KEY,
    maKhachHang VARCHAR(20),
    maNhanVien VARCHAR(20),
    ngayHen DATE,
    gioKham VARCHAR(10),
    trangThai NVARCHAR(50),
    ghiChu NVARCHAR(MAX),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang) ON DELETE CASCADE,
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien) ON DELETE SET NULL
);

-- 13. Bảng Tài Khoản
CREATE TABLE TaiKhoan (
    tenDN VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(255) NOT NULL,
    trangThai BIT DEFAULT 1,
    quyen NVARCHAR(50),
    maNhanVien VARCHAR(20) UNIQUE,
    qrPayload VARCHAR(255),
    qrImage VARBINARY(MAX),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien) ON DELETE CASCADE
);

-- 14. Bảng Thuốc
CREATE TABLE THUOC (
    MaThuoc VARCHAR(20) PRIMARY KEY,
    TenThuoc NVARCHAR(200) NOT NULL,
    HoatChatChinh NVARCHAR(200) NOT NULL,
    MaNhomThuoc VARCHAR(10) NOT NULL,
    MaDangBaoChe VARCHAR(10) NOT NULL,
    MaDuongDung VARCHAR(10) NOT NULL,
    NhaSanXuat NVARCHAR(150),
    NuocSanXuat NVARCHAR(100),
    QuyCachDongGoi NVARCHAR(150),
    MoTa NVARCHAR(1000),
    HinhAnh NVARCHAR(500),
    TrangThai BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_THUOC_NHOMTHUOC FOREIGN KEY (MaNhomThuoc) REFERENCES NHOMTHUOC(MaNhomThuoc),
    CONSTRAINT FK_THUOC_DANGBAOCHE FOREIGN KEY (MaDangBaoChe) REFERENCES DANGBAOCHE(MaDangBaoChe),
    CONSTRAINT FK_THUOC_DUONGDUNG FOREIGN KEY (MaDuongDung) REFERENCES DUONGDUNG(MaDuongDung)
);

-- 15. Bảng Thuốc Liều Dùng
CREATE TABLE THUOCLIEUDUNG (
    MaLieuDung INT IDENTITY(1,1) PRIMARY KEY,
    MaThuoc VARCHAR(20) NOT NULL,
    DoiTuong NVARCHAR(100) NOT NULL,
    LieuDung NVARCHAR(500) NOT NULL,
    TanSuat NVARCHAR(200),
    ThoiGianSuDung NVARCHAR(200),
    HuongDanSuDung NVARCHAR(1000),
    CONSTRAINT FK_THUOCLIEUDUNG_THUOC FOREIGN KEY (MaThuoc) REFERENCES THUOC(MaThuoc) ON DELETE CASCADE
);

-- 16. Bảng Thuốc Chỉ Định
CREATE TABLE THUOCCHIDINH (
    MaChiDinh INT IDENTITY(1,1) PRIMARY KEY,
    MaThuoc VARCHAR(20) NOT NULL,
    NoiDungChiDinh NVARCHAR(1000) NOT NULL,
    CONSTRAINT FK_THUOCCHIDINH_THUOC FOREIGN KEY (MaThuoc) REFERENCES THUOC(MaThuoc) ON DELETE CASCADE
);

-- 17. Bảng Thuốc Lưu Ý
CREATE TABLE THUOCLUUY (
    MaLuuY INT IDENTITY(1,1) PRIMARY KEY,
    MaThuoc VARCHAR(20) NOT NULL,
    LoaiLuuY NVARCHAR(100) NOT NULL,
    NoiDung NVARCHAR(1000) NOT NULL,
    CONSTRAINT FK_THUOCLUUY_THUOC FOREIGN KEY (MaThuoc) REFERENCES THUOC(MaThuoc) ON DELETE CASCADE
);

-- 18. Bảng Hồ Sơ Bệnh Án
CREATE TABLE HoSoBenhAn (
    maBenhAn VARCHAR(20) PRIMARY KEY,
    maKhachHang VARCHAR(20),
    maNhanVien VARCHAR(20),
    ngayKham DATE,
    ketQuaKham NVARCHAR(MAX),
    chuanDoan NVARCHAR(MAX),
    trangThai NVARCHAR(50),
    CONSTRAINT FK_HoSoBenhAn_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoSoBenhAn_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

-- 19. Bảng Kế Hoạch Điều Trị
CREATE TABLE KeHoachDieuTri (
    maKeHoach VARCHAR(20) PRIMARY KEY,
    maBenhAn VARCHAR(20),
    maNhanVien VARCHAR(20),
    maDichVu VARCHAR(20),
    ngayLap DATE,
    ghiChu NVARCHAR(MAX),
    soLankham INT,
    thoiGianDuKien VARCHAR(50),
    tongChiPhiDuKien FLOAT,
    trangThai NVARCHAR(50),
    CONSTRAINT FK_KeHoachDieuTri_HoSoBenhAn FOREIGN KEY (maBenhAn) REFERENCES HoSoBenhAn(maBenhAn),
    CONSTRAINT FK_KeHoachDieuTri_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_KeHoachDieuTri_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);

-- 20. Bảng Đơn Thuốc
CREATE TABLE DonThuoc (
    maDonThuoc VARCHAR(20) PRIMARY KEY,
    maBenhAn VARCHAR(20),
    maNhanVien VARCHAR(20),
    maKhachHang VARCHAR(20),
    maThuoc VARCHAR(20),
    tenThuoc NVARCHAR(200),
    ngayKe DATE,
    ghiChu NVARCHAR(MAX),
    soLuong INT,
    cachDung NVARCHAR(MAX),
    CONSTRAINT FK_DonThuoc_HoSoBenhAn FOREIGN KEY (maBenhAn) REFERENCES HoSoBenhAn(maBenhAn),
    CONSTRAINT FK_DonThuoc_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_DonThuoc_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_DonThuoc_Thuoc FOREIGN KEY (maThuoc) REFERENCES THUOC(MaThuoc)
);

-- 21. Bảng Hóa Đơn
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maBenhAn VARCHAR(20),
    maKhachHang VARCHAR(20),
    maNhanVien VARCHAR(20),
    maKhuyenMai VARCHAR(20),
    ngayLap DATE,
    CONSTRAINT FK_HoaDon_HoSoBenhAn FOREIGN KEY (maBenhAn) REFERENCES HoSoBenhAn(maBenhAn),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai)
);

-- 22. Bảng Chi Tiết Hóa Đơn
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20),
    maDichVu VARCHAR(20),
    maVatTu VARCHAR(20),
    soLuong INT,
    donGia FLOAT,
    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHoaDon, maDichVu, maVatTu),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),
    CONSTRAINT FK_ChiTietHoaDon_VatTu FOREIGN KEY (maVatTu) REFERENCES VatTu(maVatTu)
);

-- 23. Bảng Phiếu Thanh Toán
CREATE TABLE PhieuThanhToan (
    maPhieu VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20),
    maKhachHang VARCHAR(20),
    maNhanVien VARCHAR(20),
    ngayThanhToan DATE,
    hinhThucThanhToan NVARCHAR(50),
    daThanhToan FLOAT,
    soTienConLai FLOAT,
    ghiChu NVARCHAR(MAX),
    CONSTRAINT FK_PhieuThanhToan_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_PhieuThanhToan_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_PhieuThanhToan_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- ----------------------------------------------------------------------------
-- PHẦN 4: CHÈN DỮ LIỆU MẪU ĐẦY ĐỦ VÀ NGUYÊN BẢN
-- ----------------------------------------------------------------------------

-- 1. NhanVien
INSERT INTO NhanVien (maNhanVien, tenNhanVien, ngaySinh, sdt, chucVu, lichLamViec, trinhDo, mucLuong) VALUES
('NV001', N'Nguyễn Minh Tuấn', '1985-03-12', '0901234001', N'Bác sĩ trưởng', N'Thứ 2 - Thứ 7', N'Thạc sĩ Răng Hàm Mặt', 30000000.0),
('NV002', N'Trần Ngọc Mai', '1990-07-21', '0901234002', N'Bác sĩ nha khoa', N'Thứ 2 - Thứ 6', N'Đại học Răng Hàm Mặt', 22000000.0),
('NV003', N'Lê Hoàng Nam', '1988-11-05', '0901234003', N'Bác sĩ nha khoa', N'Thứ 2 - Thứ 7', N'Đại học Răng Hàm Mặt', 23000000.0),
('NV004', N'Phạm Thu Hà', '1995-02-18', '0901234004', N'Lễ tân', N'Thứ 2 - Thứ 7', N'Cao đẳng', 10000000.0),
('NV005', N'Võ Quốc Bảo', '1992-09-09', '0901234005', N'Điều dưỡng', N'Thứ 2 - Thứ 7', N'Cao đẳng Điều dưỡng', 12000000.0);

-- 2. KhachHang
INSERT INTO KhachHang (maKhachHang, tenKH, gioiTinh, diaChi, ngaySinh, sdt, tienSuBenh, diemTichLuy) VALUES
('KH001', N'Nguyễn Thị Thanh Trúc', N'Nữ', N'Số 12 Nguyễn Trãi, Q.1', '2002-05-14', '0911000001', N'Không', 120),
('KH002', N'Trần Văn Hoàng', N'Nam', N'Số 45 Lê Lợi, Q.1', '1998-08-20', '0911000002', N'Không', 80),
('KH003', N'Lê Thị Mỹ Linh', N'Nữ', N'Số 88 Cách Mạng Tháng 8, Q.3', '2000-12-01', '0911000003', N'Dị ứng nhẹ', 150),
('KH004', N'Phạm Minh Khang', N'Nam', N'Số 102 Võ Văn Tần, Q.3', '1995-04-10', '0911000004', N'Không', 60),
('KH005', N'Võ Ngọc Ánh', N'Nữ', N'Số 15 Điện Biên Phủ, Q.Bình Thạnh', '2001-09-25', '0911000005', N'Răng nhạy cảm', 200),
('KH006', N'Đặng Quốc Huy', N'Nam', N'Số 77 Đinh Bộ Lĩnh, Q.Bình Thạnh', '1999-01-16', '0911000006', N'Không', 40),
('KH007', N'Bùi Khánh Vy', N'Nữ', N'Số 23 Hoàng Văn Thụ, Q.Phú Nhuận', '2003-06-30', '0911000007', N'Không', 90),
('KH008', N'Đỗ Thành Đạt', N'Nam', N'Số 56 Phan Xích Long, Q.Phú Nhuận', '1997-10-08', '0911000008', N'Viêm lợi trước đây', 110),
('KH009', N'Hoàng Thu Thảo', N'Nữ', N'Số 90 Lý Thường Kiệt, Q.10', '1996-02-22', '0911000009', N'Không', 70),
('KH010', N'Phan Gia Bảo', N'Nam', N'Số 14 Ba Tháng Hai, Q.10', '2004-07-19', '0911000010', N'Không', 30);

-- 3. TaiKhoan
INSERT INTO TaiKhoan (tenDN, matKhau, trangThai, quyen, maNhanVien) VALUES
('admin', N'123456', 1, N'Admin', 'NV001'),
('bsmai', N'123456', 1, N'Bác sĩ', 'NV002'),
('bsnam', N'123456', 1, N'Bác sĩ', 'NV003'),
('letan', N'123456', 1, N'Lễ tân', 'NV004'),
('dieuduong', N'123456', 1, N'Điều dưỡng', 'NV005');

-- 4. LichLamViec
INSERT INTO LichLamViec (maLichLamViec, maNhanVien, ngayLamViec, gioBatDau, gioKetThuc, trangThai) VALUES
('LLV001', 'NV001', '2026-10-24', '08:00', '17:00', N'Đang làm'),
('LLV002', 'NV002', '2026-10-24', '08:00', '16:00', N'Đang làm'),
('LLV003', 'NV003', '2026-10-24', '09:00', '17:00', N'Đang làm'),
('LLV004', 'NV004', '2026-10-24', '07:30', '17:30', N'Đang làm'),
('LLV005', 'NV005', '2026-10-24', '08:00', '17:00', N'Đang làm'),
('LLV006', 'NV002', '2026-10-25', '08:00', '16:00', N'Đã xếp lịch'),
('LLV007', 'NV003', '2026-10-25', '09:00', '17:00', N'Đã xếp lịch');

-- 5. DichVu
INSERT INTO DichVu (maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh, moTa) VALUES
('DV001', N'Lấy cao răng', N'Nha khoa tổng quát', 200000.0, N'Lần', N'Vệ sinh răng miệng'),
('DV002', N'Trám răng', N'Nha khoa tổng quát', 350000.0, N'Răng', N'Trám răng sâu'),
('DV003', N'Tẩy trắng răng', N'Thẩm mỹ', 1500000.0, N'Liệu trình', N'Làm trắng răng'),
('DV004', N'Nhổ răng', N'Tiểu phẫu', 500000.0, N'Răng', N'Nhổ răng thông thường'),
('DV005', N'Khám tổng quát', N'Khám tổng quát', 100000.0, N'Lần', N'Kiểm tra sức khỏe răng miệng'),
('DV006', N'Niềng răng', N'Chỉnh nha', 25000000.0, N'Gói', N'Dịch vụ chỉnh nha');

-- 6. VatTu
INSERT INTO VatTu (maVatTu, tenVatTu, donViTinh, soLuongTon, ngaySuDung) VALUES
('VT001', N'Găng tay y tế', N'Hộp', 50, '2026-10-01'),
('VT002', N'Khẩu trang y tế', N'Hộp', 80, '2026-10-01'),
('VT003', N'Bông y tế', N'Gói', 100, '2026-10-01'),
('VT004', N'Vật liệu trám răng', N'Tuýp', 30, '2026-10-01'),
('VT005', N'Dụng cụ khám răng', N'Bộ', 20, '2026-10-01');

-- 7. KhuyenMai
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai) VALUES
('KM001', N'Ưu đãi khách hàng mới', 10.0, '2026-10-01', '2026-12-31', 1),
('KM002', N'Ưu đãi sinh viên', 5.0, '2026-09-01', '2026-12-31', 1),
('KM003', N'Khuyến mãi cuối năm', 15.0, '2026-11-01', '2026-12-31', 0);

-- 8. RANG (32 Răng FDI chuẩn)
INSERT INTO RANG (MaRang, TenRang, Ham, ViTri, PhanLoai, MoTaChung, GhiChu) VALUES
(11, N'Răng cửa giữa', N'Hàm trên', N'Bên phải', N'Răng cửa', N'Răng cửa giữa hàm trên bên phải, nằm cạnh đường giữa cung hàm.', N'Răng số 1 theo thứ tự FDI của cung hàm trên bên phải.'),
(12, N'Răng cửa bên', N'Hàm trên', N'Bên phải', N'Răng cửa', N'Răng cửa bên hàm trên bên phải, nằm phía ngoài răng cửa giữa.', N'Răng số 2 theo thứ tự FDI của cung hàm trên bên phải.'),
(13, N'Răng nanh', N'Hàm trên', N'Bên phải', N'Răng nanh', N'Răng nanh hàm trên bên phải, có thân răng nhọn.', N'Răng số 3 theo thứ tự FDI của cung hàm trên bên phải.'),
(14, N'Răng tiền hàm thứ nhất', N'Hàm trên', N'Bên phải', N'Răng tiền hàm', N'Răng tiền hàm thứ nhất hàm trên bên phải.', N'Răng số 4 theo thứ tự FDI của cung hàm trên bên phải.'),
(15, N'Răng tiền hàm thứ hai', N'Hàm trên', N'Bên phải', N'Răng tiền hàm', N'Răng tiền hàm thứ hai hàm trên bên phải.', N'Răng số 5 theo thứ tự FDI của cung hàm trên bên phải.'),
(16, N'Răng hàm lớn thứ nhất', N'Hàm trên', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ nhất hàm trên bên phải, đảm nhiệm chức năng nghiền thức ăn.', N'Răng số 6 theo thứ tự FDI của cung hàm trên bên phải.'),
(17, N'Răng hàm lớn thứ hai', N'Hàm trên', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ hai hàm trên bên phải.', N'Răng số 7 theo thứ tự FDI của cung hàm trên bên phải.'),
(18, N'Răng khôn', N'Hàm trên', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ ba hàm trên bên phải.', N'Răng số 8 theo thứ tự FDI của cung hàm trên bên phải.'),
(21, N'Răng cửa giữa', N'Hàm trên', N'Bên trái', N'Răng cửa', N'Răng cửa giữa hàm trên bên trái, nằm cạnh đường giữa cung hàm.', N'Răng số 1 theo thứ tự FDI của cung hàm trên bên trái.'),
(22, N'Răng cửa bên', N'Hàm trên', N'Bên trái', N'Răng cửa', N'Răng cửa bên hàm trên bên trái, nằm phía ngoài răng cửa giữa.', N'Răng số 2 theo thứ tự FDI của cung hàm trên bên trái.'),
(23, N'Răng nanh', N'Hàm trên', N'Bên trái', N'Răng nanh', N'Răng nanh hàm trên bên trái, có thân răng nhọn.', N'Răng số 3 theo thứ tự FDI của cung hàm trên bên trái.'),
(24, N'Răng tiền hàm thứ nhất', N'Hàm trên', N'Bên trái', N'Răng tiền hàm', N'Răng tiền hàm thứ nhất hàm trên bên trái.', N'Răng số 4 theo thứ tự FDI của cung hàm trên bên trái.'),
(25, N'Răng tiền hàm thứ hai', N'Hàm trên', N'Bên trái', N'Răng tiền hàm', N'Răng tiền hàm thứ hai hàm trên bên trái.', N'Răng số 5 theo thứ tự FDI của cung hàm trên bên trái.'),
(26, N'Răng hàm lớn thứ nhất', N'Hàm trên', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ nhất hàm trên bên trái, đảm nhiệm chức năng nghiền thức ăn.', N'Răng số 6 theo thứ tự FDI của cung hàm trên bên trái.'),
(27, N'Răng hàm lớn thứ hai', N'Hàm trên', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ hai hàm trên bên trái.', N'Răng số 7 theo thứ tự FDI của cung hàm trên bên trái.'),
(28, N'Răng khôn', N'Hàm trên', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ ba hàm trên bên trái.', N'Răng số 8 theo thứ tự FDI của cung hàm trên bên trái.'),
(31, N'Răng cửa giữa', N'Hàm dưới', N'Bên trái', N'Răng cửa', N'Răng cửa giữa hàm dưới bên trái, nằm cạnh đường giữa cung hàm.', N'Răng số 1 theo thứ tự FDI của cung hàm dưới bên trái.'),
(32, N'Răng cửa bên', N'Hàm dưới', N'Bên trái', N'Răng cửa', N'Răng cửa bên hàm dưới bên trái, nằm phía ngoài răng cửa giữa.', N'Răng số 2 theo thứ tự FDI của cung hàm dưới bên trái.'),
(33, N'Răng nanh', N'Hàm dưới', N'Bên trái', N'Răng nanh', N'Răng nanh hàm dưới bên trái, có thân răng nhọn.', N'Răng số 3 theo thứ tự FDI của cung hàm dưới bên trái.'),
(34, N'Răng tiền hàm thứ nhất', N'Hàm dưới', N'Bên trái', N'Răng tiền hàm', N'Răng tiền hàm thứ nhất hàm dưới bên trái.', N'Răng số 4 theo thứ tự FDI của cung hàm dưới bên trái.'),
(35, N'Răng tiền hàm thứ hai', N'Hàm dưới', N'Bên trái', N'Răng tiền hàm', N'Răng tiền hàm thứ hai hàm dưới bên trái.', N'Răng số 5 theo thứ tự FDI của cung hàm dưới bên trái.'),
(36, N'Răng hàm lớn thứ nhất', N'Hàm dưới', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ nhất hàm dưới bên trái, đảm nhiệm chức năng nghiền thức ăn.', N'Răng số 6 theo thứ tự FDI của cung hàm dưới bên trái.'),
(37, N'Răng hàm lớn thứ hai', N'Hàm dưới', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ hai hàm dưới bên trái.', N'Răng số 7 theo thứ tự FDI của cung hàm dưới bên trái.'),
(38, N'Răng khôn', N'Hàm dưới', N'Bên trái', N'Răng hàm', N'Răng hàm lớn thứ ba hàm dưới bên trái.', N'Răng số 8 theo thứ tự FDI của cung hàm dưới bên trái.'),
(41, N'Răng cửa giữa', N'Hàm dưới', N'Bên phải', N'Răng cửa', N'Răng cửa giữa hàm dưới bên phải, nằm cạnh đường giữa cung hàm.', N'Răng số 1 theo thứ tự FDI của cung hàm dưới bên phải.'),
(42, N'Răng cửa bên', N'Hàm dưới', N'Bên phải', N'Răng cửa', N'Răng cửa bên hàm dưới bên phải, nằm phía ngoài răng cửa giữa.', N'Răng số 2 theo thứ tự FDI của cung hàm dưới bên phải.'),
(43, N'Răng nanh', N'Hàm dưới', N'Bên phải', N'Răng nanh', N'Răng nanh hàm dưới bên phải, có thân răng nhọn.', N'Răng số 3 theo thứ tự FDI của cung hàm dưới bên phải.'),
(44, N'Răng tiền hàm thứ nhất', N'Hàm dưới', N'Bên phải', N'Răng tiền hàm', N'Răng tiền hàm thứ nhất hàm dưới bên phải.', N'Răng số 4 theo thứ tự FDI của cung hàm dưới bên phải.'),
(45, N'Răng tiền hàm thứ hai', N'Hàm dưới', N'Bên phải', N'Răng tiền hàm', N'Răng tiền hàm thứ hai hàm dưới bên phải.', N'Răng số 5 theo thứ tự FDI của cung hàm dưới bên phải.'),
(46, N'Răng hàm lớn thứ nhất', N'Hàm dưới', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ nhất hàm dưới bên phải, đảm nhiệm chức năng nghiền thức ăn.', N'Răng số 6 theo thứ tự FDI của cung hàm dưới bên phải.'),
(47, N'Răng hàm lớn thứ hai', N'Hàm dưới', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ hai hàm dưới bên phải.', N'Răng số 7 theo thứ tự FDI của cung hàm dưới bên phải.'),
(48, N'Răng khôn', N'Hàm dưới', N'Bên phải', N'Răng hàm', N'Răng hàm lớn thứ ba hàm dưới bên phải.', N'Răng số 8 theo thứ tự FDI của cung hàm dưới bên phải.');

-- 9. LichHen (Có cột gioKham)
INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVien, ngayHen, gioKham, trangThai, ghiChu) VALUES
('LH001', 'KH001', 'NV002', '2026-10-24', '08:30', N'Hoàn thành', N'Lấy cao răng'),
('LH002', 'KH002', 'NV003', '2026-10-24', '09:15', N'Đã xác nhận', N'Khám tổng quát'),
('LH003', 'KH003', 'NV002', '2026-10-24', '10:00', N'Chờ xác nhận', N'Trám răng'),
('LH004', 'KH004', 'NV001', '2026-10-24', '14:00', N'Đã xác nhận', N'Tư vấn chỉnh nha'),
('LH005', 'KH005', 'NV003', '2026-10-25', '15:30', N'Đã xác nhận', N'Tẩy trắng răng'),
('LH006', 'KH006', 'NV002', '2026-10-25', '09:00', N'Chờ xác nhận', N'Khám răng'),
('LH007', 'KH007', 'NV003', '2026-10-22', '10:30', N'Hoàn thành', N'Lấy cao răng'),
('LH008', 'KH008', 'NV002', '2026-10-23', '14:30', N'Hoàn thành', N'Trám răng'),
('LH009', 'KH009', 'NV001', '2026-10-26', '16:00', N'Đã xác nhận', N'Tư vấn nha khoa'),
('LH010', 'KH010', 'NV003', '2026-10-26', '08:00', N'Chờ xác nhận', N'Khám tổng quát');

-- 10. HoSoBenhAn
INSERT INTO HoSoBenhAn (maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai) VALUES
('BA001', 'KH001', 'NV002', '2026-10-24', N'Răng sạch sau điều trị', N'Cao răng', N'Hoàn thành'),
('BA002', 'KH002', 'NV003', '2026-10-23', N'Có một răng sâu nhẹ', N'Sâu răng', N'Đang điều trị'),
('BA003', 'KH003', 'NV002', '2026-10-22', N'Răng hàm có lỗ sâu', N'Sâu răng hàm', N'Đang điều trị'),
('BA004', 'KH004', 'NV001', '2026-10-21', N'Cần tư vấn chỉnh nha', N'Lệch khớp cắn', N'Đang điều trị'),
('BA005', 'KH005', 'NV003', '2026-10-20', N'Răng nhạy cảm nhẹ', N'Răng nhạy cảm', N'Hoàn thành'),
('BA006', 'KH007', 'NV002', '2026-10-19', N'Cao răng mức độ nhẹ', N'Cao răng', N'Hoàn thành'),
('BA007', 'KH008', 'NV003', '2026-10-23', N'Viêm lợi nhẹ', N'Viêm lợi', N'Đang điều trị');

-- 11. KeHoachDieuTri
INSERT INTO KeHoachDieuTri (maKeHoach, maBenhAn, maNhanVien, maDichVu, ngayLap, ghiChu, soLankham, thoiGianDuKien, tongChiPhiDuKien, trangThai) VALUES
('KHDT001', 'BA001', 'NV002', 'DV001', '2026-10-24', N'Điều trị hoàn tất', 1, '30 phút', 200000.0, N'Hoàn thành'),
('KHDT002', 'BA002', 'NV003', 'DV002', '2026-10-23', N'Trám răng sâu', 2, '45 phút', 350000.0, N'Đang điều trị'),
('KHDT003', 'BA003', 'NV002', 'DV002', '2026-10-22', N'Trám răng hàm', 2, '45 phút', 350000.0, N'Đang điều trị'),
('KHDT004', 'BA004', 'NV001', 'DV006', '2026-10-21', N'Tư vấn niềng răng', 12, '12 tháng', 25000000.0, N'Đang điều trị'),
('KHDT005', 'BA005', 'NV003', 'DV003', '2026-10-20', N'Tẩy trắng răng', 1, '60 phút', 1500000.0, N'Hoàn thành'),
('KHDT006', 'BA006', 'NV002', 'DV001', '2026-10-19', N'Lấy cao răng', 1, '30 phút', 200000.0, N'Hoàn thành');

-- 12. DANH MỤC THUỐC GỐC (GIỮ NGUYÊN BẢN 100%)
INSERT INTO NHOMTHUOC (MaNhomThuoc, TenNhomThuoc, MoTa) VALUES
('NT001', N'Kháng sinh', N'Thuốc kháng khuẩn sử dụng trong điều trị nhiễm khuẩn răng miệng'),
('NT002', N'Giảm đau - Hạ sốt', N'Thuốc hỗ trợ giảm đau và hạ sốt'),
('NT003', N'Kháng viêm', N'Thuốc hỗ trợ kiểm soát tình trạng viêm'),
('NT004', N'Sát khuẩn', N'Dung dịch sát khuẩn và vệ sinh khoang miệng'),
('NT005', N'Gây tê nha khoa', N'Thuốc sử dụng trong gây tê khi thực hiện thủ thuật'),
('NT006', N'Corticosteroid', N'Thuốc chống viêm corticosteroid');

INSERT INTO DANGBAOCHE (MaDangBaoChe, TenDangBaoChe, MoTa) VALUES
('DBC001', N'Viên nén', N'Thuốc dạng viên nén'),
('DBC002', N'Viên nang', N'Thuốc dạng viên nang'),
('DBC003', N'Dung dịch', N'Thuốc dạng dung dịch'),
('DBC004', N'Gel bôi', N'Thuốc dạng gel dùng tại chỗ'),
('DBC005', N'Ống tiêm', N'Thuốc dạng dung dịch tiêm'),
('DBC006', N'Viên sủi', N'Thuốc dạng viên sủi');

INSERT INTO DUONGDUNG (MaDuongDung, TenDuongDung, MoTa) VALUES
('DD001', N'Đường uống', N'Sử dụng thuốc qua đường uống'),
('DD002', N'Súc miệng', N'Sử dụng bằng cách súc miệng'),
('DD003', N'Dùng ngoài', N'Sử dụng trực tiếp tại vị trí cần điều trị'),
('DD004', N'Tiêm', N'Sử dụng bằng đường tiêm theo chỉ định chuyên môn'),
('DD005', N'Bôi tại chỗ', N'Sử dụng trực tiếp tại vùng cần điều trị');

INSERT INTO THUOC (
    MaThuoc, TenThuoc, HoatChatChinh,
    MaNhomThuoc, MaDangBaoChe, MaDuongDung,
    NhaSanXuat, NuocSanXuat, QuyCachDongGoi,
    MoTa, HinhAnh, TrangThai
) VALUES
('TH001', N'Amoxicillin 500mg', N'Amoxicillin', 'NT001', 'DBC001', 'DD001', N'GSK', N'Việt Nam', N'Hộp 10 vỉ x 10 viên', N'Kháng sinh nhóm Penicillin, sử dụng trong một số trường hợp nhiễm khuẩn răng miệng theo chỉ định nha sĩ.', N'/img/thuoc/amoxicillin.png', 1),
('TH002', N'Metronidazole 250mg', N'Metronidazole', 'NT001', 'DBC001', 'DD001', N'Domesco', N'Việt Nam', N'Hộp 10 vỉ x 10 viên', N'Thuốc kháng khuẩn được sử dụng trong một số trường hợp nhiễm khuẩn kỵ khí.', N'/img/thuoc/metronidazole.png', 1),
('TH003', N'Clindamycin 300mg', N'Clindamycin', 'NT001', 'DBC002', 'DD001', N'Pfizer', N'Mỹ', N'Hộp 10 vỉ x 10 viên', N'Kháng sinh nhóm Lincosamide, sử dụng theo chỉ định chuyên môn.', N'/img/thuoc/clindamycin.png', 1),
('TH004', N'Paracetamol 500mg', N'Paracetamol', 'NT002', 'DBC001', 'DD001', N'Stada', N'Việt Nam', N'Hộp 10 vỉ x 10 viên', N'Thuốc giảm đau và hạ sốt.', N'/img/thuoc/paracetamol.png', 1),
('TH005', N'Ibuprofen 400mg', N'Ibuprofen', 'NT003', 'DBC001', 'DD001', N'Abbott', N'Mỹ', N'Hộp 10 vỉ x 10 viên', N'Thuốc giảm đau, kháng viêm không steroid.', N'/img/thuoc/ibuprofen.png', 1),
('TH006', N'Chlorhexidine 0.12%', N'Chlorhexidine', 'NT004', 'DBC003', 'DD002', N'Colgate', N'Mỹ', N'Chai 250ml', N'Dung dịch sát khuẩn khoang miệng, hỗ trợ kiểm soát mảng bám.', N'/img/thuoc/chlorhexidine.png', 1),
('TH007', N'Povidone Iodine 10%', N'Povidone Iodine', 'NT004', 'DBC003', 'DD003', N'Betadine', N'Việt Nam', N'Chai 100ml', N'Dung dịch sát khuẩn dùng ngoài.', N'/img/thuoc/povidone.png', 1),
('TH008', N'Dexamethasone 0.5mg', N'Dexamethasone', 'NT006', 'DBC001', 'DD001', N'Pharbaco', N'Việt Nam', N'Hộp 10 vỉ x 10 viên', N'Thuốc corticosteroid có tác dụng chống viêm.', N'/img/thuoc/dexamethasone.png', 1),
('TH009', N'Lidocaine 2%', N'Lidocaine', 'NT005', 'DBC005', 'DD004', N'Pharmaceutical', N'Việt Nam', N'Hộp ống tiêm', N'Thuốc gây tê tại chỗ sử dụng trong thủ thuật nha khoa.', N'/img/thuoc/lidocaine.png', 1),
('TH010', N'Gel Lidocaine 2%', N'Lidocaine', 'NT005', 'DBC004', 'DD005', N'Pharmaceutical', N'Việt Nam', N'Tuýp 10g', N'Chế phẩm gây tê tại chỗ dạng gel.', N'/img/thuoc/gel-lidocaine.png', 1);

INSERT INTO THUOCLIEUDUNG (MaThuoc, DoiTuong, LieuDung, TanSuat, ThoiGianSuDung, HuongDanSuDung) VALUES
('TH001', N'Người lớn', N'Theo liều lượng được nha sĩ hoặc bác sĩ kê đơn', N'Theo chỉ định', N'Theo liệu trình được kê đơn', N'Uống thuốc theo đơn, tuân thủ đầy đủ liệu trình điều trị'),
('TH002', N'Người lớn', N'Theo liều lượng được nha sĩ hoặc bác sĩ kê đơn', N'Theo chỉ định', N'Theo liệu trình được kê đơn', N'Uống thuốc theo hướng dẫn của nhân viên y tế'),
('TH003', N'Người lớn', N'Theo liều lượng được nha sĩ hoặc bác sĩ kê đơn', N'Theo chỉ định', N'Theo liệu trình được kê đơn', N'Sử dụng đúng đơn thuốc, không tự ý ngừng thuốc'),
('TH004', N'Người lớn', N'Theo hướng dẫn sử dụng hoặc chỉ định của bác sĩ', N'Theo hướng dẫn', N'Tùy tình trạng sử dụng', N'Kiểm tra thành phần thuốc để tránh sử dụng đồng thời nhiều sản phẩm chứa Paracetamol'),
('TH005', N'Người lớn', N'Theo hướng dẫn sử dụng hoặc chỉ định của bác sĩ', N'Theo hướng dẫn', N'Tùy tình trạng sử dụng', N'Sử dụng theo hướng dẫn chuyên môn, lưu ý các chống chỉ định và cảnh báo của thuốc'),
('TH006', N'Người lớn', N'Theo hướng dẫn trên sản phẩm hoặc chỉ định của nha sĩ', N'Theo hướng dẫn', N'Theo liệu trình chăm sóc răng miệng', N'Dùng để súc miệng, không nuốt dung dịch'),
('TH007', N'Người lớn', N'Theo chỉ định của nhân viên y tế', N'Theo hướng dẫn', N'Tùy mục đích sử dụng', N'Dùng ngoài theo hướng dẫn chuyên môn, tránh tự ý sử dụng'),
('TH008', N'Người lớn', N'Theo đơn kê của bác sĩ', N'Theo chỉ định', N'Theo liệu trình được kê đơn', N'Không tự ý tăng giảm liều hoặc kéo dài thời gian sử dụng'),
('TH009', N'Người bệnh tại phòng khám', N'Do nha sĩ hoặc nhân viên y tế xác định', N'Tùy thủ thuật', N'Tùy quy trình điều trị', N'Thuốc tiêm gây tê, chỉ được sử dụng bởi nhân viên y tế có chuyên môn'),
('TH010', N'Người bệnh tại phòng khám', N'Theo chỉ định của nha sĩ', N'Tùy thủ thuật', N'Tùy quy trình điều trị', N'Gel gây tê tại chỗ, sử dụng theo hướng dẫn của nha sĩ');

INSERT INTO THUOCCHIDINH (MaThuoc, NoiDungChiDinh) VALUES
('TH001', N'Một số trường hợp nhiễm khuẩn răng miệng do vi khuẩn nhạy cảm.'),
('TH002', N'Một số trường hợp nhiễm khuẩn do vi khuẩn kỵ khí trong nha khoa.'),
('TH003', N'Một số trường hợp nhiễm khuẩn răng miệng khi có chỉ định phù hợp.'),
('TH004', N'Hỗ trợ giảm đau sau các thủ thuật nha khoa.'),
('TH005', N'Hỗ trợ giảm đau và viêm trong một số tình trạng nha khoa.'),
('TH006', N'Hỗ trợ kiểm soát vi khuẩn và mảng bám trong khoang miệng.'),
('TH007', N'Sát khuẩn ngoài da theo chỉ định chuyên môn.'),
('TH008', N'Một số tình trạng viêm cần điều trị bằng corticosteroid.'),
('TH009', N'Gây tê tại chỗ trong các thủ thuật nha khoa.'),
('TH010', N'Gây tê tại chỗ theo chỉ định chuyên môn.');

INSERT INTO THUOCLUUY (MaThuoc, LoaiLuuY, NoiDung) VALUES
('TH001', N'Chống chỉ định', N'Không sử dụng khi dị ứng với Penicillin hoặc thành phần thuốc.'),
('TH002', N'Lưu ý', N'Cần kiểm tra tương tác thuốc và tình trạng sức khỏe của bệnh nhân.'),
('TH003', N'Cảnh báo', N'Cần lưu ý nguy cơ viêm đại tràng liên quan đến kháng sinh.'),
('TH004', N'Cảnh báo', N'Tránh sử dụng đồng thời nhiều chế phẩm có chứa Paracetamol vượt liều cho phép.');

-- 13. DonThuoc
INSERT INTO DonThuoc (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, ghiChu, soLuong, cachDung) VALUES
('DT001', 'BA002', 'NV003', 'KH002', 'TH001', N'Paracetamol', '2026-10-23', N'Đơn thuốc mẫu', 6, N'Dùng theo chỉ định của bác sĩ'),
('DT002', 'BA003', 'NV002', 'KH003', 'TH004', N'Nước súc miệng', '2026-10-22', N'Vệ sinh răng miệng', 1, N'Sử dụng theo hướng dẫn'),
('DT003', 'BA007', 'NV003', 'KH008', 'TH003', N'Vitamin C', '2026-10-23', N'Đơn thuốc mẫu', 10, N'Dùng theo chỉ định của bác sĩ');

-- 14. HoaDon
INSERT INTO HoaDon (maHoaDon, maBenhAn, maKhachHang, maNhanVien, maKhuyenMai, ngayLap) VALUES
('HD001', 'BA001', 'KH001', 'NV002', 'KM001', '2026-10-24'),
('HD002', 'BA002', 'KH002', 'NV003', NULL, '2026-10-23'),
('HD003', 'BA003', 'KH003', 'NV002', 'KM002', '2026-10-22'),
('HD004', 'BA005', 'KH005', 'NV003', NULL, '2026-10-20'),
('HD005', 'BA006', 'KH007', 'NV002', 'KM001', '2026-10-19'),
('HD006', 'BA007', 'KH008', 'NV003', NULL, '2026-10-23');

-- 15. ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maDichVu, maVatTu, soLuong, donGia) VALUES
('HD001', 'DV001', 'VT001', 1, 200000.0),
('HD002', 'DV002', 'VT004', 1, 350000.0),
('HD003', 'DV002', 'VT004', 1, 350000.0),
('HD004', 'DV003', 'VT002', 1, 1500000.0),
('HD005', 'DV001', 'VT003', 1, 200000.0),
('HD006', 'DV005', 'VT005', 1, 100000.0);

-- 16. PhieuThanhToan
INSERT INTO PhieuThanhToan (maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu) VALUES
('PTT001', 'HD001', 'KH001', 'NV004', '2026-10-24', N'Tiền mặt', 200000.0, 0.0, N'Đã thanh toán'),
('PTT002', 'HD002', 'KH002', 'NV004', '2026-10-23', N'Chuyển khoản', 350000.0, 0.0, N'Đã thanh toán'),
('PTT003', 'HD003', 'KH003', 'NV004', '2026-10-22', N'Tiền mặt', 0.0, 350000.0, N'Chưa thanh toán'),
('PTT004', 'HD004', 'KH005', 'NV004', '2026-10-20', N'Chuyển khoản', 1500000.0, 0.0, N'Đã thanh toán'),
('PTT005', 'HD005', 'KH007', 'NV004', '2026-10-19', N'Tiền mặt', 200000.0, 0.0, N'Đã thanh toán'),
('PTT006', 'HD006', 'KH008', 'NV004', '2026-10-23', N'Tiền mặt', 0.0, 100000.0, N'Chưa thanh toán');
GO



