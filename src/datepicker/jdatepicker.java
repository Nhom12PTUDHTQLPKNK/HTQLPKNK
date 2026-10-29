package datepicker;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Ô nhập ngày đơn giản, không cần thư viện ngoài.
 * Ngày được hiển thị và nhập theo định dạng yyyy-MM-dd.
 */
public class jdatepicker {

    public static final String PATTERN = "yyyy-MM-dd";
    private final SimpleDateFormat format = new SimpleDateFormat(PATTERN);
    public final JFormattedTextField picker;

    /** @param initial ngày ban đầu; null = chưa chọn ngày. */
    public jdatepicker(LocalDate initial) {
        format.setLenient(false);
        picker = new JFormattedTextField(new Formatter());
        picker.setColumns(10);
        setDate(initial);
    }

    /** Đặt ngày; truyền null để xóa lựa chọn. */
    public void setDate(LocalDate date) {
        picker.setText(date == null ? "" : date.toString());
    }

    /** Trả về ngày đã nhập, hoặc null nếu đang để trống hoặc ngày không hợp lệ. */
    public LocalDate getDate() {
        String text = picker.getText();
        if (text == null || text.trim().isEmpty()) return null;
        try {
            Date date = format.parse(text.trim());
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException ex) {
            return null;
        }
    }

    /** Ngày dạng yyyy-MM-dd, hoặc chuỗi rỗng nếu chưa có ngày hợp lệ. */
    public String getDateString() {
        LocalDate date = getDate();
        return date == null ? "" : date.toString();
    }

    /** Gọi lại khi nội dung ô nhập thay đổi. */
    public void onChange(final Runnable callback) {
        picker.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { callback.run(); }
            @Override public void removeUpdate(DocumentEvent event) { callback.run(); }
            @Override public void changedUpdate(DocumentEvent event) { callback.run(); }
        });
    }

    public void setEnabled(boolean enabled) {
        picker.setEnabled(enabled);
    }

    private static class Formatter extends JFormattedTextField.AbstractFormatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN);

        Formatter() {
            dateFormat.setLenient(false);
        }

        @Override public Object stringToValue(String text) throws ParseException {
            if (text == null || text.trim().isEmpty()) return null;
            return dateFormat.parse(text.trim());
        }

        @Override public String valueToString(Object value) {
            return value instanceof Date ? dateFormat.format((Date) value) : "";
        }
    }
}
