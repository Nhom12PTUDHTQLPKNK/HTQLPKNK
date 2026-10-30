package views;

import Audit.AuditLog;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppointmentBookingPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private JTable tableLichDat;
    private DefaultTableModel modelLichDat;
    private TableRowSorter<DefaultTableModel> sorterLichDat;
    private JTextField txtSearchPhone;

    // Các trường form đặt lịch
    private JTextField txtAppId, txtCustId, txtCustName, txtPhone, txtAppDate, txtTime;
    private JComboBox<String> cbService, cbDoctor, cbStatus;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public AppointmentBookingPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER & Ô TÌM KIẾM NHANH BẰNG SĐT
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý & Đặt Lịch Hẹn Khách Hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Thêm mới lịch hẹn hoặc nhập SĐT để tra cứu và cập nhật thông tin đặt lịch");
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

        JButton btnSearch = createStaticButton("Tra Cứu", BLUE_PRIMARY, 90);
        btnSearch.addActionListener(e -> performPhoneLookup());

        toolbarPanel.add(lblSearchPhone);
        toolbarPanel.add(txtSearchPhone);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // =========================================================================
        // CENTER: BẢNG DANH SÁCH LỊCH HẸN & FORM NHẬP BÊN PHẢI
        // =========================================================================
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        // Khu vực bảng danh sách
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Mã Lịch Hẹn", "Mã KH", "Tên Khách Hàng", "SĐT", "Dịch vụ khám", "Nha sĩ phụ trách", "Ngày hẹn", "Giờ", "Trạng thái"};
        Object[][] data = {
            {"LH001", "KH001", "Nguyễn Gia Băng", "0901122334", "Khám tổng quát", "BS. Nguyễn Minh Sỹ Luân", "21/10/2026", "09:00", "Đã xác nhận"},
            {"LH002", "KH002", "Nguyễn Hoàng Khải", "0988776655", "Lấy cao răng", "BS. Phan Lê Ngọc Ánh", "21/10/2026", "10:30", "Chờ khám"},
            {"LH003", "KH003", "Nguyễn Ngọc Minh Châu", "0912345678", "Niềng răng", "BS. Nguyễn Hoàng Khải", "22/10/2026", "14:00", "Đã xác nhận"}
        };

        modelLichDat = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLichDat = new JTable(modelLichDat);
        sorterLichDat = new TableRowSorter<>(modelLichDat);
        tableLichDat.setRowSorter(sorterLichDat);
        styleTable(tableLichDat);

        tableLichDat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableLichDat.getSelectedRow() != -1) {
                loadDataToFormForEdit();
            }
        });

        JScrollPane scroll = new JScrollPane(tableLichDat);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        tablePanel.add(scroll, BorderLayout.CENTER);

        centerContainer.add(tablePanel, BorderLayout.CENTER);

        // Form nhập liệu bên phải
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

        JLabel lblFormTitle = new JLabel("Thông Tin Đặt Lịch Hẹn");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtAppId = addFieldToForm(panel, "Mã lịch hẹn:");
        txtCustId = addFieldToForm(panel, "Mã khách hàng:");
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

        txtAppDate = addFieldToForm(panel, "Ngày hẹn (dd/MM/yyyy):");
        txtTime = addFieldToForm(panel, "Giờ hẹn (HH:mm):");

        JLabel lblStatus = createFieldLabel("Trạng thái lịch hẹn:");
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(4));
        cbStatus = new JComboBox<>(new String[]{"Chờ khám", "Đã xác nhận", "Đã hủy"});
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
        JButton btnClear = createStaticButton("Làm Mới", PINK_ACCENT, Integer.MAX_VALUE);

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
        txtCustId.setText("");
        txtCustName.setText("");
        txtPhone.setText("");
        cbService.setSelectedIndex(0);
        cbDoctor.setSelectedIndex(0);
        txtAppDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        txtTime.setText("09:00");
        cbStatus.setSelectedIndex(0);
        tableLichDat.clearSelection();
        txtSearchPhone.setText("");
    }

    private void loadDataToFormForEdit() {
        int viewRow = tableLichDat.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = tableLichDat.convertRowIndexToModel(viewRow);
        
        txtAppId.setText((String) modelLichDat.getValueAt(selectedRowIndex, 0));
        txtAppId.setEnabled(false); // Không cho sửa mã lịch hẹn khi đang chọn dòng cũ
        txtCustId.setText((String) modelLichDat.getValueAt(selectedRowIndex, 1));
        txtCustName.setText((String) modelLichDat.getValueAt(selectedRowIndex, 2));
        txtPhone.setText((String) modelLichDat.getValueAt(selectedRowIndex, 3));
        cbService.setSelectedItem(modelLichDat.getValueAt(selectedRowIndex, 4));
        cbDoctor.setSelectedItem(modelLichDat.getValueAt(selectedRowIndex, 5));
        txtAppDate.setText((String) modelLichDat.getValueAt(selectedRowIndex, 6));
        txtTime.setText((String) modelLichDat.getValueAt(selectedRowIndex, 7));
        cbStatus.setSelectedItem(modelLichDat.getValueAt(selectedRowIndex, 8));
        isEditing = true;
    }

    private void performPhoneLookup() {
        String phoneKey = txtSearchPhone.getText().trim();
        if (phoneKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại cần tra cứu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean found = false;
        for (int i = 0; i < modelLichDat.getRowCount(); i++) {
            String phoneInTable = (String) modelLichDat.getValueAt(i, 3);
            if (phoneInTable.contains(phoneKey)) {
                int viewRow = tableLichDat.convertRowIndexToView(i);
                tableLichDat.setRowSelectionInterval(viewRow, viewRow);
                tableLichDat.scrollRectToVisible(tableLichDat.getCellRect(viewRow, 0, true));
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy lịch hẹn cho SĐT: " + phoneKey, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void performSave() {
        String id = txtAppId.getText().trim();
        String custId = txtCustId.getText().trim();
        String name = txtCustName.getText().trim();
        String phone = txtPhone.getText().trim();
        String serv = (String) cbService.getSelectedItem();
        String doc = (String) cbDoctor.getSelectedItem();
        String date = txtAppDate.getText().trim();
        String time = txtTime.getText().trim();
        String status = (String) cbStatus.getSelectedItem();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã lịch hẹn, Tên và SĐT khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] rowData = new Object[]{id, custId, name, phone, serv, doc, date, time, status};

        if (!isEditing) {
            modelLichDat.addRow(rowData);
            JOptionPane.showMessageDialog(this, "Thêm mới lịch hẹn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            modelLichDat.removeRow(selectedRowIndex);
            modelLichDat.insertRow(selectedRowIndex, rowData);
            JOptionPane.showMessageDialog(this, "Cập nhật lịch hẹn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        
        try {
            AuditLog.event(isEditing ? "UPDATE" : "CREATE", "Lịch hẹn - mã " + id);
        } catch (Exception ignored) {}

        clearForm();
    }
}