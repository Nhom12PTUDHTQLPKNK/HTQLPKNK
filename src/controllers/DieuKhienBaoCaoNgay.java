package controllers;

import java.time.LocalDate;
import java.util.List;

import models.dao.BaoCaoKhoDAO;
import models.dao.BaoCaoPhongKhamDAO;
import models.dao.TruyVanBaoCaoNgayDAO;

/** Coordinates report queries for the date based report screens. */
public class DieuKhienBaoCaoNgay {
    public static final String DOANH_THU = "REVENUE";
    public static final String DICH_VU_NHA_SI = "SERVICE_DENTIST";
    public static final String NHAP_XUAT_KHO = "STOCK";

    public KetQua fetch(String report, LocalDate from, LocalDate to, String period) throws Exception {
        if (DOANH_THU.equals(report)) {
            String sqlPeriod = BaoCaoPhongKhamDAO.periodExpression("H.ngayLap", period);
            return query(BaoCaoPhongKhamDAO.revenueDebtQuery(sqlPeriod), from, to, 6,
                    "Số dòng báo cáo");
        }
        if (DICH_VU_NHA_SI.equals(report)) {
            String sqlPeriod = BaoCaoPhongKhamDAO.periodExpression("BA.ngayKham", period);
            return query(BaoCaoPhongKhamDAO.serviceDentistQuery(sqlPeriod), from, to, 8,
                    "Số dòng báo cáo");
        }
        if (NHAP_XUAT_KHO.equals(report)) {
            BaoCaoKhoDAO.DuLieuBaoCao data = new BaoCaoKhoDAO().fetch(
                    from, to, BaoCaoPhongKhamDAO.periodExpression("D.ngayKe", period));
            return new KetQua(data.rows, "Dòng xuất theo đơn: " + data.issuedRows
                    + " (bao gồm snapshot tồn hiện tại; không có lịch sử nhập kho trong lược đồ hiện tại)");
        }
        throw new IllegalArgumentException("Loại báo cáo không được hỗ trợ: " + report);
    }

    private KetQua query(String sql, LocalDate from, LocalDate to, int columns, String summaryLabel)
            throws Exception {
        List<Object[]> rows = new TruyVanBaoCaoNgayDAO().query(sql, from, to, columns);
        return new KetQua(rows, summaryLabel + ": " + rows.size());
    }

    public static final class KetQua {
        public final List<Object[]> rows;
        public final String summary;
        private KetQua(List<Object[]> rows, String summary) {
            this.rows = rows;
            this.summary = summary;
        }
    }
}
