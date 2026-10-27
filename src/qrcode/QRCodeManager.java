package qrcode;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class QRCodeManager {

    public static void main(String[] args) {
        String qrContent = "https://example.com/product/12345";
        String filePath = "QRCode_Legacy.png";
        int width = 300;
        int height = 300;

        try {
            // 1. Tạo mã QR và lưu file
            generateQRCode(qrContent, filePath, width, height);
            System.out.println("1. Tạo mã QR thành công tại: " + filePath);

            // 2. Đọc (quét) nội dung từ mã QR
            String decodedContent = readQRCode(filePath);
            System.out.println("2. Nội dung quét từ QR: " + decodedContent);

            // 3. Hiển thị hình ảnh mã QR lên màn hình giao diện (Swing)
            displayQRCodeOnScreen(filePath, decodedContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tính năng 1: Tạo mã QR (Sử dụng Reflection)
     */
    public static void generateQRCode(String text, String filePath, int width, int height) throws Exception {
        Class<?> qrWriterClass = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
        Object qrWriter = qrWriterClass.getDeclaredConstructor().newInstance();

        Class<?> barcodeFormatClass = Class.forName("com.google.zxing.BarcodeFormat");
        Object qrFormat = Enum.valueOf((Class<Enum>) barcodeFormatClass, "QR_CODE");

        Method encodeMethod = qrWriterClass.getMethod("encode", String.class, barcodeFormatClass, int.class, int.class);
        Object bitMatrix = encodeMethod.invoke(qrWriter, text, qrFormat, width, height);

        File file = new File(filePath);
        Method writeToFileMethod = MatrixToImageWriter.class.getMethod("writeToFile", bitMatrix.getClass(), String.class, File.class);
        writeToFileMethod.invoke(null, bitMatrix, "PNG", file);
    }

    /**
     * Tính năng 2: Đọc mã QR (Sử dụng Reflection)
     */
    public static String readQRCode(String filePath) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(new File(filePath));
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);

        Class<?> binarizerClass = Class.forName("com.google.zxing.common.HybridBinarizer");
        Class<?> luminanceSourceClass = Class.forName("com.google.zxing.LuminanceSource");
        Constructor<?> binarizerConstructor = binarizerClass.getConstructor(luminanceSourceClass);
        Object binarizer = binarizerConstructor.newInstance(source);

        Class<?> binaryBitmapClass = Class.forName("com.google.zxing.BinaryBitmap");
        Class<?> binarizerBaseClass = Class.forName("com.google.zxing.Binarizer");
        Constructor<?> bitmapConstructor = binaryBitmapClass.getConstructor(binarizerBaseClass);
        Object bitmap = bitmapConstructor.newInstance(binarizer);

        Class<?> readerClass = Class.forName("com.google.zxing.MultiFormatReader");
        Object reader = readerClass.getDeclaredConstructor().newInstance();

        Method decodeMethod = readerClass.getMethod("decode", binaryBitmapClass);
        Object result = decodeMethod.invoke(reader, bitmap);

        Method getTextMethod = result.getClass().getMethod("getText");
        return (String) getTextMethod.invoke(result);
    }

    /**
     * Tính năng 3: Hiển thị hình ảnh mã QR lên cửa sổ giao diện màn hình
     */
    public static void displayQRCodeOnScreen(String filePath, String contentText) {
        JFrame frame = new JFrame("Mã QR Code");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 450);
        frame.setLocationRelativeTo(null); // Căn giữa màn hình

        // Nạp ảnh QR vào ImageIcon
        ImageIcon qrIcon = new ImageIcon(filePath);
        
        // Gắn ảnh và chữ vào JLabel
        JLabel label = new JLabel("Nội dung: " + contentText, qrIcon, SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setHorizontalTextPosition(SwingConstants.CENTER);

        frame.add(label);
        frame.setVisible(true);
    }
}