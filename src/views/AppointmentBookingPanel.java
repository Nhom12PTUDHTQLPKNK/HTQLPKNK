package views;

import controllers.AuditLog;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AppointmentBookingPanel extends JPanel {

    // màu

    private static final Color BLUE_PRIMARY = new Color(45, 112, 220);
    private static final Color BLUE_LIGHT = new Color(232, 241, 255);

    private static final Color GREEN_ACCENT = new Color(35, 160, 115);
    private static final Color GREEN_LIGHT = new Color(229, 247, 239);

    private static final Color RED_ACCENT = new Color(220, 75, 85);
    private static final Color RED_LIGHT = new Color(255, 235, 237);

    private static final Color ORANGE_ACCENT = new Color(235, 153, 55);
    private static final Color ORANGE_LIGHT = new Color(255, 245, 225);

    private static final Color BG_CANVAS = new Color(244, 248, 253);
    private static final Color CARD_BG = Color.WHITE;

    private static final Color TEXT_DARK = new Color(35, 52, 78);
    private static final Color TEXT_GRAY = new Color(119, 135, 157);
    private static final Color BORDER_LIGHT = new Color(225, 233, 243);
    private static final Color READONLY_BG = new Color(243, 246, 250);

    private static final String STATUS_PENDING = "Chờ xác nhận";

    private static final String[] STANDARD_TIMES = {
            "08:00", "08:30",
            "09:00", "09:30",
            "10:00", "10:30",
            "11:00", "11:30",
            "13:30", "14:00",
            "14:30", "15:00",
            "15:30", "16:00",
            "16:30", "17:00"
    };

    private static final int PAGE_SIZE = 10;


    private static class ServiceItem {
        String id;
        String name;

        ServiceItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class DoctorItem {
        String id;
        String name;

        DoctorItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // dữ liệu form

    private JTextField txtAppId;
    private JTextField txtCustId;
    private JTextField txtPhone;
    private JTextField txtCustName;

    private JComboBox<ServiceItem> cbService;
    private JComboBox<DoctorItem> cbDoctor;
    private JComboBox<String> cbTime;

    private JSpinner spDate;

    private JLabel lblCustomerStatus;
    private JLabel lblDayTableTitle;

    private JPanel timeGridPanel;

    // dl form

    private JTable table;
    private DefaultTableModel modelDayTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;

    private JButton btnPrevious;
    private JButton btnNext;

    private JLabel lblPageInfo;

    // Thống kê nhanh
    private JLabel lblTotalAppointments;
    private JLabel lblPendingAppointments;
    private JLabel lblConfirmedAppointments;

    private int currentPage = 0;
    private int totalPages = 1;

    private int hoveredRow = -1;


    private boolean isDateSelected = false;
    private boolean existingCustomer = false;
    private String existingCustomerId = null;
    private String lastLookedPhone = "";

    private boolean loading = false;

    private Set<String> busyTimes = new HashSet<String>();

    // CONSTRUCTOR 

    public AppointmentBookingPanel() {

        setLayout(new BorderLayout(0, 18));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        content.add(createFormCard(), BorderLayout.WEST);
        content.add(createDayTableCard(), BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        refreshData();
    }

    // refresh dl

    public void refreshData() {

        loading = true;

        try {
            loadServices();
            loadDoctors();

            isDateSelected = false;

        } finally {
            loading = false;
        }

        refreshPreviewIds();
        refreshAvailability();
    }

    //  HEADER

    private JPanel createHeaderPanel() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0)
        );

        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel();

        java.net.URL imageURL =
                getClass().getResource("/img/rang-tiepnhan.png");

        if (imageURL != null) {

            ImageIcon originalIcon = new ImageIcon(imageURL);

            Image scaledImage = originalIcon.getImage()
                    .getScaledInstance(70, 70, Image.SCALE_SMOOTH);

            iconLabel.setIcon(new ImageIcon(scaledImage));

        } else {
            System.err.println("Không tìm thấy ảnh: /img/rang-tiepnhan.png");
        }

        JLabel title = new JLabel("Đặt Lịch Hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT_DARK);

        titleRow.add(iconLabel);
        titleRow.add(title);

        JLabel subtitle = new JLabel(
                "Phòng khám nha khoa Smile Dental"
        );

        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleRow);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);

        return panel;
    }

    // FORM CARD

    private JPanel createFormCard() {

        RoundedPanel card = new RoundedPanel(16, CARD_BG);

        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(420, 0));

        JLabel heading = new JLabel("Thông tin lịch hẹn");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(TEXT_DARK);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // Mã lịch hẹn

        txtAppId = createTextField(false);
        txtCustId = createTextField(false);

        addRow(form, "Mã lịch hẹn", txtAppId);

        // Nhóm khách hàng
        JPanel customerGroup = createGroupPanel("Thông tin khách hàng");

        txtPhone = createTextField(true);
        txtCustName = createTextField(true);

        addRow(customerGroup, "Số điện thoại", txtPhone);

        lblCustomerStatus = new JLabel("Nhập số điện thoại để kiểm tra khách hàng");
        lblCustomerStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCustomerStatus.setOpaque(true);
        lblCustomerStatus.setBorder(new EmptyBorder(7, 9, 7, 9));

        lblCustomerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCustomerStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        customerGroup.add(lblCustomerStatus);
        customerGroup.add(Box.createVerticalStrut(8));
        addRow(customerGroup, "Tên khách hàng", txtCustName);
        form.add(customerGroup);
        form.add(Box.createVerticalStrut(14));

        // Nhóm dịch vụ và lịch hẹn
        JPanel appointmentGroup = createGroupPanel("Thông tin dịch vụ và lịch hẹn");
        cbService = new JComboBox<ServiceItem>();
        cbDoctor = new JComboBox<DoctorItem>();
        cbTime = new JComboBox<String>();

        styleCombo(cbService);
        styleCombo(cbDoctor);
        styleCombo(cbTime);

        addRow(appointmentGroup, "Dịch vụ khám", cbService);
        addRow(appointmentGroup, "Nha sĩ phụ trách", cbDoctor);

        spDate = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        Calendar.DAY_OF_MONTH
                )
        );

        JSpinner.DateEditor dateEditor =new JSpinner.DateEditor(spDate, "dd/MM/yyyy");
        spDate.setEditor(dateEditor);
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addRow(appointmentGroup, "Ngày hẹn", spDate);

        JLabel timeLabel = new JLabel("Chọn giờ hẹn");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(TEXT_DARK);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentGroup.add(timeLabel);
        appointmentGroup.add(Box.createVerticalStrut(7));

        // Lưới giờ trống
        timeGridPanel = new JPanel();
        timeGridPanel.setOpaque(false);
        timeGridPanel.setLayout(new BoxLayout(timeGridPanel, BoxLayout.Y_AXIS));
        JScrollPane timeScroll = new JScrollPane(timeGridPanel);
        timeScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        timeScroll.setPreferredSize(new Dimension(340, 125));
        timeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        timeScroll.setMinimumSize(new Dimension(0, 125));

        timeScroll.getVerticalScrollBar().setUnitIncrement(12);
        appointmentGroup.add(timeScroll);

        // Chú thích màu giờ hẹn

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 7));
        legendPanel.setOpaque(false);
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendPanel.add(createLegendItem(new Color(211, 245, 229), "Còn trống"));
        legendPanel.add(createLegendItem(new Color(255, 225, 228),"Đã đặt"));
        legendPanel.add(createLegendItem(new Color(232, 235, 240),"Không khả dụng"));
        appointmentGroup.add(legendPanel);

        // Combo giờ được giữ lại để phục vụ logic lưu dữ liệu.
        // Người dùng chọn giờ trực tiếp trên lưới.

        cbTime.setVisible(false);
        form.add(appointmentGroup);JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(15);

        // Sự kiện chọn nha sĩ

        cbDoctor.addActionListener(e -> {
            if (!loading) {
                refreshAvailability();
            }
        });

        // Sự kiện chọn ngày
        spDate.addChangeListener(e -> {
            if (!loading) {
                isDateSelected = true;
                refreshAvailability();
            }
        });

        // Tra cứu khách hàng

        txtPhone.addActionListener(e -> lookupCustomer(true));
        txtPhone.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                lookupCustomer(true);
            }
        });

        txtPhone.getDocument().addDocumentListener(
                new DocumentListener() {
                    private void changed() {
                        SwingUtilities.invokeLater(
                                () -> lookupCustomer(false)
                        );
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        changed();
                    }
                }
        );

        // Nút lưu lịch hẹn
        JButton btnSave = createButton("LƯU LỊCH HẸN",new Color(20, 145, 75));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));

        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(20, 145, 75));

        btnSave.setOpaque(true);
        btnSave.setContentAreaFilled(true);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);

        btnSave.setPreferredSize(new Dimension(0, 54));
        btnSave.setMinimumSize(new Dimension(0, 54));
        btnSave.setBorder(new EmptyBorder(12, 20, 12, 20));
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> performSave());

        card.add(heading, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(btnSave, BorderLayout.SOUTH);

        setCustomerBadge("Nhập số điện thoại để kiểm tra khách hàng",
                TEXT_GRAY,
                READONLY_BG);
        return card;
    }

    //  thgian
    private JPanel createLegendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        item.setOpaque(false);

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBackground(color);
        colorBox.setBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 218, 228)));

        JLabel label = new JLabel(text);
        label.setFont(
                new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(TEXT_GRAY);

        item.add(colorBox);
        item.add(label);
        return item;
    }

    // GROUP PANEL 
    private JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel();

        panel.setOpaque(false);
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS));
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),title);

        border.setTitleFont(
                new Font("Segoe UI", Font.BOLD, 12));

        border.setTitleColor(BLUE_PRIMARY);
        panel.setBorder(new CompoundBorder(
                border,
                new EmptyBorder(10, 10, 8, 10)));
        return panel;
    }

    //  TABLE CARD 
    private JPanel createDayTableCard() {

        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        lblDayTableTitle = new JLabel("Tất cả lịch hẹn hiện có");
        lblDayTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDayTableTitle.setForeground(TEXT_DARK);

        // Thanh phía trên
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblDayTableTitle, BorderLayout.WEST);
        JButton btnReload = createOutlineButton("Tải lại");
        btnReload.setPreferredSize(new Dimension(100, 34));

        btnReload.addActionListener(e -> {
            isDateSelected = false;
            currentPage = 0;

            refreshAvailability();
            JOptionPane.showMessageDialog(
                    this,
                    "Đã tải lại danh sách lịch hẹn!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        titlePanel.add(btnReload, BorderLayout.EAST);
        topPanel.add(titlePanel);
        topPanel.add(Box.createVerticalStrut(12));

        // THẺ THỐNG KÊ 

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        statsPanel.setOpaque(false);
        JPanel totalCard = createStatCard(
                "Tổng lịch hẹn",
                "0",
                new Color(45, 112, 220),
                new Color(232, 241, 255),
                "/img/ca-wait.png"
        );

        JPanel pendingCard = createStatCard(
                "Chờ xác nhận",
                "0",new Color(190, 130, 25),
                new Color(255, 245, 225),
                "/img/ca-cancel.png"
        );

        JPanel confirmedCard = createStatCard(
                "Đã xác nhận",
                "0",
                new Color(25, 140, 85),
                new Color(229, 247, 239),
                "/img/ca-confirm.png"
        );

        lblTotalAppointments =(JLabel) totalCard.getClientProperty("valueLabel");
        lblPendingAppointments =(JLabel) pendingCard.getClientProperty("valueLabel");
        lblConfirmedAppointments =(JLabel) confirmedCard.getClientProperty("valueLabel");

        statsPanel.add(totalCard);
        statsPanel.add(pendingCard);
        statsPanel.add(confirmedCard);

        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        statsPanel.setPreferredSize(new Dimension(600, 76));

        topPanel.add(statsPanel);
        topPanel.add(Box.createVerticalStrut(14));

        //  THANH TÌM KIẾM 
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));

        searchPanel.setOpaque(false);

        txtSearch = createTextField(true);
        txtSearch.setToolTipText("Tìm theo tên khách hàng hoặc số điện thoại");
        txtSearch.setPreferredSize(new Dimension(220, 36));

        cbStatusFilter = new JComboBox<String>(
                new String[]{
                        "Tất cả",
                        "Chờ xác nhận",
                        "Đã xác nhận",
                        "Hoàn thành",
                        "Đã hủy"
                }
        );

        styleCombo(cbStatusFilter);
        cbStatusFilter.setPreferredSize(
                new Dimension(155, 36)
        );

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(cbStatusFilter, BorderLayout.EAST);

        topPanel.add(searchPanel);
        topPanel.add(Box.createVerticalStrut(12));

        // BẢNG DỮ LIỆU 

        String[] columns = {
                "Mã Lịch Hẹn",
                "Giờ",
                "Khách Hàng",
                "SĐT",
                "Dịch Vụ",
                "Trạng Thái"
        };

        modelDayTable = new DefaultTableModel(null, columns) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(modelDayTable) {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();

                    g2.setRenderingHint(
                            RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                    );

                    String message = "Chưa có lịch hẹn nào";
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 15));

                    g2.setColor(TEXT_GRAY);
                    FontMetrics fm = g2.getFontMetrics();

                    int x = (getWidth() - fm.stringWidth(message)) / 2;

                    int y = getHeight() / 2;
                    g2.drawString(message, x, y);
                    g2.dispose();
                }
            }
        };

        sorter = new TableRowSorter<DefaultTableModel>(
                modelDayTable);

        table.setRowSorter(sorter);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(Color.WHITE);

        // PHÂN TRANG 

        JPanel paginationPanel = new JPanel( new FlowLayout(FlowLayout.CENTER, 12, 5));

        paginationPanel.setOpaque(false);
        btnPrevious = createOutlineButton("‹ Trước");
        btnNext = createOutlineButton("Sau ›");
        lblPageInfo = new JLabel("Trang 1/1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblPageInfo.setForeground(TEXT_DARK);
        btnPrevious.addActionListener(e -> {

            if (currentPage > 0) {
                currentPage--;
                updatePagination();
            }
        });

        btnNext.addActionListener(e -> {

            if (currentPage < totalPages - 1) {
                currentPage++;
                updatePagination();
            }
        });

        paginationPanel.add(btnPrevious);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);

        // SỰ KIỆN TÌM KIẾM

        txtSearch.getDocument().addDocumentListener(
                new DocumentListener() {
                    private void changed() {
                        currentPage = 0;
                        updatePagination();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        changed();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        changed();
                    }
                }
        );

        cbStatusFilter.addActionListener(e -> {
            currentPage = 0; updatePagination();});

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(paginationPanel,BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }

    //thẻ thống kê
    private JPanel createStatCard(String title, String value, Color foreground, Color background, String imagePath) {
        RoundedPanel card = new RoundedPanel(18, background);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(44, 44));

        java.net.URL imageURL = getClass().getResource(imagePath);
        if (imageURL != null) {
            ImageIcon original = new ImageIcon(imageURL);
            Image scaled = original.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(scaled));
        } else {
            lblImage.setText("●");
            lblImage.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblImage.setForeground(foreground);
        }

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(foreground);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 23));
        lblValue.setForeground(foreground);

        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(2));
        content.add(lblValue);

        card.add(lblImage, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", lblValue);

        card.setPreferredSize(new Dimension(200, 76));
        card.setMinimumSize(new Dimension(120, 76));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        return card;
    }

    //cập nhật thống kê

    private void updateStatistics() {
        if (modelDayTable == null
                || lblTotalAppointments == null
                || lblPendingAppointments == null
                || lblConfirmedAppointments == null) {
            return;
        }
        int total = modelDayTable.getRowCount();
        int pending = 0;
        int confirmed = 0;
        for (int i = 0; i < modelDayTable.getRowCount(); i++) {
            String status = String.valueOf(modelDayTable.getValueAt(i, 5) ).trim();

            if ("Chờ xác nhận".equalsIgnoreCase(status)) {
                pending++;
            }
            if ("Đã xác nhận".equalsIgnoreCase(status)) {
            	confirmed++;
            }
        }
        lblTotalAppointments.setText( String.valueOf(total));
        lblPendingAppointments.setText(String.valueOf(pending));
        lblConfirmedAppointments.setText( String.valueOf(confirmed));
    }

    //bảng STYLE 

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(239, 243, 248));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        table.setSelectionBackground(BLUE_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader header = table.getTableHeader();
        header.setFont( new Font("Segoe UI", Font.BOLD, 12) );
        header.setBackground(new Color(239, 245, 252));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer =
                (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        // Renderer căn giữa các cột thông thường
        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(JTable table,Object value,boolean selected,boolean focus,int row,int column) {

                        super.getTableCellRendererComponent(
                                table,
                                value,
                                selected,
                                focus,
                                row,
                                column
                        );
                        setHorizontalAlignment(SwingConstants.CENTER);

                        if (selected) {
                            setBackground(BLUE_LIGHT);
                        } else if (row == hoveredRow) {
                            setBackground(new Color(235, 244, 255));
                        } else if (row % 2 == 0) {
                            setBackground(Color.WHITE);
                        } else {
                            setBackground(new Color(248, 250, 253));
                        }
                        setForeground(TEXT_DARK);
                        return this;
                    }
                };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        // Badge trạng thái

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        // Hover hàng
        table.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });
    }

    // trạng thái
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,Object value,boolean selected, boolean focus, int row,int column) {
            JLabel label = new JLabel( value == null ? "" : value.toString());
            label.setFont(new Font("Segoe UI", Font.BOLD, 11) );
            label.setHorizontalAlignment(SwingConstants.CENTER);

            label.setOpaque(true);
            String status = value == null ? "" : value.toString().trim();

            Color fg = TEXT_GRAY; Color bg = READONLY_BG;

            if ("Chờ xác nhận".equalsIgnoreCase(status)) {
                fg = new Color(160, 110, 10);bg = ORANGE_LIGHT;

            } else if ("Đã xác nhận".equalsIgnoreCase(status)) {

                fg = BLUE_PRIMARY; bg = BLUE_LIGHT;
            } else if ("Hoàn thành".equalsIgnoreCase(status)) {
                fg = new Color(25, 130, 85);bg = GREEN_LIGHT;

            } else if ("Đã hủy".equalsIgnoreCase(status)) {
                fg = RED_ACCENT;bg = RED_LIGHT;
            }

            if (selected) {
                bg = BLUE_LIGHT;
            }

            label.setForeground(fg);
            label.setBackground(bg);
            label.setBorder(new EmptyBorder(5, 10, 5, 10));
            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(true);
            if (selected) {
                wrapper.setBackground(BLUE_LIGHT);
            } else if (row == hoveredRow) {
                wrapper.setBackground(new Color(235, 244, 255));
            } else if (row % 2 == 0) {
                wrapper.setBackground(Color.WHITE);
            } else {
                wrapper.setBackground(new Color(248, 250, 253));
            }
            wrapper.add(label);
            return wrapper;
        }
    }

    // cập nhật trang đếm

    private void updatePagination() {
        if (sorter == null) {
            return;
        }
        final String keyword = txtSearch == null ? "": txtSearch.getText().trim().toLowerCase();
        final String status = cbStatusFilter == null ? "Tất cả": (String) cbStatusFilter.getSelectedItem();
        // Đếm số dòng phù hợp với tìm kiếm và trạng thái

        int matchingRows = 0;
        for (int i = 0; i < modelDayTable.getRowCount(); i++) {
            String customer = String.valueOf(modelDayTable.getValueAt(i, 2) ).toLowerCase();
            String phone = String.valueOf(modelDayTable.getValueAt(i, 3)).toLowerCase();
            String rowStatus = String.valueOf( modelDayTable.getValueAt(i, 5) );

            boolean matchKeyword = keyword.isEmpty()
                    || customer.contains(keyword)
                    || phone.contains(keyword);

            boolean matchStatus = "Tất cả".equals(status)
                    || status.equalsIgnoreCase(rowStatus);
            if (matchKeyword && matchStatus) {
                matchingRows++;
            }
        }

        totalPages = Math.max(
                1,(int) Math.ceil( matchingRows / (double) PAGE_SIZE) );

        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }

        if (currentPage < 0) {
            currentPage = 0;
        }

        final int start = currentPage * PAGE_SIZE;
        final int end = start + PAGE_SIZE;

        RowFilter<DefaultTableModel, Integer> filter =
                new RowFilter<DefaultTableModel, Integer>() {

                    @Override
                    public boolean include(
                            Entry<? extends DefaultTableModel,
                                    ? extends Integer> entry
                    ) {
                        int modelRow = entry.getIdentifier();
                        String customer = String.valueOf(entry.getValue(2)).toLowerCase();
                        String phone = String.valueOf(entry.getValue(3)).toLowerCase();
                        String rowStatus = String.valueOf(entry.getValue(5));

                        boolean matchKeyword = keyword.isEmpty()
                                || customer.contains(keyword)
                                || phone.contains(keyword);

                        boolean matchStatus = "Tất cả".equals(status)
                                || status.equalsIgnoreCase(rowStatus);

                        return matchKeyword
                                && matchStatus
                                && modelRow >= start
                                && modelRow < end;
                    }
                };

        sorter.setRowFilter(filter);
        lblPageInfo.setText(
                "Trang " + (currentPage + 1)
                        + "/" + totalPages
                        + "  |  " + matchingRows + " lịch hẹn"
        );
        btnPrevious.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    //UI thành phần

    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField();

        field.setFont( new Font("Segoe UI", Font.PLAIN, 13));

        field.setForeground(TEXT_DARK);
        field.setEditable(editable);

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(5, 10, 5, 10)
        );
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BLUE_PRIMARY, 2),
                new EmptyBorder(4, 9, 4, 9)
        );
        field.setBorder(normalBorder);

        if (!editable) {
            field.setBackground(READONLY_BG);

        } else {

            field.setBackground(Color.WHITE);
            field.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    field.setBorder(focusBorder);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    field.setBorder(normalBorder);
                }
            });
        }
        return field;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_DARK);
    }
    private void addRow( JPanel panel,String labelText,JComponent field) {
        addRow(panel, labelText, field, 36);
    }
    private void addRow(JPanel panel,String labelText,JComponent field,int height) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12) );
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(360, height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }
    private void setCustomerBadge(String text,Color fg, Color bg) {
        lblCustomerStatus.setText(text);
        lblCustomerStatus.setForeground(fg);
        lblCustomerStatus.setBackground(bg);
    }
    // BUTTON

    private JButton createButton(  String text, Color background ) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14) );

        button.setForeground(Color.WHITE);
        button.setBackground(background);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(
                new EmptyBorder(8, 16, 8, 16)
        );

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR )
        );

        Color normal = background;
        Color hover = new Color(15, 112, 57);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
            	button.setBackground(normal);
            }});
        return button;
    }
    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));

        button.setForeground(BLUE_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);

        button.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(190, 211, 243) ),new EmptyBorder(6, 12, 6, 12) )
        );
        button.setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR ));
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(BLUE_LIGHT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    //  tải service
    private void loadServices() {
        cbService.removeAllItems();
        String sql = "SELECT maDichVu, tenDichVu "+ "FROM DichVu ORDER BY tenDichVu";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                cbService.addItem(new ServiceItem(
                                rs.getString("maDichVu"),
                                rs.getString("tenDichVu")));
            }

        } catch (SQLException e) {
            showError("Không thể tải danh sách dịch vụ!\n"+ e.getMessage());
        }
    }

    // LOAD DOCTOR
    private void loadDoctors() {
        DoctorItem old =(DoctorItem) cbDoctor.getSelectedItem();
        String oldId = old == null ? null : old.id;
        cbDoctor.removeAllItems();
        String sql ="SELECT maNhanVien, tenNhanVien FROM NhanVien "
                        + "WHERE chucVu LIKE N'%Bác sĩ%' "
                        + "OR chucVu LIKE N'%Nha sĩ%' "
                        + "ORDER BY tenNhanVien";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                cbDoctor.addItem(
                        new DoctorItem(
                                rs.getString("maNhanVien"),
                                rs.getString("tenNhanVien")));
            }

        } catch (SQLException e) {
            showError( "Không thể tải danh sách nha sĩ!\n"+ e.getMessage());
        }

        if (oldId != null) {
            for (int i = 0; i < cbDoctor.getItemCount(); i++) {
                if (cbDoctor.getItemAt(i).id.equals(oldId)) {
                    cbDoctor.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    //  giới tính

    private String generateId(
            Connection conn,
            String tableName,
            String column,
            String prefix,
            boolean lock
    ) throws SQLException {
        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, "
                        + "SUBSTRING(" + column + ", 3, 18))), 0) + 1 "
                        + "FROM " + tableName
                        + (lock ? " WITH (UPDLOCK, HOLDLOCK)" : "")
                        + " WHERE " + column + " LIKE '"
                        + prefix + "%'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {
                return String.format(
                        prefix + "%03d",
                        rs.getInt(1)
                );
            }
        } return prefix + "001";
    }

    private void refreshPreviewIds() {
        try (Connection conn = DBConnection.getConnection()) {
            txtAppId.setText(
                    generateId(
                            conn,
                            "LichHen",
                            "maLichHen",
                            "LH",
                            false
                    )
            );

            if (!existingCustomer) {
                String phone = txtPhone.getText().trim();
                if (phone.matches("\\d{10,15}")) {
                    txtCustId.setText(
                            generateId(
                                    conn,
                                    "KhachHang",
                                    "maKhachHang",
                                    "KH",
                                    false
                            ) + " (mới)"
                    );

                } else {
                    txtCustId.setText("");
                }
            }

        } catch (SQLException e) {
            txtAppId.setText("Tự tạo khi lưu");
        }
    }

    // K/Hang

    private void resetCustomerState() {
        if (existingCustomer) {
            txtCustName.setText("");
        }
        existingCustomer = false;
        existingCustomerId = null;
        lastLookedPhone = "";

        txtCustName.setEditable(true);
        txtCustName.setBackground(Color.WHITE);
        txtCustId.setText("");
        setCustomerBadge(
                "Nhập số điện thoại để kiểm tra khách hàng",
                TEXT_GRAY,READONLY_BG);
    }

    private void lookupCustomer(boolean force) {
        String phone = txtPhone.getText().trim();
        if (!phone.matches("\\d{10,15}")) {

            if (!phone.isEmpty()&& phone.length() >= 10&& force) {
                setCustomerBadge("Số điện thoại phải gồm 10 đến 15 chữ số",RED_ACCENT,RED_LIGHT);

            } else {
                resetCustomerState();
            }
            return;
        }
        if (!force && phone.equals(lastLookedPhone)) {
            return;
        }
        String sql = "SELECT TOP 1 maKhachHang, tenKH, diemTichLuy "
                        + "FROM KhachHang "
                        + "WHERE sdt = ? "
                        + "ORDER BY maKhachHang";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                lastLookedPhone = phone;

                if (rs.next()) {
                    existingCustomer = true;
                    existingCustomerId = rs.getString("maKhachHang");
                    txtCustId.setText(existingCustomerId);
                    txtCustName.setText(rs.getString("tenKH"));
                    txtCustName.setEditable(false);
                    txtCustName.setBackground(READONLY_BG);

                    setCustomerBadge(
                            "Khách hàng cũ - "
                                    + rs.getInt("diemTichLuy")
                                    + " điểm tích lũy",
                            BLUE_PRIMARY,
                            BLUE_LIGHT
                    );

                } else {

                    existingCustomer = false;
                    existingCustomerId = null;

                    if (!txtCustName.isEditable()) {
                        txtCustName.setText("");
                    }

                    txtCustName.setEditable(true);
                    txtCustName.setBackground(Color.WHITE);
                    txtCustId.setText(
                            generateId(
                                    conn,
                                    "KhachHang",
                                    "maKhachHang",
                                    "KH",
                                    false
                            ) + " (mới)"
                    );
                    setCustomerBadge(
                            "Khách hàng mới - hồ sơ sẽ được tạo khi lưu",
                            ORANGE_ACCENT,ORANGE_LIGHT);
                }
            }

        } catch (SQLException e) {
            showError("Lỗi tra cứu khách hàng!\n"+ e.getMessage());
        }
    }

    // ngày
    private LocalDate getSelectedDate() {
        Date value = (Date) spDate.getValue();
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String formatTime(String time) {
        if (time == null) {
            return "";
        }
        time = time.trim();
        if (time.length() >= 5) {
            return time.substring(0, 5);
        }
        return time;
    }

    // tải all cái lịch hẹn

    private void loadAllAppointments() {
        modelDayTable.setRowCount(0);
        String sql =
                "SELECT LH.maLichHen, LH.gioKham, "
                        + "KH.tenKH, KH.sdt, LH.ghiChu, LH.trangThai "
                        + "FROM LichHen LH "
                        + "LEFT JOIN KhachHang KH "
                        + "ON LH.maKhachHang = KH.maKhachHang "
                        + "ORDER BY LH.ngayHen DESC, LH.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                modelDayTable.addRow(new Object[]{
                        rs.getString("maLichHen"),
                        formatTime(rs.getString("gioKham")),
                        rs.getString("tenKH"),
                        rs.getString("sdt"),
                        rs.getString("ghiChu"),
                        rs.getString("trangThai")
                });
            }

        } catch (SQLException e) {
            showError("Không thể tải tất cả lịch hẹn!\n"+ e.getMessage());
        }
        currentPage = 0;
        updateStatistics();
        updatePagination();
    }
    // tải all bởi ngày

    private void loadAppointmentsByDate(LocalDate date) {
        modelDayTable.setRowCount(0);
        String sql =
                "SELECT LH.maLichHen, LH.gioKham, "
                        + "KH.tenKH, KH.sdt, LH.ghiChu, LH.trangThai "
                        + "FROM LichHen LH "
                        + "LEFT JOIN KhachHang KH "
                        + "ON LH.maKhachHang = KH.maKhachHang "
                        + "WHERE LH.ngayHen = ? "
                        + "ORDER BY LH.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1,java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelDayTable.addRow(new Object[]{
                            rs.getString("maLichHen"),
                            formatTime(rs.getString("gioKham")),
                            rs.getString("tenKH"),
                            rs.getString("sdt"),
                            rs.getString("ghiChu"),
                            rs.getString("trangThai")
                    });
                }
            }

        } catch (SQLException e) {
            showError("Không thể tải lịch hẹn của ngày đã chọn!\n"+ e.getMessage());
        }

        currentPage = 0;
        updateStatistics();
        updatePagination();
    }

    // ô thời gian
    private void renderTimeGrid(
            Set<String> busy,
            LocalDate date
    ) {

        timeGridPanel.removeAll();
        JPanel morningPanel = createTimeGroup("Buổi sáng",0,8,busy,date);
        JPanel afternoonPanel = createTimeGroup("Buổi chiều",8,STANDARD_TIMES.length,busy,date);

        timeGridPanel.add(morningPanel);
        timeGridPanel.add(Box.createVerticalStrut(10));
        timeGridPanel.add(afternoonPanel);
        timeGridPanel.revalidate();
        timeGridPanel.repaint();
    }

    private JPanel createTimeGroup(
            String title,
            int start,
            int end,
            Set<String> busy,
            LocalDate date
    ) {
    	JPanel group = new JPanel(new GridLayout(0, 4, 7, 7));

        group.setOpaque(false);
        group.setBorder(new EmptyBorder(5, 7, 8, 7));
        for (int i = start; i < end; i++) {

            final String slot = STANDARD_TIMES[i];

            boolean occupied = busy.contains(slot);
            boolean expired = date.isBefore(LocalDate.now())
                    || (date.equals(LocalDate.now())
                    && !LocalTime.parse(slot).isAfter(LocalTime.now()));
            JButton button = new JButton(slot);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setMargin(new Insets(2, 4, 2, 4));
            button.setBorder(new EmptyBorder(3, 5, 3, 5));
            button.setPreferredSize(new Dimension(65, 30));
            button.setMinimumSize(new Dimension(65, 30));

            if (occupied) {
                button.setBackground(new Color(255, 225, 228));
                button.setForeground(new Color(190, 45, 55));
                button.setToolTipText("Giờ đã có lịch hẹn");
                button.setEnabled(false);

            } else if (expired) {
                button.setBackground(new Color(232, 235, 240));
                button.setForeground(new Color(125, 135, 150));
                button.setToolTipText("Không thể chọn giờ này");
                button.setEnabled(false);

            } else {
                button.setBackground(new Color(211, 245, 229));
                button.setForeground(new Color(20, 125, 85));
                button.setToolTipText("Chọn giờ " + slot);
                button.addActionListener(e -> {
                    cbTime.setSelectedItem(slot);
                    updateSelectedTimeButton(slot);
                });
            }
            group.add(button);
        }

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel groupTitle = new JLabel(title);
        groupTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));

        groupTitle.setForeground(TEXT_DARK);
        groupTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(groupTitle);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(group);

        return wrapper;
    }

    private void updateSelectedTimeButton(String selected) {
        for (Component component : timeGridPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel wrapper = (JPanel) component;

                for (Component child : wrapper.getComponents()) {

                    if (child instanceof JPanel) {
                        JPanel group = (JPanel) child;
                        for (Component buttonComponent :
                                group.getComponents()) {

                            if (buttonComponent instanceof JButton) {

                                JButton button =
                                        (JButton) buttonComponent;
                                if (button.getText().equals(selected)) {

                                    button.setBackground(BLUE_PRIMARY);
                                    button.setForeground(Color.WHITE);

                                } else if (button.isEnabled()) {

                                	button.setBackground(new Color(211, 245, 229));
                                    button.setForeground(new Color(20, 125, 85));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // load lại trang
    private void refreshAvailability() {
        if (cbTime == null || modelDayTable == null) {
            return;
        }
        DoctorItem doctor =
                (DoctorItem) cbDoctor.getSelectedItem();
        String previousTime =
                (String) cbTime.getSelectedItem();
        cbTime.removeAllItems();
        modelDayTable.setRowCount(0);

        if (!isDateSelected) {
            lblDayTableTitle.setText(
                    "Tất cả lịch hẹn hiện có"
            );
            loadAllAppointments();

        } else {
            LocalDate selectedDate = getSelectedDate();
            lblDayTableTitle.setText("Lịch hẹn ngày "+ String.format("%02d/%02d/%d",
                                    selectedDate.getDayOfMonth(),
                                    selectedDate.getMonthValue(),
                                    selectedDate.getYear()
                            ));

            loadAppointmentsByDate(selectedDate);
        }

        LocalDate date = getSelectedDate();
        if (doctor == null) {
            busyTimes.clear();
            renderTimeGrid(busyTimes, date);
            return;
        }

        busyTimes.clear();

        String sql ="SELECT gioKham, trangThai FROM LichHen "+ "WHERE maNhanVien = ? AND ngayHen = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.id);
            ps.setDate(
                    2,
                    java.sql.Date.valueOf(date)
            );
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String time = formatTime(
                            rs.getString("gioKham")
                    );
                    String status = rs.getString("trangThai");
                    if (!time.isEmpty()&& !"Đã hủy".equalsIgnoreCase(
                                    status == null ? "" : status.trim()
                            )) {
                        busyTimes.add(time);
                    }
                }
            }

        } catch (SQLException e) {
            showError("Không thể kiểm tra giờ trống!\n"+ e.getMessage());

            return;
        }

        renderTimeGrid(busyTimes, date);
        LocalDate today = LocalDate.now();
        for (String slot : STANDARD_TIMES) {

            if (busyTimes.contains(slot)) {
                continue;
            }

            if (date.isBefore(today)) {
                continue;
            }
            if (date.equals(today)
                    && !LocalTime.parse(slot).isAfter(LocalTime.now())) {
                continue;
            }
            cbTime.addItem(slot);
        }

        if (previousTime != null) {
            for (int i = 0; i < cbTime.getItemCount(); i++) {
                if (previousTime.equals(cbTime.getItemAt(i))) {
                    cbTime.setSelectedItem(previousTime);
                    updateSelectedTimeButton(previousTime);
                    break;
                }
            }
        }
    }

    // lưu lịch

    private void performSave() {
        String phone = txtPhone.getText().trim();
        String name = txtCustName.getText().trim();
        ServiceItem service =(ServiceItem) cbService.getSelectedItem();
        DoctorItem doctor = (DoctorItem) cbDoctor.getSelectedItem();
        String time = cbTime.getSelectedItem() == null? null: cbTime.getSelectedItem().toString();
        LocalDate date = getSelectedDate();

        if (phone.isEmpty() || name.isEmpty()) {
            showWarning("Vui lòng nhập số điện thoại và tên khách hàng!");
            return;
        }
        if (!phone.matches("\\d{10,15}")) {
            showWarning("Số điện thoại phải có từ 10 đến 15 chữ số!");
            return;
        }

        if (service == null || doctor == null) {
            showWarning("Chưa có dịch vụ hoặc nha sĩ. " + "Vui lòng kiểm tra dữ liệu trong hệ thống!");

            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showWarning("Ngày hẹn không được nằm trong quá khứ!");
            return;
        }

        if (time == null) {
            showWarning("Vui lòng chọn một giờ hẹn còn trống!");
            return;
        }
        if (busyTimes.contains(time)) {
            showWarning( "Giờ hẹn này đã có người đặt. "+ "Vui lòng chọn giờ khác!");
            refreshAvailability();
            return;
        }

        java.sql.Date sqlDate =
                java.sql.Date.valueOf(date);
        String savedAppId;
        String savedCustId;

        boolean createdNewCustomer = false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Tìm khách hàng
                String customerId = null;
                String sqlFind =
                        "SELECT TOP 1 maKhachHang "
                                + "FROM KhachHang WITH (UPDLOCK, HOLDLOCK) "
                                + "WHERE sdt = ? "
                                + "ORDER BY maKhachHang";
                try (PreparedStatement ps =conn.prepareStatement(sqlFind)
                ) {
                    ps.setString(1, phone);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            customerId =
                                    rs.getString("maKhachHang");
                        }
                    }
                }

                // 2. Tạo khách hàng mới nếu chưa tồn tại
                if (customerId == null) {
                    customerId = generateId(
                            conn,
                            "KhachHang",
                            "maKhachHang",
                            "KH",
                            true
                    );
                    String sqlInsertCustomer =
                            "INSERT INTO KhachHang "
                                    + "(maKhachHang, tenKH, sdt, diemTichLuy) "
                                    + "VALUES (?, ?, ?, 0)";

                    try (PreparedStatement ps =conn.prepareStatement(sqlInsertCustomer)
                    ) {
                        ps.setString(1, customerId);
                        ps.setString(2, name);
                        ps.setString(3, phone);
                        ps.executeUpdate();
                    }
                    createdNewCustomer = true;
                }

                // 3. Kiểm tra trùng lịch
                String sqlDup =
                        "SELECT COUNT(*) FROM LichHen "
                                + "WHERE maNhanVien = ? "
                                + "AND ngayHen = ? "
                                + "AND gioKham = ? "
                                + "AND ISNULL(trangThai, N'') <> N'Đã hủy'";

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(sqlDup)
                ) {
                    ps.setString(1, doctor.id);
                    ps.setDate(2, sqlDate);
                    ps.setString(3, time);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            showWarning(
                                    "Nha sĩ " + doctor.name
                                            + " vừa có người đặt lúc "
                                            + time
                                            + ". Vui lòng chọn giờ khác!"
                            );
                            refreshAvailability();
                            return;
                        }
                    }
                }

                // 4. Tạo lịch hẹn
                savedAppId = generateId(
                        conn,
                        "LichHen",
                        "maLichHen",
                        "LH",
                        true
                );
                String sqlInsertApp =
                        "INSERT INTO LichHen "
                                + "(maLichHen, maKhachHang, maNhanVien, "
                                + "ngayHen, gioKham, trangThai, ghiChu) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(sqlInsertApp)
                ) {
                    ps.setString(1, savedAppId);
                    ps.setString(2, customerId);
                    ps.setString(3, doctor.id);
                    ps.setDate(4, sqlDate);
                    ps.setString(5, time);
                    ps.setString(6, STATUS_PENDING);
                    ps.setString(7, service.name);
                    ps.executeUpdate();
                }

                conn.commit();
                savedCustId = customerId;

            } catch (SQLException ex) {

                conn.rollback();
                throw ex;

            } finally { 
            	conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError(
                    "Không thể lưu lịch hẹn!\n"
                            + ex.getMessage());
            return;
        }

        // Ghi nhật ký
        try {
            AuditLog.event("CREATE","Đặt lịch hẹn - mã " + savedAppId
                            + (createdNewCustomer
                            ? " (tạo khách hàng mới "
                            + savedCustId + ")"
                            : "")
            );

        } catch (Exception ignored) {
            // Không ảnh hưởng việc đặt lịch
        }

        showSuccessDialog(savedAppId, savedCustId, createdNewCustomer);

        clearForm();
    }

    private void showSuccessDialog(String appId, String custId, boolean isNew) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Đặt lịch thành công",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(700, 550);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 50, 30, 50));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel();
        java.net.URL imageURL = getClass().getResource("/img/smile.png");

        if (imageURL != null) {
            ImageIcon originalIcon = new ImageIcon(imageURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    160, 160, Image.SCALE_SMOOTH
            );
            icon.setIcon(new ImageIcon(scaledImage));
        } else {
            icon.setText("☺");
            icon.setFont(new Font("Segoe UI", Font.BOLD, 100));
            System.err.println("Không tìm thấy ảnh: /img/smile.png");
        }

        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setPreferredSize(new Dimension(165, 165));
        icon.setMaximumSize(new Dimension(165, 165));
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel("ĐẶT LỊCH THÀNH CÔNG!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(30, 145, 95));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("Lịch hẹn của bạn đã được lưu vào hệ thống.");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        message.setForeground(TEXT_GRAY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel info = new JPanel(new GridLayout(3, 1, 8, 8));
        info.setBackground(new Color(243, 248, 253));
        info.setBorder(new EmptyBorder(22, 30, 22, 30));
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));

        JLabel app = new JLabel("Mã lịch hẹn: " + appId);
        JLabel customer = new JLabel(
                "Mã khách hàng: " + custId + (isNew ? " (mới)" : " (cũ)")
        );
        JLabel status = new JLabel("Trạng thái: " + STATUS_PENDING);

        for (JLabel label : new JLabel[]{app, customer, status}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 17));
            label.setForeground(TEXT_DARK);
            info.add(label);
        }

        JButton btnClose = new JButton("HOÀN TẤT");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(new Color(35, 160, 115));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setOpaque(true);
        btnClose.setPreferredSize(new Dimension(210, 52));
        btnClose.setMaximumSize(new Dimension(210, 52));
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());

        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setBackground(new Color(25, 135, 95));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setBackground(new Color(35, 160, 115));
            }
        });

        content.add(icon);
        content.add(Box.createVerticalStrut(10));
        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(message);
        content.add(Box.createVerticalStrut(25));
        content.add(info);
        content.add(Box.createVerticalStrut(25));
        content.add(btnClose);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // clean 

    private void clearForm() {
        existingCustomer = false;
        existingCustomerId = null;
        lastLookedPhone = "";

        txtPhone.setText("");
        txtCustName.setText("");

        txtCustName.setEditable(true);
        txtCustName.setBackground(Color.WHITE);
        txtCustId.setText("");

        if (cbService.getItemCount() > 0) {
            cbService.setSelectedIndex(0);
        }

        setCustomerBadge("Nhập số điện thoại để kiểm tra khách hàng",TEXT_GRAY,READONLY_BG);
        refreshPreviewIds();
        refreshAvailability();
    }

    // tin nhắn
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message,"Thông báo",JOptionPane.WARNING_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,message,"Lỗi",JOptionPane.ERROR_MESSAGE);
    }

    // panel tròn
    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        RoundedPanel(int radius,Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.setColor(BORDER_LIGHT);
            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );
            g2.dispose();
            super.paintComponent(g);
        }
    }
}