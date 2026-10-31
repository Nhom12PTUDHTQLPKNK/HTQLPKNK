package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryFlowReportPanel extends JPanel {

    private JTable tblReport;
    private DefaultTableModel tableModel;

    public InventoryFlowReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        initUI();
    }

    private void initUI() {
        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlControl.setBorder(BorderFactory.createTitledBorder("Báo cáo Xuất - Nhập - Tồn"));

        pnlControl.add(new JLabel("Từ ngày:"));
        pnlControl.add(new JTextField("2026-10-01", 10));
        pnlControl.add(new JLabel("Đến ngày:"));
        pnlControl.add(new JTextField("2026-10-31", 10));

        JButton btnFilter = new JButton("Xem báo cáo");
        btnFilter.setBackground(new Color(40, 167, 69));
        btnFilter.setForeground(Color.WHITE);
        pnlControl.add(btnFilter);

        add(pnlControl, BorderLayout.NORTH);

        String[] cols = {"Mã MH", "Tên vật tư / Thuốc", "ĐVT", "Tồn đầu kỳ", "Tổng Nhập", "Tổng Xuất", "Tồn cuối kỳ", "Giá trị tồn (VNĐ)"};
        tableModel = new DefaultTableModel(cols, 0);
        tblReport = new JTable(tableModel);
        tblReport.setRowHeight(25);

        tableModel.addRow(new Object[]{"VT001", "Găng tay y tế không bột", "Hộp", 50, 100, 30, 120, "10,200,000"});
        tableModel.addRow(new Object[]{"TH002", "Metronidazole 250mg", "Viên", 200, 500, 200, 500, "1,250,000"});

        add(new JScrollPane(tblReport), BorderLayout.CENTER);
    }
}