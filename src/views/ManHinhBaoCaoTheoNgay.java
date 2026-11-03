package views;

import controllers.TruongChonNgay;
import models.dao.TruyVanBaoCaoNgayDAO;
import models.dao.BaoCaoPhongKhamDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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
    private final String[] columns;
    private final DefaultTableModel model;
    private final JTable table;
    private final TruongChonNgay fromDate = new TruongChonNgay();
    private final TruongChonNgay toDate = new TruongChonNgay();
    private final JComboBox<ChuKyBaoCao> period = new JComboBox<>(ChuKyBaoCao.values());
    private final JLabel status = new JLabel("Chọn khoảng thời gian rồi bấm Thống kê.");
    private final JButton runButton = new JButton("Thống kê");
    private SwingWorker<KetQuaBaoCao, Void> activeWorker;
    private String reportSubtitle = "";

    protected ManHinhBaoCaoTheoNgay(String title, String description, String[] columns) {
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
        add(scroll, BorderLayout.CENTER);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.setForeground(MUTED);
        add(status, BorderLayout.SOUTH);

        LocalDate today = LocalDate.now();
        fromDate.setText(today.minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        toDate.setText(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        runButton.addActionListener(e -> refreshReport());
        print.addActionListener(e -> printReport());
    }

    protected abstract KetQuaBaoCao fetchReport(LocalDate from, LocalDate to, ChuKyBaoCao period) throws Exception;

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
        ChuKyBaoCao selected = (ChuKyBaoCao) period.getSelectedItem();
        if (activeWorker != null) activeWorker.cancel(true);
        runButton.setEnabled(false);
        status.setForeground(MUTED);
        status.setText("Đang lấy số liệu từ SQL Server...");
        activeWorker = new SwingWorker<KetQuaBaoCao, Void>() {
            @Override protected KetQuaBaoCao doInBackground() throws Exception {
                return fetchReport(from, to, selected);
            }

            @Override protected void done() {
                if (isCancelled()) return;
                runButton.setEnabled(true);
                try {
                    KetQuaBaoCao result = get();
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

    protected enum ChuKyBaoCao {
        DAY("Ngày"), MONTH("Tháng"), YEAR("Năm");
        final String label;
        ChuKyBaoCao(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    protected static final class KetQuaBaoCao {
        final List<Object[]> rows;
        final String summary;
        protected KetQuaBaoCao(List<Object[]> rows, String summary) {
            this.rows = rows;
            this.summary = summary;
        }
    }

    protected static String periodExpression(String dateColumn, ChuKyBaoCao period) {
        return BaoCaoPhongKhamDAO.periodExpression(dateColumn, period.name());
    }

    protected static KetQuaBaoCao query(String sql, LocalDate from, LocalDate to,
                                        int columnCount) throws Exception {
        List<Object[]> rows = new TruyVanBaoCaoNgayDAO().query(sql, from, to, columnCount);
        return new KetQuaBaoCao(rows, "Số dòng báo cáo: " + rows.size());
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
}
