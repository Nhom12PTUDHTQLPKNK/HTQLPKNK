package views;

import inventory.InventoryStore;
import inventory.InventoryStore.FlowRow;
import inventory.InventoryStore.Item;
import inventory.InventoryStore.Txn;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class InventoryFlowReportPanel extends JPanel {

    private static final String[] SUM_COLS = {"Mã hàng", "Tên hàng", "Loại", "ĐVT", "Tồn đầu kỳ",
            "Nhập trong kỳ", "Xuất trong kỳ", "Điều chỉnh", "Tồn cuối kỳ"};
    private static final Class<?>[] SUM_TYPES = {String.class, String.class, String.class, String.class,
            Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
    private static final String[] TXN_COLS = {"Mã GD", "Xác nhận lúc", "Loại", "Mã hàng", "Lô", "Số lượng",
            "Lý do / Ghi chú", "Người lập", "Người xác nhận"};
    private static final Class<?>[] TXN_TYPES = {String.class, String.class, String.class, String.class, String.class,
            Integer.class, String.class, String.class, String.class};

    private final InventoryStore store = InventoryStore.getInstance();
    private final DefaultTableModel sumModel = UiKit.readOnlyModel(SUM_COLS, SUM_TYPES);
    private final DefaultTableModel txnModel = UiKit.readOnlyModel(TXN_COLS, TXN_TYPES);
    private final JTable sumTable = new JTable(sumModel);
    private final JTable txnTable = new JTable(txnModel);

    private final JTextField txtFrom = UiKit.textField();
    private final JTextField txtTo = UiKit.textField();
    private final JComboBox<String> cbItem = new JComboBox<String>();
    private final List<String> itemCodes = new ArrayList<String>();
    private final JLabel lblSummary = new JLabel(" ");
    private final JLabel lblDetailTitle = new JLabel(" ");

    private LocalDate periodFrom;
    private LocalDate periodTo;

    private final Runnable refreshListener = new Runnable() {
        @Override public void run() { UiKit.onEdt(new Runnable() { @Override public void run() { reload(); } }); }
    };

    public InventoryFlowReportPanel() {
        setLayout(new BorderLayout());
        setBackground(UiKit.BG_CANVAS);
        JPanel root = UiKit.pageRoot();
        add(root, BorderLayout.CENTER);

        root.add(UiKit.pageHeader("Thống Kê Xuất Nhập Tồn Kho",
                "Tồn đầu kỳ, nhập, xuất, điều chỉnh, tồn cuối kỳ - đối chiếu với giao dịch kho đã xác nhận", null),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildFilterCard(), BorderLayout.NORTH);

        UiKit.styleTable(sumTable);
        sumTable.setAutoCreateRowSorter(true);
        UiKit.setColumnWidths(sumTable, 75, 200, 80, 80, 100, 110, 110, 100, 100);
        PdfTableExport.install(sumTable, "Thống kê xuất nhập tồn");
        UiKit.styleTable(txnTable);
        txnTable.setAutoCreateRowSorter(true);
        UiKit.setColumnWidths(txnTable, 75, 150, 90, 75, 90, 80, 220, 100, 110);
        PdfTableExport.install(txnTable, "Giao dịch kho trong kỳ");
        sumTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) loadDetail();
            }
        });

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(UiKit.scroll(sumTable), BorderLayout.CENTER);
        lblSummary.setFont(UiKit.font(Font.PLAIN, 12));
        lblSummary.setForeground(UiKit.MUTED);
        top.add(lblSummary, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        lblDetailTitle.setFont(UiKit.font(Font.BOLD, 13));
        lblDetailTitle.setForeground(UiKit.TEXT_DARK);
        bottom.add(lblDetailTitle, BorderLayout.NORTH);
        bottom.add(UiKit.scroll(txnTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(8);
        body.add(split, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        LocalDate today = LocalDate.now();
        txtFrom.setText(UiKit.fmt(today.minusDays(90)));
        txtTo.setText(UiKit.fmt(today));
        fillItemCombo();
        generate(false);
    }

    private JPanel buildFilterCard() {
        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        UiKit.styleCombo(cbItem);
        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 2));
        grid.setOpaque(false);
        grid.add(UiKit.fieldLabel("Từ ngày (dd/MM/yyyy)"));
        grid.add(UiKit.fieldLabel("Đến ngày (dd/MM/yyyy)"));
        grid.add(UiKit.fieldLabel("Mặt hàng"));
        grid.add(txtFrom);
        grid.add(txtTo);
        grid.add(cbItem);
        card.add(grid, BorderLayout.CENTER);

        JButton btnRun = UiKit.button("Thống kê", UiKit.BLUE_PRIMARY, 100);
        JButton btnMonth = UiKit.button("Tháng này", UiKit.GREEN_ACCENT, 115);
        JButton btnReset = UiKit.button("Đặt lại", UiKit.PINK_ACCENT, 90);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnReset);
        buttons.add(btnMonth);
        buttons.add(btnRun);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(8, 0, 0, 0));
        south.add(buttons, BorderLayout.EAST);
        card.add(south, BorderLayout.SOUTH);

        btnRun.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { generate(true); }
        });
        btnMonth.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                LocalDate t = LocalDate.now();
                txtFrom.setText(UiKit.fmt(t.withDayOfMonth(1)));
                txtTo.setText(UiKit.fmt(t));
                generate(true);
            }
        });
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                LocalDate t = LocalDate.now();
                txtFrom.setText(UiKit.fmt(t.minusDays(90)));
                txtTo.setText(UiKit.fmt(t));
                cbItem.setSelectedIndex(0);
                generate(true);
            }
        });
        return card;
    }

    private void fillItemCombo() {
        String keep = selectedItemCode();
        cbItem.removeAllItems();
        itemCodes.clear();
        cbItem.addItem("Tất cả mặt hàng");
        itemCodes.add(null);
        for (Item it : store.items()) {
            cbItem.addItem(it.getCode() + " - " + it.getName());
            itemCodes.add(it.getCode());
        }
        int idx = keep == null ? 0 : itemCodes.indexOf(keep);
        cbItem.setSelectedIndex(idx < 0 ? 0 : idx);
    }

    private String selectedItemCode() {
        int i = cbItem.getSelectedIndex();
        return (i <= 0 || i >= itemCodes.size()) ? null : itemCodes.get(i);
    }

    private void generate(boolean interactive) {
        LocalDate from, to;
        try {
            from = UiKit.parseDate(txtFrom.getText());
            to = UiKit.parseDate(txtTo.getText());
        } catch (DateTimeParseException ex) {
            if (interactive) UiKit.warn(this, "Ngày phải đúng định dạng dd/MM/yyyy (ví dụ 31/10/2026).");
            return;
        }
        if (from == null || to == null) {
            if (interactive) UiKit.warn(this, "Vui lòng nhập đủ \"Từ ngày\" và \"Đến ngày\".");
            return;
        }
        if (to.isBefore(from)) {
            if (interactive) UiKit.warn(this, "\"Đến ngày\" phải sau hoặc bằng \"Từ ngày\".");
            return;
        }
        periodFrom = from;
        periodTo = to;
        reload();
    }

    private void reload() {
        if (periodFrom == null) return;
        String keep = selectedCode();
        List<FlowRow> rows = store.flow(periodFrom, periodTo, selectedItemCode());
        sumModel.setRowCount(0);
        for (FlowRow r : rows) {
            Item it = r.getItem();
            sumModel.addRow(new Object[]{it.getCode(), it.getName(), it.getCategory().getLabel(), it.getUnit(),
                    Integer.valueOf(r.getOpening()), Integer.valueOf(r.getIn()), Integer.valueOf(r.getOut()),
                    Integer.valueOf(r.getAdjust()), Integer.valueOf(r.getClosing())});
        }
        if (keep != null) {
            for (int i = 0; i < sumModel.getRowCount(); i++) {
                if (keep.equals(sumModel.getValueAt(i, 0))) {
                    int v = sumTable.convertRowIndexToView(i);
                    if (v >= 0) sumTable.setRowSelectionInterval(v, v);
                    break;
                }
            }
        }
        int txnCount = store.confirmedBetween(periodFrom, periodTo, selectedItemCode()).size();
        lblSummary.setText("Kỳ " + UiKit.fmt(periodFrom) + " - " + UiKit.fmt(periodTo) + ": " + rows.size()
                + " mặt hàng, " + txnCount + " giao dịch đã xác nhận. Tồn cuối = Tồn đầu + Nhập - Xuất + Điều chỉnh.");
        loadDetail();
    }

    private String selectedCode() {
        int r = sumTable.getSelectedRow();
        return r == -1 ? null : String.valueOf(sumModel.getValueAt(sumTable.convertRowIndexToModel(r), 0));
    }

    private void loadDetail() {
        if (periodFrom == null) return;
        String code = selectedCode();
        String filterCode = code != null ? code : selectedItemCode();
        List<Txn> list = store.confirmedBetween(periodFrom, periodTo, filterCode);
        txnModel.setRowCount(0);
        for (Txn t : list) {
            txnModel.addRow(new Object[]{t.getId(), t.getConfirmedAt().format(UiKit.DATE_TIME_FMT), t.getType().getLabel(),
                    t.getItemCode(), t.getLotNo(), Integer.valueOf(t.getDelta()), t.getReason(),
                    t.getCreatedBy(), t.getConfirmedBy()});
        }
        String scope = filterCode == null ? "tất cả mặt hàng" : filterCode;
        lblDetailTitle.setText("Giao dịch kho trong kỳ - " + scope + " (" + list.size() + " giao dịch)"
                + (code == null ? "  |  chọn một dòng ở bảng trên để xem riêng từng mặt hàng" : ""));
    }

    @Override public void addNotify() { super.addNotify(); store.addListener(refreshListener); fillItemCombo(); reload(); }
    @Override public void removeNotify() { store.removeListener(refreshListener); super.removeNotify(); }
}
