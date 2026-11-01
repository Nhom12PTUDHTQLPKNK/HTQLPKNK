package views;

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

public class PromotionProcessPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(24, 117, 58);
    private static final Color RED_ACCENT = new Color(180, 35, 45);
    private static final Color PINK_ACCENT = new Color(174, 35, 86);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField txtSearch, txtPromoId, txtPromoName, txtDiscount;
    private datepicker.DatePickerField txtEndDate;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private final KhuyenMaiDAO promotionDAO = new KhuyenMaiDAO();

   

   
    public PromotionProcessPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Xử Lý: Cập Nhật Khuyến Mãi (F3)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Thiết lập và quản lý các chương trình ưu đãi, giảm giá dịch vụ nha khoa");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // TOOLBAR TÌM KIẾM
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

        // CONTAINER CHÍNH
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {"Mã KM", "Tên Chương Trình Ưu Đãi", "Mức Giảm (%)", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Trạng Thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Xử lý khuyến mãi");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
                txtPromoId.setText(String.valueOf(tableModel.getValueAt(selectedRowIndex, 0)));
                txtPromoId.setEnabled(false);
                txtPromoName.setText(String.valueOf(tableModel.getValueAt(selectedRowIndex, 1)));
                txtDiscount.setText(String.valueOf(tableModel.getValueAt(selectedRowIndex, 2)));
                txtEndDate.setText(String.valueOf(tableModel.getValueAt(selectedRowIndex, 4)));
                isEditing = true;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        centerContainer.add(scroll, BorderLayout.CENTER);

        centerContainer.add(createFormPanel(), BorderLayout.EAST);
        add(centerContainer, BorderLayout.CENTER);
        loadDataFromDatabase();
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
        // Cố định chiều rộng form để tránh tràn giao diện
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Cập Nhật Khuyến Mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        txtPromoId = addField(panel, "Mã Khuyến Mãi (Tự nhập):");
        txtPromoName = addField(panel, "Tên Chương Trình:");
        txtDiscount = addField(panel, "Mức Giảm (%):");
        txtEndDate = addDateField(panel, "Ngày Kết Thúc (dd/MM/yyyy):");

        // Nhóm nút Lưu / Xóa
        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("💾 Lưu KM", GREEN_ACCENT, Integer.MAX_VALUE);
        JButton btnDelete = createStaticButton("🗑️ Xóa KM", RED_ACCENT, Integer.MAX_VALUE);
        btnSave.addActionListener(e -> performSave());
        btnDelete.addActionListener(e -> performDelete());

        btnBox1.add(btnSave);
        btnBox1.add(btnDelete);
        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

        // Nhóm nút Làm mới
        JPanel btnBox2 = new JPanel(new GridLayout(1, 1, 0, 0));
        btnBox2.setOpaque(false);
        btnBox2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClear = createStaticButton("🔄 Làm Mới Form", PINK_ACCENT, Integer.MAX_VALUE);
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
        JLabel lbl = createFieldLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
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

    private datepicker.DatePickerField addDateField(JPanel p, String labelText) {
        p.add(createFieldLabel(labelText));
        p.add(Box.createVerticalStrut(3));
        datepicker.DatePickerField field = new datepicker.DatePickerField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(field);
        p.add(Box.createVerticalStrut(10));
        return field;
    }
    private void performSave() {
        String id = txtPromoId.getText().trim();
        String name = txtPromoName.getText().trim();
        String discount = txtDiscount.getText().trim();
        String endDate = txtEndDate.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã khuyến mãi và Tên chương trình!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate until = UiKit.parseDate(endDate);
            if (until == null) throw new IllegalArgumentException("Vui lòng chọn ngày kết thúc.");
            double percent = Double.parseDouble(discount.replace("%", "").replace(",", ".").trim());
            if (percent < 0 || percent > 100) throw new NumberFormatException();
            LocalDate from = LocalDate.now();
            boolean active = true;
            if (isEditing) {
                for (KhuyenMai current : promotionDAO.getAll()) {
                    if (id.equals(current.getMaKhuyenMai())) {
                        if (current.getNgayBatDau() != null) from = new java.sql.Date(current.getNgayBatDau().getTime()).toLocalDate();
                        active = current.getTrangThai() == null || current.getTrangThai();
                        break;
                    }
                }
            }
            if (until.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải từ ngày bắt đầu trở đi.");
            KhuyenMai promotion = new KhuyenMai();
            promotion.setMaKhuyenMai(id);
            promotion.setTenKhuyenMai(name);
            promotion.setGiaTriGiam(percent);
            promotion.setNgayBatDau(java.sql.Date.valueOf(from));
            promotion.setNgayKetThuc(java.sql.Date.valueOf(until));
            promotion.setTrangThai(active);
            boolean saved = isEditing ? promotionDAO.update(promotion) : promotionDAO.insert(promotion);
            if (!saved) throw new SQLException("Không tìm thấy hoặc không lưu được khuyến mãi.");
            clearForm();
            loadDataFromDatabase();
            JOptionPane.showMessageDialog(this, "Đã lưu khuyến mãi vào SQL Server.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải đúng định dạng dd/MM/yyyy.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mức giảm phải là số từ 0 đến 100.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu khuyến mãi: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDelete() {
        int r = table.getSelectedRow();
        if (r != -1) {
            int modelRow = table.convertRowIndexToModel(r);
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khuyến mãi này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
                try {
                    if (!promotionDAO.delete(id)) throw new SQLException("Không tìm thấy khuyến mãi.");
                    clearForm();
                    loadDataFromDatabase();
                    JOptionPane.showMessageDialog(this, "Đã xóa khuyến mãi.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa khuyến mãi: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtPromoId.setText("");
        txtPromoId.setEnabled(true);
        txtPromoName.setText("");
        txtDiscount.setText("");
        txtEndDate.setText("");
        table.clearSelection();
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        List<KhuyenMai> promotions = promotionDAO.getAll();
        for (KhuyenMai promotion : promotions) {
            Date start = promotion.getNgayBatDau();
            Date end = promotion.getNgayKetThuc();
            String status = promotion.getTrangThai() != null && promotion.getTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng";
            tableModel.addRow(new Object[]{promotion.getMaKhuyenMai(), promotion.getTenKhuyenMai(),
                    String.valueOf(promotion.getGiaTriGiam()) + "%", start == null ? "" : UiKit.fmt(new java.sql.Date(start.getTime()).toLocalDate()),
                    end == null ? "" : UiKit.fmt(new java.sql.Date(end.getTime()).toLocalDate()), status});
        }
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
}
