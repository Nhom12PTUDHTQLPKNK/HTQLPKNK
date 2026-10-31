package views;

import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ActivityLogPanel extends JPanel {

    public static final class LogEntry {
        private final LocalDateTime time;
        private final String user, action, detail;
        public LogEntry(LocalDateTime time, String user, String action, String detail) {
            this.time = time;
            this.user = user == null ? "" : user;
            this.action = action == null ? "" : action;
            this.detail = detail == null ? "" : detail;
        }
        public LocalDateTime getTime() { return time; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getDetail() { return detail; }
    }

    public interface LogSource {
        List<LogEntry> load();
    }

    private static final String[] COLS = {"Thời gian", "Người dùng", "Thao tác", "Chi tiết"};
    private static final String ALL = "Tất cả";

    private final LogSource source;
    private List<LogEntry> all = new ArrayList<LogEntry>();

    private final DefaultTableModel model = UiKit.readOnlyModel(COLS, String.class, String.class, String.class, String.class);
    private final JTable table = new JTable(model);
    private final JComboBox<String> cbUser = new JComboBox<String>();
    private final JComboBox<String> cbAction = new JComboBox<String>();
    private final JTextField txtFrom = UiKit.textField();
    private final JTextField txtTo = UiKit.textField();
    private final JTextField txtKeyword = UiKit.textField();
    private final JLabel lblCount = new JLabel(" ");
    private boolean updatingCombos = false;

    public ActivityLogPanel() { this(new DemoSource()); }

    public ActivityLogPanel(LogSource source) {
        this.source = source;
        setLayout(new BorderLayout());
        setBackground(UiKit.BG_CANVAS);
        JPanel root = UiKit.pageRoot();
        add(root, BorderLayout.CENTER);

        JPanel tb = UiKit.toolbar();
        JButton btnRefresh = UiKit.button("Tải lại", UiKit.BLUE_PRIMARY, 90);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { reloadSource(); }
        });
        tb.add(btnRefresh);
        root.add(UiKit.pageHeader("Nhật Ký Hoạt Động",
                "Theo dõi các thao tác do hệ thống ghi nhận", tb), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildFilterCard(), BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        UiKit.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(600);
        table.getColumnModel().getColumn(2).setCellRenderer(actionRenderer());
        PdfTableExport.install(table, "Nhật ký hoạt động");

        JPanel tp = new JPanel(new BorderLayout(0, 6));
        tp.setOpaque(false);
        tp.add(UiKit.scroll(table), BorderLayout.CENTER);
        lblCount.setFont(UiKit.font(Font.PLAIN, 12));
        lblCount.setForeground(UiKit.MUTED);
        tp.add(lblCount, BorderLayout.SOUTH);
        body.add(tp, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        reloadSource();
    }

    private JPanel buildFilterCard() {
        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        UiKit.styleCombo(cbUser);
        UiKit.styleCombo(cbAction);
        JPanel grid = new JPanel(new GridLayout(2, 5, 12, 2));
        grid.setOpaque(false);
        grid.add(UiKit.fieldLabel("Người dùng"));
        grid.add(UiKit.fieldLabel("Thao tác"));
        grid.add(UiKit.fieldLabel("Từ ngày (dd/MM/yyyy)"));
        grid.add(UiKit.fieldLabel("Đến ngày (dd/MM/yyyy)"));
        grid.add(UiKit.fieldLabel("Từ khóa trong chi tiết"));
        grid.add(cbUser);
        grid.add(cbAction);
        grid.add(txtFrom);
        grid.add(txtTo);
        grid.add(txtKeyword);
        card.add(grid, BorderLayout.CENTER);

        JButton btnFilter = UiKit.button("Lọc", UiKit.BLUE_PRIMARY, 80);
        JButton btnReset = UiKit.button("Đặt lại", UiKit.PINK_ACCENT, 90);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnReset);
        buttons.add(btnFilter);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(8, 0, 0, 0));
        south.add(buttons, BorderLayout.EAST);
        card.add(south, BorderLayout.SOUTH);

        java.awt.event.ActionListener doFilter = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (!updatingCombos) applyFilter(true); }
        };
        btnFilter.addActionListener(doFilter);
        cbUser.addActionListener(doFilter);
        cbAction.addActionListener(doFilter);
        txtKeyword.addActionListener(doFilter);
        txtFrom.addActionListener(doFilter);
        txtTo.addActionListener(doFilter);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                txtFrom.setText(""); txtTo.setText(""); txtKeyword.setText("");
                updatingCombos = true;
                cbUser.setSelectedIndex(0);
                cbAction.setSelectedIndex(0);
                updatingCombos = false;
                applyFilter(true);
            }
        });
        return card;
    }

    public void reloadSource() {
        List<LogEntry> list;
        try {
            list = source.load();
        } catch (RuntimeException ex) {
            UiKit.warn(this, "Không đọc được nhật ký: " + ex.getMessage());
            return;
        }
        all = list == null ? new ArrayList<LogEntry>() : new ArrayList<LogEntry>(list);
        Collections.sort(all, new Comparator<LogEntry>() { // mới nhất lên đầu
            @Override public int compare(LogEntry a, LogEntry b) { return b.getTime().compareTo(a.getTime()); }
        });
        refillCombos();
        applyFilter(false);
    }

    private void refillCombos() {
        String keepUser = (String) cbUser.getSelectedItem();
        String keepAction = (String) cbAction.getSelectedItem();
        TreeSet<String> users = new TreeSet<String>(), actions = new TreeSet<String>();
        for (LogEntry e : all) { users.add(e.getUser()); actions.add(e.getAction()); }
        updatingCombos = true;
        try {
            cbUser.removeAllItems();
            cbAction.removeAllItems();
            cbUser.addItem(ALL);
            cbAction.addItem(ALL);
            for (String u : users) cbUser.addItem(u);
            for (String a : actions) cbAction.addItem(a);
            if (keepUser != null) cbUser.setSelectedItem(keepUser);
            if (keepAction != null) cbAction.setSelectedItem(keepAction);
        } finally {
            updatingCombos = false;
        }
    }

    private void applyFilter(boolean interactive) {
        LocalDate from, to;
        try {
            from = UiKit.parseDate(txtFrom.getText());
            to = UiKit.parseDate(txtTo.getText());
        } catch (DateTimeParseException ex) {
            if (interactive) UiKit.warn(this, "Ngày phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (from != null && to != null && to.isBefore(from)) {
            if (interactive) UiKit.warn(this, "\"Đến ngày\" phải sau hoặc bằng \"Từ ngày\".");
            return;
        }
        String user = (String) cbUser.getSelectedItem();
        String action = (String) cbAction.getSelectedItem();
        String kw = txtKeyword.getText().trim().toLowerCase();

        model.setRowCount(0);
        int shown = 0;
        for (LogEntry e : all) {
            LocalDate d = e.getTime().toLocalDate();
            if (from != null && d.isBefore(from)) continue;
            if (to != null && d.isAfter(to)) continue;
            if (user != null && !ALL.equals(user) && !e.getUser().equals(user)) continue;
            if (action != null && !ALL.equals(action) && !e.getAction().equals(action)) continue;
            if (!kw.isEmpty() && !e.getDetail().toLowerCase().contains(kw)) continue;
            model.addRow(new Object[]{e.getTime().format(UiKit.DATE_TIME_FMT), e.getUser(), e.getAction(), e.getDetail()});
            shown++;
        }
        lblCount.setText("Hiển thị " + shown + " / " + all.size() + " bản ghi (mới nhất ở trên).");
    }

    private static UiKit.ColorTextRenderer actionRenderer() {
        Map<String, Color> m = new HashMap<String, Color>();
        m.put("CREATE", UiKit.GREEN_ACCENT);
        m.put("UPDATE", UiKit.BLUE_PRIMARY);
        m.put("CONFIRM", UiKit.GREEN_ACCENT);
        m.put("DELETE", UiKit.RED_ACCENT);
        m.put("CANCEL", UiKit.ORANGE_ACCENT);
        return new UiKit.ColorTextRenderer(m);
    }

    private static final class DemoSource implements LogSource {
        @Override public List<LogEntry> load() {
            LocalDateTime n = LocalDateTime.now();
            List<LogEntry> l = new ArrayList<LogEntry>();
            l.add(new LogEntry(n.minusMinutes(5), "luan", "CREATE", "Tạo giao dịch kho PN0017 (Nhập kho) - TH001, SL 100"));
            l.add(new LogEntry(n.minusMinutes(2), "luan", "CONFIRM", "Xác nhận giao dịch kho PN0017"));
            l.add(new LogEntry(n.minusHours(3), "admin", "UPDATE", "Cập nhật ca #12 - BS. Lê Thu Hà"));
            l.add(new LogEntry(n.minusDays(1), "admin", "DELETE", "Xóa ca #9 - Phạm Thị Lan"));
            l.add(new LogEntry(n.minusDays(2), "letan01", "CREATE", "Đặt lịch hẹn cho bệnh nhân BN005"));
            l.add(new LogEntry(n.minusDays(4), "luan", "CANCEL", "Hủy giao dịch kho PX0008"));
            return l;
        }
    }
}
