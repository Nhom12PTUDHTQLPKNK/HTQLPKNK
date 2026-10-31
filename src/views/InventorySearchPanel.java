package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventorySearchPanel extends JPanel {

    private JTable tblStock;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbWarningFilter;

    public InventorySearchPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlFilter.setBorder(BorderFactory.createTitledBorder("Tra cứu & Cảnh báo tồn kho"));

        txtSearch = new JTextField(18);
        pnlFilter.add(new JLabel("Từ khóa (Tên/Mã):"));
        pnlFilter.add(txtSearch);

        pnlFilter.add(new JLabel("Trạng thái cảnh báo:"));
        cbWarningFilter = new JComboBox<>(new String[]{"Tất cả", "⚠ Sắp hết hàng (Tồn < 10)", "⛔ Hàng sắp hết hạn (< 30 ngày)", "✅ Bình thường"});
        pnlFilter.add(cbWarningFilter);

        JButton btnSearch = new JButton("Tra cứu");
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        pnlFilter.add(btnSearch);

        add(pnlFilter, BorderLayout.NORTH);

        String[] cols = {"Mã VT/Thuốc", "Tên sản phẩm", "Loại", "ĐVT", "Số lượng tồn", "Vị trí kho", "Số lô", "Hạn sử dụng", "Cảnh báo"};
        tableModel = new DefaultTableModel(cols, 0);
        tblStock = new JTable(tableModel);
        tblStock.setRowHeight(26);

        tableModel.addRow(new Object[]{"VT001", "Găng tay y tế không bột", "Vật tư", "Hộp", 120, "Khu A - Kệ 1", "LOT-GT2026", "2028-05-01", "✅ An toàn"});
        tableModel.addRow(new Object[]{"VT005", "Composite trám răng 3M", "Vật tư", "Týp", 3, "Khu B - Kệ 2", "LOT-CP883", "2026-11-15", "⚠ Sắp hết hàng"});
        tableModel.addRow(new Object[]{"TH002", "Metronidazole 250mg", "Thuốc", "Viên", 500, "Khu C - Thuốc", "LOT-MT2024", "2026-11-10", "⛔ Sắp hết hạn"});

        add(new JScrollPane(tblStock), BorderLayout.CENTER);
    }
}