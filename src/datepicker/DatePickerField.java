package datepicker;

import java.awt.BorderLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.swing.JPanel;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/** JDatePicker wrapper that preserves the dd/MM/yyyy text API used by forms. */
public class DatePickerField extends JPanel {
    private static final String PATTERN = "dd/MM/yyyy";
    private final UtilDateModel model;
    private final JDatePickerImpl picker;
    private final SimpleDateFormat format = new SimpleDateFormat(PATTERN);

    public DatePickerField() {
        super(new BorderLayout());
        format.setLenient(false);
        model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("text.today", "Hôm nay");
        properties.put("text.month", "Tháng");
        properties.put("text.year", "Năm");
        properties.put("date.format", PATTERN);
        picker = new JDatePickerImpl(new JDatePanelImpl(model, properties),
                new javax.swing.JFormattedTextField.AbstractFormatter() {
                    @Override
                    public Object stringToValue(String text) throws ParseException {
                        if (text == null || text.trim().isEmpty()) return null;
                        Date date = format.parse(text.trim());
                        if (!format.format(date).equals(text.trim())) {
                            throw new ParseException("Ngày không hợp lệ", 0);
                        }
                        return date;
                    }

                    @Override
                    public String valueToString(Object value) {
                        return value instanceof Date ? format.format((Date) value) : "";
                    }
                });
        picker.getJFormattedTextField().setColumns(10);
        add(picker, BorderLayout.CENTER);
    }

    public String getText() {
        return picker.getJFormattedTextField().getText();
    }

    public void setText(String text) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            model.setValue(null);
            model.setSelected(false);
            picker.getJFormattedTextField().setText("");
            return;
        }
        try {
            Date date = format.parse(value);
            if (!format.format(date).equals(value)) throw new ParseException("Invalid date", 0);
            model.setValue(date);
            model.setSelected(true);
            picker.getJFormattedTextField().setText(value);
        } catch (ParseException ex) {
            model.setValue(null);
            model.setSelected(false);
            picker.getJFormattedTextField().setText(value);
        }
    }

    public boolean isValidDate() {
        String value = getText();
        if (value == null || value.trim().isEmpty()) return false;
        try {
            Date date = format.parse(value.trim());
            return format.format(date).equals(value.trim());
        } catch (ParseException ex) {
            return false;
        }
    }

    public Date getDate() {
        Object value = model.getValue();
        return value instanceof Date ? (Date) value : null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (picker != null) {
            picker.setEnabled(enabled);
            picker.getJFormattedTextField().setEnabled(enabled);
        }
    }
}
