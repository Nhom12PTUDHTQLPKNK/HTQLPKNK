package views;

	import javax.swing.*;
	import javax.swing.table.DefaultTableModel;
	import java.awt.*;

	public class WorkProcessPanel extends JPanel {

<<<<<<< HEAD
	    private JTable tblSchedule;
	    private DefaultTableModel tableModel;
=======
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch, txtShiftId, txtStaffName;
    private datepicker.DatePickerField txtDate;
    private JComboBox<String> cbShiftTime;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git

	    public WorkProcessPanel() {
	        setLayout(new BorderLayout(10, 10));
	        setBackground(Color.WHITE);
	        initUI();
	    }

	    private void initUI() {
	        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
	        pnlForm.setBorder(BorderFactory.createTitledBorder("Phân ca làm việc & Kiểm tra trùng lịch"));

	        pnlForm.add(new JLabel("Nhân viên/Nha sĩ:"));
	        pnlForm.add(new JComboBox<>(new String[]{"BS. Nguyễn Văn A (Nha sĩ)", "NV. Trần Văn Luân (Phụ tá)"}));
	        pnlForm.add(new JLabel("Ngày làm:"));
	        pnlForm.add(new JTextField("2026-11-01", 8));
	        pnlForm.add(new JLabel("Ca làm:"));
	        pnlForm.add(new JComboBox<>(new String[]{"Ca Sáng (08:00 - 12:00)", "Ca Chiều (13:30 - 17:30)"}));

	        JButton btnCheck = new JButton("🔍 Kiểm tra trùng ca");
	        JButton btnAssign = new JButton("➕ Phân ca");
	        btnAssign.setBackground(new Color(0, 123, 255));
	        btnAssign.setForeground(Color.WHITE);

	        pnlForm.add(btnCheck);
	        pnlForm.add(btnAssign);

	        add(pnlForm, BorderLayout.NORTH);

	        String[] cols = {"Mã NV", "Họ và Tên", "Chức vụ", "Ngày làm", "Ca làm", "Thời gian", "Phòng / Ghế nha", "Trạng thái"};
	        tableModel = new DefaultTableModel(cols, 0);
	        tblSchedule = new JTable(tableModel);
	        tblSchedule.setRowHeight(26);

	        tableModel.addRow(new Object[]{"NV001", "BS. Nguyễn Văn A", "Nha sĩ", "2026-11-01", "Ca Sáng", "08:00 - 12:00", "Phòng khám 1 - Ghế 1", "Đã phân ca"});
	        tableModel.addRow(new Object[]{"NV002", "Trần Văn Luân", "Phụ tá", "2026-11-01", "Ca Sáng", "08:00 - 12:00", "Phòng khám 1 - Ghế 1", "Đã phân ca"});

<<<<<<< HEAD
	        add(new JScrollPane(tblSchedule), BorderLayout.CENTER);
	    }
	}
=======
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {"Mã Phân Ca", "Họ Tên Nhân Viên", "Ca Trực", "Ngày Trực", "Trạng Thái"};
        Object[][] data = {
            {"PC001", "Nguyễn Gia Băng", "Ca Sáng (08:00 - 12:00)", "21/10/2026", "Đã phân công"},
            {"PC002", "Nguyễn Hoàng Khải", "Ca Chiều (13:30 - 17:30)", "21/10/2026", "Đã phân công"},
            {"PC003", "Nguyễn Ngọc Minh Châu", "Ca Tối (18:00 - 21:00)", "22/10/2026", "Đã phân công"}
        };

        tableModel = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Lịch làm việc");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
                txtShiftId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
                txtShiftId.setEnabled(false);
                txtStaffName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
                cbShiftTime.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 2));
                txtDate.setText((String) tableModel.getValueAt(selectedRowIndex, 3));
                isEditing = true;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        centerContainer.add(scroll, BorderLayout.CENTER);

        centerContainer.add(createFormPanel(), BorderLayout.EAST);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
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

        JLabel title = new JLabel("Phân Ca Xếp Lịch Trực");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        txtShiftId = addField(panel, "Mã Phân Ca:");
        txtStaffName = addField(panel, "Tên Nhân Viên:");

        JLabel lblShift = createFieldLabel("Chọn Ca Trực:");
        panel.add(lblShift);
        panel.add(Box.createVerticalStrut(3));
        cbShiftTime = new JComboBox<>(new String[]{"Ca Sáng (08:00 - 12:00)", "Ca Chiều (13:30 - 17:30)", "Ca Tối (18:00 - 21:00)"});
        cbShiftTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbShiftTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbShiftTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbShiftTime.setBackground(Color.WHITE);
        panel.add(cbShiftTime);
        panel.add(Box.createVerticalStrut(10));

        txtDate = addDateField(panel, "Ngày Trực (dd/MM/yyyy):");

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

    private datepicker.DatePickerField addDateField(JPanel p, String labelText) {
        p.add(createFieldLabel(labelText));
        p.add(Box.createVerticalStrut(3));
        datepicker.DatePickerField field = new datepicker.DatePickerField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(10));
        return field;
    }
    private void performSave() {
        String id = txtShiftId.getText().trim();
        String name = txtStaffName.getText().trim();
        String shift = (String) cbShiftTime.getSelectedItem();
        String date = txtDate.getText().trim();

        if (id.isEmpty() || name.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin phân ca!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!txtDate.isValidDate()) {
            JOptionPane.showMessageDialog(this, "Ngày trực không hợp lệ. Vui lòng chọn ngày theo định dạng dd/MM/yyyy!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isEditing) {
            tableModel.addRow(new Object[]{id, name, shift, date, "Đã phân công"});
            JOptionPane.showMessageDialog(this, "Phân ca thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt(name, selectedRowIndex, 1);
            tableModel.setValueAt(shift, selectedRowIndex, 2);
            tableModel.setValueAt(date, selectedRowIndex, 3);
            JOptionPane.showMessageDialog(this, "Cập nhật ca trực thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        AuditLog.event(isEditing ? "UPDATE" : "CREATE", "Phân ca - mã " + id);
        clearForm();
    }

    private void performDelete() {
        int r = table.getSelectedRow();
        if (r != -1) {
            int modelRow = table.convertRowIndexToModel(r);
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa ca trực này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
                tableModel.removeRow(modelRow);
                AuditLog.event("DELETE", "Phân ca - mã " + id);
                clearForm();
                JOptionPane.showMessageDialog(this, "Đã xóa ca trực!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtShiftId.setText("");
        txtShiftId.setEnabled(true);
        txtStaffName.setText("");
        cbShiftTime.setSelectedIndex(0);
        txtDate.setText("");
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
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git
