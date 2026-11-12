package views;

import controllers.DieuKhienBaoCaoNgay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Shared date range, SQL refresh and print workflow for reports. */
public abstract class ManHinhBaoCaoTheoNgay extends JPanel {
    protected static final Color TEXT_DARK = new Color(15, 29, 69);
    protected static final Color MUTED = new Color(91, 112, 145);
    protected static final Color BORDER_LIGHT = new Color(224, 233, 243);
    protected static final Color BG_CANVAS = new Color(243, 248, 253);
    protected static final Color CARD_BG = Color.WHITE;

    private final String title;
    private final String reportType;
    private final DieuKhienBaoCaoNgay reportController = new DieuKhienBaoCaoNgay();
    private final String[] columns;
    private final DefaultTableModel model;
    private final JTable table;
    private final ReportChartPanel chart = new ReportChartPanel();
    private final TruongChonNgayBaoCao fromDate = new TruongChonNgayBaoCao();
    private final TruongChonNgayBaoCao toDate = new TruongChonNgayBaoCao();
    private final JComboBox<ChuKyBaoCao> period = new JComboBox<>(ChuKyBaoCao.values());
    private final JLabel status = new JLabel("Chọn khoảng thời gian rồi bấm Thống kê.");
    private final JButton runButton = new JButton("Thống kê");
    private SwingWorker<DieuKhienBaoCaoNgay.KetQua, Void> activeWorker;
    private String reportSubtitle = "";
    private final Timer filterRefreshTimer = new Timer(500, e -> refreshReport());

    protected ManHinhBaoCaoTheoNgay(String title, String description, String[] columns, String reportType) {
        this.title = title;
        this.reportType = reportType;
        this.columns = columns.clone();
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(titleLabel);
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionLabel.setForeground(MUTED);
        heading.add(descriptionLabel);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(label("Từ ngày"));
        filters.add(fromDate);
        filters.add(label("Đến ngày"));
        filters.add(toDate);
        filters.add(label("Thống kê theo"));
        period.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterRefreshTimer.setRepeats(false);
        period.addActionListener(e -> scheduleFilterRefresh());
        ChangeListener dateListener = new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) { scheduleFilterRefresh(); }
        };
        fromDate.addChangeListener(dateListener);
        toDate.addChangeListener(dateListener);
        filters.add(period);
        styleButton(runButton, new Color(0, 126, 255));
        JButton print = new JButton("In");
        styleButton(print, new Color(74, 85, 96));
        filters.add(runButton);
        filters.add(print);
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
        JPanel reportBody = new JPanel(new BorderLayout(0, 10));
        reportBody.setOpaque(false);
        chart.setPreferredSize(new Dimension(0, 220));
        reportBody.add(chart, BorderLayout.NORTH);
        reportBody.add(scroll, BorderLayout.CENTER);
        add(reportBody, BorderLayout.CENTER);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(MUTED);
        add(status, BorderLayout.SOUTH);

        LocalDate today = LocalDate.now();
        fromDate.setText(today.minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toDate.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        runButton.addActionListener(e -> refreshReport());
        print.addActionListener(e -> printReport());
    }

    /** Override when a report has a specific metric that should drive its chart. */
    protected int chartValueColumn(String[] headers) { return -1; }

    private void scheduleFilterRefresh() {
        filterRefreshTimer.restart();
    }

    private void clearDisplayedReport() {
        if (activeWorker != null) activeWorker.cancel(true);
        runButton.setEnabled(true);
        model.setRowCount(0);
        chart.setRows(java.util.Collections.<Object[]>emptyList(), columns, chartValueColumn(columns));
    }

    protected final void refreshReport() {
        if (!fromDate.isValidDate() || !toDate.isValidDate()) {
            clearDisplayedReport();
            status.setForeground(new Color(180, 45, 55));
            status.setText("Vui lòng nhập ngày hợp lệ theo định dạng dd/MM/yyyy.");
            return;
        }
        LocalDate from = fromDate.getLocalDate();
        LocalDate to = toDate.getLocalDate();
        if (from.isAfter(to)) {
            clearDisplayedReport();
            status.setForeground(new Color(180, 45, 55));
            status.setText("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
            return;
        }
        ChuKyBaoCao selected = (ChuKyBaoCao) period.getSelectedItem();
        if (activeWorker != null) activeWorker.cancel(true);
        runButton.setEnabled(false);
        status.setForeground(MUTED);
        status.setText("Đang lấy số liệu từ SQL Server...");
        activeWorker = new SwingWorker<DieuKhienBaoCaoNgay.KetQua, Void>() {
            @Override protected DieuKhienBaoCaoNgay.KetQua doInBackground() throws Exception {
                return reportController.fetch(reportType, from, to, selected.name());
            }

            @Override protected void done() {
                if (isCancelled()) return;
                runButton.setEnabled(true);
                try {
                    DieuKhienBaoCaoNgay.KetQua result = get();
                    model.setRowCount(0);
                    for (Object[] row : result.rows) model.addRow(row);
                    chart.setRows(result.rows, columns, chartValueColumn(columns));
                    reportSubtitle = "Từ " + from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " đến " + to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + " — nhóm theo " + selected.label;
                    status.setForeground(MUTED);
                    status.setText(result.summary + "  |  " + reportSubtitle);
                } catch (Exception ex) {
                    model.setRowCount(0);
                    chart.setRows(java.util.Collections.<Object[]>emptyList(), columns, chartValueColumn(columns));
                    Throwable cause = ex.getCause();
                    status.setForeground(new Color(180, 45, 55));
                    status.setText("Không thể tải báo cáo SQL Server: "
                            + (cause == null ? ex.getMessage() : cause.getMessage()));
                }
            }
        };
        activeWorker.execute();
    }

    protected enum ChuKyBaoCao {
        DAY("Ngày"), MONTH("Tháng"), YEAR("Năm");
        final String label;
        ChuKyBaoCao(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private void styleButton(JButton button, Color color) {
        final Color normalColor = color.darker();
        final Color hoverColor = normalColor.darker();
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(normalColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(hoverColor); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { button.setBackground(normalColor); }
        });
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

    private void printReport() {
        try {
            table.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat(title + " — " + reportSubtitle),
                    new java.text.MessageFormat("Trang {0}"), true, null, true);
        } catch (PrinterException ex) {
            showError("Không thể in báo cáo: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi báo cáo", JOptionPane.ERROR_MESSAGE);
    }

    private static final class ReportChartPanel extends JPanel {
        private List<Object[]> rows = java.util.Collections.emptyList();
        private String[] headers = new String[0];
        private int valueColumn = -1;

        ReportChartPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_LIGHT), new EmptyBorder(10, 14, 10, 14)));
        }

        void setRows(List<Object[]> rows, String[] headers, int preferredColumn) {
            this.rows = new ArrayList<>(rows);
            this.headers = headers.clone();
            valueColumn = preferredColumn >= 0 && preferredColumn < headers.length ? preferredColumn : -1;
            for (int c = headers.length - 1; valueColumn < 0 && c >= 0; c--) {
                for (Object[] row : rows) if (number(row[c]) != null) { valueColumn = c; break; }
                if (valueColumn >= 0) break;
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            String metric = valueColumn < 0 ? "Tổng quan báo cáo" : "Biểu đồ: " + headers[valueColumn];
            g2.drawString(metric, 12, 22);
            if (rows.isEmpty() || valueColumn < 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(MUTED);
                g2.drawString("Chọn khoảng thời gian và bấm Thống kê để hiển thị dữ liệu.", 12, 54);
                g2.dispose();
                return;
            }
            java.util.LinkedHashMap<String, Double> grouped = new java.util.LinkedHashMap<>();
            for (Object[] row : rows) {
                if (row.length == 0) continue;
                Double value = number(row[valueColumn]);
                if (value != null) grouped.merge(String.valueOf(row[0]), value, Double::sum);
            }
            List<java.util.Map.Entry<String, Double>> points = new ArrayList<>(grouped.entrySet());
            if (points.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(MUTED);
                g2.drawString("Không có số liệu cho chỉ số này trong kỳ đã chọn.", 12, 54);
                g2.dispose();
                return;
            }
            int count = Math.min(points.size(), 12);
            int left = 48, top = 38, bottom = getHeight() - 35, width = getWidth() - left - 22;
            double max = 0;
            for (int i = points.size() - count; i < points.size(); i++) {
                max = Math.max(max, points.get(i).getValue());
            }
            if (max <= 0) max = 1;
            g2.setColor(new Color(231, 238, 248));
            g2.drawLine(left, bottom, left + width, bottom);
            int slot = Math.max(1, width / count);
            for (int i = 0; i < count; i++) {
                java.util.Map.Entry<String, Double> point = points.get(points.size() - count + i);
                Double value = point.getValue();
                int barHeight = (int) ((Math.max(0, value) / max) * Math.max(1, bottom - top));
                int x = left + i * slot + Math.max(2, slot / 5);
                int barWidth = Math.max(8, slot * 3 / 5);
                g2.setColor(new Color(42, 122, 219));
                g2.fillRoundRect(x, bottom - barHeight, barWidth, barHeight, 7, 7);
                g2.setColor(MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = point.getKey();
                if (label.matches("\\d{2}/\\d{2}/\\d{4}")) label = label.substring(0, 5);
                else if (label.length() > 9) label = label.substring(0, 9);
                g2.drawString(label, x - 2, bottom + 15);
            }
            g2.dispose();
        }

        private static Double number(Object value) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            if (value == null) return null;
            String text = value.toString().replaceAll("[^0-9,.-]", "").replace(",", "");
            try { return text.isEmpty() ? null : Double.valueOf(text); }
            catch (NumberFormatException ex) { return null; }
        }
    }
}
