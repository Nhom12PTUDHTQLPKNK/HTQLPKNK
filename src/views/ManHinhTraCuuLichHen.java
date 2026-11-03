package views;


import models.dao.LichHenDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.SQLException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ManHinhTraCuuLichHen extends JPanel {

    // Màu sắc giao diện
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color ORANGE = new Color(217, 119, 6);
    private static final Color RED = new Color(220, 38, 38);

    // Danh sách các trang tra cứu đang mở, dùng để báo cập nhật khi nơi khác sửa lịch hẹn
    private static final List<ManHinhTraCuuLichHen> INSTANCES = new ArrayList<>();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JLabel lblTotal;
    private JLabel lblUrgent;
    private JLabel lblCompletion;
    private JLabel lblResultCount;

    private JComboBox<String> cboDateFilter;
    private JSpinner spFromDate;
    private JSpinner spToDate;

    // Nút lọc "Tất cả" để đưa bộ lọc về mặc định khi bấm Tải lại
    private JToggleButton statusAllButton;

    private JPanel detailContent;
    private JPanel detailHost;
    private JPanel detailPlaceholder;
    private JScrollPane detailScroll;
    private Timer detailAnimation;

    private JLabel lblDetailHint;
    private JLabel lblDetailName;
    private JLabel lblDetailPhone;
    private JLabel lblDetailDoctor;
    private JLabel lblDetailService;
    private JLabel lblDetailId;
    private JLabel lblTimeline;

    private String selectedStatus = "Tất cả";
    private String selectedAppointmentId;

    private final Set<String> remindedAppointments = new HashSet<>();

    public ManHinhTraCuuLichHen() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        topContainer.add(createHeaderPanel());
        topContainer.add(Box.createVerticalStrut(12));
        topContainer.add(createDashboardPanel());
        topContainer.add(Box.createVerticalStrut(12));
        topContainer.add(createFilterPanel());

        add(topContainer, BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadAppointments();

        // Đăng ký trang này để nhận thông báo khi lịch hẹn thay đổi ở màn hình khác
        INSTANCES.add(this);
    }

    // Các màn hình khác (vd: ManHinhXuLyLichHen) gọi hàm này sau khi thêm/sửa/xác nhận/hủy lịch hẹn
    public static void notifyAppointmentChanged() {
        for (ManHinhTraCuuLichHen panel : new ArrayList<>(INSTANCES)) {
            SwingUtilities.invokeLater(panel::loadAppointments);
        }
    }

    // Tải hình ảnh từ thư mục src/img
    private ImageIcon loadImage(String fileName, int width, int height) {
        java.net.URL imageUrl = getClass().getResource("/img/" + fileName);

        if (imageUrl == null) {
            System.err.println("Không tìm thấy hình ảnh: src/img/" + fileName);
            return null;
        }

        ImageIcon original = new ImageIcon(imageUrl);
        Image scaled = original.getImage().getScaledInstance(
                width, height, Image.SCALE_SMOOTH);

        return new ImageIcon(scaled);
    }

    // Tạo nhãn hình ảnh
    private JLabel createImageLabel(String fileName, int width, int height) {
        JLabel label = new JLabel();
        ImageIcon icon = loadImage(fileName, width, height);

        if (icon != null) {
            label.setIcon(icon);
        }

        label.setPreferredSize(new Dimension(width, height));
        label.setMinimumSize(new Dimension(width, height));
        label.setMaximumSize(new Dimension(width, height));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        return label;
    }

    // Tạo header
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(255, 232, 243),
                        getWidth(), 0, new Color(219, 234, 254));

                g2.setPaint(gradient);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(
                        0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

                g2.dispose();
            }
        };

        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout(15, 0));
        headerPanel.setBorder(new EmptyBorder(13, 18, 13, 18));
        headerPanel.setPreferredSize(new Dimension(0, 88));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 13, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = createImageLabel("logo5.png", 50, 50);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Tra Cứu Lịch Hẹn Khám");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel(
                "Theo dõi, lọc và xem nhanh hồ sơ lịch hẹn");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        leftPanel.add(logoLabel);
        leftPanel.add(titleBox);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        searchPanel.setOpaque(false);

        txtSearch = new JTextField(17);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setPreferredSize(new Dimension(190, 35));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(4, 9, 4, 9)));

        JButton btnSearch = createButton("Tra cứu", BLUE_PRIMARY, 88);
        btnSearch.addActionListener(e -> applyFilters());

        // Nút tải lại nằm cạnh nút tra cứu: xóa bộ lọc và tải mới dữ liệu
        JButton btnRefresh = createButton("Tải lại", new Color(74, 88, 105), 88);
        btnRefresh.addActionListener(e -> resetPage());

        txtSearch.addActionListener(e -> applyFilters());

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchLabel.setForeground(TEXT_DARK);

        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        headerPanel.add(searchPanel, BorderLayout.EAST);

        return headerPanel;
    }

    // Làm mới lại cả trang: xóa từ khóa, đưa bộ lọc về mặc định, bỏ chọn hồ sơ rồi tải lại dữ liệu
    private void resetPage() {
        txtSearch.setText("");

        selectedStatus = "Tất cả";
        if (statusAllButton != null) {
            statusAllButton.setSelected(true);
        }

        if (cboDateFilter != null) {
            cboDateFilter.setSelectedIndex(0);
        }

        table.clearSelection();
        resetDetailPanel();
        loadAppointments();
    }

    // Đưa khu vực hồ sơ bên phải về trạng thái chưa chọn (hiện logo lớn)
    private void resetDetailPanel() {
        selectedAppointmentId = null;

        if (detailAnimation != null && detailAnimation.isRunning()) {
            detailAnimation.stop();
        }

        lblDetailHint.setText("Chọn một lịch hẹn trong bảng để xem chi tiết");

        detailHost.removeAll();
        detailHost.add(detailPlaceholder, BorderLayout.CENTER);
        detailHost.revalidate();
        detailHost.repaint();
    }

    // Sau khi tải lại dữ liệu: nếu hồ sơ đang xem vẫn còn thì chọn lại và cập nhật thông tin mới
    private void restoreSelectionAfterReload() {
        if (selectedAppointmentId == null) {
            return;
        }

        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);

            if (selectedAppointmentId.equals(
                    String.valueOf(tableModel.getValueAt(modelRow, 0)))) {

                table.setRowSelectionInterval(viewRow, viewRow);
                showAppointmentDetail(modelRow);
                return;
            }
        }

        // Không còn trong danh sách đang hiển thị thì đóng hồ sơ
        resetDetailPanel();
    }

    // Tạo ba thẻ thống kê
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setOpaque(false);

        lblTotal = new JLabel("0");
        lblUrgent = new JLabel("0");
        lblCompletion = new JLabel("0%");

        panel.add(createStatCard(
                "Lịch hẹn tìm thấy",
                lblTotal,
                "Theo từ khóa và bộ lọc hiện tại",
                "ok.png",
                new Color(232, 242, 255),
                BLUE_PRIMARY));

        panel.add(createStatCard(
                "Cần xử lý trong ngày",
                lblUrgent,
                "Chờ xác nhận hoặc đã xác nhận",
                "ca-cancel.png",
                new Color(255, 246, 229),
                ORANGE));

        panel.add(createStatCard(
                "Tỷ lệ hoàn thành",
                lblCompletion,
                "Trên danh sách đang hiển thị",
                "yes.png",
                new Color(230, 248, 238),
                GREEN));

        return panel;
    }

    // Tạo từng thẻ thống kê có hình ảnh
    private JPanel createStatCard(
            String title,
            JLabel value,
            String description,
            String imageName,
            Color background,
            Color accent) {

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel image = createImageLabel(imageName, 48, 48);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 27));
        value.setForeground(accent);

        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDescription.setForeground(MUTED);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(value);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(lblDescription);

        card.add(image, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // Tạo hàng lọc trạng thái và thời gian
    private JPanel createFilterPanel() {
        JPanel outer = new JPanel(new BorderLayout(8, 7));
        outer.setOpaque(false);

        JPanel statusPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 7, 0));
        statusPanel.setOpaque(false);

        String[] statuses = {
                "Tất cả",
                "Chờ xác nhận",
                "Đã xác nhận",
                "Hoàn thành",
                "Đã hủy"
        };

        ButtonGroup group = new ButtonGroup();

        for (String status : statuses) {
            JToggleButton button = new JToggleButton(status);

            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT),
                    new EmptyBorder(6, 11, 6, 11)));

            button.setBackground(Color.WHITE);
            button.setForeground(MUTED);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setSelected(status.equals("Tất cả"));

            // Giữ lại nút "Tất cả" để dùng cho chức năng Tải lại
            if (status.equals("Tất cả")) {
                statusAllButton = button;
            }

            group.add(button);
            statusPanel.add(button);

            button.addActionListener(e -> {
                selectedStatus = status;
                applyFilters();
            });
        }

        JPanel datePanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0));
        datePanel.setOpaque(false);

        cboDateFilter = new JComboBox<>(new String[]{
                "Tất cả thời gian",
                "Hôm nay",
                "Tuần này",
                "Tùy chọn từ - đến"
        });

        cboDateFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        spFromDate = createDateSpinner();
        spToDate = createDateSpinner();

        spFromDate.setPreferredSize(new Dimension(112, 29));
        spToDate.setPreferredSize(new Dimension(112, 29));

        JLabel fromLabel = new JLabel("Từ");
        JLabel toLabel = new JLabel("Đến");

        fromLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        datePanel.add(cboDateFilter);
        datePanel.add(fromLabel);
        datePanel.add(spFromDate);
        datePanel.add(toLabel);
        datePanel.add(spToDate);

        cboDateFilter.addActionListener(e -> {
            boolean custom = cboDateFilter.getSelectedIndex() == 3;

            spFromDate.setEnabled(custom);
            spToDate.setEnabled(custom);

            applyFilters();
        });

        spFromDate.addChangeListener(e -> applyFilters());
        spToDate.addChangeListener(e -> applyFilters());

        spFromDate.setEnabled(false);
        spToDate.setEnabled(false);

        outer.add(statusPanel, BorderLayout.WEST);
        outer.add(datePanel, BorderLayout.EAST);

        return outer;
    }

    // Tạo bộ chọn ngày
    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());

        spinner.setEditor(
                new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));

        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        return spinner;
    }

    // Chia màn hình thành bảng kết quả và hồ sơ chi tiết
    private JComponent createMainContent() {
        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);

        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(10, 10, 10, 10)));

        JPanel tableTitle = new JPanel(new BorderLayout());
        tableTitle.setOpaque(false);

        JLabel title = new JLabel("Danh sách kết quả tra cứu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        lblResultCount = new JLabel("0 kết quả");
        lblResultCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblResultCount.setForeground(MUTED);

        tableTitle.add(title, BorderLayout.WEST);
        tableTitle.add(lblResultCount, BorderLayout.EAST);

        String[] columns = {
                "Mã lịch hẹn",
                "Mã KH",
                "Tên khách hàng",
                "Số điện thoại",
                "Dịch vụ khám",
                "Nha sĩ phụ trách",
                "Ngày hẹn",
                "Giờ hẹn",
                "Trạng thái"
        };

        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        table.getColumnModel()
                .getColumn(8)
                .setCellRenderer(new BoVeTrangThai());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.getSelectedRow();

                if (viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    showAppointmentDetail(modelRow);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(tableTitle, BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel detailCard = createDetailPanel();

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tableCard,
                detailCard);

        split.setResizeWeight(0.70);
        split.setDividerLocation(0.70);
        split.setDividerSize(7);
        split.setBorder(null);
        split.setOpaque(false);
        split.setContinuousLayout(true);

        main.add(split, BorderLayout.CENTER);

        return main;
    }

    // Tạo khu vực hồ sơ lịch hẹn
    private JPanel createDetailPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));

        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(13, 13, 13, 13)));

        card.setPreferredSize(new Dimension(325, 400));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel("HỒ SƠ LỊCH HẸN");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetailHint = new JLabel(
                "Chọn một lịch hẹn trong bảng để xem chi tiết");

        lblDetailHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDetailHint.setForeground(MUTED);
        lblDetailHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(heading);
        top.add(Box.createVerticalStrut(6));
        top.add(lblDetailHint);

        // Khu vực hiển thị logo khi chưa chọn lịch hẹn
        detailPlaceholder = new JPanel();
        detailPlaceholder.setBackground(Color.WHITE);
        detailPlaceholder.setLayout(new BoxLayout(
                detailPlaceholder, BoxLayout.Y_AXIS));

        detailPlaceholder.add(Box.createVerticalGlue());

        JLabel largeLogo = createImageLabel("logo1.png", 175, 175);
        largeLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel placeholderText = new JLabel("Chưa có lịch hẹn được chọn");
        placeholderText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        placeholderText.setForeground(MUTED);
        placeholderText.setAlignmentX(Component.CENTER_ALIGNMENT);

        detailPlaceholder.add(largeLogo);
        detailPlaceholder.add(Box.createVerticalStrut(12));
        detailPlaceholder.add(placeholderText);

        detailPlaceholder.add(Box.createVerticalGlue());

        // Nội dung chi tiết khách hàng
        detailContent = new JPanel();
        detailContent.setOpaque(false);
        detailContent.setLayout(new BoxLayout(
                detailContent, BoxLayout.Y_AXIS));

        JPanel customerCard = createInnerCard();
        JPanel avatarRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 3));

        avatarRow.setOpaque(false);

        JLabel avatar = new JLabel("KH", SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(225, 240, 255));
        avatar.setForeground(BLUE_PRIMARY);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        avatar.setPreferredSize(new Dimension(43, 43));

        JPanel customerInfo = new JPanel();
        customerInfo.setOpaque(false);
        customerInfo.setLayout(new BoxLayout(
                customerInfo, BoxLayout.Y_AXIS));

        lblDetailName = new JLabel("Chưa chọn khách hàng");
        lblDetailName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailName.setForeground(TEXT_DARK);

        lblDetailPhone = new JLabel("SĐT: --");
        lblDetailPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDetailPhone.setForeground(MUTED);

        customerInfo.add(lblDetailName);
        customerInfo.add(Box.createVerticalStrut(4));
        customerInfo.add(lblDetailPhone);

        avatarRow.add(avatar);
        avatarRow.add(customerInfo);

        customerCard.add(avatarRow, BorderLayout.CENTER);

        // Thẻ thông tin nha khoa
        JPanel dentalCard = createInnerCard();
        dentalCard.setLayout(new BoxLayout(
                dentalCard, BoxLayout.Y_AXIS));

        JLabel dentalTitle = sectionLabel("THÔNG TIN NHA KHOA");

        lblDetailDoctor = detailLabel("Nha sĩ phụ trách: --");
        lblDetailService = detailLabel("Dịch vụ: --");

        JPanel doctorRow = createImageTextRow(
                "logo5.png",
                25,
                25,
                lblDetailDoctor);

        JPanel serviceRow = createImageTextRow(
                "logo3.png",
                25,
                25,
                lblDetailService);

        dentalCard.add(dentalTitle);
        dentalCard.add(Box.createVerticalStrut(10));
        dentalCard.add(doctorRow);
        dentalCard.add(Box.createVerticalStrut(9));
        dentalCard.add(serviceRow);

        // Thẻ tiến trình lịch hẹn
        JPanel timelineCard = createInnerCard();
        timelineCard.setLayout(new BoxLayout(
                timelineCard, BoxLayout.Y_AXIS));

        JLabel timelineTitle = sectionLabel("TIẾN TRÌNH LỊCH HẸN");

        lblTimeline = detailLabel(
                "Đặt lịch  →  Đã xác nhận  →  Đang khám  →  Hoàn thành");

        timelineCard.add(timelineTitle);
        timelineCard.add(Box.createVerticalStrut(9));
        timelineCard.add(lblTimeline);

        lblDetailId = detailLabel("Mã lịch hẹn: --");

        JButton btnInstruction = createButton(
                "Xem dặn dò trước khám",
                new Color(255, 224, 236),
                220);

        btnInstruction.setForeground(new Color(190, 24, 93));
        btnInstruction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnInstruction.setBorder(BorderFactory.createLineBorder(
                new Color(244, 160, 190), 1));
        btnInstruction.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnInstruction.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 36));

        btnInstruction.addActionListener(e -> showInstructionPopup());

        detailContent.add(customerCard);
        detailContent.add(Box.createVerticalStrut(9));
        detailContent.add(dentalCard);
        detailContent.add(Box.createVerticalStrut(9));
        detailContent.add(timelineCard);
        detailContent.add(Box.createVerticalStrut(9));
        detailContent.add(lblDetailId);
        detailContent.add(Box.createVerticalStrut(10));
        detailContent.add(btnInstruction);

        detailScroll = new JScrollPane(detailContent);
        detailScroll.setBorder(null);
        detailScroll.setOpaque(false);
        detailScroll.getViewport().setOpaque(false);
        detailScroll.getVerticalScrollBar().setUnitIncrement(14);

        // Ban đầu chỉ hiển thị logo lớn
        detailHost = new JPanel(new BorderLayout());
        detailHost.setOpaque(false);
        detailHost.add(detailPlaceholder, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(detailHost, BorderLayout.CENTER);

        return card;
    }

    // Tạo một hàng gồm hình ảnh và nội dung
    private JPanel createImageTextRow(
            String imageName,
            int imageWidth,
            int imageHeight,
            JLabel contentLabel) {

        JPanel row = new JPanel(new BorderLayout(9, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel image = createImageLabel(
                imageName, imageWidth, imageHeight);

        contentLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        contentLabel.setForeground(TEXT_DARK);

        row.add(image, BorderLayout.WEST);
        row.add(contentLabel, BorderLayout.CENTER);

        return row;
    }

    // Tạo card con cho phần hồ sơ
    private JPanel createInnerCard() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(new Color(249, 251, 255));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(10, 10, 10, 10)));

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    // Tạo nhãn tiêu đề khu vực
    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);

        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(BLUE_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // Tạo nhãn nội dung chi tiết
    private JLabel detailLabel(String text) {
        JLabel label = new JLabel(text);

        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // Lấy danh sách lịch hẹn từ database
    private void loadAppointments() {
        tableModel.setRowCount(0);
        try {
            for (Object[] row : new LichHenDAO().getAppointmentSearchRows()) tableModel.addRow(row);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải danh sách lịch hẹn!\n" + e.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE);
        }

        applyFilters();

        // Giữ nguyên hồ sơ đang xem (nếu còn) và cập nhật thông tin mới nhất
        restoreSelectionAfterReload();
    }

    // Kết hợp lọc từ khóa, trạng thái và thời gian
    private void applyFilters() {
        if (rowSorter == null) {
            return;
        }

        final String keyword = txtSearch == null
                ? ""
                : txtSearch.getText().trim();

        final String status = selectedStatus;

        final int dateMode = cboDateFilter == null
                ? 0
                : cboDateFilter.getSelectedIndex();

        LocalDate from = null;
        LocalDate to = null;

        if (dateMode == 1) {
            from = LocalDate.now();
            to = LocalDate.now();

        } else if (dateMode == 2) {
            from = LocalDate.now().with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            to = from.plusDays(6);

        } else if (dateMode == 3) {
            from = getSpinnerDate(spFromDate);
            to = getSpinnerDate(spToDate);
        }

        final LocalDate finalFrom = from;
        final LocalDate finalTo = to;

        rowSorter.setRowFilter(
                new RowFilter<DefaultTableModel, Integer>() {

            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                // Lọc theo từ khóa trên tất cả cột
                if (!keyword.isEmpty()) {
                    boolean found = false;

                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);

                        if (value != null && Pattern.compile(
                                Pattern.quote(keyword),
                                Pattern.CASE_INSENSITIVE)
                                .matcher(value.toString()).find()) {

                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        return false;
                    }
                }

                // Lọc theo trạng thái
                String rowStatus = String.valueOf(entry.getValue(8));

                if (!"Tất cả".equals(status)
                        && !status.equalsIgnoreCase(rowStatus)) {
                    return false;
                }

                // Lọc theo ngày hẹn
                Object dateValue = entry.getValue(6);

                if (dateValue instanceof Date
                        && finalFrom != null
                        && finalTo != null) {

                    LocalDate appointmentDate =
                            ((Date) dateValue).toLocalDate();

                    if (appointmentDate.isBefore(finalFrom)
                            || appointmentDate.isAfter(finalTo)) {
                        return false;
                    }
                }

                return true;
            }
        });

        updateDashboard();

        lblResultCount.setText(table.getRowCount() + " kết quả");
    }

    // Chuyển ngày từ bộ chọn sang LocalDate
    private LocalDate getSpinnerDate(JSpinner spinner) {
        java.util.Date value = (java.util.Date) spinner.getValue();

        return new Date(value.getTime()).toLocalDate();
    }

    // Cập nhật số liệu thống kê
    private void updateDashboard() {
        int total = table.getRowCount();
        int urgent = 0;
        int completed = 0;

        LocalDate today = LocalDate.now();

        for (int i = 0; i < total; i++) {
            int modelRow = table.convertRowIndexToModel(i);

            String status = String.valueOf(
                    tableModel.getValueAt(modelRow, 8));

            Date date = (Date) tableModel.getValueAt(modelRow, 6);

            if (date != null
                    && date.toLocalDate().equals(today)
                    && ("Chờ xác nhận".equalsIgnoreCase(status)
                    || "Đã xác nhận".equalsIgnoreCase(status))) {

                urgent++;
            }

            if ("Hoàn thành".equalsIgnoreCase(status)) {
                completed++;
            }
        }

        int percent = total == 0
                ? 0
                : (int) Math.round(completed * 100.0 / total);

        lblTotal.setText(String.valueOf(total));
        lblUrgent.setText(String.valueOf(urgent));
        lblCompletion.setText(percent + "%");
    }

    // Hiển thị hồ sơ khi chọn một lịch hẹn
    private void showAppointmentDetail(int modelRow) {
        selectedAppointmentId = String.valueOf(
                tableModel.getValueAt(modelRow, 0));

        String customerName = String.valueOf(
                tableModel.getValueAt(modelRow, 2));

        String phone = String.valueOf(
                tableModel.getValueAt(modelRow, 3));

        String service = String.valueOf(
                tableModel.getValueAt(modelRow, 4));

        String doctor = String.valueOf(
                tableModel.getValueAt(modelRow, 5));

        String status = String.valueOf(
                tableModel.getValueAt(modelRow, 8));

        lblDetailHint.setText("Thông tin lịch hẹn đang được chọn");

        lblDetailName.setText(customerName);
        lblDetailPhone.setText("SĐT: " + phone);

        lblDetailDoctor.setText("Nha sĩ phụ trách: " + doctor);
        lblDetailService.setText("Dịch vụ: " + service);

        lblDetailId.setText(
                "Mã lịch hẹn: " + selectedAppointmentId);

        lblTimeline.setText(buildTimeline(status));

        // Chuyển từ logo chờ sang nội dung hồ sơ
        if (detailHost.getComponentCount() == 0
                || detailHost.getComponent(0) != detailScroll) {

            detailHost.removeAll();
            detailHost.add(detailScroll, BorderLayout.CENTER);
        }

        animateDetailPanel();

        detailHost.revalidate();
        detailHost.repaint();
    }

    // Tạo hiệu ứng hiển thị hồ sơ
    private void animateDetailPanel() {
        if (detailAnimation != null
                && detailAnimation.isRunning()) {

            detailAnimation.stop();
        }

        int width = Math.max(250, detailHost.getWidth());

        detailContent.setPreferredSize(
                new Dimension(width, 0));

        final int[] currentHeight = {0};

        int targetHeight = detailContent.getPreferredSize().height;

        detailAnimation = new Timer(12, e -> {
            currentHeight[0] += 24;

            if (currentHeight[0] >= targetHeight) {
                currentHeight[0] = targetHeight;
                detailAnimation.stop();
            }

            detailContent.setPreferredSize(
                    new Dimension(width, currentHeight[0]));

            detailContent.revalidate();
            detailContent.repaint();
        });

        detailAnimation.start();
    }

    // Tạo tiến trình trạng thái theo trạng thái hiện tại
    private String buildTimeline(String status) {
        if ("Đã hủy".equalsIgnoreCase(status)) {
            return "<html>Đặt lịch  →  "
                    + "<font color='red'><b>Đã hủy</b></font></html>";
        }

        String[] steps = {
                "Đặt lịch",
                "Đã xác nhận",
                "Đang khám",
                "Hoàn thành"
        };

        int active = 0;

        if ("Chờ xác nhận".equalsIgnoreCase(status)) {
            active = 0;

        } else if ("Đã xác nhận".equalsIgnoreCase(status)) {
            active = 1;

        } else if ("Đang khám".equalsIgnoreCase(status)) {
            active = 2;

        } else if ("Hoàn thành".equalsIgnoreCase(status)) {
            active = 3;
        }

        StringBuilder html = new StringBuilder("<html>");

        for (int i = 0; i < steps.length; i++) {
            if (i > 0) {
                html.append("  →  ");
            }

            if (i <= active) {
                html.append("<font color='#16803C'><b>")
                        .append(steps[i])
                        .append("</b></font>");
            } else {
                html.append("<font color='#9AA7B8'>")
                        .append(steps[i])
                        .append("</font>");
            }
        }

        html.append("</html>");

        return html.toString();
    }

    // Mở cửa sổ dặn dò trước khi khám
    private void showInstructionPopup() {
        if (selectedAppointmentId == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn một lịch hẹn trước.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        String service = String.valueOf(
                tableModel.getValueAt(modelRow, 4));

        String customer = String.valueOf(
                tableModel.getValueAt(modelRow, 2));

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Dặn dò & chuẩn bị trước khi khám",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(18, 20, 15, 20));

        content.setLayout(new BoxLayout(
                content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(
                "PHIẾU THÔNG TIN TRƯỚC KHI KHÁM");

        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(BLUE_PRIMARY);

        JLabel customerLabel = new JLabel(
                "Khách hàng: " + customer);
        customerLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12));
        JLabel serviceLabel = new JLabel(
                "Dịch vụ: " + service);
        serviceLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(customerLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(serviceLabel);
        content.add(Box.createVerticalStrut(14));

        content.add(createInstructionSection(
                "LƯU Ý Y TẾ",
                "Các yêu cầu ăn uống, thuốc đang sử dụng và chuẩn bị trước thủ thuật "
                + "phải được nha sĩ xác nhận theo tình trạng riêng của khách hàng."));

        content.add(Box.createVerticalStrut(9));

        content.add(createInstructionSection(
                "GIẤY TỜ NÊN MANG",
                "• Hồ sơ khám hoặc phim X-quang gần nhất (nếu có).\n"
                + "• Danh sách thuốc đang sử dụng và thông tin dị ứng đã biết."));

        content.add(Box.createVerticalStrut(9));

        content.add(createInstructionSection(
                "THÔNG TIN SỨC KHỎE CẦN TRAO ĐỔI",
                "• Tiền sử bệnh lý, dị ứng thuốc hoặc phản ứng với thuốc tê.\n"
                + "• Tình trạng sức khỏe hiện tại và các thuốc đang sử dụng.\n"
                + "• Nha sĩ sẽ đánh giá và hướng dẫn riêng trước khi thực hiện dịch vụ."));

        content.add(Box.createVerticalStrut(12));

        JLabel warning = new JLabel(
                "<html><b>Lưu ý:</b> Đây là checklist nhắc việc chung, "
                + "không thay thế chỉ định hoặc dặn dò trực tiếp của nha sĩ.</html>");

        warning.setFont(
                new Font("Segoe UI", Font.ITALIC, 13));

        warning.setForeground(new Color(180, 83, 9));

        content.add(warning);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);

        dialog.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 10));

        buttons.setBackground(Color.WHITE);
        JButton btnReminded = createButton(
                "Đánh dấu đã dặn dò", GREEN, 155);
        JButton btnClose = createButton(
                "Đóng", MUTED, 75);

        btnReminded.addActionListener(e -> {
            remindedAppointments.add(selectedAppointmentId);

            JOptionPane.showMessageDialog(
                    dialog,
                    "Đã đánh dấu lịch hẹn "
                    + selectedAppointmentId
                    + " là đã dặn dò trong phiên làm việc này.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);

            dialog.dispose();
        });

        btnClose.addActionListener(e -> dialog.dispose());

        buttons.add(btnReminded);
        buttons.add(btnClose);

        dialog.add(buttons, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Tạo từng mục nội dung trong phiếu dặn dò
    private JPanel createInstructionSection(
            String title,
            String description) {

        JPanel panel = new JPanel();

        panel.setOpaque(true);
        panel.setBackground(new Color(248, 250, 253));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(9, 11, 9, 11)));

        panel.setLayout(new BoxLayout(
                panel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(BLUE_PRIMARY);

        JTextArea text = new JTextArea(description);

        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(TEXT_DARK);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setOpaque(false);
        text.setFocusable(false);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(5));
        panel.add(text);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    // Tạo nút bo góc dùng chung
    private JButton createButton(
            String text,
            Color background,
            int width) {

        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);

        button.setPreferredSize(new Dimension(width, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Định dạng bảng kết quả
    private void styleTable(JTable t) {
        t.setRowHeight(42);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_DARK);
        t.setGridColor(new Color(238, 242, 247));

        t.setSelectionBackground(new Color(225, 240, 255));
        t.setSelectionForeground(TEXT_DARK);

        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);

        t.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        t.getTableHeader().setBackground(
                new Color(245, 248, 253));

        t.getTableHeader().setForeground(TEXT_DARK);
        t.getTableHeader().setPreferredSize(
                new Dimension(0, 34));

        t.getTableHeader().setReorderingAllowed(false);

        // Hiển thị ngày theo định dạng Việt Nam
        t.getColumnModel().getColumn(6)
                .setCellRenderer(new DefaultTableCellRenderer() {

            private final SimpleDateFormat format =
                    new SimpleDateFormat("dd/MM/yyyy");

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);

                if (value instanceof java.util.Date) {
                    setText(format.format((java.util.Date) value));
                }

                return this;
            }
        });
    }

    // Tạo badge màu cho trạng thái lịch hẹn
    private class BoVeTrangThai extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String status = value == null
                    ? ""
                    : value.toString();

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(TEXT_DARK);

            } else if ("Hoàn thành".equalsIgnoreCase(status)) {
                label.setBackground(new Color(220, 252, 231));
                label.setForeground(new Color(22, 101, 52));

            } else if ("Đã xác nhận".equalsIgnoreCase(status)) {
                label.setBackground(new Color(219, 234, 254));
                label.setForeground(new Color(29, 78, 216));

            } else if ("Chờ xác nhận".equalsIgnoreCase(status)) {
                label.setBackground(new Color(254, 243, 199));
                label.setForeground(new Color(146, 64, 14));

            } else if ("Đã hủy".equalsIgnoreCase(status)) {
                label.setBackground(new Color(254, 226, 226));
                label.setForeground(new Color(153, 27, 27));

            } else {
                label.setBackground(new Color(241, 245, 249));
                label.setForeground(MUTED);
            }

            return label;
        }
    }
}
