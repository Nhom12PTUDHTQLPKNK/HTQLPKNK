USE QuanLyPhongKhamNhaKhoa;
GO

IF OBJECT_ID(N'dbo.ChucVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChucVu (
        tenChucVu NVARCHAR(50) NOT NULL CONSTRAINT PK_ChucVu PRIMARY KEY,
        moTa NVARCHAR(255) NULL,
        trangThai BIT NOT NULL CONSTRAINT DF_ChucVu_trangThai DEFAULT 1
    );
END;
GO

MERGE dbo.ChucVu AS target
USING (VALUES (N'Nha sĩ trưởng'),(N'Nha sĩ'),(N'Y tá'),(N'Lễ tân'),(N'Quản lý'),(N'Admin')) source(tenChucVu)
ON target.tenChucVu=source.tenChucVu
WHEN NOT MATCHED THEN INSERT(tenChucVu) VALUES(source.tenChucVu);
GO

INSERT INTO dbo.ChucVu(tenChucVu)
SELECT DISTINCT roleName.tenChucVu
FROM (
    SELECT chucVu AS tenChucVu FROM dbo.NhanVien WHERE chucVu IS NOT NULL AND LTRIM(RTRIM(chucVu))<>N''
    UNION
    SELECT quyen FROM dbo.TaiKhoan WHERE quyen IS NOT NULL AND LTRIM(RTRIM(quyen))<>N''
) roleName
WHERE NOT EXISTS(SELECT 1 FROM dbo.ChucVu cv WHERE cv.tenChucVu=roleName.tenChucVu);
GO

-- Chỉ tài khoản admin cố định được giữ chức vụ Admin.
IF (SELECT COUNT(*) FROM dbo.TaiKhoan WHERE LOWER(tenDN)='admin') > 1
    THROW 50002, N'Cơ sở dữ liệu có nhiều tài khoản tên admin khác kiểu chữ; cần hợp nhất trước khi áp dụng.', 1;
UPDATE dbo.TaiKhoan SET tenDN='admin'
WHERE LOWER(tenDN)='admin' AND tenDN COLLATE Latin1_General_100_BIN2 <> 'admin';
UPDATE dbo.NhanVien SET chucVu=N'Lễ tân'
WHERE UPPER(LTRIM(RTRIM(ISNULL(chucVu,N''))))=N'ADMIN';
UPDATE dbo.TaiKhoan SET quyen=N'Lễ tân'
WHERE UPPER(LTRIM(RTRIM(ISNULL(quyen,N''))))=N'ADMIN' AND LOWER(tenDN)<>'admin';
UPDATE dbo.TaiKhoan SET quyen=N'Admin' WHERE LOWER(tenDN)='admin';
GO

IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE LOWER(tenDN)='admin')
    THROW 50001, N'Cần có tài khoản admin cố định trước khi áp dụng phân quyền chức vụ.', 1;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_NhanVien_ChucVu')
    ALTER TABLE dbo.NhanVien ADD CONSTRAINT FK_NhanVien_ChucVu
        FOREIGN KEY(chucVu) REFERENCES dbo.ChucVu(tenChucVu);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_NhanVien_NotAdminRole')
    ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_NhanVien_NotAdminRole
        CHECK (chucVu IS NULL OR UPPER(LTRIM(RTRIM(chucVu)))<>'ADMIN');
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_TaiKhoan_ChucVu')
    ALTER TABLE dbo.TaiKhoan ADD CONSTRAINT FK_TaiKhoan_ChucVu
        FOREIGN KEY(quyen) REFERENCES dbo.ChucVu(tenChucVu);
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_TaiKhoan_AdminRole')
    ALTER TABLE dbo.TaiKhoan WITH CHECK ADD CONSTRAINT CK_TaiKhoan_AdminRole
        CHECK ((LOWER(tenDN)='admin' AND tenDN COLLATE Latin1_General_100_BIN2='admin'
                    AND quyen IS NOT NULL AND UPPER(LTRIM(RTRIM(quyen)))='ADMIN') OR
               (LOWER(tenDN)<>'admin' AND (quyen IS NULL OR UPPER(LTRIM(RTRIM(quyen)))<>'ADMIN')));
GO

IF OBJECT_ID(N'dbo.ChucVuQuyen', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChucVuQuyen (
        tenChucVu NVARCHAR(50) NOT NULL,
        maQuyen NVARCHAR(40) NOT NULL,
        CONSTRAINT PK_ChucVuQuyen PRIMARY KEY (tenChucVu, maQuyen),
        CONSTRAINT FK_ChucVuQuyen_ChucVu FOREIGN KEY (tenChucVu)
            REFERENCES dbo.ChucVu(tenChucVu) ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

INSERT INTO dbo.ChucVuQuyen(tenChucVu, maQuyen)
SELECT p.tenChucVu, p.maQuyen
FROM (VALUES
    (N'Nha sĩ trưởng',N'D_BA'),(N'Nha sĩ trưởng',N'D_GOI'),(N'Nha sĩ trưởng',N'D_KM'),(N'Nha sĩ trưởng',N'D_LLV'),(N'Nha sĩ trưởng',N'D_NV'),(N'Nha sĩ trưởng',N'BOOK'),(N'Nha sĩ trưởng',N'S_KH'),(N'Nha sĩ trưởng',N'S_KM'),(N'Nha sĩ trưởng',N'S_KHO'),(N'Nha sĩ trưởng',N'ST_DV'),
    (N'Nha sĩ',N'D_BA'),(N'Nha sĩ',N'D_GOI'),(N'Nha sĩ',N'D_KM'),(N'Nha sĩ',N'D_LLV'),(N'Nha sĩ',N'BOOK'),(N'Nha sĩ',N'S_KH'),(N'Nha sĩ',N'S_KM'),(N'Nha sĩ',N'S_KHO'),
    (N'Lễ tân',N'D_GOI'),(N'Lễ tân',N'D_KM'),(N'Lễ tân',N'D_LLV'),(N'Lễ tân',N'BOOK'),(N'Lễ tân',N'PAY'),(N'Lễ tân',N'S_KH'),(N'Lễ tân',N'S_KM'),(N'Lễ tân',N'ST_DT'),(N'Lễ tân',N'ST_KH'),
    (N'Y tá',N'D_BA'),(N'Y tá',N'D_KHO'),(N'Y tá',N'D_LLV'),(N'Y tá',N'S_KH'),(N'Y tá',N'S_KHO'),
    (N'Quản lý',N'HOME'),(N'Quản lý',N'ACCOUNT'),(N'Quản lý',N'HELP'),(N'Quản lý',N'D_BA'),(N'Quản lý',N'D_GOI'),(N'Quản lý',N'D_KM'),(N'Quản lý',N'D_LLV'),(N'Quản lý',N'D_KHO'),(N'Quản lý',N'D_NV'),(N'Quản lý',N'BOOK'),(N'Quản lý',N'PAY'),(N'Quản lý',N'S_KH'),(N'Quản lý',N'S_KM'),(N'Quản lý',N'S_KHO'),(N'Quản lý',N'ST_DT'),(N'Quản lý',N'ST_KH'),(N'Quản lý',N'ST_DV'),(N'Quản lý',N'ST_KHO'),
    (N'Admin',N'HOME'),(N'Admin',N'ACCOUNT'),(N'Admin',N'HELP'),(N'Admin',N'D_BA'),(N'Admin',N'D_GOI'),(N'Admin',N'D_KM'),(N'Admin',N'D_LLV'),(N'Admin',N'D_KHO'),(N'Admin',N'D_NV'),(N'Admin',N'BOOK'),(N'Admin',N'PAY'),(N'Admin',N'S_KH'),(N'Admin',N'S_KM'),(N'Admin',N'S_KHO'),(N'Admin',N'ST_DT'),(N'Admin',N'ST_KH'),(N'Admin',N'ST_DV'),(N'Admin',N'ST_KHO')
) p(tenChucVu,maQuyen)
WHERE EXISTS (SELECT 1 FROM dbo.ChucVu cv WHERE cv.tenChucVu=p.tenChucVu)
  AND NOT EXISTS (SELECT 1 FROM dbo.ChucVuQuyen q WHERE q.tenChucVu=p.tenChucVu AND q.maQuyen=p.maQuyen);
GO
