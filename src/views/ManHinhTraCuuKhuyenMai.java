package views;

import models.dao.CauTruyVanTimKiemDAO;

public class ManHinhTraCuuKhuyenMai extends ManHinhTraCuuDuLieu {

    public ManHinhTraCuuKhuyenMai() {

        super(
                "Tra Cứu Chương Trình Khuyến Mãi",

                "Tra cứu chương trình, mức giảm, thời gian áp dụng và trạng thái",

                new String[]{
                        "Mã KM",
                        "Tên chương trình",
                        "Mức giảm",
                        "Ngày bắt đầu",
                        "Ngày kết thúc",
                        "Trạng thái"
                },

                CauTruyVanTimKiemDAO.promotionLookup(),

                "Tra cứu chương trình khuyến mãi",

                new String[]{
                        "maKhuyenMai",
                        "tenKhuyenMai",
                        "Mức giảm",
                        "Ngày bắt đầu",
                        "Ngày kết thúc",
                        "Trạng thái"
                }
        );
    }
}
