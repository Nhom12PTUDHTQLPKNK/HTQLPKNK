package views;

import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryUpdatePanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private JTable tableThuoc, tableVatTu, tablePhieuNhap, tablePhieuXuat;
    private DefaultTableModel modelThuoc, modelVatTu, modelPhieuNhap, modelPhieuXuat;
    private TableRowSorter<DefaultTableModel> sorterThuoc, sorterVatTu, sorterPhieuNhap, sorterPhieuXuat;
    private JTextField txtSearch;

    private JTabbedPane tabbedPane;
    private JPanel dynamicFormContainer;

    // Các trường Input thay đổi linh hoạt
    private JComboBox<String> cbType;
    private JTextField txtField1, txtField2, txtField3, txtField4, txtField5;
    private JLabel lbl1, lbl2, lbl3, lbl4, lbl5;
    
    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private boolean isUpdatingTab = false;

    public InventoryUpdatePanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Cập Nhật Kho - Quản Lý Nhập Xuất (Tự Động Ngày Giờ)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Phiếu nhập tự động cập nhật thời gian hệ thống và tính thành tiền chính xác");
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

        JButton btnDeleteHeader = createStaticButton("Xóa mục", RED_ACCENT, 90);
        btnDeleteHeader.addActionListener(e -> performDelete());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(btnDeleteHeader);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. KHỞI TẠO FORM BÊN PHẢI TRƯỚC
        JPanel formPanel = createInlineFormPanel();

        // 3. TẠO TABBED PANE VÀ CÁC BẢNG DỮ LIỆU
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("Danh mục Thuốc", createThuocTab());
        tabbedPane.addTab("Danh mục Vật tư y tế", createVatTuTab());
        tabbedPane.addTab("Phiếu Nhập Hàng", createPhieuNhapTab());
        tabbedPane.addTab("Phiếu Xuất Hàng", createPhieuXuatTab());

        tabbedPane.addChangeListener(e -> {
            if (!isUpdatingTab) {
                isUpdatingTab = true;
                int idx = tabbedPane.getSelectedIndex();
                if (cbType != null) cbType.setSelectedIndex(idx);
                updateFormFieldsBasedOnTab();
                isUpdatingTab = false;
            }
        });

        centerContainer.add(tabbedPane, BorderLayout.CENTER);
        centerContainer.add(formPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
        
        updateFormFieldsBasedOnTab();
    }

    private JPanel createThuocTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        String[] cols = {"Mã thuốc", "Tên thuốc", "Đơn vị tính", "Đơn giá (VNĐ)", "Số lượng tồn"};
        Object[][] data = {{"TH001", "Amoxicillin 500mg", "Viên", "2500", "1200"}, {"TH002", "Paracetamol", "Vỉ", "15000", "450"}};
        modelThuoc = new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableThuoc = new JTable(modelThuoc);
        PdfTableExport.install(tableThuoc, "Tồn kho thuốc");
        sorterThuoc = new TableRowSorter<>(modelThuoc);
        tableThuoc.setRowSorter(sorterThuoc);
        styleTable(tableThuoc);
        tableThuoc.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tableThuoc.getSelectedRow() != -1) loadDataToFormForEdit(); });
        panel.add(new JScrollPane(tableThuoc), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVatTuTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        String[] cols = {"Mã vật tư", "Tên vật tư", "Đơn vị tính", "Số lượng tồn", "Hạn sử dụng"};
        Object[][] data = {{"VT001", "Găng tay y tế", "Hộp", "50", "20/12/2027"}};
        modelVatTu = new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tableVatTu = new JTable(modelVatTu);
        PdfTableExport.install(tableVatTu, "Tồn kho vật tư");
        sorterVatTu = new TableRowSorter<>(modelVatTu);
        tableVatTu.setRowSorter(sorterVatTu);
        styleTable(tableVatTu);
        tableVatTu.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tableVatTu.getSelectedRow() != -1) loadDataToFormForEdit(); });
        panel.add(new JScrollPane(tableVatTu), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPhieuNhapTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        String[] cols = {"Mã Phiếu Nhập", "Nhà Cung Cấp", "Tên Hàng Hóa", "Số Lượng", "Đơn Giá", "Thành Tiền (VNĐ)", "Thời Gian Lập"};
        Object[][] data = {{"PN001", "Công ty Dược Phẩm ABC", "Amoxicillin 500mg", "100", "2500", "250000", "20/10/2026 08:30:00"}};
        modelPhieuNhap = new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablePhieuNhap = new JTable(modelPhieuNhap);
        PdfTableExport.install(tablePhieuNhap, "Phiếu nhập kho");
        sorterPhieuNhap = new TableRowSorter<>(modelPhieuNhap);
        tablePhieuNhap.setRowSorter(sorterPhieuNhap);
        styleTable(tablePhieuNhap);
        tablePhieuNhap.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tablePhieuNhap.getSelectedRow() != -1) loadDataToFormForEdit(); });
        panel.add(new JScrollPane(tablePhieuNhap), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPhieuXuatTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        String[] cols = {"Mã Phiếu Xuất", "Bộ Phận Nhận", "Tên Hàng Hóa", "Số Lượng Xuất", "Lý Do Xuất", "Thời Gian Lập"};
        Object[][] data = {{"PX001", "Phòng Khám Nha Sĩ 1", "Găng tay y tế", "5", "Sử dụng khám bệnh", "20/10/2026 09:15:00"}};
        modelPhieuXuat = new DefaultTableModel(data, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tablePhieuXuat = new JTable(modelPhieuXuat);
        PdfTableExport.install(tablePhieuXuat, "Phiếu xuất kho");
        sorterPhieuXuat = new TableRowSorter<>(modelPhieuXuat);
        tablePhieuXuat.setRowSorter(sorterPhieuXuat);
        styleTable(tablePhieuXuat);
        tablePhieuXuat.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting() && tablePhieuXuat.getSelectedRow() != -1) loadDataToFormForEdit(); });
        panel.add(new JScrollPane(tablePhieuXuat), BorderLayout.CENTER);
        return panel;
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
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblFormTitle = new JLabel("Chi Tiết & Nhập Liệu Kho");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(10));

        JLabel lblType = createFieldLabel("Chuyển Tab Quản Lý:");
        panel.add(lblType);
        panel.add(Box.createVerticalStrut(4));
        cbType = new JComboBox<>(new String[]{"Danh mục Thuốc", "Danh mục Vật tư y tế", "Phiếu Nhập Hàng (Tự tính tiền)", "Phiếu Xuất Hàng"});
        cbType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbType.setBackground(Color.WHITE);
        cbType.addActionListener(e -> {
            if (!isUpdatingTab) {
                isUpdatingTab = true;
                int idx = cbType.getSelectedIndex();
                if (idx >= 0 && idx <= 3) {
                    tabbedPane.setSelectedIndex(idx);
                    updateFormFieldsBasedOnTab();
                }
                isUpdatingTab = false;
            }
        });
        panel.add(cbType);
        panel.add(Box.createVerticalStrut(10));

        dynamicFormContainer = new JPanel();
        dynamicFormContainer.setOpaque(false);
        dynamicFormContainer.setLayout(new BoxLayout(dynamicFormContainer, BoxLayout.Y_AXIS));
        dynamicFormContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        lbl1 = createFieldLabel("Mã:");
        txtField1 = createTextField();
        lbl2 = createFieldLabel("Tên:");
        txtField2 = createTextField();
        lbl3 = createFieldLabel("Đơn vị:");
        txtField3 = createTextField();
        lbl4 = createFieldLabel("Số lượng / Đơn giá:");
        txtField4 = createTextField();
        lbl5 = createFieldLabel("Thành tiền:");
        txtField5 = createTextField();
        txtField5.setEditable(false);

        KeyAdapter calcListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (tabbedPane.getSelectedIndex() == 2) {
                    try {
                        double qty = Double.parseDouble(txtField3.getText().trim().isEmpty() ? "0" : txtField3.getText().trim());
                        double price = Double.parseDouble(txtField4.getText().trim().isEmpty() ? "0" : txtField4.getText().trim());
                        txtField5.setText(String.format("%.0f", qty * price));
                    } catch (NumberFormatException ignored) {
                        txtField5.setText("0");
                    }
                }
            }
        };
        txtField3.addKeyListener(calcListener);
        txtField4.addKeyListener(calcListener);

        panel.add(dynamicFormContainer);
        panel.add(Box.createVerticalStrut(15));

        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnDelete = createStaticButton("Xóa", RED_ACCENT, Integer.MAX_VALUE);
        btnSave.addActionListener(e -> performSave());
        btnDelete.addActionListener(e -> performDelete());

        btnBox1.add(btnSave);
        btnBox1.add(btnDelete);
        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

        JPanel btnBox2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox2.setOpaque(false);
        btnBox2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEdit = createStaticButton("Sửa", GREEN_ACCENT, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);
        btnEdit.addActionListener(e -> loadDataToFormForEdit());
        btnClear.addActionListener(e -> clearForm());

        btnBox2.add(btnEdit);
        btnBox2.add(btnClear);
        panel.add(btnBox2);

        return panel;
    }

    private void updateFormFieldsBasedOnTab() {
        int tab = tabbedPane.getSelectedIndex();
        if (dynamicFormContainer == null) return;
        dynamicFormContainer.removeAll();

        if (tab == 0) { // Thuốc
            lbl1.setText("Mã thuốc:");
            lbl2.setText("Tên thuốc:");
            lbl3.setText("Đơn vị tính:");
            lbl4.setText("Đơn giá (VNĐ):");
            addPairToDynamicForm(lbl1, txtField1, lbl2, txtField2, lbl3, txtField3, lbl4, txtField4);
        } else if (tab == 1) { // Vật tư
            lbl1.setText("Mã vật tư:");
            lbl2.setText("Tên vật tư:");
            lbl3.setText("Đơn vị tính:");
            lbl4.setText("Hạn sử dụng:");
            addPairToDynamicForm(lbl1, txtField1, lbl2, txtField2, lbl3, txtField3, lbl4, txtField4);
        } else if (tab == 2) { // Phiếu Nhập Hàng
            lbl1.setText("Mã Phiếu Nhập:");
            lbl2.setText("Nhà Cung Cấp:");
            lbl3.setText("Số Lượng Nhập:");
            lbl4.setText("Đơn Giá Nhập:");
            addPairToDynamicForm(lbl1, txtField1, lbl2, txtField2, lbl3, txtField3, lbl4, txtField4);
            
            dynamicFormContainer.add(lbl5);
            dynamicFormContainer.add(Box.createVerticalStrut(2));
            dynamicFormContainer.add(txtField5);
            dynamicFormContainer.add(Box.createVerticalStrut(8));
            lbl5.setText("Thành Tiền:");
        } else { // Phiếu Xuất Hàng
            lbl1.setText("Mã Phiếu Xuất:");
            lbl2.setText("Bộ Phận Nhận:");
            lbl3.setText("Tên Hàng Hóa:");
            lbl4.setText("Số Lượng Xuất:");
            addPairToDynamicForm(lbl1, txtField1, lbl2, txtField2, lbl3, txtField3, lbl4, txtField4);
        }
        dynamicFormContainer.revalidate();
        dynamicFormContainer.repaint();
    }

    private void addPairToDynamicForm(JLabel l1, JTextField t1, JLabel l2, JTextField t2, JLabel l3, JTextField t3, JLabel l4, JTextField t4) {
        dynamicFormContainer.add(l1); dynamicFormContainer.add(Box.createVerticalStrut(2)); dynamicFormContainer.add(t1); dynamicFormContainer.add(Box.createVerticalStrut(8));
        dynamicFormContainer.add(l2); dynamicFormContainer.add(Box.createVerticalStrut(2)); dynamicFormContainer.add(t2); dynamicFormContainer.add(Box.createVerticalStrut(8));
        dynamicFormContainer.add(l3); dynamicFormContainer.add(Box.createVerticalStrut(2)); dynamicFormContainer.add(t3); dynamicFormContainer.add(Box.createVerticalStrut(8));
        dynamicFormContainer.add(l4); dynamicFormContainer.add(Box.createVerticalStrut(2)); dynamicFormContainer.add(t4); dynamicFormContainer.add(Box.createVerticalStrut(8));
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
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
        RowFilter<DefaultTableModel, Object> rf = keyword.isEmpty() ? null : RowFilter.regexFilter("(?i)" + keyword);
        int tab = tabbedPane.getSelectedIndex();
        if (tab == 0 && sorterThuoc != null) sorterThuoc.setRowFilter(rf);
        else if (tab == 1 && sorterVatTu != null) sorterVatTu.setRowFilter(rf);
        else if (tab == 2 && sorterPhieuNhap != null) sorterPhieuNhap.setRowFilter(rf);
        else if (tab == 3 && sorterPhieuXuat != null) sorterPhieuXuat.setRowFilter(rf);
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        if (txtField1 != null) { txtField1.setText(""); txtField1.setEnabled(true); }
        if (txtField2 != null) txtField2.setText("");
        if (txtField3 != null) txtField3.setText("");
        if (txtField4 != null) txtField4.setText("");
        if (txtField5 != null) txtField5.setText("");
        if (tableThuoc != null) tableThuoc.clearSelection();
        if (tableVatTu != null) tableVatTu.clearSelection();
        if (tablePhieuNhap != null) tablePhieuNhap.clearSelection();
        if (tablePhieuXuat != null) tablePhieuXuat.clearSelection();
        if (txtSearch != null) txtSearch.setText("");
    }

    private void loadDataToFormForEdit() {
        int tab = tabbedPane.getSelectedIndex();
        if (tab == 0) {
            int r = tableThuoc.getSelectedRow(); if (r == -1) return;
            selectedRowIndex = tableThuoc.convertRowIndexToModel(r);
            txtField1.setText((String) modelThuoc.getValueAt(selectedRowIndex, 0)); txtField1.setEnabled(false);
            txtField2.setText((String) modelThuoc.getValueAt(selectedRowIndex, 1));
            txtField3.setText((String) modelThuoc.getValueAt(selectedRowIndex, 2));
            txtField4.setText((String) modelThuoc.getValueAt(selectedRowIndex, 3));
        } else if (tab == 1) {
            int r = tableVatTu.getSelectedRow(); if (r == -1) return;
            selectedRowIndex = tableVatTu.convertRowIndexToModel(r);
            txtField1.setText((String) modelVatTu.getValueAt(selectedRowIndex, 0)); txtField1.setEnabled(false);
            txtField2.setText((String) modelVatTu.getValueAt(selectedRowIndex, 1));
            txtField3.setText((String) modelVatTu.getValueAt(selectedRowIndex, 2));
            txtField4.setText((String) modelVatTu.getValueAt(selectedRowIndex, 3));
        } else if (tab == 2) {
            int r = tablePhieuNhap.getSelectedRow(); if (r == -1) return;
            selectedRowIndex = tablePhieuNhap.convertRowIndexToModel(r);
            txtField1.setText((String) modelPhieuNhap.getValueAt(selectedRowIndex, 0)); txtField1.setEnabled(false);
            txtField2.setText((String) modelPhieuNhap.getValueAt(selectedRowIndex, 1));
            txtField3.setText((String) modelPhieuNhap.getValueAt(selectedRowIndex, 3));
            txtField4.setText((String) modelPhieuNhap.getValueAt(selectedRowIndex, 4));
            txtField5.setText((String) modelPhieuNhap.getValueAt(selectedRowIndex, 5));
        } else {
            int r = tablePhieuXuat.getSelectedRow(); if (r == -1) return;
            selectedRowIndex = tablePhieuXuat.convertRowIndexToModel(r);
            txtField1.setText((String) modelPhieuXuat.getValueAt(selectedRowIndex, 0)); txtField1.setEnabled(false);
            txtField2.setText((String) modelPhieuXuat.getValueAt(selectedRowIndex, 1));
            txtField3.setText((String) modelPhieuXuat.getValueAt(selectedRowIndex, 2));
            txtField4.setText((String) modelPhieuXuat.getValueAt(selectedRowIndex, 3));
        }
        isEditing = true;
    }

    private void performSave() {
        String f1 = txtField1.getText().trim();
        String f2 = txtField2.getText().trim();
        String f3 = txtField3.getText().trim();
        String f4 = txtField4.getText().trim();
        int tab = tabbedPane.getSelectedIndex();

        if (f1.isEmpty() || f2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy ngày giờ hệ thống tự động
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        if (tab == 0) {
            if (!isEditing) modelThuoc.addRow(new Object[]{f1, f2, f3, f4, "100"});
            else { modelThuoc.setValueAt(f2, selectedRowIndex, 1); modelThuoc.setValueAt(f3, selectedRowIndex, 2); modelThuoc.setValueAt(f4, selectedRowIndex, 3); }
        } else if (tab == 1) {
            if (!isEditing) modelVatTu.addRow(new Object[]{f1, f2, f3, f4, "31/12/2027"});
            else { modelVatTu.setValueAt(f2, selectedRowIndex, 1); modelVatTu.setValueAt(f3, selectedRowIndex, 2); modelVatTu.setValueAt(f4, selectedRowIndex, 3); }
        } else if (tab == 2) { // Phiếu Nhập Hàng (Tự tính thành tiền & lấy ngày giờ hiện tại)
            try {
                double qty = Double.parseDouble(f3.isEmpty() ? "0" : f3);
                double price = Double.parseDouble(f4.isEmpty() ? "0" : f4);
                double total = qty * price;
                String totalStr = String.format("%.0f", total);
                
                if (!isEditing) {
                    modelPhieuNhap.addRow(new Object[]{f1, f2, "Hàng hóa tổng hợp", f3, f4, totalStr, currentDateTime});
                } else {
                    modelPhieuNhap.setValueAt(f2, selectedRowIndex, 1);
                    modelPhieuNhap.setValueAt(f3, selectedRowIndex, 3);
                    modelPhieuNhap.setValueAt(f4, selectedRowIndex, 4);
                    modelPhieuNhap.setValueAt(totalStr, selectedRowIndex, 5);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số lượng và đơn giá phải là số hợp lệ để tính tiền!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { // Phiếu Xuất Hàng (Lấy ngày giờ hiện tại)
            if (!isEditing) {
                modelPhieuXuat.addRow(new Object[]{f1, f2, f3, f4, "Xuất kho sử dụng", currentDateTime});
            } else {
                modelPhieuXuat.setValueAt(f2, selectedRowIndex, 1);
                modelPhieuXuat.setValueAt(f3, selectedRowIndex, 2);
                modelPhieuXuat.setValueAt(f4, selectedRowIndex, 3);
            }
        }
        JOptionPane.showMessageDialog(this, "Thao tác thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
    }

    private void performDelete() {
        int tab = tabbedPane.getSelectedIndex();
        if (tab == 0 && tableThuoc.getSelectedRow() != -1) modelThuoc.removeRow(tableThuoc.convertRowIndexToModel(tableThuoc.getSelectedRow()));
        else if (tab == 1 && tableVatTu.getSelectedRow() != -1) modelVatTu.removeRow(tableVatTu.convertRowIndexToModel(tableVatTu.getSelectedRow()));
        else if (tab == 2 && tablePhieuNhap.getSelectedRow() != -1) modelPhieuNhap.removeRow(tablePhieuNhap.convertRowIndexToModel(tablePhieuNhap.getSelectedRow()));
        else if (tab == 3 && tablePhieuXuat.getSelectedRow() != -1) modelPhieuXuat.removeRow(tablePhieuXuat.convertRowIndexToModel(tablePhieuXuat.getSelectedRow()));
        else JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        clearForm();
    }
}