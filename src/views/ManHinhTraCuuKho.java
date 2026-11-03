package views;

import controllers.BoNhoKho;
import controllers.BoNhoKho.TongHopCanhBao;
import controllers.BoNhoKho.DanhMucKho;
import controllers.BoNhoKho.DongTonKho;
import controllers.BoNhoKho.TrangThaiTonKho;
import controllers.XuatBangPDF;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManHinhTraCuuKho extends JPanel {

    private static final String[] COLS = {"Mã hàng", "Tên hàng", "Loại", "Lô", "Hạn sử dụng", "SL tồn (lô)",
            "Tổng tồn", "ĐVT", "Tồn tối thiểu", "Tình trạng"};
    private static final Class<?>[] TYPES = {String.class, String.class, String.class, String.class, String.class,
            Integer.class, Integer.class, String.class, Integer.class, String.class};
    private static final int COL_STATUS = 9;
    private static final String ALL = "Tất cả";

    private final BoNhoKho store = BoNhoKho.getInstance();
    private final DefaultTableModel model = BoCongCuGiaoDien.readOnlyModel(COLS, TYPES);
    private final JTable table = new JTable(model);

    private final JTextField txtKeyword = BoCongCuGiaoDien.textField();
    private final JTextField txtLot = BoCongCuGiaoDien.textField();
    private final JTextField txtExpiryTo = BoCongCuGiaoDien.textField();
    private final JComboBox<String> cbCategory = new JComboBox<String>();
    private final JComboBox<String> cbStatus = new JComboBox<String>();
    private final JLabel lblAlert = new JLabel(" ");
    private final JLabel lblCount = new JLabel(" ");

    private final Runnable refreshListener = new Runnable() {
        @Override public void run() { BoCongCuGiaoDien.onEdt(new Runnable() { @Override public void run() { search(false); } }); }
    };

    public ManHinhTraCuuKho() {
        setLayout(new BorderLayout());
        setBackground(BoCongCuGiaoDien.BG_CANVAS);
        JPanel root = BoCongCuGiaoDien.pageRoot();
        add(root, BorderLayout.CENTER);

        root.add(BoCongCuGiaoDien.pageHeader("Tra Cứu Tồn Kho",
                "Tra cứu số lượng tồn theo thuốc, vật tư, thiết bị; theo dõi lô và hạn sử dụng", null), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildFilterCard(), BorderLayout.NORTH);
        body.add(buildTable(), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        search(false);
    }

    private JPanel buildFilterCard() {
        JPanel card = BoCongCuGiaoDien.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        lblAlert.setFont(BoCongCuGiaoDien.font(Font.BOLD, 13));
        lblAlert.setBorder(new EmptyBorder(6, 10, 6, 10));
        lblAlert.setOpaque(true);
        card.add(lblAlert, BorderLayout.NORTH);

        cbCategory.addItem(ALL);
        for (DanhMucKho c : DanhMucKho.values()) cbCategory.addItem(c.getLabel());
        cbStatus.addItem(ALL);
        for (TrangThaiTonKho s : TrangThaiTonKho.values()) cbStatus.addItem(s.getLabel());
        BoCongCuGiaoDien.styleCombo(cbCategory);
        BoCongCuGiaoDien.styleCombo(cbStatus);
        txtKeyword.setToolTipText("Mã hoặc tên mặt hàng");
        txtExpiryTo.setToolTipText("Chỉ hiện lô có hạn sử dụng đến hết ngày này, định dạng dd/MM/yyyy");

        JPanel grid = new JPanel(new GridLayout(2, 5, 12, 2));
        grid.setOpaque(false);
        grid.add(BoCongCuGiaoDien.fieldLabel("Từ khóa (mã / tên)"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Loại hàng"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Lô"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Hạn sử dụng đến ngày"));
        grid.add(BoCongCuGiaoDien.fieldLabel("Tình trạng tồn"));
        grid.add(txtKeyword);
        grid.add(cbCategory);
        grid.add(txtLot);
        grid.add(txtExpiryTo);
        grid.add(cbStatus);

        JButton btnSearch = BoCongCuGiaoDien.button("Tìm kiếm", BoCongCuGiaoDien.BLUE_PRIMARY, 100);
        JButton btnReset = BoCongCuGiaoDien.button("Đặt lại", BoCongCuGiaoDien.PINK_ACCENT, 90);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnReset);
        buttons.add(btnSearch);

        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setOpaque(false);
        mid.add(grid, BorderLayout.CENTER);
        mid.add(buttons, BorderLayout.SOUTH);
        card.add(mid, BorderLayout.CENTER);

        java.awt.event.ActionListener doSearch = new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { search(true); }
        };
        btnSearch.addActionListener(doSearch);
        txtKeyword.addActionListener(doSearch);
        txtLot.addActionListener(doSearch);
        txtExpiryTo.addActionListener(doSearch);
        cbCategory.addActionListener(doSearch);
        cbStatus.addActionListener(doSearch);
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                txtKeyword.setText(""); txtLot.setText(""); txtExpiryTo.setText("");
                cbCategory.setSelectedIndex(0); cbStatus.setSelectedIndex(0);
                search(true);
            }
        });
        return card;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        table.setAutoCreateRowSorter(true);
        BoCongCuGiaoDien.styleTable(table);
        BoCongCuGiaoDien.setColumnWidths(table, 75, 190, 80, 90, 100, 90, 80, 80, 100, 110);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(statusRenderer());
        XuatBangPDF.install(table, "Tra cứu tồn kho");
        p.add(BoCongCuGiaoDien.scroll(table), BorderLayout.CENTER);
        lblCount.setFont(BoCongCuGiaoDien.font(Font.PLAIN, 12));
        lblCount.setForeground(BoCongCuGiaoDien.MUTED);
        p.add(lblCount, BorderLayout.SOUTH);
        return p;
    }
    
    private void search(boolean interactive) {
        LocalDate expiryTo = null;
        try {
            expiryTo = BoCongCuGiaoDien.parseDate(txtExpiryTo.getText());
        } catch (DateTimeParseException ex) {
            if (interactive) BoCongCuGiaoDien.warn(this, "Hạn sử dụng phải đúng định dạng dd/MM/yyyy (ví dụ 31/12/2026).");
            return;
        }
        String kw = txtKeyword.getText().trim().toLowerCase();
        String lot = txtLot.getText().trim().toUpperCase();
        String cat = (String) cbCategory.getSelectedItem();
        String st = (String) cbStatus.getSelectedItem();

        model.setRowCount(0);
        List<DongTonKho> rows = store.stock();
        int shown = 0;
        for (DongTonKho r : rows) {
            if (!kw.isEmpty() && !(r.getItem().getCode().toLowerCase().contains(kw)
                    || r.getItem().getName().toLowerCase().contains(kw))) continue;
            if (cat != null && !ALL.equals(cat) && !r.getItem().getCategory().getLabel().equals(cat)) continue;
            if (!lot.isEmpty() && !r.getLotNo().contains(lot)) continue;
            if (expiryTo != null && (r.getExpiry() == null || r.getExpiry().isAfter(expiryTo))) continue;
            if (st != null && !ALL.equals(st) && !r.getStatus().getLabel().equals(st)) continue;
            model.addRow(new Object[]{r.getItem().getCode(), r.getItem().getName(), r.getItem().getCategory().getLabel(),
                    r.getLotNo(), BoCongCuGiaoDien.fmt(r.getExpiry()), Integer.valueOf(r.getLotQty()), Integer.valueOf(r.getTotalQty()),
                    r.getItem().getUnit(), Integer.valueOf(r.getItem().getMinStock()), r.getStatus().getLabel()});
            shown++;
        }
        lblCount.setText("Hiển thị " + shown + " dòng (mỗi lô một dòng) trên tổng số " + rows.size() + " dòng tồn kho.");
        updateAlert();
    }

    private void updateAlert() {
        TongHopCanhBao a = store.alerts();
        if (a.isEmpty()) {
            lblAlert.setText("Không có cảnh báo: tồn kho và hạn sử dụng đều trong ngưỡng an toàn.");
            lblAlert.setBackground(new Color(232, 247, 237));
            lblAlert.setForeground(BoCongCuGiaoDien.GREEN_ACCENT);
            return;
        }
        StringBuilder sb = new StringBuilder("Cảnh báo: ");
        boolean first = true;
        first = append(sb, a.outOfStock, " mặt hàng hết hàng", first);
        first = append(sb, a.lowStock, " mặt hàng sắp hết (dưới mức tối thiểu)", first);
        first = append(sb, a.nearExpiry, " lô gần hết hạn (trong " + BoNhoKho.NEAR_EXPIRY_DAYS + " ngày)", first);
        append(sb, a.expired, " lô đã hết hạn", first);
        lblAlert.setText(sb.toString());
        boolean severe = a.outOfStock > 0 || a.expired > 0;
        lblAlert.setBackground(severe ? new Color(253, 236, 238) : new Color(255, 243, 224));
        lblAlert.setForeground(severe ? BoCongCuGiaoDien.RED_ACCENT : BoCongCuGiaoDien.ORANGE_ACCENT);
    }

    private static boolean append(StringBuilder sb, int n, String text, boolean first) {
        if (n <= 0) return first;
        if (!first) sb.append("  |  ");
        sb.append(n).append(text);
        return false;
    }

    private static BoCongCuGiaoDien.BoVeChuMau statusRenderer() {
        Map<String, Color> m = new HashMap<String, Color>();
        m.put(TrangThaiTonKho.BINH_THUONG.getLabel(), BoCongCuGiaoDien.GREEN_ACCENT);
        m.put(TrangThaiTonKho.SAP_HET.getLabel(), BoCongCuGiaoDien.ORANGE_ACCENT);
        m.put(TrangThaiTonKho.GAN_HET_HAN.getLabel(), BoCongCuGiaoDien.ORANGE_ACCENT);
        m.put(TrangThaiTonKho.HET_HANG.getLabel(), BoCongCuGiaoDien.RED_ACCENT);
        m.put(TrangThaiTonKho.HET_HAN.getLabel(), BoCongCuGiaoDien.RED_ACCENT);
        return new BoCongCuGiaoDien.BoVeChuMau(m);
    }

    @Override public void addNotify() { super.addNotify(); store.addListener(refreshListener); search(false); }
    @Override public void removeNotify() { store.removeListener(refreshListener); super.removeNotify(); }
}
