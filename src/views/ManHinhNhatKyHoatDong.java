package views;

import controllers.NhatKyKiemToan;
import controllers.XuatBangPDF;

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
import views.BoCongCuGiaoDien;

public class ManHinhNhatKyHoatDong extends JPanel {

    public static final class BanGhiNhatKy {
        private final LocalDateTime time;
        private final String user, action, detail;
        public BanGhiNhatKy(LocalDateTime time, String user, String action, String detail) {
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

    public interface NguonNhatKy {
        List<BanGhiNhatKy> load();
    }

    private static final String[] COLS = {"Thời gian", "Người dùng", "Thao tác", "Chi tiết"};
    private static final String ALL = "Tất cả";

    private final NguonNhatKy source;
    private List<BanGhiNhatKy> all = new ArrayList<BanGhiNhatKy>();

    private final DefaultTableModel model = BoCongCuGiaoDien.readOnlyModel(COLS, String.class, String.class, String.class, String.class);
    private final JTable table = new JTable(model);
    private final JComboBox<String> cbUser = new JComboBox<String>();
    private final JComboBox<String> cbAction = new JComboBox<String>();
    private final JTextField txtFrom = BoCongCuGiaoDien.textField();
    private final JTextField txtTo = BoCongCuGiaoDien.textField();
    private final JTextField txtKeyword = BoCongCuGiaoDien.textField();
    private final JLabel lblCount = new JLabel(" ");
    private boolean updatingCombos = false;

    /** Màn hình mặc định luôn đọc nhật ký đã lưu trong dbo.NhatKyKiemToan. */
    public ManHinhNhatKyHoatDong() { this(new NguonNhatKySQL()); }

    public ManHinhNhatKyHoatDong(NguonNhatKy source) {
        this.source = source;
        setLayout(new BorderLayout());
        setBackground(BoCongCuGiaoDien.BG_CANVAS);
        JPanel root = BoCongCuGiaoDien.pageRoot();
        add(root, BorderLayout.CENTER);

        JPanel tb = BoCongCuGiaoDien.toolbar();
        JButton btnRefresh = BoCongCuGiaoDien.button("Tải lại", BoCongCuGiaoDien.BLUE_PRIMARY, 90);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { reloadSource(); }
        });
        tb.add(btnRefresh);
        root.add(BoCongCuGiaoDien.pageHeader("Nhật Ký Hoạt Động",
                "Theo dõi các thao tác do hệ thống ghi nhận", tb), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildFilterCard(), BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        BoCongCuGiaoDien.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(600);
        table.getColumnModel().getColumn(2).setCellRenderer(actionRenderer());
        XuatBangPDF.install(table, "Nhật ký hoạt động");

        JPanel tp = new JPanel(new BorderLayout(0, 6));
        tp.setOpaque(false);
        tp.add(BoCongCuGiaoDien.scroll(table), BorderLayout.CENTER);
        lblCount.setFont(BoCongCuGiaoDien.font(Font.PLAIN, 12));
        lblCount.setForeground(BoCongCuGiaoDien.MUTED);
        tp.add(lblCount, BorderLayout.SOUTH);
        body.add(tp, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        reloadSource();
    }

    private JPanel buildFilterCard() {
        JPanel card = BoCongCuGiaoDien.card();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        BoCongCuGiaoDien.styleCombo(cbUser);
        BoCongCuGiaoDien.styleCombo(cbAction);
        JPanel grid = new JPanel(new GridLayout(2, 5, 12, 2));
        grid.setOpaque(false);
        grid.add(BoCongCuGiaoDien.fieldLabel("Người dùng"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Thao tác"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Từ ngày (dd/MM/yyyy)"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Đến ngày (dd/MM/yyyy)"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Từ khóa trong chi tiết"));
        grid.add(cbUser);
        grid.add(cbAction);
        grid.add(txtFrom);
        grid.add(txtTo);
        grid.add(txtKeyword);
        card.add(grid, BorderLayout.CENTER);

        JButton btnFilter = BoCongCuGiaoDien.button("Lọc", BoCongCuGiaoDien.BLUE_PRIMARY, 80);
        JButton btnReset = BoCongCuGiaoDien.button("Đặt lại", BoCongCuGiaoDien.PINK_ACCENT, 90);
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
        List<BanGhiNhatKy> list;
        try {
            list = source.load();
        } catch (RuntimeException ex) {
            BoCongCuGiaoDien.warn(this, "Không đọc được nhật ký: " + ex.getMessage());
            return;
        }
        all = list == null ? new ArrayList<BanGhiNhatKy>() : new ArrayList<BanGhiNhatKy>(list);
        Collections.sort(all, new Comparator<BanGhiNhatKy>() { // mới nhất lên đầu
            @Override public int compare(BanGhiNhatKy a, BanGhiNhatKy b) { return b.getTime().compareTo(a.getTime()); }
        });
        refillCombos();
        applyFilter(false);
    }

    private void refillCombos() {
        String keepUser = (String) cbUser.getSelectedItem();
        String keepAction = (String) cbAction.getSelectedItem();
        TreeSet<String> users = new TreeSet<String>(), actions = new TreeSet<String>();
        for (BanGhiNhatKy e : all) { users.add(e.getUser()); actions.add(e.getAction()); }
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
            from = BoCongCuGiaoDien.parseDate(txtFrom.getText());
            to = BoCongCuGiaoDien.parseDate(txtTo.getText());
        } catch (DateTimeParseException ex) {
            if (interactive) BoCongCuGiaoDien.warn(this, "Ngày phải đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (from != null && to != null && to.isBefore(from)) {
            if (interactive) BoCongCuGiaoDien.warn(this, "\"Đến ngày\" phải sau hoặc bằng \"Từ ngày\".");
            return;
        }
        String user = (String) cbUser.getSelectedItem();
        String action = (String) cbAction.getSelectedItem();
        String kw = txtKeyword.getText().trim().toLowerCase();

        model.setRowCount(0);
        int shown = 0;
        for (BanGhiNhatKy e : all) {
            LocalDate d = e.getTime().toLocalDate();
            if (from != null && d.isBefore(from)) continue;
            if (to != null && d.isAfter(to)) continue;
            if (user != null && !ALL.equals(user) && !e.getUser().equals(user)) continue;
            if (action != null && !ALL.equals(action) && !e.getAction().equals(action)) continue;
            if (!kw.isEmpty() && !e.getDetail().toLowerCase().contains(kw)) continue;
            model.addRow(new Object[]{e.getTime().format(BoCongCuGiaoDien.DATE_TIME_FMT), e.getUser(), e.getAction(), e.getDetail()});
            shown++;
        }
        lblCount.setText("Hiển thị " + shown + " / " + all.size() + " bản ghi (mới nhất ở trên).");
    }

    private static BoCongCuGiaoDien.BoVeChuMau actionRenderer() {
        Map<String, Color> m = new HashMap<String, Color>();
        m.put("CREATE", BoCongCuGiaoDien.GREEN_ACCENT);
        m.put("UPDATE", BoCongCuGiaoDien.BLUE_PRIMARY);
        m.put("CONFIRM", BoCongCuGiaoDien.GREEN_ACCENT);
        m.put("DELETE", BoCongCuGiaoDien.RED_ACCENT);
        m.put("CANCEL", BoCongCuGiaoDien.ORANGE_ACCENT);
        return new BoCongCuGiaoDien.BoVeChuMau(m);
    }

    private static final class NguonNhatKySQL implements NguonNhatKy {
        @Override public List<BanGhiNhatKy> load() {
            List<BanGhiNhatKy> l = new ArrayList<BanGhiNhatKy>();
            for (NhatKyKiemToan.MucNhatKy entry : NhatKyKiemToan.all()) {
                l.add(new BanGhiNhatKy(entry.time, entry.username, entry.action, entry.detail));
            }
            return l;
        }

    }
}
