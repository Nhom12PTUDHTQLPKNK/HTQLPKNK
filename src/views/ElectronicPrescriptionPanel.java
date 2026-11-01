package views;

public class ElectronicPrescriptionPanel extends ClinicalWorkflowPanel {
    public ElectronicPrescriptionPanel() {
        super(Workflow.PRESCRIPTION,
                "Kê đơn thuốc điện tử",
                "Lập đơn thuốc theo bệnh án và lưu thông tin kê đơn vào hệ thống",
                new String[]{"Mã đơn thuốc", "Mã bệnh án", "Mã nha sĩ", "Mã khách hàng", "Mã thuốc", "Tên thuốc", "Ngày kê (dd/MM/yyyy)", "Số lượng", "Cách dùng", "Ghi chú"},
                new String[]{"Mã đơn thuốc", "Mã bệnh án", "Nha sĩ", "Khách hàng", "Mã thuốc", "Tên thuốc", "Ngày kê", "Số lượng", "Cách dùng"});
    }
}
