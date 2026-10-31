package views;

import Audit.AuditLog;
import inventory.InventoryStore;
import inventory.InventoryStore.Item;
import inventory.InventoryStore.Txn;
import inventory.InventoryStore.TxnStatus;
import inventory.InventoryStore.TxnType;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Cập nhật kho vật tư, thiết bị, thuốc.
 *  - Tạo giao dịch NHẬP / XUẤT / ĐIỀU CHỈNH (có lô, hạn sử dụng, lý do).
 *  - Giao dịch mới ở trạng thái "Chờ xác nhận"; chỉ khi bấm "Xác nhận" thì tồn kho mới thay đổi.
 */
public class InventoryUpdatePanel extends JPanel {

    private static final String[] COLS = {"Mã GD", "Thời gian lập", "Loại", "Mã hàng", "Tên hàng", "Lô",
            "Hạn sử dụng", "Số lượng", "ĐVT", "Lý do / Ghi chú", "Trạng thái", "Người lập"};
    private static final Class<?>[] TYPES = {String.class, String.class, String.class, String.class, String.class,
            String.class, String.class, Integer.class, String.class, String.class, String.class, String.class};
    private static final int COL_STATUS = 10;
    private static final String ALL = "Tất cả";

    private final InventoryStore store = InventoryStore.getInstance();
    private final String currentUser;

    private final DefaultTableModel model = UiKit.readOnlyModel(COLS, TYPES);
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);

    private final JTextField txtSearch = UiKit.textField();
    private final JComboBox<String> cbStatusFilter = new JComboBox<String>();

    private final JComboBox<TxnType> cbType = new JComboBox<TxnType>(TxnType.values());
    private final JComboBox<Item> cbItem = new JComboBox<Item>();
    private final JComboBox<String> cbLot = new JComboBox<String>();
    private final JTextField txtExpiry = UiKit.textField();
    private final JTextField txtQty = UiKit.textField();
    private final JTextField txtReason = UiKit.textField();
    private final JLabel lblLot = UiKit.fieldLabel("Lô hàng (để trống nếu không có lô):");
    private final JLabel lblExpiry = UiKit.fieldLabel("Hạn sử dụng (dd/MM/yyyy, nếu có):");
    private final JLabel lblQty = UiKit.fieldLabel("Số lượng:");
    private final JLabel lblReason = UiKit.fieldLabel("Nguồn nhập / ghi chú:");
    private final JLabel lblStockInfo = new JLabel(" ");
    private final JLabel lblSelected = new JLabel("Chọn giao dịch trong bảng để xác nhận / hủy.");
    private final JButton btnCreate = UiKit.button("Tạo giao dịch", UiKit.BLUE_PRIMARY, Integer.MAX_VALUE);
    private final JButton btnConfirm = UiKit.button("Xác nhận", UiKit.GREEN_ACCENT, Integer.MAX_VALUE);
    private final JButton btnCancel = UiKit.button("Hủy giao dịch", UiKit.RED_ACCENT, Integer.MAX_VALUE);
    private final JButton btnClear = UiKit.button("Làm mới", UiKit.PINK_ACCENT, Integer.MAX_VALUE);

    private boolean adjusting = false;
    private final Runnable refreshListener = new Runnable() {
        @Override public void run() { UiKit.onEdt(new Runnable() { @Override public void run() { reloadAll(); } }); }
    };

    public InventoryUpdatePanel() { this(System.getProperty("user.name", "Hệ thống")); }

    public InventoryUpdatePanel(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(UiKit.BG_CANVAS);

        JPanel root = UiKit.pageRoot();
        add(root, BorderLayout.CENTER);

        // ----- header + toolbar
        JPanel tb = UiKit.toolbar();
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setToolTipText("Tìm theo mã GD, mã/tên hàng, lô, lý do...");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { applyFilter(); }
        });
        cbStatusFilter.addItem(ALL);
        for (TxnStatus s : TxnStatus.values()) cbStatusFilter.addItem(s.getLabel());
        UiKit.styleCombo(cbStatusFilter);
        cbStatusFilter.setPreferredSize(new Dimension(140, 36));
        cbStatusFilter.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { applyFilter(); }
        });
        JButton btnSearch = UiKit.button("Tìm", UiKit.BLUE_PRIMARY, 75);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { applyFilter(); }
        });
        tb.add(txtSearch);
        tb.add(cbStatusFilter);
        tb.add(btnSearch);
        root.add(UiKit.pageHeader("Cập Nhật Kho - Nhập, Xuất, Điều Chỉnh",
                "Tồn kho chỉ thay đổi sau khi giao dịch được xác nhận", tb), BorderLayout.NORTH);

        // ----- bảng + form
        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);

        table.setRowSorter(sorter);
        UiKit.styleTable(table);
        UiKit.setColumnWidths(table, 75, 140, 85, 70, 170, 80, 95, 75, 70, 190, 100, 90);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(statusRenderer());
        PdfTableExport.install(table, "Giao dịch kho");
        table.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) syncSelectedTxn();
            }
        });
        center.add(UiKit.scroll(table), BorderLayout.CENTER);
        center.add(buildForm(), BorderLayout.EAST);
        root.add(center, BorderLayout.CENTER);

        for (Item it : store.items()) cbItem.addItem(it);
        syncForm();
        reloadAll();
    }

    // ------------------------------------------------------------------ form
    private JPanel buildForm() {
        JPanel p = UiKit.card();
        p.setPreferredSize(new Dimension(360, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Lập giao dịch kho");
        title.setFont(UiKit.font(Font.BOLD, 15));
        title.setForeground(UiKit.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));

        UiKit.styleCombo(cbType);
        UiKit.styleCombo(cbItem);
        UiKit.styleCombo(cbLot);
        cbLot.setEditable(true);
        cbItem.setPrototypeDisplayValue(null);

        addRow(p, UiKit.fieldLabel("Loại giao dịch:"), cbType);
        addRow(p, UiKit.fieldLabel("Mặt hàng (thuốc / vật tư / thiết bị):"), cbItem);
        addRow(p, lblLot, cbLot);
        addRow(p, lblExpiry, txtExpiry);
        addRow(p, lblQty, txtQty);
        addRow(p, lblReason, txtReason);

        lblStockInfo.setFont(UiKit.font(Font.BOLD, 12));
        lblStockInfo.setForeground(UiKit.BLUE_PRIMARY);
        lblStockInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblStockInfo);
        p.add(Box.createVerticalStrut(10));

        p.add(UiKit.buttonRow(btnCreate, btnClear));
        p.add(Box.createVerticalStrut(14));

        lblSelected.setFont(UiKit.font(Font.PLAIN, 12));
        lblSelected.setForeground(UiKit.MUTED);
        lblSelected.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblSelected);
        p.add(Box.createVerticalStrut(6));
        p.add(UiKit.buttonRow(btnConfirm, btnCancel));
        p.add(Box.createVerticalGlue());

        // sự kiện
        cbType.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { syncForm(); }
        });
        cbItem.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { syncForm(); }
        });
        cbLot.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { if (!adjusting) onLotChanged(); }
        });
        ((JTextComponent) cbLot.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { if (!adjusting) onLotChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { if (!adjusting) onLotChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { }
        });
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onCreate(); }
        });
        btnConfirm.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onConfirm(); }
        });
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onCancel(); }
        });
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { clearForm(); }
        });
        btnConfirm.setEnabled(false);
        btnCancel.setEnabled(false);
        return p;
    }

    private static void addRow(JPanel p, JLabel l, JComponent c) {
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(c);
        p.add(Box.createVerticalStrut(8));
    }

    private Item selectedItem() { return (Item) cbItem.getSelectedItem(); }
    private TxnType selectedType() { return (TxnType) cbType.getSelectedItem(); }

    private String lotText() {
        Object o = cbLot.getEditor().getItem();
        return o == null ? "" : o.toString().trim();
    }

    /** Đổi nhãn / danh sách lô theo loại giao dịch và mặt hàng. */
    private void syncForm() {
        if (adjusting) return;
        adjusting = true;
        try {
            TxnType type = selectedType();
            Item item = selectedItem();
            if (type == TxnType.NHAP) {
                lblQty.setText("Số lượng nhập:");
                lblReason.setText("Nguồn nhập / ghi chú:");
            } else if (type == TxnType.XUAT) {
                lblQty.setText("Số lượng xuất:");
                lblReason.setText("Lý do / bộ phận nhận (bắt buộc):");
            } else {
                lblQty.setText("Số lượng điều chỉnh (+ tăng / - giảm):");
                lblReason.setText("Lý do điều chỉnh (bắt buộc):");
            }
            cbLot.removeAllItems();
            if (item != null) {
                for (String lot : store.lotsOf(item.getCode(), type == TxnType.XUAT)) cbLot.addItem(lot);
                if (cbLot.getItemCount() > 0) cbLot.setSelectedIndex(0); else cbLot.setSelectedItem("");
            }
        } finally {
            adjusting = false;
        }
        onLotChanged();
    }

    private void onLotChanged() {
        Item item = selectedItem();
        TxnType type = selectedType();
        if (item == null) { lblStockInfo.setText(" "); return; }
        String lot = lotText();
        LocalDate known = store.expiryOf(item.getCode(), lot);
        if (known != null) {
            txtExpiry.setText(UiKit.fmt(known));
            txtExpiry.setEditable(false);
        } else {
            if (!txtExpiry.isEditable()) txtExpiry.setText("");
            txtExpiry.setEditable(type != TxnType.XUAT);
        }
        updateStockInfo();
    }

    private void updateStockInfo() {
        Item item = selectedItem();
        if (item == null) { lblStockInfo.setText(" "); return; }
        String lot = lotText();
        int total = store.totalQty(item.getCode());
        int inLot = store.lotQty(item.getCode(), lot);
        String lotPart = (lot.isEmpty() || InventoryStore.NO_LOT.equals(lot)) ? "" : " | Lô " + lot.toUpperCase() + ": " + inLot;
        lblStockInfo.setText("Tồn hiện tại: " + total + " " + item.getUnit() + lotPart);
    }

    private void clearForm() {
        txtQty.setText("");
        txtReason.setText("");
        txtExpiry.setText("");
        table.clearSelection();
        syncForm();
    }

    // ------------------------------------------------------------------ hành động
    private void onCreate() {
        Item item = selectedItem();
        if (item == null) { UiKit.warn(this, "Vui lòng chọn mặt hàng."); return; }
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException ex) {
            UiKit.warn(this, "Số lượng phải là số nguyên (ví dụ 50" + (selectedType() == TxnType.DIEU_CHINH ? " hoặc -3" : "") + ").");
            return;
        }
        LocalDate expiry;
        try {
            expiry = UiKit.parseDate(txtExpiry.getText());
        } catch (DateTimeParseException ex) {
            UiKit.warn(this, "Hạn sử dụng phải đúng định dạng dd/MM/yyyy (ví dụ 31/12/2027).");
            return;
        }
        try {
            Txn t = store.create(selectedType(), item.getCode(), lotText(), expiry, qty, txtReason.getText(), currentUser);
            AuditLog.event("CREATE", "Tạo giao dịch kho " + t.getId() + " (" + t.getType().getLabel() + ") - "
                    + item.getCode() + ", SL " + t.getDelta() + ", lô " + t.getLotNo());
            UiKit.info(this, "Đã tạo giao dịch " + t.getId() + " (Chờ xác nhận).\nTồn kho sẽ thay đổi sau khi bạn bấm \"Xác nhận\".");
            clearForm();
            selectTxn(t.getId());
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        }
    }

    private void onConfirm() {
        String id = selectedTxnId();
        if (id == null) { UiKit.warn(this, "Vui lòng chọn giao dịch cần xác nhận."); return; }
        if (!UiKit.confirm(this, "Xác nhận giao dịch " + id + "?\nTồn kho sẽ được cập nhật và không thể hoàn tác (chỉ có thể điều chỉnh).")) return;
        try {
            Txn t = store.confirm(id, currentUser);
            AuditLog.event("CONFIRM", "Xác nhận giao dịch kho " + t.getId() + " (" + t.getType().getLabel() + ") - "
                    + t.getItemCode() + ", biến động " + t.getDelta());
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        }
    }

    private void onCancel() {
        String id = selectedTxnId();
        if (id == null) { UiKit.warn(this, "Vui lòng chọn giao dịch cần hủy."); return; }
        if (!UiKit.confirm(this, "Hủy giao dịch " + id + "?")) return;
        try {
            store.cancel(id, currentUser);
            AuditLog.event("CANCEL", "Hủy giao dịch kho " + id);
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ bảng
    private void reloadAll() {
        String keep = selectedTxnId();
        Item selItem = selectedItem();
        model.setRowCount(0);
        List<Txn> list = store.transactions();
        for (int i = list.size() - 1; i >= 0; i--) {
            Txn t = list.get(i);
            Item it = store.item(t.getItemCode());
            LocalDate exp = store.expiryOf(t.getItemCode(), t.getLotNo());
            if (exp == null) exp = t.getExpiry();
            model.addRow(new Object[]{t.getId(), t.getCreatedAt().format(UiKit.DATE_TIME_FMT), t.getType().getLabel(),
                    t.getItemCode(), it == null ? "" : it.getName(), t.getLotNo(), UiKit.fmt(exp),
                    Integer.valueOf(t.getDelta()), it == null ? "" : it.getUnit(), t.getReason(),
                    t.getStatus().getLabel(), t.getCreatedBy()});
        }
        if (keep != null) selectTxn(keep);
        syncSelectedTxn();
        if (selItem != null) updateStockInfo();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Object>> fs = new ArrayList<RowFilter<DefaultTableModel, Object>>();
        String kw = txtSearch.getText().trim();
        if (!kw.isEmpty()) fs.add(RowFilter.<DefaultTableModel, Object>regexFilter("(?i)" + Pattern.quote(kw)));
        String st = (String) cbStatusFilter.getSelectedItem();
        if (st != null && !ALL.equals(st))
            fs.add(RowFilter.<DefaultTableModel, Object>regexFilter("^" + Pattern.quote(st) + "$", COL_STATUS));
        sorter.setRowFilter(fs.isEmpty() ? null : RowFilter.andFilter(fs));
    }

    private String selectedTxnId() {
        int r = table.getSelectedRow();
        if (r == -1) return null;
        return String.valueOf(model.getValueAt(table.convertRowIndexToModel(r), 0));
    }

    private void selectTxn(String id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (id.equals(model.getValueAt(i, 0))) {
                int v = table.convertRowIndexToView(i);
                if (v >= 0) { table.setRowSelectionInterval(v, v); table.scrollRectToVisible(table.getCellRect(v, 0, true)); }
                return;
            }
        }
    }

    private void syncSelectedTxn() {
        String id = selectedTxnId();
        Txn t = id == null ? null : store.transaction(id);
        boolean pending = t != null && t.getStatus() == TxnStatus.CHO_XAC_NHAN;
        btnConfirm.setEnabled(pending);
        btnCancel.setEnabled(pending);
        if (t == null) lblSelected.setText("Chọn giao dịch trong bảng để xác nhận / hủy.");
        else lblSelected.setText("Đang chọn: " + t.getId() + " - " + t.getStatus().getLabel());
    }

    private static UiKit.ColorTextRenderer statusRenderer() {
        Map<String, Color> m = new HashMap<String, Color>();
        m.put(TxnStatus.CHO_XAC_NHAN.getLabel(), UiKit.ORANGE_ACCENT);
        m.put(TxnStatus.DA_XAC_NHAN.getLabel(), UiKit.GREEN_ACCENT);
        m.put(TxnStatus.DA_HUY.getLabel(), UiKit.MUTED);
        return new UiKit.ColorTextRenderer(m);
    }

    // ------------------------------------------------------------------ vòng đời
    @Override public void addNotify() { super.addNotify(); store.addListener(refreshListener); reloadAll(); }
    @Override public void removeNotify() { store.removeListener(refreshListener); super.removeNotify(); }
}
