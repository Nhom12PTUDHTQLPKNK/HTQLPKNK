
package views;

import controllers.PatientController;
import models.entity.KhachHang;
import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerPanel extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField txtCustId, txtCustName, txtPhone, txtAddress;
    private JComboBox<String> cbGender;
    private JButton btnSave, btnClear;

    private JPanel tableContainer;
    private CardLayout tableCardLayout;

    private final PatientController patientController = new PatientController();

    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        createHeader();

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        createCustomerTable();

        centerContainer.add(tableContainer, BorderLayout.CENTER);
        centerContainer.add(createInlineFormPanel(), BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

        try {
            refreshPatients();
        } catch (SQLException e) {
            showDatabaseError("Không thể tải danh sách khách hàng", e);
        }
    }


    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        titleLine.setOpaque(false);

        JLabel lblIcon = new JLabel();
        ImageIcon titleIcon = loadImage("/img/rang-tiepnhan.png", 34, 34);

        if (titleIcon != null) {
            lblIcon.setIcon(titleIcon);
        } else {
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 27));
        }

        JLabel lblTitle = new JLabel("Quản Lý Danh Mục Khách Hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        titleLine.add(lblIcon);
        titleLine.add(lblTitle);

        JLabel lblSub = new JLabel(
                "Quản lý hồ sơ thông tin bệnh nhân và khách hàng nha khoa"
        );
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        titleBox.add(titleLine);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel toolbarPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        toolbarPanel.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(210, 36));
        txtSearch.setToolTipText("Tìm theo mã khách hàng, tên hoặc số điện thoại");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        // Live Search
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> performSearch());

        JButton btnEdit = createStaticButton("Sửa", GREEN_ACCENT, 75);
        btnEdit.addActionListener(e -> loadDataToFormForEdit());

        JButton btnDelete = createStaticButton("Xóa", RED_ACCENT, 75);
        btnDelete.addActionListener(e -> performDelete());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(Box.createHorizontalStrut(5));
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }


    private void createCustomerTable() {

        String[] columns = {
                "Mã KH",
                "Họ và tên",
                "Giới tính",
                "Số điện thoại",
                "Địa chỉ",
                "Lịch sử khám"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        table = new JTable(tableModel);
        PdfTableExport.install(table, "Danh sách khách hàng");

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        // giới tính
        table.getColumnModel().getColumn(2)
                .setCellRenderer(new GenderRenderer());

        //nút lịch sử khám
        table.getColumnModel().getColumn(5)
                .setCellRenderer(new HistoryButtonRenderer());

        // Editor nút lịch sử khám
        table.getColumnModel().getColumn(5)
                .setCellEditor(new HistoryButtonEditor());

        table.getColumnModel().getColumn(0).setPreferredWidth(85);
        table.getColumnModel().getColumn(1).setPreferredWidth(155);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDataToFormForEdit();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        // Khu vực bảng và hình minh họa khi trống
        tableCardLayout = new CardLayout();
        tableContainer = new JPanel(tableCardLayout);
        tableContainer.setOpaque(false);

        tableContainer.add(scrollPane, "TABLE");
        tableContainer.add(createEmptyPanel(), "EMPTY");

        tableCardLayout.show(tableContainer, "EMPTY");
    }


    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblIcon = new JLabel("🦷");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 65));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Chưa có khách hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDescription = new JLabel(
                "Danh sách khách hàng sẽ xuất hiện tại đây."
        );
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescription.setForeground(MUTED);
        lblDescription.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(lblIcon);
        content.add(Box.createVerticalStrut(12));
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(7));
        content.add(lblDescription);

        panel.add(content);

        return panel;
    }

    private void updateEmptyPanel() {
        if (tableModel.getRowCount() == 0) {
            tableCardLayout.show(tableContainer, "EMPTY");
        } else {
            tableCardLayout.show(tableContainer, "TABLE");
        }
    }

    // forrmr

    private JPanel createInlineFormPanel() {

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
                        0, 0, getWidth() - 1, getHeight() - 1, 16, 16
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblFormTitle = new JLabel("Thông Tin Khách Hàng");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtCustId = addFieldToForm(panel, "Mã khách hàng:");
        txtCustName = addFieldToForm(panel, "Họ và tên:");

        JLabel lblGen = createFieldLabel("Giới tính:");
        panel.add(lblGen);
        panel.add(Box.createVerticalStrut(4));

        cbGender = new JComboBox<>(
                new String[]{"Nam", "Nữ", "Khác"}
        );

        cbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbGender.setBackground(Color.WHITE);

        cbGender.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 7, 3, 7)
        ));

        panel.add(cbGender);
        panel.add(Box.createVerticalStrut(12));

        txtPhone = addFieldToForm(panel, "Số điện thoại:");
        txtAddress = addFieldToForm(panel, "Địa chỉ:");

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave = createStaticButton(
                "Lưu", BLUE_PRIMARY, Integer.MAX_VALUE
        );

        btnClear = createStaticButton(
                "Làm mới", PINK_ACCENT, Integer.MAX_VALUE
        );

        btnSave.addActionListener(e -> performSave());
        btnClear.addActionListener(e -> clearForm());

        btnBox.add(btnSave);
        btnBox.add(btnClear);

        panel.add(btnBox);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToForm(
            JPanel form, String labelText
    ) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));

        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);

        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));

        form.add(txt);
        form.add(Box.createVerticalStrut(12));

        return txt;
    }

    // BUTTON

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
                        0, 0, getWidth(), getHeight(), 8, 8
                );

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);

        if (width != Integer.MAX_VALUE) {
            btn.setPreferredSize(new Dimension(width, 36));
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // style bảng

    private void styleTable(JTable table) {

        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);

        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);

        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(245, 248, 253));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
    }

    // gioi tinh xanh hong

    private class GenderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel lbl = new JLabel(
                    value == null ? "" : value.toString(),
                    SwingConstants.CENTER
            );

            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

            if (isSelected) {
                lbl.setBackground(table.getSelectionBackground());
                lbl.setForeground(TEXT_DARK);
            } else {
                String gender = value == null
                        ? "" : value.toString();

                if ("Nữ".equalsIgnoreCase(gender)) {
                    lbl.setBackground(new Color(255, 235, 244));
                    lbl.setForeground(PINK_ACCENT);
                } else if ("Nam".equalsIgnoreCase(gender)) {
                    lbl.setBackground(new Color(229, 241, 255));
                    lbl.setForeground(BLUE_PRIMARY);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(MUTED);
                }
            }

            return lbl;
        }
    }


    private class HistoryButtonRenderer extends JButton
            implements TableCellRenderer {

        public HistoryButtonRenderer() {
            setText("Lịch sử");
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(BLUE_PRIMARY);
            setBackground(new Color(232, 243, 255));
            setFocusPainted(false);
            setBorder(BorderFactory.createLineBorder(
                    new Color(200, 225, 255)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            ImageIcon icon = loadImage("/img/TimKiem.png", 16, 16);

            if (icon != null) {
                setIcon(icon);
                setText("");
            } else {
                setIcon(null);
                setText("Lịch sử");
            }

            return this;
        }
    }

    // nút lưu ls

    private class HistoryButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JButton button = new JButton();
        private String customerId;

        public HistoryButtonEditor() {
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setForeground(BLUE_PRIMARY);
            button.setBackground(new Color(232, 243, 255));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createLineBorder(
                    new Color(200, 225, 255)
            ));

            button.addActionListener(e -> {
                fireEditingStopped();

                if (customerId != null) {
                    showMedicalHistory(customerId);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column
        ) {
            int modelRow = table.convertRowIndexToModel(row);

            customerId = tableModel.getValueAt(
                    modelRow, 0
            ).toString();

            ImageIcon icon = loadImage("/img/TimKiem.png", 16, 16);

            if (icon != null) {
                button.setIcon(icon);
                button.setText("");
            } else {
                button.setIcon(null);
                button.setText("Lịch sử");
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Lịch sử";
        }
    }

    // ls khám/ hs bệnh án

    private void showMedicalHistory(String customerId) {

        String customerName = "";

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (customerId.equals(
                    tableModel.getValueAt(i, 0).toString()
            )) {
                customerName = tableModel.getValueAt(i, 1).toString();
                break;
            }
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Lịch sử khám bệnh",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setSize(850, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.getContentPane().setBackground(BG_CANVAS);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 20, 8, 20));

        JLabel lblTitle = new JLabel("HỒ SƠ BỆNH ÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblCustomer = new JLabel(
                "Khách hàng: " + customerName + "  |  Mã KH: " + customerId
        );
        lblCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCustomer.setForeground(MUTED);

        header.add(lblTitle);
        header.add(Box.createVerticalStrut(6));
        header.add(lblCustomer);

        dialog.add(header, BorderLayout.NORTH);

        String[] columns = {
                "Mã bệnh án",
                "Ngày khám",
                "Bác sĩ phụ trách",
                "Chẩn đoán",
                "Kết quả khám",
                "Trạng thái"
        };

        DefaultTableModel historyModel = new DefaultTableModel(
                columns, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(historyModel);
        styleTable(historyTable);

        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(210);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollPane.setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(CARD_BG);
        content.setBorder(new EmptyBorder(10, 15, 10, 15));

        content.add(scrollPane, BorderLayout.CENTER);

        JLabel lblImageNote = new JLabel(
                "🦷  Hình ảnh X-quang chưa có dữ liệu lưu trữ trong cơ sở dữ liệu."
        );
        lblImageNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImageNote.setForeground(MUTED);

        content.add(lblImageNote, BorderLayout.SOUTH);

        dialog.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 10, 12, 15));

        JButton btnClose = createStaticButton(
                "Đóng", BLUE_PRIMARY, 90
        );

        btnClose.addActionListener(e -> dialog.dispose());

        footer.add(btnClose);
        dialog.add(footer, BorderLayout.SOUTH);

        // Lấy lịch sử khám từ SQL Server
        String sql =
                "SELECT ba.maBenhAn, ba.ngayKham, " +
                "nv.tenNhanVien, ba.chuanDoan, " +
                "ba.ketQuaKham, ba.trangThai " +
                "FROM HoSoBenhAn ba " +
                "LEFT JOIN NhanVien nv " +
                "ON ba.maNhanVien = nv.maNhanVien " +
                "WHERE ba.maKhachHang = ? " +
                "ORDER BY ba.ngayKham DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Date sqlDate = rs.getDate("ngayKham");

                    String date = sqlDate == null
                            ? ""
                            : new SimpleDateFormat("dd/MM/yyyy")
                                    .format(sqlDate);

                    String doctor = rs.getString("tenNhanVien");

                    if (doctor == null) {
                        doctor = "Chưa có thông tin";
                    }

                    historyModel.addRow(new Object[]{
                            rs.getString("maBenhAn"),
                            date,
                            doctor,
                            rs.getString("chuanDoan"),
                            rs.getString("ketQuaKham"),
                            rs.getString("trangThai")
                    });
                }
            }

            if (historyModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Khách hàng này chưa có lịch sử khám bệnh.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {
            showDatabaseError("Không thể tải lịch sử khám bệnh", e);
        }

        dialog.setVisible(true);
    }

    // tìm

    private void performSearch() {

        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(
                    RowFilter.regexFilter(
                            "(?i)" + Pattern.quote(keyword)
                    )
            );
        }
    }

    // xóa form

    private void clearForm() {

        isEditing = false;
        selectedRowIndex = -1;

        txtCustId.setText("");
        txtCustId.setEnabled(true);

        txtCustName.setText("");
        cbGender.setSelectedIndex(0);
        txtPhone.setText("");
        txtAddress.setText("");

        table.clearSelection();
    }

    //tải DL dô form
    private void loadDataToFormForEdit() {

        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            return;
        }

        selectedRowIndex = table.convertRowIndexToModel(viewRow);

        txtCustId.setText(
                tableModel.getValueAt(selectedRowIndex, 0).toString()
        );

        txtCustId.setEnabled(false);

        txtCustName.setText(
                tableModel.getValueAt(selectedRowIndex, 1).toString()
        );

        cbGender.setSelectedItem(
                tableModel.getValueAt(selectedRowIndex, 2)
        );

        Object phone = tableModel.getValueAt(selectedRowIndex, 3);
        Object address = tableModel.getValueAt(selectedRowIndex, 4);

        txtPhone.setText(phone == null ? "" : phone.toString());
        txtAddress.setText(address == null ? "" : address.toString());

        isEditing = true;
    }

  //lưu
    private void performSave() {

        String id = txtCustId.getText().trim();
        String name = txtCustName.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập Mã và Họ tên khách hàng!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        KhachHang patient = new KhachHang();

        patient.setMaKhachHang(id);
        patient.setTenKH(name);
        patient.setGioiTinh(gender);
        patient.setSdt(phone);
        patient.setDiaChi(address);

        boolean editing = isEditing;

        try {

            boolean saved = editing
                    ? patientController.updatePatient(patient)
                    : patientController.addPatient(patient);

            if (!saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể lưu khách hàng. Vui lòng kiểm tra dữ liệu hoặc mã khách hàng đã tồn tại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            clearForm();

            try {
                refreshPatients();
            } catch (SQLException e) {
                showDatabaseError(
                        "Đã lưu khách hàng nhưng không thể tải lại danh sách",
                        e
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    editing
                            ? "Cập nhật khách hàng thành công!"
                            : "Thêm khách hàng thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {
            showDatabaseError("Không thể lưu khách hàng", e);
        }
    }

    //xóa 
    private void performDelete() {

        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn dòng cần xóa trên bảng!",
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa khách hàng này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {

            String customerId = tableModel.getValueAt(
                    modelRow, 0
            ).toString();

            try {

                if (!patientController.deletePatient(customerId)) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không tìm thấy khách hàng để xóa.",
                            "Thông báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                clearForm();

                try {
                    refreshPatients();
                } catch (SQLException e) {
                    showDatabaseError(
                            "Đã xóa khách hàng nhưng không thể tải lại danh sách",
                            e
                    );
                    return;
                }

                JOptionPane.showMessageDialog(
                        this,
                        "Xóa thành công!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (SQLException e) {
                showDatabaseError("Không thể xóa khách hàng", e);
            }
        }
    }

    //làm mới
    public void refreshPatients() throws SQLException {

        List<KhachHang> patients = patientController.getPatients();

        tableModel.setRowCount(0);

        for (KhachHang patient : patients) {

            tableModel.addRow(new Object[]{
                    patient.getMaKhachHang(),
                    patient.getTenKH(),
                    patient.getGioiTinh(),
                    patient.getSdt(),
                    patient.getDiaChi(),
                    "Lịch sử"
            });
        }

        rowSorter.setRowFilter(null);
        updateEmptyPanel();
    }
//xóa

    private ImageIcon loadImage(
            String path, int width, int height
    ) {
        java.net.URL url = getClass().getResource(path);

        if (url == null) {
            return null;
        }

        ImageIcon original = new ImageIcon(url);

        Image image = original.getImage().getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        return new ImageIcon(image);
    }

   //database nếu lỗi
    private void showDatabaseError(
            String action, SQLException error
    ) {
        System.err.println(action + ": " + error.getMessage());

        JOptionPane.showMessageDialog(
                this,
                action + ". Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                "Lỗi cơ sở dữ liệu",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
