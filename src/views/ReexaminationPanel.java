package views;

public class ReexaminationPanel extends ClinicalWorkflowPanel {
    public ReexaminationPanel() {
        super(Workflow.FOLLOW_UP,
                "Quản lý lịch hẹn tái khám",
                "Đặt lịch tái khám, chọn ngày giờ và nha sĩ phụ trách",
                new String[]{"Mã lịch hẹn", "Mã khách hàng", "Mã nha sĩ", "Ngày hẹn (dd/MM/yyyy)", "Giờ hẹn (HH:mm)", "Dịch vụ tái khám", "Ghi chú"},
                new String[]{"Mã lịch hẹn", "Mã khách hàng", "Tên khách hàng", "Mã nha sĩ", "Nha sĩ", "Ngày hẹn", "Giờ", "Trạng thái", "Ghi chú"});
    }
}
