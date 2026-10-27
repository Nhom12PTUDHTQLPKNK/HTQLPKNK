package datepicker;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.JFormattedTextField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Ô chọn ngày dùng thư viện JDatePicker (org.jdatepicker:jdatepicker:1.3.4).
 * Dùng: jdatepicker f = new jdatepicker(LocalDate.now()); panel.add(f.picker); LocalDate d = f.getDate();
 *
 * Được DentalClinicApp dùng ở: đặt lịch, khách hàng mới, form thêm/sửa danh mục (các cột ngày),
 * thống kê doanh thu và lọc khoảng ngày của nhật ký hoạt động (AuditLog).
 */
public class jdatepicker {

    /** Định dạng ngày thống nhất toàn hệ thống (khớp với chuỗi ngày lưu trong các bảng dữ liệu). */
    public static final String PATTERN = "yyyy-MM-dd";

    public final UtilDateModel model = new UtilDateModel();
    public final JDatePickerImpl picker;

    /** @param initial ngày ban đầu; null = chưa chọn ngày. */
    public jdatepicker(LocalDate initial) {
        Properties p = new Properties();
        p.put("text.today", "Hôm nay");
        p.put("text.month", "Tháng");
        p.put("text.year", "Năm");
        picker = new JDatePickerImpl(new JDatePanelImpl(model, p), new Formatter());
        if (initial != null) setDate(initial);
    }

    /** Đặt ngày; truyền null để xóa lựa chọn. */
    public void setDate(LocalDate d) {
        if (d == null) {
            model.setSelected(false);
            model.setValue(null);
            return;
        }
        model.setDate(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        model.setSelected(true);
    }

    /** Trả về ngày đã chọn, hoặc null nếu chưa chọn. */
    public LocalDate getDate() {
        Date v = model.getValue();
        if (v == null) return null;
        return Instant.ofEpochMilli(v.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Ngày đã chọn dạng yyyy-MM-dd, hoặc "" nếu chưa chọn (tiện lưu vào bảng dữ liệu). */
    public String getDateString() {
        LocalDate d = getDate();
        return d == null ? "" : d.toString();
    }

    /** Gọi lại khi người dùng đổi ngày. */
    public void onChange(Runnable r) {
        model.addChangeListener(e -> r.run());
    }

    public void setEnabled(boolean enabled) {
        picker.setEnabled(enabled);
        picker.getJFormattedTextField().setEnabled(enabled);
    }

    /** Định dạng hiển thị yyyy-MM-dd trong ô chọn ngày. */
    static class Formatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat fmt = new SimpleDateFormat(PATTERN);

        Formatter() { fmt.setLenient(false); }

        @Override public Object stringToValue(String text) throws ParseException {
            if (text == null || text.trim().isEmpty()) return null;
            return fmt.parseObject(text.trim());
        }

        @Override public String valueToString(Object value) {
            if (value instanceof Calendar) return fmt.format(((Calendar) value).getTime());
            if (value instanceof Date) return fmt.format((Date) value);
            return "";
        }
    }
}