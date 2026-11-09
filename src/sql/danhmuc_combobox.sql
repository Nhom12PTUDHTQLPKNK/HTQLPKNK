USE QuanLyPhongKhamNhaKhoa;
GO

IF OBJECT_ID(N'dbo.NHOMTHUOC', N'U') IS NULL
    CREATE TABLE dbo.NHOMTHUOC (MaNhomThuoc VARCHAR(10) NOT NULL PRIMARY KEY, TenNhomThuoc NVARCHAR(100) NOT NULL UNIQUE, MoTa NVARCHAR(500) NULL);
IF OBJECT_ID(N'dbo.DANGBAOCHE', N'U') IS NULL
    CREATE TABLE dbo.DANGBAOCHE (MaDangBaoChe VARCHAR(10) NOT NULL PRIMARY KEY, TenDangBaoChe NVARCHAR(100) NOT NULL UNIQUE, MoTa NVARCHAR(255) NULL);
IF OBJECT_ID(N'dbo.DUONGDUNG', N'U') IS NULL
    CREATE TABLE dbo.DUONGDUNG (MaDuongDung VARCHAR(10) NOT NULL PRIMARY KEY, TenDuongDung NVARCHAR(100) NOT NULL UNIQUE, MoTa NVARCHAR(255) NULL);
GO
IF NOT EXISTS (SELECT 1 FROM dbo.NHOMTHUOC)
    INSERT dbo.NHOMTHUOC VALUES ('NT001', N'Kháng sinh', N''), ('NT002', N'Giảm đau - Hạ sốt', N''), ('NT003', N'Kháng viêm', N''), ('NT004', N'Sát khuẩn', N''), ('NT005', N'Gây tê nha khoa', N''), ('NT006', N'Corticosteroid', N'');
IF NOT EXISTS (SELECT 1 FROM dbo.DANGBAOCHE)
    INSERT dbo.DANGBAOCHE VALUES ('DBC001', N'Viên nén', N''), ('DBC002', N'Viên nang', N''), ('DBC003', N'Dung dịch', N''), ('DBC004', N'Gel bôi', N''), ('DBC005', N'Ống tiêm', N''), ('DBC006', N'Viên sủi', N'');
IF NOT EXISTS (SELECT 1 FROM dbo.DUONGDUNG)
    INSERT dbo.DUONGDUNG VALUES ('DD001', N'Đường uống', N''), ('DD002', N'Súc miệng', N''), ('DD003', N'Dùng ngoài', N''), ('DD004', N'Tiêm', N''), ('DD005', N'Bôi tại chỗ', N'');
GO

IF OBJECT_ID(N'dbo.DanhMucLoaiDieuTri', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DanhMucLoaiDieuTri (
        maLoaiDieuTri VARCHAR(10) NOT NULL PRIMARY KEY,
        tenLoaiDieuTri NVARCHAR(80) NOT NULL UNIQUE,
        moTa NVARCHAR(255) NULL,
        trangThai BIT NOT NULL CONSTRAINT DF_DanhMucLoaiDieuTri_trangThai DEFAULT 1
    );
END;
GO
IF NOT EXISTS (SELECT 1 FROM dbo.DanhMucLoaiDieuTri)
    INSERT dbo.DanhMucLoaiDieuTri (maLoaiDieuTri, tenLoaiDieuTri) VALUES
    ('LDT001', N'Nha khoa tổng quát'), ('LDT002', N'Thẩm mỹ'), ('LDT003', N'Chỉnh nha'),
    ('LDT004', N'Tiểu phẫu'), ('LDT005', N'Phục hình sứ');
GO

IF OBJECT_ID(N'dbo.DanhMucGioiTinh', N'U') IS NOT NULL
    DROP TABLE dbo.DanhMucGioiTinh;
GO
