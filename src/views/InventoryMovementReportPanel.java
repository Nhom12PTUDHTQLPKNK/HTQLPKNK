package views;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import sql.DBConnection;

public class InventoryMovementReportPanel extends SqlDateReportPanel {
    public InventoryMovementReportPanel() {
        super("Thống Kê Nhập, Xuất & Tồn Kho",
                "Xuất thuốc thống kê từ đơn thuốc; tồn vật tư là số hiện tại. CSDL chưa lưu lịch sử nhập kho hoặc lịch sử biến động tồn.",
                new String[]{"Kỳ", "Loại số liệu", "Mã hàng", "Tên hàng", "Số lượng", "Đơn vị / ghi chú"});
    }

    @Override
    protected ReportResult fetchReport(LocalDate from, LocalDate to, Period period) throws Exception {
        String key = periodExpression("D.ngayKe", period);
        String issueSql = "SELECT " + key + " AS ky, N'Xuất theo đơn thuốc' AS loai, D.maThuoc, "
                + "COALESCE(T.TenThuoc, D.tenThuoc, N'(Chưa có tên thuốc)') AS tenThuoc, "
                + "SUM(COALESCE(D.soLuong, 0)) AS soLuong, N'Số lượng kê trong đơn' AS ghiChu "
                + "FROM DonThuoc D LEFT JOIN THUOC T ON D.maThuoc = T.MaThuoc "
                + "WHERE D.ngayKe >= ? AND D.ngayKe <= ? "
                + "GROUP BY " + key + ", D.maThuoc, T.TenThuoc, D.tenThuoc";
        List<Object[]> rows = new ArrayList<>();
        try (java.sql.Connection connection = DBConnection.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(issueSql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));
            try (java.sql.ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(new Object[]{result.getObject(1), result.getObject(2), result.getObject(3),
                            result.getObject(4), result.getObject(5), result.getObject(6)});
                }
            }
        }
        int issuedRows = rows.size();
        String stockSql = "SELECT N'Tồn hiện tại' AS ky, N'Tồn vật tư (snapshot)' AS loai, "
                + "maVatTu, tenVatTu, soLuongTon, COALESCE(donViTinh, N'') AS donViTinh FROM VatTu";
        try (java.sql.Connection connection = DBConnection.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(stockSql);
             java.sql.ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new Object[]{result.getObject(1), result.getObject(2), result.getObject(3),
                        result.getObject(4), result.getObject(5), result.getObject(6)});
            }
        }
        return new ReportResult(rows, "Dòng xuất theo đơn: " + issuedRows
                + " (bao gồm snapshot tồn hiện tại; không có lịch sử nhập kho trong lược đồ hiện tại)");
    }
}
