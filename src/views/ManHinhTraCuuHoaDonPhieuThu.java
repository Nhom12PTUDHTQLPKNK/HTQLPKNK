package views;

import models.dao.CauTruyVanTimKiemDAO;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ManHinhTraCuuHoaDonPhieuThu extends JPanel {
    public ManHinhTraCuuHoaDonPhieuThu() {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Hóa đơn", new ManHinhTraCuuDuLieu(
                "Tra Cứu Hóa Đơn",
                "Tra cứu hóa đơn theo khách hàng, nhân viên lập, khuyến mãi và tổng tiền",
                new String[]{"Mã hóa đơn", "Mã KH", "Khách hàng", "Nhân viên lập", "Khuyến mãi", "Ngày lập", "Tổng tiền"},
                CauTruyVanTimKiemDAO.invoiceLookup(),
                "Tra cứu hóa đơn", "maHoaDon", "maKhachHang", "tenKH", "tenNhanVien", "tenKhuyenMai"));
        tabs.addTab("Phiếu thu", new ManHinhTraCuuDuLieu(
                "Tra Cứu Phiếu Thu",
                "Tra cứu giao dịch thu tiền, số đã thanh toán và số dư hóa đơn",
                new String[]{"Mã phiếu", "Mã hóa đơn", "Mã KH", "Khách hàng", "Nhân viên thu", "Ngày thanh toán", "Hình thức", "Đã thanh toán", "Còn lại", "Ghi chú"},
                CauTruyVanTimKiemDAO.paymentLookup(),
                "Tra cứu phiếu thu", "maPhieu", "maHoaDon", "maKhachHang", "tenKH", "tenNhanVien", "hinhThucThanhToan"));
        add(tabs, BorderLayout.CENTER);
    }
}
