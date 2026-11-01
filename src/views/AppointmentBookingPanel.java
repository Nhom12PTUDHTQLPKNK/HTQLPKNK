
package views;

import Audit.AuditLog;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Đặt lịch hẹn tại phòng khám nha khoa Smile Dental.
 */
public class AppointmentBookingPanel extends JPanel {

<<<<<<< HEAD
    //COLORS 

    private static final Color BLUE_PRIMARY = new Color(45, 112, 220);
    private static final Color BLUE_LIGHT = new Color(232, 241, 255);
    private static final Color GREEN_ACCENT = new Color(35, 160, 115);
    private static final Color GREEN_LIGHT = new Color(229, 247, 239);
    private static final Color RED_ACCENT = new Color(220, 75, 85);
    private static final Color ORANGE_ACCENT = new Color(235, 153, 55);
    private static final Color ORANGE_LIGHT = new Color(255, 245, 225);

    private static final Color BG_CANVAS = new Color(244, 248, 253);
=======
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(24, 117, 58);
    private static final Color PINK_ACCENT = new Color(174, 35, 86);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
>>>>>>> 0fba35ebfccd35d7e3fac9dccd9dcda3b89a86dc
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(35, 52, 78);
    private static final Color TEXT_GRAY = new Color(119, 135, 157);
    private static final Color BORDER_LIGHT = new Color(225, 233, 243);
    private static final Color READONLY_BG = new Color(243, 246, 250);

    // CONSTANTS

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

    // MODEL

    private static class ServiceItem {
        final String id;
        final String name;

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
        final String id;
        final String name;

        DoctorItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // FORM
    private JTextField txtAppId;
    private JTextField txtCustId;
    private JTextField txtPhone;
    private JTextField txtCustName;

    private JComboBox<ServiceItem> cbService;
    private JComboBox<DoctorItem> cbDoctor;

    private JTextArea txtFreeSlots;
    private JSpinner spDate;
    private JComboBox<String> cbTime;

    private JLabel lblCustomerStatus;

    private DefaultTableModel modelDayTable;
    private JLabel lblDayTableTitle;

    private boolean isDateSelected = false;
    private boolean existingCustomer = false;
    private String existingCustomerId = null;
    private String lastLookedPhone = "";

    private boolean loading = false;

    //CONSTRUCTOR 

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

    // PUBLIC

    public void refreshData() {

        loading = true;

        try {
            loadServices();
            loadDoctors();

            // Mỗi lần mở màn hình sẽ hiển thị tất cả lịch hẹn trước.
            isDateSelected = false;

        } finally {
            loading = false;
        }

        refreshPreviewIds();
        refreshAvailability();
    }

    // hEADER 

    private JPanel createHeaderPanel() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Hàng tiêu đề gồm ảnh và chữ
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Đọc ảnh từ thư mục src/img
        JLabel iconLabel = new JLabel();

        java.net.URL imageURL = getClass().getResource("/img/rang-tiepnhan.png");

        if (imageURL != null) {
            ImageIcon originalIcon = new ImageIcon(imageURL);

            Image scaledImage = originalIcon.getImage().getScaledInstance(
                    38, 38, Image.SCALE_SMOOTH
            );

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
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(420, 0));

        JLabel heading = new JLabel("Thông tin lịch hẹn");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(TEXT_DARK);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        txtAppId = createTextField(false);
        txtCustId = createTextField(false);

        addRow(form, "Mã lịch hẹn", txtAppId);
        addRow(form, "Mã khách hàng", txtCustId);

        txtPhone = createTextField(true);
        addRow(form, "Số điện thoại", txtPhone);

        lblCustomerStatus = new JLabel(" ");
        lblCustomerStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCustomerStatus.setOpaque(true);
        lblCustomerStatus.setBorder(new EmptyBorder(5, 9, 5, 9));
        lblCustomerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCustomerStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        form.add(lblCustomerStatus);
        form.add(Box.createVerticalStrut(12));

        txtCustName = createTextField(true);
        addRow(form, "Tên khách hàng", txtCustName);

        cbService = new JComboBox<>();
        cbDoctor = new JComboBox<>();

        styleCombo(cbService);
        styleCombo(cbDoctor);

        addRow(form, "Dịch vụ khám", cbService);
        addRow(form, "Nha sĩ phụ trách", cbDoctor);

        // Giờ trống

        txtFreeSlots = new JTextArea(2, 20);
        txtFreeSlots.setEditable(false);
        txtFreeSlots.setLineWrap(true);
        txtFreeSlots.setWrapStyleWord(true);
        txtFreeSlots.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtFreeSlots.setForeground(GREEN_ACCENT);
        txtFreeSlots.setBackground(GREEN_LIGHT);
        txtFreeSlots.setBorder(new EmptyBorder(7, 10, 7, 10));

        JScrollPane freeScroll = new JScrollPane(txtFreeSlots);
        freeScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        freeScroll.setPreferredSize(new Dimension(360, 60));
        freeScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        addRow(form, "Giờ trống của nha sĩ", freeScroll, 60);

        // Ngày hẹn

        spDate = new JSpinner(
                new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)
        );

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spDate, "dd/MM/yyyy");
        spDate.setEditor(dateEditor);
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addRow(form, "Ngày hẹn (dd/MM/yyyy)", spDate);

        // Giờ hẹn

        cbTime = new JComboBox<>();
        styleCombo(cbTime);

        addRow(form, "Giờ hẹn", cbTime);

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

        // Sự kiện tìm khách hàng

        txtPhone.addActionListener(e -> lookupCustomer(true));

        txtPhone.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookupCustomer(true);
            }
        });

        txtPhone.getDocument().addDocumentListener(new DocumentListener() {

            private void changed() {
                SwingUtilities.invokeLater(() -> lookupCustomer(false));
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
        });

        // Nút lưu

        JButton btnSave = createButton("Lưu lịch hẹn", GREEN_ACCENT);
        btnSave.setPreferredSize(new Dimension(0, 44));
        btnSave.addActionListener(e -> performSave());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        card.add(heading, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(btnSave, BorderLayout.SOUTH);

        setCustomerBadge(
                "Nhập số điện thoại để kiểm tra khách hàng",
                TEXT_GRAY,
                READONLY_BG
        );

        return card;
    }

    // DAY TABLE CARD

    private JPanel createDayTableCard() {

        RoundedPanel card = new RoundedPanel(16, CARD_BG);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        lblDayTableTitle = new JLabel("Tất cả lịch hẹn hiện có");
        lblDayTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDayTableTitle.setForeground(TEXT_DARK);

        //THANH TIÊU ĐỀ 

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        titlePanel.add(lblDayTableTitle, BorderLayout.WEST);

        // Nút tải lại
        JButton btnReload = createButton("Tải lại", BLUE_PRIMARY);
        btnReload.setPreferredSize(new Dimension(105, 34));
        btnReload.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnReload.addActionListener(e -> {

            // Bỏ trạng thái lọc theo ngày
            isDateSelected = false;

            // Tải lại toàn bộ lịch hẹn từ database
            refreshAvailability();

            // Thông báo tải lại thành công
            JOptionPane.showMessageDialog(
                    this,
                    "Đã tải lại danh sách lịch hẹn!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        titlePanel.add(btnReload, BorderLayout.EAST);

        // BẢNG LỊCH HẸN

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

        JTable table = new JTable(modelDayTable);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void styleTable(JTable table) {

        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(239, 243, 248));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(BLUE_LIGHT);
        table.setSelectionForeground(TEXT_DARK);

        // Căn giữa tiêu đề các cột
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(239, 245, 252));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer =
                (DefaultTableCellRenderer) header.getDefaultRenderer();

        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Căn giữa nội dung các ô
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    // UI 

    private JTextField createTextField(boolean editable) {

        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT_DARK);
        field.setEditable(editable);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(5, 10, 5, 10)
        ));

        if (!editable) {
            field.setBackground(READONLY_BG);
        }

        return field;
    }

    private void styleCombo(JComboBox<?> combo) {

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_DARK);
    }

    private void addRow(JPanel panel, String labelText, JComponent field) {
        addRow(panel, labelText, field, 36);
    }

    private void addRow(
            JPanel panel,
            String labelText,
            JComponent field,
            int height
    ) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

    private void setCustomerBadge(String text, Color fg, Color bg) {

        lblCustomerStatus.setText(text);
        lblCustomerStatus.setForeground(fg);
        lblCustomerStatus.setBackground(bg);
    }

    private JButton createButton(String text, Color background) {

        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(background);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final Color normal = background;
        final Color hover = background.darker();

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normal);
            }
        });

        return button;
    }

    // tải lại

    private void loadServices() {

        cbService.removeAllItems();

        String sql = "SELECT maDichVu, tenDichVu FROM DichVu ORDER BY tenDichVu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbService.addItem(new ServiceItem(
                        rs.getString("maDichVu"),
                        rs.getString("tenDichVu")
                ));
            }

        } catch (SQLException e) {
            showError("Không thể tải danh sách dịch vụ!\n" + e.getMessage());
        }
    }

    // LOAD DOCTORS 

    private void loadDoctors() {

        DoctorItem old = (DoctorItem) cbDoctor.getSelectedItem();
        String oldId = old == null ? null : old.id;

        cbDoctor.removeAllItems();

        String sql = "SELECT maNhanVien, tenNhanVien FROM NhanVien "
                + "WHERE chucVu LIKE N'%Bác sĩ%' OR chucVu LIKE N'%Nha sĩ%' "
                + "ORDER BY tenNhanVien";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbDoctor.addItem(new DoctorItem(
                        rs.getString("maNhanVien"),
                        rs.getString("tenNhanVien")
                ));
            }

        } catch (SQLException e) {
            showError("Không thể tải danh sách nha sĩ!\n" + e.getMessage());
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

    // thong tin

    private String generateId(
            Connection conn,
            String table,
            String column,
            String prefix,
            boolean lock
    ) throws SQLException {

        String sql = "SELECT ISNULL(MAX(TRY_CONVERT(INT, SUBSTRING("
                + column + ", 3, 18))), 0) + 1 "
                + "FROM " + table
                + (lock ? " WITH (UPDLOCK, HOLDLOCK)" : "")
                + " WHERE " + column + " LIKE '" + prefix + "%'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format(prefix + "%03d", rs.getInt(1));
            }
        }

        return prefix + "001";
    }

    private void refreshPreviewIds() {

        try (Connection conn = DBConnection.getConnection()) {

            txtAppId.setText(
                    generateId(conn, "LichHen", "maLichHen", "LH", false)
            );

            if (!existingCustomer) {

                String phone = txtPhone.getText().trim();

                if (phone.matches("\\d{10,15}")) {
                    txtCustId.setText(
                            generateId(conn, "KhachHang", "maKhachHang", "KH", false)
                                    + " (mới)"
                    );
                } else {
                    txtCustId.setText("");
                }
            }

        } catch (SQLException e) {
            txtAppId.setText("Tự tạo khi lưu");
        }
    }

    // khachhang

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
                TEXT_GRAY,
                READONLY_BG
        );
    }

    private void lookupCustomer(boolean force) {

        String phone = txtPhone.getText().trim();

        if (!phone.matches("\\d{10,15}")) {

            if (!phone.isEmpty() && phone.length() >= 10 && force) {
                setCustomerBadge(
                        "Số điện thoại phải gồm 10 đến 15 chữ số",
                        RED_ACCENT,
                        new Color(255, 235, 237)
                );
            } else {
                resetCustomerState();
            }

            return;
        }

        if (!force && phone.equals(lastLookedPhone)) {
            return;
        }

        String sql = "SELECT TOP 1 maKhachHang, tenKH, diemTichLuy "
                + "FROM KhachHang WHERE sdt = ? ORDER BY maKhachHang";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
                            "Khách hàng cũ - " + rs.getInt("diemTichLuy")
                                    + " điểm tích lũy. Chỉ tạo lịch hẹn mới.",
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
                            generateId(conn, "KhachHang", "maKhachHang", "KH", false)
                                    + " (mới)"
                    );

                    setCustomerBadge(
                            "Khách hàng mới - hồ sơ sẽ được tạo khi lưu lịch hẹn.",
                            ORANGE_ACCENT,
                            ORANGE_LIGHT
                    );
                }
            }

        } catch (SQLException e) {
            showError("Lỗi tra cứu khách hàng!\n" + e.getMessage());
        }
    }

    // AVAILABILITY

    private LocalDate getSelectedDate() {

        Date value = (Date) spDate.getValue();

        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /**
     * Chuyển giờ lấy từ SQL Server về định dạng HH:mm.
     * Ví dụ: 08:30:00 -> 08:30.
     */
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

    /**
     * Tải tất cả lịch hẹn có trong database.
     * Được gọi khi mới mở màn hình.
     */
    private void loadAllAppointments() {

        modelDayTable.setRowCount(0);

        String sql = "SELECT LH.maLichHen, LH.gioKham, "
                + "KH.tenKH, KH.sdt, LH.ghiChu, LH.trangThai "
                + "FROM LichHen LH "
                + "LEFT JOIN KhachHang KH "
                + "ON LH.maKhachHang = KH.maKhachHang "
                + "ORDER BY LH.ngayHen DESC, LH.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
            showError("Không thể tải tất cả lịch hẹn!\n" + e.getMessage());
        }
    }

    /**
     * Tải lịch hẹn theo ngày được chọn.
     * Không giới hạn nha sĩ: hiển thị lịch của tất cả nha sĩ trong ngày đó.
     */
    private void loadAppointmentsByDate(LocalDate date) {

        modelDayTable.setRowCount(0);

        String sql = "SELECT LH.maLichHen, LH.gioKham, "
                + "KH.tenKH, KH.sdt, LH.ghiChu, LH.trangThai "
                + "FROM LichHen LH "
                + "LEFT JOIN KhachHang KH "
                + "ON LH.maKhachHang = KH.maKhachHang "
                + "WHERE LH.ngayHen = ? "
                + "ORDER BY LH.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

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
            showError("Không thể tải lịch hẹn của ngày đã chọn!\n" + e.getMessage());
        }
    }

    /**
     * Cập nhật giờ trống của nha sĩ và bảng lịch hẹn bên phải.
     */
    private void refreshAvailability() {

        DoctorItem doctor = (DoctorItem) cbDoctor.getSelectedItem();

        String previousTime = (String) cbTime.getSelectedItem();

        cbTime.removeAllItems();
        modelDayTable.setRowCount(0);

        // Chưa có nha sĩ

        if (doctor == null) {

            txtFreeSlots.setText("Chưa có nha sĩ để chọn");
            txtFreeSlots.setForeground(RED_ACCENT);

            if (!isDateSelected) {
                lblDayTableTitle.setText("Tất cả lịch hẹn hiện có");
                loadAllAppointments();
            } else {
                LocalDate date = getSelectedDate();

                lblDayTableTitle.setText(
                        "Lịch hẹn ngày " + String.format(
                                "%02d/%02d/%d",
                                date.getDayOfMonth(),
                                date.getMonthValue(),
                                date.getYear()
                        )
                );

                loadAppointmentsByDate(date);
            }

            return;
        }

        LocalDate date = getSelectedDate();
        LocalDate today = LocalDate.now();
        
        // BẢNG BÊN PHẢI
        if (!isDateSelected) {

            // Chưa chọn ngày: hiển thị tất cả lịch hẹn.

            lblDayTableTitle.setText("Tất cả lịch hẹn hiện có");
            loadAllAppointments();

        } else {

            // Đã chọn ngày: hiển thị tất cả lịch hẹn trong ngày đó.

            lblDayTableTitle.setText(
                    "Lịch hẹn ngày " + String.format(
                            "%02d/%02d/%d",
                            date.getDayOfMonth(),
                            date.getMonthValue(),
                            date.getYear()
                    )
            );

            loadAppointmentsByDate(date);
        }

        // KIỂM TRA GIỜ TRỐNG CỦA NHA SĨ
        
        if (date.isBefore(today)) {

            txtFreeSlots.setText(
                    "Ngày hẹn nằm trong quá khứ, vui lòng chọn ngày khác"
            );

            txtFreeSlots.setForeground(RED_ACCENT);
            return;
        }

        Set<String> busy = new HashSet<>();

        String sql = "SELECT gioKham, trangThai FROM LichHen "
                + "WHERE maNhanVien = ? AND ngayHen = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.id);
            ps.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String time = formatTime(rs.getString("gioKham"));
                    String status = rs.getString("trangThai");

                    if (!time.isEmpty()
                            && !"Đã hủy".equalsIgnoreCase(
                                    status == null ? "" : status.trim())) {

                        busy.add(time);
                    }
                }
            }

        } catch (SQLException e) {

            showError("Không thể kiểm tra giờ trống!\n" + e.getMessage());
            return;
        }

        List<String> free = new ArrayList<>();

        LocalTime now = LocalTime.now();

        for (String slot : STANDARD_TIMES) {

            if (busy.contains(slot)) {
                continue;
            }

            if (date.equals(today)
                    && !LocalTime.parse(slot).isAfter(now)) {
                continue;
            }

            free.add(slot);
        }

        if (free.isEmpty()) {

            txtFreeSlots.setText("Nha sĩ đã kín lịch trong ngày này");
            txtFreeSlots.setForeground(RED_ACCENT);

            return;
        }

        txtFreeSlots.setText(String.join("   ", free));
        txtFreeSlots.setForeground(GREEN_ACCENT);

        for (String slot : free) {
            cbTime.addItem(slot);
        }

        if (previousTime != null && free.contains(previousTime)) {
            cbTime.setSelectedItem(previousTime);
        }
    }

    //  lưu

    private void performSave() {

        String phone = txtPhone.getText().trim();
        String name = txtCustName.getText().trim();

        ServiceItem service = (ServiceItem) cbService.getSelectedItem();
        DoctorItem doctor = (DoctorItem) cbDoctor.getSelectedItem();
        String time = (String) cbTime.getSelectedItem();

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
            showWarning(
                    "Chưa có dịch vụ hoặc nha sĩ. Vui lòng kiểm tra dữ liệu trong hệ thống!"
            );
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showWarning("Ngày hẹn không được nằm trong quá khứ!");
            return;
        }

        if (time == null) {
            showWarning(
                    "Nha sĩ không còn giờ trống trong ngày này, vui lòng chọn ngày hoặc nha sĩ khác!"
            );
            return;
        }

        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        String savedAppId;
        String savedCustId;
        boolean createdNewCustomer = false;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Tìm khách hàng theo số điện thoại.

                String customerId = null;

                String sqlFind = "SELECT TOP 1 maKhachHang "
                        + "FROM KhachHang WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE sdt = ? ORDER BY maKhachHang";

                try (PreparedStatement ps = conn.prepareStatement(sqlFind)) {

                    ps.setString(1, phone);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            customerId = rs.getString("maKhachHang");
                        }
                    }
                }

                // 2. Tạo khách hàng mới nếu chưa tồn tại.

                if (customerId == null) {

                    customerId = generateId(
                            conn, "KhachHang", "maKhachHang", "KH", true
                    );

                    String sqlInsertCustomer = "INSERT INTO KhachHang "
                            + "(maKhachHang, tenKH, sdt, diemTichLuy) "
                            + "VALUES (?, ?, ?, 0)";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlInsertCustomer)) {

                        ps.setString(1, customerId);
                        ps.setString(2, name);
                        ps.setString(3, phone);
                        ps.executeUpdate();
                    }

                    createdNewCustomer = true;
                }

                // 3. Kiểm tra trùng lịch nha sĩ.

                String sqlDup = "SELECT COUNT(*) FROM LichHen "
                        + "WHERE maNhanVien = ? AND ngayHen = ? AND gioKham = ? "
                        + "AND ISNULL(trangThai, N'') <> N'Đã hủy'";

                try (PreparedStatement ps = conn.prepareStatement(sqlDup)) {

                    ps.setString(1, doctor.id);
                    ps.setDate(2, sqlDate);
                    ps.setString(3, time);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next() && rs.getInt(1) > 0) {

                            conn.rollback();

                            showWarning(
                                    "Nha sĩ " + doctor.name
                                            + " vừa có người đặt lúc " + time
                                            + ". Vui lòng chọn giờ khác!"
                            );

                            refreshAvailability();
                            return;
                        }
                    }
                }

                // 4. Tạo lịch hẹn.

                savedAppId = generateId(
                        conn, "LichHen", "maLichHen", "LH", true
                );

                String sqlInsertApp = "INSERT INTO LichHen "
                        + "(maLichHen, maKhachHang, maNhanVien, "
                        + "ngayHen, gioKham, trangThai, ghiChu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(sqlInsertApp)) {

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

            showError("Không thể lưu lịch hẹn!\n" + ex.getMessage());
            return;
        }

        // Ghi nhật ký.

        try {

            AuditLog.event(
                    "CREATE",
                    "Đặt lịch hẹn - mã " + savedAppId
                            + (createdNewCustomer
                            ? " (tạo khách hàng mới " + savedCustId + ")"
                            : "")
            );

        } catch (Exception ignored) {
            // Không để lỗi ghi nhật ký ảnh hưởng việc đặt lịch.
        }

        JOptionPane.showMessageDialog(
                this,
                "Đặt lịch hẹn thành công!\n"
                        + "Mã lịch hẹn: " + savedAppId + "\n"
                        + "Mã khách hàng: " + savedCustId
                        + (createdNewCustomer
                        ? " (khách hàng mới)"
                        : " (khách hàng cũ)") + "\n"
                        + "Trạng thái: " + STATUS_PENDING,
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
        );

        clearForm();
    }

    // CLEAR FORM

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

        setCustomerBadge(
                "Nhập số điện thoại để kiểm tra khách hàng",
                TEXT_GRAY,
                READONLY_BG
        );

        refreshPreviewIds();
        refreshAvailability();
    }

    //MESSAGES 

    private void showWarning(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }

    //ROUNDED PANEL 

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {

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
                    0, 0, getWidth(), getHeight(), radius, radius
            );

            g2.setColor(BORDER_LIGHT);
            g2.drawRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1, radius, radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
