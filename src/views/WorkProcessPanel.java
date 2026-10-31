package views;

import Audit.AuditLog;
import report.PdfTableExport;
<<<<<<< HEAD
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class WorkProcessPanel extends JPanel {
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
    private JTextField txtSearch, txtShiftId, txtStaffName, txtDate;
    private JComboBox<String> cbShiftTime;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public WorkProcessPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Phân Ca Lịch Làm Việc");
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

        txtDate = addField(panel, "Ngày Trực (dd/MM/yyyy):");

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
        String id = txtShiftId.getText().trim();
        String name = txtStaffName.getText().trim();
        String shift = (String) cbShiftTime.getSelectedItem();
        String date = txtDate.getText().trim();

        if (id.isEmpty() || name.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin phân ca!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
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
=======
import schedule.ShiftStore;
import schedule.ShiftStore.Preset;
import schedule.ShiftStore.Role;
import schedule.ShiftStore.Shift;
import schedule.ShiftStore.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Phân ca làm việc cho nhân viên (nha sĩ, phụ tá, lễ tân).
 *  - Chặn ca trùng của cùng một nhân viên trong cùng ngày.
 *  - Mục "Kiểm tra lịch phù hợp" dùng cùng logic mà chức năng đặt lịch hẹn gọi qua {@link ShiftStore}.
 */
public class WorkProcessPanel extends JPanel {

    private static final String[] COLS = {"Mã ca", "Ngày", "Thứ", "Nhân viên", "Chức vụ", "Bắt đầu", "Kết thúc", "Số giờ", "Ghi chú"};
    private static final Class<?>[] TYPES = {Integer.class, String.class, String.class, String.class, String.class,
            String.class, String.class, String.class, String.class};
    private static final String ALL = "Tất cả";

    private final ShiftStore store = ShiftStore.getInstance();
    private final DefaultTableModel model = UiKit.readOnlyModel(COLS, TYPES);
    private final JTable table = new JTable(model);

    // bộ lọc
    private final JTextField txtFilterFrom = UiKit.textField();
    private final JTextField txtFilterTo = UiKit.textField();
    private final JComboBox<String> cbFilterRole = new JComboBox<String>();
    private LocalDate filterFrom, filterTo;

    // form
    private final JComboBox<Staff> cbStaff = new JComboBox<Staff>();
    private final JTextField txtDate = UiKit.textField();
    private final JComboBox<Preset> cbPreset = new JComboBox<Preset>(Preset.values());
    private final JTextField txtStart = UiKit.textField();
    private final JTextField txtEnd = UiKit.textField();
    private final JTextField txtNote = UiKit.textField();
    private int selectedShiftId = -1;
    private boolean loading = false;

    // kiểm tra lịch phù hợp
    private final JTextField txtQDate = UiKit.textField();
    private final JTextField txtQStart = UiKit.textField();
    private final JTextField txtQEnd = UiKit.textField();
    private final JTextArea txtResult = new JTextArea(4, 20);

    private final Runnable refreshListener = new Runnable() {
        @Override public void run() { UiKit.onEdt(new Runnable() { @Override public void run() { reload(); } }); }
    };

    public WorkProcessPanel() {
        setLayout(new BorderLayout());
        setBackground(UiKit.BG_CANVAS);
        JPanel root = UiKit.pageRoot();
        add(root, BorderLayout.CENTER);

        root.add(UiKit.pageHeader("Phân Ca Làm Việc",
                "Phân ca cho nha sĩ, phụ tá; kiểm tra trùng ca; cung cấp lịch làm việc cho đặt lịch hẹn", null), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);
        center.add(buildLeft(), BorderLayout.CENTER);
        center.add(buildForm(), BorderLayout.EAST);
        root.add(center, BorderLayout.CENTER);

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        txtFilterFrom.setText(UiKit.fmt(monday));
        txtFilterTo.setText(UiKit.fmt(monday.plusDays(6)));
        txtDate.setText(UiKit.fmt(LocalDate.now()));
        txtQDate.setText(UiKit.fmt(LocalDate.now()));
        txtQStart.setText("08:00");
        txtQEnd.setText("09:00");
        applyFilter(false);
    }

    // ------------------------------------------------------------------ bố cục
    private JPanel buildLeft() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);

        JPanel bar = UiKit.card();
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        txtFilterFrom.setPreferredSize(new Dimension(110, 34));
        txtFilterTo.setPreferredSize(new Dimension(110, 34));
        cbFilterRole.addItem(ALL);
        for (Role r : Role.values()) cbFilterRole.addItem(r.getLabel());
        UiKit.styleCombo(cbFilterRole);
        cbFilterRole.setPreferredSize(new Dimension(130, 34));
        JButton btnFilter = UiKit.button("Lọc", UiKit.BLUE_PRIMARY, 70);
        JButton btnWeek = UiKit.button("Tuần này", UiKit.GREEN_ACCENT, 115);
        bar.add(UiKit.fieldLabel("Từ ngày"));
        bar.add(txtFilterFrom);
        bar.add(UiKit.fieldLabel("Đến ngày"));
        bar.add(txtFilterTo);
        bar.add(UiKit.fieldLabel("Chức vụ"));
        bar.add(cbFilterRole);
        bar.add(btnFilter);
        bar.add(btnWeek);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { applyFilter(true); }
        });
        cbFilterRole.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { applyFilter(true); }
        });
        btnWeek.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                LocalDate mon = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                txtFilterFrom.setText(UiKit.fmt(mon));
                txtFilterTo.setText(UiKit.fmt(mon.plusDays(6)));
                applyFilter(true);
            }
        });
        left.add(bar, BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        UiKit.styleTable(table);
        UiKit.setColumnWidths(table, 60, 95, 70, 190, 90, 80, 80, 70, 200);
        PdfTableExport.install(table, "Lịch phân ca");
        table.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) loadSelectedToForm();
            }
        });
        left.add(UiKit.scroll(table), BorderLayout.CENTER);
        return left;
    }

    private JPanel buildForm() {
        JPanel p = UiKit.card();
        p.setPreferredSize(new Dimension(350, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Thông tin ca làm việc");
        title.setFont(UiKit.font(Font.BOLD, 15));
        title.setForeground(UiKit.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));

        for (Staff s : store.staff()) cbStaff.addItem(s);
        UiKit.styleCombo(cbStaff);
        UiKit.styleCombo(cbPreset);
        row(p, "Nhân viên:", cbStaff);
        row(p, "Ngày làm việc (dd/MM/yyyy):", txtDate);
        row(p, "Loại ca:", cbPreset);
        JPanel times = new JPanel(new GridLayout(2, 2, 8, 2));
        times.setOpaque(false);
        times.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        times.setAlignmentX(Component.LEFT_ALIGNMENT);
        times.add(UiKit.fieldLabel("Bắt đầu (HH:mm):"));
        times.add(UiKit.fieldLabel("Kết thúc (HH:mm):"));
        times.add(txtStart);
        times.add(txtEnd);
        p.add(times);
        p.add(Box.createVerticalStrut(8));
        row(p, "Ghi chú:", txtNote);

        JButton btnAdd = UiKit.button("Thêm ca", UiKit.BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnUpdate = UiKit.button("Cập nhật", UiKit.GREEN_ACCENT, Integer.MAX_VALUE);
        JButton btnDelete = UiKit.button("Xóa ca", UiKit.RED_ACCENT, Integer.MAX_VALUE);
        JButton btnClear = UiKit.button("Làm mới", UiKit.PINK_ACCENT, Integer.MAX_VALUE);
        p.add(UiKit.buttonRow(btnAdd, btnUpdate));
        p.add(Box.createVerticalStrut(8));
        p.add(UiKit.buttonRow(btnDelete, btnClear));
        p.add(Box.createVerticalStrut(16));

        // kiểm tra lịch phù hợp
        JLabel t2 = new JLabel("Kiểm tra lịch phù hợp (cho đặt lịch hẹn)");
        t2.setFont(UiKit.font(Font.BOLD, 13));
        t2.setForeground(UiKit.TEXT_DARK);
        t2.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t2);
        p.add(Box.createVerticalStrut(6));
        JPanel q = new JPanel(new GridLayout(2, 3, 6, 2));
        q.setOpaque(false);
        q.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        q.setAlignmentX(Component.LEFT_ALIGNMENT);
        q.add(UiKit.fieldLabel("Ngày"));
        q.add(UiKit.fieldLabel("Từ giờ"));
        q.add(UiKit.fieldLabel("Đến giờ"));
        q.add(txtQDate);
        q.add(txtQStart);
        q.add(txtQEnd);
        p.add(q);
        p.add(Box.createVerticalStrut(6));
        JButton btnQuery = UiKit.button("Tìm nhân sự có ca", UiKit.ORANGE_ACCENT, Integer.MAX_VALUE);
        p.add(UiKit.buttonRow(btnQuery));
        p.add(Box.createVerticalStrut(6));
        txtResult.setEditable(false);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        txtResult.setFont(UiKit.font(Font.PLAIN, 12));
        txtResult.setForeground(UiKit.TEXT_DARK);
        JScrollPane sp = new JScrollPane(txtResult);
        sp.setBorder(BorderFactory.createLineBorder(UiKit.BORDER_LIGHT, 1));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sp);

        cbPreset.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (loading) return;
                Preset pr = (Preset) cbPreset.getSelectedItem();
                if (pr != null && pr.getStart() != null) {
                    txtStart.setText(pr.getStart().format(UiKit.TIME_FMT));
                    txtEnd.setText(pr.getEnd().format(UiKit.TIME_FMT));
                }
            }
        });
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { save(false); }
        });
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { save(true); }
        });
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { delete(); }
        });
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { clearForm(); }
        });
        btnQuery.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { queryAvailability(); }
        });
        cbPreset.setSelectedItem(Preset.SANG);
        return p;
    }

    private static void row(JPanel p, String label, JComponent c) {
        p.add(UiKit.fieldLabel(label));
        p.add(Box.createVerticalStrut(2));
        p.add(c);
        p.add(Box.createVerticalStrut(8));
    }

    // ------------------------------------------------------------------ hành động
    private void save(boolean update) {
        if (update && selectedShiftId < 0) { UiKit.warn(this, "Vui lòng chọn ca cần cập nhật trong bảng."); return; }
        Staff staff = (Staff) cbStaff.getSelectedItem();
        LocalDate date;
        LocalTime start, end;
        try {
            date = UiKit.parseDate(txtDate.getText());
        } catch (DateTimeParseException ex) {
            UiKit.warn(this, "Ngày làm việc phải đúng định dạng dd/MM/yyyy."); return;
        }
        try {
            start = UiKit.parseTime(txtStart.getText());
            end = UiKit.parseTime(txtEnd.getText());
        } catch (DateTimeParseException ex) {
            UiKit.warn(this, "Giờ phải đúng định dạng HH:mm (ví dụ 07:30)."); return;
        }
        try {
            String sid = staff == null ? null : staff.getId();
            Shift s;
            if (update) {
                s = store.updateShift(selectedShiftId, sid, date, start, end, txtNote.getText());
                AuditLog.event("UPDATE", "Cập nhật ca #" + s.getId() + " - " + staff.getName() + " " + UiKit.fmt(date) + " " + s.timeRange());
            } else {
                s = store.addShift(sid, date, start, end, txtNote.getText());
                AuditLog.event("CREATE", "Phân ca #" + s.getId() + " - " + staff.getName() + " " + UiKit.fmt(date) + " " + s.timeRange());
            }
            UiKit.info(this, update ? "Đã cập nhật ca làm việc." : "Đã thêm ca làm việc.");
            // mở rộng bộ lọc nếu ca vừa lưu nằm ngoài khoảng đang xem
            if (date != null && (date.isBefore(filterFrom) || date.isAfter(filterTo))) {
                txtFilterFrom.setText(UiKit.fmt(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))));
                txtFilterTo.setText(UiKit.fmt(date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))));
                applyFilter(false);
            }
            clearForm();
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        }
    }

    private void delete() {
        if (selectedShiftId < 0) { UiKit.warn(this, "Vui lòng chọn ca cần xóa trong bảng."); return; }
        Shift s = store.shift(selectedShiftId);
        if (s == null) return;
        Staff st = store.staffById(s.getStaffId());
        if (!UiKit.confirm(this, "Xóa ca " + s.timeRange() + " ngày " + UiKit.fmt(s.getDate())
                + (st == null ? "" : " của " + st.getName()) + "?")) return;
        try {
            store.deleteShift(selectedShiftId);
            AuditLog.event("DELETE", "Xóa ca #" + s.getId() + (st == null ? "" : " - " + st.getName()) + " " + UiKit.fmt(s.getDate()) + " " + s.timeRange());
            clearForm();
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        }
    }

    private void queryAvailability() {
        LocalDate d;
        LocalTime s, e;
        try {
            d = UiKit.parseDate(txtQDate.getText());
            s = UiKit.parseTime(txtQStart.getText());
            e = UiKit.parseTime(txtQEnd.getText());
        } catch (DateTimeParseException ex) {
            UiKit.warn(this, "Ngày dd/MM/yyyy và giờ HH:mm không hợp lệ."); return;
        }
        if (d == null || s == null || e == null || !e.isAfter(s)) {
            UiKit.warn(this, "Vui lòng nhập ngày và khung giờ hợp lệ (giờ kết thúc sau giờ bắt đầu)."); return;
        }
        StringBuilder sb = new StringBuilder();
        for (Role r : new Role[]{Role.NHA_SI, Role.PHU_TA}) {
            List<Staff> list = store.availableStaff(r, d, s, e);
            sb.append(r.getLabel()).append(": ");
            if (list.isEmpty()) sb.append("(không có ca phù hợp)");
            for (int i = 0; i < list.size(); i++) { if (i > 0) sb.append(", "); sb.append(list.get(i).getName()); }
            sb.append('\n');
        }
        txtResult.setText(sb.toString());
    }

    // ------------------------------------------------------------------ bảng
    private void applyFilter(boolean interactive) {
        try {
            LocalDate f = UiKit.parseDate(txtFilterFrom.getText());
            LocalDate t = UiKit.parseDate(txtFilterTo.getText());
            if (f == null || t == null || t.isBefore(f)) {
                if (interactive) UiKit.warn(this, "Khoảng ngày không hợp lệ: \"Đến ngày\" phải sau hoặc bằng \"Từ ngày\".");
                return;
            }
            filterFrom = f;
            filterTo = t;
        } catch (DateTimeParseException ex) {
            if (interactive) UiKit.warn(this, "Ngày phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        reload();
    }

    private Role filterRole() {
        int i = cbFilterRole.getSelectedIndex();
        return i <= 0 ? null : Role.values()[i - 1];
    }

    private void reload() {
        if (filterFrom == null) return;
        int keep = selectedShiftId;
        model.setRowCount(0);
        for (Shift s : store.shifts(filterFrom, filterTo, filterRole(), null)) {
            Staff st = store.staffById(s.getStaffId());
            model.addRow(new Object[]{Integer.valueOf(s.getId()), UiKit.fmt(s.getDate()), dayName(s.getDate().getDayOfWeek()),
                    st == null ? s.getStaffId() : st.getName(), st == null ? "" : st.getRole().getLabel(),
                    s.getStart().format(UiKit.TIME_FMT), s.getEnd().format(UiKit.TIME_FMT),
                    String.format("%.1f", s.getHours()), s.getNote()});
        }
        if (keep >= 0) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (((Integer) model.getValueAt(i, 0)).intValue() == keep) {
                    int v = table.convertRowIndexToView(i);
                    if (v >= 0) table.setRowSelectionInterval(v, v);
                    break;
                }
            }
        }
    }

    private void loadSelectedToForm() {
        int r = table.getSelectedRow();
        if (r == -1) return;
        int id = ((Integer) model.getValueAt(table.convertRowIndexToModel(r), 0)).intValue();
        Shift s = store.shift(id);
        if (s == null) return;
        loading = true;
        try {
            selectedShiftId = s.getId();
            for (int i = 0; i < cbStaff.getItemCount(); i++)
                if (cbStaff.getItemAt(i).getId().equals(s.getStaffId())) { cbStaff.setSelectedIndex(i); break; }
            txtDate.setText(UiKit.fmt(s.getDate()));
            txtStart.setText(s.getStart().format(UiKit.TIME_FMT));
            txtEnd.setText(s.getEnd().format(UiKit.TIME_FMT));
            cbPreset.setSelectedItem(Preset.of(s.getStart(), s.getEnd()));
            txtNote.setText(s.getNote());
        } finally {
            loading = false;
        }
    }

    private void clearForm() {
        selectedShiftId = -1;
        table.clearSelection();
        txtNote.setText("");
        txtDate.setText(UiKit.fmt(LocalDate.now()));
        cbPreset.setSelectedItem(Preset.SANG);
        Preset p = Preset.SANG;
        txtStart.setText(p.getStart().format(UiKit.TIME_FMT));
        txtEnd.setText(p.getEnd().format(UiKit.TIME_FMT));
    }

    private static String dayName(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            default: return "Chủ nhật";
        }
    }

    @Override public void addNotify() { super.addNotify(); store.addListener(refreshListener); reload(); }
    @Override public void removeNotify() { store.removeListener(refreshListener); super.removeNotify(); }
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git
}
