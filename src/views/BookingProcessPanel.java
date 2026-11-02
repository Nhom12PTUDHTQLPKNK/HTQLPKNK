package views;

import controllers.AuditLog;
import models.dao.LichHenDAO;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BookingProcessPanel extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color BLUE_DARK = new Color(5, 70, 160);
    private static final Color BLUE_LIGHT = new Color(232, 241, 255);
    private static final Color GREEN_ACCENT = new Color(24, 117, 58);
    private static final Color GREEN_LIGHT = new Color(229, 247, 239);
    private static final Color RED_ACCENT = new Color(180, 35, 45);
    private static final Color RED_LIGHT = new Color(255, 235, 237);
    private static final Color ORANGE_ACCENT = new Color(148, 75, 8);
    private static final Color ORANGE_LIGHT = new Color(255, 245, 225);
    private static final Color BG_CANVAS = new Color(244, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(35, 52, 78);
    private static final Color TEXT_GRAY = new Color(119, 135, 157);
    private static final Color BORDER_LIGHT = new Color(225, 233, 243);

    // Màu đậm hơn cho chữ + số trong 3 card thống kê
    private static final Color STAT_BLUE_TEXT = new Color(8, 62, 150);
    private static final Color STAT_GREEN_TEXT = new Color(12, 92, 40);
    private static final Color STAT_ORANGE_TEXT = new Color(175, 85, 0);

    // Màu viền nhẹ cho 3 card thống kê
    private static final Color STAT_BLUE_BORDER = new Color(196, 218, 250);
    private static final Color STAT_GREEN_BORDER = new Color(190, 230, 208);
    private static final Color STAT_ORANGE_BORDER = new Color(250, 222, 176);

    private static final Map<String, Image> IMG_CACHE = new HashMap<>();

    private JPanel gridPendingContainer, gridConfirmedContainer, gridHistoryContainer;
    private JPanel detailContent;
    private JPanel selectedCard;
    private CardLayout detailLayout;
    private JTabbedPane tabbedPane;
    private JTextField txtSearch;
    private LichHenDAO lichHenDAO;

    private JLabel lblPendingCount, lblConfirmedCount, lblHistoryCount;
    private JLabel lblDetailName, lblDetailPhone, lblDetailDate, lblDetailService, lblDetailDoctor, lblDetailStatus;

    private JButton btnActionPrimary, btnActionEdit, btnActionCancel, btnClear;
    private JButton btnReturnPending;
    private String selectedAppointmentId = null;
    private List<Object[]> allAppointmentsList;

    public BookingProcessPanel() {
        lichHenDAO = new LichHenDAO();
        setLayout(new BorderLayout(0, 22));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(22));
        topPanel.add(createStatisticsPanel());
        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createCardGridPanel(), BorderLayout.CENTER);
        contentPanel.add(createBookingFormPanel(), BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);

        loadAppointments();
        updateActionButtonsState();
    }

    // 1. HEADER DẠNG CARD NGANG (GRADIENT HỒNG <-> XANH + LOGO)
    private JPanel createHeaderPanel() {
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Định nghĩa màu hồng pastel và xanh pastel
                Color pinkPastel = new Color(255, 232, 243);
                Color bluePastel = new Color(219, 234, 254);

                // Hiệu ứng trải màu từ trái sang phải
                GradientPaint gradient = new GradientPaint(0, 0, pinkPastel, getWidth(), 0, bluePastel);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Viền card
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        headerCard.setLayout(new BorderLayout());
        headerCard.setPreferredSize(new Dimension(0, 110));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Lịch Hẹn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(TEXT_DARK);

        // Tải và thu nhỏ logo cho cân đối (45x45 pixel)
        if (img("logo5.png") != null) {
            lblTitle.setIcon(icon("logo5.png", 45, 45));
            lblTitle.setIconTextGap(14); // Khoảng cách giữa ảnh và chữ tiêu đề
        }

        JLabel lblSubtitle = new JLabel("Theo dõi và quản lý lịch khám tại phòng khám nha khoa");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSubtitle.setForeground(TEXT_GRAY);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSubtitle);

        // Căn giữa tiêu đề theo chiều dọc trong card
        JPanel headerWestWrapper = new JPanel(new GridBagLayout());
        headerWestWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        headerWestWrapper.add(titleBox, gbc);

        headerCard.add(headerWestWrapper, BorderLayout.CENTER);

        // Khu vực tìm kiếm nằm ở góc phải của Card Header
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);

        txtSearch = new PlaceholderTextField("Nhập tên, SĐT hoặc mã lịch hẹn");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setPreferredSize(new Dimension(310, 45));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 12, 5, 12)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createButton("Tìm kiếm", BLUE_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(115, 45));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = createButton("Tải lại", new Color(74, 88, 105));
        btnRefresh.setPreferredSize(new Dimension(95, 45));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            clearForm();
            loadAppointments();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel headerRight = new JPanel(new GridBagLayout());
        headerRight.setOpaque(false);
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.anchor = GridBagConstraints.EAST;
        headerRight.add(searchPanel, gbcRight);

        headerCard.add(headerRight, BorderLayout.EAST);

        return headerCard;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 18, 0));
        panel.setOpaque(false);

        JPanel pendingCard = createStatisticCard(
                "LỊCH CHỜ XÁC NHẬN", "0", "Đang chờ xử lý",
                STAT_BLUE_TEXT, BLUE_LIGHT, STAT_BLUE_BORDER, "ca-wait.png"
        );

        JPanel confirmedCard = createStatisticCard(
                "LỊCH ĐÃ XÁC NHẬN", "0", "Đã được tiếp nhận",
                STAT_GREEN_TEXT, GREEN_LIGHT, STAT_GREEN_BORDER, "ca-confirm.png"
        );

        JPanel historyCard = createStatisticCard(
                "LỊCH ĐÃ XỬ LÝ", "0", "Hoàn thành hoặc đã hủy",
                STAT_ORANGE_TEXT, ORANGE_LIGHT, STAT_ORANGE_BORDER, "ca-cancel.png"
        );

        lblPendingCount = (JLabel) pendingCard.getClientProperty("countLabel");
        lblConfirmedCount = (JLabel) confirmedCard.getClientProperty("countLabel");
        lblHistoryCount = (JLabel) historyCard.getClientProperty("countLabel");

        makeCardClickable(pendingCard, 0);
        makeCardClickable(confirmedCard, 1);
        makeCardClickable(historyCard, 2);

        panel.add(pendingCard);
        panel.add(confirmedCard);
        panel.add(historyCard);

        return panel;
    }

    private JPanel createStatisticCard(String title, String number, String description,
                                       Color textColor, Color bgColor, Color borderColor, String image) {
        RoundedPanel card = new RoundedPanel(18, bgColor, borderColor);
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new EmptyBorder(22, 26, 22, 26));
        card.setPreferredSize(new Dimension(200, 150));

        // Icon to hơn, không còn ô nền vuông
        JLabel iconLabel = new JLabel(icon(image, 64, 64));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconLabel);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(textColor);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 44));
        lblNumber.setForeground(textColor);

        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDescription.setForeground(textColor);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblNumber);
        textPanel.add(lblDescription);

        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        textWrap.add(textPanel, gbc);

        card.add(iconWrap, BorderLayout.WEST);
        card.add(textWrap, BorderLayout.CENTER);
        card.putClientProperty("countLabel", lblNumber);

        return card;
    }

    private void makeCardClickable(Component component, final int tabIndex) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tabbedPane != null) tabbedPane.setSelectedIndex(tabIndex);
            }
        });

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                makeCardClickable(child, tabIndex);
            }
        }
    }

    private JPanel createCardGridPanel() {
        RoundedPanel panel = new RoundedPanel(18, CARD_BG);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách lịch hẹn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblHint = new JLabel("Chọn một thẻ lịch hẹn để xem thông tin chi tiết");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHint.setForeground(TEXT_GRAY);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(lblTitle);
        heading.add(Box.createVerticalStrut(5));
        heading.add(lblHint);

        titlePanel.add(heading, BorderLayout.WEST);

        gridPendingContainer = new JPanel(new GridLayout(0, 2, 16, 16));
        gridPendingContainer.setOpaque(false);

        gridConfirmedContainer = new JPanel(new GridLayout(0, 2, 16, 16));
        gridConfirmedContainer.setOpaque(false);

        gridHistoryContainer = new JPanel(new GridLayout(0, 2, 16, 16));
        gridHistoryContainer.setOpaque(false);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setForeground(TEXT_DARK);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Chờ xác nhận (0)", createCardScrollPane(gridPendingContainer));
        tabbedPane.addTab("Đã xác nhận (0)", createCardScrollPane(gridConfirmedContainer));
        tabbedPane.addTab("Lịch sử xử lý (0)", createCardScrollPane(gridHistoryContainer));

        tabbedPane.addChangeListener(e -> {
            if (selectedAppointmentId != null) updateActionButtonsState();
        });

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createCardScrollPane(JPanel gridContainer) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CANVAS);
        wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        wrapper.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        return scroll;
    }

    private JPanel createAppointmentCard(Object[] row) {
        String id = String.valueOf(row[0]);
        String customerName = String.valueOf(row[1]);
        String phone = String.valueOf(row[2]);
        String service = String.valueOf(row[3]);
        String doctor = String.valueOf(row[4]);
        String date = String.valueOf(row[5]);
        String time = String.valueOf(row[6]);
        String status = String.valueOf(row[7]);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean selected = this == selectedCard;

                g2.setColor(selected ? BLUE_LIGHT : CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setStroke(new BasicStroke(selected ? 3f : 1f));
                g2.setColor(selected ? BLUE_PRIMARY : BORDER_LIGHT);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        //(Tăng chiều cao card để nội dung có đủ không gian hiển thị)
        card.setPreferredSize(new Dimension(0, 245));

        JPanel topBox = new JPanel(new BorderLayout(10, 0));
        topBox.setOpaque(false);

        JPanel customerPanel = new JPanel(new BorderLayout(10, 0));
        customerPanel.setOpaque(false);

        JLabel avatar = new JLabel(icon("rang-tiepnhan.png", 50, 50));
        avatar.setPreferredSize(new Dimension(50, 50));

        JLabel lblName = new JLabel(htmlText(customerName, 190));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setForeground(TEXT_DARK);

        customerPanel.add(avatar, BorderLayout.WEST);
        customerPanel.add(lblName, BorderLayout.CENTER);

        JLabel lblStatusBadge = new JLabel(" " + status + " ");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatusBadge.setOpaque(true);

        if (status.equalsIgnoreCase("Chờ xác nhận")) {
            lblStatusBadge.setBackground(BLUE_LIGHT);
            lblStatusBadge.setForeground(BLUE_PRIMARY);
        } else if (status.equalsIgnoreCase("Đã xác nhận")) {
            lblStatusBadge.setBackground(GREEN_LIGHT);
            lblStatusBadge.setForeground(GREEN_ACCENT);
        } else if (status.equalsIgnoreCase("Đã hủy")) {
            lblStatusBadge.setBackground(RED_LIGHT);
            lblStatusBadge.setForeground(RED_ACCENT);
        } else {
            lblStatusBadge.setBackground(ORANGE_LIGHT);
            lblStatusBadge.setForeground(ORANGE_ACCENT);
        }

        topBox.add(customerPanel, BorderLayout.CENTER);
        topBox.add(lblStatusBadge, BorderLayout.EAST);

        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);
        centerBox.setBorder(new EmptyBorder(2, 60, 0, 0));

        JLabel lblDateTime = new JLabel(date + "  •  " + time, icon("lich.png", 18, 18), SwingConstants.LEFT);
        lblDateTime.setIconTextGap(8);
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDateTime.setForeground(TEXT_GRAY);

        JLabel lblPhone = new JLabel(phone.isEmpty() ? "Chưa có SĐT" : phone, icon("phone.png", 18, 18), SwingConstants.LEFT);
        lblPhone.setIconTextGap(8);
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPhone.setForeground(TEXT_GRAY);

        JLabel lblService = new JLabel(htmlText(service.isEmpty() ? "Khám nha khoa" : service, 260), icon("average-score.png", 20, 20), SwingConstants.LEFT);
        lblService.setIconTextGap(8);
        lblService.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblService.setForeground(BLUE_PRIMARY);

        lblDateTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblService.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerBox.add(lblDateTime);
        centerBox.add(Box.createVerticalStrut(8));
        centerBox.add(lblPhone);
        centerBox.add(Box.createVerticalStrut(8));
        centerBox.add(lblService);

        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);

        JLabel lblDoctor = new JLabel(htmlText("Bác sĩ: " + (doctor.isEmpty() ? "Chưa phân công" : doctor), 300));
        lblDoctor.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblDoctor.setForeground(TEXT_GRAY);

        bottomBox.add(lblDoctor, BorderLayout.WEST);

        card.add(topBox, BorderLayout.NORTH);
        card.add(centerBox, BorderLayout.CENTER);
        card.add(bottomBox, BorderLayout.SOUTH);

        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFormFromData(id, customerName, phone, service, doctor, date, time, status);
                if (selectedCard != null) selectedCard.repaint();
                selectedCard = card;
                card.repaint();
            }
        };

        card.addMouseListener(selectListener);

        for (Component c : topBox.getComponents()) {
            addMouseListenerRecursively(c, selectListener);
        }

        for (Component c : centerBox.getComponents()) {
            addMouseListenerRecursively(c, selectListener);
        }

        for (Component c : bottomBox.getComponents()) {
            addMouseListenerRecursively(c, selectListener);
        }

        return card;
    }

    private void addMouseListenerRecursively(Component component, MouseAdapter listener) {
        component.addMouseListener(listener);
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                addMouseListenerRecursively(child, listener);
            }
        }
    }

    private JPanel createBookingFormPanel() {
        RoundedPanel panel = new RoundedPanel(18, CARD_BG);
        panel.setLayout(new BorderLayout(0, 18));
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));
        panel.setPreferredSize(new Dimension(430, 0));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thông tin lịch hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Chi tiết khách hàng và thao tác");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(subtitle);

        detailLayout = new CardLayout();
        detailContent = new JPanel(detailLayout);
        detailContent.setOpaque(false);

        detailContent.add(createEmptyDetailPanel(), "empty");
        detailContent.add(createAppointmentDetailPanel(), "details");

        //(Dùng thanh cuộn riêng để không bị mất thông tin khi nội dung dài)
        JScrollPane detailScroll = new JScrollPane(detailContent);
        detailScroll.setBorder(null);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        buttonPanel.setOpaque(false);

        btnActionPrimary = createButton("Xác nhận lịch hẹn", GREEN_ACCENT);
        btnActionEdit = createButton("Sửa lịch hẹn", ORANGE_ACCENT);
        btnActionCancel = createButton("Hủy lịch hẹn", RED_ACCENT);
        btnClear = createButton("Làm mới lựa chọn", BLUE_PRIMARY);

        // Nút chuyển lịch đã xác nhận về chờ xác nhận
        btnReturnPending = createButton("Chuyển về chờ xác nhận", ORANGE_ACCENT);

        btnActionPrimary.addActionListener(e -> performFlowAction());
        btnActionEdit.addActionListener(e -> performEdit());
        btnActionCancel.addActionListener(e -> performDelete());
        btnClear.addActionListener(e -> clearForm());
        btnReturnPending.addActionListener(e -> performReturnToPending());

        buttonPanel.add(btnActionPrimary);
        buttonPanel.add(btnActionEdit);
        buttonPanel.add(btnActionCancel);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnReturnPending);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        detailLayout.show(detailContent, "empty");

        return panel;
    }

    private JPanel createEmptyDetailPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(icon("logo1.png", 220, 220));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Chưa chọn lịch hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Vui lòng chọn một lịch hẹn");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(15));
        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);

        panel.add(content);

        return panel;
    }

    private JPanel createAppointmentDetailPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel customerCard = new JPanel();
        customerCard.setLayout(new BoxLayout(customerCard, BoxLayout.Y_AXIS));
        customerCard.setBackground(new Color(248, 250, 254));
        customerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel customerTitle = new JLabel("THÔNG TIN KHÁCH HÀNG");
        customerTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        customerTitle.setForeground(BLUE_PRIMARY);

        lblDetailName = new JLabel("Chưa chọn lịch hẹn");
        lblDetailName.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblDetailName.setForeground(TEXT_DARK);
        lblDetailName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetailPhone = new JLabel("---");
        lblDetailPhone.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDetailPhone.setForeground(TEXT_GRAY);
        lblDetailPhone.setIconTextGap(8);
        lblDetailPhone.setAlignmentX(Component.LEFT_ALIGNMENT);

        customerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        customerCard.add(customerTitle);
        customerCard.add(Box.createVerticalStrut(16));
        customerCard.add(lblDetailName);
        customerCard.add(Box.createVerticalStrut(12));
        customerCard.add(lblDetailPhone);

        JPanel appointmentCard = new JPanel();
        appointmentCard.setLayout(new BoxLayout(appointmentCard, BoxLayout.Y_AXIS));
        appointmentCard.setBackground(new Color(248, 250, 254));
        appointmentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel appointmentTitle = new JLabel("CHI TIẾT LỊCH HẸN");
        appointmentTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        appointmentTitle.setForeground(BLUE_PRIMARY);
        appointmentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        appointmentCard.add(appointmentTitle);
        appointmentCard.add(Box.createVerticalStrut(18));

        lblDetailDate = addDetailRow(appointmentCard, "Ngày / Giờ hẹn", "---");
        lblDetailService = addDetailRow(appointmentCard, "Dịch vụ", "---");
        lblDetailDoctor = addDetailRow(appointmentCard, "Bác sĩ", "---");
        lblDetailStatus = addDetailRow(appointmentCard, "Trạng thái", "---");

        customerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(customerCard);
        panel.add(Box.createVerticalStrut(15));
        panel.add(appointmentCard);

        return panel;
    }

    private JLabel addDetailRow(JPanel panel, String labelText, String val) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKey = new JLabel(labelText);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKey.setForeground(TEXT_GRAY);
        lblKey.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblVal = new JLabel(htmlText(val, 300));
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblVal.setForeground(TEXT_DARK);
        lblVal.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(lblKey);
        row.add(Box.createVerticalStrut(5));
        row.add(lblVal);

        panel.add(row);
        panel.add(Box.createVerticalStrut(14));

        return lblVal;
    }

    private void showPhone(String phone) {
        lblDetailPhone.setIcon(phone == null ? null : icon("phone.png", 20, 20));
        lblDetailPhone.setText(phone == null ? "---" : (phone.isEmpty() ? "Chưa có SĐT" : phone));
    }

    private void fillFormFromData(String id, String name, String phone, String service, String doctor, String date, String time, String status) {
        selectedAppointmentId = id;

        lblDetailName.setText(htmlText(name, 300));
        showPhone(phone);
        lblDetailDate.setText(htmlText(date + " • " + time, 300));
        lblDetailService.setText(htmlText(service.isEmpty() ? "Khám nha khoa" : service, 300));
        lblDetailDoctor.setText(htmlText(doctor.isEmpty() ? "Chưa phân công" : doctor, 300));
        lblDetailStatus.setText(htmlText(status, 300));

        detailLayout.show(detailContent, "details");
        detailContent.revalidate();
        detailContent.repaint();
        updateActionButtonsState();
    }

    private void performFlowAction() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int currentTab = tabbedPane.getSelectedIndex();
        boolean success = false;

        if (currentTab == 0) {
            success = lichHenDAO.xacNhanLichHen(selectedAppointmentId);

            if (success) {
                AuditLog.event("UPDATE", "Xác nhận lịch hẹn - mã " + selectedAppointmentId);
                JOptionPane.showMessageDialog(this, "Đã xác nhận lịch hẹn!");
            }
        } else if (currentTab == 1) {
        	performReturnToPending();
            return;
        } else {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này đã kết thúc xử lý!");
            return;
        }

        if (success) {
            // Báo cho trang Tra cứu lịch hẹn cập nhật theo
            AppointmentSearchPanel.notifyAppointmentChanged();
            clearForm();
            loadAppointments();
        }
    }
    
    private void performReturnToPending() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn lịch hẹn cần chuyển!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Tạo ô nhập lý do
        JTextArea txtReason = new JTextArea(5, 30);
        txtReason.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollReason = new JScrollPane(txtReason);
        scrollReason.setPreferredSize(new Dimension(350, 120));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel lblGuide = new JLabel(
                "<html>Vui lòng nhập lý do chuyển lịch hẹn về trạng thái chờ xác nhận:</html>"
        );
        lblGuide.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        form.add(lblGuide);
        form.add(Box.createVerticalStrut(10));
        form.add(scrollReason);

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Chuyển về chờ xác nhận - " + selectedAppointmentId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String reason = txtReason.getText().trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập lý do chuyển trạng thái!",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn chuyển lịch hẹn này về trạng thái Chờ xác nhận không?",
                "Xác nhận thao tác",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String ma = selectedAppointmentId;

        String sql = "UPDATE LichHen "
                   + "SET trangThai = N'Chờ xác nhận' "
                   + "WHERE maLichHen = ? AND trangThai = N'Đã xác nhận'";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, ma);

            int affectedRows = p.executeUpdate();

            if (affectedRows > 0) {
                // Lưu lý do vào nhật ký hoạt động
                AuditLog.event(
                        "UPDATE",
                        "Chuyển lịch hẹn mã " + ma
                        + " từ Đã xác nhận về Chờ xác nhận. Lý do: " + reason
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Đã chuyển lịch hẹn về trạng thái Chờ xác nhận!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Báo cho trang Tra cứu lịch hẹn cập nhật theo
                AppointmentSearchPanel.notifyAppointmentChanged();

                clearForm();
                loadAppointments();

                // Tự động chuyển sang tab Chờ xác nhận
                tabbedPane.setSelectedIndex(0);

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể chuyển lịch hẹn. Có thể lịch đã thay đổi trạng thái trước đó!",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi chuyển trạng thái lịch hẹn:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void updateActionButtonsState() {
        if (btnActionPrimary == null) return;

        int tabIdx = tabbedPane.getSelectedIndex();

        btnActionEdit.setVisible(tabIdx != 2);

        if (tabIdx == 0) {
            // Tab chờ xác nhận
            btnActionPrimary.setText("Xác nhận lịch hẹn");
            btnActionPrimary.setEnabled(true);
            btnActionPrimary.setVisible(true);

            btnActionCancel.setVisible(true);
            btnReturnPending.setVisible(false);

        } else if (tabIdx == 1) {
            // Tab đã xác nhận
            btnActionPrimary.setText("Đã xác nhận");
            btnActionPrimary.setEnabled(false);
            btnActionPrimary.setVisible(true);

            btnActionCancel.setVisible(false);
            btnReturnPending.setVisible(true);
            btnReturnPending.setEnabled(true);

        } else {
            // Tab lịch sử xử lý
            btnActionPrimary.setVisible(false);
            btnActionCancel.setVisible(false);
            btnReturnPending.setVisible(false);
        }

        if (btnClear != null) {
            btnClear.setVisible(true);
        }

        revalidate();
        repaint();
    }

    private void performEdit() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String ma = selectedAppointmentId;

        try (Connection c = DBConnection.getConnection()) {
            java.sql.Date oldDate;
            String oldTime, oldDoc, oldNote;

            try (PreparedStatement p = c.prepareStatement("SELECT ngayHen, gioKham, maNhanVien, ghiChu FROM LichHen WHERE maLichHen=?")) {
                p.setString(1, ma);

                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn " + ma + " trong cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    oldDate = r.getDate(1);
                    oldTime = nz(r.getString(2));
                    oldDoc = r.getString(3);
                    oldNote = nz(r.getString(4));
                }
            }

            List<String> ids = new ArrayList<>();
            JComboBox<String> cbDoc = new JComboBox<>();

            ids.add(null);
            cbDoc.addItem("Chưa phân công");

            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT maNhanVien, tenNhanVien FROM NhanVien WHERE chucVu LIKE N'%sĩ%' ORDER BY tenNhanVien")) {
                while (r.next()) {
                    ids.add(r.getString(1));
                    cbDoc.addItem(r.getString(2));
                }
            }

            if (ids.indexOf(oldDoc) > 0) cbDoc.setSelectedIndex(ids.indexOf(oldDoc));

            JComboBox<String> cbSv = new JComboBox<>();
            cbSv.setEditable(true);

            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT tenDichVu FROM DichVu ORDER BY tenDichVu")) {
                while (r.next()) cbSv.addItem(r.getString(1));
            }

            cbSv.setSelectedItem(oldNote);

            JComboBox<String> cbTime = new JComboBox<>();
            cbTime.setEditable(true);

            for (int m = 450; m <= 1050; m += 30) {
                cbTime.addItem(String.format("%02d:%02d", m / 60, m % 60));
            }

            cbTime.setSelectedItem(oldTime);

            JTextField txtDate = new JTextField(oldDate == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(oldDate));
            txtDate.setEditable(false);
            txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            JButton btnCalendar = new JButton("Chọn ngày");
            btnCalendar.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCalendar.addActionListener(e -> showDatePicker(txtDate));

            JPanel datePanel = new JPanel(new BorderLayout(5, 0));
            datePanel.add(txtDate, BorderLayout.CENTER);
            datePanel.add(btnCalendar, BorderLayout.EAST);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
            form.setPreferredSize(new Dimension(370, 370));

            form.add(new JLabel("Khách hàng: " + lblDetailName.getText()));
            form.add(new JLabel("Ngày hẹn:"));
            form.add(datePanel);
            form.add(new JLabel("Giờ khám (HH:mm):"));
            form.add(cbTime);
            form.add(new JLabel("Bác sĩ phụ trách:"));
            form.add(cbDoc);
            form.add(new JLabel("Dịch vụ / ghi chú:"));
            form.add(cbSv);

            while (JOptionPane.showConfirmDialog(this, form, "Sửa lịch hẹn - " + ma, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                String err = null;
                String time = val(cbTime);
                String docId = ids.get(cbDoc.getSelectedIndex());
                java.sql.Date nd = null;

                try {
                    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                    f.setLenient(false);
                    nd = new java.sql.Date(f.parse(txtDate.getText().trim()).getTime());
                } catch (java.text.ParseException ex) {
                    err = "Ngày hẹn không hợp lệ, vui lòng nhập dạng dd/MM/yyyy!";
                }

                if (err == null && !Pattern.matches("([01]\\d|2[0-3]):[0-5]\\d", time)) {
                    err = "Giờ khám không hợp lệ, vui lòng nhập dạng HH:mm!";
                }

                if (err == null && nd.before(java.sql.Date.valueOf(LocalDate.now())) && !nd.equals(oldDate)) {
                    err = "Ngày hẹn mới không được nằm trong quá khứ!";
                }

                if (err == null && docId != null) {
                    try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM LichHen WHERE maNhanVien=? AND ngayHen=? AND gioKham=? AND maLichHen<>? AND trangThai<>N'Đã hủy'")) {
                        p.setString(1, docId);
                        p.setDate(2, nd);
                        p.setString(3, time);
                        p.setString(4, ma);

                        try (ResultSet r = p.executeQuery()) {
                            if (r.next() && r.getInt(1) > 0) {
                                err = "Bác sĩ đã có lịch hẹn vào khung giờ này, vui lòng chọn giờ hoặc bác sĩ khác!";
                            }
                        }
                    }
                }

                if (err != null) {
                    JOptionPane.showMessageDialog(this, err, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    continue;
                }

                try (PreparedStatement p = c.prepareStatement("UPDATE LichHen SET ngayHen=?, gioKham=?, maNhanVien=?, ghiChu=? WHERE maLichHen=?")) {
                    p.setDate(1, nd);
                    p.setString(2, time);

                    if (docId == null) p.setNull(3, Types.VARCHAR);
                    else p.setString(3, docId);

                    p.setString(4, val(cbSv));
                    p.setString(5, ma);
                    p.executeUpdate();
                }

                AuditLog.event("UPDATE", "Sửa lịch hẹn - mã " + ma);
                JOptionPane.showMessageDialog(this, "Đã cập nhật lịch hẹn!");

                // Báo cho trang Tra cứu lịch hẹn cập nhật theo
                AppointmentSearchPanel.notifyAppointmentChanged();

                clearForm();
                loadAppointments();
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi sửa lịch hẹn:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDatePicker(JTextField txtDate) {
        LocalDate selectedDate = LocalDate.now();

        try {
            if (!txtDate.getText().trim().isEmpty()) {
                selectedDate = LocalDate.parse(txtDate.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (Exception e) {
            selectedDate = LocalDate.now();
        }

        final LocalDate[] currentDate = {selectedDate};

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Chọn ngày hẹn",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setOpaque(false);

        JButton btnPrevious = new JButton("<");
        JButton btnNext = new JButton(">");

        JLabel lblMonth = new JLabel("", SwingConstants.CENTER);
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMonth.setForeground(TEXT_DARK);

        monthPanel.add(btnPrevious, BorderLayout.WEST);
        monthPanel.add(lblMonth, BorderLayout.CENTER);
        monthPanel.add(btnNext, BorderLayout.EAST);

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 6, 6));
        calendarPanel.setOpaque(false);

        String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        Runnable updateCalendar = () -> {
            calendarPanel.removeAll();

            for (String day : weekdays) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setForeground(TEXT_GRAY);
                calendarPanel.add(label);
            }

            YearMonth month = YearMonth.from(currentDate[0]);
            lblMonth.setText("Tháng " + month.getMonthValue() + " / " + month.getYear());

            int firstDay = month.atDay(1).getDayOfWeek().getValue();
            int daysInMonth = month.lengthOfMonth();

            for (int i = 1; i < firstDay; i++) {
                calendarPanel.add(new JLabel(""));
            }

            for (int day = 1; day <= daysInMonth; day++) {
                final LocalDate date = month.atDay(day);

                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btnDay.setFocusPainted(false);
                btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));

                if (date.equals(LocalDate.now())) {
                    btnDay.setBackground(BLUE_LIGHT);
                    btnDay.setForeground(BLUE_PRIMARY);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
                }

                if (date.equals(currentDate[0])) {
                    btnDay.setBackground(BLUE_PRIMARY);
                    btnDay.setForeground(Color.WHITE);
                }

                btnDay.addActionListener(e -> {
                    txtDate.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    dialog.dispose();
                });

                calendarPanel.add(btnDay);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();
        };

        btnPrevious.addActionListener(e -> {
            currentDate[0] = currentDate[0].minusMonths(1);
            updateCalendar.run();
        });

        btnNext.addActionListener(e -> {
            currentDate[0] = currentDate[0].plusMonths(1);
            updateCalendar.run();
        });

        updateCalendar.run();

        mainPanel.add(monthPanel, BorderLayout.NORTH);
        mainPanel.add(calendarPanel, BorderLayout.CENTER);

        dialog.setContentPane(mainPanel);
        dialog.setSize(380, 360);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void loadAppointments() {
        try {
            allAppointmentsList = lichHenDAO.getAllAppointments();
            renderFilteredAppointments(txtSearch == null ? "" : txtSearch.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách lịch hẹn:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderFilteredAppointments(String keyword) {
        gridPendingContainer.removeAll();
        gridConfirmedContainer.removeAll();
        gridHistoryContainer.removeAll();

        selectedCard = null;

        int pendingCount = 0;
        int confirmedCount = 0;
        int historyCount = 0;

        String query = keyword.toLowerCase();

        for (Object[] row : allAppointmentsList) {
            String id = String.valueOf(row[0]);
            String name = String.valueOf(row[1]);
            String phone = String.valueOf(row[2]);
            String status = String.valueOf(row[7]);

            if (!query.isEmpty() && !id.toLowerCase().contains(query) && !name.toLowerCase().contains(query) && !phone.contains(query)) {
                continue;
            }

            JPanel card = createAppointmentCard(row);

            if (status.equalsIgnoreCase("Chờ xác nhận")) {
                gridPendingContainer.add(card);
                pendingCount++;
            } else if (status.equalsIgnoreCase("Đã xác nhận")) {
                gridConfirmedContainer.add(card);
                confirmedCount++;
            } else {
                gridHistoryContainer.add(card);
                historyCount++;
            }
        }

        lblPendingCount.setText(String.valueOf(pendingCount));
        lblConfirmedCount.setText(String.valueOf(confirmedCount));
        lblHistoryCount.setText(String.valueOf(historyCount));

        tabbedPane.setTitleAt(0, "Chờ xác nhận (" + pendingCount + ")");
        tabbedPane.setTitleAt(1, "Đã xác nhận (" + confirmedCount + ")");
        tabbedPane.setTitleAt(2, "Lịch sử xử lý (" + historyCount + ")");

        gridPendingContainer.revalidate();
        gridConfirmedContainer.revalidate();
        gridHistoryContainer.revalidate();

        gridPendingContainer.repaint();
        gridConfirmedContainer.repaint();
        gridHistoryContainer.repaint();
    }

    private void performDelete() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy lịch hẹn " + selectedAppointmentId + " không?",
                "Xác nhận hủy lịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION && lichHenDAO.cancelAppointment(selectedAppointmentId)) {
            AuditLog.event("UPDATE", "Hủy lịch hẹn - mã " + selectedAppointmentId);
            JOptionPane.showMessageDialog(this, "Hủy lịch hẹn thành công!");

            // Báo cho trang Tra cứu lịch hẹn cập nhật theo
            AppointmentSearchPanel.notifyAppointmentChanged();

            clearForm();
            loadAppointments();
        }
    }

    private void clearForm() {
        selectedAppointmentId = null;

        if (selectedCard != null) {
            selectedCard.repaint();
            selectedCard = null;
        }

        lblDetailName.setText("Chưa chọn lịch hẹn");
        showPhone(null);
        lblDetailDate.setText("---");
        lblDetailService.setText("---");
        lblDetailDoctor.setText("---");
        lblDetailStatus.setText("---");

        detailLayout.show(detailContent, "empty");
        updateActionButtonsState();

        repaint();
    }

    private void performSearch() {
        renderFilteredAppointments(txtSearch.getText().trim());
    }

    private static String htmlText(String text, int width) {
        if (text == null) text = "";
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width:" + width + "px'>" + escaped + "</div></html>";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String val(JComboBox<String> cb) {
        Object o = cb.getEditor().getItem();
        return o == null ? "" : o.toString().trim();
    }

    private static Image img(String name) {
        return IMG_CACHE.computeIfAbsent(name, n -> {
            java.net.URL u = BookingProcessPanel.class.getResource("/img/" + n);
            if (u != null) return new ImageIcon(u).getImage();

            File f = new File("src/img/" + n);
            return f.exists() ? new ImageIcon(f.getAbsolutePath()).getImage() : null;
        });
    }

    private static ImageIcon icon(String name, int w, int h) {
        Image image = img(name);

        if (image == null) {
            return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        }

        return new ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color = background;

                if (!isEnabled()) {
                    color = new Color(205, 210, 218);
                } else if (getModel().isPressed()) {
                    color = background.darker();
                } else if (getModel().isRollover()) {
                    color = background.brighter();
                }

                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 48));

        return button;
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(160, 170, 185));

                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics(getFont());

                g2.drawString(
                        placeholder,
                        insets.left + 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent()
                );

                g2.dispose();
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        public RoundedPanel(int radius, Color backgroundColor) {
            this(radius, backgroundColor, BORDER_LIGHT);
        }

        public RoundedPanel(int radius, Color backgroundColor, Color borderColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public void refreshData() {
        loadAppointments();
    }
}