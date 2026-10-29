package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import models.entity.Account;
import models.entity.Role;
import sql.DBConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AuthController {

    private static Account currentUser;
    private static final String ACCOUNT_SELECT =
            "SELECT tk.tenDN, tk.matKhau, tk.quyen, nv.tenNhanVien " +
            "FROM TaiKhoan tk LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien ";

    public boolean login(String username, String password) throws SQLException {
        currentUser = null;
        if (username == null || password == null || username.trim().isEmpty() || password.isEmpty()) {
            return false;
        }

        String sql = ACCOUNT_SELECT + "WHERE tk.tenDN = ? AND tk.matKhau = ? AND tk.trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;
                currentUser = mapAccount(rs);
                return true;
            }
        }
    }

    public boolean loginWithQr(String qrPayload) throws SQLException {
        currentUser = null;
        if (!isLoginQrPayload(qrPayload)) return false;

        String sql = ACCOUNT_SELECT + "WHERE tk.qrPayload = ? AND tk.trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, qrPayload.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;
                currentUser = mapAccount(rs);
                return true;
            }
        }
    }

    public byte[] regenerateCurrentUserLoginQr() throws SQLException, WriterException, IOException {
        Account user = currentUser;
        if (user == null) {
            throw new IllegalStateException("Cần đăng nhập trước khi tạo mã QR.");
        }

        String payload = "CLINIC|" + UUID.randomUUID();
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 240, 240);
        ByteArrayOutputStream image = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", image);

        String sql = "UPDATE TaiKhoan SET qrPayload = ?, qrImage = ? " +
                     "WHERE tenDN = ? AND trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, payload);
            stmt.setBytes(2, image.toByteArray());
            stmt.setString(3, user.getUsername());
            if (stmt.executeUpdate() == 0) return null;
        }
        return image.toByteArray();
    }

    private boolean isLoginQrPayload(String qrPayload) {
        if (qrPayload == null) return false;
        String payload = qrPayload.trim();
        if (!payload.startsWith("CLINIC|")) return false;
        try {
            UUID.fromString(payload.substring("CLINIC|".length()));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        String username = rs.getString("tenDN");
        String fullName = rs.getString("tenNhanVien");
        if (fullName == null || fullName.trim().isEmpty()) fullName = username;
        return new Account(username, rs.getString("matKhau"), fullName,
                Role.fromString(rs.getString("quyen")));
    }

    public static Account getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Account user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}