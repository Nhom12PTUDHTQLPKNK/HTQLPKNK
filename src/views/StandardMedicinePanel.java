package views;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import sql.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StandardMedicinePanel extends JPanel {

    // ================= MÀU SẮC =================
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color BLUE_DARK = new Color(0, 91, 190);
    private static final Color NAVY = new Color(15, 29, 69);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color BORDER = new Color(218, 230, 245);
    private static final Color TEXT = new Color(35, 55, 95);
    private static final Color MUTED = new Color(100, 116, 145);
    private static final Color LIGHT_BLUE = new Color(232, 243, 255);
    private static final Color GREEN = new Color(0, 166, 82);
    private static final Color GREEN_BG = new Color(222, 250, 235);
    private static final Color RED = new Color(225, 55, 85);
    private static final Color PINK = new Color(238, 65, 130);
    private static final Color PINK_BG = new Color(255, 239, 245);

    // ================= DỮ LIỆU =================
    private final List<MedicineData> medicines = new ArrayList<MedicineData>();
    private final List<MedicineData> filteredMedicines = new ArrayList<MedicineData>();

    private DefaultTableModel tableModel;
    private JTable medicineTable;
    private JComboBox<String> cbGroup, cbForm, cbRoute, cbStatus;
    private JComboBox<Integer> cbPageSize;
    private JTextField txtSearch;
    private JLabel lblMedicineCode, lblMedicineName, lblActiveIngredient, lblStatus, lblImage, lblRowCount, lblPage;
    private JLabel[] generalValues, dosageValues;
    private JTextArea txtDescription, txtDosageNote, txtIndication, txtCaution;
    private JButton btnPrevious, btnNext;
    private int currentPage = 1, pageSize = 16;
    private MedicineData selectedMedicine;

    // ================= CONSTRUCTOR =================
    public StandardMedicinePanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(createFilterCard(), BorderLayout.NORTH);
        content.add(createMainContent(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // Tải dữ liệu trực tiếp từ SQL Server khi khởi tạo
        loadMedicinesFromDatabase();
        applyFilter();
    }

    // ================= HEADER =================
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 62));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("", SwingConstants.CENTER);
        ImageIcon logoIcon = loadImage("/img/medicine-icon.png", 48, 48);
        if (logoIcon != null) {
            logo.setIcon(logoIcon);
        } else {
            logo.setText("✚");
            logo.setFont(new Font("Segoe UI Symbol", Font.BOLD, 30));
            logo.setForeground(PINK);
        }
        logo.setOpaque(true);
        logo.setBackground(PINK_BG);
        logo.setPreferredSize(new Dimension(56, 56));
        logo.setBorder(BorderFactory.createLineBorder(new Color(250, 210, 226)));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Danh mục thuốc chuẩn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel("Quản lý danh mục thuốc sử dụng trong điều trị nha khoa");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        left.add(logo);
        left.add(titlePanel);

        JLabel breadcrumb = new JLabel("⌂   |   Danh mục   ›   Thuốc chuẩn");
        breadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        breadcrumb.setForeground(MUTED);

        header.add(left, BorderLayout.WEST);
        header.add(breadcrumb, BorderLayout.EAST);
        return header;
    }

    // ================= FILTER =================
    private JPanel createFilterCard() {
        JPanel card = createCard();
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);

        cbGroup = new JComboBox<String>(new String[]{
        	    "Tất cả",
        	    "Kháng sinh",
        	    "Giảm đau - Hạ sốt",
        	    "Kháng viêm",
        	    "Sát khuẩn",
        	    "Gây tê nha khoa",
        	    "Corticosteroid"
        	});
        
        cbForm = new JComboBox<String>(new String[]{
        	    "Tất cả",
        	    "Viên nén",
        	    "Viên nang",
        	    "Dung dịch",
        	    "Gel bôi",
        	    "Ống tiêm",
        	    "Viên sủi"
        	});
        cbRoute = new JComboBox<String>(new String[]{
        	    "Tất cả",
        	    "Đường uống",
        	    "Súc miệng",
        	    "Dùng ngoài",
        	    "Tiêm",
        	    "Bôi tại chỗ"
        	});
        
        cbStatus = new JComboBox<String>(new String[]{"Tất cả", "Đang kinh doanh", "Ngừng kinh doanh"});

        styleCombo(cbGroup);
        styleCombo(cbForm);
        styleCombo(cbRoute);
        styleCombo(cbStatus);

        JPanel groupField = createLabeledField("Nhóm thuốc", cbGroup);
        JPanel formField = createLabeledField("Dạng bào chế", cbForm);
        JPanel routeField = createLabeledField("Đường dùng", cbRoute);
        JPanel statusField = createLabeledField("Trạng thái", cbStatus);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(new EmptyBorder(0, 8, 0, 8));

        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(0, 8, 0, 8)));
        searchBox.setPreferredSize(new Dimension(230, 40));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        searchIcon.setForeground(MUTED);

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JButton btnSearch = createButton("⌕  Tìm kiếm", true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 10);

        gbc.gridx = 0; gbc.weightx = 1; filters.add(groupField, gbc);
        gbc.gridx = 1; filters.add(formField, gbc);
        gbc.gridx = 2; filters.add(routeField, gbc);
        gbc.gridx = 3; filters.add(statusField, gbc);
        gbc.gridx = 4; gbc.weightx = 1.2; filters.add(searchBox, gbc);
        gbc.gridx = 5; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 0); filters.add(btnSearch, gbc);

        card.add(filters, BorderLayout.CENTER);

        cbGroup.addActionListener(e -> applyFilter());
        cbForm.addActionListener(e -> applyFilter());
        cbRoute.addActionListener(e -> applyFilter());
        cbStatus.addActionListener(e -> applyFilter());
        btnSearch.addActionListener(e -> applyFilter());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        return card;
    }

    // ================= MAIN CONTENT =================
    private JPanel createMainContent() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridx = 0; gbc.weightx = 0.67; gbc.insets = new Insets(0, 0, 0, 9);
        main.add(createTableArea(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.33; gbc.insets = new Insets(0, 0, 0, 0);
        main.add(createDetailArea(), gbc);

        return main;
    }

    // ================= TABLE AREA =================
    private JPanel createTableArea() {
        JPanel card = createCard();

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.setOpaque(false);

        JButton btnAdd = createButton("+  Thêm thuốc", true);
        JButton btnEdit = createButton("✎  Sửa", false);
        JButton btnDelete = createDeleteButton("▤  Xóa");
        JButton btnRefresh = createButton("↻  Làm mới", false);

        btnAdd.setPreferredSize(new Dimension(145, 42));
        btnEdit.setPreferredSize(new Dimension(105, 42));
        btnDelete.setPreferredSize(new Dimension(105, 42));
        btnRefresh.setPreferredSize(new Dimension(125, 42));

        actions.add(btnAdd);
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);
        top.add(actions, BorderLayout.NORTH);

        String[] columns = {"Mã thuốc", "Tên thuốc", "Hoạt chất chính", "Nhóm thuốc", "Dạng bào chế", "Đường dùng", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        medicineTable = new JTable(tableModel);
        styleTable(medicineTable);

        int[] widths = {85, 155, 125, 145, 110, 105, 120};
        for (int i = 0; i < widths.length; i++) {
            medicineTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(medicineTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableBody = new JPanel(new BorderLayout(0, 10));
        tableBody.setOpaque(false);
        tableBody.add(top, BorderLayout.NORTH);
        tableBody.add(scroll, BorderLayout.CENTER);

        card.add(tableBody, BorderLayout.CENTER);
        card.add(createPaginationPanel(), BorderLayout.SOUTH);

        medicineTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = medicineTable.getSelectedRow();
            if (row < 0) { clearDetail(); return; }
            int modelRow = medicineTable.convertRowIndexToModel(row);
            String code = String.valueOf(tableModel.getValueAt(modelRow, 0));
            MedicineData medicine = findMedicine(code);
            if (medicine != null) showMedicine(medicine);
        });

        // Xử lý sự kiện nút Thêm thuốc
        btnAdd.addActionListener(e -> showNotConnectedMessage("Thêm thuốc"));

        // Xử lý sự kiện nút Sửa thuốc
        btnEdit.addActionListener(e -> {
            if (selectedMedicine == null) showSelectMessage();
            else showNotConnectedMessage("Sửa thuốc");
        });

        // Xử lý sự kiện nút Xóa thuốc (Đã tích hợp mẫu lệnh SQL DELETE trực tiếp nếu muốn dùng)
        btnDelete.addActionListener(e -> {
            if (selectedMedicine == null) { 
                showSelectMessage(); 
                return; 
            }
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Bạn có chắc chắn muốn xóa thuốc " + selectedMedicine.name + "?", 
                "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM THUOC WHERE MaThuoc = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, selectedMedicine.code);
                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        JOptionPane.showMessageDialog(this, "Xóa thuốc thành công!");
                        loadMedicinesFromDatabase();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Không thể xóa thuốc này vì đang được sử dụng ở bảng khác!", "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> {
            cbGroup.setSelectedIndex(0); cbForm.setSelectedIndex(0); cbRoute.setSelectedIndex(0); cbStatus.setSelectedIndex(0);
            txtSearch.setText(""); currentPage = 1; medicineTable.clearSelection(); clearDetail();
            loadMedicinesFromDatabase();
        });

        return card;
    }

    // ================= PAGINATION =================
    private JPanel createPaginationPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 2, 0, 2));

        lblRowCount = new JLabel("Hiển thị 0 thuốc");
        lblRowCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRowCount.setForeground(MUTED);

        JPanel pages = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pages.setOpaque(false);

        btnPrevious = createPageButton("‹", false);
        lblPage = new JLabel("Trang 1/1");
        lblPage.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPage.setForeground(NAVY);
        btnNext = createPageButton("›", false);

        pages.add(btnPrevious);
        pages.add(lblPage);
        pages.add(btnNext);

        JLabel pageSizeLabel = new JLabel("Số dòng/trang:");
        pageSizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pageSizeLabel.setForeground(MUTED);

        cbPageSize = new JComboBox<Integer>(new Integer[]{10, 16, 25, 50});
        cbPageSize.setSelectedItem(16);
        styleCombo(cbPageSize);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        right.setOpaque(false);
        right.add(pageSizeLabel);
        right.add(cbPageSize);

        bottom.add(lblRowCount, BorderLayout.WEST);
        bottom.add(pages, BorderLayout.CENTER);
        bottom.add(right, BorderLayout.EAST);

        btnPrevious.addActionListener(e -> { if (currentPage > 1) { currentPage--; updateTable(); } });
        btnNext.addActionListener(e -> { if (currentPage < getTotalPages()) { currentPage++; updateTable(); } });
        cbPageSize.addActionListener(e -> { pageSize = (Integer) cbPageSize.getSelectedItem(); currentPage = 1; updateTable(); });

        return bottom;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil(filteredMedicines.size() / (double) pageSize));
    }

    // ================= DETAIL AREA =================
    private JPanel createDetailArea() {
        JPanel card = createCard();

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JLabel title = new JLabel("Thông tin thuốc");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(NAVY);

        JButton btnEdit = createButton("✎  Sửa", false);
        btnEdit.addActionListener(e -> {
            if (selectedMedicine == null) showSelectMessage();
            else showNotConnectedMessage("Sửa thuốc");
        });

        heading.add(title, BorderLayout.WEST);
        heading.add(btnEdit, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(createMedicineSummary());
        body.add(Box.createVerticalStrut(12));
        body.add(createTabs());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(15);
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ================= MEDICINE SUMMARY =================
    private JPanel createMedicineSummary() {
        JPanel summary = new JPanel(new BorderLayout(12, 0));
        summary.setOpaque(false);
        summary.setPreferredSize(new Dimension(0, 150));
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblImage = new JLabel("", SwingConstants.CENTER);
        lblImage.setOpaque(true);
        lblImage.setBackground(new Color(247, 250, 255));
        lblImage.setBorder(BorderFactory.createLineBorder(BORDER));
        lblImage.setPreferredSize(new Dimension(155, 140));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        lblMedicineCode = new JLabel("Mã thuốc: --");
        lblMedicineCode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMedicineCode.setForeground(MUTED);
        lblMedicineCode.setOpaque(true);
        lblMedicineCode.setBackground(LIGHT_BLUE);
        lblMedicineCode.setBorder(new EmptyBorder(6, 9, 6, 9));

        lblMedicineName = new JLabel("Chưa chọn thuốc");
        lblMedicineName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMedicineName.setForeground(BLUE_DARK);

        lblActiveIngredient = new JLabel("Hoạt chất: --");
        lblActiveIngredient.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblActiveIngredient.setForeground(TEXT);

        lblStatus = new JLabel("Chưa chọn thuốc");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(MUTED);
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(242, 245, 249));
        lblStatus.setBorder(new EmptyBorder(5, 9, 5, 9));

        info.add(lblMedicineCode);
        info.add(Box.createVerticalStrut(8));
        info.add(lblMedicineName);
        info.add(Box.createVerticalStrut(7));
        info.add(lblActiveIngredient);
        info.add(Box.createVerticalStrut(9));
        info.add(lblStatus);

        summary.add(lblImage, BorderLayout.WEST);
        summary.add(info, BorderLayout.CENTER);
        return summary;
    }

    // ================= TABS =================
    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabs.setForeground(NAVY);
        tabs.setBackground(Color.WHITE);
        tabs.addTab("▦ Thông tin chung", createGeneralTab());
        tabs.addTab("◇ Liều dùng", createDosageTab());
        tabs.addTab("▤ Chỉ định", createIndicationTab());
        tabs.addTab("ⓘ Lưu ý", createCautionTab());
        tabs.setPreferredSize(new Dimension(0, 440));
        tabs.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tabs;
    }

    // ================= GENERAL TAB =================
    private JPanel createGeneralTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 2, 4, 2));

        String[] names = {"Mã thuốc", "Tên thuốc", "Hoạt chất chính", "Nhóm thuốc", "Dạng bào chế", "Đường dùng", "Nhà sản xuất", "Nước sản xuất", "Quy cách đóng gói", "Trạng thái"};
        generalValues = new JLabel[names.length];
        panel.add(createInfoGrid(names, generalValues), BorderLayout.NORTH);

        JPanel description = createTextInfoBox("Mô tả", 105);
        txtDescription = getTextAreaFromBox(description);
        panel.add(description, BorderLayout.CENTER);
        return panel;
    }

    // ================= DOSAGE TAB =================
    private JPanel createDosageTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 2, 4, 2));

        String[] names = {"Liều dùng người lớn", "Liều dùng trẻ em", "Số lần sử dụng", "Thời gian sử dụng"};
        dosageValues = new JLabel[names.length];
        panel.add(createInfoGrid(names, dosageValues), BorderLayout.NORTH);

        JPanel note = createTextInfoBox("Hướng dẫn sử dụng", 160);
        txtDosageNote = getTextAreaFromBox(note);
        panel.add(note, BorderLayout.CENTER);
        return panel;
    }

    // ================= INDICATION TAB =================
    private JPanel createIndicationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 2, 4, 2));

        JPanel box = createTextInfoBox("Thông tin chỉ định", 240);
        txtIndication = getTextAreaFromBox(box);
        panel.add(box, BorderLayout.NORTH);
        return panel;
    }

    // ================= CAUTION TAB =================
    private JPanel createCautionTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 2, 4, 2));

        JPanel box = createTextInfoBox("Chống chỉ định và lưu ý", 240);
        txtCaution = getTextAreaFromBox(box);
        panel.add(box, BorderLayout.NORTH);
        return panel;
    }

    // ================= INFORMATION GRID =================
    private JPanel createInfoGrid(String[] names, JLabel[] values) {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createLineBorder(BORDER));

        for (int i = 0; i < names.length; i++) {
            JLabel name = new JLabel(names[i]);
            name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            name.setForeground(MUTED);
            name.setOpaque(true);
            name.setBackground(new Color(246, 249, 253));
            name.setBorder(new EmptyBorder(7, 10, 7, 5));

            values[i] = new JLabel("--");
            values[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            values[i].setForeground(TEXT);
            values[i].setBorder(new EmptyBorder(7, 10, 7, 7));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1;
            gbc.gridx = 0; gbc.weightx = 0.38; gbc.insets = new Insets(0, 0, 1, 1);
            grid.add(name, gbc);

            gbc.gridx = 1; gbc.weightx = 0.62; gbc.insets = new Insets(0, 0, 1, 0);
            grid.add(values[i], gbc);
        }
        return grid;
    }

    // ================= TEXT INFORMATION BOX =================
    private JPanel createTextInfoBox(String title, int height) {
        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.setBackground(new Color(247, 250, 255));
        box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 11, 10, 11)));
        box.setPreferredSize(new Dimension(0, height));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        JLabel heading = new JLabel("▏ " + title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(NAVY);

        JTextArea area = new JTextArea();
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setForeground(TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        box.add(heading, BorderLayout.NORTH);
        box.add(scroll, BorderLayout.CENTER);
        return box;
    }

    private JTextArea getTextAreaFromBox(JPanel box) {
        JScrollPane scroll = (JScrollPane) box.getComponent(1);
        return (JTextArea) scroll.getViewport().getView();
    }

    // ================= FILTER AND SEARCH =================
    private void applyFilter() {
        if (tableModel == null) return;

        String group = String.valueOf(cbGroup.getSelectedItem());
        String form = String.valueOf(cbForm.getSelectedItem());
        String route = String.valueOf(cbRoute.getSelectedItem());
        String status = String.valueOf(cbStatus.getSelectedItem());
        String keyword = txtSearch.getText().trim().toLowerCase();

        filteredMedicines.clear();
        for (MedicineData m : medicines) {
            if (!"Tất cả".equals(group) && !group.equals(m.group)) continue;
            if (!"Tất cả".equals(form) && !form.equals(m.form)) continue;
            if (!"Tất cả".equals(route) && !route.equals(m.route)) continue;
            if (!"Tất cả".equals(status) && !status.equals(m.status)) continue;

            String searchData = (safe(m.code) + " " + safe(m.name) + " " + safe(m.activeIngredient)).toLowerCase();
            if (!keyword.isEmpty() && !searchData.contains(keyword)) continue;

            filteredMedicines.add(m);
        }

        currentPage = 1;
        updateTable();
    }

    // ================= UPDATE TABLE AND PAGINATION =================
    private void updateTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        int total = filteredMedicines.size();
        int totalPages = getTotalPages();
        if (currentPage > totalPages) currentPage = totalPages;

        // Đã sửa lại lỗi cú pháp ngắt dòng ở đây
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        for (int i = start; i < end; i++) {
            MedicineData m = filteredMedicines.get(i);
            tableModel.addRow(new Object[]{m.code, m.name, m.activeIngredient, m.group, m.form, m.route, m.status});
        }

        lblPage.setText("Trang " + currentPage + "/" + totalPages);
        lblRowCount.setText(total == 0 ? "Hiển thị 0 thuốc trong tổng số 0 thuốc" : "Hiển thị " + (start + 1) + " - " + end + " trong tổng số " + total + " thuốc");
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }

    // ================= KẾT NỐI VÀ LẤY DỮ LIỆU TỪ SQL SERVER =================
    public void loadMedicinesFromDatabase() {
        medicines.clear();

        String query =
            "SELECT " +
            "T.MaThuoc, T.TenThuoc, T.HoatChatChinh, " +
            "N.TenNhomThuoc, DBC.TenDangBaoChe, DD.TenDuongDung, " +
            "T.NhaSanXuat, T.NuocSanXuat, T.QuyCachDongGoi, " +
            "T.MoTa, T.HinhAnh, T.TrangThai, " +

            "LD.DoiTuong, LD.LieuDung, LD.TanSuat, " +
            "LD.ThoiGianSuDung, LD.HuongDanSuDung, " +

            "CD.NoiDungChiDinh, " +
            "LY.LoaiLuuY, LY.NoiDung AS NoiDungLuuY " +

            "FROM THUOC T " +

            "LEFT JOIN NHOMTHUOC N " +
            "ON T.MaNhomThuoc = N.MaNhomThuoc " +

            "LEFT JOIN DANGBAOCHE DBC " +
            "ON T.MaDangBaoChe = DBC.MaDangBaoChe " +

            "LEFT JOIN DUONGDUNG DD " +
            "ON T.MaDuongDung = DD.MaDuongDung " +

            "LEFT JOIN THUOCLIEUDUNG LD " +
            "ON T.MaThuoc = LD.MaThuoc " +

            "LEFT JOIN THUOCCHIDINH CD " +
            "ON T.MaThuoc = CD.MaThuoc " +

            "LEFT JOIN THUOCLUUY LY " +
            "ON T.MaThuoc = LY.MaThuoc " +

            "ORDER BY T.MaThuoc";

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()
        ) {

            Map<String, MedicineData> medicineMap =
                new LinkedHashMap<String, MedicineData>();

            while (rs.next()) {

                String code = rs.getString("MaThuoc");

                MedicineData m = medicineMap.get(code);

                if (m == null) {

                    m = new MedicineData();

                    m.code = code;
                    m.name = rs.getString("TenThuoc");
                    m.activeIngredient = rs.getString("HoatChatChinh");

                    m.group = rs.getString("TenNhomThuoc");
                    m.form = rs.getString("TenDangBaoChe");
                    m.route = rs.getString("TenDuongDung");

                    m.manufacturer = rs.getString("NhaSanXuat");
                    m.country = rs.getString("NuocSanXuat");
                    m.packageInfo = rs.getString("QuyCachDongGoi");

                    m.description = rs.getString("MoTa");
                    m.imagePath = rs.getString("HinhAnh");

                    m.status = rs.getBoolean("TrangThai")
                        ? "Đang kinh doanh"
                        : "Ngừng kinh doanh";

                    m.adultDosage = "";
                    m.childDosage = "";
                    m.frequency = "";
                    m.duration = "";
                    m.dosageNote = "";
                    m.indication = "";
                    m.caution = "";

                    medicineMap.put(code, m);
                }

                // LIỀU DÙNG
                String doiTuong = rs.getString("DoiTuong");
                String lieuDung = rs.getString("LieuDung");

                if (doiTuong != null) {
                    if (doiTuong.toLowerCase().contains("trẻ em")) {
                        m.childDosage = appendText(m.childDosage, lieuDung);
                    } else {
                        m.adultDosage = appendText(m.adultDosage, lieuDung);
                    }
                }

                m.frequency = appendText(m.frequency, rs.getString("TanSuat"));
                m.duration = appendText(m.duration, rs.getString("ThoiGianSuDung"));
                m.dosageNote = appendText(m.dosageNote, rs.getString("HuongDanSuDung"));

                // CHỈ ĐỊNH
                m.indication = appendText(m.indication, rs.getString("NoiDungChiDinh"));

                // LƯU Ý VÀ CHỐNG CHỈ ĐỊNH
                String loaiLuuY = rs.getString("LoaiLuuY");
                String noiDung = rs.getString("NoiDungLuuY");

                if (noiDung != null) {
                    m.caution = appendText(
                        m.caution,
                        (loaiLuuY == null ? "" : loaiLuuY + ": ") + noiDung
                    );
                }
            }

            medicines.addAll(medicineMap.values());

            System.out.println("Đã tải thành công " + medicines.size() + " thuốc từ SQL Server!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Lỗi tải dữ liệu thuốc: " + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }

        if (medicineTable != null) {
            medicineTable.clearSelection();
        }

        clearDetail();
        applyFilter();
    }

    private String appendText(String oldText, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            return oldText;
        }
        if (oldText == null || oldText.trim().isEmpty()) {
            return newText;
        }
        if (oldText.contains(newText)) {
            return oldText;
        }
        return oldText + "\n" + newText;
    }

    // ================= FIND MEDICINE =================
    private MedicineData findMedicine(String code) {
        for (MedicineData m : medicines) {
            if (safe(m.code).equals(code)) return m;
        }
        return null;
    }

    // ================= DISPLAY MEDICINE DETAIL =================
    private void showMedicine(MedicineData m) {
        selectedMedicine = m;

        lblMedicineCode.setText("Mã thuốc: " + safe(m.code));
        lblMedicineName.setText(safe(m.name));
        lblActiveIngredient.setText("Hoạt chất: " + safe(m.activeIngredient));

        boolean active = "Đang kinh doanh".equalsIgnoreCase(m.status);
        lblStatus.setText(active ? "● Đang kinh doanh" : "● Ngừng kinh doanh");
        lblStatus.setForeground(active ? GREEN : RED);
        lblStatus.setBackground(active ? GREEN_BG : PINK_BG);

        String[] general = {safe(m.code), safe(m.name), safe(m.activeIngredient), safe(m.group), safe(m.form), safe(m.route), safe(m.manufacturer), safe(m.country), safe(m.packageInfo), safe(m.status)};
        for (int i = 0; i < general.length; i++) generalValues[i].setText(general[i]);

        String[] dosage = {safe(m.adultDosage), safe(m.childDosage), safe(m.frequency), safe(m.duration)};
        for (int i = 0; i < dosage.length; i++) dosageValues[i].setText(dosage[i]);

        txtDescription.setText(safe(m.description));
        txtDosageNote.setText(safe(m.dosageNote));
        txtIndication.setText(safe(m.indication));
        txtCaution.setText(safe(m.caution));

        lblImage.setIcon(null);
        lblImage.setText("");
        if (m.imagePath != null && !m.imagePath.trim().isEmpty()) {
            ImageIcon icon = loadImage(m.imagePath, 145, 130);
            if (icon != null) lblImage.setIcon(icon);
        }
    }

    // ================= CLEAR DETAIL =================
    private void clearDetail() {
        selectedMedicine = null;
        if (lblMedicineCode == null) return;

        lblMedicineCode.setText("Mã thuốc: --");
        lblMedicineName.setText("Chưa chọn thuốc");
        lblActiveIngredient.setText("Hoạt chất: --");
        lblStatus.setText("Chưa chọn thuốc");
        lblStatus.setForeground(MUTED);
        lblStatus.setBackground(new Color(242, 245, 249));

        for (JLabel label : generalValues) label.setText("--");
        for (JLabel label : dosageValues) label.setText("--");

        txtDescription.setText("");
        txtDosageNote.setText("");
        txtIndication.setText("");
        txtCaution.setText("");
        lblImage.setIcon(null);
        lblImage.setText("");
    }

    // ================= MEDICINE DATA MODEL =================
    public static class MedicineData {
        public String code, name, activeIngredient, group, form, route, status;
        public String manufacturer, country, packageInfo;
        public String description;
        public String adultDosage, childDosage, frequency, duration, dosageNote;
        public String indication, caution;
        public String imagePath;

        public MedicineData() {}

        public MedicineData(String code, String name, String activeIngredient, String group, String form, String route, String status) {
            this.code = code;
            this.name = name;
            this.activeIngredient = activeIngredient;
            this.group = group;
            this.form = form;
            this.route = route;
            this.status = status;
        }
    }

    // ================= LOAD IMAGE =================
    private ImageIcon loadImage(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);
            ImageIcon original;
            if (url != null) {
                original = new ImageIcon(url);
            } else {
                java.io.File file = new java.io.File(path);
                if (!file.exists()) return null;
                original = new ImageIcon(path);
            }
            Image image = original.getImage();
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            System.out.println("Không tải được ảnh: " + path);
            return null;
        }
    }

    // ================= UI HELPERS =================
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 13, 12, 13)));
        return card;
    }

    private JPanel createLabeledField(String title, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT);
        field.setPreferredSize(new Dimension(100, 39));
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(120, 39));
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? BLUE : BORDER), new EmptyBorder(7, 10, 7, 10)));
        return button;
    }

    private JButton createDeleteButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(PINK_BG);
        button.setForeground(RED);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(250, 210, 226)), new EmptyBorder(7, 10, 7, 10)));
        return button;
    }

    private JButton createPageButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(36, 32));
        button.setBackground(selected ? BLUE : Color.WHITE);
        button.setForeground(selected ? Color.WHITE : NAVY);
        button.setBorder(BorderFactory.createLineBorder(BORDER));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(210, 232, 255));
        table.setSelectionForeground(NAVY);
        table.setGridColor(new Color(224, 234, 246));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(NAVY);
        header.setBackground(new Color(235, 244, 255));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
    }

    // ================= MESSAGES =================
    private void showSelectMessage() {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một thuốc trong danh sách.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showNotConnectedMessage(String action) {
        JOptionPane.showMessageDialog(this, action + " cần được kết nối với DAO và SQL Server.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}