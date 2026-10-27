package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class ChangePasswordPanel extends JPanel {
    private Image bgDangNhapImage;

    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(110, 128, 156);

    private JPasswordField txtOldPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    public ChangePasswordPanel() {
        // Tải ảnh nền tổng từ package img
        bgDangNhapImage = loadImageSafe("/img/bgmatkhau.png");
        if (bgDangNhapImage == null) {
            bgDangNhapImage = loadImageSafe("/img/bgmatkhau.jpg");
        }

        setLayout(new GridBagLayout());
        setBackground(new Color(243, 248, 253));

        // Thẻ card tổng kích thước lớn
        JPanel mainCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        mainCard.setOpaque(false);
        mainCard.setPreferredSize(new Dimension(1160, 640));

        // =========================================================================
        // CỘT BÊN TRÁI
        // =========================================================================
        JPanel leftBanner = new JPanel() {
            private final Image bgImage = loadBannerBg();

            private Image loadBannerBg() {
                Image img = loadImageSafe("/img/bgtrai.png");
                if (img == null) img = loadImageSafe("/img/bgtrai.jpg");
                return img;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (bgImage != null) {
                    Shape clipShape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24);
                    g2.setClip(clipShape);
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2.setColor(new Color(17, 34, 68));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

                    g2.setColor(new Color(254, 225, 238));
                    Path2D wave = new Path2D.Double();
                    wave.moveTo(0, getHeight() - 60);
                    wave.curveTo(getWidth() * 0.35, getHeight() - 100, getWidth() * 0.65, getHeight() - 10, getWidth(), getHeight() - 40);
                    wave.lineTo(getWidth(), getHeight());
                    wave.lineTo(0, getHeight());
                    wave.closePath();
                    g2.fill(wave);
                }
                g2.dispose();
            }
        };
        leftBanner.setOpaque(false);
        leftBanner.setPreferredSize(new Dimension(420, 0));
        leftBanner.setLayout(new BoxLayout(leftBanner, BoxLayout.Y_AXIS));
        leftBanner.setBorder(new EmptyBorder(50, 40, 30, 40));

        // 1. Logo & Tên hệ thống (Dùng file logo1.png và giữ nguyên vẹn ảnh gốc sắc nét)
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel lblLogoIcon = createLogoLabel("/img/logo1.png", 64);

        JPanel logoTextPanel = new JPanel();
        logoTextPanel.setOpaque(false);
        logoTextPanel.setLayout(new BoxLayout(logoTextPanel, BoxLayout.Y_AXIS));
        
        JLabel lblBrand = new JLabel("SMILE DENTAL");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(Color.WHITE);
        
        JLabel lblSubBrand = new JLabel("MANAGEMENT SYSTEM");
        lblSubBrand.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubBrand.setForeground(new Color(160, 180, 210));

        logoTextPanel.add(lblBrand);
        logoTextPanel.add(Box.createVerticalStrut(4));
        logoTextPanel.add(lblSubBrand);
        
        logoPanel.add(lblLogoIcon);
        logoPanel.add(logoTextPanel);

        leftBanner.add(logoPanel);
        leftBanner.add(Box.createVerticalStrut(45));

        // 2. Nút "Quay lại"
        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnBack.setForeground(new Color(180, 200, 225));
        btnBack.setFocusPainted(false);
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnBack.addActionListener(e -> {
            // TODO: Code chuyển màn hình
        });
        leftBanner.add(btnBack);
        leftBanner.add(Box.createVerticalStrut(35));

        // 3. Tiêu đề mục tài khoản trong banner trái
        JPanel bannerTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bannerTitlePanel.setOpaque(false);
        bannerTitlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bannerTitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel bannerLockBg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 225, 238));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bannerLockBg.setOpaque(false);
        bannerLockBg.setPreferredSize(new Dimension(52, 52));
        JLabel lblBannerLockIcon = new JLabel("🔒");
        lblBannerLockIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblBannerLockIcon.setForeground(PINK_ACCENT);
        bannerLockBg.add(lblBannerLockIcon);

        JPanel bannerTitleText = new JPanel();
        bannerTitleText.setOpaque(false);
        bannerTitleText.setLayout(new BoxLayout(bannerTitleText, BoxLayout.Y_AXIS));

        JLabel lblBannerTitle = new JLabel("Đổi Mật Khẩu");
        lblBannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBannerTitle.setForeground(Color.WHITE);

        JLabel lblBannerSub = new JLabel("Bảo mật tài khoản của bạn");
        lblBannerSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBannerSub.setForeground(new Color(160, 180, 210));

        bannerTitleText.add(lblBannerTitle);
        bannerTitleText.add(Box.createVerticalStrut(3));
        bannerTitleText.add(lblBannerSub);

        bannerTitlePanel.add(bannerLockBg);
        bannerTitlePanel.add(bannerTitleText);

        leftBanner.add(bannerTitlePanel);
        leftBanner.add(Box.createVerticalStrut(45));

        // 4. Đoạn mô tả bảo mật
        JPanel tipBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        tipBox.setOpaque(false);
        tipBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        tipBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel lblShieldIcon = new JLabel("🛡️");
        lblShieldIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblShieldIcon.setForeground(new Color(160, 180, 210));

        JTextArea txtTipDesc = new JTextArea("Mật khẩu mạnh giúp bảo vệ thông tin cá nhân và dữ liệu quan trọng của bạn!");
        txtTipDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTipDesc.setForeground(new Color(200, 215, 235));
        txtTipDesc.setOpaque(false);
        txtTipDesc.setLineWrap(true);
        txtTipDesc.setWrapStyleWord(true);
        txtTipDesc.setEditable(false);
        txtTipDesc.setPreferredSize(new Dimension(280, 70));

        tipBox.add(lblShieldIcon);
        tipBox.add(txtTipDesc);

        leftBanner.add(tipBox);
        leftBanner.add(Box.createVerticalGlue());

        // 5. Hình minh họa chiếc răng ở đáy trái
        JLabel lblToothArt = new JLabel("🦷✨", SwingConstants.CENTER);
        lblToothArt.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        lblToothArt.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftBanner.add(lblToothArt);

        mainCard.add(leftBanner, BorderLayout.WEST);

        // =========================================================================
        // CỘT BÊN PHẢI (Form Nhập Liệu)
        // =========================================================================
        JPanel rightForm = new JPanel();
        rightForm.setOpaque(false);
        rightForm.setLayout(new BoxLayout(rightForm, BoxLayout.Y_AXIS));
        rightForm.setBorder(new EmptyBorder(50, 50, 20, 50));

        // Header Tiêu đề form
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        headerLeft.setOpaque(false);

        JPanel lockIconBg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 225, 238));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lockIconBg.setOpaque(false);
        lockIconBg.setPreferredSize(new Dimension(58, 58));
        JLabel lblMainLock = new JLabel("🔒");
        lblMainLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        lblMainLock.setForeground(PINK_ACCENT);
        lockIconBg.add(lblMainLock);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

        JLabel lblFormHeader = new JLabel("Đổi Mật Khẩu");
        lblFormHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblFormHeader.setForeground(TEXT_DARK);

        JLabel lblFormSub = new JLabel("Vui lòng nhập mật khẩu hiện tại và mật khẩu mới để thay đổi.");
        lblFormSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblFormSub.setForeground(MUTED);

        headerText.add(lblFormHeader);
        headerText.add(Box.createVerticalStrut(5));
        headerText.add(lblFormSub);

        headerLeft.add(lockIconBg);
        headerLeft.add(headerText);

        headerRow.add(headerLeft, BorderLayout.WEST);
        rightForm.add(headerRow);
        rightForm.add(Box.createVerticalStrut(35));

        // Các trường nhập mật khẩu
        rightForm.add(createFieldLabel("Mật khẩu hiện tại *"));
        rightForm.add(Box.createVerticalStrut(8));
        txtOldPass = createPasswordField();
        rightForm.add(createInputContainer(txtOldPass, "Nhập mật khẩu hiện tại"));
        rightForm.add(Box.createVerticalStrut(20));

        rightForm.add(createFieldLabel("Mật khẩu mới *"));
        rightForm.add(Box.createVerticalStrut(8));
        txtNewPass = createPasswordField();
        rightForm.add(createInputContainer(txtNewPass, "Nhập mật khẩu mới"));
        rightForm.add(Box.createVerticalStrut(20));

        rightForm.add(createFieldLabel("Xác nhận mật khẩu mới *"));
        rightForm.add(Box.createVerticalStrut(8));
        txtConfirmPass = createPasswordField();
        rightForm.add(createInputContainer(txtConfirmPass, "Nhập lại mật khẩu mới"));
        rightForm.add(Box.createVerticalStrut(30));

        // Nút hành động
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        btnBox.setOpaque(false);
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnCancel = createOutlineButton("Hủy", MUTED);
        JButton btnSave = createGradientButton("Lưu Thay Đổi", PINK_ACCENT);

        btnCancel.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> performChangePassword());

        btnBox.add(btnCancel);
        btnBox.add(btnSave);

        rightForm.add(btnBox);

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.add(rightForm, BorderLayout.CENTER);

        // Chú thích dưới cùng bên phải có đường kẻ ngang ở 2 bên
        JPanel bottomNotePanel = new JPanel(new GridBagLayout());
        bottomNotePanel.setOpaque(false);
        bottomNotePanel.setBorder(new EmptyBorder(0, 35, 20, 35));

        GridBagConstraints gbcNote = new GridBagConstraints();
        gbcNote.gridy = 0;
        gbcNote.anchor = GridBagConstraints.CENTER;

        JPanel lineLeft = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_LIGHT);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        lineLeft.setOpaque(false);
        gbcNote.gridx = 0;
        gbcNote.weightx = 1.0;
        gbcNote.fill = GridBagConstraints.HORIZONTAL;
        bottomNotePanel.add(lineLeft, gbcNote);

        JPanel noteContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        noteContent.setOpaque(false);
        JLabel lblNoteIcon = new JLabel("🛡️");
        lblNoteIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        JLabel lblNoteText = new JLabel("Chỉ bạn mới có thể thay đổi mật khẩu của mình");
        lblNoteText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoteText.setForeground(MUTED);
        noteContent.add(lblNoteIcon);
        noteContent.add(lblNoteText);

        gbcNote.gridx = 1;
        gbcNote.weightx = 0.0;
        gbcNote.fill = GridBagConstraints.NONE;
        gbcNote.insets = new Insets(0, 15, 0, 15);
        bottomNotePanel.add(noteContent, gbcNote);

        JPanel lineRight = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_LIGHT);
                g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        lineRight.setOpaque(false);
        gbcNote.gridx = 2;
        gbcNote.weightx = 1.0;
        gbcNote.fill = GridBagConstraints.HORIZONTAL;
        gbcNote.insets = new Insets(0, 0, 0, 0);
        bottomNotePanel.add(lineRight, gbcNote);
        
        rightWrapper.add(bottomNotePanel, BorderLayout.SOUTH);
        mainCard.add(rightWrapper, BorderLayout.CENTER);

        add(mainCard, new GridBagConstraints());
    }

    private Image loadImageSafe(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            // Bỏ qua nếu lỗi
        }
        return null;
    }

    // Nạp ảnh logo1 trực tiếp sắc nét nguyên bản, không dùng bộ lọc để tránh mất nét
    private JLabel createLogoLabel(String path, int size) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(img));
            }
        } catch (Exception e) {
            // Fallback
        }
        JLabel lbl = new JLabel("🦷");
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        lbl.setForeground(PINK_ACCENT);
        return lbl;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgDangNhapImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(bgDangNhapImage, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pwd = new JPasswordField();
        pwd.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pwd.setBorder(null);
        pwd.setOpaque(false);
        return pwd;
    }

    private JPanel createInputContainer(JPasswordField pwdField, String placeholder) {
        JPanel container = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        container.setPreferredSize(new Dimension(540, 52));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lblFieldLock = new JLabel("🔒");
        lblFieldLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblFieldLock.setForeground(MUTED);
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 16, 0, 12);
        container.add(lblFieldLock, gbc);

        pwdField.setEchoChar((char) 0);
        pwdField.setForeground(MUTED);
        pwdField.setText(placeholder);

        pwdField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(pwdField.getPassword()).equals(placeholder)) {
                    pwdField.setText("");
                    pwdField.setEchoChar('•');
                    pwdField.setForeground(TEXT_DARK);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (String.valueOf(pwdField.getPassword()).isEmpty()) {
                    pwdField.setText(placeholder);
                    pwdField.setEchoChar((char) 0);
                    pwdField.setForeground(MUTED);
                }
            }
        });

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        container.add(pwdField, gbc);

        JToggleButton btnEye = new JToggleButton("👁️");
        btnEye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btnEye.setFocusPainted(false);
        btnEye.setBorderPainted(false);
        btnEye.setContentAreaFilled(false);
        btnEye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnEye.addActionListener(e -> {
            String currentText = String.valueOf(pwdField.getPassword());
            if (!currentText.equals(placeholder)) {
                if (btnEye.isSelected()) {
                    pwdField.setEchoChar((char) 0);
                } else {
                    pwdField.setEchoChar('•');
                }
            }
        });

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 8, 0, 14);
        container.add(btnEye, gbc);

        return container;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(color);
        btn.setPreferredSize(new Dimension(150, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createGradientButton(String text, Color pinkColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, pinkColor, getWidth(), getHeight(), new Color(255, 120, 160));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(180, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void clearForm() {
        txtOldPass.setText("");
        txtNewPass.setText("");
        txtConfirmPass.setText("");
    }

    private void performChangePassword() {
        String oldPass = new String(txtOldPass.getPassword()).trim();
        String newPass = new String(txtNewPass.getPassword()).trim();
        String confirmPass = new String(txtConfirmPass.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty() || oldPass.contains("Nhập")) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tất cả các trường!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!", "Lỗi xác nhận", JOptionPane.ERROR_MESSAGE);
            txtConfirmPass.requestFocusInWindow();
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", "Mật khẩu yếu", JOptionPane.WARNING_MESSAGE);
            txtNewPass.requestFocusInWindow();
            return;
        }

        JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }
}