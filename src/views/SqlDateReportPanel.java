package views;

import controllers.AuthController;
import datepicker.DatePickerField;
import report.ExcelReportExport;
import report.ReportServices;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Shared date range, SQL refresh, print, Excel and PDF workflow for reports. */
public abstract class SqlDateReportPanel extends JPanel {
    protected static final Color TEXT_DARK = new Color(15, 29, 69);
    protected static final Color MUTED = new Color(91, 112, 145);
    protected static final Color BORDER_LIGHT = new Color(224, 233, 243);
    protected static final Color BG_CANVAS = new Color(243, 248, 253);
    protected static final Color CARD_BG = Color.WHITE;

    private final String title;
    private final String[] columns;
    private final DefaultTableModel model;
    private final JTable table;
    private final DatePickerField fromDate = new DatePickerField();
    private final DatePickerField toDate = new DatePickerField();
    private final JComboBox<Period> period = new JComboBox<>(Period.values());
    private final JLabel status = new JLabel("Chọn khoảng thời gian rồi bấm Thống kê.");
    private final JButton runButton = new JButton("Thống kê");
    private SwingWorker<ReportResult, Void> activeWorker;
    private String reportSubtitle = "";

    protected SqlDateReportPanel(String title, String description, String[] columns) {
        this.title = title;
        this.columns = columns.clone();
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(MUTED);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(titleLabel);
        heading.add(Box.createVerticalStrut(3));
        heading.add(descriptionLabel);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(label("Từ ngày"));
        filters.add(fromDate);
        filters.add(label("Đến ngày"));
        filters.add(toDate);
        filters.add(label("Thống kê theo"));
        period.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filters.add(period);
        styleButton(runButton, new Color(0, 126, 255));
        JButton print = new JButton("In");
        JButton excel = new JButton("Excel");
        JButton pdf = new JButton("PDF");
        styleButton(print, new Color(108, 117, 125));
        styleButton(excel, new Color(40, 167, 69));
        styleButton(pdf, new Color(220, 53, 69));
        filters.add(runButton);
        filters.add(print);
        filters.add(excel);
        filters.add(pdf);
        JPanel header = new JPanel(new BorderLayout(0, 10));
        header.setOpaque(false);
        header.add(heading, BorderLayout.NORTH);
        header.add(filters, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        add(scroll, BorderLayout.CENTER);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(MUTED);
        add(status, BorderLayout.SOUTH);

        LocalDate today = LocalDate.now();
        fromDate.setText(today.minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toDate.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        runButton.addActionListener(e -> refreshReport());
        print.addActionListener(e -> printReport());
        excel.addActionListener(e -> exportExcel());
        pdf.addActionListener(e -> exportPdf());
    }

    protected abstract ReportResult fetchReport(LocalDate from, LocalDate to, Period period) throws Exception;

    protected final void refreshReport() {
        if (!fromDate.isValidDate() || !toDate.isValidDate()) {
            status.setForeground(new Color(180, 45, 55));
            status.setText("Vui lòng nhập ngày hợp lệ theo định dạng dd/MM/yyyy.");
            return;
        }
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate from = LocalDate.parse(fromDate.getText(), dateFormat);
        LocalDate to = LocalDate.parse(toDate.getText(), dateFormat);
        if (from.isAfter(to)) {
            status.setForeground(new Color(180, 45, 55));
            status.setText("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            return;
        }
        Period selected = (Period) period.getSelectedItem();
        if (activeWorker != null) activeWorker.cancel(true);
        runButton.setEnabled(false);
        status.setForeground(MUTED);
        status.setText("Đang lấy số liệu từ SQL Server...");
        activeWorker = new SwingWorker<ReportResult, Void>() {
            @Override protected ReportResult doInBackground() throws Exception {
                return fetchReport(from, to, selected);
            }

            @Override protected void done() {
                if (isCancelled()) return;
                runButton.setEnabled(true);
                try {
                    ReportResult result = get();
                    model.setRowCount(0);
                    for (Object[] row : result.rows) model.addRow(row);
                    reportSubtitle = "Từ " + from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " đến " + to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " — nhóm theo " + selected.label;
                    status.setForeground(MUTED);
                    status.setText(result.summary + "  |  " + reportSubtitle);
                } catch (Exception ex) {
                    model.setRowCount(0);
                    Throwable cause = ex.getCause();
                    status.setForeground(new Color(180, 45, 55));
                    status.setText("Không thể tải báo cáo SQL Server: "
                            + (cause == null ? ex.getMessage() : cause.getMessage()));
                }
            }
        };
        activeWorker.execute();
    }

    protected enum Period {
        DAY("Ngày"), MONTH("Tháng"), YEAR("Năm");
        final String label;
        Period(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    protected static final class ReportResult {
        final List<Object[]> rows;
        final String summary;
        protected ReportResult(List<Object[]> rows, String summary) {
            this.rows = rows;
            this.summary = summary;
        }
    }

    protected static String periodExpression(String dateColumn, Period period) {
        if (period == Period.MONTH) {
            return "CONVERT(CHAR(7), " + dateColumn + ", 126)";
        }
        if (period == Period.YEAR) {
            return "CONVERT(CHAR(4), YEAR(" + dateColumn + "))";
        }
        return "CONVERT(VARCHAR(10), " + dateColumn + ", 103)";
    }

    protected static ReportResult query(String sql, LocalDate from, LocalDate to,
                                        int columnCount) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (java.sql.Connection connection = DBConnection.getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, java.sql.Date.valueOf(from));
            statement.setDate(2, java.sql.Date.valueOf(to));
            try (java.sql.ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) row[i] = result.getObject(i + 1);
                    rows.add(row);
                }
            }
        }
        return new ReportResult(rows, "Số dòng báo cáo: " + rows.size());
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 253));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    private List<Object[]> currentRows() {
        List<Object[]> rows = new ArrayList<>();
        for (int r = 0; r < table.getRowCount(); r++) {
            Object[] row = new Object[table.getColumnCount()];
            int modelRow = table.convertRowIndexToModel(r);
            for (int c = 0; c < table.getColumnCount(); c++) {
                row[c] = model.getValueAt(modelRow, table.convertColumnIndexToModel(c));
            }
            rows.add(row);
        }
        return rows;
    }

    private String[] visibleColumns() {
        String[] result = new String[table.getColumnCount()];
        for (int c = 0; c < result.length; c++) {
            result[c] = String.valueOf(model.getColumnName(table.convertColumnIndexToModel(c)));
        }
        return result;
    }

    private String currentUser() {
        return AuthController.getCurrentUser() == null
                ? "Người dùng" : AuthController.getCurrentUser().getUsername();
    }

    private void printReport() {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat(title + " — " + reportSubtitle),
                    new java.text.MessageFormat("Trang {0}"), true, null, true);
        } catch (PrinterException ex) {
            showError("Không thể in báo cáo: " + ex.getMessage());
        }
    }

    private void exportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất báo cáo Excel");
        chooser.setSelectedFile(new File("bao-cao.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = withExtension(chooser.getSelectedFile(), ".xlsx");
        try {
            ExcelReportExport.write(file, title, visibleColumns(), currentRows());
            JOptionPane.showMessageDialog(this, "Đã xuất báo cáo: " + file.getAbsolutePath());
        } catch (Exception ex) {
            showError("Không thể xuất Excel: " + ex.getMessage());
        }
    }

    private void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất báo cáo PDF");
        chooser.setSelectedFile(new File("bao-cao.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = withExtension(chooser.getSelectedFile(), ".pdf");
        try {
            ReportServices.exportTable(file, title, reportSubtitle, visibleColumns(), currentRows(),
                    "Số dòng: " + table.getRowCount(), currentUser());
            JOptionPane.showMessageDialog(this, "Đã xuất báo cáo: " + file.getAbsolutePath());
        } catch (Exception ex) {
            showError("Không thể xuất PDF: " + ex.getMessage());
        }
    }

    private File withExtension(File file, String extension) {
        return file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(extension)
                ? file : new File(file.getParentFile(), file.getName() + extension);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi báo cáo", JOptionPane.ERROR_MESSAGE);
    }
}
