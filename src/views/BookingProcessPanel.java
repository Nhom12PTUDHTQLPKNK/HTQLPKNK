package views;

import Audit.AuditLog;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class BookingProcessPanel extends JPanel {
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
    private JTextField txtSearch, txtAppId, txtCustomerName, txtPhone, txtAppDate, txtAppTime;
    private JComboBox<String> cbService, cbDoctor;
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

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {"Mã Lịch Hẹn", "Tên Khách Hàng", "Số Điện Thoại", "Dịch Vụ Khám", "Nha Sĩ Phụ Trách", "Ngày Hẹn", "Giờ Hẹn", "Trạng Thái"};
        Object[][] data = {
            {"LH001", "Nguyễn Gia Băng", "0901122334", "Khám tổng quát", "BS. Nguyễn Minh Sỹ Luân", "21/10/2026", "09:00", "Chờ xác nhận"},
            {"LH002", "Nguyễn Hoàng Khải", "0988776655", "Lấy cao răng", "BS. Phan Lê Ngọc Ánh", "21/10/2026", "10:30", "Đã xác nhận"},
            {"LH003", "Nguyễn Ngọc Minh Châu", "0912345678", "Niềng răng", "BS. Nguyễn Hoàng Khải", "22/10/2026", "14:00", "Chờ khám"}
        };

        tableModel = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Lịch hẹn");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
                txtAppId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
                txtAppId.setEnabled(false);
                txtCustomerName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
                txtPhone.setText((String) tableModel.getValueAt(selectedRowIndex, 2));
                cbService.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 3));
                cbDoctor.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 4));
                txtAppDate.setText((String) tableModel.getValueAt(selectedRowIndex, 5));
                txtAppTime.setText((String) tableModel.getValueAt(selectedRowIndex, 6));
                isEditing = true;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        centerContainer.add(scroll, BorderLayout.CENTER);

        centerContainer.add(createBookingFormPanel(), BorderLayout.EAST);
        add(centerContainer, BorderLayout.CENTER);
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
        cbService = new JComboBox<>(new String[]{"Khám tổng quát", "Lấy cao răng", "Trám răng", "Niềng răng", "Bọc răng sứ"});
        cbService.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbService.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbService.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbService.setBackground(Color.WHITE);
        panel.add(cbService);
        panel.add(Box.createVerticalStrut(10));

        JLabel lblDoc = createFieldLabel("Nha Sĩ Phụ Trách:");
        panel.add(lblDoc);
        panel.add(Box.createVerticalStrut(3));
        cbDoctor = new JComboBox<>(new String[]{"BS. Nguyễn Minh Sỹ Luân", "BS. Phan Lê Ngọc Ánh", "BS. Nguyễn Hoàng Khải"});
        cbDoctor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDoctor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDoctor.setBackground(Color.WHITE);
        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(10));

        txtAppDate = addField(panel, "Ngày Hẹn (dd/MM/yyyy):");
        txtAppTime = addField(panel, "Giờ Hẹn (HH:mm):");

        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", GREEN_ACCENT, Integer.MAX_VALUE);
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

        JButton btnClear = createStaticButton("Làm Mới", PINK_ACCENT, Integer.MAX_VALUE);
        btnClear.addActionListener(e -> clearForm());
        btnBox2.add(btnClear);
        panel.add(btnBox2);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addField(JPanel p, String labelText) {
        p.add(createFieldLabel(labelText));
        p.add(Box.createVerticalStrut(3));
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        p.add(t);
        p.add(Box.createVerticalStrut(10));
        return t;
    }

    private void performSave() {
        String id = txtAppId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String phone = txtPhone.getText().trim();
        String service = (String) cbService.getSelectedItem();
        String doctor = (String) cbDoctor.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtAppTime.getText().trim();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã lịch, Tên khách và SĐT!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isEditing) {
            tableModel.addRow(new Object[]{id, name, phone, service, doctor, date, time, "Chờ xác nhận"});
            JOptionPane.showMessageDialog(this, "Tạo lịch hẹn mới thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt(name, selectedRowIndex, 1);
            tableModel.setValueAt(phone, selectedRowIndex, 2);
            tableModel.setValueAt(service, selectedRowIndex, 3);
            tableModel.setValueAt(doctor, selectedRowIndex, 4);
            tableModel.setValueAt(date, selectedRowIndex, 5);
            tableModel.setValueAt(time, selectedRowIndex, 6);
            JOptionPane.showMessageDialog(this, "Cập nhật lịch hẹn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        AuditLog.event(isEditing ? "UPDATE" : "CREATE", "Tiếp nhận lịch hẹn - mã " + id);
        clearForm();
    }

    private void performDelete() {
        int r = table.getSelectedRow();
        if (r != -1) {
            int modelRow = table.convertRowIndexToModel(r);
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy lịch hẹn này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
                tableModel.removeRow(modelRow);
                AuditLog.event("DELETE", "Tiếp nhận lịch hẹn - mã " + id);
                clearForm();
                JOptionPane.showMessageDialog(this, "Đã xóa lịch hẹn!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch hẹn cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtAppId.setText("");
        txtAppId.setEnabled(true);
        txtCustomerName.setText("");
        txtPhone.setText("");
        cbService.setSelectedIndex(0);
        cbDoctor.setSelectedIndex(0);
        txtAppDate.setText("");
        txtAppTime.setText("");
        table.clearSelection();
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        rowSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
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
}
