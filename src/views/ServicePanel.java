package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ServicePanel extends JPanel {
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

    private JTextField txtServiceId, txtServiceName, txtPrice, txtUnit;
    private JComboBox<String> cbCategory;
    private JButton btnSave, btnClear;

    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public ServicePanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Danh Mục Dịch Vụ Nha Khoa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý danh sách dịch vụ, đơn giá và phân loại điều trị");
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
        btnDelete.addActionListener(e -> performDeleteService());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(Box.createHorizontalStrut(5));
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] columns = {"Mã DV", "Tên dịch vụ", "Loại điều trị", "Đơn giá (VNĐ)", "Đơn vị tính"};
        Object[][] data = {
            {"DV001", "Cạo vôi răng tổng quát", "Nha khoa tổng quát", "320.000", "Lần"},
            {"DV002", "Tẩy trắng răng Laser", "Thẩm mỹ", "2.500.000", "Liệu trình"},
            {"DV003", "Niềng răng mắc cài kim loại", "Chỉnh nha", "25.000.000", "Gói"},
            {"DV004", "Nhổ răng khôn mọc lệch", "Tiểu phẫu", "1.500.000", "Răng"},
            {"DV005", "Trám răng thẩm mỹ Composite", "Nha khoa tổng quát", "450.000", "Răng"}
        };

        tableModel = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        // Tích hợp chọn dòng tự động điền form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDataToFormForEdit();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

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

        JLabel lblFormTitle = new JLabel("Thông Tin Dịch Vụ");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtServiceId = addFieldToForm(panel, "Mã dịch vụ:");
        txtServiceName = addFieldToForm(panel, "Tên dịch vụ:");
        
        JLabel lblCat = createFieldLabel("Loại điều trị:");
        panel.add(lblCat);
        panel.add(Box.createVerticalStrut(4));
        cbCategory = new JComboBox<>(new String[]{"Nha khoa tổng quát", "Thẩm mỹ", "Chỉnh nha", "Tiểu phẫu", "Phục hình sứ"});
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCategory.setBackground(Color.WHITE);
        panel.add(cbCategory);
        panel.add(Box.createVerticalStrut(12));

        txtPrice = addFieldToForm(panel, "Đơn giá (VNĐ):");
        txtUnit = addFieldToForm(panel, "Đơn vị tính:");

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> performSaveService());
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
        txtServiceId.setText("");
        txtServiceId.setEnabled(true);
        txtServiceName.setText("");
        cbCategory.setSelectedIndex(0);
        txtPrice.setText("");
        txtUnit.setText("");
        table.clearSelection();
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
    }

    private void loadDataToFormForEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = table.convertRowIndexToModel(viewRow);
        txtServiceId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
        txtServiceId.setEnabled(false);
        txtServiceName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
        cbCategory.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 2));
        txtPrice.setText((String) tableModel.getValueAt(selectedRowIndex, 3));
        txtUnit.setText((String) tableModel.getValueAt(selectedRowIndex, 4));
        isEditing = true;
    }

    private void performSaveService() {
        String id = txtServiceId.getText().trim();
        String name = txtServiceName.getText().trim();
        String category = (String) cbCategory.getSelectedItem();
        String price = txtPrice.getText().trim();
        String unit = txtUnit.getText().trim();

        if (id.isEmpty() || name.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã, Tên dịch vụ và Đơn giá!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isEditing) {
            tableModel.addRow(new Object[]{id, name, category, price, unit});
            JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt(name, selectedRowIndex, 1);
            tableModel.setValueAt(category, selectedRowIndex, 2);
            tableModel.setValueAt(price, selectedRowIndex, 3);
            tableModel.setValueAt(unit, selectedRowIndex, 4);
            JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        clearForm();
    }

    private void performDeleteService() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(modelRow);
            clearForm();
            JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}