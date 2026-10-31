
package views;

import Audit.AuditLog;
import models.dao.LichHenDAO;
import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

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

        JLabel lblSubTitle = new JLabel(
            "Quản lý riêng biệt lịch hẹn chờ xác nhận và đã xác nhận"
        );
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

        JLabel lblSearch = new JLabel("Nhập SĐT:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField(14);
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {
            "Mã Lịch Hẹn", "Tên Khách Hàng", "SĐT",
            "Nha sĩ phụ trách", "Ngày hẹn", "Giờ",
            "Trạng thái", "Ghi chú"
        };

        tableModelPending = createModel(cols);
        tableModelConfirmed = createModel(cols);

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
                int row = tablePending.convertRowIndexToModel(
                    tablePending.getSelectedRow()
                );
                fillFormFromTable(tableModelPending, row);
            }
        });

        tableConfirmed.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableConfirmed.getSelectedRow() != -1) {
                int row = tableConfirmed.convertRowIndexToModel(
                    tableConfirmed.getSelectedRow()
                );
                fillFormFromTable(tableModelConfirmed, row);
            }
        });

        JScrollPane scrollPending = new JScrollPane(tablePending);
        scrollPending.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollPending.getViewport().setBackground(CARD_BG);

        JScrollPane scrollConfirmed = new JScrollPane(tableConfirmed);
        scrollConfirmed.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollConfirmed.getViewport().setBackground(CARD_BG);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Lịch Hẹn Chờ Xử Lý", scrollPending);
        tabbedPane.addTab("Lịch Hẹn Đã Xác Nhận", scrollConfirmed);

        centerContainer.add(tabbedPane, BorderLayout.CENTER);
        centerContainer.add(createBookingFormPanel(), BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

        loadServices();
        loadDoctors();
        refreshData();
    }

    private DefaultTableModel createModel(String[] cols) {
        return new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Làm mới dữ liệu hai tab
    public void refreshData() {
        tableModelPending.setRowCount(0);
        tableModelConfirmed.setRowCount(0);

        DefaultTableModel pending = lichHenDAO.getLichHenChoXuLy();
        DefaultTableModel confirmed = lichHenDAO.getLichHenDaXacNhan();

        copyModel(pending, tableModelPending);
        copyModel(confirmed, tableModelConfirmed);

        clearForm();
        revalidate();
        repaint();
    }

    private void copyModel(DefaultTableModel source, DefaultTableModel target) {
        for (int i = 0; i < source.getRowCount(); i++) {
            Object[] row = new Object[source.getColumnCount()];
            for (int j = 0; j < source.getColumnCount(); j++) {
                row[j] = source.getValueAt(i, j);
            }
            target.addRow(row);
        }
    }

    private JPanel createBookingFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(20, 20, 20, 20)
        ));

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
        cbService.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbService.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cbService);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Nha sĩ phụ trách:"));
        panel.add(Box.createVerticalStrut(3));

        cbDoctor = new JComboBox<>();
        cbDoctor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(10));

        txtAppDate = addField(panel, "Ngày khám (dd/MM/yyyy):");
        txtAppTime = addField(panel, "Giờ (HH:mm):");

        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnConfirm = createStaticButton("Xác Nhận", GREEN_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> performSave());
        btnConfirm.addActionListener(e -> performConfirm());

        btnBox1.add(btnSave);
        btnBox1.add(btnConfirm);
        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

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

            while (rs.next()) {
                cbService.addItem(rs.getString("tenDichVu"));
            }

            if (cbService.getItemCount() == 0) {
                cbService.addItem("Không có dịch vụ");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi tải dịch vụ:\n" + e.getMessage());
        }
    }

    private void loadDoctors() {
        cbDoctor.removeAllItems();

        String sql =
            "SELECT tenNhanVien FROM NhanVien " +
            "WHERE chucVu LIKE N'%Bác sĩ%' " +
            "ORDER BY tenNhanVien";

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
            JOptionPane.showMessageDialog(this,
                "Lỗi tải nha sĩ:\n" + e.getMessage());
        }
    }

    private void fillFormFromTable(DefaultTableModel model, int row) {
        txtAppId.setText(String.valueOf(model.getValueAt(row, 0)));
        txtAppId.setEnabled(false);

        txtCustomerName.setText(String.valueOf(model.getValueAt(row, 1)));
        txtPhone.setText(String.valueOf(model.getValueAt(row, 2)));

        cbDoctor.setSelectedItem(model.getValueAt(row, 3));

        Object dateValue = model.getValueAt(row, 4);

        if (dateValue instanceof java.sql.Date) {
            txtAppDate.setText(
                new SimpleDateFormat("dd/MM/yyyy").format(dateValue)
            );
        } else {
            txtAppDate.setText(String.valueOf(dateValue));
        }

        txtAppTime.setText(String.valueOf(model.getValueAt(row, 5)));
        isEditing = true;
    }

    // Xác nhận lịch hẹn
    private void performConfirm() {
        int selectedRow = tablePending.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn lịch hẹn cần xác nhận!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tablePending.convertRowIndexToModel(selectedRow);
        String id = tableModelPending.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xác nhận lịch hẹn " + id + " này?",
            "Xác nhận lịch hẹn",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (lichHenDAO.xacNhanLichHen(id)) {
                AuditLog.event("UPDATE", "Xác nhận lịch hẹn - mã " + id);

                JOptionPane.showMessageDialog(this,
                    "Xác nhận lịch hẹn thành công!");

                refreshData();
                tabbedPane.setSelectedIndex(1);

            } else {
                JOptionPane.showMessageDialog(this,
                    "Xác nhận lịch hẹn thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Lưu hoặc cập nhật lịch hẹn
    private void performSave() {
        String id = txtAppId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        String doctor = (String) cbDoctor.getSelectedItem();
        String service = (String) cbService.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtAppTime.getText().trim();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty()
                || date.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập đầy đủ thông tin!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("\\d{10,15}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!");
            return;
        }

        if (doctor == null || doctor.equals("Không có nha sĩ")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nha sĩ!");
            return;
        }

        java.sql.Date sqlDate;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(date);
            sqlDate = new java.sql.Date(parsedDate.getTime());

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this,
                "Ngày hẹn phải có định dạng dd/MM/yyyy!");
            return;
        }

        if (!time.matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            JOptionPane.showMessageDialog(this,
                "Giờ hẹn không hợp lệ! Vui lòng nhập HH:mm.");
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
                    throw new SQLException("Không tìm thấy nha sĩ!");
                }

                if (lichHenDAO.isDuplicateAppointment(
                        conn, doctorId, sqlDate, time, isEditing ? id : "")) {

                    conn.rollback();

                    JOptionPane.showMessageDialog(this,
                        "Nha sĩ đã có lịch hẹn vào thời gian này!",
                        "Trùng lịch",
                        JOptionPane.WARNING_MESSAGE);
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

                JOptionPane.showMessageDialog(this,
                    isEditing ? "Cập nhật thành công!" : "Tạo lịch hẹn thành công!");

                refreshData();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi lưu lịch hẹn:\n" + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Hủy lịch hẹn
    private void performDelete() {
        JTable activeTable = tabbedPane.getSelectedIndex() == 0
            ? tablePending : tableConfirmed;

        DefaultTableModel activeModel = tabbedPane.getSelectedIndex() == 0
            ? tableModelPending : tableModelConfirmed;

        int selectedRow = activeTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn lịch hẹn cần hủy!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = activeTable.convertRowIndexToModel(selectedRow);
        String id = activeModel.getValueAt(modelRow, 0).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn hủy lịch hẹn " + id + " này?",
            "Xác nhận hủy",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (lichHenDAO.cancelAppointment(id)) {
                AuditLog.event("UPDATE", "Hủy lịch hẹn - mã " + id);

                JOptionPane.showMessageDialog(this,
                    "Hủy lịch hẹn thành công!");

                refreshData();

            } else {
                JOptionPane.showMessageDialog(this,
                    "Không thể hủy lịch hẹn!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        isEditing = false;

        txtAppId.setText("");
        txtAppId.setEnabled(true);

        txtCustomerName.setText("");
        txtPhone.setText("");
        txtAppDate.setText("");
        txtAppTime.setText("");

        if (cbService.getItemCount() > 0) {
            cbService.setSelectedIndex(0);
        }

        if (cbDoctor.getItemCount() > 0) {
            cbDoctor.setSelectedIndex(0);
        }

        tablePending.clearSelection();
        tableConfirmed.clearSelection();
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();

        RowFilter<DefaultTableModel, Object> filter = keyword.isEmpty()
            ? null
            : RowFilter.regexFilter(
                "(?i)" + Pattern.quote(keyword), 2
            );

        rowSorterPending.setRowFilter(filter);
        rowSorterConfirmed.setRowFilter(filter);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(
            new Color(245, 248, 253)
        );

        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField addField(JPanel panel, String labelText) {
        panel.add(createFieldLabel(labelText));
        panel.add(Box.createVerticalStrut(3));

        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(3, 8, 3, 8)
        ));

        panel.add(textField);
        panel.add(Box.createVerticalStrut(10));

        return textField;
    }

    private JButton createStaticButton(String text, Color bg, int width) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(bg);
                g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 8, 8
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);

        if (width != Integer.MAX_VALUE) {
            button.setPreferredSize(new Dimension(width, 36));
        }

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }
}
