package views;

import models.dao.HoSoBenhAnDAO;
import models.entity.HoSoBenhAn;
import controllers.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MedicalRecordPanel extends JPanel {
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
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;

    // Các thành phần input bên phải
    private JTextField txtRecordId, txtCustName, txtDiagnosis;
    private JComboBox<DoctorOption> cbDoctor;
    private JComboBox<String> cbStatus;
    private JButton btnSave, btnClear;

    private boolean isEditing = false;
    private int selectedRowIndex = -1;
    private final HoSoBenhAnDAO medicalRecordDAO = new HoSoBenhAnDAO();

    public MedicalRecordPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Header & Thanh tìm kiếm
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Hồ Sơ Bệnh Án");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý chẩn đoán, lịch sử điều trị và phác đồ nha khoa của bệnh nhân");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

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

        JButton btnEdit = createStaticButton("Sửa", GREEN_ACCENT, 75);
        // Bấm nút Sửa sẽ nạp dữ liệu từ bảng lên form input
        btnEdit.addActionListener(e -> loadDataToFormForEdit());

        JButton btnDelete = createStaticButton("Xóa", RED_ACCENT, 75);
        btnDelete.addActionListener(e -> performDelete());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(Box.createHorizontalStrut(5));
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Khu vực trung tâm (Bảng bên trái + Form bên phải)
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] columns = {"Mã HS", "Mã khách hàng", "Chẩn đoán", "Nha sĩ", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        PdfTableExport.install(table, "Hồ sơ bệnh án");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        // Lắng nghe sự kiện click chuột trên bảng để tự động điền dữ liệu lên form input bên phải ngay lập tức
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDataToFormForEdit();
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                if ("Hoàn thành".equals(value)) {
                    c.setForeground(new Color(40, 167, 69));
                } else {
                    c.setForeground(BLUE_PRIMARY);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        centerContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel formPanel = createInlineFormPanel();
        centerContainer.add(formPanel, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
        loadDataFromDatabase();
    }

    private JPanel createInlineFormPanel() {
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
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblFormTitle = new JLabel("Thông Tin Bệnh Án");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(lblFormTitle);
        panel.add(Box.createVerticalStrut(14));

        txtRecordId = addFieldToForm(panel, "Mã hồ sơ:");
        txtRecordId.setEditable(false);
        txtRecordId.setBackground(new Color(240, 244, 248));
        txtCustName = addFieldToForm(panel, "Mã khách hàng:");
        txtDiagnosis = addFieldToForm(panel, "Chẩn đoán:");
        JLabel lblDoctor = createFieldLabel("Nha sĩ phụ trách:");
        panel.add(lblDoctor);
        panel.add(Box.createVerticalStrut(4));
        cbDoctor = new JComboBox<>();
        cbDoctor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDoctor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbDoctor.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDoctor.setBackground(Color.WHITE);
        panel.add(cbDoctor);
        panel.add(Box.createVerticalStrut(12));

        JLabel lblStat = createFieldLabel("Trạng thái:");
        panel.add(lblStat);
        panel.add(Box.createVerticalStrut(4));
        cbStatus = new JComboBox<>(new String[]{"Đang điều trị", "Hoàn thành"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setBackground(Color.WHITE);
        panel.add(cbStatus);
        panel.add(Box.createVerticalStrut(20));

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> performSave());
        btnClear.addActionListener(e -> clearForm());

        btnBox.add(btnSave);
        btnBox.add(btnClear);
        panel.add(btnBox);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToForm(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
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

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtRecordId.setText("");
        txtRecordId.setEnabled(false);
        txtCustName.setText("");
        txtDiagnosis.setText("");
        if (cbDoctor.getItemCount() > 0) cbDoctor.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
        txtSearch.setText("");
        rowSorter.setRowFilter(null);
    }

    private void loadDataToFormForEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) return;
        selectedRowIndex = table.convertRowIndexToModel(viewRow);
        txtRecordId.setText((String) tableModel.getValueAt(selectedRowIndex, 0));
        txtRecordId.setEnabled(false); // Khóa mã khi đang sửa
        txtCustName.setText((String) tableModel.getValueAt(selectedRowIndex, 1));
        txtDiagnosis.setText((String) tableModel.getValueAt(selectedRowIndex, 2));
        String recordId = (String) tableModel.getValueAt(selectedRowIndex, 0);
        for (HoSoBenhAn record : medicalRecordDAO.getAll()) {
            if (recordId.equals(record.getMaBenhAn())) {
                selectDoctor(record.getMaNhanVien());
                break;
            }
        }
        cbStatus.setSelectedItem(tableModel.getValueAt(selectedRowIndex, 4));
        isEditing = true;
    }

    private void performSave() {
        String id = txtRecordId.getText().trim();
        String name = txtCustName.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        DoctorOption selectedDoctor = (DoctorOption) cbDoctor.getSelectedItem();
        String doctor = selectedDoctor == null ? "" : selectedDoctor.id;
        String status = (String) cbStatus.getSelectedItem();

        if (!isEditing) {
            try { id = medicalRecordDAO.getNextMaBenhAn(); }
            catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể tạo mã hồ sơ: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (!name.matches("KH\\d{3}")) {
            JOptionPane.showMessageDialog(this,
                    "Mã khách hàng không đúng định dạng.\n"
                    + "Hướng dẫn: nhập KH theo sau bởi đúng 3 chữ số, ví dụ KH001.",
                    "Sai định dạng", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(this, "Chưa có nhân viên nào mang vai trò nha sĩ. Vui lòng cập nhật nhân viên trước.", "Thiếu nha sĩ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!medicalRecordDAO.customerExists(name)) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng " + name + " trong hệ thống.", "Không tìm thấy khách hàng", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date examDate = new Date();
            String examResult = "";
            if (isEditing) {
                for (HoSoBenhAn current : medicalRecordDAO.getAll()) {
                    if (id.equals(current.getMaBenhAn())) {
                        if (current.getNgayKham() != null) examDate = current.getNgayKham();
                        examResult = current.getKetQuaKham();
                        break;
                    }
                }
            }
            HoSoBenhAn record = new HoSoBenhAn(id, name, doctor, examDate, examResult, diagnosis, status);
            boolean saved = isEditing ? medicalRecordDAO.update(record) : medicalRecordDAO.insert(record);
            if (!saved) throw new SQLException("Bản ghi không tồn tại hoặc không được lưu.");
            clearForm();
            loadDataFromDatabase();
            JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ bệnh án vào SQL Server.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu hồ sơ bệnh án: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDelete() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
            try {
                if (!medicalRecordDAO.delete(id)) throw new SQLException("Không tìm thấy hồ sơ bệnh án.");
                clearForm();
                loadDataFromDatabase();
                JOptionPane.showMessageDialog(this, "Đã xóa hồ sơ bệnh án.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa hồ sơ bệnh án: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        List<HoSoBenhAn> records = medicalRecordDAO.getAll();
        for (HoSoBenhAn record : records) {
            tableModel.addRow(new Object[]{record.getMaBenhAn(), record.getMaKhachHang(), record.getChuanDoan(),
                    record.getTenNhanVien() == null ? record.getMaNhanVien() : record.getTenNhanVien(), record.getTrangThai()});
        }
        refreshDentistOptions();
        if (!isEditing) setNextRecordId();
    }

    private void refreshDentistOptions() {
        String selectedId = cbDoctor == null || cbDoctor.getSelectedItem() == null ? null : ((DoctorOption) cbDoctor.getSelectedItem()).id;
        cbDoctor.removeAllItems();
        try {
            for (String[] dentist : medicalRecordDAO.getDentists()) cbDoctor.addItem(new DoctorOption(dentist[0], dentist[1]));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách nha sĩ: " + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
        if (selectedId != null) selectDoctor(selectedId);
    }

    private void selectDoctor(String id) {
        for (int i = 0; i < cbDoctor.getItemCount(); i++) {
            if (cbDoctor.getItemAt(i).id.equals(id)) { cbDoctor.setSelectedIndex(i); return; }
        }
    }

    private void setNextRecordId() {
        try { txtRecordId.setText(medicalRecordDAO.getNextMaBenhAn()); }
        catch (SQLException ex) { txtRecordId.setText("Tự tạo khi lưu"); }
    }

    private static class DoctorOption {
        private final String id;
        private final String name;
        private DoctorOption(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }
}
