
package views;

import Audit.AuditLog;
import models.dao.LichHenDAO;
import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class BookingProcessPanel extends JPanel {

    // ==================== COLORS ====================

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

    // ==================== TABLES ====================

    private DefaultTableModel tableModelPending;
    private DefaultTableModel tableModelConfirmed;
    private DefaultTableModel tableModelHistory;

    private JTable tablePending;
    private JTable tableConfirmed;
    private JTable tableHistory;

    private JTabbedPane tabbedPane;

    private TableRowSorter<DefaultTableModel> rowSorterPending;
    private TableRowSorter<DefaultTableModel> rowSorterConfirmed;
    private TableRowSorter<DefaultTableModel> rowSorterHistory;

    // ==================== FORM ====================

    private JTextField txtSearch;
    private JTextField txtAppId;
    private JTextField txtCustomerName;
    private JTextField txtPhone;
    private JTextField txtAppDate;
    private JTextField txtAppTime;

    private JComboBox<String> cbService;
    private JComboBox<String> cbDoctor;

    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    private LichHenDAO lichHenDAO;

    // ==================== STATISTICS ====================

    private JLabel lblPendingCount;
    private JLabel lblConfirmedCount;
    private JLabel lblHistoryCount;

    // ==================== CONSTRUCTOR ====================

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

        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createBookingFormPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);

        loadServices();
        loadDoctors();
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

        JLabel lblSubtitle = new JLabel(
                "Theo dõi và quản lý lịch khám tại phòng khám nha khoa"
        );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_GRAY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitle);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        searchPanel.setOpaque(false);

        txtSearch = new PlaceholderTextField(
                "Nhập tên, SĐT hoặc mã lịch hẹn..."
        );

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(270, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 12, 5, 12)
        ));

        txtSearch.setToolTipText(
                "Nhập mã lịch hẹn, tên khách hàng hoặc số điện thoại"
        );

        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createButton("Tìm kiếm", BLUE_PRIMARY);
        btnSearch.setPreferredSize(new Dimension(100, 38));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = createButton(
                "Tải lại",
                new Color(105, 125, 150)
        );

        btnRefresh.setPreferredSize(new Dimension(85, 38));

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            performSearch();
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

        JPanel pendingCard = createStatisticCard(
                "LỊCH CHỜ XÁC NHẬN",
                "0",
                "Đang chờ xử lý",
                BLUE_PRIMARY,
                BLUE_LIGHT
        );

        JPanel confirmedCard = createStatisticCard(
                "LỊCH ĐÃ XÁC NHẬN",
                "0",
                "Đã được tiếp nhận",
                GREEN_ACCENT,
                GREEN_LIGHT
        );

        JPanel historyCard = createStatisticCard(
                "LỊCH ĐÃ XỬ LÝ",
                "0",
                "Hoàn thành hoặc đã hủy",
                ORANGE_ACCENT,
                ORANGE_LIGHT
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

    private JPanel createStatisticCard(
            String title,
            String number,
            String description,
            Color accent,
            Color lightColor
    ) {

        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout(15, 0));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        card.setPreferredSize(new Dimension(200, 115));
        card.setMinimumSize(new Dimension(200, 115));

        String imagePath;

        if (title.contains("CHỜ")) {
            imagePath = "/img/ca-wait.png";
        } else if (title.contains("XÁC NHẬN")) {
            imagePath = "/img/ca-confirm.png";
        } else {
            imagePath = "/img/ca-cancel.png";
        }

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(true);
        iconPanel.setBackground(lightColor);
        iconPanel.setPreferredSize(new Dimension(52, 52));
        iconPanel.setMinimumSize(new Dimension(52, 52));

        java.net.URL imageURL = getClass().getResource(imagePath);

        JLabel icon = new JLabel();

        if (imageURL != null) {

            ImageIcon originalIcon = new ImageIcon(imageURL);

            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    32, 32, Image.SCALE_SMOOTH
            );

            icon.setIcon(new ImageIcon(scaledImage));

        } else {
            System.out.println("Không tìm thấy hình ảnh: " + imagePath);
        }

        iconPanel.add(icon);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_GRAY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblNumber.setForeground(TEXT_DARK);
        lblNumber.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDescription = new JLabel(description);
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDescription.setForeground(TEXT_GRAY);
        lblDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblNumber);
        textPanel.add(lblDescription);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.putClientProperty("countLabel", lblNumber);

        return card;
    }

    // ==================== CLICKABLE STATISTIC CARDS ====================

    private void makeCardClickable(
            Component component,
            final int tabIndex
    ) {

        component.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        component.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                if (tabbedPane != null) {
                    tabbedPane.setSelectedIndex(tabIndex);
                }
            }
        });

        if (component instanceof Container) {

            Component[] children =
                    ((Container) component).getComponents();

            for (Component child : children) {
                makeCardClickable(child, tabIndex);
            }
        }
    }

    // ==================== UPDATE STATISTICS ====================

    private void updateStatistics() {

        int pendingCount = tableModelPending.getRowCount();
        int confirmedCount = tableModelConfirmed.getRowCount();
        int historyCount = tableModelHistory.getRowCount();

        if (lblPendingCount != null) {
            lblPendingCount.setText(String.valueOf(pendingCount));
        }

        if (lblConfirmedCount != null) {
            lblConfirmedCount.setText(String.valueOf(confirmedCount));
        }

        if (lblHistoryCount != null) {
            lblHistoryCount.setText(String.valueOf(historyCount));
        }

        if (tabbedPane != null) {

            tabbedPane.setTitleAt(
                    0,
                    "Chờ xác nhận (" + pendingCount + ")"
            );

            tabbedPane.setTitleAt(
                    1,
                    "Đã xác nhận (" + confirmedCount + ")"
            );

            tabbedPane.setTitleAt(
                    2,
                    "Lịch sử xử lý (" + historyCount + ")"
            );
        }
    }

    // ==================== TABLE PANEL ====================

    private JPanel createTablePanel() {

        RoundedPanel panel = new RoundedPanel(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách lịch hẹn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblHint = new JLabel("Chọn một lịch hẹn để xem thông tin");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(TEXT_GRAY);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(lblTitle);
        heading.add(Box.createVerticalStrut(4));
        heading.add(lblHint);

        titlePanel.add(heading, BorderLayout.WEST);

        String[] columns = {
                "Mã Lịch Hẹn",
                "Tên Khách Hàng",
                "Số Điện Thoại",
                "Dịch Vụ Khám",
                "Nha Sĩ Phụ Trách",
                "Ngày Hẹn",
                "Giờ Hẹn",
                "Trạng Thái"
        };

        tableModelPending = createTableModel(columns);
        tableModelConfirmed = createTableModel(columns);
        tableModelHistory = createTableModel(columns);

        tablePending = new JTable(tableModelPending);
        tableConfirmed = new JTable(tableModelConfirmed);
        tableHistory = new JTable(tableModelHistory);

        PdfTableExport.install(tablePending, "Lịch hẹn chờ xử lý");
        PdfTableExport.install(tableConfirmed, "Lịch hẹn đã xác nhận");
        PdfTableExport.install(tableHistory, "Lịch hẹn đã xử lý");

        styleTable(tablePending);
        styleTable(tableConfirmed);
        styleTable(tableHistory);

        rowSorterPending = new TableRowSorter<>(tableModelPending);
        rowSorterConfirmed = new TableRowSorter<>(tableModelConfirmed);
        rowSorterHistory = new TableRowSorter<>(tableModelHistory);

        tablePending.setRowSorter(rowSorterPending);
        tableConfirmed.setRowSorter(rowSorterConfirmed);
        tableHistory.setRowSorter(rowSorterHistory);

        addSelectionListener(tablePending, tableModelPending);
        addSelectionListener(tableConfirmed, tableModelConfirmed);
        addSelectionListener(tableHistory, tableModelHistory);

        JScrollPane scrollPending = createScrollPane(tablePending);
        JScrollPane scrollConfirmed = createScrollPane(tableConfirmed);
        JScrollPane scrollHistory = createScrollPane(tableHistory);

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

    private DefaultTableModel createTableModel(String[] columns) {

        return new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JScrollPane createScrollPane(JTable table) {

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private void addSelectionListener(
            JTable table,
            DefaultTableModel model
    ) {

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() != -1) {

                int modelRow = table.convertRowIndexToModel(
                        table.getSelectedRow()
                );

                selectedRowIndex = modelRow;
                fillFormFromTable(model, modelRow);
            }
        });
    }

    // ==================== BOOKING FORM ====================

    private JPanel createBookingFormPanel() {

        RoundedPanel panel = new RoundedPanel(16, CARD_BG);
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 18, 20, 18));
        panel.setPreferredSize(new Dimension(320, 0));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thông tin lịch hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Nhập hoặc cập nhật thông tin");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(TEXT_GRAY);

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        txtAppId = addField(formFields, "Mã lịch hẹn");
        txtCustomerName = addField(formFields, "Tên khách hàng");
        txtPhone = addField(formFields, "Số điện thoại");

        cbService = addComboBox(formFields, "Dịch vụ khám");
        cbDoctor = addComboBox(formFields, "Nha sĩ phụ trách");

        txtAppDate = addField(formFields, "Ngày hẹn (dd/MM/yyyy)");
        txtAppTime = addField(formFields, "Giờ hẹn");

        JScrollPane formScroll = new JScrollPane(formFields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);
        formScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 9));
        buttonPanel.setOpaque(false);

        JButton btnSave = createButton("Lưu thông tin", GREEN_ACCENT);
        JButton btnDelete = createButton("Hủy lịch hẹn", ORANGE_ACCENT);
        JButton btnClear = createButton("Làm mới biểu mẫu", BLUE_PRIMARY);

        btnSave.addActionListener(e -> performSave());
        btnDelete.addActionListener(e -> performDelete());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(formScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField addField(JPanel panel, String labelText) {

        JLabel label = createFieldLabel(labelText);

        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_DARK);
        field.setPreferredSize(new Dimension(250, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 10, 5, 10)
        ));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        panel.add(Box.createVerticalStrut(13));

        return field;
    }

    private JComboBox<String> addComboBox(
            JPanel panel,
            String labelText
    ) {

        JLabel label = createFieldLabel(labelText);

        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_DARK);
        combo.setPreferredSize(new Dimension(250, 36));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(combo);
        panel.add(Box.createVerticalStrut(13));

        return combo;
    }

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    // ==================== LOAD SERVICES ====================

    private void loadServices() {

        cbService.removeAllItems();

        String sql = "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbService.addItem(rs.getString("tenDichVu"));
            }

            if (cbService.getItemCount() == 0) {
                cbService.addItem("Không có dịch vụ");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách dịch vụ:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================== LOAD DOCTORS ====================

    private void loadDoctors() {

        cbDoctor.removeAllItems();

        String sql = "SELECT tenNhanVien FROM NhanVien "
                + "WHERE chucVu LIKE N'%Bác sĩ%' "
                + "ORDER BY tenNhanVien";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbDoctor.addItem(rs.getString("tenNhanVien"));
            }

            if (cbDoctor.getItemCount() == 0) {
                cbDoctor.addItem("Không có nha sĩ");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách nha sĩ:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================== LOAD APPOINTMENTS ====================

    private void loadAppointments() {

        tableModelPending.setRowCount(0);
        tableModelConfirmed.setRowCount(0);
        tableModelHistory.setRowCount(0);

        try {

            List<Object[]> list = lichHenDAO.getAllAppointments();

            for (Object[] row : list) {

                String status = row[7] == null
                        ? ""
                        : row[7].toString().trim();

                if (status.equalsIgnoreCase("Chờ xác nhận")) {

                    tableModelPending.addRow(row);

                } else if (status.equalsIgnoreCase("Đã xác nhận")) {

                    tableModelConfirmed.addRow(row);

                } else {

                    tableModelHistory.addRow(row);
                }
            }

            updateStatistics();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách lịch hẹn:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================== FILL FORM ====================


private void fillFormFromTable(
        DefaultTableModel model,
        int row
) {

    txtAppId.setText(getCellText(model, row, 0));
    txtAppId.setEnabled(false);

    txtCustomerName.setText(getCellText(model, row, 1));
    txtPhone.setText(getCellText(model, row, 2));

    cbService.setSelectedItem(getCellText(model, row, 3));
    cbDoctor.setSelectedItem(getCellText(model, row, 4));

    txtAppDate.setText(getCellText(model, row, 5));
    txtAppTime.setText(getCellText(model, row, 6));

    isEditing = true;
}

private String getCellText(
        DefaultTableModel model,
        int row,
        int column
) {
    Object value = model.getValueAt(row, column);
    return value == null ? "" : value.toString();
}


    // ==================== SAVE APPOINTMENT ====================

    private void performSave() {

        String id = txtAppId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();

        String service = (String) cbService.getSelectedItem();
        String doctor = (String) cbDoctor.getSelectedItem();

        String date = txtAppDate.getText().trim();
        String time = txtAppTime.getText().trim();

        if (id.isEmpty() || name.isEmpty()
                || phone.isEmpty() || date.isEmpty() || time.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (service == null || doctor == null
                || service.startsWith("Không có")
                || doctor.startsWith("Không có")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng kiểm tra danh sách dịch vụ và nha sĩ!"
            );

            return;
        }

        if (!phone.matches("\\d{10,15}")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Số điện thoại phải có từ 10 đến 15 chữ số!"
            );

            return;
        }

        java.sql.Date sqlDate;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);

            Date parsedDate = sdf.parse(date);
            sqlDate = new java.sql.Date(parsedDate.getTime());

        } catch (ParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ngày hẹn phải có định dạng dd/MM/yyyy!",
                    "Sai định dạng",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Giờ hẹn không hợp lệ!\nVui lòng nhập theo dạng HH:mm."
            );

            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String customerId = lichHenDAO.findCustomerId(conn, phone);

                if (customerId == null) {
                    customerId = lichHenDAO.createCustomer(conn, name, phone);
                }

                String doctorId = lichHenDAO.findDoctorId(conn, doctor);

                if (doctorId == null) {
                    throw new SQLException(
                            "Không tìm thấy nha sĩ trong bảng NhanVien!"
                    );
                }

                if (lichHenDAO.isDuplicateAppointment(
                        conn, doctorId, sqlDate, time, id)) {

                    conn.rollback();

                    JOptionPane.showMessageDialog(
                            this,
                            "Nha sĩ đã có lịch hẹn vào thời gian này!",
                            "Trùng lịch",
                            JOptionPane.WARNING_MESSAGE
                    );

                    return;
                }

                if (isEditing) {

                    lichHenDAO.updateAppointment(
                            conn, id, customerId, doctorId,
                            sqlDate, time, service
                    );

                } else {

                    lichHenDAO.insertAppointment(
                            conn, id, customerId, doctorId,
                            sqlDate, time, service
                    );
                }

                conn.commit();

                AuditLog.event(
                        isEditing ? "UPDATE" : "CREATE",
                        "Quản lý lịch hẹn - mã " + id
                );

                JOptionPane.showMessageDialog(
                        this,
                        isEditing
                                ? "Cập nhật lịch hẹn thành công!"
                                : "Tạo lịch hẹn thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();
                loadAppointments();

            } catch (Exception e) {

                conn.rollback();
                throw e;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi lưu lịch hẹn:\n" + e.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==================== CANCEL APPOINTMENT ====================

    private void performDelete() {

        JTable activeTable = getActiveTable();
        DefaultTableModel activeModel = getActiveModel();

        int selectedRow = activeTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn lịch hẹn cần hủy!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int modelRow = activeTable.convertRowIndexToModel(selectedRow);

        String id = activeModel.getValueAt(modelRow, 0).toString();
        String status = activeModel.getValueAt(modelRow, 7).toString();

        if ("Đã hủy".equalsIgnoreCase(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lịch hẹn này đã được hủy!"
            );

            return;
        }

        if ("Hoàn thành".equalsIgnoreCase(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể hủy lịch hẹn đã hoàn thành!"
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy lịch hẹn " + id + " không?",
                "Xác nhận hủy lịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {

            if (lichHenDAO.cancelAppointment(id)) {

                AuditLog.event(
                        "UPDATE",
                        "Hủy lịch hẹn - mã " + id
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Hủy lịch hẹn thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();
                loadAppointments();
            }
        }
    }

    private JTable getActiveTable() {

        switch (tabbedPane.getSelectedIndex()) {

            case 0:
                return tablePending;

            case 1:
                return tableConfirmed;

            default:
                return tableHistory;
        }
    }

    private DefaultTableModel getActiveModel() {

        switch (tabbedPane.getSelectedIndex()) {

            case 0:
                return tableModelPending;

            case 1:
                return tableModelConfirmed;

            default:
                return tableModelHistory;
        }
    }

    // ==================== CLEAR FORM ====================

    private void clearForm() {

        isEditing = false;
        selectedRowIndex = -1;

        txtAppId.setText("");
        txtAppId.setEnabled(true);

        txtCustomerName.setText("");
        txtPhone.setText("");

        if (cbService.getItemCount() > 0) {
            cbService.setSelectedIndex(0);
        }

        if (cbDoctor.getItemCount() > 0) {
            cbDoctor.setSelectedIndex(0);
        }

        txtAppDate.setText("");
        txtAppTime.setText("");

        tablePending.clearSelection();
        tableConfirmed.clearSelection();
        tableHistory.clearSelection();
    }

    // ==================== SEARCH ====================

    private void performSearch() {

        String keyword = txtSearch.getText().trim();

        RowFilter<DefaultTableModel, Object> filter = null;

        if (!keyword.isEmpty()) {

            filter = RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(keyword)
            );
        }

        rowSorterPending.setRowFilter(filter);
        rowSorterConfirmed.setRowFilter(filter);
        rowSorterHistory.setRowFilter(filter);
    }

    // ==================== TABLE STYLE ====================

    private void styleTable(JTable table) {

        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);

        table.setGridColor(new Color(239, 243, 248));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        table.setSelectionBackground(BLUE_LIGHT);
        table.setSelectionForeground(TEXT_DARK);

        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(239, 245, 252));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(7).setCellRenderer(
                new StatusRenderer()
        );

        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    // ==================== STATUS RENDERER ====================

    private class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
            );

            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));

            String status = value == null ? "" : value.toString();

            if (isSelected) {

                label.setBackground(BLUE_LIGHT);
                label.setForeground(TEXT_DARK);

            } else if (status.equalsIgnoreCase("Chờ xác nhận")) {

                label.setBackground(BLUE_LIGHT);
                label.setForeground(BLUE_PRIMARY);

            } else if (status.equalsIgnoreCase("Đã xác nhận")) {

                label.setBackground(GREEN_LIGHT);
                label.setForeground(GREEN_ACCENT);

            } else if (status.equalsIgnoreCase("Đã hủy")) {

                label.setBackground(RED_LIGHT);
                label.setForeground(RED_ACCENT);

            } else {

                label.setBackground(ORANGE_LIGHT);
                label.setForeground(ORANGE_ACCENT);
            }

            return label;
        }
    }

    // ==================== BUTTON ====================

    private JButton createButton(String text, Color background) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color color = background;

                if (getModel().isPressed()) {
                    color = background.darker();
                } else if (getModel().isRollover()) {
                    color = background.brighter();
                }

                g2.setColor(color);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 10, 10
                );

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

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // ==================== PLACEHOLDER TEXT FIELD ====================

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

                g2.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );

                g2.setColor(new Color(160, 170, 185));

                Insets insets = getInsets();

                FontMetrics fm = g2.getFontMetrics(getFont());

                int y = (getHeight() - fm.getHeight()) / 2
                        + fm.getAscent();

                g2.drawString(
                        placeholder,
                        insets.left + 2,
                        y
                );

                g2.dispose();
            }
        }
    }

    // ==================== ROUNDED PANEL ====================

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

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);

            g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(),
                    radius, radius
            );

            g2.setColor(BORDER_LIGHT);

            g2.drawRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1,
                    radius, radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // ==================== REFRESH DATA ====================

    public void refreshData() {
        loadServices();
        loadDoctors();
        loadAppointments();
    }
}
