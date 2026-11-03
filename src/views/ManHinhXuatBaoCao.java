package views;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Central location for generating and exporting the SQL-backed reports. */
public class ManHinhXuatBaoCao extends JPanel {
    public ManHinhXuatBaoCao() {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Doanh thu & công nợ", new ManHinhBaoCaoDoanhThuCongNo());
        tabs.addTab("Dịch vụ & nha sĩ", new ManHinhBaoCaoDichVuNhaSi());
        tabs.addTab("Nhập, xuất & tồn", new ManHinhBaoCaoNhapXuatKho());
        add(tabs, BorderLayout.CENTER);
    }
}
