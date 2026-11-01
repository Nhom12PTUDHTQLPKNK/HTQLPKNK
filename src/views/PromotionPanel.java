package views;

import datepicker.DatePickerField;
import models.dao.KhuyenMaiDAO;
import models.entity.KhuyenMai;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class PromotionPanel extends JPanel {
    private static final String[] COLUMNS = {
            "Mã khuyến mãi", "Tên chương trình", "Mức giảm (%)", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"
    };

    private final KhuyenMaiDAO promotionDAO = new KhuyenMaiDAO();
    private final DefaultTableModel model = UiKit.readOnlyModel(COLUMNS);
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
    private final JTextField txtSearch = UiKit.textField();
    private final JTextField txtId = UiKit.textField();
    private final JTextField txtName = UiKit.textField();
    private final JTextField txtDiscount = UiKit.textField();
    private final DatePickerField dateFrom = new DatePickerField();
    private final DatePickerField dateTo = new DatePickerField();
    private final JCheckBox chkActive = new JCheckBox("Đang áp dụng", true);
    private final JLabel lblCount = new JLabel(" ");
    private boolean editing;

    public PromotionPanel() {
        setLayout(new BorderLayout());
        setBackground(UiKit.BG_CANVAS);
        JPanel root = UiKit.pageRoot();
        add(root, BorderLayout.CENTER);

        JPanel toolbar = UiKit.toolbar();
        txtSearch.setPreferredSize(new Dimension(190, 36));
        txtSearch.setToolTipText("Tìm theo mã, tên chương trình hoặc trạng thái");
        txtSearch.addActionListener(e -> applySearch());
        JButton btnSearch = UiKit.button("Tìm", UiKit.BLUE_PRIMARY, 75);
        JButton btnReload = UiKit.button("Tải lại", new Color(74, 85, 96), 90);
        btnSearch.addActionListener(e -> applySearch());
        btnReload.addActionListener(e -> { txtSearch.setText(""); reload(); });
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnReload);
        root.add(UiKit.pageHeader("Quản lý chương trình khuyến mãi",
                "Thêm, cập nhật, xóa và theo dõi chương trình khuyến mãi trong SQL Server", toolbar), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(buildForm(), BorderLayout.NORTH);
        UiKit.styleTable(table);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) loadSelection();
        });
        PdfTableExport.install(table, "Quản lý chương trình khuyến mãi");

        JPanel listCard = UiKit.card();
        listCard.setLayout(new BorderLayout(0, 8));
        listCard.setBorder(new EmptyBorder(14, 16, 12, 16));
        listCard.add(UiKit.fieldLabel("Danh sách chương trình"), BorderLayout.NORTH);
        listCard.add(UiKit.scroll(table), BorderLayout.CENTER);
        lblCount.setFont(UiKit.font(Font.PLAIN, 12));
        lblCount.setForeground(UiKit.MUTED);
        listCard.add(lblCount, BorderLayout.SOUTH);
        content.add(listCard, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);

        clearForm();
        reload();
    }

    private JPanel buildForm() {
        JPanel card = UiKit.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel grid = new JPanel(new GridLayout(0, 3, 10, 8));
        grid.setOpaque(false);
        addField(grid, "Mã khuyến mãi", txtId);
        addField(grid, "Tên chương trình", txtName);
        addField(grid, "Mức giảm (%)", txtDiscount);
        addField(grid, "Ngày bắt đầu (dd/MM/yyyy)", dateFrom);
        addField(grid, "Ngày kết thúc (dd/MM/yyyy)", dateTo);
        JPanel statusField = new JPanel(new BorderLayout());
        statusField.setOpaque(false);
        statusField.add(UiKit.fieldLabel("Trạng thái"), BorderLayout.NORTH);
        chkActive.setOpaque(false);
        chkActive.setFont(UiKit.font(Font.PLAIN, 13));
        statusField.add(chkActive, BorderLayout.CENTER);
        grid.add(statusField);

        configureDateField(dateFrom);
        configureDateField(dateTo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton btnClear = UiKit.button("Làm mới", UiKit.PINK_ACCENT, 95);
        JButton btnDelete = UiKit.button("Xóa", UiKit.RED_ACCENT, 85);
        JButton btnSave = UiKit.button("Lưu", UiKit.GREEN_ACCENT, 85);
        btnClear.addActionListener(e -> clearForm());
        btnDelete.addActionListener(e -> deletePromotion());
        btnSave.addActionListener(e -> savePromotion());
        actions.add(btnClear);
        actions.add(btnDelete);
        actions.add(btnSave);
        card.add(grid, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel grid, String label, JComponent field) {
        JPanel holder = new JPanel(new BorderLayout(0, 5));
        holder.setOpaque(false);
        holder.add(UiKit.fieldLabel(label), BorderLayout.NORTH);
        holder.add(field, BorderLayout.CENTER);
        grid.add(holder);
    }

    private void configureDateField(DatePickerField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setPreferredSize(new Dimension(140, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void savePromotion() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String discountText = txtDiscount.getText().trim().replace("%", "").replace(",", ".");
        if (id.isEmpty() || name.isEmpty()) {
            UiKit.warn(this, "Nhập mã khuyến mãi và tên chương trình.");
            return;
        }
        try {
            LocalDate from = parseDate(dateFrom, "Ngày bắt đầu");
            LocalDate to = parseDate(dateTo, "Ngày kết thúc");
            if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
            double discount = Double.parseDouble(discountText);
            if (discount < 0 || discount > 100) throw new NumberFormatException();

            KhuyenMai promotion = new KhuyenMai();
            promotion.setMaKhuyenMai(id);
            promotion.setTenKhuyenMai(name);
            promotion.setGiaTriGiam(discount);
            promotion.setNgayBatDau(java.sql.Date.valueOf(from));
            promotion.setNgayKetThuc(java.sql.Date.valueOf(to));
            promotion.setTrangThai(chkActive.isSelected());
            boolean wasEditing = editing;
            boolean saved = wasEditing ? promotionDAO.update(promotion) : promotionDAO.insert(promotion);
            if (!saved) throw new SQLException("SQL Server không lưu được thay đổi.");
            clearForm();
            reload();
            UiKit.info(this, wasEditing ? "Đã cập nhật chương trình khuyến mãi." : "Đã thêm chương trình khuyến mãi.");
        } catch (NumberFormatException ex) {
            UiKit.warn(this, "Mức giảm phải là số từ 0 đến 100.");
        } catch (DateTimeParseException ex) {
            UiKit.warn(this, "Ngày phải đúng định dạng dd/MM/yyyy.");
        } catch (IllegalArgumentException ex) {
            UiKit.warn(this, ex.getMessage());
        } catch (SQLException ex) {
            UiKit.warn(this, "Không thể lưu vào SQL Server: " + ex.getMessage());
        }
    }

    private LocalDate parseDate(DatePickerField field, String label) {
        if (!field.isValidDate()) throw new IllegalArgumentException(label + " không hợp lệ.");
        LocalDate date = UiKit.parseDate(field.getText());
        if (date == null) throw new IllegalArgumentException("Vui lòng chọn " + label.toLowerCase() + ".");
        return date;
    }

    private void deletePromotion() {
        if (!editing || txtId.getText().trim().isEmpty()) {
            UiKit.warn(this, "Chọn chương trình cần xóa trong danh sách trước.");
            return;
        }
        if (!UiKit.confirm(this, "Xóa chương trình khuyến mãi " + txtId.getText().trim() + "?")) return;
        try {
            if (!promotionDAO.delete(txtId.getText().trim())) throw new SQLException("Không tìm thấy chương trình.");
            clearForm();
            reload();
            UiKit.info(this, "Đã xóa chương trình khuyến mãi khỏi SQL Server.");
        } catch (SQLException ex) {
            UiKit.warn(this, "Không thể xóa chương trình: " + ex.getMessage());
        }
    }

    private void loadSelection() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        txtId.setText(String.valueOf(model.getValueAt(row, 0)));
        txtId.setEnabled(false);
        txtName.setText(String.valueOf(model.getValueAt(row, 1)));
        txtDiscount.setText(String.valueOf(model.getValueAt(row, 2)));
        dateFrom.setText(String.valueOf(model.getValueAt(row, 3)));
        dateTo.setText(String.valueOf(model.getValueAt(row, 4)));
        chkActive.setSelected("Đang áp dụng".equals(model.getValueAt(row, 5)));
        editing = true;
    }

    private void clearForm() {
        editing = false;
        table.clearSelection();
        txtId.setEnabled(true);
        txtId.setText("");
        txtName.setText("");
        txtDiscount.setText("");
        dateFrom.setText(UiKit.fmt(LocalDate.now()));
        dateTo.setText(UiKit.fmt(LocalDate.now().plusMonths(1)));
        chkActive.setSelected(true);
    }

    private void reload() {
        model.setRowCount(0);
        List<KhuyenMai> promotions = promotionDAO.getAll();
        for (KhuyenMai promotion : promotions) {
            Date start = promotion.getNgayBatDau();
            Date end = promotion.getNgayKetThuc();
            model.addRow(new Object[]{promotion.getMaKhuyenMai(), promotion.getTenKhuyenMai(),
                    promotion.getGiaTriGiam(), start == null ? "" : UiKit.fmt(new java.sql.Date(start.getTime()).toLocalDate()),
                    end == null ? "" : UiKit.fmt(new java.sql.Date(end.getTime()).toLocalDate()),
                    Boolean.TRUE.equals(promotion.getTrangThai()) ? "Đang áp dụng" : "Ngừng áp dụng"});
        }
        lblCount.setText("Tổng " + model.getRowCount() + " chương trình trong SQL Server.");
        applySearch();
    }

    private void applySearch() {
        String keyword = txtSearch.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
    }
}
