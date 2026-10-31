package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StandardMedicinePanel extends JPanel {

    private JTable tblMedicine;
    private DefaultTableModel tableModel;
    private JTextField txtSearch, txtMaThuoc, txtTenThuoc, txtHoatChat, txtNhaSanXuat, txtNuocSanXuat, txtQuyCach;
    private JComboBox<String> cbNhomThuoc, cbDangBaoChe, cbDuongDung, cbDonViTinh, cbTrangThai;
    private JTextArea txtMoTa, txtLiieuDung, txtChiDinh, txtLuuY;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;

    public StandardMedicinePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlFilter.setBackground(new Color(245, 247, 250));
        pnlFilter.setBorder(BorderFactory.createTitledBorder("Bộ lọc danh mục thuốc"));

        pnlFilter.add(new JLabel("Nhóm thuốc:"));
        cbNhomThuoc = new JComboBox<>(new String[]{"Tất cả", "Kháng sinh", "Giảm đau - Hạ sốt", "Kháng viêm", "Sát khuẩn", "Corticosteroid", "Gây tê nha khoa"});
        pnlFilter.add(cbNhomThuoc);

        pnlFilter.add(new JLabel("Dạng bào chế:"));
        cbDangBaoChe = new JComboBox<>(new String[]{"Tất cả", "Viên nén", "Viên nang", "Dung dịch", "Ống tiêm", "Gel bôi"});
        pnlFilter.add(cbDangBaoChe);

        pnlFilter.add(new JLabel("Đường dùng:"));
        cbDuongDung = new JComboBox<>(new String[]{"Tất cả", "Đường uống", "Súc miệng", "Dùng ngoài", "Tiêm", "Bôi tại chỗ"});
        pnlFilter.add(cbDuongDung);

        pnlFilter.add(new JLabel("Trạng thái:"));
        cbTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang kinh doanh", "Ngừng kinh doanh"});
        pnlFilter.add(cbTrangThai);

        txtSearch = new JTextField(15);
        pnlFilter.add(new JLabel("Tìm kiếm:"));
        pnlFilter.add(txtSearch);
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(220, 53, 69));
        btnSearch.setForeground(Color.WHITE);
        pnlFilter.add(btnSearch);

        add(pnlFilter, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);

        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setBackground(Color.WHITE);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pnlActions.setBackground(Color.WHITE);
        btnAdd = new JButton("+ Thêm thuốc");
        btnEdit = new JButton("✏ Sửa");
        btnDelete = new JButton("🗑 Xóa");
        btnRefresh = new JButton("🔄 Làm mới");
        pnlActions.add(btnAdd);
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);
        pnlActions.add(btnRefresh);

        pnlTable.add(pnlActions, BorderLayout.NORTH);

        String[] columns = {"Mã thuốc", "Tên thuốc", "Hoạt chất chính", "Nhóm thuốc", "Dạng bào chế", "Đường dùng", "ĐVT", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblMedicine = new JTable(tableModel);
        tblMedicine.setRowHeight(26);
        tblMedicine.getTableHeader().setReorderingAllowed(false);
        tblMedicine.getTableHeader().setBackground(new Color(230, 238, 248));
        
        loadMockData();

        pnlTable.add(new JScrollPane(tblMedicine), BorderLayout.CENTER);
        splitPane.setLeftComponent(pnlTable);

        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBorder(BorderFactory.createTitledBorder("Thông tin thuốc & Cập nhật chuẩn"));

        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel pnlGeneral = new JPanel(new GridBagLayout());
        pnlGeneral.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaThuoc = new JTextField();
        txtMaThuoc.setEditable(false);
        txtTenThuoc = new JTextField();
        txtHoatChat = new JTextField();
        cbDonViTinh = new JComboBox<>(new String[]{"Viên", "Vỉ", "Hộp", "Lọ", "Chai", "Tube", "Ống", "Gói"});
        txtQuyCach = new JTextField("Hộp 10 vỉ x 10 viên");
        txtNhaSanXuat = new JTextField();
        txtNuocSanXuat = new JTextField("Việt Nam");

        int row = 0;
        addFormRow(pnlGeneral, gbc, row++, "Mã thuốc (Chuẩn):", txtMaThuoc);
        addFormRow(pnlGeneral, gbc, row++, "Tên thuốc:", txtTenThuoc);
        addFormRow(pnlGeneral, gbc, row++, "Hoạt chất chính:", txtHoatChat);
        addFormRow(pnlGeneral, gbc, row++, "Đơn vị tính (ĐVT):", cbDonViTinh);
        addFormRow(pnlGeneral, gbc, row++, "Quy cách đóng gói:", txtQuyCach);
        addFormRow(pnlGeneral, gbc, row++, "Nhà sản xuất:", txtNhaSanXuat);
        addFormRow(pnlGeneral, gbc, row++, "Nước sản xuất:", txtNuocSanXuat);

        tabbedPane.addTab("Thông tin chung", new JScrollPane(pnlGeneral));

        JPanel pnlDetailText = new JPanel(new GridLayout(4, 1, 5, 5));
        txtMoTa = new JTextArea(2, 20);
        txtLiieuDung = new JTextArea(2, 20);
        txtChiDinh = new JTextArea(2, 20);
        txtLuuY = new JTextArea(2, 20);

        pnlDetailText.add(createLabeledScroll("Mô tả:", txtMoTa));
        pnlDetailText.add(createLabeledScroll("Liều dùng:", txtLiieuDung));
        pnlDetailText.add(createLabeledScroll("Chỉ định:", txtChiDinh));
        pnlDetailText.add(createLabeledScroll("Lưu ý / Tác dụng phụ:", txtLuuY));

        tabbedPane.addTab("Hướng dẫn & Lưu ý", pnlDetailText);

        pnlRight.add(tabbedPane, BorderLayout.CENTER);

        JButton btnSaveDetail = new JButton("💾 Cập nhật thuốc");
        btnSaveDetail.setBackground(new Color(40, 167, 69));
        btnSaveDetail.setForeground(Color.WHITE);
        pnlRight.add(btnSaveDetail, BorderLayout.SOUTH);

        splitPane.setRightComponent(pnlRight);
        add(splitPane, BorderLayout.CENTER);

        tblMedicine.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = tblMedicine.getSelectedRow();
                if (r >= 0) {
                    txtMaThuoc.setText((String) tableModel.getValueAt(r, 0));
                    txtTenThuoc.setText((String) tableModel.getValueAt(r, 1));
                    txtHoatChat.setText((String) tableModel.getValueAt(r, 2));
                    cbDonViTinh.setSelectedItem(tableModel.getValueAt(r, 6));
                }
            }
        });
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private JPanel createLabeledScroll(String title, JTextArea textArea) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(new JLabel(title), BorderLayout.NORTH);
        pnl.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return pnl;
    }

    private void loadMockData() {
        tableModel.addRow(new Object[]{"TH001", "Amoxicillin 500mg", "Amoxicillin", "Kháng sinh", "Viên nén", "Đường uống", "Viên", "Đang kinh doanh"});
        tableModel.addRow(new Object[]{"TH002", "Metronidazole 250mg", "Metronidazole", "Kháng sinh", "Viên nén", "Đường uống", "Viên", "Đang kinh doanh"});
        tableModel.addRow(new Object[]{"TH003", "Clindamycin 300mg", "Clindamycin", "Kháng sinh", "Viên nang", "Đường uống", "Viên", "Đang kinh doanh"});
        tableModel.addRow(new Object[]{"TH004", "Paracetamol 500mg", "Paracetamol", "Giảm đau - Hạ sốt", "Viên nén", "Đường uống", "Viên", "Đang kinh doanh"});
        tableModel.addRow(new Object[]{"TH005", "Chlorhexidine 0.12%", "Chlorhexidine", "Sát khuẩn", "Dung dịch", "Súc miệng", "Chai", "Đang kinh doanh"});
    }
}