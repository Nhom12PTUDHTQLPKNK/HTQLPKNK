package controllers;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import models.entity.TaiKhoanThongTin;
import models.entity.VaiTro;
import sql.DBConnection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.*;

/** QR-code generation, decoding and lookup operations. */
public final class DieuKhienMaQR {
    private DieuKhienMaQR() { }
public static String buildLoginQrPayload(String username, String fullName, VaiTro role) {
        if (username == null || username.trim().isEmpty()) return null;
        String safeUser = username.trim();
        String safeName = fullName == null || fullName.trim().isEmpty() ? safeUser : fullName.trim();
        String safeRole = role == null ? "" : role.name();
        return "CLINIC|" + safeUser + "|" + safeRole + "|" + safeName;
    }

public static TaiKhoanThongTin authenticateQr(String qrPayload) {
        if (qrPayload == null || qrPayload.trim().isEmpty()) return null;
        String payload = qrPayload.trim();
        String sql = "SELECT tk.tenDN, tk.matKhau, tk.quyen, nv.tenNhanVien " +
                     "FROM TaiKhoan tk LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien " +
                     "WHERE tk.qrPayload = ? AND tk.trangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payload);
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

        String[] parts = payload.split("\\|");
        if (parts.length >= 2 && "CLINIC".equalsIgnoreCase(parts[0])) {
            String username = parts[1].trim();
            String sqlByUser = "SELECT tk.tenDN, tk.matKhau, tk.quyen, nv.tenNhanVien " +
                    "FROM TaiKhoan tk LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien " +
                    "WHERE tk.tenDN = ? AND tk.trangThai = 1";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlByUser)) {
                ps.setString(1, username);
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
        }
        return null;
    }

public static byte[] generateAndStoreQrForUser(String username, String fullName, VaiTro role) {
        if (username == null || username.trim().isEmpty()) return null;
        String payload = buildLoginQrPayload(username, fullName, role);
        if (payload == null) return null;
        byte[] image = generateQrCodeBytes(payload);
        if (image == null) return null;
        if (!saveQrData(username.trim(), payload, image)) {
            return null;
        }
        return image;
    }

public static byte[] generateQrCodeBytes(String payload) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 240, 240);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

public static String decodeQrFile(File file) {
        if (file == null || !file.exists()) return null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) return null;
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>());
            return result == null ? null : result.getText();
        } catch (Exception e) {
            return null;
        }
    }

public static byte[] generateQrCode(String data) {
        return generateQrCodeBytes(data);
    }

public static String decodeQrCode(File file) {
        return decodeQrFile(file);
    }

public static boolean saveInventoryQrData(String tableName, String itemId, String itemName, String itemType) {
        if (itemId == null || itemId.trim().isEmpty()) return false;
        String payload = "MATERIAL|" + (itemType == null ? "" : itemType.trim()) + "|" + itemId.trim() + "|" + (itemName == null ? itemId.trim() : itemName.trim());
        byte[] image = generateQrCodeBytes(payload);
        if (image == null) return false;
        String sql = "UPDATE " + tableName + " SET qrPayload = ?, qrImage = ? WHERE " + ("Thuoc".equalsIgnoreCase(tableName) ? "maThuoc" : "maVatTu") + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, payload);
            ps.setBytes(2, image);
            ps.setString(3, itemId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public static String findMaterialByQr(String qrPayload) {
        if (qrPayload == null || qrPayload.trim().isEmpty()) return null;
        String[] parts = qrPayload.trim().split("\\|");
        if (parts.length < 4 || !"MATERIAL".equalsIgnoreCase(parts[0])) return null;
        String type = parts[1] == null ? "" : parts[1].trim();
        String id = parts[2] == null ? "" : parts[2].trim();
        if (id.isEmpty()) return null;

        String table = "VAT".equalsIgnoreCase(type) || "VATTU".equalsIgnoreCase(type) ? "VatTu" : "Thuoc";
        String idColumn = "Thuoc".equalsIgnoreCase(table) ? "maThuoc" : "maVatTu";
        String sql = "SELECT " + idColumn + " FROM " + table + " WHERE qrPayload = ? OR " + idColumn + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qrPayload.trim());
            ps.setString(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fallback: nếu QR không có type chính xác hoặc chưa lưu qrPayload, vẫn tìm theo mã vật tư trong cả 2 bảng.
        String fallbackSql = "SELECT maThuoc AS id FROM Thuoc WHERE maThuoc = ? UNION ALL SELECT maVatTu AS id FROM VatTu WHERE maVatTu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(fallbackSql)) {
            ps.setString(1, id);
            ps.setString(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public static String getQrPayloadForInventory(String tableName, String itemId) {
        String column = "Thuoc".equalsIgnoreCase(tableName) ? "maThuoc" : "maVatTu";
        String sql = "SELECT qrPayload FROM " + tableName + " WHERE " + column + " = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("qrPayload");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public static byte[] getInventoryQrImage(String tableName, String itemId) {
        String column = "Thuoc".equalsIgnoreCase(tableName) ? "maThuoc" : "maVatTu";
        String sql = "SELECT qrImage FROM " + tableName + " WHERE " + column + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] image = rs.getBytes("qrImage");
                    if (image != null && image.length > 0) return image;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public static String getQrPayload(String username) {
        String sql = "SELECT qrPayload FROM TaiKhoan WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("qrPayload");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public static byte[] getQrImageBytes(String username) {
        String sql = "SELECT qrImage FROM TaiKhoan WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Blob blob = rs.getBlob("qrImage");
                    if (blob != null) {
                        byte[] data = blob.getBytes(1, (int) blob.length());
                        blob.free();
                        return data;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

public static boolean saveQrData(String username, String qrPayload, byte[] qrImage) {
        String sql = "UPDATE TaiKhoan SET qrPayload = ?, qrImage = ? WHERE tenDN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, qrPayload);
            if (qrImage != null) ps.setBytes(2, qrImage);
            else ps.setNull(2, java.sql.Types.VARBINARY);
            ps.setString(3, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
