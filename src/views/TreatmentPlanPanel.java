package views;

public class TreatmentPlanPanel extends ClinicalWorkflowPanel {
    public TreatmentPlanPanel() {
        super(Workflow.TREATMENT_PLAN,
                "Lập kế hoạch điều trị",
                "Tạo phác đồ, phân công nha sĩ và theo dõi chi phí điều trị dự kiến",
                new String[]{"Mã kế hoạch", "Mã bệnh án", "Mã nha sĩ", "Mã dịch vụ", "Ngày lập (dd/MM/yyyy)", "Số lần khám", "Thời gian dự kiến", "Tổng chi phí dự kiến", "Trạng thái", "Ghi chú"},
                new String[]{"Mã kế hoạch", "Mã bệnh án", "Nha sĩ", "Dịch vụ", "Ngày lập", "Số lần khám", "Thời gian", "Chi phí dự kiến", "Trạng thái"});
    }
}
