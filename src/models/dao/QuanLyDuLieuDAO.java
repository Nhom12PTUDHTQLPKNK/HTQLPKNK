package models.dao;

import models.entity.*;
import controllers.NhatKyKiemToan;
import models.entity.TaiKhoanThongTin;
import models.entity.VaiTro;
import sql.DBConnection;
import controllers.KiemSoatTruyCap;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.*;


/**
 * Lớp quản lý các hoạt động cơ sở dữ liệu cho hệ thống phòng khám
 */
public class QuanLyDuLieuDAO {

    /**
     * Xác thực người dùng từ bảng TaiKhoan & NhanVien trong SQL Server
     */
    public static TaiKhoanThongTin authenticate(String username, String password) {
        String sql = "SELECT tk.tenDN, tk.matKhau, tk.quyen, nv.tenNhanVien " +
                     "FROM TaiKhoan tk LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien " +
                     "WHERE tk.tenDN = ? AND tk.matKhau = ? AND tk.trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("tenNhanVien");
                    if (name == null) name = rs.getString("tenDN");
                    VaiTro role = VaiTro.fromString(rs.getString("quyen"));
                    return new TaiKhoanThongTin(rs.getString("tenDN"), rs.getString("matKhau"), name, role);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



















    public static String getInventoryItemDisplayName(String itemId, String itemType) {
        if (itemId == null || itemId.trim().isEmpty()) return null;
        String table = "Thuoc".equalsIgnoreCase(itemType) || "THUOC".equalsIgnoreCase(itemType) ? "Thuoc" : "VatTu";
        String idColumn = "Thuoc".equalsIgnoreCase(table) ? "maThuoc" : "maVatTu";
        String nameColumn = "Thuoc".equalsIgnoreCase(table) ? "tenThuoc" : "tenVatTu";
        String sql = "SELECT " + nameColumn + " FROM " + table + " WHERE " + idColumn + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInventoryItemTypeById(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) return null;
        String sql = "SELECT 'Thuoc' AS loai, maThuoc AS id FROM Thuoc WHERE maThuoc = ? UNION ALL SELECT 'VatTu', maVatTu FROM VatTu WHERE maVatTu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId.trim());
            ps.setString(2, itemId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("loai");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }





    /**
     * Tạo DefaultTableModel rỗng có tiêu đề cột
     */
    public static DefaultTableModel createEmptyModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    /**
     * Đọc dữ liệu từ SQL Server vào TableModel dựa trên TinhNang tương ứng
     */
    public static DefaultTableModel loadModelFromDB(TinhNang f) {
        switch (f) {
            case D_KHO:
            case S_KHO:
                return executeQueryToModel(
                    "SELECT maThuoc AS Ma, tenThuoc AS Ten, 'Thuoc' AS Loai, soLuongTon AS Ton, donViTinh AS DonVi, CAST(0 AS DECIMAL(18,2)) AS giaBan, GiaNhap AS giaNhap, '' AS HanDung FROM Thuoc " +
                    "UNION ALL " +
                    "SELECT maVatTu AS Ma, tenVatTu AS Ten, 'VatTu' AS Loai, soLuongTon AS Ton, donViTinh AS DonVi, giaBan, giaNhap, CAST(ngaySuDung AS VARCHAR) AS HanDung FROM VatTu",
                    new String[]{"Mã", "Tên thuốc / vật tư", "Loại", "Tồn kho", "Đơn vị", "Giá bán (VNĐ)", "Giá nhập (VNĐ)", "Hạn dùng"});
            case D_GOI:
                return executeQueryToModel(
                    "SELECT maDichVu, tenDichVu, moTa, donGia, 30 AS ThoiGian FROM DichVu",
                    new String[]{"Mã gói", "Tên gói", "Mô tả", "Giá (VNĐ)", "Thời gian (phút)"});
            case D_KM:
            case S_KM:
                return executeQueryToModel(
                    "SELECT maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, " +
                    "CASE WHEN trangThai = 1 THEN N'Còn hiệu lực' ELSE N'Hết hạn' END FROM KhuyenMai",
                    new String[]{"Mã KM", "Tên khuyến mãi", "Giảm (%)", "Từ ngày", "Đến ngày", "Trạng thái"});
            case D_NV:
                return executeQueryToModel(
                    "SELECT maNhanVien, tenNhanVien, chucVu, sdt, N'Đang làm' FROM NhanVien",
                    new String[]{"Mã NV", "Họ tên", "Chức vụ", "SĐT", "Trạng thái"});
            case D_LLV:
                return executeQueryToModel(
                    "SELECT nv.tenNhanVien, l.ngayLamViec, (l.gioBatDau + ' - ' + l.gioKetThuc) AS Ca, l.trangThai " +
                    "FROM LichLamViec l JOIN NhanVien nv ON l.maNhanVien = nv.maNhanVien",
                    new String[]{"Nhân viên", "Thứ / Ngày", "Ca", "Vị trí"});
            case D_BA:
                return executeQueryToModel(
                    "SELECT h.maBenhAn, k.tenKH, h.chuanDoan, h.ngayKham, nv.tenNhanVien, h.ketQuaKham " +
                    "FROM HoSoBenhAn h JOIN KhachHang k ON h.maKhachHang = k.maKhachHang JOIN NhanVien nv ON h.maNhanVien = nv.maNhanVien",
                    new String[]{"Mã BA", "Bệnh nhân", "Chẩn đoán", "Ngày khám", "Bác sĩ", "Điều trị"});
            case S_KH:
                return executeQueryToModel(
                    "SELECT maKhachHang, tenKH, sdt, ngaySinh, CASE WHEN diemTichLuy > 0 THEN N'Có' ELSE N'Không' END, GETDATE() FROM KhachHang",
                    new String[]{"Mã KH", "Họ tên", "SĐT", "Ngày sinh", "Thành viên", "Ngày đăng ký"});
            default:
                return createEmptyModel(new String[]{"Thông tin"});
        }
    }

    /**
     * Thực thi câu truy vấn và trả về DefaultTableModel
     */
    public static DefaultTableModel executeQueryToModel(String sql, String[] cols) {
        DefaultTableModel model = createEmptyModel(cols);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int colCount = cols.length;
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * Lấy danh sách Lịch hẹn từ bảng LichHen trong SQL Server
     */
    public static List<Object[]> getUpcomingAppointments() {
        List<Object[]> res = new ArrayList<>();
        String sql = "SELECT l.maLichHen, k.tenKH, nv.tenNhanVien, l.ngayHen, '08:00' AS Gio, 'Kham benh' AS DichVu, l.trangThai " +
                     "FROM LichHen l LEFT JOIN KhachHang k ON l.maKhachHang = k.maKhachHang " +
                     "LEFT JOIN NhanVien nv ON l.maNhanVien = nv.maNhanVien " +
                     "WHERE l.ngayHen >= CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                res.add(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    rs.getDate(4) != null ? rs.getDate(4).toString() : "",
                    rs.getString(5), rs.getString(6), rs.getString(7)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Lấy thống kê các số liệu tổng quan
     */
    public static Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("customers", 0);
        stats.put("members", 0);
        stats.put("services", 0);

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM KhachHang")) {
                if (rs.next()) stats.put("customers", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM KhachHang WHERE diemTichLuy > 0")) {
                if (rs.next()) stats.put("members", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM DichVu")) {
                if (rs.next()) stats.put("services", rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Ghi một dòng dữ liệu vào SQL Server theo từng TinhNang.
     * oldKey = null => insert, ngược lại => update theo khóa cũ.
     */
    public static boolean saveRow(TinhNang feature, Object[] row, Object oldKey) {
        boolean saved;
        switch (feature) {
            case D_KHO:
                saved = saveKhoRow(row, oldKey); break;
            case D_GOI:
                saved = saveDichVu(row, oldKey); break;
            case D_KM:
                saved = saveKhuyenMai(row, oldKey); break;
            case D_NV:
                saved = saveNhanVien(row, oldKey); break;
            case D_LLV:
                saved = saveLichLamViec(row, oldKey); break;
            case D_BA:
                saved = saveBenhAn(row, oldKey); break;
            case S_KH:
                saved = saveKhachHang(row, oldKey); break;
            default:
                return false;
        }
        if (saved) {
            String action = oldKey == null ? "CREATE" : "UPDATE";
            String key = row != null && row.length > 0 ? String.valueOf(row[0]) : "";
            NhatKyKiemToan.event(action, feature.title + " - mã " + key);
        }
        return saved;
    }

    public static boolean deleteRow(TinhNang feature, Object key, Object[] row) {
        if (key == null) return false;
        String id = String.valueOf(key).trim();
        if (id.isEmpty()) return false;
        try (Connection conn = DBConnection.getConnection()) {
            switch (feature) {
                case D_KHO: {
                    String loai = row != null && row.length > 2 ? String.valueOf(row[2]) : "";
                    String sql = "Thuoc".equalsIgnoreCase(loai) ? "DELETE FROM Thuoc WHERE maThuoc = ?" : "DELETE FROM VatTu WHERE maVatTu = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, id);
                        boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id);
                        return deleted;
                    }
                }
                case D_GOI:
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM DichVu WHERE maDichVu = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                case D_KM:
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KhuyenMai WHERE maKhuyenMai = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                case D_NV:
                    try (PreparedStatement lookup = conn.prepareStatement("SELECT tenDN,quyen FROM TaiKhoan WHERE maNhanVien=?")) {
                        lookup.setString(1, id);
                        try (ResultSet account = lookup.executeQuery()) {
                            if (account.next()) {
                                String username = account.getString("tenDN");
                                if (!KiemSoatTruyCap.isAdministrator()) return false;
                                if (KiemSoatTruyCap.isCurrentAccount(username) || KiemSoatTruyCap.isProtectedAdministrator(username, account.getString("quyen"))) return false;
                            }
                        }
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM NhanVien WHERE maNhanVien = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                case D_LLV:
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM LichLamViec WHERE maLichLamViec = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                case D_BA:
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM HoSoBenhAn WHERE maBenhAn = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                case S_KH:
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KhachHang WHERE maKhachHang = ?")) {
                        ps.setString(1, id); boolean deleted = ps.executeUpdate() > 0;
                        if (deleted) NhatKyKiemToan.event("DELETE", feature.title + " - mã " + id); return deleted;
                    }
                default:
                    return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveKhoRow(Object[] row, Object oldKey) {
        if (row == null || row.length < 8) return false;
        String loai = String.valueOf(row[2]);
        String key = String.valueOf(row[0]);
        String name = String.valueOf(row[1]);
        String unit = String.valueOf(row[4]);
        String qty = String.valueOf(row[3]);
        String salePrice = String.valueOf(row[5]);
        String purchasePrice = String.valueOf(row[6]);
        String expiry = String.valueOf(row[7]);
        boolean isThuoc = "Thuoc".equalsIgnoreCase(loai) || "thuốc".equalsIgnoreCase(loai);
        String sql = isThuoc
                ? (oldKey == null ? "INSERT INTO Thuoc (maThuoc, tenThuoc, donViTinh, soLuongTon) VALUES (?, ?, ?, ?)"
                : "UPDATE Thuoc SET maThuoc = ?, tenThuoc = ?, donViTinh = ?, soLuongTon = ? WHERE maThuoc = ?")
                : (oldKey == null ? "INSERT INTO VatTu (maVatTu, tenVatTu, donViTinh, soLuongTon, giaBan, giaNhap, ngaySuDung) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE VatTu SET maVatTu = ?, tenVatTu = ?, donViTinh = ?, soLuongTon = ?, giaBan = ?, giaNhap = ?, ngaySuDung = ? WHERE maVatTu = ?");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (isThuoc) {
                ps.setString(1, key);
                ps.setString(2, name);
                ps.setString(3, unit);
                ps.setInt(4, parseInt(qty));
                if (oldKey != null) ps.setString(5, String.valueOf(oldKey));
            } else {
                ps.setString(1, key);
                ps.setString(2, name);
                ps.setString(3, unit);
                ps.setInt(4, parseInt(qty));
                ps.setBigDecimal(5, parsePrice(salePrice));
                ps.setBigDecimal(6, parsePrice(purchasePrice));
                ps.setString(7, expiry == null || expiry.isEmpty() ? null : expiry);
                if (oldKey != null) ps.setString(8, String.valueOf(oldKey));
            }
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                String table = isThuoc ? "Thuoc" : "VatTu";
                controllers.DieuKhienMaQR.saveInventoryQrData(table, key, name, isThuoc ? "THUOC" : "VAT");
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static java.math.BigDecimal parsePrice(String value) {
        String normalized = value == null ? "" : value.replaceAll("[^0-9,.-]", "").replace(",", "").trim();
        java.math.BigDecimal price = new java.math.BigDecimal(normalized);
        if (price.signum() < 0) throw new NumberFormatException("Giá phải không âm");
        return price;
    }

    private static boolean saveDichVu(Object[] row, Object oldKey) {
        if (row == null || row.length < 5) return false;
        String sql = oldKey == null
                ? "INSERT INTO DichVu (maDichVu, tenDichVu, moTa, donGia) VALUES (?, ?, ?, ?)"
                : "UPDATE DichVu SET maDichVu = ?, tenDichVu = ?, moTa = ?, donGia = ? WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setString(3, String.valueOf(row[2]));
            ps.setDouble(4, Double.parseDouble(String.valueOf(row[3]).replaceAll("[.,]", "").replace("₫", "")));
            if (oldKey != null) ps.setString(5, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveKhuyenMai(Object[] row, Object oldKey) {
        if (row == null || row.length < 6) return false;
        String sql = oldKey == null
                ? "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giaTriGiam, ngayBatDau, ngayKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE KhuyenMai SET maKhuyenMai = ?, tenKhuyenMai = ?, giaTriGiam = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ? WHERE maKhuyenMai = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setDouble(3, parseDouble(row[2]));
            ps.setString(4, String.valueOf(row[3]));
            ps.setString(5, String.valueOf(row[4]));
            ps.setBoolean(6, "Còn hiệu lực".equalsIgnoreCase(String.valueOf(row[5])) || "1".equals(String.valueOf(row[5])));
            if (oldKey != null) ps.setString(7, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveNhanVien(Object[] row, Object oldKey) {
        if (row == null || row.length < 4) return false;
        String sql = oldKey == null
                ? "INSERT INTO NhanVien (maNhanVien, tenNhanVien, chucVu, sdt) VALUES (?, ?, ?, ?)"
                : "UPDATE NhanVien SET maNhanVien = ?, tenNhanVien = ?, chucVu = ?, sdt = ? WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setString(3, String.valueOf(row[2]));
            ps.setString(4, String.valueOf(row[3]));
            if (oldKey != null) ps.setString(5, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveLichLamViec(Object[] row, Object oldKey) {
        if (row == null || row.length < 4) return false;
        String sql = oldKey == null
                ? "INSERT INTO LichLamViec (maLichLamViec, maNhanVien, ngayLamViec, gioBatDau, gioKetThuc, trangThai) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE LichLamViec SET maLichLamViec = ?, maNhanVien = ?, ngayLamViec = ?, gioBatDau = ?, gioKetThuc = ?, trangThai = ? WHERE maLichLamViec = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setString(3, String.valueOf(row[2]));
            ps.setString(4, String.valueOf(row[3]).split(" - ")[0].trim());
            ps.setString(5, String.valueOf(row[3]).split(" - ").length > 1 ? String.valueOf(row[3]).split(" - ")[1].trim() : "");
            ps.setString(6, String.valueOf(row[4]));
            if (oldKey != null) ps.setString(7, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveBenhAn(Object[] row, Object oldKey) {
        if (row == null || row.length < 6) return false;
        String sql = oldKey == null
                ? "INSERT INTO HoSoBenhAn (maBenhAn, maKhachHang, maNhanVien, ngayKham, chuanDoan, ketQuaKham, trangThai) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE HoSoBenhAn SET maBenhAn = ?, maKhachHang = ?, maNhanVien = ?, ngayKham = ?, chuanDoan = ?, ketQuaKham = ?, trangThai = ? WHERE maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setString(3, String.valueOf(row[2]));
            ps.setString(4, String.valueOf(row[3]));
            ps.setString(5, String.valueOf(row[4]));
            ps.setString(6, String.valueOf(row[5]));
            ps.setString(7, "Đang xử lý");
            if (oldKey != null) ps.setString(8, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveKhachHang(Object[] row, Object oldKey) {
        if (row == null || row.length < 5) return false;
        String sql = oldKey == null
                ? "INSERT INTO KhachHang (maKhachHang, tenKH, sdt, ngaySinh, diemTichLuy) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE KhachHang SET maKhachHang = ?, tenKH = ?, sdt = ?, ngaySinh = ?, diemTichLuy = ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(row[0]));
            ps.setString(2, String.valueOf(row[1]));
            ps.setString(3, String.valueOf(row[2]));
            ps.setString(4, String.valueOf(row[3]));
            ps.setInt(5, "Có".equalsIgnoreCase(String.valueOf(row[4])) ? 1 : 0);
            if (oldKey != null) ps.setString(6, String.valueOf(oldKey));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int parseInt(String v) {
        try { return Integer.parseInt(v.toString().replace(".", "").replace(",", "")); }
        catch (Exception ex) { return 0; }
    }

    private static double parseDouble(Object v) {
        try { return Double.parseDouble(String.valueOf(v).replaceAll("[.,]", "").replace("₫", "")); }
        catch (Exception ex) { return 0; }
    }







}
