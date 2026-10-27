package qrcode;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;

public final class LoginQrScannerDialog extends JDialog {

    private final Webcam webcam;
    private final SwingWorker<String, Void> scanWorker;
    private String scannedPayload;

    private LoginQrScannerDialog(Window owner) {
        super(owner, "Đăng nhập bằng mã QR", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IllegalStateException("Không tìm thấy webcam trên thiết bị.");
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());

        JLabel instruction = new JLabel("Đưa mã QR đăng nhập vào trước camera", SwingConstants.CENTER);
        instruction.setFont(new Font("Segoe UI", Font.BOLD, 15));
        instruction.setBorder(new EmptyBorder(12, 8, 12, 8));

        WebcamPanel webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setMirrored(true);

        JLabel status = new JLabel("Đang mở camera...", SwingConstants.CENTER);
        status.setBorder(new EmptyBorder(8, 8, 12, 8));

        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(event -> dispose());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(0, 12, 12, 12));
        footer.add(status, BorderLayout.CENTER);
        footer.add(cancelButton, BorderLayout.EAST);

        add(instruction, BorderLayout.NORTH);
        add(webcamPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
        setSize(680, 560);
        setLocationRelativeTo(owner);

        scanWorker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                long openDeadline = System.currentTimeMillis() + 15000;
                while (!isCancelled() && !webcam.isOpen()) {
                    if (System.currentTimeMillis() >= openDeadline) {
                        throw new IllegalStateException("Không thể mở webcam.");
                    }
                    Thread.sleep(100);
                }

                MultiFormatReader reader = new MultiFormatReader();
                while (!isCancelled()) {
                    BufferedImage image = webcam.getImage();
                    if (image == null) {
                        Thread.sleep(100);
                        continue;
                    }

                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    try {
                        Result result = reader.decode(bitmap);
                        return result.getText();
                    } catch (NotFoundException ignored) {
                        // Keep scanning until a QR code is visible.
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    scannedPayload = get();
                    if (scannedPayload != null) dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    status.setText("Quá trình quét bị gián đoạn.");
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause == null ? e.getMessage() : cause.getMessage();
                    JOptionPane.showMessageDialog(LoginQrScannerDialog.this,
                            "Không thể quét mã QR: " + message,
                            "Lỗi webcam", JOptionPane.ERROR_MESSAGE);
                    dispose();
                }
            }
        };

        scanWorker.addPropertyChangeListener(event -> {
            if ("state".equals(event.getPropertyName())
                    && SwingWorker.StateValue.STARTED.equals(event.getNewValue())) {
                status.setText("Đang quét mã QR...");
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                scanWorker.cancel(true);
                if (webcam.isOpen()) webcam.close();
            }
        });
    }

    public static String scanForLoginQr(Window owner) {
        LoginQrScannerDialog dialog = new LoginQrScannerDialog(owner);
        dialog.scanWorker.execute();
        dialog.setVisible(true);
        return dialog.scannedPayload;
    }
}
