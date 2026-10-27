-- Kịch bản khởi tạo cơ sở dữ liệu QuanLyPhongKhamNhaKhoa (init.sql)

USE master;
GO

-- Kiểm tra nếu database CHƯA TỒN TẠI thì mới tạo mới
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QuanLyPhongKhamNhaKhoa')
BEGIN
    CREATE DATABASE QuanLyPhongKhamNhaKhoa;
END
GO

USE QuanLyPhongKhamNhaKhoa;
GO

-- 1. Bảng Khách Hàng
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'KhachHang')
BEGIN
    CREATE TABLE KhachHang (
        maKhachHang VARCHAR(20) PRIMARY KEY,
        tenKH NVARCHAR(100) NOT NULL,
        gioiTinh NVARCHAR(10),
        ngaySinh DATE,
        sdt VARCHAR(15),
        tienSuBenh NVARCHAR(MAX),
        diemTichLuy INT DEFAULT 0
    );
END;

-- 2. Bảng Nhân Viên
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'NhanVien')
BEGIN
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
END;

-- 3. Bảng Lịch Làm Việc
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'LichLamViec')
BEGIN
    CREATE TABLE LichLamViec (
        maLichLamViec VARCHAR(20) PRIMARY KEY,
        maNhanVien VARCHAR(20),
        ngayLamViec DATE,
        gioBatDau VARCHAR(10),
        gioKetThuc VARCHAR(10),
        trangThai NVARCHAR(50),
        CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
    );
END;

-- 4. Bảng Lịch Hẹn
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'LichHen')
BEGIN
    CREATE TABLE LichHen (
        maLichHen VARCHAR(20) PRIMARY KEY,
        maKhachHang VARCHAR(20),
        maNhanVien VARCHAR(20),
        ngayHen DATE,
        trangThai NVARCHAR(50),
        ghiChu NVARCHAR(MAX),
        CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
        CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
    );
END;

-- 5. Bảng Tài Khoản 
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'TaiKhoan')
BEGIN
    CREATE TABLE TaiKhoan (
        tenDN VARCHAR(50) PRIMARY KEY,
        matKhau VARCHAR(255) NOT NULL,
        trangThai BIT DEFAULT 1,
        quyen NVARCHAR(50),
        maNhanVien VARCHAR(20) UNIQUE,
        CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
    );
END;

-- 6. Bảng Hồ Sơ Bệnh Án
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HoSoBenhAn')
BEGIN
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
END;

-- 7. Bảng Dịch Vụ
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DichVu')
BEGIN
    CREATE TABLE DichVu (
        maDichVu VARCHAR(20) PRIMARY KEY,
        tenDichVu NVARCHAR(100) NOT NULL,
        donGia FLOAT,
        moTa NVARCHAR(MAX)
    );
END;

-- 8. Bảng Kế Hoạch Điều Trị
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'KeHoachDieuTri')
BEGIN
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
END;

-- 9. Bảng Thuốc
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Thuoc')
BEGIN
    CREATE TABLE Thuoc (
        maThuoc VARCHAR(20) PRIMARY KEY,
        tenThuoc NVARCHAR(100) NOT NULL,
        donViTinh NVARCHAR(20),
        donGia FLOAT,
        soLuongTon INT
    );
END;

-- 10. Bảng Đơn Thuốc
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DonThuoc')
BEGIN
    CREATE TABLE DonThuoc (
        maDonThuoc VARCHAR(20) PRIMARY KEY,
        maBenhAn VARCHAR(20),
        maNhanVien VARCHAR(20),
        maKhachHang VARCHAR(20),
        maThuoc VARCHAR(20),
        tenThuoc NVARCHAR(100),
        ngayKe DATE,
        ghiChu NVARCHAR(MAX),
        soLuong INT,
        cachDung NVARCHAR(MAX),
        CONSTRAINT FK_DonThuoc_HoSoBenhAn FOREIGN KEY (maBenhAn) REFERENCES HoSoBenhAn(maBenhAn),
        CONSTRAINT FK_DonThuoc_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
        CONSTRAINT FK_DonThuoc_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
        CONSTRAINT FK_DonThuoc_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc)
    );
END;

-- 11. Bảng Vật Tư
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'VatTu')
BEGIN
    CREATE TABLE VatTu (
        maVatTu VARCHAR(20) PRIMARY KEY,
        tenVatTu NVARCHAR(100) NOT NULL,
        donViTinh NVARCHAR(20),
        soLuongTon INT,
        ngaySuDung DATE
    );
END;

-- 12. Bảng Khuyến Mãi
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'KhuyenMai')
BEGIN
    CREATE TABLE KhuyenMai (
        maKhuyenMai VARCHAR(20) PRIMARY KEY,
        tenKhuyenMai NVARCHAR(100),
        giaTriGiam FLOAT,
        ngayBatDau DATE,
        ngayKetThuc DATE,
        trangThai BIT
    );
END;

-- 13. Bảng Hóa Đơn
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HoaDon')
BEGIN
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
END;

-- 14. Bảng Chi Tiết Hóa Đơn
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ChiTietHoaDon')
BEGIN
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
END;

-- 15. Bảng Phiếu Thanh Toán
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PhieuThanhToan')
BEGIN
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
END;

-- Bổ sung cột sdt cho NhanVien nếu database đã được tạo từ phiên bản cũ
IF COL_LENGTH('NhanVien', 'sdt') IS NULL
    ALTER TABLE NhanVien ADD sdt VARCHAR(15) NULL;
GO







USE QuanLyPhongKhamNhaKhoa;
GO

-- ============================================
-- 1. NHAN VIEN
-- ============================================
MERGE INTO NhanVien AS T
USING (VALUES
('NV001', N'Nguyễn Minh Tuấn', '1985-03-12', '0901234001', N'Bác sĩ trưởng', N'Thứ 2 - Thứ 7', N'Thạc sĩ Răng Hàm Mặt', 30000000.0),
('NV002', N'Trần Ngọc Mai', '1990-07-21', '0901234002', N'Bác sĩ nha khoa', N'Thứ 2 - Thứ 6', N'Đại học Răng Hàm Mặt', 22000000.0),
('NV003', N'Lê Hoàng Nam', '1988-11-05', '0901234003', N'Bác sĩ nha khoa', N'Thứ 2 - Thứ 7', N'Đại học Răng Hàm Mặt', 23000000.0),
('NV004', N'Phạm Thu Hà', '1995-02-18', '0901234004', N'Lễ tân', N'Thứ 2 - Thứ 7', N'Cao đẳng', 10000000.0),
('NV005', N'Võ Quốc Bảo', '1992-09-09', '0901234005', N'Điều dưỡng', N'Thứ 2 - Thứ 7', N'Cao đẳng Điều dưỡng', 12000000.0)
) AS S(maNhanVien, tenNhanVien, ngaySinh, sdt, chucVu, lichLamViec, trinhDo, mucLuong)
ON T.maNhanVien = S.maNhanVien
WHEN NOT MATCHED THEN
INSERT (maNhanVien, tenNhanVien, ngaySinh, sdt, chucVu, lichLamViec, trinhDo, mucLuong)
VALUES (S.maNhanVien, S.tenNhanVien, S.ngaySinh, S.sdt, S.chucVu, S.lichLamViec, S.trinhDo, S.mucLuong);
GO

-- ============================================
-- 2. KHACH HANG
-- ============================================
MERGE INTO KhachHang AS T
USING (VALUES
('KH001', N'Nguyễn Thị Thanh Trúc', N'Nữ', '2002-05-14', '0911000001', N'Không', 120),
('KH002', N'Trần Văn Hoàng', N'Nam', '1998-08-20', '0911000002', N'Không', 80),
('KH003', N'Lê Thị Mỹ Linh', N'Nữ', '2000-12-01', '0911000003', N'Dị ứng nhẹ', 150),
('KH004', N'Phạm Minh Khang', N'Nam', '1995-04-10', '0911000004', N'Không', 60),
('KH005', N'Võ Ngọc Ánh', N'Nữ', '2001-09-25', '0911000005', N'Răng nhạy cảm', 200),
('KH006', N'Đặng Quốc Huy', N'Nam', '1999-01-16', '0911000006', N'Không', 40),
('KH007', N'Bùi Khánh Vy', N'Nữ', '2003-06-30', '0911000007', N'Không', 90),
('KH008', N'Đỗ Thành Đạt', N'Nam', '1997-10-08', '0911000008', N'Viêm lợi trước đây', 110),
('KH009', N'Hoàng Thu Thảo', N'Nữ', '1996-02-22', '0911000009', N'Không', 70),
('KH010', N'Phan Gia Bảo', N'Nam', '2004-07-19', '0911000010', N'Không', 30)
) AS S(maKhachHang, tenKH, gioiTinh, ngaySinh, sdt, tienSuBenh, diemTichLuy)
ON T.maKhachHang = S.maKhachHang
WHEN NOT MATCHED THEN
INSERT (maKhachHang, tenKH, gioiTinh, ngaySinh, sdt, tienSuBenh, diemTichLuy)
VALUES (S.maKhachHang, S.tenKH, S.gioiTinh, S.ngaySinh, S.sdt, S.tienSuBenh, S.diemTichLuy);
GO

-- ============================================
-- 3. TAI KHOAN
-- trangThai: 1 = hoat dong, 0 = ngung hoat dong
-- ============================================
MERGE INTO TaiKhoan AS T
USING (VALUES
('admin', N'123456', 1, N'Admin', 'NV001'),
('bsmai', N'123456', 1, N'Bác sĩ', 'NV002'),
('bsnam', N'123456', 1, N'Bác sĩ', 'NV003'),
('letan', N'123456', 1, N'Lễ tân', 'NV004'),
('dieuduong', N'123456', 1, N'Điều dưỡng', 'NV005')
) AS S(tenDN, matKhau, trangThai, quyen, maNhanVien)
ON T.tenDN = S.tenDN
WHEN NOT MATCHED THEN
INSERT (tenDN, matKhau, trangThai, quyen, maNhanVien)
VALUES (S.tenDN, S.matKhau, S.trangThai, S.quyen, S.maNhanVien);
GO

-- ============================================
-- 4. LICH LAM VIEC
-- ============================================
MERGE INTO LichLamViec AS T
USING (VALUES
('LLV001', 'NV001', '2026-10-24', '08:00', '17:00', N'Đang làm'),
('LLV002', 'NV002', '2026-10-24', '08:00', '16:00', N'Đang làm'),
('LLV003', 'NV003', '2026-10-24', '09:00', '17:00', N'Đang làm'),
('LLV004', 'NV004', '2026-10-24', '07:30', '17:30', N'Đang làm'),
('LLV005', 'NV005', '2026-10-24', '08:00', '17:00', N'Đang làm'),
('LLV006', 'NV002', '2026-10-25', '08:00', '16:00', N'Đã xếp lịch'),
('LLV007', 'NV003', '2026-10-25', '09:00', '17:00', N'Đã xếp lịch')
) AS S(maLichLamViec, maNhanVien, ngayLamViec, gioBatDau, gioKetThuc, trangThai)
ON T.maLichLamViec = S.maLichLamViec
WHEN NOT MATCHED THEN
INSERT (maLichLamViec, maNhanVien, ngayLamViec, gioBatDau, gioKetThuc, trangThai)
VALUES (S.maLichLamViec, S.maNhanVien, S.ngayLamViec, S.gioBatDau, S.gioKetThuc, S.trangThai);
GO

-- ============================================
-- 5. DICH VU
-- ============================================
MERGE INTO DichVu AS T
USING (VALUES
('DV001', N'Lấy cao răng', 200000.0, N'Vệ sinh răng miệng'),
('DV002', N'Trám răng', 350000.0, N'Trám răng sâu'),
('DV003', N'Tẩy trắng răng', 1500000.0, N'Làm trắng răng'),
('DV004', N'Nhổ răng', 500000.0, N'Nhổ răng thông thường'),
('DV005', N'Khám tổng quát', 100000.0, N'Kiểm tra sức khỏe răng miệng'),
('DV006', N'Niềng răng', 25000000.0, N'Dịch vụ chỉnh nha')
) AS S(maDichVu, tenDichVu, donGia, moTa)
ON T.maDichVu = S.maDichVu
WHEN NOT MATCHED THEN
INSERT (maDichVu, tenDichVu, donGia, moTa)
VALUES (S.maDichVu, S.tenDichVu, S.donGia, S.moTa);
GO

-- ============================================
-- 6. THUOC
-- ============================================
MERGE INTO Thuoc AS T
USING (VALUES
('TH001', N'Paracetamol', N'Viên', 2000.0, 500),
('TH002', N'Amoxicillin', N'Viên', 3000.0, 300),
('TH003', N'Vitamin C', N'Viên', 1000.0, 400),
('TH004', N'Nước súc miệng', N'Chai', 45000.0, 100),
('TH005', N'Gel giảm đau răng', N'Tuýp', 35000.0, 80)
) AS S(maThuoc, tenThuoc, donViTinh, donGia, soLuongTon)
ON T.maThuoc = S.maThuoc
WHEN NOT MATCHED THEN
INSERT (maThuoc, tenThuoc, donViTinh, donGia, soLuongTon)
VALUES (S.maThuoc, S.tenThuoc, S.donViTinh, S.donGia, S.soLuongTon);
GO

-- ============================================
-- 7. VAT TU
-- ============================================
MERGE INTO VatTu AS T
USING (VALUES
('VT001', N'Găng tay y tế', N'Hộp', 50, '2026-10-01'),
('VT002', N'Khẩu trang y tế', N'Hộp', 80, '2026-10-01'),
('VT003', N'Bông y tế', N'Gói', 100, '2026-10-01'),
('VT004', N'Vật liệu trám răng', N'Tuýp', 30, '2026-10-01'),
('VT005', N'Dụng cụ khám răng', N'Bộ', 20, '2026-10-01')
) AS S(maVatTu, tenVatTu, donViTinh, soLuongTon, ngaySuDung)
ON T.maVatTu = S.maVatTu
WHEN NOT MATCHED THEN
INSERT (maVatTu, tenVatTu, donViTinh, soLuongTon, ngaySuDung)
VALUES (S.maVatTu, S.tenVatTu, S.donViTinh, S.soLuongTon, S.ngaySuDung);
GO

-- ============================================
-- 8. KHUYEN MAI
-- trangThai: 1 = dang ap dung, 0 = chua ap dung
-- ============================================
MERGE INTO KhuyenMai AS T
USING (VALUES
('KM001', N'Ưu đãi khách hàng mới', 10.0, '2026-10-01', '2026-12-31', 1),
('KM002', N'Ưu đãi sinh viên', 5.0, '2026-09-01', '2026-12-31', 1),
('KM003', N'Khuyến mãi cuối năm', 15.0, '2026-11-01', '2026-12-31', 0)
) AS S(maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai)
ON T.maKhuyenMai = S.maKhuyenMai
WHEN NOT MATCHED THEN
INSERT (maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai)
VALUES (S.maKhuyenMai, S.tenKhuyenMai, S.giaTriGiam, S.ngayBatDau, S.ngayKetThuc, S.trangThai);
GO

-- ============================================
-- 9. LICH HEN
-- ngayHen co kieu DATE, chi luu ngay
-- ============================================
MERGE INTO LichHen AS T
USING (VALUES
('LH001', 'KH001', 'NV002', '2026-10-24', N'Hoàn thành', N'Lấy cao răng'),
('LH002', 'KH002', 'NV003', '2026-10-24', N'Đã xác nhận', N'Khám tổng quát'),
('LH003', 'KH003', 'NV002', '2026-10-24', N'Chờ xác nhận', N'Trám răng'),
('LH004', 'KH004', 'NV001', '2026-10-24', N'Đã xác nhận', N'Tư vấn chỉnh nha'),
('LH005', 'KH005', 'NV003', '2026-10-25', N'Đã xác nhận', N'Tẩy trắng răng'),
('LH006', 'KH006', 'NV002', '2026-10-25', N'Chờ xác nhận', N'Khám răng'),
('LH007', 'KH007', 'NV003', '2026-10-22', N'Hoàn thành', N'Lấy cao răng'),
('LH008', 'KH008', 'NV002', '2026-10-23', N'Hoàn thành', N'Trám răng'),
('LH009', 'KH009', 'NV001', '2026-10-26', N'Đã xác nhận', N'Tư vấn nha khoa'),
('LH010', 'KH010', 'NV003', '2026-10-26', N'Chờ xác nhận', N'Khám tổng quát')
) AS S(maLichHen, maKhachHang, maNhanVien, ngayHen, trangThai, ghiChu)
ON T.maLichHen = S.maLichHen
WHEN NOT MATCHED THEN
INSERT (maLichHen, maKhachHang, maNhanVien, ngayHen, trangThai, ghiChu)
VALUES (S.maLichHen, S.maKhachHang, S.maNhanVien, S.ngayHen, S.trangThai, S.ghiChu);
GO

-- ============================================
-- 10. HO SO BENH AN
-- ============================================
MERGE INTO HoSoBenhAn AS T
USING (VALUES
('BA001', 'KH001', 'NV002', '2026-10-24', N'Răng sạch sau điều trị', N'Cao răng', N'Hoàn thành'),
('BA002', 'KH002', 'NV003', '2026-10-23', N'Có một răng sâu nhẹ', N'Sâu răng', N'Đang điều trị'),
('BA003', 'KH003', 'NV002', '2026-10-22', N'Răng hàm có lỗ sâu', N'Sâu răng hàm', N'Đang điều trị'),
('BA004', 'KH004', 'NV001', '2026-10-21', N'Cần tư vấn chỉnh nha', N'Lệch khớp cắn', N'Đang điều trị'),
('BA005', 'KH005', 'NV003', '2026-10-20', N'Răng nhạy cảm nhẹ', N'Răng nhạy cảm', N'Hoàn thành'),
('BA006', 'KH007', 'NV002', '2026-10-19', N'Cao răng mức độ nhẹ', N'Cao răng', N'Hoàn thành'),
('BA007', 'KH008', 'NV003', '2026-10-23', N'Viêm lợi nhẹ', N'Viêm lợi', N'Đang điều trị')
) AS S(maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai)
ON T.maBenhAn = S.maBenhAn
WHEN NOT MATCHED THEN
INSERT (maBenhAn, maKhachHang, maNhanVien, ngayKham, ketQuaKham, chuanDoan, trangThai)
VALUES (S.maBenhAn, S.maKhachHang, S.maNhanVien, S.ngayKham, S.ketQuaKham, S.chuanDoan, S.trangThai);
GO

-- ============================================
-- 11. KE HOACH DIEU TRI
-- ============================================
MERGE INTO KeHoachDieuTri AS T
USING (VALUES
('KHDT001', 'BA001', 'NV002', 'DV001', '2026-10-24', N'Điều trị hoàn tất', 1, '30 phút', 200000.0, N'Hoàn thành'),
('KHDT002', 'BA002', 'NV003', 'DV002', '2026-10-23', N'Trám răng sâu', 2, '45 phút', 350000.0, N'Đang điều trị'),
('KHDT003', 'BA003', 'NV002', 'DV002', '2026-10-22', N'Trám răng hàm', 2, '45 phút', 350000.0, N'Đang điều trị'),
('KHDT004', 'BA004', 'NV001', 'DV006', '2026-10-21', N'Tư vấn niềng răng', 12, '12 tháng', 25000000.0, N'Đang điều trị'),
('KHDT005', 'BA005', 'NV003', 'DV003', '2026-10-20', N'Tẩy trắng răng', 1, '60 phút', 1500000.0, N'Hoàn thành'),
('KHDT006', 'BA006', 'NV002', 'DV001', '2026-10-19', N'Lấy cao răng', 1, '30 phút', 200000.0, N'Hoàn thành')
) AS S(maKeHoach, maBenhAn, maNhanVien, maDichVu, ngayLap, ghiChu, soLankham, thoiGianDuKien, tongChiPhiDuKien, trangThai)
ON T.maKeHoach = S.maKeHoach
WHEN NOT MATCHED THEN
INSERT (maKeHoach, maBenhAn, maNhanVien, maDichVu, ngayLap, ghiChu, soLankham, thoiGianDuKien, tongChiPhiDuKien, trangThai)
VALUES (S.maKeHoach, S.maBenhAn, S.maNhanVien, S.maDichVu, S.ngayLap, S.ghiChu, S.soLankham, S.thoiGianDuKien, S.tongChiPhiDuKien, S.trangThai);
GO

-- ============================================
-- 12. DON THUOC
-- ============================================
MERGE INTO DonThuoc AS T
USING (VALUES
('DT001', 'BA002', 'NV003', 'KH002', 'TH001', N'Paracetamol', '2026-10-23', N'Đơn thuốc mẫu', 6, N'Dùng theo chỉ định của bác sĩ'),
('DT002', 'BA003', 'NV002', 'KH003', 'TH004', N'Nước súc miệng', '2026-10-22', N'Vệ sinh răng miệng', 1, N'Sử dụng theo hướng dẫn'),
('DT003', 'BA007', 'NV003', 'KH008', 'TH003', N'Vitamin C', '2026-10-23', N'Đơn thuốc mẫu', 10, N'Dùng theo chỉ định của bác sĩ')
) AS S(maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, ghiChu, soLuong, cachDung)
ON T.maDonThuoc = S.maDonThuoc
WHEN NOT MATCHED THEN
INSERT (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, ghiChu, soLuong, cachDung)
VALUES (S.maDonThuoc, S.maBenhAn, S.maNhanVien, S.maKhachHang, S.maThuoc, S.tenThuoc, S.ngayKe, S.ghiChu, S.soLuong, S.cachDung);
GO

-- ============================================
-- 13. HOA DON
-- ============================================
MERGE INTO HoaDon AS T
USING (VALUES
('HD001', 'BA001', 'KH001', 'NV002', 'KM001', '2026-10-24'),
('HD002', 'BA002', 'KH002', 'NV003', NULL, '2026-10-23'),
('HD003', 'BA003', 'KH003', 'NV002', 'KM002', '2026-10-22'),
('HD004', 'BA005', 'KH005', 'NV003', NULL, '2026-10-20'),
('HD005', 'BA006', 'KH007', 'NV002', 'KM001', '2026-10-19'),
('HD006', 'BA007', 'KH008', 'NV003', NULL, '2026-10-23')
) AS S(maHoaDon, maBenhAn, maKhachHang, maNhanVien, maKhuyenMai, ngayLap)
ON T.maHoaDon = S.maHoaDon
WHEN NOT MATCHED THEN
INSERT (maHoaDon, maBenhAn, maKhachHang, maNhanVien, maKhuyenMai, ngayLap)
VALUES (S.maHoaDon, S.maBenhAn, S.maKhachHang, S.maNhanVien, S.maKhuyenMai, S.ngayLap);
GO

-- ============================================
-- 14. CHI TIET HOA DON
-- ============================================
MERGE INTO ChiTietHoaDon AS T
USING (VALUES
('HD001', 'DV001', 'VT001', 1, 200000.0),
('HD002', 'DV002', 'VT004', 1, 350000.0),
('HD003', 'DV002', 'VT004', 1, 350000.0),
('HD004', 'DV003', 'VT002', 1, 1500000.0),
('HD005', 'DV001', 'VT003', 1, 200000.0),
('HD006', 'DV005', 'VT005', 1, 100000.0)
) AS S(maHoaDon, maDichVu, maVatTu, soLuong, donGia)
ON T.maHoaDon = S.maHoaDon
   AND T.maDichVu = S.maDichVu
   AND T.maVatTu = S.maVatTu
WHEN NOT MATCHED THEN
INSERT (maHoaDon, maDichVu, maVatTu, soLuong, donGia)
VALUES (S.maHoaDon, S.maDichVu, S.maVatTu, S.soLuong, S.donGia);
GO

-- ============================================
-- 15. PHIEU THANH TOAN
-- daThanhToan la so tien da thanh toan (FLOAT)
-- ============================================
MERGE INTO PhieuThanhToan AS T
USING (VALUES
('PTT001', 'HD001', 'KH001', 'NV004', '2026-10-24', N'Tiền mặt', 200000.0, 0.0, N'Đã thanh toán'),
('PTT002', 'HD002', 'KH002', 'NV004', '2026-10-23', N'Chuyển khoản', 350000.0, 0.0, N'Đã thanh toán'),
('PTT003', 'HD003', 'KH003', 'NV004', '2026-10-22', N'Tiền mặt', 0.0, 350000.0, N'Chưa thanh toán'),
('PTT004', 'HD004', 'KH005', 'NV004', '2026-10-20', N'Chuyển khoản', 1500000.0, 0.0, N'Đã thanh toán'),
('PTT005', 'HD005', 'KH007', 'NV004', '2026-10-19', N'Tiền mặt', 200000.0, 0.0, N'Đã thanh toán'),
('PTT006', 'HD006', 'KH008', 'NV004', '2026-10-23', N'Tiền mặt', 0.0, 100000.0, N'Chưa thanh toán')
) AS S(maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu)
ON T.maPhieu = S.maPhieu
WHEN NOT MATCHED THEN
INSERT (maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu)
VALUES (S.maPhieu, S.maHoaDon, S.maKhachHang, S.maNhanVien, S.ngayThanhToan, S.hinhThucThanhToan, S.daThanhToan, S.soTienConLai, S.ghiChu);
GO

-- ============================================
-- 16. KIEM TRA DU LIEU SAU KHI INSERT
-- ============================================
SELECT * FROM NhanVien;
SELECT * FROM KhachHang;
SELECT * FROM TaiKhoan;
SELECT * FROM LichLamViec;
SELECT * FROM LichHen;
SELECT * FROM HoSoBenhAn;
SELECT * FROM DichVu;
SELECT * FROM KeHoachDieuTri;
SELECT * FROM Thuoc;
SELECT * FROM DonThuoc;
SELECT * FROM VatTu;
SELECT * FROM KhuyenMai;
SELECT * FROM HoaDon;
SELECT * FROM ChiTietHoaDon;
SELECT * FROM PhieuThanhToan;
GO



USE QuanLyPhongKhamNhaKhoa;
GO

SELECT COUNT(*) AS TongNhanVien FROM NhanVien;
SELECT COUNT(*) AS TongKhachHang FROM KhachHang;
SELECT COUNT(*) AS TongLichHen FROM LichHen;
SELECT COUNT(*) AS TongHoaDon FROM HoaDon;

SELECT * FROM KhachHang;
SELECT * FROM NhanVien;