package views;

import Audit.AuditLog;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppointmentBookingPanel extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    // Các khung giờ chuẩn (dùng chung cho combobox và kiểm tra giờ trống)
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

    private JTable tableLichDat;
    private DefaultTableModel modelLichDat;
    private TableRowSorter<DefaultTableModel> sorterLichDat;

    private JTextField txtSearchPhone;

    private JTextField txtAppId;
    private JTextField txtCustId;
    private JTextField txtCustName;
    private JTextField txtPhone;
    private JTextField txtAppDate;

    private JComboBox<ServiceItem> cbService;
    private JComboBox<EmployeeItem> cbDoctor;
    private JComboBox<String> cbStatus;
    private JComboBox<String> cbTime;

    private JLabel lblCustomerStatus;

    private boolean isEditing = false;

    // =====================================================
    // MODEL COMBOBOX
    // =====================================================

    private static class ServiceItem {

        String id;
        String name;
        double price;

        ServiceItem(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class EmployeeItem {

        String id;
        String name;
        String position;

        EmployeeItem(String id, String name, String position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AppointmentBookingPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        createInterface();

        // Lấy dữ liệu trực tiếp từ SQL Server
        loadServices();
        loadDoctors();
        loadAppointments();

        clearForm();
    }

    // =====================================================
    // LÀM MỚI DỮ LIỆU KHI MỞ MÀN HÌNH (MenuFrame gọi hàm này)
    // =====================================================

    public void refreshData() {

        // Nhớ lại lựa chọn hiện tại để không mất khi tải lại
        ServiceItem oldService = (ServiceItem) cbService.getSelectedItem();
        EmployeeItem oldDoctor = (EmployeeItem) cbDoctor.getSelectedItem();

        String oldServiceName = oldService == null ? null : oldService.name;
        String oldDoctorId = oldDoctor == null ? null : oldDoctor.id;

        loadServices();
        loadDoctors();

        if (oldServiceName != null) {
            selectServiceByName(oldServiceName);
        }

        if (oldDoctorId != null) {
            selectDoctorById(oldDoctorId);
        }

        loadAppointments();

        if (!isEditing) {
            refreshNextAppointmentId();
        }
    }

    // =====================================================
    // GIAO DIỆN CHÍNH
    // =====================================================

    private void createInterface() {

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(
            "Quản Lý & Đặt Lịch Hẹn Khách Hàng"
        );

        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(
            "Thêm mới lịch hẹn hoặc nhập SĐT để tra cứu thông tin khách hàng"
        );

        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel toolbarPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        toolbarPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Nhập SĐT:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtSearchPhone = new JTextField(14);
        txtSearchPhone.setPreferredSize(new Dimension(150, 36));
        styleTextField(txtSearchPhone);

        txtSearchPhone.addActionListener(e -> performPhoneLookup());

        JButton btnSearch = createButton(
            "Tra Cứu", BLUE_PRIMARY
        );

        btnSearch.addActionListener(e -> performPhoneLookup());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearchPhone);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // BẢNG LỊCH HẸN
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

        String[] columns = {
            "Mã Lịch Hẹn",
            "Mã KH",
            "Tên Khách Hàng",
            "SĐT",
            "Dịch vụ khám",
            "Nha sĩ phụ trách",
            "Ngày hẹn",
            "Giờ",
            "Trạng thái"
        };

        modelLichDat = new DefaultTableModel(null, columns) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableLichDat = new JTable(modelLichDat);

        sorterLichDat = new TableRowSorter<>(modelLichDat);
        tableLichDat.setRowSorter(sorterLichDat);

        styleTable(tableLichDat);

        tableLichDat.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()
                    && tableLichDat.getSelectedRow() != -1) {

                loadDataToFormForEdit();
            }
        });

        JScrollPane scroll = new JScrollPane(tableLichDat);

        scroll.setBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT)
        );

        scroll.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scroll, BorderLayout.CENTER);

        JPanel formPanel = createFormPanel();

        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
        centerPanel.setOpaque(false);

        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(formPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);
    }

    // =====================================================
    // FORM ĐẶT LỊCH
    // =====================================================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);

        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(18, 18, 18, 18)
            )
        );

        panel.setPreferredSize(new Dimension(330, 0));

        JLabel lblTitle = new JLabel("Thông Tin Đặt Lịch Hẹn");

        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(14));

        txtAppId = addTextField(panel, "Mã lịch hẹn:");
        txtAppId.setEditable(false);

        txtCustId = addTextField(panel, "Mã khách hàng:");
        txtCustId.setEditable(false);

        txtCustName = addTextField(panel, "Tên khách hàng:");
        txtPhone = addTextField(panel, "Số điện thoại:");

        lblCustomerStatus = new JLabel(
            "Chưa kiểm tra khách hàng"
        );

        lblCustomerStatus.setFont(
            new Font("Segoe UI", Font.BOLD, 11)
        );

        lblCustomerStatus.setForeground(MUTED);
        lblCustomerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblCustomerStatus);
        panel.add(Box.createVerticalStrut(12));

        txtPhone.addActionListener(e -> lookupCustomerByPhone());

        // DỊCH VỤ LẤY TỪ SQL
        cbService = new JComboBox<>();

        addComboBox(panel, "Dịch vụ khám:", cbService);

        // NHA SĨ LẤY TỪ SQL
        cbDoctor = new JComboBox<>();

        addComboBox(panel, "Nha sĩ phụ trách:", cbDoctor);

        txtAppDate = addTextField(
            panel, "Ngày hẹn (dd/MM/yyyy):"
        );

        cbTime = new JComboBox<>(STANDARD_TIMES);

        addComboBox(panel, "Giờ hẹn:", cbTime);

        JButton btnAvailable = createButton(
            "Xem giờ trống của nha sĩ", BLUE_PRIMARY
        );

        btnAvailable.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 36)
        );

        btnAvailable.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAvailable.addActionListener(e -> showAvailableTimes());

        panel.add(btnAvailable);
        panel.add(Box.createVerticalStrut(12));

        cbStatus = new JComboBox<>(new String[]{
            "Chờ xác nhận",
            "Đã xác nhận",
            "Hoàn thành",
            "Đã hủy"
        });

        addComboBox(panel, "Trạng thái:", cbStatus);

        JPanel buttonPanel = new JPanel(
            new GridLayout(1, 2, 10, 0)
        );

        buttonPanel.setOpaque(false);

        buttonPanel.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 38)
        );

        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createButton(
            "Lưu Lịch", GREEN_ACCENT
        );

        JButton btnClear = createButton(
            "Làm Mới", PINK_ACCENT
        );

        btnSave.addActionListener(e -> performSave());
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnClear);

        panel.add(buttonPanel);

        return panel;
    }

    // =====================================================
    // HÀM TẠO GIAO DIỆN
    // =====================================================

    private JTextField addTextField(
            JPanel panel, String label) {

        JLabel lbl = new JLabel(label);

        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));

        JTextField txt = new JTextField();

        styleTextField(txt);

        txt.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 34)
        );

        txt.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(txt);
        panel.add(Box.createVerticalStrut(10));

        return txt;
    }

    private void addComboBox(
            JPanel panel,
            String label,
            JComboBox<?> combo) {

        JLabel lbl = new JLabel(label);

        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));

        combo.setFont(
            new Font("Segoe UI", Font.PLAIN, 13)
        );

        combo.setBackground(Color.WHITE);

        combo.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 34)
        );

        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(combo);
        panel.add(Box.createVerticalStrut(10));
    }

    private void styleTextField(JTextField txt) {

        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txt.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(4, 8, 4, 8)
            )
        );
    }

    private JButton createButton(String text, Color color) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        btn.setPreferredSize(new Dimension(100, 36));

        return btn;
    }

    private void styleTable(JTable table) {

        table.setRowHeight(38);

        table.setFont(
            new Font("Segoe UI", Font.PLAIN, 13)
        );

        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));

        table.setSelectionBackground(
            new Color(225, 240, 255)
        );

        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(
            new Color(245, 248, 253)
        );

        table.getTableHeader().setForeground(TEXT_DARK);

        table.getTableHeader().setPreferredSize(
            new Dimension(0, 36)
        );
    }

    // =====================================================
    // LOAD DỊCH VỤ TỪ DATABASE
    // =====================================================

    private void loadServices() {

        cbService.removeAllItems();

        String sql =
            "SELECT maDichVu, tenDichVu, donGia "
            + "FROM DichVu "
            + "ORDER BY tenDichVu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                ServiceItem item = new ServiceItem(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getDouble("donGia")
                );

                cbService.addItem(item);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể tải danh sách dịch vụ!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // LOAD NHA SĨ TỪ DATABASE
    // =====================================================

    private void loadDoctors() {

        cbDoctor.removeAllItems();

        String sql =
            "SELECT maNhanVien, tenNhanVien, chucVu "
            + "FROM NhanVien "
            + "WHERE chucVu LIKE N'%Bác sĩ%' "
            + "ORDER BY tenNhanVien";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                EmployeeItem item = new EmployeeItem(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getString("chucVu")
                );

                cbDoctor.addItem(item);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể tải danh sách nha sĩ!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // LOAD LỊCH HẸN TỪ DATABASE
    // =====================================================

    private void loadAppointments() {

        modelLichDat.setRowCount(0);

        // Tên dịch vụ được lưu trong cột ghiChu nên lấy trực tiếp LH.ghiChu
        String sql =
            "SELECT "
            + "LH.maLichHen, "
            + "KH.maKhachHang, "
            + "KH.tenKH, "
            + "KH.sdt, "
            + "LH.ghiChu, "
            + "NV.tenNhanVien, "
            + "CONVERT(VARCHAR(10), LH.ngayHen, 103) AS ngayHen, "
            + "LH.gioKham, "
            + "LH.trangThai "
            + "FROM LichHen LH "
            + "INNER JOIN KhachHang KH "
            + "ON LH.maKhachHang = KH.maKhachHang "
            + "LEFT JOIN NhanVien NV "
            + "ON LH.maNhanVien = NV.maNhanVien "
            + "ORDER BY LH.ngayHen DESC, LH.gioKham DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Object[] row = {
                    rs.getString("maLichHen"),
                    rs.getString("maKhachHang"),
                    rs.getString("tenKH"),
                    rs.getString("sdt"),
                    rs.getString("ghiChu"),
                    rs.getString("tenNhanVien"),
                    rs.getString("ngayHen"),
                    rs.getString("gioKham"),
                    rs.getString("trangThai")
                };

                modelLichDat.addRow(row);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể tải danh sách lịch hẹn!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // TRA CỨU KHÁCH HÀNG BẰNG SĐT
    // =====================================================

    private boolean lookupCustomerByPhone() {

        String phone = txtPhone.getText().trim();

        if (phone.isEmpty()) {
            return false;
        }

        String sql =
            "SELECT maKhachHang, tenKH, diemTichLuy "
            + "FROM KhachHang "
            + "WHERE sdt = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    txtCustId.setText(
                        rs.getString("maKhachHang")
                    );

                    txtCustName.setText(
                        rs.getString("tenKH")
                    );

                    int points = rs.getInt("diemTichLuy");

                    lblCustomerStatus.setText(
                        "Khách hàng cũ - "
                        + points + " điểm tích lũy"
                    );

                    lblCustomerStatus.setForeground(
                        BLUE_PRIMARY
                    );

                    return true;
                }
            }

            txtCustId.setText("");

            lblCustomerStatus.setText(
                "Khách hàng mới - Tạo hồ sơ khi lưu"
            );

            lblCustomerStatus.setForeground(
                new Color(220, 140, 20)
            );

            return false;

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Lỗi tra cứu khách hàng!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );

            return false;
        }
    }

    // =====================================================
    // THOÁT CHẾ ĐỘ SỬA (để đặt lịch mới)
    // =====================================================

    private void leaveEditMode() {

        if (!isEditing) {
            return;
        }

        isEditing = false;

        tableLichDat.clearSelection();

        refreshNextAppointmentId();
    }

    // =====================================================
    // TRA CỨU LỊCH HẸN BẰNG SĐT
    // =====================================================

    private void performPhoneLookup() {

        String phone = txtSearchPhone.getText().trim();

        if (phone.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Vui lòng nhập số điện thoại!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // Tra cứu để đặt lịch mới -> không giữ trạng thái đang sửa
        leaveEditMode();

        txtPhone.setText(phone);

        boolean customerExists = lookupCustomerByPhone();

        if (!customerExists) {

            JOptionPane.showMessageDialog(
                this,
                "Không tìm thấy khách hàng có số điện thoại này!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        loadAppointments();

        sorterLichDat.setRowFilter(
            RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(phone),
                3
            )
        );

        // Không tự chọn dòng đầu: chọn dòng sẽ đẩy form sang chế độ SỬA
        // và làm nút "Lưu Lịch" cập nhật lịch cũ thay vì tạo lịch mới.
        if (tableLichDat.getRowCount() == 0) {

            JOptionPane.showMessageDialog(
                this,
                "Khách hàng đã tồn tại nhưng chưa có lịch hẹn.",
                "Thông tin",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // =====================================================
    // TẠO MÃ LỊCH HẸN
    // =====================================================

    private String generateAppointmentId(
            Connection conn) throws SQLException {

        String sql =
            "SELECT ISNULL(MAX(TRY_CONVERT(INT, "
            + "SUBSTRING(maLichHen, 3, 18))), 0) + 1 "
            + "FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE maLichHen LIKE 'LH%'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format("LH%03d", rs.getInt(1));
            }
        }

        return "LH001";
    }

    // Hiển thị mã lịch hẹn kế tiếp trên form (chỉ để xem trước)
    private void refreshNextAppointmentId() {

        try (Connection conn = DBConnection.getConnection()) {

            txtAppId.setText(
                generateAppointmentId(conn)
            );

        } catch (SQLException e) {

            txtAppId.setText("Tự tạo khi lưu");
        }
    }

    // =====================================================
    // TẠO MÃ KHÁCH HÀNG
    // =====================================================

    private String generateCustomerId(
            Connection conn) throws SQLException {

        String sql =
            "SELECT ISNULL(MAX(TRY_CONVERT(INT, "
            + "SUBSTRING(maKhachHang, 3, 18))), 0) + 1 "
            + "FROM KhachHang WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE maKhachHang LIKE 'KH%'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return String.format("KH%03d", rs.getInt(1));
            }
        }

        return "KH001";
    }

    // =====================================================
    // TÌM HOẶC TẠO KHÁCH HÀNG
    // =====================================================

    private String findOrCreateCustomer(
            Connection conn,
            String name,
            String phone) throws SQLException {

        String sqlFind =
            "SELECT maKhachHang, tenKH "
            + "FROM KhachHang WITH (UPDLOCK, HOLDLOCK) "
            + "WHERE sdt = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlFind)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String id = rs.getString("maKhachHang");

                    txtCustId.setText(id);
                    txtCustName.setText(rs.getString("tenKH"));

                    return id;
                }
            }
        }

        String newId = generateCustomerId(conn);

        String sqlInsert =
            "INSERT INTO KhachHang "
            + "(maKhachHang, tenKH, sdt, diemTichLuy) "
            + "VALUES (?, ?, ?, 0)";

        try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {

            ps.setString(1, newId);
            ps.setString(2, name);
            ps.setString(3, phone);

            ps.executeUpdate();
        }

        txtCustId.setText(newId);
        txtCustName.setText(name);

        return newId;
    }

    // =====================================================
    // KIỂM TRA TRÙNG LỊCH
    // =====================================================

    private boolean isDuplicateAppointment(
            Connection conn,
            String doctorId,
            java.sql.Date appointmentDate,
            String time,
            String appointmentId) throws SQLException {

        String sql =
            "SELECT COUNT(*) "
            + "FROM LichHen "
            + "WHERE maNhanVien = ? "
            + "AND ngayHen = ? "
            + "AND gioKham = ? "
            + "AND ISNULL(trangThai, N'') <> N'Đã hủy' "
            + "AND maLichHen <> ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctorId);
            ps.setDate(2, appointmentDate);
            ps.setString(3, time);
            ps.setString(4, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // =====================================================
    // KIỂM TRA GIỜ TRỐNG
    // =====================================================

    private void showAvailableTimes() {

        EmployeeItem doctor =
            (EmployeeItem) cbDoctor.getSelectedItem();

        if (doctor == null) {

            JOptionPane.showMessageDialog(
                this,
                "Không có nha sĩ để chọn!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        java.sql.Date date;

        try {

            date = parseDate(txtAppDate.getText().trim());

        } catch (ParseException e) {

            JOptionPane.showMessageDialog(
                this,
                "Ngày phải đúng định dạng dd/MM/yyyy!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String sql =
            "SELECT gioKham "
            + "FROM LichHen "
            + "WHERE maNhanVien = ? "
            + "AND ngayHen = ? "
            + "AND ISNULL(trangThai, N'') <> N'Đã hủy' "
            + "AND maLichHen <> ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, doctor.id);
            ps.setDate(2, date);
            ps.setString(3, isEditing
                ? txtAppId.getText().trim()
                : "");

            java.util.Set<String> occupied =
                new java.util.HashSet<>();

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    occupied.add(rs.getString("gioKham"));
                }
            }

            java.util.List<String> available =
                new java.util.ArrayList<>();

            for (String time : STANDARD_TIMES) {

                if (!occupied.contains(time)) {
                    available.add(time);
                }
            }

            if (available.isEmpty()) {

                JOptionPane.showMessageDialog(
                    this,
                    "Nha sĩ không còn giờ trống trong ngày này!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            String selected = (String) JOptionPane.showInputDialog(
                this,
                "Chọn giờ khám còn trống:",
                "Giờ khám",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                available.toArray(),
                available.get(0)
            );

            if (selected != null) {
                selectTime(selected);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể kiểm tra giờ trống!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // CHUYỂN NGÀY
    // =====================================================

    private java.sql.Date parseDate(
            String dateText) throws ParseException {

        SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy");

        sdf.setLenient(false);

        Date date = sdf.parse(dateText);

        if (!sdf.format(date).equals(dateText)) {
            throw new ParseException("Ngày không hợp lệ", 0);
        }

        return new java.sql.Date(date.getTime());
    }

    // =====================================================
    // LƯU LỊCH HẸN
    // =====================================================

    private void performSave() {

        String name = txtCustName.getText().trim();
        String phone = txtPhone.getText().trim();

        ServiceItem service =
            (ServiceItem) cbService.getSelectedItem();

        EmployeeItem doctor =
            (EmployeeItem) cbDoctor.getSelectedItem();

        String dateText = txtAppDate.getText().trim();

        String time = (String) cbTime.getSelectedItem();

        String status = (String) cbStatus.getSelectedItem();

        if (name.isEmpty() || phone.isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Vui lòng nhập tên và số điện thoại khách hàng!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!phone.matches("\\d{10,15}")) {

            JOptionPane.showMessageDialog(
                this,
                "Số điện thoại phải có từ 10 đến 15 chữ số!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (service == null || doctor == null
                || time == null || status == null) {

            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn đầy đủ dịch vụ, nha sĩ, giờ và trạng thái!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        java.sql.Date appointmentDate;

        try {

            appointmentDate = parseDate(dateText);

        } catch (ParseException e) {

            JOptionPane.showMessageDialog(
                this,
                "Ngày hẹn không đúng định dạng dd/MM/yyyy!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!isEditing && appointmentDate.toLocalDate()
                .isBefore(java.time.LocalDate.now())) {

            JOptionPane.showMessageDialog(
                this,
                "Ngày hẹn không được nằm trong quá khứ!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String appointmentId = txtAppId.getText().trim();

        boolean editing = isEditing;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                if (!editing) {
                    appointmentId = generateAppointmentId(conn);
                }

                if (!"Đã hủy".equals(status)
                        && isDuplicateAppointment(
                            conn,
                            doctor.id,
                            appointmentDate,
                            time,
                            appointmentId)) {

                    conn.rollback();

                    JOptionPane.showMessageDialog(
                        this,
                        "Nha sĩ đã có lịch hẹn vào "
                        + time + " ngày " + dateText
                        + ". Vui lòng chọn giờ khác!",
                        "Trùng lịch",
                        JOptionPane.WARNING_MESSAGE
                    );

                    return;
                }

                String customerId = findOrCreateCustomer(
                    conn, name, phone
                );

                if (!editing) {

                    String sql =
                        "INSERT INTO LichHen "
                        + "(maLichHen, maKhachHang, maNhanVien, "
                        + "ngayHen, gioKham, trangThai, ghiChu) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps =
                            conn.prepareStatement(sql)) {

                        ps.setString(1, appointmentId);
                        ps.setString(2, customerId);
                        ps.setString(3, doctor.id);
                        ps.setDate(4, appointmentDate);
                        ps.setString(5, time);
                        ps.setString(6, status);

                        // Lưu tên dịch vụ trong ghiChu
                        ps.setString(7, service.name);

                        ps.executeUpdate();
                    }

                } else {

                    String sql =
                        "UPDATE LichHen SET "
                        + "maKhachHang = ?, "
                        + "maNhanVien = ?, "
                        + "ngayHen = ?, "
                        + "gioKham = ?, "
                        + "trangThai = ?, "
                        + "ghiChu = ? "
                        + "WHERE maLichHen = ?";

                    try (PreparedStatement ps =
                            conn.prepareStatement(sql)) {

                        ps.setString(1, customerId);
                        ps.setString(2, doctor.id);
                        ps.setDate(3, appointmentDate);
                        ps.setString(4, time);
                        ps.setString(5, status);
                        ps.setString(6, service.name);
                        ps.setString(7, appointmentId);

                        int result = ps.executeUpdate();

                        if (result == 0) {
                            throw new SQLException(
                                "Không tìm thấy lịch hẹn cần cập nhật!"
                            );
                        }
                    }
                }

                conn.commit();

                try {
                    AuditLog.event(
                        editing ? "UPDATE" : "CREATE",
                        "Lịch hẹn - mã " + appointmentId
                    );
                } catch (Exception ignored) {
                }

                JOptionPane.showMessageDialog(
                    this,
                    editing
                        ? "Cập nhật lịch hẹn thành công!"
                        : "Đặt lịch thành công!\nMã lịch hẹn: "
                            + appointmentId,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } catch (SQLException e) {

                conn.rollback();

                JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu lịch hẹn!\n"
                    + e.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
                );

                return;

            } finally {

                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Lỗi kết nối database!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        loadAppointments();
        clearForm();
    }

    // =====================================================
    // ĐỔ DỮ LIỆU TỪ BẢNG LÊN FORM
    // =====================================================

    private void loadDataToFormForEdit() {

        int viewRow = tableLichDat.getSelectedRow();

        if (viewRow == -1) {
            return;
        }

        int row = tableLichDat.convertRowIndexToModel(viewRow);

        String appointmentId =
            modelLichDat.getValueAt(row, 0).toString();

        String sql =
            "SELECT "
            + "LH.maLichHen, "
            + "LH.maKhachHang, "
            + "KH.tenKH, "
            + "KH.sdt, "
            + "LH.maNhanVien, "
            + "LH.ngayHen, "
            + "LH.gioKham, "
            + "LH.trangThai, "
            + "LH.ghiChu "
            + "FROM LichHen LH "
            + "JOIN KhachHang KH "
            + "ON LH.maKhachHang = KH.maKhachHang "
            + "WHERE LH.maLichHen = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    txtAppId.setText(
                        rs.getString("maLichHen")
                    );

                    txtCustId.setText(
                        rs.getString("maKhachHang")
                    );

                    txtCustName.setText(
                        rs.getString("tenKH")
                    );

                    txtPhone.setText(
                        rs.getString("sdt")
                    );

                    selectDoctorById(rs.getString("maNhanVien"));

                    // Nếu ghiChu không khớp dịch vụ nào (dữ liệu cũ)
                    // thì bỏ chọn để người dùng chọn lại, tránh ghi đè sai.
                    if (!selectServiceByName(rs.getString("ghiChu"))) {
                        cbService.setSelectedIndex(-1);
                    }

                    java.sql.Date date =
                        rs.getDate("ngayHen");

                    if (date != null) {

                        txtAppDate.setText(
                            new SimpleDateFormat("dd/MM/yyyy")
                                .format(date)
                        );
                    }

                    selectTime(rs.getString("gioKham"));

                    cbStatus.setSelectedItem(
                        rs.getString("trangThai")
                    );

                    isEditing = true;

                    lblCustomerStatus.setText(
                        "Đang chỉnh sửa lịch hẹn "
                        + appointmentId
                    );

                    lblCustomerStatus.setForeground(
                        BLUE_PRIMARY
                    );
                }
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể tải thông tin lịch hẹn!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // CHỌN NHA SĨ THEO MÃ
    // =====================================================

    private void selectDoctorById(String id) {

        if (id == null) {
            return;
        }

        for (int i = 0; i < cbDoctor.getItemCount(); i++) {

            EmployeeItem item = cbDoctor.getItemAt(i);

            if (item.id.equals(id)) {
                cbDoctor.setSelectedIndex(i);
                return;
            }
        }
    }

    // =====================================================
    // CHỌN DỊCH VỤ THEO TÊN
    // =====================================================

    private boolean selectServiceByName(String name) {

        if (name == null) {
            return false;
        }

        String target = name.trim();

        for (int i = 0; i < cbService.getItemCount(); i++) {

            ServiceItem item = cbService.getItemAt(i);

            if (item.name.equalsIgnoreCase(target)) {
                cbService.setSelectedIndex(i);
                return true;
            }
        }

        return false;
    }

    // =====================================================
    // CHỌN GIỜ (thêm vào danh sách nếu giờ cũ không có sẵn)
    // =====================================================

    private void selectTime(String time) {

        if (time == null) {
            return;
        }

        String target = time.trim();

        for (int i = 0; i < cbTime.getItemCount(); i++) {

            if (cbTime.getItemAt(i).equals(target)) {
                cbTime.setSelectedIndex(i);
                return;
            }
        }

        // Ví dụ lịch cũ giờ 09:15 không nằm trong khung giờ chuẩn
        cbTime.addItem(target);
        cbTime.setSelectedItem(target);
    }

    // =====================================================
    // LÀM MỚI FORM
    // =====================================================

    private void clearForm() {

        isEditing = false;

        txtAppId.setText("");
        txtCustId.setText("");
        txtCustName.setText("");
        txtPhone.setText("");

        txtAppDate.setText(
            new SimpleDateFormat("dd/MM/yyyy").format(new Date())
        );

        cbService.setSelectedIndex(
            cbService.getItemCount() > 0 ? 0 : -1
        );

        cbDoctor.setSelectedIndex(
            cbDoctor.getItemCount() > 0 ? 0 : -1
        );

        // Trả danh sách giờ về khung chuẩn (bỏ giờ lẻ thêm lúc sửa)
        cbTime.setModel(
            new DefaultComboBoxModel<>(STANDARD_TIMES)
        );

        cbTime.setSelectedItem("09:00");
        cbStatus.setSelectedItem("Chờ xác nhận");

        txtSearchPhone.setText("");

        lblCustomerStatus.setText(
            "Chưa kiểm tra khách hàng"
        );

        lblCustomerStatus.setForeground(MUTED);

        tableLichDat.clearSelection();

        // Tạo mã mới để hiển thị
        refreshNextAppointmentId();

        if (sorterLichDat != null) {
            sorterLichDat.setRowFilter(null);
        }
    }
}