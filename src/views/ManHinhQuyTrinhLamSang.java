package views;


import controllers.NhatKyKiemToan;
import models.dao.DonThuocDAO;
import models.dao.KeHoachDieuTriDAO;
import models.dao.LichHenDAO;
import models.dao.QuyTrinhLamSangDAO;
import models.entity.DonThuoc;
import models.entity.KeHoachDieuTri;
import models.entity.LichHen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared layout and data handling for the three clinical workflow screens. */
class ManHinhQuyTrinhLamSang extends JPanel {
    enum QuyTrinh { TREATMENT_PLAN, PRESCRIPTION, FOLLOW_UP }

    private final QuyTrinh workflow;
    private final QuyTrinhLamSangDAO workflowDAO = new QuyTrinhLamSangDAO();
    private final LinkedHashMap<String, JTextField> fields = new LinkedHashMap<String, JTextField>();
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel count = new JLabel(" ");

    ManHinhQuyTrinhLamSang(QuyTrinh workflow, String title, String subtitle, String[] labels, String[] columns) {
        this.workflow = workflow;
        setLayout(new BorderLayout());
        setBackground(BoCongCuGiaoDien.BG_CANVAS);
        JPanel root = BoCongCuGiaoDien.pageRoot();
        add(root, BorderLayout.CENTER);

        JPanel toolbar = BoCongCuGiaoDien.toolbar();
        JButton refresh = BoCongCuGiaoDien.button("Tải lại", BoCongCuGiaoDien.BLUE_PRIMARY, 90);
        refresh.addActionListener(e -> reload());
        toolbar.add(refresh);
        root.add(BoCongCuGiaoDien.pageHeader(title, toolbar), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(buildForm(labels), BorderLayout.NORTH);
        model = BoCongCuGiaoDien.readOnlyModel(columns);
        table = new JTable(model);
        BoCongCuGiaoDien.styleTable(table);
        table.setAutoCreateRowSorter(true);
        JPanel listCard = BoCongCuGiaoDien.card();
        listCard.setLayout(new BorderLayout(0, 8));
        listCard.setBorder(new EmptyBorder(14, 16, 12, 16));
        JLabel listTitle = BoCongCuGiaoDien.fieldLabel("Dữ liệu đã lưu");
        listCard.add(listTitle, BorderLayout.NORTH);
        listCard.add(BoCongCuGiaoDien.scroll(table), BorderLayout.CENTER);
        count.setFont(BoCongCuGiaoDien.font(Font.PLAIN, 12));
        count.setForeground(BoCongCuGiaoDien.MUTED);
        listCard.add(count, BorderLayout.SOUTH);
        content.add(listCard, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);
        reload();
    }

    private JPanel buildForm(String[] labels) {
        JPanel card = BoCongCuGiaoDien.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        JPanel grid = new JPanel(new GridLayout(0, 4, 12, 8));
        grid.setOpaque(false);
        for (String label : labels) {
            grid.add(BoCongCuGiaoDien.fieldLabel(label));
            JTextField field = BoCongCuGiaoDien.textField();
            fields.put(label, field);
            grid.add(field);
        }
        String idLabel = labels[0];
        fields.get(idLabel).setText(nextId());
        if (fields.containsKey("Ngày lập (dd/MM/yyyy)")) fields.get("Ngày lập (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now()));
        if (fields.containsKey("Ngày kê (dd/MM/yyyy)")) fields.get("Ngày kê (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now()));
        if (fields.containsKey("Ngày hẹn (dd/MM/yyyy)")) fields.get("Ngày hẹn (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now().plusWeeks(1)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton clear = BoCongCuGiaoDien.button("Làm mới", BoCongCuGiaoDien.PINK_ACCENT, 95);
        JButton save = BoCongCuGiaoDien.button("Lưu", BoCongCuGiaoDien.GREEN_ACCENT, 90);
        clear.addActionListener(e -> clearForm());
        save.addActionListener(e -> save());
        buttons.add(clear);
        buttons.add(save);
        card.add(grid, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private String value(String label) { return fields.get(label).getText().trim(); }

    private void save() {
        try {
            switch (workflow) {
                case TREATMENT_PLAN: savePlan(); break;
                case PRESCRIPTION: savePrescription(); break;
                case FOLLOW_UP: saveFollowUp(); break;
                default: throw new IllegalStateException("Loại xử lý không hợp lệ");
            }
            BoCongCuGiaoDien.info(this, "Đã lưu dữ liệu vào SQL Server.");
            clearForm();
            reload();
        } catch (IllegalArgumentException ex) {
            BoCongCuGiaoDien.warn(this, ex.getMessage());
        } catch (SQLException ex) {
            BoCongCuGiaoDien.warn(this, "Không thể lưu dữ liệu: " + ex.getMessage());
        } catch (RuntimeException ex) {
            BoCongCuGiaoDien.warn(this, "Không thể tải hoặc lưu dữ liệu: " + ex.getMessage());
        }
    }

    private void savePlan() throws SQLException {
        require("Mã kế hoạch", "Mã bệnh án", "Mã nha sĩ", "Mã dịch vụ", "Ngày lập (dd/MM/yyyy)");
        KeHoachDieuTri plan = new KeHoachDieuTri();
        plan.setMaKeHoach(value("Mã kế hoạch"));
        plan.setMaBenhAn(value("Mã bệnh án"));
        plan.setMaNhanVien(value("Mã nha sĩ"));
        plan.setMaDichVu(value("Mã dịch vụ"));
        plan.setNgayLap(java.sql.Date.valueOf(parseDate("Ngày lập (dd/MM/yyyy)")));
        plan.setGhiChu(value("Ghi chú"));
        plan.setSoLankham(parsePositiveInt("Số lần khám", false));
        plan.setThoiGianDuKien(value("Thời gian dự kiến"));
        plan.setTongChiPhiDuKien(parseMoney());
        plan.setTrangThai(value("Trạng thái").isEmpty() ? "Đang thực hiện" : value("Trạng thái"));
        if (!new KeHoachDieuTriDAO().insert(plan)) throw new SQLException("SQL Server không ghi nhận kế hoạch.");
    }

    private void savePrescription() throws SQLException {
        require("Mã đơn thuốc", "Mã bệnh án", "Mã nha sĩ", "Mã khách hàng", "Mã thuốc", "Tên thuốc", "Ngày kê (dd/MM/yyyy)", "Cách dùng");
        DonThuoc rx = new DonThuoc();
        rx.setMaDonThuoc(value("Mã đơn thuốc"));
        rx.setMaBenhAn(value("Mã bệnh án"));
        rx.setMaNhanVien(value("Mã nha sĩ"));
        rx.setMaKhachHang(value("Mã khách hàng"));
        rx.setMaThuoc(value("Mã thuốc"));
        rx.setTenThuoc(value("Tên thuốc"));
        rx.setNgayKe(java.sql.Date.valueOf(parseDate("Ngày kê (dd/MM/yyyy)")));
        rx.setGhiChu(value("Ghi chú"));
        rx.setSoLuong(parsePositiveInt("Số lượng", true));
        rx.setCachDung(value("Cách dùng"));
        if (!new DonThuocDAO().insert(rx)) throw new SQLException("SQL Server không ghi nhận đơn thuốc.");
    }

    private void saveFollowUp() throws SQLException {
        require("Mã lịch hẹn", "Mã khách hàng", "Mã nha sĩ", "Ngày hẹn (dd/MM/yyyy)", "Giờ hẹn (HH:mm)");
        LocalDate date = parseDate("Ngày hẹn (dd/MM/yyyy)");
        java.time.LocalTime time;
        try { time = BoCongCuGiaoDien.parseTime(value("Giờ hẹn (HH:mm)")); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Giờ hẹn phải đúng định dạng HH:mm."); }
        if (time == null) throw new IllegalArgumentException("Vui lòng nhập giờ hẹn.");
        String service = value("Dịch vụ tái khám");
        if (service.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập dịch vụ tái khám.");
        String appointmentId = value("Mã lịch hẹn");
        String note = "Tái khám - " + service + (value("Ghi chú").isEmpty() ? "" : " | " + value("Ghi chú"));
        LichHen appointment = new LichHen();
        appointment.setMaLichHen(appointmentId);
        appointment.setMaKhachHang(value("Mã khách hàng"));
        appointment.setMaNhanVien(value("Mã nha sĩ"));
        appointment.setNgayHen(java.sql.Date.valueOf(date));
        appointment.setGioKham(time.format(BoCongCuGiaoDien.TIME_FMT));
        appointment.setTrangThai("Chờ xác nhận");
        appointment.setGhiChu(note);
        new LichHenDAO().insert(appointment);
        NhatKyKiemToan.event("CREATE", "Lịch hẹn tái khám - mã " + appointmentId);
    }

    private void require(String... labels) {
        for (String label : labels) if (value(label).isEmpty()) throw new IllegalArgumentException("Vui lòng nhập " + label + ".");
    }

    private LocalDate parseDate(String label) {
        try {
            LocalDate date = BoCongCuGiaoDien.parseDate(value(label));
            if (date == null) throw new IllegalArgumentException("Vui lòng nhập " + label + ".");
            return date;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " không hợp lệ; dùng định dạng dd/MM/yyyy.");
        }
    }

    private int parsePositiveInt(String label, boolean required) {
        String raw = value(label);
        if (raw.isEmpty() && !required) return 0;
        try {
            int number = Integer.parseInt(raw);
            if (number < 0 || (required && number == 0)) throw new NumberFormatException();
            return number;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException(label + " phải là số nguyên hợp lệ."); }
    }

    private double parseMoney() {
        String raw = value("Tổng chi phí dự kiến");
        if (raw.isEmpty()) return 0.0;
        try {
            double amount = Double.parseDouble(raw.replace(",", ""));
            if (amount < 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Tổng chi phí phải là số không âm."); }
    }

    private void clearForm() {
        for (Map.Entry<String, JTextField> entry : fields.entrySet()) entry.getValue().setText("");
        fields.get(fields.keySet().iterator().next()).setText(nextId());
        if (fields.containsKey("Ngày lập (dd/MM/yyyy)")) fields.get("Ngày lập (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now()));
        if (fields.containsKey("Ngày kê (dd/MM/yyyy)")) fields.get("Ngày kê (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now()));
        if (fields.containsKey("Ngày hẹn (dd/MM/yyyy)")) fields.get("Ngày hẹn (dd/MM/yyyy)").setText(BoCongCuGiaoDien.fmt(LocalDate.now().plusWeeks(1)));
    }

    private String nextId() {
        String prefix = workflow == QuyTrinh.TREATMENT_PLAN ? "KHDT" : workflow == QuyTrinh.PRESCRIPTION ? "DT" : "LH";
        return prefix + System.currentTimeMillis();
    }

    private void reload() {
        model.setRowCount(0);
        try {
            for (Object[] row : workflowDAO.getRows(workflow.name())) model.addRow(row);
            count.setText("Tổng " + model.getRowCount() + " bản ghi trong SQL Server.");
        } catch (SQLException ex) {
            count.setText("Không tải được dữ liệu từ SQL Server.");
            System.err.println("Không tải được dữ liệu xử lý: " + ex.getMessage());
        }
    }

}
