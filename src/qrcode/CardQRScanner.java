package qrcode;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class CardQRScanner extends JFrame implements Runnable {

    private Webcam webcam = null;
    private WebcamPanel panel = null;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isScanning = new AtomicBoolean(true);

    public CardQRScanner() {
        super("Quét Mã QR Trên Thẻ - Webcam Scanner");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. Lấy camera mặc định của máy tính
        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy Webcam trên máy tính!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // 2. Thiết lập độ phân giải camera (VGA 640x480 giúp máy chạy nhẹ và nhận diện nhanh)
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        // 3. Khởi tạo Panel hiển thị luồng Camera
        panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setMirrored(true); // Lật ảnh giống gương cho dễ đưa thẻ vào khung hình
        add(panel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 4. Bắt đầu luồng quét QR nền
        executor.execute(this);
    }

    @Override
    public void run() {
        MultiFormatReader reader = new MultiFormatReader();

        while (isScanning.get()) {
            try {
                Thread.sleep(80); // Tần số quét (~12 khung hình/giây)
            } catch (InterruptedException e) {
                break;
            }

            BufferedImage image = null;

            if (webcam.isOpen()) {
                if ((image = webcam.getImage()) == null) {
                    continue;
                }
            }

            if (image != null) {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                try {
                    // Đọc mã QR từ hình ảnh hiện tại
                    Result result = reader.decode(bitmap);

                    if (result != null) {
                        String qrContent = result.getText();
                        
                        // Tạm dừng quét khi đã tìm thấy mã
                        isScanning.set(false);

                        // Hiển thị kết quả lên giao diện
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Đã đọc thành công mã QR trên thẻ:\n\n" + qrContent,
                                    "Kết Quả Quét",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            // Tiếp tục quét sau khi đóng hộp thoại thông báo
                            isScanning.set(true);
                        });
                    }
                } catch (Exception e) {
                    // Không tìm thấy mã QR trong khung hình này -> Bỏ qua và quét khung tiếp theo
                }
            }
        }
    }

    public static void main(String[] args) {
        // Tắt kiểm tra cập nhật và cấu hình bộ nhớ đệm cho BridJ trên Java 21
        System.setProperty("bridj.quiet", "true");
        
        SwingUtilities.invokeLater(CardQRScanner::new);
    }
}