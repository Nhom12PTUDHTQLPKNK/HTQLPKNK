package views;

import controllers.AuthController;
import qrcode.LoginQrScannerDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.SQLException;
import javax.imageio.ImageIO;

public class LoginFrame extends JFrame {

    private static final Color NAVY = new Color(15, 34, 80);
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color PINK = new Color(239, 63, 131);
    private static final Color GRAY = new Color(105, 128, 157);
    private static final Color PLACEHOLDER = new Color(150, 150, 150);
    private static final Color BORDER = new Color(210, 222, 236);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkRemember;

    private JButton btnEye;
    private boolean passwordVisible = false;

    // Khởi tạo AuthController để kết nối xác thực dữ liệu CSDL
    private AuthController authController = new AuthController();

    public LoginFrame() {
        setTitle("Đăng nhập - SmileDental");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1648, 920);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setFocusableWindowState(true);

        // background
        JPanel background = new JPanel() {
            private BufferedImage image;

            {
                try (InputStream in = getClass().getResourceAsStream("/img/background.jpg")) {
                    if (in != null) {
                        image = ImageIO.read(in);
                    } else {
                        System.err.println("Không tìm thấy /img/background.jpg");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                } else {
                    g.setColor(new Color(230, 244, 255));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        background.setLayout(new GridBagLayout());
        setContentPane(background);

        // content trái
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));

        JLabel title1 = new JLabel("Chăm sóc nụ cười,") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.setFont(getFont());
                g2.drawString(getText(), 1, getFontMetrics(getFont()).getAscent() + 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        title1.setFont(new Font("Segoe UI", Font.BOLD, 65));
        title1.setForeground(NAVY);
        title1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title2 = new JLabel("quản lý tận tâm");
        title2.setFont(new Font("Segoe UI", Font.BOLD, 58));
        title2.setForeground(BLUE);
        title2.setAlignmentX(Component.LEFT_ALIGNMENT);

        intro.add(title1);
        intro.add(Box.createVerticalStrut(0));
        intro.add(title2);
        intro.add(Box.createVerticalStrut(12));

        // thẻ card bên phải
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(255, 255, 255, 190));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 28, 28);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 42, 22, 42));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 700));

        // logo
        JLabel logo;
        Icon logoIcon = loadIcon("/img/logo1.png", 80, 80);
        if (logoIcon != null) {
            logo = new JLabel(logoIcon);
        } else {
            logo = new JLabel("SD", SwingConstants.CENTER);
            logo.setFont(new Font("Segoe UI", Font.BOLD, 40));
            logo.setForeground(PINK);
        }

        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setMaximumSize(new Dimension(390, 80));
        logo.setPreferredSize(new Dimension(390, 80));

        // tên nha khoa
        JLabel brand = new JLabel("<html><b><span style='color:#007EFF'>SMILE</span><span style='color:#EF3F83'>DENTAL</span></b></html>", SwingConstants.CENTER);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 42));
        brand.setForeground(NAVY);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.setMaximumSize(new Dimension(390, 48));

        JLabel tagline = new JLabel("HỆ THỐNG QUẢN LÝ PHÒNG KHÁM NHA KHOA", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(GRAY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setMaximumSize(new Dimension(390, 25));

        // Thanh màu hồng
        JPanel divider = new JPanel();
        divider.setBackground(PINK);
        divider.setPreferredSize(new Dimension(110, 4));
        divider.setMinimumSize(new Dimension(110, 4));
        divider.setMaximumSize(new Dimension(110, 4));

        JPanel dividerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        dividerWrapper.setOpaque(false);
        dividerWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        dividerWrapper.setMaximumSize(new Dimension(390, 4));
        dividerWrapper.add(divider);

        // tiêu đề đăng nhập
        JLabel loginTitle = new JLabel("Đăng nhập hệ thống", SwingConstants.LEFT);
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        loginTitle.setForeground(NAVY);
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginTitle.setMaximumSize(new Dimension(390, 35));
        loginTitle.setPreferredSize(new Dimension(390, 35));

        // ô đăng nhập
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtUsername.setForeground(new Color(130, 140, 150));
        txtUsername.setBackground(Color.WHITE);
        txtUsername.setBorder(new EmptyBorder(10, 8, 10, 10));
        txtUsername.setText("Tên đăng nhập");

        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtUsername.getText().equals("Tên đăng nhập")) {
                    txtUsername.setText("");
                    txtUsername.setForeground(NAVY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtUsername.getText().trim().isEmpty()) {
                    txtUsername.setText("Tên đăng nhập");
                    txtUsername.setForeground(PLACEHOLDER);
                }
            }
        });

        JPanel usernamePanel = new JPanel(new BorderLayout());
        usernamePanel.setBackground(Color.WHITE);
        usernamePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(0, 4, 0, 4)
        ));
        usernamePanel.setPreferredSize(new Dimension(390, 52));
        usernamePanel.setMinimumSize(new Dimension(390, 52));
        usernamePanel.setMaximumSize(new Dimension(390, 52));
        usernamePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernamePanel.add(createIconLabel("/img/dangnhap.png"), BorderLayout.WEST);
        usernamePanel.add(txtUsername, BorderLayout.CENTER);

        // mật khẩu
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.setForeground(PLACEHOLDER);
        txtPassword.setBackground(Color.WHITE);
        txtPassword.setBorder(new EmptyBorder(10, 8, 10, 5));
        txtPassword.setEchoChar((char) 0);
        txtPassword.setText("Mật khẩu");
        txtPassword.setPreferredSize(new Dimension(280, 50));

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String value = new String(txtPassword.getPassword());
                if (value.equals("Mật khẩu")) {
                    txtPassword.setText("");
                    txtPassword.setEchoChar('•');
                    txtPassword.setForeground(NAVY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtPassword.getPassword().length == 0) {
                    txtPassword.setEchoChar((char) 0);
                    txtPassword.setText("Mật khẩu");
                    txtPassword.setForeground(PLACEHOLDER);
                    passwordVisible = false;
                    updateEyeIcon();
                }
            }
        });

        // Nút icon con mắt
        btnEye = new JButton();
        btnEye.setPreferredSize(new Dimension(42, 42));
        btnEye.setMinimumSize(new Dimension(42, 42));
        btnEye.setMaximumSize(new Dimension(42, 42));
        btnEye.setBackground(Color.WHITE);
        btnEye.setBorderPainted(false);
        btnEye.setFocusPainted(false);
        btnEye.setContentAreaFilled(false);
        btnEye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        updateEyeIcon();

        btnEye.addActionListener(e -> {
            String value = new String(txtPassword.getPassword());
            if (value.equals("Mật khẩu")) {
                txtPassword.requestFocusInWindow();
                return;
            }

            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
            updateEyeIcon();
            txtPassword.requestFocusInWindow();
        });

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(0, 4, 0, 4)
        ));
        passwordPanel.setPreferredSize(new Dimension(390, 52));
        passwordPanel.setMinimumSize(new Dimension(390, 52));
        passwordPanel.setMaximumSize(new Dimension(390, 52));
        passwordPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordPanel.add(createIconLabel("/img/lockgrey.png"), BorderLayout.WEST);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        passwordPanel.add(btnEye, BorderLayout.EAST);

        // ghi nhớ đăng nhập
        chkRemember = new JCheckBox("Ghi nhớ đăng nhập");
        chkRemember.setOpaque(false);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkRemember.setForeground(NAVY);
        chkRemember.setFocusPainted(false);
        chkRemember.setAlignmentX(Component.CENTER_ALIGNMENT);
        chkRemember.setMaximumSize(new Dimension(390, 25));

        // nút đăng nhập
        JButton btnLogin = new JButton("   ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(BLUE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(true);
        btnLogin.setOpaque(true);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(390, 58));
        btnLogin.setMinimumSize(new Dimension(390, 58));
        btnLogin.setMaximumSize(new Dimension(390, 58));

        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(new Color(0, 105, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(BLUE);
            }
        });

        btnLogin.addActionListener(e -> login());

        JButton btnQrLogin = new JButton("ĐĂNG NHẬP BẰNG QR");
        btnQrLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnQrLogin.setForeground(BLUE);
        btnQrLogin.setBackground(Color.WHITE);
        btnQrLogin.setBorder(BorderFactory.createLineBorder(BLUE, 1));
        btnQrLogin.setFocusPainted(false);
        btnQrLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQrLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnQrLogin.setPreferredSize(new Dimension(390, 46));
        btnQrLogin.setMinimumSize(new Dimension(390, 46));
        btnQrLogin.setMaximumSize(new Dimension(390, 46));
        btnQrLogin.addActionListener(e -> loginWithQr());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(215, 228, 240));
        separator.setMaximumSize(new Dimension(390, 1));

        JLabel footer = new JLabel("Vui lòng đăng nhập bằng tài khoản nhân viên", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        footer.setForeground(GRAY);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setMaximumSize(new Dimension(390, 25));

        // sắp xếp các thành phần vào card
        card.add(logo);
        card.add(Box.createVerticalStrut(2));
        card.add(brand);
        card.add(Box.createVerticalStrut(5));
        card.add(tagline);
        card.add(Box.createVerticalStrut(18));
        card.add(dividerWrapper);
        card.add(Box.createVerticalStrut(28));
        card.add(loginTitle);
        card.add(Box.createVerticalStrut(14));
        card.add(usernamePanel);
        card.add(Box.createVerticalStrut(12));
        card.add(passwordPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(chkRemember);
        card.add(Box.createVerticalStrut(20));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(btnQrLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(separator);
        card.add(Box.createVerticalStrut(14));
        card.add(footer);

        // Bố trí GridBagConstraints
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(125, 55, 20, 25);
        background.add(intro, c);

        c.gridx = 1;
        c.weightx = 0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(20, 25, 20, 70);
        background.add(card, c);

        getRootPane().setDefaultButton(btnLogin);
        SwingUtilities.invokeLater(() -> {
            getRootPane().setFocusable(true);
            getRootPane().requestFocusInWindow();
        });
    }

    private Icon loadIcon(String path, int width, int height) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Không tìm thấy ảnh: " + path);
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JLabel createIconLabel(String path) {
        JLabel label = new JLabel();
        Icon icon = loadIcon(path, 22, 22);
        if (icon != null) {
            label.setIcon(icon);
        }
        label.setBorder(new EmptyBorder(0, 12, 0, 8));
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void updateEyeIcon() {
        if (btnEye == null) return;
        String path = passwordVisible ? "/img/eye.png" : "/img/eye.png";
        Icon icon = loadIcon(path, 22, 22);
        if (icon != null) {
            btnEye.setIcon(icon);
            btnEye.setText("");
        } else {
            btnEye.setIcon(null);
            btnEye.setText(passwordVisible ? "Ẩn" : "Hiện");
            btnEye.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        }
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.equals("Tên đăng nhập")) {
            username = "";
        }
        if (password.equals("Mật khẩu")) {
            password = "";
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (authController.login(username, password)) {
                completeLogin();
            } else {
                showInvalidCredentials();
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void loginWithQr() {
        try {
            String payload = LoginQrScannerDialog.scanForLoginQr(this);
            if (payload == null) return;
            if (authController.loginWithQr(payload)) {
                completeLogin();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Mã QR không hợp lệ hoặc tài khoản đã bị khóa.",
                        "Không thể đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        } catch (RuntimeException | LinkageError e) {
            String message = e.getMessage() == null ? "Không thể khởi động webcam." : e.getMessage();
            JOptionPane.showMessageDialog(this, message,
                    "Không thể sử dụng webcam", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeLogin() {
        JOptionPane.showMessageDialog(this, "Đăng nhập thành công!",
                "Smile Dental", JOptionPane.INFORMATION_MESSAGE);
        SwingUtilities.invokeLater(() -> new MenuFrame().setVisible(true));
        dispose();
    }

    private void showInvalidCredentials() {
        JOptionPane.showMessageDialog(this,
                "Tên đăng nhập hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa.",
                "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        txtPassword.requestFocusInWindow();
    }

    private void showDatabaseError(SQLException error) {
        System.err.println("Không thể xác thực tài khoản: " + error.getMessage());
        JOptionPane.showMessageDialog(this,
                "Không thể kết nối hoặc xác thực với cơ sở dữ liệu. Vui lòng kiểm tra kết nối rồi thử lại.",
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}