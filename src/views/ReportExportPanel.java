package views;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Central location for generating and exporting the SQL-backed reports. */
public class ReportExportPanel extends JPanel {
    public ReportExportPanel() {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Doanh thu & công nợ", new RevenueDebtReportPanel());
        tabs.addTab("Dịch vụ & nha sĩ", new ServiceDentistReportPanel());
        tabs.addTab("Nhập, xuất & tồn", new InventoryMovementReportPanel());
        add(tabs, BorderLayout.CENTER);
    }
}
