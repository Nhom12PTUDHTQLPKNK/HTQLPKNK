USE QuanLyPhongKhamNhaKhoa;
GO

IF COL_LENGTH('dbo.THUOC', 'GiaNhap') IS NULL
    ALTER TABLE dbo.THUOC
    ADD GiaNhap DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_THUOC_GiaNhap DEFAULT 0;
GO

IF COL_LENGTH('dbo.VatTu', 'giaBan') IS NULL
    ALTER TABLE dbo.VatTu
    ADD giaBan DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_VatTu_giaBan DEFAULT 0;
GO

IF COL_LENGTH('dbo.VatTu', 'giaNhap') IS NULL
    ALTER TABLE dbo.VatTu
    ADD giaNhap DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_VatTu_giaNhap DEFAULT 0;
GO
