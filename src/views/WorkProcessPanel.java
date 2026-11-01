package views;

import models.dao.LichLamViecDAO;
import models.entity.LichLamViec;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class WorkProcessPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch, txtShiftId, txtStaffName, txtDate;
    private JComboBox<String> cbShiftTime;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private final LichLamViecDAO scheduleDAO = new LichLamViecDAO();

    public WorkProcessPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Phân Ca Lịch Làm Việc");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);


        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        headerPanel.add(titleBox, BorderLayout.WEST);

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

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {"Mã Phân Ca", "Họ Tên Nhân Viên", "Ca Trực", "Ngày Trực", "Trạng Thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Lịch làm việc");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
                txtShiftId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
                txtShiftId.setEnabled(false);
                txtStaffName.setText(String.valueOf(tableModel.getValueAt(selectedRowIndex, 1)));
                cbShiftTime.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 2));
                txtDate.setText((String) tableModel.getValueAt(selectedRowIndex, 3));
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
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Phân Ca Xếp Lịch Trực");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        txtShiftId = addField(panel, "Mã Phân Ca:");
        txtStaffName = addField(panel, "Mã Nhân Viên:");

        JLabel lblShift = createFieldLabel("Chọn Ca Trực:");
        panel.add(lblShift);
        panel.add(Box.createVerticalStrut(3));
        cbShiftTime = new JComboBox<>(new String[]{"Ca Sáng (08:00 - 12:00)", "Ca Chiều (13:30 - 17:30)", "Ca Tối (18:00 - 21:00)"});
        cbShiftTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbShiftTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbShiftTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbShiftTime.setBackground(Color.WHITE);
        panel.add(cbShiftTime);
        panel.add(Box.createVerticalStrut(10));

        txtDate = addField(panel, "Ngày Trực (dd/MM/yyyy):");

        JPanel btnBox1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox1.setOpaque(false);
        btnBox1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", GREEN_ACCENT, Integer.MAX_VALUE);
        JButton btnDelete = createStaticButton("Xóa", RED_ACCENT, Integer.MAX_VALUE);
        btnSave.addActionListener(e -> performSave());
        btnDelete.addActionListener(e -> performDelete());

        btnBox1.add(btnSave);
        btnBox1.add(btnDelete);
        panel.add(btnBox1);
        panel.add(Box.createVerticalStrut(8));

        JPanel btnBox2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnBox2.setOpaque(false);
        btnBox2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnClear = createStaticButton("Làm Mới", PINK_ACCENT, Integer.MAX_VALUE);
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
        p.add(createFieldLabel(labelText));
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

    private void performSave() {
        String id = txtShiftId.getText().trim();
        String employeeId = txtStaffName.getText().trim();
        String shift = (String) cbShiftTime.getSelectedItem();
        String date = txtDate.getText().trim();

        if (id.isEmpty() || employeeId.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã lịch, mã nhân viên và ngày trực!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate workDate = UiKit.parseDate(date);
            if (workDate == null) throw new DateTimeParseException("empty", date, 0);
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\((\\d{2}:\\d{2})\\s*-\\s*(\\d{2}:\\d{2})\\)").matcher(shift);
            if (!matcher.find()) throw new IllegalArgumentException("Ca trực không hợp lệ.");
            LichLamViec schedule = new LichLamViec(id, employeeId, java.sql.Date.valueOf(workDate),
                    matcher.group(1), matcher.group(2), "Đã phân công");
            boolean saved = isEditing ? scheduleDAO.update(schedule) : scheduleDAO.insert(schedule);
            if (!saved) throw new SQLException("Không tìm thấy hoặc không lưu được lịch làm việc.");
            clearForm();
            loadDataFromDatabase();
            JOptionPane.showMessageDialog(this, "Đã lưu lịch làm việc vào SQL Server.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày trực phải đúng định dạng dd/MM/yyyy.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu lịch làm việc: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performDelete() {
        int r = table.getSelectedRow();
        if (r != -1) {
            int modelRow = table.convertRowIndexToModel(r);
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa ca trực này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
                try {
                    if (!scheduleDAO.delete(id)) throw new SQLException("Không tìm thấy lịch làm việc.");
                    clearForm();
                    loadDataFromDatabase();
                    JOptionPane.showMessageDialog(this, "Đã xóa lịch làm việc.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Không thể xóa lịch làm việc: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtShiftId.setText("");
        txtShiftId.setEnabled(true);
        txtStaffName.setText("");
        cbShiftTime.setSelectedIndex(0);
        txtDate.setText("");
        table.clearSelection();
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        rowSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        List<LichLamViec> schedules = scheduleDAO.getAllLichLamViec();
        for (LichLamViec schedule : schedules) {
            String shift = shiftLabel(schedule.getGioBatDau(), schedule.getGioKetThuc());
            tableModel.addRow(new Object[]{schedule.getMaLichLamViec(), schedule.getMaNhanVien(), shift,
                    schedule.getNgayLamViec() == null ? "" : UiKit.fmt(schedule.getNgayLamViec().toLocalDate()),
                    schedule.getTrangThai()});
        }
    }

    private String shiftLabel(String start, String end) {
        String shift = "(" + start + " - " + end + ")";
        for (int i = 0; i < cbShiftTime.getItemCount(); i++) {
            String option = cbShiftTime.getItemAt(i);
            if (option.contains(shift)) return option;
        }
        return "Ca " + shift;
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
