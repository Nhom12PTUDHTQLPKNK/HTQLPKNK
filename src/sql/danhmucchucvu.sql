USE QuanLyPhongKhamNhaKhoa;
GO

IF OBJECT_ID(N'dbo.ChucVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChucVu (
        tenChucVu NVARCHAR(50) NOT NULL PRIMARY KEY,
        moTa NVARCHAR(255) NULL,
        trangThai BIT NOT NULL CONSTRAINT DF_ChucVu_trangThai DEFAULT 1
    );
END;
GO

MERGE dbo.ChucVu AS target
USING (VALUES
    (N'Nha sĩ trưởng'), (N'Nha sĩ'), (N'Y tá'), (N'Lễ tân'), (N'Quản lý'), (N'Admin')
) AS source(tenChucVu)
ON target.tenChucVu = source.tenChucVu
WHEN NOT MATCHED THEN INSERT (tenChucVu) VALUES (source.tenChucVu);
GO

INSERT INTO dbo.ChucVu (tenChucVu)
SELECT DISTINCT nv.chucVu
FROM dbo.NhanVien nv
WHERE nv.chucVu IS NOT NULL AND LTRIM(RTRIM(nv.chucVu)) <> N''
  AND NOT EXISTS (SELECT 1 FROM dbo.ChucVu cv WHERE cv.tenChucVu = nv.chucVu);
GO

INSERT INTO dbo.ChucVu (tenChucVu)
SELECT DISTINCT tk.quyen
FROM dbo.TaiKhoan tk
WHERE tk.quyen IS NOT NULL AND LTRIM(RTRIM(tk.quyen)) <> N''
  AND NOT EXISTS (SELECT 1 FROM dbo.ChucVu cv WHERE cv.tenChucVu = tk.quyen);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_NhanVien_ChucVu')
    ALTER TABLE dbo.NhanVien ADD CONSTRAINT FK_NhanVien_ChucVu
        FOREIGN KEY (chucVu) REFERENCES dbo.ChucVu(tenChucVu);
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_TaiKhoan_ChucVu')
    ALTER TABLE dbo.TaiKhoan ADD CONSTRAINT FK_TaiKhoan_ChucVu
        FOREIGN KEY (quyen) REFERENCES dbo.ChucVu(tenChucVu);
GO

IF COL_LENGTH(N'dbo.NhanVien', N'hinhAnh') IS NULL
    ALTER TABLE dbo.NhanVien ADD hinhAnh NVARCHAR(1000) NULL;
GO
IF COL_LENGTH(N'dbo.HoaDon', N'loaiHoaDon') IS NULL
    ALTER TABLE dbo.HoaDon ADD loaiHoaDon NVARCHAR(40) NOT NULL
        CONSTRAINT DF_HoaDon_loaiHoaDon DEFAULT N'Hóa đơn khám chữa bệnh';
GO
