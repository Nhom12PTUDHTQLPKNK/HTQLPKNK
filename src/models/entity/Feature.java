package models.entity;

/**
 * Các chức năng được sử dụng để tra cứu dữ liệu và phân quyền.
 */
public enum Feature {
    HOME("Trang chủ"),
    ACCOUNT("Tài khoản"),
    HELP("Trợ giúp"),
    D_BA("Danh mục bệnh án"),
    D_GOI("Danh mục dịch vụ"),
    D_KM("Danh mục khuyến mãi"),
    D_LLV("Danh mục lịch làm việc"),
    D_KHO("Danh mục kho"),
    D_NV("Danh mục nhân viên"),
    BOOK("Đặt lịch hẹn"),
    PAY("Thanh toán"),
    S_KH("Tra cứu khách hàng"),
    S_KM("Tra cứu khuyến mãi"),
    S_KHO("Tra cứu kho"),
    ST_DT("Thống kê doanh thu"),
    ST_KH("Thống kê khách hàng");

    public final String title;

    Feature(String title) {
        this.title = title;
    }
}
