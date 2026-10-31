package views;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class InvoiceReceiptSearchPanel extends JPanel {
    public InvoiceReceiptSearchPanel() {
        super(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Hóa đơn", new SqlSearchPanel(
                "Tra Cứu Hóa Đơn",
                "Tra cứu hóa đơn theo khách hàng, nhân viên lập, khuyến mãi và tổng tiền",
                new String[]{"Mã hóa đơn", "Mã KH", "Khách hàng", "Nhân viên lập", "Khuyến mãi", "Ngày lập", "Tổng tiền"},
                "SELECT H.maHoaDon, K.maKhachHang, K.tenKH, N.tenNhanVien, KM.tenKhuyenMai, "
                + "CONVERT(VARCHAR(10), H.ngayLap, 103) AS ngayLap, "
                + "CAST(COALESCE(CT.tongTien, 0) AS DECIMAL(18, 0)) AS tongTien "
                + "FROM HoaDon H "
                + "LEFT JOIN KhachHang K ON H.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N ON H.maNhanVien = N.maNhanVien "
                + "LEFT JOIN KhuyenMai KM ON H.maKhuyenMai = KM.maKhuyenMai "
                + "LEFT JOIN (SELECT maHoaDon, SUM(CAST(soLuong AS DECIMAL(18, 2)) * donGia) AS tongTien "
                + "FROM ChiTietHoaDon GROUP BY maHoaDon) CT ON H.maHoaDon = CT.maHoaDon",
                "Tra cứu hóa đơn", "maHoaDon", "maKhachHang", "tenKH", "tenNhanVien", "tenKhuyenMai"));
        tabs.addTab("Phiếu thu", new SqlSearchPanel(
                "Tra Cứu Phiếu Thu",
                "Tra cứu giao dịch thu tiền, số đã thanh toán và số dư hóa đơn",
                new String[]{"Mã phiếu", "Mã hóa đơn", "Mã KH", "Khách hàng", "Nhân viên thu", "Ngày thanh toán", "Hình thức", "Đã thanh toán", "Còn lại", "Ghi chú"},
                "SELECT P.maPhieu, P.maHoaDon, K.maKhachHang, K.tenKH, N.tenNhanVien, "
                + "CONVERT(VARCHAR(10), P.ngayThanhToan, 103) AS ngayThanhToan, "
                + "P.hinhThucThanhToan, P.daThanhToan, P.soTienConLai, P.ghiChu "
                + "FROM PhieuThanhToan P "
                + "LEFT JOIN KhachHang K ON P.maKhachHang = K.maKhachHang "
                + "LEFT JOIN NhanVien N ON P.maNhanVien = N.maNhanVien",
                "Tra cứu phiếu thu", "maPhieu", "maHoaDon", "maKhachHang", "tenKH", "tenNhanVien", "hinhThucThanhToan"));
        add(tabs, BorderLayout.CENTER);
    }
}
