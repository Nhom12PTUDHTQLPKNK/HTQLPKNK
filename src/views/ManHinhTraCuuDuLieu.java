package views;

import controllers.XuatBangPDF;
import controllers.TruongChonNgayCu;
import models.dao.TruyVanTraCuuDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ManHinhTraCuuDuLieu extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private final String query;
    private final TruyVanTraCuuDAO searchDAO = new TruyVanTraCuuDAO();
    private final String[] searchColumns;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JLabel lblStatus;
    private final JButton btnSearch;
    private final String dateFilterColumn;

    // DatePicker tự viết, không sử dụng thư viện bên ngoài
    private final TruongChonNgayCu datePicker;

    private SwingWorker<List<Object[]>, Void> currentLoad;

    public ManHinhTraCuuDuLieu(String title, String subtitle,
                          String[] columns, String query,
                          String exportTitle,
                          String... searchColumns) {

        this(title, subtitle, columns, query,
                exportTitle, searchColumns, null);
    }

    public ManHinhTraCuuDuLieu(String title, String subtitle,
                          String[] columns, String query,
                          String exportTitle,
                          String[] searchColumns,
                          String dateFilterColumn) {

        this.query = query;
        this.searchColumns = searchColumns.clone();
        this.dateFilterColumn = dateFilterColumn;

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // ================= HEADER =================

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_DARK);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitleLabel);

        header.add(titleBox, BorderLayout.WEST);

        // ================= TOOLBAR =================

        JPanel toolbar = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0));

        toolbar.setOpaque(false);

        if (dateFilterColumn != null) {

            datePicker = new TruongChonNgayCu(null);

            datePicker.picker.setPreferredSize(
                    new Dimension(150, 36));

            JLabel dateLabel = new JLabel("Ngày trực:");
            dateLabel.setFont(
                    new Font("Segoe UI", Font.BOLD, 12));

            dateLabel.setForeground(TEXT_DARK);

            toolbar.add(dateLabel);
            toolbar.add(datePicker.picker);

        } else {
            datePicker = null;
        }

        JLabel searchLabel = new JLabel(
                dateFilterColumn == null
                        ? "Tìm kiếm:"
                        : "Mã lịch / mã NV:");

        searchLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        searchLabel.setForeground(TEXT_DARK);

        txtSearch = new JTextField(18);

        txtSearch.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        txtSearch.setPreferredSize(
                new Dimension(180, 36));

        txtSearch.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_LIGHT, 1),
                        new EmptyBorder(4, 10, 4, 10)));

        btnSearch = createButton("Tra Cứu", BLUE_PRIMARY);

        JButton btnReload = createButton(
                "Tải lại", new Color(74, 85, 96));

        btnSearch.addActionListener(
                e -> reloadFromControls());

        btnReload.addActionListener(e -> {

            txtSearch.setText("");

            if (datePicker != null) {
                datePicker.setDate(null);
            }

            reload("");
        });

        txtSearch.addActionListener(
                e -> reloadFromControls());

        toolbar.add(searchLabel);
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnReload);

        header.add(toolbar, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ================= TABLE =================

        tableModel = new DefaultTableModel(null, columns) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);

        XuatBangPDF.install(table, exportTitle);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        table.setRowSorter(sorter);

        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1));

        scroll.getViewport().setBackground(CARD_BG);

        add(scroll, BorderLayout.CENTER);

        // ================= STATUS =================

        lblStatus = new JLabel(
                "Đang tải dữ liệu từ SQL Server...");

        lblStatus.setFont(
                new Font("Segoe UI", Font.PLAIN, 12));

        lblStatus.setForeground(MUTED);

        add(lblStatus, BorderLayout.SOUTH);

        reload("");
    }

    // ================= RELOAD =================

    public final void reload(String keyword) {
        reload(keyword, null);
    }

    private void reloadFromControls() {

        LocalDate selectedDate = null;

        if (datePicker != null) {
            selectedDate = datePicker.getDate();
        }

        reload(txtSearch.getText().trim(), selectedDate);
    }

    private void reload(String keyword, LocalDate selectedDate) {

        final String search =
                keyword == null ? "" : keyword.trim();

        if (currentLoad != null) {
            currentLoad.cancel(true);
        }

        btnSearch.setEnabled(false);

        lblStatus.setForeground(MUTED);
        lblStatus.setText(
                "Đang tải dữ liệu từ SQL Server...");

        currentLoad = new SwingWorker<List<Object[]>, Void>() {

            @Override
            protected List<Object[]> doInBackground()
                    throws Exception {
                return searchDAO.search(query, searchColumns, search, selectedDate,
                        dateFilterColumn, tableModel.getColumnCount());
            }

            @Override
            protected void done() {

                if (isCancelled()) {
                    return;
                }

                btnSearch.setEnabled(true);

                try {

                    List<Object[]> rows = get();

                    tableModel.setRowCount(0);

                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }

                    lblStatus.setForeground(MUTED);

                    lblStatus.setText(
                            "Tìm thấy " + rows.size()
                            + " kết quả.");

                } catch (Exception ex) {

                    tableModel.setRowCount(0);

                    Throwable cause = ex.getCause();

                    String message =
                            cause instanceof SQLException
                                    ? cause.getMessage()
                                    : ex.getMessage();

                    lblStatus.setForeground(
                            new Color(180, 45, 55));

                    lblStatus.setText(
                            "Không thể tải dữ liệu SQL Server: "
                            + message);
                }
            }
        };

        currentLoad.execute();
    }

    // ================= BUTTON =================

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 =
                        (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(color);

                g2.fillRoundRect(
                        0, 0,
                        getWidth(), getHeight(),
                        8, 8);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12));

        button.setForeground(Color.WHITE);

        button.setPreferredSize(
                new Dimension(
                        text.equals("Tra Cứu") ? 90 : 80,
                        36));

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        return button;
    }

    // ================= TABLE STYLE =================

    private void styleTable(JTable table) {

        table.setRowHeight(38);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 13));

        table.setForeground(TEXT_DARK);

        table.setGridColor(
                new Color(240, 244, 248));

        table.setSelectionBackground(
                new Color(225, 240, 255));

        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));

        table.getTableHeader().setBackground(
                new Color(245, 248, 253));

        table.getTableHeader().setForeground(TEXT_DARK);

        table.getTableHeader().setPreferredSize(
                new Dimension(0, 36));
    }
}
