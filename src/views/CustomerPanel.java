package views;

import controllers.PatientController;
import models.entity.KhachHang;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
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
    private final PatientController patientController = new PatientController();

    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Danh Mục Khách Hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý hồ sơ thông tin bệnh nhân và khách hàng nha khoa");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

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

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] columns = {"Mã KH", "Họ và tên", "Giới tính", "Số điện thoại", "Địa chỉ"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        PdfTableExport.install(table, "Danh sách khách hàng");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDataToFormForEdit();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = createInlineFormPanel();
        centerContainer.add(formPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
        try {
            refreshPatients();
        } catch (SQLException e) {
            showDatabaseError("Không thể tải danh sách khách hàng", e);
        }
    }

    private JPanel createInlineFormPanel() {
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
        cbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cbGender.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbGender.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbGender.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbGender.setBackground(Color.WHITE);
        panel.add(cbGender);
        panel.add(Box.createVerticalStrut(12));

        txtPhone = addFieldToForm(panel, "Số điện thoại:");
        txtAddress = addFieldToForm(panel, "Địa chỉ:");

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

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

    private JTextField addFieldToForm(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
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

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 253));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
    }

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
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
    }

    private void loadDataToFormForEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = table.convertRowIndexToModel(viewRow);
        txtCustId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
        txtCustId.setEnabled(false);
        txtCustName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
        cbGender.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 2));
        txtPhone.setText((String) tableModel.getValueAt(selectedRowIndex, 3));
        txtAddress.setText((String) tableModel.getValueAt(selectedRowIndex, 4));
        isEditing = true;
    }

    private void performSave() {
        String id = txtCustId.getText().trim();
        String name = txtCustName.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã và Họ tên khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        "Không thể lưu khách hàng. Vui lòng kiểm tra dữ liệu hoặc mã khách hàng đã tồn tại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            clearForm();
            try {
                refreshPatients();
            } catch (SQLException e) {
                showDatabaseError("Đã lưu khách hàng nhưng không thể tải lại danh sách", e);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    editing ? "Cập nhật khách hàng thành công!" : "Thêm khách hàng thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError("Không thể lưu khách hàng", e);
            return;
        }
    }

    private void performDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String customerId = (String) tableModel.getValueAt(modelRow, 0);
            try {
                if (!patientController.deletePatient(customerId)) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                clearForm();
                try {
                    refreshPatients();
                } catch (SQLException e) {
                    showDatabaseError("Đã xóa khách hàng nhưng không thể tải lại danh sách", e);
                    return;
                }
            } catch (SQLException e) {
                showDatabaseError("Không thể xóa khách hàng", e);
                return;
            }
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refreshPatients() throws SQLException {
        List<KhachHang> patients = patientController.getPatients();
        tableModel.setRowCount(0);
        for (KhachHang patient : patients) {
            tableModel.addRow(new Object[]{
                    patient.getMaKhachHang(),
                    patient.getTenKH(),
                    patient.getGioiTinh(),
                    patient.getSdt(),
                    patient.getDiaChi()
            });
        }
        rowSorter.setRowFilter(null);
    }

    private void showDatabaseError(String action, SQLException error) {
        System.err.println(action + ": " + error.getMessage());
        JOptionPane.showMessageDialog(this,
                action + ". Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}