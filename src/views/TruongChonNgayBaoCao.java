package views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.UtilDateModel;

/** Calendar date picker used by date range reports. */
final class TruongChonNgayBaoCao extends JPanel {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final UtilDateModel model;
    private final JDatePickerImpl picker;

    TruongChonNgayBaoCao() {
        setLayout(new BorderLayout());
        model = new UtilDateModel();
        Properties datePickerText = new Properties();
        datePickerText.setProperty("text.today", "Hôm nay");
        datePickerText.setProperty("text.month", "Tháng");
        datePickerText.setProperty("text.year", "Năm");
        datePickerText.setProperty("text.clear", "Xóa");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, datePickerText);
        picker = new JDatePickerImpl(datePanel, new DateComponentFormatter());
        picker.setPreferredSize(new Dimension(155, 30));
        add(picker, BorderLayout.CENTER);
    }

    void setText(String text) {
        try {
            LocalDate date = LocalDate.parse(text, FORMAT);
            model.setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            model.setSelected(true);
        } catch (RuntimeException ex) {
            model.setSelected(false);
        }
    }

    String getText() {
        LocalDate date = selectedDate();
        return date == null ? "" : date.format(FORMAT);
    }

    boolean isValidDate() { return selectedDate() != null; }

    LocalDate getLocalDate() { return selectedDate(); }

    void addChangeListener(ChangeListener listener) { model.addChangeListener(listener); }

    private LocalDate selectedDate() {
        if (!model.isSelected()) return null;
        Object value = model.getValue();
        if (!(value instanceof Date)) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) value);
        return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
