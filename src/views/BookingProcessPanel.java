package views;

import Audit.AuditLog;
import models.dao.LichHenDAO;
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
import java.util.List;
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

    private DefaultTableModel tableModelPending;
    private DefaultTableModel tableModelConfirmed;
    private JTable tablePending;
    private JTable tableConfirmed;
    private JTabbedPane tabbedPane;

    private JTextField txtSearch;
    private JTextField txtAppId;
    private JTextField txtCustomerName;
    private JTextField txtPhone;
    private JTextField txtAppDate;
    private JTextField txtAppTime;

    private JComboBox<String> cbService;
    private JComboBox<String> cbDoctor;

    private TableRowSorter<DefaultTableModel> rowSorterPending;
    private TableRowSorter<DefaultTableModel> rowSorterConfirmed;

    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private LichHenDAO lichHenDAO;

    public BookingProcessPanel() {
        lichHenDAO = new LichHenDAO();

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tiếp Nhận & Quản Lý Lịch Hẹn Khám");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubTitle = new JLabel("Nhập SĐT để tra cứu lịch hẹn; Quản lý riêng biệt 'Chờ xác nhận' và 'Đã xác nhận'");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubTitle.setForeground(new Color(100, 110, 130));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSubTitle);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JLabel lblSearchSdt = new JLabel("Nhập SĐT:");
        lblSearchSdt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearchSdt.setForeground(TEXT_DARK);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(lblSearchSdt);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {
            "Mã Lịch Hẹn", "Tên Khách Hàng", "SĐT",
            "Dịch vụ khám", "Nha sĩ phụ trách", "Ngày hẹn", "Giờ", "Trạng thái"
        };

        tableModelPending = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableModelConfirmed = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablePending = new JTable(tableModelPending);
        tableConfirmed = new JTable(tableModelConfirmed);

        PdfTableExport.install(tablePending, "Lịch hẹn chờ xác nhận");
        PdfTableExport.install(tableConfirmed, "Lịch hẹn đã xác nhận");

        rowSorterPending = new TableRowSorter<>(tableModelPending);
        rowSorterConfirmed = new TableRowSorter<>(tableModelConfirmed);
        tablePending.setRowSorter(rowSorterPending);
        tableConfirmed.setRowSorter(rowSorterConfirmed);

        styleTable(tablePending);
        styleTable(tableConfirmed);

        tablePending.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablePending.getSelectedRow() != -1) {
                selectedRowIndex = tablePending.convertRowIndexToModel(tablePending.getSelectedRow());
                fillFormFromTable(tableModelPending, selectedRowIndex);
            }
        });

        tableConfirmed.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableConfirmed.getSelectedRow() != -1) {
                selectedRowIndex = tableConfirmed.convertRowIndexToModel(tableConfirmed.getSelectedRow());
                fillFormFromTable(tableModelConfirmed, selectedRowIndex);
            }
        });

        JScrollPane scrollPending = new JScrollPane(tablePending);
        scrollPending.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPending.getViewport().setBackground(CARD_BG);

        JScrollPane scrollConfirmed = new JScrollPane(tableConfirmed);
        scrollConfirmed.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollConfirmed.getViewport().setBackground(CARD_BG);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Lịch Hẹn Chờ Xử Lý", scrollPending);
        tabbedPane.addTab("Lịch Hẹn Đã Xác Nhận", scrollConfirmed); // Tên tab chính xác

        centerContainer.add(tabbedPane, BorderLayout.CENTER);
        centerContainer.add(createBookingFormPanel(), BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

        loadServices();
        loadDoctors();
        loadAppointments();
    }

    private void fillFormFromTable(DefaultTableModel model, int row) {
        txtAppId.setText(model.getValueAt(row, 0).toString());
        txtAppId.setEnabled(false);
        txtCustomerName.setText(model.getValueAt(row, 1).toString());
        txtPhone.setText(model.getValueAt(row, 2).toString());
        cbService.setSelectedItem(model.getValueAt(row, 3).toString());
        cbDoctor.setSelectedItem(model.getValueAt(row, 4).toString());
        txtAppDate.setText(model.getValueAt(row, 5).toString());
        txtAppTime.setText(model.getValueAt(row, 6).toString());
        isEditing = true;
    }

    private JPanel createBookingFormPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Xử Lý Tiếp Nhận Lịch");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        txtAppId = addField(panel, "Mã lịch hẹn:");
        txtCustomerName = addField(panel, "Tên khách hàng:");
        txtPhone = addField(panel, "Số điện thoại:");

        panel.add(createFieldLabel("Dịch vụ khám:"));
        panel.add(Box.createVerticalStrut(3));
        cbService = new JComboBox<>();
        cbService.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbService.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbService.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbService.setBackground(Color.WHITE);
        panel.add(cbService);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Nha sĩ phụ trách:"));
        panel.add(Box.createVerticalStrut(3));
        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDoctor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDoctor.setBackground(Color.WHITE);
        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(10));

        txtAppDate = addField(panel, "Ngày khám (dd/MM/yyyy):");
        txtAppTime = addField(panel, "Giờ (HH:mm):");

        // Hàng nút 1: Lưu & Xác Nhận chuyển tab
        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnConfirm = createStaticButton("Xác Nhận", GREEN_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> performSave());
        btnConfirm.addActionListener(e -> performConfirm()); // Chuyển lịch sang Đã xác nhận

        btnBox1.add(btnSave);
        btnBox1.add(btnConfirm);
        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

        // Hàng nút 2: Hủy lịch & Làm mới
        JPanel btnBox2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox2.setOpaque(false);
        btnBox2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnDelete = createStaticButton("Hủy lịch", RED_ACCENT, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm Mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnDelete.addActionListener(e -> performDelete());
        btnClear.addActionListener(e -> clearForm());

        btnBox2.add(btnDelete);
        btnBox2.add(btnClear);
        panel.add(btnBox2);

        return panel;
    }

    private void loadServices() {
        cbService.removeAllItems();
        String sql = "SELECT tenDichVu FROM DichVu ORDER BY tenDichVu";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbService.addItem(rs.getString("tenDichVu"));
            if (cbService.getItemCount() == 0) cbService.addItem("Không có dịch vụ");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dịch vụ:\n" + e.getMessage());
        }
    }

    private void loadDoctors() {
        cbDoctor.removeAllItems();
        String sql = "SELECT tenNhanVien FROM NhanVien WHERE chucVu LIKE N'%Bác sĩ%' ORDER BY tenNhanVien";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbDoctor.addItem(rs.getString("tenNhanVien"));
            if (cbDoctor.getItemCount() == 0) cbDoctor.addItem("Không có nha sĩ");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải nha sĩ:\n" + e.getMessage());
        }
    }

    private void loadAppointments() {
        tableModelPending.setRowCount(0);
        tableModelConfirmed.setRowCount(0);

        List<Object[]> list = lichHenDAO.getAllAppointments();
        for (Object[] row : list) {
            String trangThai = row[7] != null ? row[7].toString().trim() : "";
            
            // Tab 1: Chỉ nhận "Chờ xác nhận"
            if (trangThai.equalsIgnoreCase("Chờ xác nhận")) {
                tableModelPending.addRow(row);
            } 
            // Tab 2: Chỉ nhận "Đã xác nhận"
            else if (trangThai.equalsIgnoreCase("Đã xác nhận")) {
                tableModelConfirmed.addRow(row);
            }
        }
    }

    // Xử lý khi nhấn nút "Xác Nhận": chuyển dòng được chọn sang trạng thái "Đã xác nhận"
    private void performConfirm() {
        int selectedRow = tablePending.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch hẹn ở tab 'Lịch Hẹn Chờ Xử Lý' cần xác nhận!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablePending.convertRowIndexToModel(selectedRow);
        String id = tableModelPending.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xác nhận lịch hẹn " + id + " này?", "Xác nhận lịch hẹn", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (lichHenDAO.updateAppointmentStatus(id, "Đã xác nhận")) {
                AuditLog.event("UPDATE", "Xác nhận lịch hẹn - mã " + id);
                JOptionPane.showMessageDialog(this, "Xác nhận thành công! Lịch hẹn đã chuyển sang tab 'Lịch Hẹn Đã Xác Nhận'.");
                clearForm();
                loadAppointments();
                tabbedPane.setSelectedIndex(1); // Tự động bật sang tab thứ 2 để người dùng xem kết quả
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performSave() {
        String id = txtAppId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        String service = (String) cbService.getSelectedItem();
        String doctor = (String) cbDoctor.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtAppTime.getText().trim();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || date.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("\\d{10,15}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!");
            return;
        }

        java.sql.Date sqlDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(date);
            sqlDate = new java.sql.Date(parsedDate.getTime());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày hẹn phải có định dạng dd/MM/yyyy!");
            return;
        }

        if (time == null || !time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            JOptionPane.showMessageDialog(this, "Giờ hẹn không hợp lệ!\nVui lòng nhập theo dạng HH:mm.");
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
                    throw new SQLException("Không tìm thấy nha sĩ trong bảng NhanVien!");
                }

                if (lichHenDAO.isDuplicateAppointment(conn, doctorId, sqlDate, time, id)) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Nha sĩ đã có lịch hẹn vào thời gian này!", "Trùng lịch", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (isEditing) {
                    lichHenDAO.updateAppointment(conn, id, customerId, doctorId, sqlDate, time, service);
                } else {
                    lichHenDAO.insertAppointment(conn, id, customerId, doctorId, sqlDate, time, service);
                }

                conn.commit();
                AuditLog.event(isEditing ? "UPDATE" : "CREATE", "Quản lý lịch hẹn - mã " + id);
                JOptionPane.showMessageDialog(this, isEditing ? "Cập nhật thành công!" : "Tạo lịch hẹn thành công!");
                clearForm();
                loadAppointments();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu lịch hẹn:\n" + e.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDelete() {
        JTable activeTable = (tabbedPane.getSelectedIndex() == 0) ? tablePending : tableConfirmed;
        DefaultTableModel activeModel = (tabbedPane.getSelectedIndex() == 0) ? tableModelPending : tableModelConfirmed;

        int selectedRow = activeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch hẹn cần hủy!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = activeTable.convertRowIndexToModel(selectedRow);
        String id = activeModel.getValueAt(modelRow, 0).toString();
        String status = activeModel.getValueAt(modelRow, 7).toString();

        if ("Đã hủy".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này đã được hủy!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy lịch hẹn " + id + " này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (lichHenDAO.cancelAppointment(id)) {
                AuditLog.event("UPDATE", "Hủy lịch hẹn - mã " + id);
                JOptionPane.showMessageDialog(this, "Hủy lịch hẹn thành công!");
                clearForm();
                loadAppointments();
            }
        }
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtAppId.setText("");
        txtAppId.setEnabled(true);
        txtCustomerName.setText("");
        txtPhone.setText("");
        if (cbService.getItemCount() > 0) cbService.setSelectedIndex(0);
        if (cbDoctor.getItemCount() > 0) cbDoctor.setSelectedIndex(0);
        txtAppDate.setText("");
        txtAppTime.setText("");
        tablePending.clearSelection();
        tableConfirmed.clearSelection();
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        RowFilter<DefaultTableModel, Object> rf = kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(kw));
        rowSorterPending.setRowFilter(rf);
        rowSorterConfirmed.setRowFilter(rf);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_DARK);
        t.setGridColor(new Color(240, 244, 248));
        t.setSelectionBackground(new Color(225, 240, 255));
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(245, 248, 253));
        t.getTableHeader().setForeground(TEXT_DARK);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addField(JPanel panel, String labelText) {
        panel.add(createFieldLabel(labelText));
        panel.add(Box.createVerticalStrut(3));
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        panel.add(textField);
        panel.add(Box.createVerticalStrut(10));
        return textField;
    }

    private JButton createStaticButton(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}