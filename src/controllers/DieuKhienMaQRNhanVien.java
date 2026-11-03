package controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import sql.DBConnection;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/** Creates login QR codes for employee identity cards. */
public final class DieuKhienMaQRNhanVien {
    private DieuKhienMaQRNhanVien() { }

    public static BufferedImage createLoginQrImage(String employeeId) throws Exception {
        String payload;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT qrPayload, trangThai FROM TaiKhoan WHERE maNhanVien = ?")) {
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new SQLException("Nhân viên này chưa được cấp tài khoản đăng nhập.");
                if (rs.getInt("trangThai") != 1) throw new SQLException("Tài khoản nhân viên đang bị khóa.");
                payload = rs.getString("qrPayload");
            }
        }

        if (!isLoginQrPayload(payload)) {
            payload = "CLINIC|" + UUID.randomUUID();
            BitMatrix storedQr = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 220, 220);
            ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(storedQr, "PNG", imageBytes);
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE TaiKhoan SET qrPayload = ?, qrImage = ? WHERE maNhanVien = ? AND trangThai = 1")) {
                stmt.setString(1, payload);
                stmt.setBytes(2, imageBytes.toByteArray());
                stmt.setString(3, employeeId);
                if (stmt.executeUpdate() == 0) {
                    throw new SQLException("Không thể gắn mã đăng nhập QR vào tài khoản nhân viên.");
                }
            }
        }

        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 220, 220);
        BufferedImage image = new BufferedImage(220, 220, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 220; y++) {
            for (int x = 0; x < 220; x++) {
                image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    private static boolean isLoginQrPayload(String value) {
        if (value == null || !value.startsWith("CLINIC|")) return false;
        try {
            UUID.fromString(value.substring("CLINIC|".length()));
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
