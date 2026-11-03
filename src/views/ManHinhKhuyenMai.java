
package views;

import controllers.TruongChonNgay;
import models.dao.KhuyenMaiDAO;
import models.entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ManHinhKhuyenMai extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(13, 102, 218);
    private static final Color BLUE_LIGHT = new Color(230, 240, 255);
    private static final Color GREEN_ACCENT = new Color(22, 130, 60);
    private static final Color GREEN_LIGHT = new Color(225, 247, 235);
    private static final Color RED_ACCENT = new Color(190, 38, 48);
    private static final Color RED_LIGHT = new Color(255, 232, 235);
    private static final Color BG_CANVAS = new Color(245, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(25, 42, 68);
    private static final Color TEXT_GRAY = new Color(105, 120, 140);
    private static final Color BORDER_LIGHT = new Color(218, 226, 238);

    private final KhuyenMaiDAO promotionDAO = new KhuyenMaiDAO();

    private final JTextField txtSearch = new JTextField();
    private final JTextField txtId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtDiscount = new JTextField();

    private final TruongChonNgay dateFrom = new TruongChonNgay();
    private final TruongChonNgay dateTo = new TruongChonNgay();

    private final JCheckBox chkActive = new JCheckBox("Đang áp dụng", true);

    private JPanel gridCardContainer;
    private JLabel lblTotalCount;
    private JLabel lblStatActiveCount, lblStatUpcomingCount, lblStatExpiredCount;

    private List<KhuyenMai> allPromotionsList;
    private boolean editing = false;

    public ManHinhKhuyenMai() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(createStatisticsPanel());
        topPanel.add(Box.createVerticalStrut(12));
        topPanel.add(createFilterPanel());

        add(topPanel, BorderLayout.NORTH);
        add(createCardGridArea(), BorderLayout.CENTER);

        clearForm();
        reload();
    }

    //  HEADER loang HỒNG - XANH 
    private JPanel createHeaderPanel() {
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                GradientPaint gradient = new GradientPaint( 0, 0, new Color(255, 220, 238),
                        getWidth(), 0, new Color(219, 234, 254)
                );

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };

        headerCard.setOpaque(false);
        headerCard.setLayout(new BorderLayout());
        headerCard.setBorder(new EmptyBorder(22, 28, 22, 28));
        headerCard.setPreferredSize(new Dimension(0, 110));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Quản Lý Chương Trình Khuyến Mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Thêm, cập nhật, xóa và theo dõi các chương trình khuyến mãi" );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        lblSubtitle.setForeground(TEXT_GRAY);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(7));
        titleBox.add(lblSubtitle);

     // chỗ chứa logo và tiêu đề
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titleContainer.setOpaque(false);

        // Logo bên trái
        JLabel lblLogo = new JLabel();
        ImageIcon logoIcon = loadImage("/img/logo5.png", 65, 65);

        if (logoIcon != null) {
            lblLogo.setIcon(logoIcon);
        }

        titleContainer.add(lblLogo);
        titleContainer.add(titleBox);

        // Thêm vào header
        headerCard.add(titleContainer, BorderLayout.CENTER);

        return headerCard;
    }

    //  CARD THỐNG KÊ 
    private JPanel createStatisticsPanel() {
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        statsRow.setPreferredSize(new Dimension(0, 130));

        JPanel activeCard = createStatCard(
                "Đang diễn ra", "0", BLUE_PRIMARY, new Color(232, 242, 255),"/img/ok.png"
        );

        JPanel upcomingCard = createStatCard(
                "Sắp diễn ra", "0", GREEN_ACCENT, new Color(238, 247, 238),"/img/yes.png"
        );

        JPanel expiredCard = createStatCard(
                "Đã kết thúc", "0", RED_ACCENT, new Color(254, 242, 242),"/img/no.png"
        );

        lblStatActiveCount = (JLabel) activeCard.getClientProperty("countLabel");
        lblStatUpcomingCount = (JLabel) upcomingCard.getClientProperty("countLabel");
        lblStatExpiredCount = (JLabel) expiredCard.getClientProperty("countLabel");

        statsRow.add(activeCard);
        statsRow.add(upcomingCard);
        statsRow.add(expiredCard);

        return statsRow;
    }

//  CARD THỐNG KÊ 
    private JPanel createStatCard( String title, String number, Color accent, Color bgCard,String imagePath) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Tô màu toàn bộ card
                g2.setColor(bgCard);
                g2.fillRoundRect(0, 0,getWidth(), getHeight(),18, 18 );

                // Viền card
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0,getWidth() - 1,getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        card.setPreferredSize(new Dimension(220, 130));
        card.setMinimumSize(new Dimension(220, 120));

        // ẢNH BÊN TRÁI
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icon = loadImage(imagePath, 55, 55);

        if (icon != null) {
            lblImage.setIcon(icon);
        } else {
            lblImage.setFont(new Font("Segoe UI", Font.BOLD, 35));
            lblImage.setForeground(accent);
        }

        JPanel imagePanel = new JPanel(new GridBagLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(80, 90));
        imagePanel.add(lblImage);

        // CHỮ VÀ SỐ LIỆU BÊN PHẢI
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblNumber.setForeground(accent);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(7));
        textPanel.add(lblNumber);
        textPanel.add(Box.createVerticalGlue());

        card.add(imagePanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.putClientProperty("countLabel", lblNumber);

        return card;
    }


    //TẠO CARD THỐNG KÊ 
    private JPanel createStatCard(
            String title,
            String number,
            Color accent,
            Color lightColor,
            String imagePath,
            boolean darkBackground) {

        // TheThongTin đang diễn ra: xanh dương
        //  sắp diễn ra: xanh lá
        //  đã kết thúc: đỏ nhạt
        Color cardBackground = darkBackground ? accent : lightColor;
        Color textColor = darkBackground ? Color.WHITE : accent;

        KhungVienBoGoc card = new KhungVienBoGoc(18, cardBackground);
        card.setLayout(new BorderLayout(0, 5));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setPreferredSize(new Dimension(220, 145));
        card.setMinimumSize(new Dimension(220, 145));

        // Ảnh được phóng lớn theo chiều ngang card
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon icon = loadImage(imagePath, 185, 65);

        if (icon != null) {
            lblImage.setIcon(icon);
        } else {
            lblImage.setText(title);
            lblImage.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblImage.setForeground(textColor);
        }

        imagePanel.add(lblImage, BorderLayout.CENTER);
        imagePanel.setPreferredSize(new Dimension(190, 65));

        // Khu vực tên và số lượng
        JPanel textPanel = new JPanel(new FlowLayout( FlowLayout.CENTER, 12, 0 ));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(textColor);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblNumber.setForeground(textColor);

        textPanel.add(lblTitle);
        textPanel.add(lblNumber);

        card.add(imagePanel, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.SOUTH);

        card.putClientProperty("countLabel", lblNumber);

        return card;
    }

    // đọc ảnh
    private ImageIcon loadImage(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);

            if (url == null) {
                System.out.println("Không tìm thấy ảnh: " + path);
                return null;
            }

            Image image = new ImageIcon(url).getImage()
                    .getScaledInstance(
                            width,
                            height,
                            Image.SCALE_SMOOTH
                    );

            return new ImageIcon(image);

        } catch (Exception e) {
            System.out.println("Lỗi tải ảnh: " + path);
            return null;
        }
    }

    //  FORM VÀ BỘ LỌC 
    private JPanel createFilterPanel() {
        KhungVienBoGoc panel = new KhungVienBoGoc(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel lblTitle = new JLabel(
                "Thông Tin Chi Tiết & Bộ Lọc Tìm Kiếm"
        );
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(TEXT_DARK);

        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1;

        // DÒNG 1
        gbc.gridy = 0;

        gbc.gridx = 0;
        gbc.weightx = 0.85;
        form.add(createInputGroup("Mã khuyến mãi", txtId), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.15;
        form.add(createInputGroup("Tên chương trình", txtName), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.75;
        form.add(createInputGroup("Mức giảm (%)", txtDiscount), gbc);

        // TÌM KIẾM NHANH
        gbc.gridx = 3;
        gbc.weightx = 1.25;

        JButton btnSearch = createButton(
                "Tìm kiếm", BLUE_PRIMARY, 115, 40);

        btnSearch.addActionListener(e -> applyFilter());

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setPreferredSize(new Dimension(130, 40));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 9, 5, 9)
        ));

        txtSearch.addActionListener(e -> applyFilter());

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnSearch, BorderLayout.EAST);

        JPanel searchWrapper = new JPanel(new BorderLayout(0, 6));
        searchWrapper.setOpaque(false);
        searchWrapper.add(
                createFieldLabel("Tìm kiếm nhanh (Mã/Tên)"),
                BorderLayout.NORTH
        );
        searchWrapper.add(searchRow, BorderLayout.CENTER);

        form.add(searchWrapper, gbc);

        // DÒNG 2
        gbc.gridy = 1;

        gbc.gridx = 0;
        gbc.weightx = 1.1;
        form.add(createInputGroup("Ngày bắt đầu", dateFrom), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.1;
        form.add(createInputGroup("Ngày kết thúc", dateTo), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.75;

        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);

        statusBox.add(
                createFieldLabel("Trạng thái"),
                BorderLayout.NORTH
        );

        chkActive.setOpaque(false);
        chkActive.setFont(new Font("Segoe UI", Font.BOLD, 14));

        statusBox.add(chkActive, BorderLayout.CENTER);
        form.add(statusBox, gbc);

        // CÁC NÚT CHỨC NĂNG
        gbc.gridx = 3;
        gbc.weightx = 1.25;

        JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 0));
        buttons.setOpaque(false);

        JButton btnClear = createButton(
                "Làm mới", new Color(100, 115, 130), 85, 38);

        JButton btnDelete = createButton(
                "Xóa", RED_ACCENT, 65, 38);

        JButton btnSave = createButton(
                "Lưu / Thêm", GREEN_ACCENT, 95, 38 );

        btnClear.addActionListener(e -> {
            clearForm();
            reload();
        });

        btnDelete.addActionListener(e -> deletePromotion());
        btnSave.addActionListener(e -> savePromotion());

        buttons.add(btnClear);
        buttons.add(btnDelete);
        buttons.add(btnSave);

        JPanel actionWrapper = new JPanel(new BorderLayout(0, 6));
        actionWrapper.setOpaque(false);
        actionWrapper.add(new JLabel(" "), BorderLayout.NORTH);
        actionWrapper.add(buttons, BorderLayout.CENTER);

        form.add(actionWrapper, gbc);

        panel.add(form, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputGroup(String label, JComponent comp) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        panel.add(createFieldLabel(label), BorderLayout.NORTH);

        if (comp instanceof JTextField
                && !(comp instanceof TruongChonNgay)) {

            JTextField tf = (JTextField) comp;

            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setPreferredSize(new Dimension(100, 40));

            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT),
                    new EmptyBorder(7, 10, 7, 10)
            ));
        }

        // Tăng kích thước ô ngày
        if (comp instanceof TruongChonNgay) {
            comp.setPreferredSize(new Dimension(180, 40));
            comp.setMinimumSize(new Dimension(150, 40));
        }

        panel.add(comp, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_DARK);
        return label;
    }

    // ds card Ở ĐÂY nè
    private JPanel createCardGridArea() {
        KhungVienBoGoc panel = new KhungVienBoGoc(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 15));
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel(
                "Danh Sách Chương Trình Khuyến Mãi"
        );
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblTitle.setForeground(TEXT_DARK);

        lblTotalCount = new JLabel("Tổng cộng: 0 chương trình");
        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalCount.setForeground(TEXT_GRAY);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblTotalCount, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        gridCardContainer = new JPanel(
                new GridLayout(0, 2, 20, 20)
        );
        gridCardContainer.setOpaque(false);
        gridCardContainer.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(gridCardContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // tạo CARD SALE 
    private JPanel createPromotionCard(KhuyenMai km) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(CARD_BG);
                g2.fillRoundRect( 0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect( 0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        card.setPreferredSize(new Dimension(350, 220));
        card.setMinimumSize(new Dimension(280, 210));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Mã và tên chương trình
        JPanel topBox = new JPanel(new BorderLayout(10, 0));
        topBox.setOpaque(false);

        JLabel lblIdName = new JLabel(
                "<html><b>" + km.getMaKhuyenMai()
                        + "</b> - " + km.getTenKhuyenMai() + "</html>"
        );
        lblIdName.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblIdName.setForeground(TEXT_DARK);

        boolean active = Boolean.TRUE.equals(km.getTrangThai());

        JLabel badge = new JLabel(
                active ? "Đang áp dụng" : "Ngừng áp dụng"
        );
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        badge.setBackground(active ? GREEN_LIGHT : RED_LIGHT);
        badge.setForeground(active ? GREEN_ACCENT : RED_ACCENT);

        topBox.add(lblIdName, BorderLayout.CENTER);
        topBox.add(badge, BorderLayout.EAST);

        card.add(topBox, BorderLayout.NORTH);

        // Thời gian và mức giảm
        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));

        String start = km.getNgayBatDau() != null
                ? new java.sql.Date(
                        km.getNgayBatDau().getTime()
                ).toLocalDate().toString()
                : "";

        String end = km.getNgayKetThuc() != null
                ? new java.sql.Date(
                        km.getNgayKetThuc().getTime()
                ).toLocalDate().toString()
                : "";

        JLabel lblDate = new JLabel(
                "Thời gian: " + start + "  →  " + end
        );
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDate.setForeground(TEXT_GRAY);

        JLabel lblDiscount = new JLabel(
                "Mức giảm: " + km.getGiaTriGiam() + "% tổng dịch vụ"
        );
        lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDiscount.setForeground(BLUE_PRIMARY);

        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(lblDate);
        centerBox.add(Box.createVerticalStrut(12));
        centerBox.add(lblDiscount);

        card.add(centerBox, BorderLayout.CENTER);

        // Chỉnh sửa thông tin kèm ảnh cây bút
        JLabel lblAction = new JLabel("Chỉnh sửa thông tin");
        lblAction.setIcon(loadImage("/img/pen.png", 18, 18));
        lblAction.setIconTextGap(7);
        lblAction.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblAction.setForeground(BLUE_PRIMARY);

        card.add(lblAction, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loadSelection(km);
            }
        });

        return card;
    }

    // tải DL
    private void reload() {
        try {
            allPromotionsList = promotionDAO.getAll();
            renderCards(allPromotionsList);

        } catch (Exception e) {
            e.printStackTrace();
            warn("Không thể tải danh sách khuyến mãi: " + e.getMessage());
        }
    }

    // hiển thị card và thống kê 
    private void renderCards(List<KhuyenMai> list) {
        gridCardContainer.removeAll();

        int activeCount = 0;
        int upcomingCount = 0;
        int expiredCount = 0;

        LocalDate today = LocalDate.now();

        for (KhuyenMai km : list) {
            gridCardContainer.add(createPromotionCard(km));

            if (km.getNgayBatDau() != null
                    && km.getNgayKetThuc() != null) {

                LocalDate start = new java.sql.Date(
                        km.getNgayBatDau().getTime()
                ).toLocalDate();

                LocalDate end = new java.sql.Date(
                        km.getNgayKetThuc().getTime()
                ).toLocalDate();

                if (today.isBefore(start)) {
                    upcomingCount++;
                } else if (!today.isAfter(end)) {
                    activeCount++;
                } else {
                    expiredCount++;
                }
            }
        }

        lblStatActiveCount.setText(String.valueOf(activeCount));
        lblStatUpcomingCount.setText(String.valueOf(upcomingCount));
        lblStatExpiredCount.setText(String.valueOf(expiredCount));

        lblTotalCount.setText(
                "Tổng cộng: " + list.size() + " chương trình"
        );

        gridCardContainer.revalidate();
        gridCardContainer.repaint();
    }

    // tìm
    private void applyFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();

        if (allPromotionsList == null) return;

        if (keyword.isEmpty()) {
            renderCards(allPromotionsList);
            return;
        }

        List<KhuyenMai> filtered = allPromotionsList.stream()
                .filter(km ->
                        (km.getMaKhuyenMai() != null
                                && km.getMaKhuyenMai()
                                .toLowerCase().contains(keyword))
                        ||
                        (km.getTenKhuyenMai() != null
                                && km.getTenKhuyenMai()
                                .toLowerCase().contains(keyword))
                )
                .collect(Collectors.toList());

        renderCards(filtered);
    }

    // thêm và update
    private void savePromotion() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();

        String discountText = txtDiscount.getText()
                .trim()
                .replace("%", "")
                .replace(",", ".");

        if (id.isEmpty() || name.isEmpty()) {
            warn("Vui lòng nhập Mã khuyến mãi và Tên chương trình!");
            return;
        }

        try {
            LocalDate from = parseDate(dateFrom, "Ngày bắt đầu");
            LocalDate to = parseDate(dateTo, "Ngày kết thúc");

            if (to.isBefore(from)) {
                warn("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
                return;
            }

            double discount = Double.parseDouble(discountText);

            if (discount <= 0 || discount > 100) {
                warn("Mức giảm phải lớn hơn 0 và không vượt quá 100.");
                return;
            }

            KhuyenMai promotion = new KhuyenMai();
            promotion.setMaKhuyenMai(id);
            promotion.setTenKhuyenMai(name);
            promotion.setGiaTriGiam(discount);
            promotion.setNgayBatDau(java.sql.Date.valueOf(from));
            promotion.setNgayKetThuc(java.sql.Date.valueOf(to));
            promotion.setTrangThai(chkActive.isSelected());

            boolean wasEditing = editing;

            boolean saved = wasEditing
                    ? promotionDAO.update(promotion)
                    : promotionDAO.insert(promotion);

            if (!saved) {
                throw new SQLException(
                        "SQL Server không lưu được thay đổi."
                );
            }

            clearForm();
            reload();

            info(wasEditing
                    ? "Đã cập nhật chương trình khuyến mãi thành công!"
                    : "Đã thêm chương trình khuyến mãi mới!"
            );

        } catch (NumberFormatException e) {
            warn("Mức giảm giá không hợp lệ. Vui lòng nhập số.");

        } catch (IllegalArgumentException e) {
            warn(e.getMessage());

        } catch (SQLException e) {
            warn("Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    // ktra ngày
    private LocalDate parseDate(
            TruongChonNgay field,
            String label) {

        if (!field.isValidDate()) {
            throw new IllegalArgumentException(
                    label + " không hợp lệ."
            );
        }

        String text = field.getText().trim();

        try {
            String[] parts = text.split("/");

            if (parts.length == 3) {
                return LocalDate.of(
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[0])
                );
            }

            return LocalDate.parse(text);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Vui lòng chọn " + label.toLowerCase() + " hợp lệ."
            );
        }
    }

    // xóa SALE
    private void deletePromotion() {
        if (!editing || txtId.getText().trim().isEmpty()) {
            warn(
                    "Vui lòng chọn chương trình cần xóa từ danh sách bên dưới."
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa chương trình: "
                        + txtId.getText().trim() + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (!promotionDAO.delete(txtId.getText().trim())) {
                throw new SQLException(
                        "Không tìm thấy mã chương trình trong CSDL."
                );
            }

            clearForm();
            reload();

            info("Đã xóa chương trình khuyến mãi thành công.");

        } catch (SQLException e) {
            warn("Không thể xóa chương trình: " + e.getMessage());
        }
    }

    // chọn SALE để sửa
    private void loadSelection(KhuyenMai km) {
        txtId.setText(km.getMaKhuyenMai());
        txtId.setEnabled(false);

        txtName.setText(km.getTenKhuyenMai());
        txtDiscount.setText(String.valueOf(km.getGiaTriGiam()));

        if (km.getNgayBatDau() != null) {
            dateFrom.setText(
                    new java.sql.Date(
                            km.getNgayBatDau().getTime()
                    ).toLocalDate().toString()
            );
        }

        if (km.getNgayKetThuc() != null) {
            dateTo.setText(
                    new java.sql.Date(
                            km.getNgayKetThuc().getTime()
                    ).toLocalDate().toString()
            );
        }

        chkActive.setSelected(
                Boolean.TRUE.equals(km.getTrangThai())
        );

        editing = true;
    }

    // làm mới form
    private void clearForm() {
        editing = false;
        txtId.setEnabled(true);
        txtId.setText("");
        txtName.setText("");
        txtDiscount.setText("");
        txtSearch.setText("");

        dateFrom.setText(LocalDate.now().toString());
        dateTo.setText(LocalDate.now().plusMonths(1).toString());

        chkActive.setSelected(true);
    }

    // th.báo
    private void warn(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // nút btn
    private JButton createButton(String text,Color background, int width, int height) {

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color c = getModel().isPressed()? background.darker(): getModel().isRollover()  ? background.brighter(): background;

                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10 );
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);

        button.setPreferredSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );

        return button;
    }

    //panel hơi bo góc 
    private static class KhungVienBoGoc extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        public KhungVienBoGoc(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(), radius, radius
            );

            g2.setColor(BORDER_LIGHT);
            g2.drawRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1,
                    radius, radius
            );

            g2.dispose();
        }
    }
}
