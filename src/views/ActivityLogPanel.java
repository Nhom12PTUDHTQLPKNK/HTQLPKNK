package views;

<<<<<<< HEAD
<<<<<<< HEAD
import Audit.AuditLog;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;

public class ActivityLogPanel extends JPanel {
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color GRAY_BTN = new Color(108, 117, 125);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);

    private JTextField txtSearch;
    private JTable actionTable, sessionTable;
    private DefaultTableModel actionTableModel, sessionTableModel;
    private TableRowSorter<DefaultTableModel> actionRowSorter, sessionRowSorter;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // =========================================================================
        // 1. HEADER & THANH CÔNG CỤ (TÌM KIẾM & LỌC)
        // =========================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Nhật Ký Hoạt Động");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Theo dõi chi tiết lịch sử thao tác nghiệp vụ và phiên đăng nhập của nhân sự");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // Thanh công cụ tìm kiếm bên phải
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Từ khóa:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("🔍 Tìm", BLUE);
        btnSearch.setPreferredSize(new Dimension(80, 36));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = createStaticButton("🔄 Làm mới", GRAY_BTN);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.addActionListener(e -> resetFilter());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(Box.createHorizontalStrut(5));
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // =========================================================================
        // 2. CÁC TAB NỘI DUNG NHẬT KÝ
        // =========================================================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("Nhật ký thao tác nghiệp vụ", createActionLogTab());
        tabbedPane.addTab("Phiên đăng nhập & Thời gian online", createSessionLogTab());

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(5, 0, 0, 0));
        centerWrap.add(tabbedPane, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);
        refreshTables();
    }

    /**
     * Tab 1: Nhật ký thao tác nghiệp vụ (Thêm, Sửa, Xóa, Lập hóa đơn, Đặt lịch...)
     */
    private JPanel createActionLogTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Thời gian", "Tài khoản", "Vai trò", "Hành động", "Chi tiết thực tế"};
        actionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        actionTable = new JTable(actionTableModel);
        PdfTableExport.install(actionTable, "Nhật ký hoạt động");
        actionRowSorter = new TableRowSorter<>(actionTableModel);
        actionTable.setRowSorter(actionRowSorter);

        styleTable(actionTable);

        // Căn giữa cột thời gian
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        actionTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(actionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab 2: Quản lý phiên đăng nhập và thời gian online
     */
    private JPanel createSessionLogTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Tài khoản", "Vai trò", "Thời điểm đăng nhập", "Thời điểm đăng xuất", "Thời gian online", "Trạng thái"};
        sessionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sessionTable = new JTable(sessionTableModel);
        PdfTableExport.install(sessionTable, "Nhật ký phiên đăng nhập");
        sessionRowSorter = new TableRowSorter<>(sessionTableModel);
        sessionTable.setRowSorter(sessionRowSorter);

        styleTable(sessionTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        sessionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        sessionTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        sessionTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Màu sắc trạng thái phiên
        sessionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                String status = (String) value;
                if (status != null && status.contains("Đang online")) {
                    c.setForeground(GREEN_ACCENT);
                } else {
                    c.setForeground(MUTED);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(sessionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        RowFilter<DefaultTableModel, Object> rf = null;
        if (!keyword.isEmpty()) {
            try {
                // Tìm kiếm không phân biệt hoa thường trên toàn bộ các cột
                rf = RowFilter.regexFilter("(?i)" + keyword);
            } catch (java.util.regex.PatternSyntaxException e) {
                rf = null;
            }
        }
        if (actionRowSorter != null) actionRowSorter.setRowFilter(rf);
        if (sessionRowSorter != null) sessionRowSorter.setRowFilter(rf);
    }

    /**
     * Xóa bộ lọc và làm mới lại dữ liệu hiển thị
     */
    private void resetFilter() {
        txtSearch.setText("");
        if (actionRowSorter != null) actionRowSorter.setRowFilter(null);
        if (sessionRowSorter != null) sessionRowSorter.setRowFilter(null);
        refreshTables();
    }

    public void refreshTables() {
        if (actionTableModel == null || sessionTableModel == null) return;
        actionTableModel.setRowCount(0);
        for (Object[] row : AuditLog.eventRows(LocalDate.MIN, LocalDate.MAX)) {
            actionTableModel.addRow(row);
        }
        sessionTableModel.setRowCount(0);
        for (Object[] row : AuditLog.sessionRows(LocalDate.MIN, LocalDate.MAX)) {
            sessionTableModel.addRow(row);
        }
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

    private JButton createStaticButton(String text, Color bg) {
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
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
=======
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
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git
=======
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
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git
    }
}
