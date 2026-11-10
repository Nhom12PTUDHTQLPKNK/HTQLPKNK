USE QuanLyPhongKhamNhaKhoa;
GO

-- Cho phép lưu tình trạng khám tổng quát không gắn với một răng cụ thể.
ALTER TABLE dbo.ChiTietKhamRang ALTER COLUMN maRang INT NULL;
GO

IF OBJECT_ID(N'dbo.DanhMucTinhTrangRang', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DanhMucTinhTrangRang (
        maTinhTrang VARCHAR(10) NOT NULL PRIMARY KEY,
        tenTinhTrang NVARCHAR(100) NOT NULL UNIQUE,
        trangThai BIT NOT NULL CONSTRAINT DF_DanhMucTinhTrangRang_trangThai DEFAULT 1
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.DanhMucTinhTrangRang)
    INSERT dbo.DanhMucTinhTrangRang (maTinhTrang, tenTinhTrang) VALUES
    ('TTR001', N'Bình thường'), ('TTR002', N'Sâu răng'), ('TTR003', N'Viêm nướu'),
    ('TTR004', N'Mẻ răng'), ('TTR005', N'Mất răng'), ('TTR006', N'Đã điều trị'),
    ('TTR007', N'Viêm tủy'), ('TTR008', N'Mòn cổ răng'), ('TTR009', N'Răng khôn mọc lệch');
GO
