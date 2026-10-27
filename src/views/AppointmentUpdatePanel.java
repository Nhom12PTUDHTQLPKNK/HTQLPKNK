package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AppointmentUpdatePanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private JTabbedPane tabbedPane;
    private JTable tableChoXuLy, tableDaLam;
    private DefaultTableModel modelChoXuLy, modelDaLam;
    private TableRowSorter<DefaultTableModel> sorterChoXuLy, sorterDaLam;
    private JTextField txtSearchPhone;

    private JTextField txtAppId, txtCustName, txtPhone, txtAppDate, txtTime;
    private JComboBox<String> cbService, cbDoctor, cbStatus;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private int activeTabSource = 0; // 0: Đang chờ, 1: Đã làm

    public AppointmentUpdatePanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER & Ô TÌM KIẾM NHANH BẰNG SĐT
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tiếp Nhận & Quản Lý Lịch Hẹn Khám");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Nhập SĐT để tra cứu lịch hẹn; Khi lưu hoàn thành, lịch sẽ tự động chuyển sang mục 'Đã làm'");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // THANH TÌM KIẾM SĐT Ở GÓC TRÊN BÊN PHẢI
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JLabel lblSearchPhone = new JLabel("Nhập SĐT:");
        lblSearchPhone.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearchPhone.setForeground(TEXT_DARK);

        txtSearchPhone = new JTextField(14);
        txtSearchPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchPhone.setPreferredSize(new Dimension(150, 36));
        txtSearchPhone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearchPhone.addActionListener(e -> performPhoneLookup());

        JButton btnSearch = createStaticButton("Kiểm Tra", BLUE_PRIMARY, 90);
        btnSearch.addActionListener(e -> performPhoneLookup());

        toolbarPanel.add(lblSearchPhone);
        toolbarPanel.add(txtSearchPhone);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // =========================================================================
        // 2 TAB CHÍNH: CHỜ XỬ LÝ & ĐÃ LÀM + FORM BÊN PHẢI
        // =========================================================================
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("Lịch Hẹn Chờ Xử Lý", createChoXuLyTab());
        tabbedPane.addTab("Lịch Hẹn Đã Làm", createDaLamTab());

        centerContainer.add(tabbedPane, BorderLayout.CENTER);

        JPanel formPanel = createInlineFormPanel();
        centerContainer.add(formPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createChoXuLyTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Mã Lịch Hẹn", "Tên Khách Hàng", "SĐT", "Dịch vụ khám", "Nha sĩ phụ trách", "Ngày hẹn", "Giờ", "Trạng thái"};
        Object[][] data = {
            {"LH001", "Nguyễn Minh Anh", "0901234567", "Khám tổng quát", "BS. Nguyễn Minh Sỹ Luân", "20/10/2026", "08:30", "Chờ khám"},
            {"LH002", "Lê Thị Bích", "0987654321", "Lấy cao răng", "BS. Nguyễn Minh Sỹ Luân", "20/10/2026", "09:30", "Đã xác nhận"}
        };

        modelChoXuLy = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableChoXuLy = new JTable(modelChoXuLy);
        sorterChoXuLy = new TableRowSorter<>(modelChoXuLy);
        tableChoXuLy.setRowSorter(sorterChoXuLy);
        styleTable(tableChoXuLy);

        tableChoXuLy.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableChoXuLy.getSelectedRow() != -1) {
                activeTabSource = 0;
                loadDataToFormForEdit(tableChoXuLy, modelChoXuLy);
            }
        });

        JScrollPane scroll = new JScrollPane(tableChoXuLy);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDaLamTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Mã Lịch Hẹn", "Tên Khách Hàng", "SĐT", "Dịch vụ khám", "Nha sĩ phụ trách", "Ngày hẹn", "Giờ", "Trạng thái"};
        Object[][] data = {
            {"LH003", "Phạm Văn Dũng", "0912345678", "Trám răng", "BS. Nguyễn Minh Sỹ Luân", "19/10/2026", "10:30", "Hoàn thành"}
        };

        modelDaLam = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableDaLam = new JTable(modelDaLam);
        sorterDaLam = new TableRowSorter<>(modelDaLam);
        tableDaLam.setRowSorter(sorterDaLam);
        styleTable(tableDaLam);

        tableDaLam.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableDaLam.getSelectedRow() != -1) {
                activeTabSource = 1;
                loadDataToFormForEdit(tableDaLam, modelDaLam);
            }
        });

        JScrollPane scroll = new JScrollPane(tableDaLam);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void performPhoneLookup() {
        String phoneKey = txtSearchPhone.getText().trim();
        if (phoneKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại cần tìm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean found = false;
        // Tìm trong bảng Chờ xử lý trước
        for (int i = 0; i < modelChoXuLy.getRowCount(); i++) {
            if (((String) modelChoXuLy.getValueAt(i, 2)).contains(phoneKey)) {
                tabbedPane.setSelectedIndex(0);
                int viewRow = tableChoXuLy.convertRowIndexToView(i);
                tableChoXuLy.setRowSelectionInterval(viewRow, viewRow);
                tableChoXuLy.scrollRectToVisible(tableChoXuLy.getCellRect(viewRow, 0, true));
                found = true;
                break;
            }
        }
        // Nếu không có, tìm trong bảng Đã làm
        if (!found) {
            for (int i = 0; i < modelDaLam.getRowCount(); i++) {
                if (((String) modelDaLam.getValueAt(i, 2)).contains(phoneKey)) {
                    tabbedPane.setSelectedIndex(1);
                    int viewRow = tableDaLam.convertRowIndexToView(i);
                    tableDaLam.setRowSelectionInterval(viewRow, viewRow);
                    tableDaLam.scrollRectToVisible(tableDaLam.getCellRect(viewRow, 0, true));
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn với SĐT: " + phoneKey, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

        JLabel lblFormTitle = new JLabel("Xử Lý Tiếp Nhận Lịch");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtAppId = addFieldToForm(panel, "Mã lịch hẹn:");
        txtCustName = addFieldToForm(panel, "Tên khách hàng:");
        txtPhone = addFieldToForm(panel, "Số điện thoại:");

        JLabel lblServ = createFieldLabel("Dịch vụ khám:");
        panel.add(lblServ);
        panel.add(Box.createVerticalStrut(4));
        cbService = new JComboBox<>(new String[]{"Khám tổng quát", "Lấy cao răng", "Trám răng", "Niềng răng", "Bọc răng sứ"});
        cbService.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbService.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbService.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbService.setBackground(Color.WHITE);
        panel.add(cbService);
        panel.add(Box.createVerticalStrut(12));

        JLabel lblDoc = createFieldLabel("Nha sĩ phụ trách:");
        panel.add(lblDoc);
        panel.add(Box.createVerticalStrut(4));
        cbDoctor = new JComboBox<>(new String[]{"BS. Nguyễn Minh Sỹ Luân", "BS. Phan Lê Ngọc Ánh", "BS. Nguyễn Hoàng Khải"});
        cbDoctor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDoctor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDoctor.setBackground(Color.WHITE);
        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(12));

        txtAppDate = addFieldToForm(panel, "Ngày khám (dd/MM/yyyy):");
        txtTime = addFieldToForm(panel, "Giờ khám (HH:mm):");

        JLabel lblStatus = createFieldLabel("Trạng thái (Chọn 'Hoàn thành' sẽ chuyển sang Đã làm):");
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(4));
        cbStatus = new JComboBox<>(new String[]{"Chờ khám", "Đã xác nhận", "Hoàn thành", "Đã hủy"});
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

        JButton btnSave = createStaticButton("Lưu Lịch", GREEN_ACCENT, Integer.MAX_VALUE);
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

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtAppId.setText("");
        txtAppId.setEnabled(true);
        txtCustName.setText("");
        txtPhone.setText("");
        cbService.setSelectedIndex(0);
        cbDoctor.setSelectedIndex(0);
        txtAppDate.setText("");
        txtTime.setText("");
        cbStatus.setSelectedIndex(0);
        tableChoXuLy.clearSelection();
        tableDaLam.clearSelection();
        txtSearchPhone.setText("");
    }

    private void loadDataToFormForEdit(JTable targetTable, DefaultTableModel targetModel) {
        int viewRow = targetTable.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = targetTable.convertRowIndexToModel(viewRow);
        txtAppId.setText((String) targetModel.getValueAt(selectedRowIndex, 0));
        txtAppId.setEnabled(false);
        txtCustName.setText((String) targetModel.getValueAt(selectedRowIndex, 1));
        txtPhone.setText((String) targetModel.getValueAt(selectedRowIndex, 2));
        cbService.setSelectedItem(targetModel.getValueAt(selectedRowIndex, 3));
        cbDoctor.setSelectedItem(targetModel.getValueAt(selectedRowIndex, 4));
        txtAppDate.setText((String) targetModel.getValueAt(selectedRowIndex, 5));
        txtTime.setText((String) targetModel.getValueAt(selectedRowIndex, 6));
        cbStatus.setSelectedItem(targetModel.getValueAt(selectedRowIndex, 7));
        isEditing = true;
    }

    private void performSave() {
        String id = txtAppId.getText().trim();
        String name = txtCustName.getText().trim();
        String phone = txtPhone.getText().trim();
        String serv = (String) cbService.getSelectedItem();
        String doc = (String) cbDoctor.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtTime.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin lịch hẹn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] rowData = new Object[]{id, name, phone, serv, doc, date, time, status};

        // Nếu chọn trạng thái "Hoàn thành" hoặc "Đã hủy", tự động chuyển hoặc thêm vào bảng "Đã làm"
        boolean isDone = "Hoàn thành".equals(status) || "Đã hủy".equals(status);

        if (!isEditing) {
            // Thêm mới
            if (isDone) {
                modelDaLam.addRow(rowData);
            } else {
                modelChoXuLy.addRow(rowData);
            }
            JOptionPane.showMessageDialog(this, "Thêm và tiếp nhận lịch hẹn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Đang chỉnh sửa
            if (activeTabSource == 0) { // Đang ở bảng Chờ xử lý
                modelChoXuLy.removeRow(selectedRowIndex);
                if (isDone) {
                    modelDaLam.addRow(rowData);
                    JOptionPane.showMessageDialog(this, "Đã hoàn thành và chuyển lịch sang mục 'Đã làm'!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    modelChoXuLy.addRow(rowData);
                    JOptionPane.showMessageDialog(this, "Cập nhật lịch hẹn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            } else { // Đang ở bảng Đã làm
                modelDaLam.removeRow(selectedRowIndex);
                if (!isDone) {
                    modelChoXuLy.addRow(rowData);
                    JOptionPane.showMessageDialog(this, "Chuyển ngược lịch về mục 'Chờ xử lý'!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    modelDaLam.addRow(rowData);
                    JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        clearForm();
    }
}