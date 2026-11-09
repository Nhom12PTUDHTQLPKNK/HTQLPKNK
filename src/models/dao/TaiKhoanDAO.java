package models.dao;

import controllers.NhatKyKiemToan;
import models.entity.TaiKhoanThongTin;
import models.entity.VaiTro;
import controllers.KiemSoatTruyCap;
import sql.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    /**
     * Tìm thông tin tài khoản linh hoạt (Hỗ trợ tìm theo tenDN, maNhanVien hoặc tenNhanVien)
     */
    public TaiKhoanThongTin findByUsername(String input) {
        String sql = "SELECT t.* FROM TaiKhoan t " +
                     "LEFT JOIN NhanVien n ON t.maNhanVien = n.maNhanVien " +
                     "WHERE t.tenDN = ? OR t.maNhanVien = ? OR n.tenNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, input);
            stmt.setString(2, input);
            stmt.setString(3, input);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String username = rs.getString("tenDN");
                String pass = rs.getString("matKhau");
                String quyenStr = rs.getString("quyen");
                VaiTro role = VaiTro.fromString(quyenStr);
                
                return new TaiKhoanThongTin(username, pass, rs.getString("maNhanVien"), role, quyenStr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Object[]> getAllAccounts() {
        List<Object[]> list = new ArrayList<>();
        if (!KiemSoatTruyCap.isAdministrator()) return list;
        String sql = "SELECT tenDN, maNhanVien, quyen, trangThai FROM TaiKhoan";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String u = rs.getString("tenDN");
                String id = rs.getString("maNhanVien");
                String r = rs.getString("quyen");
                boolean statusBit = rs.getBoolean("trangThai");
                String s = statusBit ? "Hoạt động" : "Tạm khóa";
                list.add(new Object[]{u, id != null ? id : "", displayRole(r), s});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertAccount(String tenDN, String matKhau, String quyen, boolean trangThai, String maNhanVien) {
        if (!KiemSoatTruyCap.isAdministrator() || tenDN == null || tenDN.trim().isEmpty()
                || matKhau == null || matKhau.trim().isEmpty()
                || "admin".equalsIgnoreCase(tenDN.trim())
                || "admin".equalsIgnoreCase(quyen == null ? "" : quyen.trim())) return false;
        String sql = "INSERT INTO TaiKhoan (tenDN, matKhau, quyen, trangThai, maNhanVien) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDN);
            stmt.setString(2, (matKhau == null || matKhau.trim().isEmpty()) ? "123456" : matKhau);
            stmt.setString(3, quyen);
            stmt.setBoolean(4, trangThai);
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                stmt.setNull(5, Types.VARCHAR);
            } else {
                stmt.setString(5, maNhanVien);
            }
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) NhatKyKiemToan.event("CREATE", "Tài khoản nhân sự " + tenDN.trim());
            return inserted;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAccount(String tenDN, String matKhau, String quyen, boolean trangThai, String maNhanVien, boolean updatePassword) {
        if (!KiemSoatTruyCap.isAdministrator() || tenDN == null || tenDN.trim().isEmpty()
                || "admin".equalsIgnoreCase(tenDN.trim())
                || "admin".equalsIgnoreCase(quyen == null ? "" : quyen.trim())
                || (updatePassword && (matKhau == null || matKhau.trim().isEmpty()))) return false;
        if (KiemSoatTruyCap.isProtectedAdministrator(tenDN, null)
                && (!trangThai || !KiemSoatTruyCap.isProtectedAdministrator(tenDN, quyen))) return false;
        String sql;
        if (updatePassword) {
            sql = "UPDATE TaiKhoan SET matKhau = ?, quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ?";
        } else {
            sql = "UPDATE TaiKhoan SET quyen = ?, trangThai = ?, maNhanVien = ? WHERE tenDN = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (isProtectedAdministrator(conn, tenDN)
                    && (!trangThai || !KiemSoatTruyCap.isProtectedAdministrator(tenDN, quyen))) return false;
            if (updatePassword) {
                stmt.setString(1, matKhau);
                stmt.setString(2, quyen);
                stmt.setBoolean(3, trangThai);
                if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                    stmt.setNull(4, Types.VARCHAR);
                } else {
                    stmt.setString(4, maNhanVien);
                }
                stmt.setString(5, tenDN);
            } else {
                stmt.setString(1, quyen);
                stmt.setBoolean(2, trangThai);
                if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                    stmt.setNull(3, Types.VARCHAR);
                } else {
                    stmt.setString(3, maNhanVien);
                }
                stmt.setString(4, tenDN);
            }
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event("UPDATE", "Tài khoản/quyền nhân sự " + tenDN.trim());
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteAccount(String tenDN) {
        if (!KiemSoatTruyCap.isAdministrator() || KiemSoatTruyCap.isCurrentAccount(tenDN)) return false;
        String sql = "DELETE FROM TaiKhoan WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (isProtectedAdministrator(conn, tenDN)) return false;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDN);
                boolean deleted = stmt.executeUpdate() > 0;
                if (deleted) NhatKyKiemToan.event("DELETE", "Tài khoản nhân sự " + tenDN.trim());
                return deleted;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAccountRoleByEmployeeId(String maNhanVien, String quyen) {
        if (!KiemSoatTruyCap.isAdministrator() || "admin".equalsIgnoreCase(quyen == null ? "" : quyen.trim())) return false;
        String sql = "UPDATE TaiKhoan SET quyen = ? WHERE maNhanVien = ? AND LOWER(tenDN) <> 'admin' AND UPPER(LTRIM(RTRIM(ISNULL(quyen,'')))) NOT IN ('ADMIN','QUAN_LY')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, quyen);
            stmt.setString(2, maNhanVien);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event("UPDATE", "Phân quyền tài khoản nhân viên " + maNhanVien);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setAccountActiveByEmployeeId(String maNhanVien, boolean active) {
        if (!KiemSoatTruyCap.isAdministrator()) return false;
        String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maNhanVien = ? AND LOWER(tenDN) <> 'admin' AND UPPER(LTRIM(RTRIM(ISNULL(quyen,'')))) NOT IN ('ADMIN','QUAN_LY')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setString(2, maNhanVien);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event(active ? "UNLOCK" : "LOCK", "Tài khoản nhân viên " + maNhanVien);
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isProtectedAdministrator(Connection conn, String username) throws SQLException {
        String sql = "SELECT quyen FROM TaiKhoan WHERE tenDN = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && KiemSoatTruyCap.isProtectedAdministrator(username, rs.getString("quyen"));
            }
        }
    }
    
    public boolean updatePassword(String tenDN, String newMatKhau) {
        if (tenDN == null || tenDN.trim().isEmpty() || newMatKhau == null || newMatKhau.trim().isEmpty()
                || (!KiemSoatTruyCap.isAdministrator() && !KiemSoatTruyCap.isCurrentAccount(tenDN))) return false;
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newMatKhau);
            stmt.setString(2, tenDN);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) NhatKyKiemToan.event("UPDATE", "Đổi mật khẩu tài khoản " + tenDN.trim());
            return updated;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String displayRole(String role) {
        if (role == null) return "Lễ tân";
        String value = role.trim().toUpperCase();
        if (value.equals("ADMIN")) return "Admin";
        if (value.equals("QUAN_LY") || role.contains("Quản lý")) return "Quản lý";
        if (value.equals("NHA_SI") || role.contains("Nha sĩ") || role.contains("Bác sĩ")) return "Nha sĩ";
        if (value.equals("Y_TA") || role.contains("Y tá") || role.contains("Điều dưỡng")) return "Y tá";
        return "Lễ tân";
    }

}

