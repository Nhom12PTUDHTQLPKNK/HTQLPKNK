package views;

import controllers.NhatKyKiemToan;
import controllers.BoNhoKho;
import controllers.BoNhoKho.MatHang;
import controllers.BoNhoKho.GiaoDichKho;
import controllers.BoNhoKho.TrangThaiGiaoDichKho;
import controllers.BoNhoKho.LoaiGiaoDichKho;
import controllers.XuatBangPDF;
import controllers.TruongChonNgay;
import models.dao.VatTuDAO;

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
import views.BoCongCuGiaoDien;

public class ManHinhCapNhatKho extends JPanel {

    private static final String[] COLS = {
        "Mã GD", "Thời gian lập", "Loại", "Mã hàng", "Tên hàng", "Lô",
        "Hạn sử dụng", "Số lượng", "ĐVT", "Lý do / Ghi chú", "Trạng thái"
    };
    private static final Class<?>[] TYPES = {
        String.class, String.class, String.class, String.class, String.class,
        String.class, String.class, Integer.class, String.class, String.class, String.class
    };
    private static final int COL_STATUS = 10;
    private static final String ALL = "Tất cả";

    private final BoNhoKho store = BoNhoKho.getInstance();
    private final String currentUser;

    private final DefaultTableModel model = BoCongCuGiaoDien.readOnlyModel(COLS, TYPES);
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    private final JTextField txtSearch = BoCongCuGiaoDien.textField();
    private final JComboBox<String> cbStatusFilter = new JComboBox<>();

    private final JComboBox<LoaiGiaoDichKho> cbType = new JComboBox<LoaiGiaoDichKho>(LoaiGiaoDichKho.values());
    private final JComboBox<MatHang> cbItem = new JComboBox<MatHang>();
    private final JComboBox<String> cbLot = new JComboBox<String>();
    private final TruongChonNgay txtExpiry = new TruongChonNgay();
    private final JTextField txtQty = BoCongCuGiaoDien.textField();
    private final JTextField txtReason = BoCongCuGiaoDien.textField();
    
    private final JLabel lblLot = BoCongCuGiaoDien.fieldLabel("Lô hàng (để trống nếu không có lô):");
    private final JLabel lblExpiry = BoCongCuGiaoDien.fieldLabel("Hạn sử dụng (dd/MM/yyyy, nếu có):");
    private final JLabel lblQty = BoCongCuGiaoDien.fieldLabel("Số lượng:");
    private final JLabel lblReason = BoCongCuGiaoDien.fieldLabel("Nguồn nhập / ghi chú:");
    private final JLabel lblStockInfo = new JLabel(" ");
    private final JLabel lblSelected = new JLabel("Chọn giao dịch trong bảng để xác nhận / hủy.");
    
    private final JButton btnCreate = BoCongCuGiaoDien.button("Tạo giao dịch", BoCongCuGiaoDien.BLUE_PRIMARY, Integer.MAX_VALUE);
    private final JButton btnConfirm = BoCongCuGiaoDien.button("Xác nhận", BoCongCuGiaoDien.GREEN_ACCENT, Integer.MAX_VALUE);
    private final JButton btnCancel = BoCongCuGiaoDien.button("Hủy giao dịch", BoCongCuGiaoDien.RED_ACCENT, Integer.MAX_VALUE);
    private final JButton btnClear = BoCongCuGiaoDien.button("Làm mới", BoCongCuGiaoDien.PINK_ACCENT, Integer.MAX_VALUE);

    private boolean adjusting = false;
    private final Runnable refreshListener = () -> BoCongCuGiaoDien.onEdt(this::reloadAll);

    public ManHinhCapNhatKho() {
        this(NhatKyKiemToan.currentUser());
    }

    public ManHinhCapNhatKho(String currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(BoCongCuGiaoDien.BG_CANVAS);

        JPanel root = BoCongCuGiaoDien.pageRoot();
        add(root, BorderLayout.CENTER);

        // 1. Toolbar & Header
        JPanel tb = BoCongCuGiaoDien.toolbar();
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setToolTipText("Tìm theo mã GD, mã/tên hàng, lô, lý do...");
        txtSearch.addActionListener(e -> applyFilter());

        cbStatusFilter.addItem(ALL);
        for (TrangThaiGiaoDichKho s : TrangThaiGiaoDichKho.values()) {
            cbStatusFilter.addItem(s.getLabel());
        }
        BoCongCuGiaoDien.styleCombo(cbStatusFilter);
        cbStatusFilter.setPreferredSize(new Dimension(140, 36));
        cbStatusFilter.addActionListener(e -> applyFilter());

        JButton btnSearch = BoCongCuGiaoDien.button("Tìm", BoCongCuGiaoDien.BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> applyFilter());

        tb.add(txtSearch);
        tb.add(cbStatusFilter);
        tb.add(btnSearch);

        root.add(BoCongCuGiaoDien.pageHeader(
            "Cập Nhật Kho - Nhập, Xuất, Điều Chỉnh", tb
        ), BorderLayout.NORTH);

        // 2. Center Content (Table + Form)
        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);

        table.setRowSorter(sorter);
        BoCongCuGiaoDien.styleTable(table);
        BoCongCuGiaoDien.setColumnWidths(table, 75, 140, 85, 70, 170, 80, 95, 75, 70, 190, 100);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(statusRenderer());
        XuatBangPDF.install(table, "Giao dịch kho");

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncSelectedTxn();
            }
        });

        center.add(BoCongCuGiaoDien.scroll(table), BorderLayout.CENTER);
        center.add(buildForm(), BorderLayout.EAST);
        root.add(center, BorderLayout.CENTER);

        // Load items
        for (MatHang it : store.items()) {
            cbItem.addItem(it);
        }
        syncForm();
        reloadAll();
    }

    private JPanel buildForm() {
        JPanel p = BoCongCuGiaoDien.card();
        p.setPreferredSize(new Dimension(360, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Lập giao dịch kho");
        title.setFont(BoCongCuGiaoDien.font(Font.BOLD, 15));
        title.setForeground(BoCongCuGiaoDien.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));

        BoCongCuGiaoDien.styleCombo(cbType);
        BoCongCuGiaoDien.styleCombo(cbItem);
        BoCongCuGiaoDien.styleCombo(cbLot);
        cbLot.setEditable(true);
        cbItem.setPrototypeDisplayValue(null);

        // Cùng bề rộng hiển thị với ô ngày hẹn trong form đặt lịch.
        txtExpiry.setPreferredSize(new Dimension(300, 34));
        txtExpiry.setMaximumSize(new Dimension(300, 34));
        txtExpiry.setAlignmentX(Component.LEFT_ALIGNMENT);

        addRow(p, BoCongCuGiaoDien.fieldLabel("Loại giao dịch:"), cbType);
        addRow(p, BoCongCuGiaoDien.fieldLabel("Mặt hàng (thuốc / vật tư / thiết bị):"), cbItem);
        addRow(p, lblLot, cbLot);
        addRow(p, lblExpiry, txtExpiry);
        addRow(p, lblQty, txtQty);
        addRow(p, lblReason, txtReason);

        lblStockInfo.setFont(BoCongCuGiaoDien.font(Font.BOLD, 12));
        lblStockInfo.setForeground(BoCongCuGiaoDien.BLUE_PRIMARY);
        lblStockInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblStockInfo);
        p.add(Box.createVerticalStrut(10));

        p.add(BoCongCuGiaoDien.buttonRow(btnCreate, btnClear));
        p.add(Box.createVerticalStrut(14));

        lblSelected.setFont(BoCongCuGiaoDien.font(Font.PLAIN, 12));
        lblSelected.setForeground(BoCongCuGiaoDien.MUTED);
        lblSelected.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblSelected);
        p.add(Box.createVerticalStrut(6));
        p.add(BoCongCuGiaoDien.buttonRow(btnConfirm, btnCancel));
        p.add(Box.createVerticalGlue());

        // Event Listeners
        cbType.addActionListener(e -> syncForm());
        cbItem.addActionListener(e -> syncForm());
        cbLot.addActionListener(e -> {
            if (!adjusting) onLotChanged();
        });

        JTextComponent editorComponent = (JTextComponent) cbLot.getEditor().getEditorComponent();
        editorComponent.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { if (!adjusting) onLotChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { if (!adjusting) onLotChanged(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        btnCreate.addActionListener(e -> onCreate());
        btnConfirm.addActionListener(e -> onConfirm());
        btnCancel.addActionListener(e -> onCancel());
        btnClear.addActionListener(e -> clearForm());

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

    private MatHang selectedItem() {
        return (MatHang) cbItem.getSelectedItem();
    }

    private LoaiGiaoDichKho selectedType() {
        return (LoaiGiaoDichKho) cbType.getSelectedItem();
    }

    private String lotText() {
        Object o = cbLot.getEditor().getItem();
        return o == null ? "" : o.toString().trim();
    }

    private void syncForm() {
        if (adjusting) return;
        adjusting = true;
        try {
            LoaiGiaoDichKho type = selectedType();
            MatHang item = selectedItem();
            if (type == LoaiGiaoDichKho.NHAP) {
                lblQty.setText("Số lượng nhập:");
                lblReason.setText("Nguồn nhập / ghi chú:");
            } else if (type == LoaiGiaoDichKho.XUAT) {
                lblQty.setText("Số lượng xuất:");
                lblReason.setText("Lý do / bộ phận nhận (bắt buộc):");
            } else {
                lblQty.setText("Số lượng điều chỉnh (+ tăng / - giảm):");
                lblReason.setText("Lý do điều chỉnh (bắt buộc):");
            }
            cbLot.removeAllItems();
            if (item != null) {
                for (String lot : store.lotsOf(item.getCode(), type == LoaiGiaoDichKho.XUAT)) {
                    cbLot.addItem(lot);
                }
                if (cbLot.getItemCount() > 0) {
                    cbLot.setSelectedIndex(0);
                } else {
                    cbLot.setSelectedItem("");
                }
            }
        } finally {
            adjusting = false;
        }
        onLotChanged();
    }

    private void onLotChanged() {
        MatHang item = selectedItem();
        LoaiGiaoDichKho type = selectedType();
        if (item == null) {
            lblStockInfo.setText(" ");
            return;
        }
        String lot = lotText();
        LocalDate known = store.expiryOf(item.getCode(), lot);
        if (known != null) {
            txtExpiry.setText(BoCongCuGiaoDien.fmt(known));
            txtExpiry.setEditable(false);
        } else {
            if (!txtExpiry.isEditable()) {
                txtExpiry.setText("");
            }
            txtExpiry.setEditable(type != LoaiGiaoDichKho.XUAT);
        }
        updateStockInfo();
    }

    private void updateStockInfo() {
        MatHang item = selectedItem();
        if (item == null) {
            lblStockInfo.setText(" ");
            return;
        }
        String lot = lotText();
        int total = store.totalQty(item.getCode());
        int inLot = store.lotQty(item.getCode(), lot);
        String lotPart = (lot.isEmpty() || BoNhoKho.NO_LOT.equals(lot)) ? "" : " | Lô " + lot.toUpperCase() + ": " + inLot;
        lblStockInfo.setText("Tồn hiện tại: " + total + " " + item.getUnit() + lotPart);
    }

    private void clearForm() {
        txtQty.setText("");
        txtReason.setText("");
        txtExpiry.setText("");
        table.clearSelection();
        syncForm();
    }

    private void onCreate() {
        MatHang item = selectedItem();
        if (item == null) {
            BoCongCuGiaoDien.warn(this, "Vui lòng chọn mặt hàng.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException ex) {
            BoCongCuGiaoDien.warn(this, "Số lượng phải là số nguyên (ví dụ 50" + (selectedType() == LoaiGiaoDichKho.DIEU_CHINH ? " hoặc -3" : "") + ").");
            return;
        }
        LocalDate expiry;
        try {
            expiry = BoCongCuGiaoDien.parseDate(txtExpiry.getText());
            if (!txtExpiry.getText().trim().isEmpty() && !txtExpiry.isValidDate()) throw new DateTimeParseException("Ngày không hợp lệ", txtExpiry.getText(), 0);
        } catch (DateTimeParseException ex) {
            BoCongCuGiaoDien.warn(this, "Hạn sử dụng phải đúng định dạng dd/MM/yyyy (ví dụ 31/12/2027).");
            return;
        }
        try {
            GiaoDichKho t = store.create(selectedType(), item.getCode(), lotText(), expiry, qty, txtReason.getText(), currentUser);
            NhatKyKiemToan.event("CREATE", "Tạo giao dịch kho " + t.getId() + " (" + t.getType().getLabel() + ") - "
                    + item.getCode() + ", SL " + t.getDelta() + ", lô " + t.getLotNo());
            BoCongCuGiaoDien.info(this, "Đã tạo giao dịch " + t.getId() + " (Chờ xác nhận).\nTồn kho sẽ thay đổi sau khi bạn bấm \"Xác nhận\".");
            clearForm();
            selectTxn(t.getId());
        } catch (IllegalArgumentException ex) {
            BoCongCuGiaoDien.warn(this, ex.getMessage());
        }
    }

    private void onConfirm() {
        String id = selectedTxnId();
        if (id == null) {
            BoCongCuGiaoDien.warn(this, "Vui lòng chọn giao dịch cần xác nhận.");
            return;
        }
        if (!BoCongCuGiaoDien.confirm(this, "Xác nhận giao dịch " + id + "?\nTồn kho sẽ được cập nhật và không thể hoàn tác (chỉ có thể điều chỉnh).")) {
            return;
        }
        try {
            store.validatePending(id);
            GiaoDichKho pending = store.transaction(id);
            if (pending != null && pending.getType() == LoaiGiaoDichKho.NHAP) {
                MatHang importedItem = store.item(pending.getItemCode());
                if (importedItem != null && importedItem.getCategory() != BoNhoKho.DanhMucKho.THUOC) {
                    boolean updated = new VatTuDAO().increaseInventory(pending.getItemCode(), pending.getQty());
                    if (!updated) {
                        BoCongCuGiaoDien.warn(this, "Không tìm thấy mã vật tư/thiết bị " + pending.getItemCode() + " trong SQL Server. Phiếu chưa được xác nhận.");
                        return;
                    }
                }
            }
            GiaoDichKho t = store.confirm(id, currentUser);
            NhatKyKiemToan.event("CONFIRM", "Xác nhận giao dịch kho " + t.getId() + " (" + t.getType().getLabel() + ") - "
                    + t.getItemCode() + ", biến động " + t.getDelta());
        } catch (java.sql.SQLException ex) {
            BoCongCuGiaoDien.warn(this, "Không thể cập nhật tồn kho trên SQL Server. Phiếu chưa được xác nhận.\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            BoCongCuGiaoDien.warn(this, ex.getMessage());
        }
    }

    private void onCancel() {
        String id = selectedTxnId();
        if (id == null) {
            BoCongCuGiaoDien.warn(this, "Vui lòng chọn giao dịch cần hủy.");
            return;
        }
        if (!BoCongCuGiaoDien.confirm(this, "Hủy giao dịch " + id + "?")) {
            return;
        }
        try {
            store.cancel(id, currentUser);
            NhatKyKiemToan.event("CANCEL", "Hủy giao dịch kho " + id);
        } catch (IllegalArgumentException ex) {
            BoCongCuGiaoDien.warn(this, ex.getMessage());
        }
    }

    private void reloadAll() {
        String keep = selectedTxnId();
        MatHang selItem = selectedItem();
        model.setRowCount(0);
        List<GiaoDichKho> list = store.transactions();
        for (int i = list.size() - 1; i >= 0; i--) {
            GiaoDichKho t = list.get(i);
            MatHang it = store.item(t.getItemCode());
            LocalDate exp = store.expiryOf(t.getItemCode(), t.getLotNo());
            if (exp == null) exp = t.getExpiry();
            model.addRow(new Object[]{
                t.getId(), t.getCreatedAt().format(BoCongCuGiaoDien.DATE_TIME_FMT), t.getType().getLabel(),
                t.getItemCode(), it == null ? "" : it.getName(), t.getLotNo(), BoCongCuGiaoDien.fmt(exp),
                t.getDelta(), it == null ? "" : it.getUnit(), t.getReason(),
                t.getStatus().getLabel()
            });
        }
        if (keep != null) selectTxn(keep);
        syncSelectedTxn();
        if (selItem != null) updateStockInfo();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Object>> fs = new ArrayList<>();
        String kw = txtSearch.getText().trim();
        if (!kw.isEmpty()) {
            fs.add(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }
        String st = (String) cbStatusFilter.getSelectedItem();
        if (st != null && !ALL.equals(st)) {
            fs.add(RowFilter.regexFilter("^" + Pattern.quote(st) + "$", COL_STATUS));
        }
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
                if (v >= 0) {
                    table.setRowSelectionInterval(v, v);
                    table.scrollRectToVisible(table.getCellRect(v, 0, true));
                }
                return;
            }
        }
    }

    private void syncSelectedTxn() {
        String id = selectedTxnId();
        GiaoDichKho t = id == null ? null : store.transaction(id);
        boolean pending = t != null && t.getStatus() == TrangThaiGiaoDichKho.CHO_XAC_NHAN;
        btnConfirm.setEnabled(pending);
        btnCancel.setEnabled(pending);
        if (t == null) {
            lblSelected.setText("Chọn giao dịch trong bảng để xác nhận / hủy.");
        } else {
            lblSelected.setText("Đang chọn: " + t.getId() + " - " + t.getStatus().getLabel());
        }
    }

    private static BoCongCuGiaoDien.BoVeChuMau statusRenderer() {
        Map<String, Color> m = new HashMap<>();
        m.put(TrangThaiGiaoDichKho.CHO_XAC_NHAN.getLabel(), BoCongCuGiaoDien.ORANGE_ACCENT);
        m.put(TrangThaiGiaoDichKho.DA_XAC_NHAN.getLabel(), BoCongCuGiaoDien.GREEN_ACCENT);
        m.put(TrangThaiGiaoDichKho.DA_HUY.getLabel(), BoCongCuGiaoDien.MUTED);
        return new BoCongCuGiaoDien.BoVeChuMau(m);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        store.addListener(refreshListener);
        reloadAll();
    }

    @Override
    public void removeNotify() {
        store.removeListener(refreshListener);
        super.removeNotify();
    }
}
