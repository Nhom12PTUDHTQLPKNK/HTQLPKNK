package views;

import Audit.AuditLog;
import models.dao.LichHenDAO;
import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

public class BookingProcessPanel extends JPanel {

    // ==================== COLORS ====================
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

    // ==================== CARD CONTAINERS ====================
    private JPanel gridPendingContainer;
    private JPanel gridConfirmedContainer;
    private JPanel gridHistoryContainer;

    private JTabbedPane tabbedPane;
    private JTextField txtSearch;

    private LichHenDAO lichHenDAO;

    // ==================== STATISTICS ====================
    private JLabel lblPendingCount;
    private JLabel lblConfirmedCount;
    private JLabel lblHistoryCount;

    // ==================== SELECTED ITEM DETAILS ====================
    private String selectedAppointmentId = null;
    private JLabel lblDetailName, lblDetailPhone, lblDetailDate, lblDetailService, lblDetailDoctor, lblDetailStatus;
    private JButton btnActionPrimary, btnActionCancel;

    // Lưu trữ toàn bộ dữ liệu thô để lọc tìm kiếm
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

    // ==================== HEADER ====================
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

        txtSearch = new PlaceholderTextField("Nhập tên, SĐT hoặc mã lịch hẹn...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(270, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 12, 5, 12)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createButton("Tìm kiếm", BLUE_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = createButton("Tải lại", new Color(74, 88, 105));
        btnRefresh.setPreferredSize(new Dimension(85, 38));
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadAppointments();
            clearForm();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    // ==================== STATISTICS ====================
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);

        JPanel pendingCard = createStatisticCard("LỊCH CHỜ XÁC NHẬN", "0", "Đang chờ xử lý", BLUE_PRIMARY, BLUE_LIGHT);
        JPanel confirmedCard = createStatisticCard("LỊCH ĐÃ XÁC NHẬN", "0", "Đã được tiếp nhận", GREEN_ACCENT, GREEN_LIGHT);
        JPanel historyCard = createStatisticCard("LỊCH ĐÃ XỬ LÝ", "0", "Hoàn thành hoặc đã hủy", ORANGE_ACCENT, ORANGE_LIGHT);

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

    private JPanel createStatisticCard(String title, String number, String description, Color accent, Color lightColor) {
        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(200, 115));

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(true);
        iconPanel.setBackground(lightColor);
        iconPanel.setPreferredSize(new Dimension(52, 52));

        JLabel icon = new JLabel("🦷");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        iconPanel.add(icon);

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

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.putClientProperty("countLabel", lblNumber);

        return card;
    }

    private void makeCardClickable(Component component, final int tabIndex) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tabbedPane != null) {
                    tabbedPane.setSelectedIndex(tabIndex);
                }
            }
        });
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                makeCardClickable(child, tabIndex);
            }
        }
    }

    // ==================== CARD GRID PANEL (THAY CHO JTABLE) ====================
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

        // Container dạng lưới (Grid 3 cột cho các thẻ card)
        gridPendingContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridPendingContainer.setOpaque(false);

        gridConfirmedContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridConfirmedContainer.setOpaque(false);

        gridHistoryContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridHistoryContainer.setOpaque(false);

        JScrollPane scrollPending = createCardScrollPane(gridPendingContainer);
        JScrollPane scrollConfirmed = createCardScrollPane(gridConfirmedContainer);
        JScrollPane scrollHistory = createCardScrollPane(gridHistoryContainer);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setForeground(TEXT_DARK);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Chờ xác nhận (0)", scrollPending);
        tabbedPane.addTab("Đã xác nhận (0)", scrollConfirmed);
        tabbedPane.addTab("Lịch sử xử lý (0)", scrollHistory);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createCardScrollPane(JPanel gridContainer) {
        // Bọc gridContainer vào một panel bọc phía trên để các card không bị kéo giãn theo chiều dọc
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

    // ==================== TẠO THẺ CARD LỊCH HẸN ====================
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
        card.setPreferredSize(new Dimension(0, 130));

        // Top: Tên khách hàng & Nhãn trạng thái nhỏ
        JPanel topBox = new JPanel(new BorderLayout());
        topBox.setOpaque(false);

        JLabel lblName = new JLabel(customerName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_DARK);
        topBox.add(lblName, BorderLayout.WEST);

        JLabel lblStatusBadge = new JLabel(" " + status + " ");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
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
        topBox.add(lblStatusBadge, BorderLayout.EAST);
        card.add(topBox, BorderLayout.NORTH);

        // Center: Ngày giờ & Dịch vụ
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);
        centerBox.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel lblDateTime = new JLabel("📅 " + date + " • " + time);
        lblDateTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDateTime.setForeground(TEXT_GRAY);

        JLabel lblService = new JLabel("🦷 " + (service.isEmpty() ? "Khám nha khoa" : service));
        lblService.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblService.setForeground(BLUE_PRIMARY);

        centerBox.add(lblDateTime);
        centerBox.add(Box.createVerticalStrut(4));
        centerBox.add(lblService);
        card.add(centerBox, BorderLayout.CENTER);

        // Bottom: Bác sĩ phụ trách & Mũi tên điều hướng
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);

        JLabel lblDoctor = new JLabel("👨‍⚕️ " + (doctor.isEmpty() ? "Chưa phân công" : doctor));
        lblDoctor.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblDoctor.setForeground(TEXT_GRAY);
        bottomBox.add(lblDoctor, BorderLayout.WEST);

        JLabel lblArrow = new JLabel("➔");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblArrow.setForeground(BLUE_PRIMARY);
        bottomBox.add(lblArrow, BorderLayout.EAST);

        card.add(bottomBox, BorderLayout.SOUTH);

        // Sự kiện click chọn card hiển thị sang panel chi tiết bên phải
        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillFormFromData(id, customerName, phone, service, doctor, date, time, status);
            }
        };
        card.addMouseListener(selectListener);
        for (Component c : topBox.getComponents()) c.addMouseListener(selectListener);
        for (Component c : centerBox.getComponents()) c.addMouseListener(selectListener);
        for (Component c : bottomBox.getComponents()) c.addMouseListener(selectListener);

        return card;
    }

    // ==================== BOOKING FORM (CHI TIẾT BÊN PHẢI) ====================
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
        detailCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(15, 15, 15, 15)
        ));

        lblDetailName = new JLabel("Chưa chọn lịch hẹn");
        lblDetailName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetailName.setForeground(TEXT_DARK);

        lblDetailPhone = new JLabel("📞 ---");
        lblDetailPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailPhone.setForeground(TEXT_GRAY);

        detailCard.add(lblDetailName);
        detailCard.add(Box.createVerticalStrut(4));
        detailCard.add(lblDetailPhone);
        detailCard.add(Box.createVerticalStrut(12));
        detailCard.add(new JSeparator());

        lblDetailDate = addDetailRow(detailCard, "📅 Ngày hẹn:", "---");
        lblDetailService = addDetailRow(detailCard, "🦷 Dịch vụ:", "---");
        lblDetailDoctor = addDetailRow(detailCard, "👨‍⚕️ Bác sĩ:", "---");
        lblDetailStatus = addDetailRow(detailCard, "📌 Trạng thái:", "---");

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);
        centerContainer.add(detailCard);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 9));
        buttonPanel.setOpaque(false);

        btnActionPrimary = createButton("Xác nhận lịch hẹn", GREEN_ACCENT);
        btnActionCancel = createButton("Hủy lịch hẹn", RED_ACCENT);
        JButton btnClear = createButton("Làm mới lựa chọn", BLUE_PRIMARY);

        btnActionPrimary.addActionListener(e -> performFlowAction());
        btnActionCancel.addActionListener(e -> performDelete());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnActionPrimary);
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

    // ==================== LUỒNG CHUYỂN TRẠNG THÁI ====================
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
                JOptionPane.showMessageDialog(this, "Đã chuyển lịch hẹn sang trạng thái [Đã xác nhận]!");
            }
        } else if (currentTab == 1) {
            success = lichHenDAO.updateStatus(selectedAppointmentId, "Hoàn thành");
            if (success) {
                AuditLog.event("UPDATE", "Hoàn thành lịch khám - mã " + selectedAppointmentId);
                JOptionPane.showMessageDialog(this, "Đã hoàn thành lịch hẹn!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này đã kết thúc xử lý!");
            return;
        }

        if (success) {
            clearForm();
            loadAppointments();
        }
    }

    // ==================== LOAD & RENDER APPOINTMENTS ====================
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

        int pendingCount = 0;
        int confirmedCount = 0;
        int historyCount = 0;

        String query = keyword.toLowerCase();

        for (Object[] row : allAppointmentsList) {
            String id = String.valueOf(row[0]);
            String name = String.valueOf(row[1]);
            String phone = String.valueOf(row[2]);
            String status = String.valueOf(row[7]);

            // Lọc theo từ khóa tìm kiếm
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

        // Cập nhật số liệu trên thẻ thống kê và tab
        if (lblPendingCount != null) lblPendingCount.setText(String.valueOf(pendingCount));
        if (lblConfirmedCount != null) lblConfirmedCount.setText(String.valueOf(confirmedCount));
        if (lblHistoryCount != null) lblHistoryCount.setText(String.valueOf(historyCount));

        if (tabbedPane != null) {
            tabbedPane.setTitleAt(0, "Chờ xác nhận (" + pendingCount + ")");
            tabbedPane.setTitleAt(1, "Đã xác nhận (" + confirmedCount + ")");
            tabbedPane.setTitleAt(2, "Lịch sử xử lý (" + historyCount + ")");
        }

        gridPendingContainer.revalidate();
        gridPendingContainer.repaint();
        gridConfirmedContainer.revalidate();
        gridConfirmedContainer.repaint();
        gridHistoryContainer.revalidate();
        gridHistoryContainer.repaint();
    }

    private void fillFormFromData(String id, String name, String phone, String service, String doctor, String date, String time, String status) {
        selectedAppointmentId = id;

        lblDetailName.setText(name);
        lblDetailPhone.setText("📞 " + phone);
        lblDetailDate.setText(date + " • " + time);
        lblDetailService.setText(service);
        lblDetailDoctor.setText(doctor);
        lblDetailStatus.setText(status);

        int tabIdx = tabbedPane.getSelectedIndex();
        if (tabIdx == 0) {
            btnActionPrimary.setText("Xác nhận lịch hẹn");
            btnActionPrimary.setVisible(true);
        } else if (tabIdx == 1) {
            btnActionPrimary.setText("Hoàn thành khám");
            btnActionPrimary.setVisible(true);
        } else {
            btnActionPrimary.setVisible(false);
        }
    }

    private void performDelete() {
        if (selectedAppointmentId == null || selectedAppointmentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thẻ lịch hẹn cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy lịch hẹn " + selectedAppointmentId + " không?", "Xác nhận hủy lịch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (lichHenDAO.cancelAppointment(selectedAppointmentId)) {
                AuditLog.event("UPDATE", "Hủy lịch hẹn - mã " + selectedAppointmentId);
                JOptionPane.showMessageDialog(this, "Hủy lịch hẹn thành công!");
                clearForm();
                loadAppointments();
            }
        }
    }

    private void clearForm() {
        selectedAppointmentId = null;
        lblDetailName.setText("Chưa chọn lịch hẹn");
        lblDetailPhone.setText("📞 ---");
        lblDetailDate.setText("---");
        lblDetailService.setText("---");
        lblDetailDoctor.setText("---");
        lblDetailStatus.setText("---");
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        renderFilteredAppointments(keyword);
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = getModel().isPressed() ? background.darker() : (getModel().isRollover() ? background.brighter() : background);
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
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, insets.left + 2, y);
                g2.dispose();
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }
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

    public void refreshData() {
        loadAppointments();
    }
}