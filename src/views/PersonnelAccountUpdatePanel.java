package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class PersonnelAccountUpdatePanel extends JPanel {
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

    private JTextField txtEmpId, txtEmpName, txtUsername, txtPhone, txtSalary;
    private JComboBox<String> cbRole, cbStatus;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public PersonnelAccountUpdatePanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cập Nhật Nhân Sự & Tài Khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Đồng bộ quản lý thông tin nhân viên, lương thưởng và tài khoản phân quyền truy cập");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // TOOLBAR
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

        // CENTER CONTAINER
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] columns = {"Mã NV", "Họ và tên", "Tên đăng nhập", "Chức vụ", "SĐT", "Mức lương (VNĐ)", "Trạng thái TK"};
        Object[][] data = {
            {"NV001", "Phan Lê Ngọc Ánh", "admin01", "NV Quản lý", "0901234567", "18.000.000", "Đang hoạt động"},
            {"NV002", "Nguyễn Minh Sỹ Luân", "bacsi01", "Nha sĩ", "0912345678", "25.000.000", "Đang hoạt động"},
            {"NV003", "Nguyễn Ngọc Minh Châu", "letan01", "Lễ tân", "0987654321", "10.000.000", "Đang hoạt động"},
            {"NV004", "Nguyễn Gia Băng", "yta01", "Y tá", "0933987654", "12.000.000", "Đang hoạt động"}
        };

        tableModel = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadDataToFormForEdit();
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = createInlineFormPanel();
        centerContainer.add(formPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
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

        JLabel lblFormTitle = new JLabel("Thông Tin Nhân Sự & TK");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtEmpId = addFieldToForm(panel, "Mã nhân viên:");
        txtEmpName = addFieldToForm(panel, "Họ và tên:");
        txtUsername = addFieldToForm(panel, "Tên đăng nhập:");

        JLabel lblRole = createFieldLabel("Chức vụ:");
        panel.add(lblRole);
        panel.add(Box.createVerticalStrut(4));
        cbRole = new JComboBox<>(new String[]{"NV Quản lý", "Nha sĩ", "Lễ tân", "Y tá"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRole.setBackground(Color.WHITE);
        panel.add(cbRole);
        panel.add(Box.createVerticalStrut(12));

        txtPhone = addFieldToForm(panel, "Số điện thoại:");
        txtSalary = addFieldToForm(panel, "Mức lương:");

        JLabel lblStatus = createFieldLabel("Trạng thái tài khoản:");
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(4));
        cbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Tạm khóa"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setBackground(Color.WHITE);
        panel.add(cbStatus);
        panel.add(Box.createVerticalStrut(16));

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

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
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtEmpId.setText("");
        txtEmpId.setEnabled(true);
        txtEmpName.setText("");
        txtUsername.setText("");
        txtUsername.setEnabled(true);
        cbRole.setSelectedIndex(0);
        txtPhone.setText("");
        txtSalary.setText("");
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
    }

    private void loadDataToFormForEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = table.convertRowIndexToModel(viewRow);
        txtEmpId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
        txtEmpId.setEnabled(false);
        txtEmpName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
        txtUsername.setText((String) tableModel.getValueAt(selectedRowIndex, 2));
        txtUsername.setEnabled(false);
        cbRole.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 3));
        txtPhone.setText((String) tableModel.getValueAt(selectedRowIndex, 4));
        txtSalary.setText((String) tableModel.getValueAt(selectedRowIndex, 5));
        cbStatus.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 6));
        isEditing = true;
    }

    private void performSave() {
        String id = txtEmpId.getText().trim();
        String name = txtEmpName.getText().trim();
        String user = txtUsername.getText().trim();
        String role = (String) cbRole.getSelectedItem();
        String phone = txtPhone.getText().trim();
        String salary = txtSalary.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (id.isEmpty() || name.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã NV, Họ tên và Tên đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isEditing) {
            tableModel.addRow(new Object[]{id, name, user, role, phone, salary, status});
            JOptionPane.showMessageDialog(this, "Thêm nhân sự & tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt(name, selectedRowIndex, 1);
            tableModel.setValueAt(role, selectedRowIndex, 3);
            tableModel.setValueAt(phone, selectedRowIndex, 4);
            tableModel.setValueAt(salary, selectedRowIndex, 5);
            tableModel.setValueAt(status, selectedRowIndex, 6);
            JOptionPane.showMessageDialog(this, "Cập nhật nhân sự thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        clearForm();
    }

    private void performDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân sự cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân sự và tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            tableModel.removeRow(modelRow);
            clearForm();
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}