package models.dao;

import sql.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoKhoDAO {
    public static final class DuLieuBaoCao {
        public final List<Object[]> rows;
        public final int issuedRows;
        private DuLieuBaoCao(List<Object[]> rows, int issuedRows) { this.rows = rows; this.issuedRows = issuedRows; }
    }

    public DuLieuBaoCao fetch(LocalDate from, LocalDate to, String key) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        String issueSql = "SELECT " + key + " AS ky, N'Xuất theo đơn thuốc' AS loai, D.maThuoc, "
                + "COALESCE(T.TenThuoc, D.tenThuoc, N'(Chưa có tên thuốc)') AS tenThuoc, "
                + "SUM(COALESCE(D.soLuong, 0)) AS soLuong, N'Số lượng kê trong đơn' AS ghiChu "
                + "FROM DonThuoc D LEFT JOIN THUOC T ON D.maThuoc = T.MaThuoc "
                + "WHERE D.ngayKe >= ? AND D.ngayKe <= ? GROUP BY " + key + ", D.maThuoc, T.TenThuoc, D.tenThuoc";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(issueSql)) {
            p.setDate(1, java.sql.Date.valueOf(from)); p.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) rows.add(new Object[]{r.getObject(1), r.getObject(2), r.getObject(3), r.getObject(4), r.getObject(5), r.getObject(6)});
            }
        }
        int issuedRows = rows.size();
        String stockSql = "SELECT N'Tồn hiện tại' AS ky, N'Tồn vật tư (snapshot)' AS loai, maVatTu, tenVatTu, soLuongTon, COALESCE(donViTinh, N'') AS donViTinh FROM VatTu";
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(stockSql); ResultSet r = p.executeQuery()) {
            while (r.next()) rows.add(new Object[]{r.getObject(1), r.getObject(2), r.getObject(3), r.getObject(4), r.getObject(5), r.getObject(6)});
        }
        return new DuLieuBaoCao(rows, issuedRows);
    }
}
