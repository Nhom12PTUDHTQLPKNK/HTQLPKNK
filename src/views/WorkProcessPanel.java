package views;

import Audit.AuditLog;
import report.PdfTableExport;
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
}
