package views;

import Audit.AuditLog;
import models.dao.LichHenDAO;
import sql.DBConnection;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BookingProcessPanel extends JPanel {

    // màu gui
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
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

    // Bộ nhớ đệm ảnh đọc từ src/img
    private static final Map<String, Image> IMG_CACHE = new HashMap<>();

 // Các khu vực giao diện
    private JPanel gridPendingContainer, gridConfirmedContainer, gridHistoryContainer;
    private JTabbedPane tabbedPane;
    private JTextField txtSearch;
    private LichHenDAO lichHenDAO;

 // Thống kê lịch hẹn
    private JLabel lblPendingCount, lblConfirmedCount, lblHistoryCount;

 // Thông tin lịch hẹn được chọn
    private String selectedAppointmentId = null;
    private JLabel lblDetailName, lblDetailPhone, lblDetailDate, lblDetailService, lblDetailDoctor, lblDetailStatus;
    private JButton btnActionPrimary, btnActionEdit, btnActionCancel;
    private List<Object[]> allAppointmentsList;

    public BookingProcessPanel() {
        lichHenDAO = new LichHenDAO();
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(22, 25, 22, 25));
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(20));
        topPanel.add(createStatisticsPanel());
        add(topPanel, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel(new BorderLayout(18, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createCardGridPanel(), BorderLayout.CENTER);
        contentPanel.add(createBookingFormPanel(), BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);
        loadAppointments();
    }

 // Tiêu đề và tìm kiếm
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Quản Lý Lịch Hẹn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(TEXT_DARK);
        JLabel lblSubtitle = new JLabel("Theo dõi và quản lý lịch khám tại phòng khám nha khoa");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_GRAY);
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitle);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        searchPanel.setOpaque(false);
        txtSearch = new PlaceholderTextField("Nhập tên, SĐT hoặc mã lịch hẹn");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(270, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_LIGHT), new EmptyBorder(5, 12, 5, 12)));
        txtSearch.addActionListener(e -> performSearch());
        JButton btnSearch = createButton("Tìm kiếm", BLUE_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> performSearch());
        JButton btnRefresh = createButton("Tải lại", new Color(74, 88, 105));
        btnRefresh.setPreferredSize(new Dimension(85, 38));
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadAppointments(); clearForm(); });
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);
        return panel;
    }

 // Thống kê lịch hẹn
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        JPanel pendingCard = createStatisticCard("LỊCH CHỜ XÁC NHẬN", "0", "Đang chờ xử lý", BLUE_PRIMARY, BLUE_LIGHT, "ca-wait.png");
        JPanel confirmedCard = createStatisticCard("LỊCH ĐÃ XÁC NHẬN", "0", "Đã được tiếp nhận", GREEN_ACCENT, GREEN_LIGHT, "ca-confirm.png");
        JPanel historyCard = createStatisticCard("LỊCH ĐÃ XỬ LÝ", "0", "Hoàn thành hoặc đã hủy", ORANGE_ACCENT, ORANGE_LIGHT, "ca-cancel.png");
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

    private JPanel createStatisticCard(String title, String number, String description, Color accent, Color lightColor, String image) {
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(200, 115));
        // Ô ảnh vuông có nền màu nhạt, đặt giữa theo chiều dọc để không bị kéo giãn
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setBackground(lightColor);
        iconPanel.setPreferredSize(new Dimension(56, 56));
        iconPanel.add(new JLabel(icon(image, 40, 40)));
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconPanel);
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_GRAY);
        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblNumber.setForeground(TEXT_DARK);
        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDescription.setForeground(TEXT_GRAY);
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblNumber);
        textPanel.add(lblDescription);
        card.add(iconWrap, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.putClientProperty("countLabel", lblNumber);
        return card;
    }

    private void makeCardClickable(Component component, final int tabIndex) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { if (tabbedPane != null) tabbedPane.setSelectedIndex(tabIndex); }
        });
        if (component instanceof Container) for (Component child : ((Container) component).getComponents()) makeCardClickable(child, tabIndex);
    }

 // Danh sách lịch hẹn
    private JPanel createCardGridPanel() {
        RoundedPanel panel = new RoundedPanel(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Danh sách lịch hẹn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);
        JLabel lblHint = new JLabel("Chọn một thẻ lịch hẹn để xem thông tin chi tiết");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(TEXT_GRAY);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(lblTitle);
        heading.add(Box.createVerticalStrut(4));
        heading.add(lblHint);
        titlePanel.add(heading, BorderLayout.WEST);
        gridPendingContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridPendingContainer.setOpaque(false);
        gridConfirmedContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridConfirmedContainer.setOpaque(false);
        gridHistoryContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridHistoryContainer.setOpaque(false);
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setForeground(TEXT_DARK);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("Chờ xác nhận (0)", createCardScrollPane(gridPendingContainer));
        tabbedPane.addTab("Đã xác nhận (0)", createCardScrollPane(gridConfirmedContainer));
        tabbedPane.addTab("Lịch sử xử lý (0)", createCardScrollPane(gridHistoryContainer));
        // Lắng nghe sự kiện chuyển đổi tab để cập nhật trạng thái các nút bấm bên phải
        tabbedPane.addChangeListener(e -> { if (selectedAppointmentId != null) updateActionButtonsState(); });
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createCardScrollPane(JPanel gridContainer) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CANVAS);
        wrapper.setBorder(new EmptyBorder(5, 5, 5, 5));
        wrapper.add(gridContainer, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

 // Tạo thẻ lịch hẹn
    private JPanel createAppointmentCard(Object[] row) {
        String id = String.valueOf(row[0]), customerName = String.valueOf(row[1]), phone = String.valueOf(row[2]), service = String.valueOf(row[3]);
        String doctor = String.valueOf(row[4]), date = String.valueOf(row[5]), time = String.valueOf(row[6]), status = String.valueOf(row[7]);
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(0, 150));
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setOpaque(false);
        JLabel lblName = new JLabel(customerName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_DARK);
        topBox.add(lblName, BorderLayout.WEST);
        JLabel lblStatusBadge = new JLabel(" " + status + " ");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatusBadge.setOpaque(true);
        if (status.equalsIgnoreCase("Chờ xác nhận")) { lblStatusBadge.setBackground(BLUE_LIGHT); lblStatusBadge.setForeground(BLUE_PRIMARY); }
        else if (status.equalsIgnoreCase("Đã xác nhận")) { lblStatusBadge.setBackground(GREEN_LIGHT); lblStatusBadge.setForeground(GREEN_ACCENT); }
        else if (status.equalsIgnoreCase("Đã hủy")) { lblStatusBadge.setBackground(RED_LIGHT); lblStatusBadge.setForeground(RED_ACCENT); }
        else { lblStatusBadge.setBackground(ORANGE_LIGHT); lblStatusBadge.setForeground(ORANGE_ACCENT); }
        topBox.add(lblStatusBadge, BorderLayout.EAST);
        card.add(topBox, BorderLayout.NORTH);
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);
        centerBox.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel lblDateTime = new JLabel("" + date + " • " + time);
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDateTime.setForeground(TEXT_GRAY);
        // Số điện thoại kèm ảnh phone.png để nhìn thẻ là biết khách nào và tra cứu theo SĐT
        JLabel lblPhone = new JLabel(phone.isEmpty() ? "Chưa có SĐT" : phone, icon("phone.png", 14, 14), SwingConstants.LEFT);
        lblPhone.setIconTextGap(6);
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhone.setForeground(TEXT_GRAY);
        JLabel lblService = new JLabel("" + (service.isEmpty() ? "Khám nha khoa" : service));
        lblService.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblService.setForeground(BLUE_PRIMARY);
        centerBox.add(lblDateTime);
        centerBox.add(Box.createVerticalStrut(3));
        centerBox.add(lblPhone);
        centerBox.add(Box.createVerticalStrut(3));
        centerBox.add(lblService);
        card.add(centerBox, BorderLayout.CENTER);
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);
        JLabel lblDoctor = new JLabel("" + (doctor.isEmpty() ? "Chưa phân công" : doctor));
        lblDoctor.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDoctor.setForeground(TEXT_GRAY);
        bottomBox.add(lblDoctor, BorderLayout.WEST);
        card.add(bottomBox, BorderLayout.SOUTH);
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { fillFormFromData(id, customerName, phone, service, doctor, date, time, status); }
        };
        card.addMouseListener(selectListener);
        for (Component c : topBox.getComponents()) c.addMouseListener(selectListener);
        for (Component c : centerBox.getComponents()) c.addMouseListener(selectListener);
        for (Component c : bottomBox.getComponents()) c.addMouseListener(selectListener);
        return card;
    }

 // Thông tin chi tiết lịch hẹn
    private JPanel createBookingFormPanel() {
        RoundedPanel panel = new RoundedPanel(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 18, 20, 18));
        panel.setPreferredSize(new Dimension(340, 0));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Thông tin lịch hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chi tiết khách hàng và thao tác");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_GRAY);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        JPanel detailCard = new JPanel();
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        detailCard.setBackground(new Color(248, 250, 254));
        detailCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_LIGHT), new EmptyBorder(15, 15, 15, 15)));
        lblDetailName = new JLabel("Chưa chọn lịch hẹn");
        lblDetailName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetailName.setForeground(TEXT_DARK);
        lblDetailPhone = new JLabel("---");
        lblDetailPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailPhone.setForeground(TEXT_GRAY);
        lblDetailPhone.setIconTextGap(6);
        detailCard.add(lblDetailName);
        detailCard.add(Box.createVerticalStrut(4));
        detailCard.add(lblDetailPhone);
        detailCard.add(Box.createVerticalStrut(12));
        detailCard.add(new JSeparator());
        lblDetailDate = addDetailRow(detailCard, "Ngày hẹn:", "---");
        lblDetailService = addDetailRow(detailCard, "Dịch vụ:", "---");
        lblDetailDoctor = addDetailRow(detailCard, "Bác sĩ:", "---");
        lblDetailStatus = addDetailRow(detailCard, "Trạng thái:", "---");
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(detailCard);
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 0, 9));
        buttonPanel.setOpaque(false);
        btnActionPrimary = createButton("Xác nhận lịch hẹn", GREEN_ACCENT);
        btnActionEdit = createButton("Sửa lịch hẹn", ORANGE_ACCENT);
        btnActionCancel = createButton("Hủy lịch hẹn", RED_ACCENT);
        JButton btnClear = createButton("Làm mới lựa chọn", BLUE_PRIMARY);
        btnActionPrimary.addActionListener(e -> performFlowAction());
        btnActionEdit.addActionListener(e -> performEdit());
        btnActionCancel.addActionListener(e -> performDelete());
        btnClear.addActionListener(e -> clearForm());
        buttonPanel.add(btnActionPrimary);
        buttonPanel.add(btnActionEdit);
        buttonPanel.add(btnActionCancel);
        buttonPanel.add(btnClear);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(centerContainer, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel addDetailRow(JPanel panel, String labelText, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel lblKey = new JLabel(labelText);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKey.setForeground(TEXT_GRAY);
        JLabel lblVal = new JLabel(val);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVal.setForeground(TEXT_DARK);
        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createVerticalStrut(6));
        return lblVal;
    }

    // Hiện số điện thoại kèm ảnh phone.png ở phần chi tiết (null = chưa chọn lịch hẹn)
    private void showPhone(String phone) {
        lblDetailPhone.setIcon(phone == null ? null : icon("phone.png", 16, 16));
        lblDetailPhone.setText(phone == null ? "---" : (phone.isEmpty() ? "Chưa có SĐT" : phone));
    }

 // Xử lý xác nhận lịch hẹn
    private void performFlowAction() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int currentTab = tabbedPane.getSelectedIndex();
        boolean success = false;
        if (currentTab == 0) {
            // Tab 0 (Chờ xác nhận): Bấm xác nhận lịch hẹn
            success = lichHenDAO.xacNhanLichHen(selectedAppointmentId);
            if (success) {
                AuditLog.event("UPDATE", "Xác nhận lịch hẹn - mã " + selectedAppointmentId);
                JOptionPane.showMessageDialog(this, "Đã xác nhận lịch hẹn!");
            }
        } else if (currentTab == 1) {
            return;
        } else {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này đã kết thúc xử lý!");
            return;
        }
        if (success) { clearForm(); loadAppointments(); }
    }

    private void updateActionButtonsState() {
        int tabIdx = tabbedPane.getSelectedIndex();
        // Chỉ cho sửa lịch hẹn ở tab Chờ xác nhận và Đã xác nhận
        btnActionEdit.setVisible(tabIdx != 2);
        if (tabIdx == 0) {
            // Tab Chờ xác nhận: Hiện nút xác nhận và nút hủy
            btnActionPrimary.setText("Xác nhận lịch hẹn");
            btnActionPrimary.setEnabled(true);
            btnActionPrimary.setVisible(true);
            btnActionCancel.setVisible(true);
        } else if (tabIdx == 1) {
            // Tab Đã xác nhận:
            btnActionPrimary.setText("Xác nhận lịch hẹn");
            btnActionPrimary.setEnabled(false);
            btnActionPrimary.setVisible(true);
            btnActionCancel.setVisible(false);
        } else {
            // Tab Lịch sử xử lý
            btnActionPrimary.setVisible(false);
            btnActionCancel.setVisible(false);
        }
    }

 // Chỉnh sửa lịch hẹn
    // Sửa ngày, giờ, bác sĩ, dịch vụ của lịch hẹn đang chọn; dữ liệu đọc và ghi trực tiếp trên sql
    private void performEdit() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final String ma = selectedAppointmentId;
        try (Connection c = DBConnection.getConnection()) {
            // Đọc dữ liệu hiện tại của lịch hẹn
            java.sql.Date oldDate; String oldTime, oldDoc, oldNote;
            try (PreparedStatement p = c.prepareStatement("SELECT ngayHen, gioKham, maNhanVien, ghiChu FROM LichHen WHERE maLichHen=?")) {
                p.setString(1, ma);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) { JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn " + ma + " trong cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
                    oldDate = r.getDate(1); oldTime = nz(r.getString(2)); oldDoc = r.getString(3); oldNote = nz(r.getString(4));
                }
            }
            // Danh sách bác sĩ, nha sĩ và dịch vụ lấy từ SQL
            List<String> ids = new ArrayList<>(); JComboBox<String> cbDoc = new JComboBox<>(); ids.add(null); cbDoc.addItem("Chưa phân công");
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT maNhanVien, tenNhanVien FROM NhanVien WHERE chucVu LIKE N'%sĩ%' ORDER BY tenNhanVien")) { while (r.next()) { ids.add(r.getString(1)); cbDoc.addItem(r.getString(2)); } }
            if (ids.indexOf(oldDoc) > 0) cbDoc.setSelectedIndex(ids.indexOf(oldDoc));
            JComboBox<String> cbSv = new JComboBox<>(); cbSv.setEditable(true);
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT tenDichVu FROM DichVu ORDER BY tenDichVu")) { while (r.next()) cbSv.addItem(r.getString(1)); }
            cbSv.setSelectedItem(oldNote);
            JComboBox<String> cbTime = new JComboBox<>(); cbTime.setEditable(true);
            for (int m = 450; m <= 1050; m += 30) cbTime.addItem(String.format("%02d:%02d", m / 60, m % 60)); // khung giờ 07:30 - 17:30
            cbTime.setSelectedItem(oldTime);
            JTextField txtDate = new JTextField(oldDate == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(oldDate));
            txtDate.setEditable(false);
            txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JButton btnCalendar = new JButton("Chọn ngày");
            btnCalendar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCalendar.addActionListener(e -> showDatePicker(txtDate));

            JPanel datePanel = new JPanel(new BorderLayout(5, 0));
            datePanel.add(txtDate, BorderLayout.CENTER);
            datePanel.add(btnCalendar, BorderLayout.EAST);
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6)); form.setPreferredSize(new Dimension(340, 340));
            form.add(new JLabel("Khách hàng: " + lblDetailName.getText()));
            form.add(new JLabel("Ngày hẹn:"));
            form.add(datePanel);
            form.add(new JLabel("Giờ khám (HH:mm):")); form.add(cbTime);
            form.add(new JLabel("Bác sĩ phụ trách:")); form.add(cbDoc);
            form.add(new JLabel("Dịch vụ / ghi chú:")); form.add(cbSv);
            // Nhập sai thì báo lỗi rồi mở lại hộp thoại, giữ nguyên những gì đã nhập
            while (JOptionPane.showConfirmDialog(this, form, "Sửa lịch hẹn - " + ma, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                String err = null, time = val(cbTime), docId = ids.get(cbDoc.getSelectedIndex()); java.sql.Date nd = null;
                try { SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy"); f.setLenient(false); nd = new java.sql.Date(f.parse(txtDate.getText().trim()).getTime()); }
                catch (java.text.ParseException ex) { err = "Ngày hẹn không hợp lệ, vui lòng nhập dạng dd/MM/yyyy!"; }
                if (err == null && !Pattern.matches("([01]\\d|2[0-3]):[0-5]\\d", time)) err = "Giờ khám không hợp lệ, vui lòng nhập dạng HH:mm!";
                if (err == null && nd.before(java.sql.Date.valueOf(java.time.LocalDate.now())) && !nd.equals(oldDate)) err = "Ngày hẹn mới không được nằm trong quá khứ!";
                // Kiểm tra bác sĩ đã có lịch khác cùng ngày và giờ hay chưa
                if (err == null && docId != null) {
                    try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM LichHen WHERE maNhanVien=? AND ngayHen=? AND gioKham=? AND maLichHen<>? AND trangThai<>N'Đã hủy'")) {
                        p.setString(1, docId); p.setDate(2, nd); p.setString(3, time); p.setString(4, ma);
                        try (ResultSet r = p.executeQuery()) { if (r.next() && r.getInt(1) > 0) err = "Bác sĩ đã có lịch hẹn vào khung giờ này, vui lòng chọn giờ hoặc bác sĩ khác!"; }
                    }
                }
                if (err != null) { JOptionPane.showMessageDialog(this, err, "Cảnh báo", JOptionPane.WARNING_MESSAGE); continue; }
                try (PreparedStatement p = c.prepareStatement("UPDATE LichHen SET ngayHen=?, gioKham=?, maNhanVien=?, ghiChu=? WHERE maLichHen=?")) {
                    p.setDate(1, nd); p.setString(2, time);
                    if (docId == null) p.setNull(3, Types.VARCHAR); else p.setString(3, docId);
                    p.setString(4, val(cbSv)); p.setString(5, ma); p.executeUpdate();
                }
                AuditLog.event("UPDATE", "Sửa lịch hẹn - mã " + ma);
                JOptionPane.showMessageDialog(this, "Đã cập nhật lịch hẹn!");
                clearForm(); loadAppointments(); return;
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Lỗi sửa lịch hẹn:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
    }
    private void showDatePicker(JTextField txtDate) {
        LocalDate selectedDate = LocalDate.now();

        try {
            if (!txtDate.getText().trim().isEmpty()) {
                selectedDate = LocalDate.parse(
                    txtDate.getText().trim(),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                );
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

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setOpaque(false);

        JButton btnPrevious = new JButton("<");
        JButton btnNext = new JButton(">");

        JLabel lblMonth = new JLabel("", SwingConstants.CENTER);
        lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblMonth.setForeground(TEXT_DARK);

        monthPanel.add(btnPrevious, BorderLayout.WEST);
        monthPanel.add(lblMonth, BorderLayout.CENTER);
        monthPanel.add(btnNext, BorderLayout.EAST);

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setOpaque(false);

        String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        Runnable updateCalendar = () -> {
            calendarPanel.removeAll();

            for (String day : weekdays) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
                final int selectedDay = day;

                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btnDay.setFocusPainted(false);
                btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));

                LocalDate date = month.atDay(day);

                if (date.equals(LocalDate.now())) {
                    btnDay.setBackground(BLUE_LIGHT);
                    btnDay.setForeground(BLUE_PRIMARY);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
        dialog.setSize(330, 310);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
 // Tải và hiển thị danh sách lịch hẹn
    public void loadAppointments() {
        try {
            allAppointmentsList = lichHenDAO.getAllAppointments();
            renderFilteredAppointments("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách lịch hẹn:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderFilteredAppointments(String keyword) {
        gridPendingContainer.removeAll();
        gridConfirmedContainer.removeAll();
        gridHistoryContainer.removeAll();
        int pendingCount = 0, confirmedCount = 0, historyCount = 0;
        String query = keyword.toLowerCase();
        for (Object[] row : allAppointmentsList) {
            String id = String.valueOf(row[0]), name = String.valueOf(row[1]), phone = String.valueOf(row[2]), status = String.valueOf(row[7]);
            if (!query.isEmpty() && !id.toLowerCase().contains(query) && !name.toLowerCase().contains(query) && !phone.contains(query)) continue;
            JPanel card = createAppointmentCard(row);
            if (status.equalsIgnoreCase("Chờ xác nhận")) { gridPendingContainer.add(card); pendingCount++; }
            else if (status.equalsIgnoreCase("Đã xác nhận")) { gridConfirmedContainer.add(card); confirmedCount++; }
            else { gridHistoryContainer.add(card); historyCount++; }
        }
        if (lblPendingCount != null) lblPendingCount.setText(String.valueOf(pendingCount));
        if (lblConfirmedCount != null) lblConfirmedCount.setText(String.valueOf(confirmedCount));
        if (lblHistoryCount != null) lblHistoryCount.setText(String.valueOf(historyCount));
        if (tabbedPane != null) {
            tabbedPane.setTitleAt(0, "Chờ xác nhận (" + pendingCount + ")");
            tabbedPane.setTitleAt(1, "Đã xác nhận (" + confirmedCount + ")");
            tabbedPane.setTitleAt(2, "Lịch sử xử lý (" + historyCount + ")");
        }
        for (JPanel p : new JPanel[]{gridPendingContainer, gridConfirmedContainer, gridHistoryContainer}) { p.revalidate(); p.repaint(); }
    }

    private void fillFormFromData(String id, String name, String phone, String service, String doctor, String date, String time, String status) {
        selectedAppointmentId = id;
        lblDetailName.setText(name);
        showPhone(phone);
        lblDetailDate.setText(date + " • " + time);
        lblDetailService.setText(service);
        lblDetailDoctor.setText(doctor);
        lblDetailStatus.setText(status);
        updateActionButtonsState();
    }

    private void performDelete() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy lịch hẹn " + selectedAppointmentId + " không?", "Xác nhận hủy lịch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION && lichHenDAO.cancelAppointment(selectedAppointmentId)) {
            AuditLog.event("UPDATE", "Hủy lịch hẹn - mã " + selectedAppointmentId);
            JOptionPane.showMessageDialog(this, "Hủy lịch hẹn thành công!");
            clearForm();
            loadAppointments();
        }
    }

    private void clearForm() {
        selectedAppointmentId = null;
        lblDetailName.setText("Chưa chọn lịch hẹn");
        showPhone(null);
        lblDetailDate.setText("---");
        lblDetailService.setText("---");
        lblDetailDoctor.setText("---");
        lblDetailStatus.setText("---");
        updateActionButtonsState();
    }

    private void performSearch() { renderFilteredAppointments(txtSearch.getText().trim()); }

 // Các hàm hỗ trợ
    private static String nz(String s) { return s == null ? "" : s; }
    // Lấy chữ đang gõ trong combobox cho phép nhập tay
    private static String val(JComboBox<String> cb) { Object o = cb.getEditor().getItem(); return o == null ? "" : o.toString().trim(); }
    // Đọc ảnh từ src/img (trong classpath hoặc thư mục dự án), có lưu đệm để không tải lại
    private static Image img(String name) {
        return IMG_CACHE.computeIfAbsent(name, n -> {
            java.net.URL u = BookingProcessPanel.class.getResource("/img/" + n);
            if (u != null) return new ImageIcon(u).getImage();
            File f = new File("src/img/" + n);
            return f.exists() ? new ImageIcon(f.getAbsolutePath()).getImage() : null;
        });
    }
    private static ImageIcon icon(String n, int w, int h) {
        Image i = img(n);
        return i == null ? new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)) : new ImageIcon(i.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = background;
                if (!isEnabled()) color = new Color(205, 210, 218); // Màu xám mờ hiện đại khi bị disable
                else if (getModel().isPressed()) color = background.darker();
                else if (getModel().isRollover()) color = background.brighter();
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        public PlaceholderTextField(String placeholder) { this.placeholder = placeholder; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(160, 170, 185));
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics(getFont());
                g2.drawString(placeholder, insets.left + 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        public RoundedPanel(int radius, Color backgroundColor) { this.radius = radius; this.backgroundColor = backgroundColor; setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public void refreshData() { loadAppointments(); }
}