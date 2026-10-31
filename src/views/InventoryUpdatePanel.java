package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryUpdatePanel extends JPanel {

    private JTable tblInventory;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbLoaiPhieu, cbNhaCungCap, cbKho;
    private JTextField txtMaPhieu, txtNguoiThucHien, txtSoLo, txtHanSuDung;
    private JButton btnTaoPhieu, btnXacNhan;

    public InventoryUpdatePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("CẬP NHẬT KHO VẬT TƯ & THIẾT BỊ NHA KHOA", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(0, 102, 204));
        add(lblTitle, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.35);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Thông tin phiếu giao dịch kho"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbLoaiPhieu = new JComboBox<>(new String[]{"Nhập kho vật tư mới", "Xuất kho sử dụng điều trị", "Điều chỉnh tồn kho (Kiểm kê)"});
        cbNhaCungCap = new JComboBox<>(new String[]{"Cty Thiết bị Y tế 3M", "Cty Dược phẩm Central", "Nha khoa Supply Co."});
        cbKho = new JComboBox<>(new String[]{"Kho chính Nha Khoa", "Kho phòng khám 1", "Kho vật tư hao phí"});

        txtMaPhieu = new JTextField("PNK-20261031-01");
        txtNguoiThucHien = new JTextField("Trần Văn Luân");
        txtSoLo = new JTextField("LOT-2026-X1");
        txtHanSuDung = new JTextField("2028-12-31");

        gbc.gridx=0; gbc.gridy=0; pnlForm.add(new JLabel("Loại phiếu:"), gbc);
        gbc.gridx=1; pnlForm.add(cbLoaiPhieu, gbc);
        gbc.gridx=2; pnlForm.add(new JLabel("Mã phiếu:"), gbc);
        gbc.gridx=3; pnlForm.add(txtMaPhieu, gbc);

        gbc.gridx=0; gbc.gridy=1; pnlForm.add(new JLabel("Nhà cung cấp:"), gbc);
        gbc.gridx=1; pnlForm.add(cbNhaCungCap, gbc);
        gbc.gridx=2; pnlForm.add(new JLabel("Người thực hiện:"), gbc);
        gbc.gridx=3; pnlForm.add(txtNguoiThucHien, gbc);

        gbc.gridx=0; gbc.gridy=2; pnlForm.add(new JLabel("Số lô:"), gbc);
        gbc.gridx=1; pnlForm.add(txtSoLo, gbc);
        gbc.gridx=2; pnlForm.add(new JLabel("Hạn sử dụng:"), gbc);
        gbc.gridx=3; pnlForm.add(txtHanSuDung, gbc);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnTaoPhieu = new JButton("Tạo phiếu mới");
        btnXacNhan = new JButton("Lưu & Xác nhận kho");
        btnXacNhan.setBackground(new Color(0, 123, 255));
        btnXacNhan.setForeground(Color.WHITE);
        pnlBtns.add(btnTaoPhieu);
        pnlBtns.add(btnXacNhan);

        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=4;
        pnlForm.add(pnlBtns, gbc);

        splitPane.setTopComponent(pnlForm);

        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBorder(BorderFactory.createTitledBorder("Chi tiết danh mục vật tư trong phiếu"));

        String[] cols = {"Mã VT/Thuốc", "Tên vật tư / Thiết bị", "Đơn vị tính", "Số lượng", "Đơn giá", "Số lô", "Hạn sử dụng", "Ghi chú"};
        tableModel = new DefaultTableModel(cols, 0);
        tblInventory = new JTable(tableModel);
        tblInventory.setRowHeight(25);
        
        tableModel.addRow(new Object[]{"VT001", "Găng tay y tế không bột", "Hộp", "50", "85,000", "LOT-GT2026", "2028-05-01", "Nhập kho đợt 1"});
        tableModel.addRow(new Object[]{"VT002", "Kim tiêm nha khoa 30G", "Hộp", "20", "120,000", "LOT-KT992", "2027-10-15", "Vật tư gây tê"});

        pnlTable.add(new JScrollPane(tblInventory), BorderLayout.CENTER);
        splitPane.setBottomComponent(pnlTable);

        add(splitPane, BorderLayout.CENTER);
    }
}
