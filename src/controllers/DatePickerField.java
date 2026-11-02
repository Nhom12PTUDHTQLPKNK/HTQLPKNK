package controllers;

import java.awt.BorderLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class DatePickerField extends JPanel {

    private static final String PATTERN = "dd/MM/yyyy";

    private final JTextField picker;
    private final SimpleDateFormat format =
            new SimpleDateFormat(PATTERN);

    public DatePickerField() {
        super(new BorderLayout());

        format.setLenient(false);

        picker = new JTextField();
        picker.setColumns(10);

        add(picker, BorderLayout.CENTER);
    }

    public String getText() {
        return picker.getText();
    }

    public void setText(String text) {
        picker.setText(text == null ? "" : text);
    }

    public boolean isValidDate() {
        String value = getText().trim();

        if (value.isEmpty()) return false;

        try {
            Date date = format.parse(value);
            return format.format(date).equals(value);
        } catch (ParseException ex) {
            return false;
        }
    }

    public boolean isEditable() {
        return picker.isEditable();
    }

    public void setEditable(boolean editable) {
        picker.setEditable(editable);
        picker.setFocusable(editable);
        picker.setEnabled(editable);
    }

    public Date getDate() {
        String value = getText().trim();

        if (value.isEmpty()) return null;

        try {
            Date date = format.parse(value);

            if (!format.format(date).equals(value)) {
                return null;
            }

            return date;
        } catch (ParseException ex) {
            return null;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (picker != null) {
            picker.setEnabled(enabled);
        }
    }
}
