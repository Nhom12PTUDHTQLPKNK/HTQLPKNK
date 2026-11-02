package views;

import controllers.AuditLog;
import sql.DBConnection;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    // dữ liệu một hồ sơ lịch hẹn dùng để in PDF
    private static class AppointmentRecord {
        String id;
        String custId;
        String custName;
        String phone;
        String service;
        String doctor;
        LocalDate date;
        String time;
        String status;
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

    // tên lễ tân đang đăng nhập, sẽ in ở góc dưới bên trái của file PDF
    private String receptionistName = "";

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

    // gọi hàm này sau khi tạo panel để truyền tên lễ tân đang đăng nhập vào
    public void setReceptionistName(String name) {
        this.receptionistName = name == null ? "" : name.trim();
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

    //  HEADER HỒNG LOANG NÈ TRỜI
    private JPanel createHeaderPanel() {

        // hiệu ứng chuyển màu và bo góc
        JPanel headerCard = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                // Làm mịn các góc của card
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                // Màu hồng pastel và xanh pastel
                Color pinkPastel = new Color(255, 232, 243);
                Color bluePastel = new Color(219, 234, 254);

                // Tạo hiệu ứng chuyển màu từ trái sang phải
                GradientPaint gradient = new GradientPaint(
                        0, 0, pinkPastel,
                        getWidth(), 0, bluePastel
                );

                g2.setPaint(gradient);

                // Vẽ nền card bo góc
                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        16, 16
                );

                // Vẽ đường viền card
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1,
                        getHeight() - 1,
                        16, 16
                );

                g2.dispose();
            }
        };

        headerCard.setOpaque(false);
        headerCard.setLayout(new BorderLayout());
        headerCard.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerCard.setPreferredSize(new Dimension(0, 110));
        headerCard.setMinimumSize(new Dimension(0, 110));

        // Khu vực bên trái chứa logo và thông tin tiêu đề
        JPanel leftPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 0)
        );
        leftPanel.setOpaque(false);

        // Tải logo phòng khám
        JLabel logoLabel = new JLabel();
        java.net.URL imgUrl = getClass().getResource("/img/logo5.png");

        if (imgUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imgUrl);

            Image scaledImg = originalIcon.getImage().getScaledInstance(
                    55, 55,
                    Image.SCALE_SMOOTH
            );

            logoLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            // Hiển thị ký hiệu thay thế nếu không tìm thấy logo
            logoLabel.setText("✚");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
            logoLabel.setForeground(BLUE_PRIMARY);

            System.out.println("Không tìm thấy file ảnh tại: /img/logo5.png");
        }

        // Khu vực chứa tiêu đề và mô tả
        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        // Tiêu đề chính của giao diện
        JLabel title = new JLabel("Đặt Lịch Hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Dòng mô tả bên dưới tiêu đề
        JLabel subtitle = new JLabel("Phòng khám nha khoa Smile Dental");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sắp xếp tiêu đề và mô tả theo chiều dọc
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        // Ghép logo và tiêu đề thành một hàng ngang
        leftPanel.add(logoLabel);
        leftPanel.add(titleBox);

        headerCard.add(leftPanel, BorderLayout.WEST);

        return headerCard;
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

        JSpinner.DateEditor dateEditor =
                new JSpinner.DateEditor(spDate, "dd/MM/yyyy");

        spDate.setEditor(dateEditor);
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JFormattedTextField dateField = dateEditor.getTextField();
        dateField.setEditable(false);
        dateField.setFocusable(false);
        dateField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dateField.setBackground(Color.WHITE);
        dateField.setToolTipText("Nhấn để chọn ngày hẹn");

        dateField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCalendarPicker();
            }
        });

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
        JButton btnSave = createButton(
                "LƯU LỊCH HẸN",
                new Color(18, 105, 60)
        );

        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.setForeground(Color.GREEN);
        btnSave.setBackground(new Color(18, 105, 60));

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

        // Nút in hồ sơ lịch hẹn đang chọn ra file PDF
        JButton btnPrintPdf = createOutlineButton("In PDF");
        btnPrintPdf.setPreferredSize(new Dimension(100, 34));
        btnPrintPdf.setToolTipText("Chọn một lịch hẹn trong bảng rồi bấm để xuất PDF");
        btnPrintPdf.addActionListener(e -> exportSelectedToPdf());

        // Gom 2 nút (In PDF + Tải lại) vào một khung bên phải
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnPrintPdf);
        actionPanel.add(btnReload);

        titlePanel.add(actionPanel, BorderLayout.EAST);
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

        // Chỉ cho chọn 1 hồ sơ mỗi lần để in PDF
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        Color hover = new Color(10, 80, 45);
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
    
    private void showCalendarPicker() {
        LocalDate currentDate = getSelectedDate();

        final JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Chọn ngày hẹn",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(360, 340);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        final int[] selectedMonth = {currentDate.getMonthValue()};
        final int[] selectedYear = {currentDate.getYear()};

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        header.setBackground(Color.WHITE);

        JButton btnPreviousMonth = new JButton("‹");
        JButton btnNextMonth = new JButton("›");

        String[] monthNames = {
                "Tháng 1", "Tháng 2", "Tháng 3",
                "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9",
                "Tháng 10", "Tháng 11", "Tháng 12"
        };

        JComboBox<String> cbMonth = new JComboBox<>(monthNames);
        cbMonth.setSelectedIndex(selectedMonth[0] - 1);

        Integer[] years = new Integer[101];
        int startYear = LocalDate.now().getYear() - 50;

        for (int i = 0; i < years.length; i++) {
            years[i] = startYear + i;
        }

        JComboBox<Integer> cbYear = new JComboBox<>(years);
        cbYear.setSelectedItem(selectedYear[0]);

        cbMonth.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbYear.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnPreviousMonth.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnNextMonth.setFont(new Font("Segoe UI", Font.BOLD, 18));

        header.add(btnPreviousMonth);
        header.add(cbMonth);
        header.add(cbYear);
        header.add(btnNextMonth);

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(new EmptyBorder(5, 12, 10, 12));

        Runnable updateCalendar = () -> {
            calendarPanel.removeAll();

            String[] weekdays = {
                    "T2", "T3", "T4", "T5", "T6", "T7", "CN"
            };

            for (String weekday : weekdays) {
                JLabel label = new JLabel(weekday, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(BLUE_PRIMARY);
                calendarPanel.add(label);
            }

            LocalDate firstDay = LocalDate.of(
                    selectedYear[0],
                    selectedMonth[0],
                    1
            );

            int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
            int daysInMonth = firstDay.lengthOfMonth();

            for (int i = 1; i < firstDayOfWeek; i++) {
                calendarPanel.add(new JLabel(""));
            }

            for (int day = 1; day <= daysInMonth; day++) {
                final int selectedDay = day;

                LocalDate date = LocalDate.of(
                        selectedYear[0],
                        selectedMonth[0],
                        selectedDay
                );

                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dayButton.setFocusPainted(false);
                dayButton.setOpaque(true);
                dayButton.setContentAreaFilled(true);
                dayButton.setBorderPainted(false);
                dayButton.setCursor(
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                );

                if (date.equals(currentDate)) {
                    dayButton.setBackground(BLUE_PRIMARY);
                    dayButton.setForeground(Color.WHITE);
                } else if (date.equals(LocalDate.now())) {
                    dayButton.setBackground(GREEN_LIGHT);
                    dayButton.setForeground(GREEN_ACCENT);
                } else {
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setForeground(TEXT_DARK);
                }

                dayButton.addActionListener(e -> {
                    LocalDate selectedDate = LocalDate.of(
                            selectedYear[0],
                            selectedMonth[0],
                            selectedDay
                    );

                    Date dateValue = Date.from(
                            selectedDate.atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                    );

                    spDate.setValue(dateValue);
                    dialog.dispose();
                });

                calendarPanel.add(dayButton);
            }

            calendarPanel.revalidate();
            calendarPanel.repaint();
        };

        cbMonth.addActionListener(e -> {
            selectedMonth[0] = cbMonth.getSelectedIndex() + 1;
            updateCalendar.run();
        });

        cbYear.addActionListener(e -> {
            Object value = cbYear.getSelectedItem();

            if (value != null) {
                selectedYear[0] = (Integer) value;
                updateCalendar.run();
            }
        });

        btnPreviousMonth.addActionListener(e -> {
            if (selectedMonth[0] == 1) {
                selectedMonth[0] = 12;
                selectedYear[0]--;
            } else {
                selectedMonth[0]--;
            }

            cbMonth.setSelectedIndex(selectedMonth[0] - 1);
            cbYear.setSelectedItem(selectedYear[0]);
            updateCalendar.run();
        });

        btnNextMonth.addActionListener(e -> {
            if (selectedMonth[0] == 12) {
                selectedMonth[0] = 1;
                selectedYear[0]++;
            } else {
                selectedMonth[0]++;
            }

            cbMonth.setSelectedIndex(selectedMonth[0] - 1);
            cbYear.setSelectedItem(selectedYear[0]);
            updateCalendar.run();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);

        JButton btnToday = new JButton("Hôm nay");
        btnToday.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToday.setForeground(BLUE_PRIMARY);
        btnToday.setBackground(BLUE_LIGHT);
        btnToday.setFocusPainted(false);
        btnToday.setBorderPainted(false);

        btnToday.addActionListener(e -> {
            Date today = Date.from(
                    LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );

            spDate.setValue(today);
            dialog.dispose();
        });

        bottomPanel.add(btnToday);

        updateCalendar.run();

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(calendarPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(spDate);
        dialog.setVisible(true);
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

    // ====================== XUẤT HỒ SƠ LỊCH HẸN RA PDF ======================

    // xử lý khi bấm nút "In PDF": lấy hồ sơ đang chọn -> hỏi nơi lưu -> xuất file
    private void exportSelectedToPdf() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showWarning("Vui lòng chọn một hồ sơ lịch hẹn trong bảng trước khi in PDF!");
            return;
        }

        // đổi chỉ số dòng trên giao diện sang chỉ số dòng trong model (do có sorter/filter)
        int modelRow = table.convertRowIndexToModel(viewRow);
        String appId = String.valueOf(modelDayTable.getValueAt(modelRow, 0));

        AppointmentRecord record = loadAppointmentRecord(appId);
        if (record == null) {
            showWarning("Không tìm thấy hồ sơ " + appId + " trong hệ thống!");
            return;
        }

        // hộp thoại chọn nơi lưu file
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu phiếu hẹn khám");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File("PhieuHen_" + appId + ".pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }

        // hỏi xác nhận nếu file đã tồn tại
        if (file.exists()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tệp \"" + file.getName() + "\" đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            writePdf(renderAppointmentPage(record), file);
        } catch (Exception ex) {
            showError("Không thể xuất file PDF!\n" + ex.getMessage());
            return;
        }

        // ghi nhật ký (lỗi ghi nhật ký không ảnh hưởng việc xuất file)
        try {
            AuditLog.event("EXPORT", "In phiếu hẹn PDF - mã " + record.id);
        } catch (Exception ignored) {
            // Không ảnh hưởng việc xuất PDF
        }

        int open = JOptionPane.showConfirmDialog(
                this,
                "Đã xuất PDF thành công!\nBạn có muốn mở tệp ngay không?",
                "Thông báo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );
        if (open == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                showWarning("Không thể mở tệp tự động. Tệp được lưu tại:\n" + file.getAbsolutePath());
            }
        }
    }

    // đọc đầy đủ thông tin 1 hồ sơ lịch hẹn từ SQL
    private AppointmentRecord loadAppointmentRecord(String appId) {
        String sql =
                "SELECT LH.maLichHen, LH.ngayHen, LH.gioKham, LH.trangThai, LH.ghiChu, "
                        + "KH.maKhachHang, KH.tenKH, KH.sdt, NV.tenNhanVien "
                        + "FROM LichHen LH "
                        + "LEFT JOIN KhachHang KH ON LH.maKhachHang = KH.maKhachHang "
                        + "LEFT JOIN NhanVien NV ON LH.maNhanVien = NV.maNhanVien "
                        + "WHERE LH.maLichHen = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AppointmentRecord r = new AppointmentRecord();
                    r.id = rs.getString("maLichHen");
                    r.custId = rs.getString("maKhachHang");
                    r.custName = rs.getString("tenKH");
                    r.phone = rs.getString("sdt");
                    r.service = rs.getString("ghiChu");
                    r.doctor = rs.getString("tenNhanVien");
                    java.sql.Date d = rs.getDate("ngayHen");
                    r.date = d == null ? null : d.toLocalDate();
                    r.time = formatTime(rs.getString("gioKham"));
                    r.status = rs.getString("trangThai");
                    return r;
                }
            }

        } catch (SQLException e) {
            showError("Không thể đọc hồ sơ lịch hẹn!\n" + e.getMessage());
        }
        return null;
    }

    // đổi null thành "---" để không in chữ "null" ra PDF
    private String pdfValue(String s) {
        return (s == null || s.trim().isEmpty()) ? "---" : s.trim();
    }

    // tạo font cho PDF với cỡ chữ số thực
    private Font pdfFont(int style, float size) {
        return new Font("Segoe UI", style, 12).deriveFont(size);
    }

    // cắt bớt chữ nếu dài hơn khung, thêm dấu ...
    private String fitText(String s, FontMetrics fm, int maxWidth) {
        if (s == null) {
            return "";
        }
        if (fm.stringWidth(s) <= maxWidth) {
            return s;
        }
        String ellipsis = "…";
        while (s.length() > 1 && fm.stringWidth(s + ellipsis) > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + ellipsis;
    }

    // vẽ chữ căn giữa theo trục x
    private void drawCenteredText(Graphics2D g, String text, float centerX, float baselineY, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, centerX - fm.stringWidth(text) / 2f, baselineY);
    }

    // vẽ khung thông tin (nhiều dòng nhãn - giá trị), trả về toạ độ y phía dưới khung
    private int drawInfoBox(Graphics2D g, int x, int y, int w, String[][] rows) {
        final int rowH = 30;
        final int pad = 6;
        int h = rows.length * rowH + pad * 2;

        // nền + viền khung
        g.setColor(new Color(244, 248, 254));
        g.fillRoundRect(x, y, w, h, 14, 14);
        g.setColor(new Color(205, 222, 245));
        g.setStroke(new BasicStroke(0.8f));
        g.drawRoundRect(x, y, w, h, 14, 14);

        // nét đứt để ngăn cách các dòng
        Stroke dashed = new BasicStroke(0.7f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{2f, 2f}, 0f);

        for (int i = 0; i < rows.length; i++) {
            int rowTop = y + pad + i * rowH;
            int baseline = rowTop + 19;

            // nhãn bên trái
            g.setFont(pdfFont(Font.PLAIN, 11f));
            g.setColor(new Color(110, 128, 152));
            g.drawString(rows[i][0], x + 18, baseline);

            // giá trị bên phải
            if ("Trạng thái".equals(rows[i][0])) {
                drawStatusBadge(g, rows[i][1], x + 170, rowTop + 5);
            } else {
                g.setFont(pdfFont(Font.BOLD, 12.5f));
                g.setColor(new Color(30, 50, 85));
                g.drawString(fitText(rows[i][1], g.getFontMetrics(), w - 170 - 18), x + 170, baseline);
            }

            if (i < rows.length - 1) {
                g.setColor(new Color(214, 226, 243));
                g.setStroke(dashed);
                g.drawLine(x + 14, rowTop + rowH, x + w - 14, rowTop + rowH);
                g.setStroke(new BasicStroke(0.8f));
            }
        }
        return y + h;
    }

    // vẽ nhãn trạng thái dạng viên thuốc có màu theo trạng thái
    private void drawStatusBadge(Graphics2D g, String status, int x, int y) {
        Color fg = new Color(90, 105, 130);
        Color bg = new Color(236, 240, 246);
        String s = status == null ? "" : status.trim();

        if ("Chờ xác nhận".equalsIgnoreCase(s)) {
            fg = new Color(160, 110, 10);
            bg = new Color(255, 240, 210);
        } else if ("Đã xác nhận".equalsIgnoreCase(s)) {
            fg = new Color(30, 95, 200);
            bg = new Color(214, 229, 252);
        } else if ("Hoàn thành".equalsIgnoreCase(s)) {
            fg = new Color(25, 130, 85);
            bg = new Color(212, 242, 227);
        } else if ("Đã hủy".equalsIgnoreCase(s)) {
            fg = new Color(200, 60, 70);
            bg = new Color(255, 225, 228);
        }

        g.setFont(pdfFont(Font.BOLD, 11f));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s) + 24;

        g.setColor(bg);
        g.fillRoundRect(x, y, w, 20, 20, 20);
        g.setColor(fg);
        g.drawString(s, x + 12, y + 14);
    }

    // vẽ cả trang A4 (595 x 842 điểm) ra ảnh độ phân giải cao (200 dpi)
    private BufferedImage renderAppointmentPage(AppointmentRecord r) {
        final int pxW = 1654;
        final int pxH = 2339;

        // bảng màu chủ đạo xanh - trắng
        final Color blueDeep = new Color(18, 88, 190);
        final Color blueMain = new Color(45, 112, 220);
        final Color blueSoft = new Color(232, 241, 255);

        BufferedImage img = new BufferedImage(pxW, pxH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // nền trắng
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, pxW, pxH);

        // từ đây vẽ theo toạ độ điểm (pt) của trang A4
        g.scale(pxW / 595.0, pxH / 842.0);

        // ---------- BANNER XANH Ở TRÊN ----------
        g.setPaint(new GradientPaint(0, 0, blueDeep, 595, 0, new Color(70, 140, 240)));
        g.fillRect(0, 0, 595, 120);

        // vài vòng tròn mờ trang trí ở góc phải banner
        g.setClip(0, 0, 595, 120);
        g.setColor(new Color(255, 255, 255, 28));
        g.fillOval(440, -70, 230, 230);
        g.setColor(new Color(255, 255, 255, 22));
        g.fillOval(360, 50, 120, 120);
        g.setClip(null);

        // vòng tròn trắng chứa logo
        g.setColor(Color.WHITE);
        g.fillOval(40, 28, 64, 64);

        java.net.URL logoUrl = getClass().getResource("/img/logo5.png");
        if (logoUrl != null) {
            Image logo = new ImageIcon(logoUrl).getImage();
            g.drawImage(logo, 46, 34, 52, 52, null);
        } else {
            // không có logo thì vẽ dấu cộng xanh thay thế
            g.setColor(blueMain);
            g.fillRoundRect(64, 42, 16, 36, 4, 4);
            g.fillRoundRect(54, 52, 36, 16, 4, 4);
        }

        // tên phòng khám
        g.setFont(pdfFont(Font.BOLD, 28f));
        g.setColor(Color.WHITE);
        g.drawString("SMILE DENTAL", 120, 64);
        g.setFont(pdfFont(Font.PLAIN, 12f));
        g.setColor(new Color(220, 234, 255));
        g.drawString("PHÒNG KHÁM NHA KHOA", 121, 86);

        // ---------- TIÊU ĐỀ PHIẾU ----------
        drawCenteredText(g, "PHIẾU HẸN KHÁM", 297.5f, 165, pdfFont(Font.BOLD, 25f), blueDeep);

        // viên thuốc hiển thị mã lịch hẹn
        g.setFont(pdfFont(Font.BOLD, 13f));
        FontMetrics pillFm = g.getFontMetrics();
        String pillText = "Mã lịch hẹn: " + pdfValue(r.id);
        int pillW = pillFm.stringWidth(pillText) + 36;
        int pillX = (595 - pillW) / 2;
        g.setColor(blueSoft);
        g.fillRoundRect(pillX, 178, pillW, 26, 26, 26);
        g.setColor(new Color(190, 211, 243));
        g.setStroke(new BasicStroke(0.8f));
        g.drawRoundRect(pillX, 178, pillW, 26, 26, 26);
        g.setColor(blueMain);
        g.drawString(pillText, pillX + 18, 196);

        // ---------- THÔNG TIN KHÁCH HÀNG ----------
        g.setColor(blueMain);
        g.fillRoundRect(40, 236, 4, 18, 4, 4);
        g.setFont(pdfFont(Font.BOLD, 13f));
        g.setColor(blueDeep);
        g.drawString("THÔNG TIN KHÁCH HÀNG", 52, 250);

        int bottom1 = drawInfoBox(g, 40, 262, 515, new String[][]{
                {"Mã khách hàng", pdfValue(r.custId)},
                {"Họ và tên", pdfValue(r.custName)},
                {"Số điện thoại", pdfValue(r.phone)}
        });

        // ---------- THÔNG TIN LỊCH HẸN ----------
        int sec2 = bottom1 + 34;
        g.setColor(blueMain);
        g.fillRoundRect(40, sec2 - 14, 4, 18, 4, 4);
        g.setFont(pdfFont(Font.BOLD, 13f));
        g.setColor(blueDeep);
        g.drawString("THÔNG TIN LỊCH HẸN", 52, sec2);

        String dateText = r.date == null ? "---" : r.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        drawInfoBox(g, 40, sec2 + 12, 515, new String[][]{
                {"Mã lịch hẹn", pdfValue(r.id)},
                {"Dịch vụ khám", pdfValue(r.service)},
                {"Nha sĩ phụ trách", pdfValue(r.doctor)},
                {"Ngày hẹn", dateText},
                {"Giờ hẹn", pdfValue(r.time)},
                {"Trạng thái", pdfValue(r.status)}
        });

        // ---------- LƯU Ý ----------
        int noteY = sec2 + 12 + 6 * 30 + 12 + 22;
        g.setColor(new Color(240, 247, 255));
        g.fillRoundRect(40, noteY, 515, 30, 10, 10);
        g.setColor(blueMain);
        g.fillRoundRect(40, noteY, 4, 30, 4, 4);
        g.setFont(pdfFont(Font.ITALIC, 10.5f));
        g.setColor(new Color(70, 95, 140));
        g.drawString("Quý khách vui lòng đến trước giờ hẹn 10 phút và mang theo phiếu này khi đến khám.", 56, noteY + 19);

        // ---------- KHU VỰC KÝ TÊN ----------
        // ngày giờ in phiếu căn phải
        String printed = "Ngày in: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        g.setFont(pdfFont(Font.ITALIC, 10.5f));
        g.setColor(TEXT_GRAY);
        g.drawString(printed, 555 - g.getFontMetrics().stringWidth(printed), noteY + 62);

        final float leftCx = 150f;   // tâm khối ký bên trái (lễ tân)
        final float rightCx = 445f;  // tâm khối ký bên phải (khách hàng)
        final float signTitleY = noteY + 92;

        drawCenteredText(g, "LỄ TÂN PHỤ TRÁCH", leftCx, signTitleY, pdfFont(Font.BOLD, 12.5f), blueDeep);
        drawCenteredText(g, "(Ký và ghi rõ họ tên)", leftCx, signTitleY + 15, pdfFont(Font.ITALIC, 10f), TEXT_GRAY);

        drawCenteredText(g, "KHÁCH HÀNG", rightCx, signTitleY, pdfFont(Font.BOLD, 12.5f), blueDeep);
        drawCenteredText(g, "(Ký và ghi rõ họ tên)", rightCx, signTitleY + 15, pdfFont(Font.ITALIC, 10f), TEXT_GRAY);

        // vị trí in tên bên dưới khoảng trống để ký
        final float nameY = signTitleY + 105;

        // đường kẻ mảnh phía trên tên
        g.setColor(new Color(190, 211, 243));
        g.setStroke(new BasicStroke(0.8f));
        g.drawLine((int) leftCx - 80, (int) nameY - 16, (int) leftCx + 80, (int) nameY - 16);
        g.drawLine((int) rightCx - 80, (int) nameY - 16, (int) rightCx + 80, (int) nameY - 16);

        // tên lễ tân bên trái (nếu chưa truyền vào thì để trống cho lễ tân tự ký)
        if (!receptionistName.isEmpty()) {
            drawCenteredText(g, receptionistName, leftCx, nameY, pdfFont(Font.BOLD, 12.5f), TEXT_DARK);
        }
        // tên khách hàng bên phải
        if (r.custName != null && !r.custName.trim().isEmpty()) {
            drawCenteredText(g, r.custName.trim(), rightCx, nameY, pdfFont(Font.BOLD, 12.5f), TEXT_DARK);
        }

        // ---------- CHÂN TRANG ----------
        g.setPaint(new GradientPaint(0, 0, blueDeep, 595, 0, new Color(70, 140, 240)));
        g.fillRect(0, 812, 595, 30);
        drawCenteredText(g, "Cảm ơn quý khách đã tin tưởng Smile Dental", 297.5f, 831,
                pdfFont(Font.PLAIN, 10.5f), Color.WHITE);

        g.dispose();
        return img;
    }

    // ghi ảnh trang vừa vẽ thành file PDF 1 trang (không cần thư viện ngoài)
    private void writePdf(BufferedImage image, File file) throws IOException {
        // nén ảnh sang JPEG chất lượng cao
        ByteArrayOutputStream jpegBytes = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.95f);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(jpegBytes)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        byte[] jpeg = jpegBytes.toByteArray();

        // dựng cấu trúc PDF: 1 Catalog, 2 Pages, 3 Page, 4 Content, 5 Image
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int[] offsets = new int[6];

        writeAscii(out, "%PDF-1.4\n");

        offsets[1] = out.size();
        writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets[2] = out.size();
        writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets[3] = out.size();
        writeAscii(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /XObject << /Im0 5 0 R >> >> /Contents 4 0 R >>\nendobj\n");

        String content = "q\n595 0 0 842 0 0 cm\n/Im0 Do\nQ\n";
        offsets[4] = out.size();
        writeAscii(out, "4 0 obj\n<< /Length " + content.length() + " >>\nstream\n"
                + content + "endstream\nendobj\n");

        offsets[5] = out.size();
        writeAscii(out, "5 0 obj\n<< /Type /XObject /Subtype /Image /Width " + image.getWidth()
                + " /Height " + image.getHeight()
                + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length "
                + jpeg.length + " >>\nstream\n");
        out.write(jpeg);
        writeAscii(out, "\nendstream\nendobj\n");

        // bảng xref để trình đọc PDF tìm được từng đối tượng
        int xrefPos = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 6\n0000000000 65535 f \n");
        for (int i = 1; i <= 5; i++) {
            xref.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        xref.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n")
                .append(xrefPos)
                .append("\n%%EOF\n");
        writeAscii(out, xref.toString());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(out.toByteArray());
        }
    }

    // ghi chuỗi ASCII vào luồng byte
    private void writeAscii(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
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