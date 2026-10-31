
package views;

import Audit.AuditLog;
import report.PdfTableExport;
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
import java.util.regex.Pattern;

public class BookingProcessPanel extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField txtSearch;
    private JTextField txtAppId;
    private JTextField txtCustomerName;
    private JTextField txtPhone;
    private JTextField txtAppDate;
    private JTextField txtAppTime;

    private JComboBox<String> cbService;
    private JComboBox<String> cbDoctor;

    private TableRowSorter<DefaultTableModel> rowSorter;

    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public BookingProcessPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tạo Lịch Hẹn Mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel toolbarPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        toolbarPanel.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton(
                "Tìm", BLUE_PRIMARY, 75
        );
        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(
                new BorderLayout(15, 0)
        );
        centerContainer.setOpaque(false);

        String[] cols = {
            "Mã Lịch Hẹn",
            "Tên Khách Hàng",
            "Số Điện Thoại",
            "Dịch Vụ Khám",
            "Nha Sĩ Phụ Trách",
            "Ngày Hẹn",
            "Giờ Hẹn",
            "Trạng Thái"
        };

        tableModel = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);

        PdfTableExport.install(table, "Lịch hẹn");

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() != -1) {

                selectedRowIndex = table.convertRowIndexToModel(
                        table.getSelectedRow()
                );

                txtAppId.setText(
                        getCellValue(selectedRowIndex, 0)
                );

                txtAppId.setEnabled(false);

                txtCustomerName.setText(
                        getCellValue(selectedRowIndex, 1)
                );

                txtPhone.setText(
                        getCellValue(selectedRowIndex, 2)
                );

                cbService.setSelectedItem(
                        getCellValue(selectedRowIndex, 3)
                );

                cbDoctor.setSelectedItem(
                        getCellValue(selectedRowIndex, 4)
                );

                txtAppDate.setText(
                        getCellValue(selectedRowIndex, 5)
                );

                // Lấy giờ hẹn từ cột thứ 7 của bảng.
                txtAppTime.setText(
                        getCellValue(selectedRowIndex, 6)
                );

                isEditing = true;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1)
        );
        scroll.getViewport().setBackground(CARD_BG);

        centerContainer.add(scroll, BorderLayout.CENTER);
        centerContainer.add(
                createBookingFormPanel(),
                BorderLayout.EAST
        );

        add(centerContainer, BorderLayout.CENTER);

        loadServices();
        loadDoctors();
        loadAppointments();
    }

    // ================= FORM =================

    private JPanel createBookingFormPanel() {

        JPanel panel = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(CARD_BG);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 16, 16
                );

                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(
                        0, 0,
                        getWidth() - 1,
                        getHeight() - 1,
                        16, 16
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );
        panel.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel title = new JLabel("Tạo, Cập Nhật Lịch Hẹn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        txtAppId = addField(panel, "Mã Lịch Hẹn:");
        txtCustomerName = addField(panel, "Tên Khách Hàng:");
        txtPhone = addField(panel, "Số Điện Thoại:");

        JLabel lblServ = createFieldLabel("Dịch Vụ Khám:");
        panel.add(lblServ);
        panel.add(Box.createVerticalStrut(3));

        cbService = new JComboBox<>();
        cbService.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        cbService.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );
        cbService.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbService.setBackground(Color.WHITE);

        panel.add(cbService);
        panel.add(Box.createVerticalStrut(10));

        JLabel lblDoc = createFieldLabel("Nha Sĩ Phụ Trách:");
        panel.add(lblDoc);
        panel.add(Box.createVerticalStrut(3));

        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        cbDoctor.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDoctor.setBackground(Color.WHITE);

        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(10));

        txtAppDate = addField(
                panel, "Ngày Hẹn (dd/MM/yyyy):"
        );

        // Giờ hẹn được nhập theo định dạng HH:mm.
        txtAppTime = addField(
                panel, "Giờ Hẹn (HH:mm):"
        );

        JPanel btnBox1 = new JPanel(
                new GridLayout(1, 2, 8, 0)
        );

        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton(
                "Lưu", GREEN_ACCENT, Integer.MAX_VALUE
        );

        JButton btnDelete = createStaticButton(
                "Hủy lịch", RED_ACCENT, Integer.MAX_VALUE
        );

        btnSave.addActionListener(e -> performSave());
        btnDelete.addActionListener(e -> performDelete());

        btnBox1.add(btnSave);
        btnBox1.add(btnDelete);

        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

        JPanel btnBox2 = new JPanel(
                new GridLayout(1, 2, 8, 0)
        );

        btnBox2.setOpaque(false);
        btnBox2.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );
        btnBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClear = createStaticButton(
                "Làm Mới", PINK_ACCENT, Integer.MAX_VALUE
        );

        btnClear.addActionListener(e -> clearForm());

        btnBox2.add(btnClear);
        panel.add(btnBox2);

        return panel;
    }

    // ================= TẢI DỊCH VỤ =================

    private void loadServices() {

        cbService.removeAllItems();

        String sql =
                "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbService.addItem(
                        rs.getString("tenDichVu")
                );
            }

            if (cbService.getItemCount() == 0) {
                cbService.addItem("Không có dịch vụ");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách dịch vụ:\n"
                    + e.getMessage()
            );
        }
    }

    // ================= TẢI NHA SĨ =================

    private void loadDoctors() {

        cbDoctor.removeAllItems();

        String sql =
                "SELECT tenNhanVien FROM NhanVien "
                + "WHERE chucVu LIKE N'%Bác sĩ%' "
                + "ORDER BY tenNhanVien";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbDoctor.addItem(
                        rs.getString("tenNhanVien")
                );
            }

            if (cbDoctor.getItemCount() == 0) {
                cbDoctor.addItem("Không có nha sĩ");
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách nha sĩ:\n"
                    + e.getMessage()
            );
        }
    }

    // ================= TẢI LỊCH HẸN =================

    private void loadAppointments() {

        tableModel.setRowCount(0);

        String sql =
                "SELECT L.maLichHen, "
                + "K.tenKH, K.sdt, "
                + "L.ghiChu AS dichVu, "
                + "N.tenNhanVien AS nhaSi, "
                + "CONVERT(VARCHAR(10), L.ngayHen, 103) AS ngayHen, "
                + "L.gioKham, "
                + "L.trangThai "
                + "FROM LichHen L "
                + "INNER JOIN KhachHang K "
                + "ON L.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N "
                + "ON L.maNhanVien = N.maNhanVien "
                + "ORDER BY L.ngayHen DESC, L.gioKham ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String gioKham = rs.getString("gioKham");

                if (gioKham == null || gioKham.trim().isEmpty()) {
                    gioKham = "";
                }

                Object[] row = {
                    rs.getString("maLichHen"),
                    rs.getString("tenKH"),
                    rs.getString("sdt"),
                    rs.getString("dichVu"),
                    rs.getString("nhaSi"),
                    rs.getString("ngayHen"),
                    gioKham,
                    rs.getString("trangThai")
                };

                tableModel.addRow(row);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải danh sách lịch hẹn:\n"
                    + e.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= LƯU LỊCH HẸN =================

    private void performSave() {

        String id = txtAppId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        String service = (String) cbService.getSelectedItem();
        String doctor = (String) cbDoctor.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtAppTime.getText().trim();

        if (id.isEmpty() || name.isEmpty()
                || phone.isEmpty() || date.isEmpty()
                || time.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!phone.matches("\\d{10,15}")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Số điện thoại không hợp lệ!"
            );
            return;
        }

        if (service == null
                || service.equals("Không có dịch vụ")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dịch vụ!"
            );
            return;
        }

        if (doctor == null
                || doctor.equals("Không có nha sĩ")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn nha sĩ!"
            );
            return;
        }

        // Kiểm tra định dạng ngày.
        java.sql.Date sqlDate;

        try {

            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy");

            sdf.setLenient(false);

            Date parsedDate = sdf.parse(date);

            if (!sdf.format(parsedDate).equals(date)) {
                throw new ParseException(
                        "Ngày không hợp lệ", 0
                );
            }

            sqlDate = new java.sql.Date(
                    parsedDate.getTime()
            );

        } catch (ParseException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Ngày hẹn phải có định dạng dd/MM/yyyy!"
            );
            return;
        }

        // Kiểm tra định dạng giờ HH:mm.
        if (!isValidTime(time)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Giờ hẹn không hợp lệ!\n"
                    + "Vui lòng nhập theo dạng HH:mm, ví dụ 08:30."
            );
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // 1. Tìm khách hàng bằng số điện thoại.
                String customerId = findCustomerId(
                        conn, phone
                );

                // 2. Nếu khách hàng mới thì tạo hồ sơ.
                if (customerId == null) {

                    customerId = createCustomer(
                            conn, name, phone
                    );
                }

                // 3. Tìm mã nha sĩ.
                String doctorId = findDoctorId(
                        conn, doctor
                );

                if (doctorId == null) {
                    throw new SQLException(
                            "Không tìm thấy nha sĩ trong bảng NhanVien!"
                    );
                }

                // 4. Kiểm tra trùng nha sĩ, ngày và giờ.
                if (isDuplicateAppointment(
                        conn, doctorId, sqlDate, time, id
                )) {

                    conn.rollback();

                    JOptionPane.showMessageDialog(
                            this,
                            "Nha sĩ đã có lịch hẹn vào "
                            + time + " ngày " + date + "!\n"
                            + "Vui lòng chọn giờ khác.",
                            "Trùng lịch",
                            JOptionPane.WARNING_MESSAGE
                    );

                    return;
                }

                // 5. Cập nhật hoặc thêm lịch hẹn.
                if (isEditing) {

                    String updateSql =
                            "UPDATE LichHen SET "
                            + "maKhachHang = ?, "
                            + "maNhanVien = ?, "
                            + "ngayHen = ?, "
                            + "gioKham = ?, "
                            + "ghiChu = ? "
                            + "WHERE maLichHen = ?";

                    try (PreparedStatement ps =
                            conn.prepareStatement(updateSql)) {

                        ps.setString(1, customerId);
                        ps.setString(2, doctorId);
                        ps.setDate(3, sqlDate);
                        ps.setString(4, time);
                        ps.setString(5, service);
                        ps.setString(6, id);

                        int result = ps.executeUpdate();

                        if (result == 0) {
                            throw new SQLException(
                                    "Không tìm thấy lịch hẹn cần cập nhật!"
                            );
                        }
                    }

                } else {

                    String insertSql =
                            "INSERT INTO LichHen "
                            + "(maLichHen, maKhachHang, "
                            + "maNhanVien, ngayHen, gioKham, "
                            + "trangThai, ghiChu) "
                            + "VALUES (?, ?, ?, ?, ?, "
                            + "N'Chờ xác nhận', ?)";

                    try (PreparedStatement ps =
                            conn.prepareStatement(insertSql)) {

                        ps.setString(1, id);
                        ps.setString(2, customerId);
                        ps.setString(3, doctorId);
                        ps.setDate(4, sqlDate);
                        ps.setString(5, time);
                        ps.setString(6, service);

                        ps.executeUpdate();
                    }
                }

                conn.commit();

                AuditLog.event(
                        isEditing ? "UPDATE" : "CREATE",
                        "Tiếp nhận lịch hẹn - mã " + id
                );

                JOptionPane.showMessageDialog(
                        this,
                        isEditing
                        ? "Cập nhật lịch hẹn thành công!"
                        : "Tạo lịch hẹn mới thành công!"
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
                    "Không thể lưu lịch hẹn!\n"
                    + e.getMessage(),
                    "Lỗi SQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= KIỂM TRA GIỜ =================

    private boolean isValidTime(String time) {

        if (time == null
                || !time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            return false;
        }

        return true;
    }

    // ================= TÌM KHÁCH HÀNG =================

    private String findCustomerId(
            Connection conn, String phone
    ) throws SQLException {

        String sql =
                "SELECT maKhachHang FROM KhachHang "
                + "WHERE sdt = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("maKhachHang");
                }
            }
        }

        return null;
    }

    // ================= TẠO HỒ SƠ KHÁCH HÀNG =================

    private String createCustomer(
            Connection conn,
            String name,
            String phone
    ) throws SQLException {

        String newId;

        String getIdSql =
                "SELECT ISNULL(MAX(TRY_CONVERT(INT, "
                + "SUBSTRING(maKhachHang, 3, 20))), 0) + 1 "
                + "FROM KhachHang "
                + "WHERE maKhachHang LIKE 'KH%'";

        try (PreparedStatement ps =
                conn.prepareStatement(getIdSql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();

            newId = String.format(
                    "KH%03d", rs.getInt(1)
            );
        }

        String insertSql =
                "INSERT INTO KhachHang "
                + "(maKhachHang, tenKH, sdt, diemTichLuy) "
                + "VALUES (?, ?, ?, 0)";

        try (PreparedStatement ps =
                conn.prepareStatement(insertSql)) {

            ps.setString(1, newId);
            ps.setString(2, name);
            ps.setString(3, phone);

            ps.executeUpdate();
        }

        return newId;
    }

    // ================= TÌM NHA SĨ =================

    private String findDoctorId(
            Connection conn, String doctorName
    ) throws SQLException {

        String sql =
                "SELECT maNhanVien FROM NhanVien "
                + "WHERE tenNhanVien = ? "
                + "AND chucVu LIKE N'%Bác sĩ%'";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, doctorName);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("maNhanVien");
                }
            }
        }

        return null;
    }

    // ================= KIỂM TRA TRÙNG LỊCH =================

    private boolean isDuplicateAppointment(
            Connection conn,
            String doctorId,
            java.sql.Date appointmentDate,
            String appointmentTime,
            String appointmentId
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM LichHen "
                + "WHERE maNhanVien = ? "
                + "AND ngayHen = ? "
                + "AND gioKham = ? "
                + "AND ISNULL(trangThai, N'') <> N'Đã hủy' "
                + "AND maLichHen <> ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, doctorId);
            ps.setDate(2, appointmentDate);
            ps.setString(3, appointmentTime);
            ps.setString(4, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // ================= HỦY LỊCH HẸN =================

    private void performDelete() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn lịch hẹn cần hủy!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(
                selectedRow
        );

        String id = getCellValue(modelRow, 0);
        String status = getCellValue(modelRow, 7);

        if ("Đã hủy".equalsIgnoreCase(status)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lịch hẹn này đã được hủy!"
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy lịch hẹn "
                + id + " không?",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE LichHen "
                + "SET trangThai = N'Đã hủy' "
                + "WHERE maLichHen = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);

            int result = ps.executeUpdate();

            if (result > 0) {

                AuditLog.event(
                        "UPDATE",
                        "Hủy lịch hẹn - mã " + id
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Đã hủy lịch hẹn thành công!"
                );

                clearForm();
                loadAppointments();
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi hủy lịch hẹn:\n" + e.getMessage()
            );
        }
    }

    // ================= LÀM MỚI FORM =================

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

        table.clearSelection();
    }

    // ================= TÌM KIẾM =================

    private void performSearch() {

        String kw = txtSearch.getText().trim();

        rowSorter.setRowFilter(
                kw.isEmpty()
                ? null
                : RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(kw)
                )
        );
    }

    // ================= LẤY GIÁ TRỊ BẢNG =================

    private String getCellValue(int row, int column) {

        Object value = tableModel.getValueAt(
                row, column
        );

        return value == null ? "" : value.toString();
    }

    // ================= LABEL =================

    private JLabel createFieldLabel(String text) {

        JLabel lbl = new JLabel(text);
        lbl.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        return lbl;
    }

    // ================= TEXT FIELD =================

    private JTextField addField(
            JPanel panel, String labelText
    ) {

        panel.add(createFieldLabel(labelText));
        panel.add(Box.createVerticalStrut(3));

        JTextField textField = new JTextField();

        textField.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        textField.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 34)
        );

        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_LIGHT, 1
                        ),
                        new EmptyBorder(3, 8, 3, 8)
                )
        );

        panel.add(textField);
        panel.add(Box.createVerticalStrut(10));

        return textField;
    }

    // ================= BUTTON =================

    private JButton createStaticButton(
            String text, Color bg, int width
    ) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(bg);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(),
                        getHeight(),
                        8, 8
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        btn.setForeground(Color.WHITE);

        if (width != Integer.MAX_VALUE) {
            btn.setPreferredSize(
                    new Dimension(width, 36)
            );
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return btn;
    }

    // ================= STYLE TABLE =================

    private void styleTable(JTable t) {

        t.setRowHeight(38);

        t.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        t.setForeground(TEXT_DARK);

        t.setGridColor(
                new Color(240, 244, 248)
        );

        t.setSelectionBackground(
                new Color(225, 240, 255)
        );

        t.setSelectionForeground(TEXT_DARK);

        t.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        t.getTableHeader().setBackground(
                new Color(245, 248, 253)
        );

        t.getTableHeader().setForeground(TEXT_DARK);

        t.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );
    }
}
