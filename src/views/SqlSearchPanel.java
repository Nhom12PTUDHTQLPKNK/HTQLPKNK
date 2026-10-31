package views;

import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Shared SQL-backed lookup UI styled after the customer and appointment lookups. */
public class SqlSearchPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private final String query;
    private final String[] searchColumns;
    private final DefaultTableModel tableModel;
    private final JTextField txtSearch;
    private final JLabel lblStatus;
    private final JButton btnSearch;
    private SwingWorker<List<Object[]>, Void> currentLoad;

    public SqlSearchPanel(String title, String subtitle, String[] columns,
                          String query, String exportTitle, String... searchColumns) {
        this.query = query;
        this.searchColumns = searchColumns.clone();
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(TEXT_DARK);
        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)));
        btnSearch = createButton("Tra Cứu", BLUE_PRIMARY);
        JButton btnReload = createButton("Tải lại", new Color(108, 117, 125));
        btnSearch.addActionListener(e -> reload(txtSearch.getText().trim()));
        btnReload.addActionListener(e -> {
            txtSearch.setText("");
            reload("");
        });
        txtSearch.addActionListener(e -> reload(txtSearch.getText().trim()));
        toolbar.add(searchLabel);
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnReload);
        header.add(toolbar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        PdfTableExport.install(table, exportTitle);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        add(scroll, BorderLayout.CENTER);

        lblStatus = new JLabel("Đang tải dữ liệu từ SQL Server...");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(MUTED);
        add(lblStatus, BorderLayout.SOUTH);
        reload("");
    }

    public final void reload(String keyword) {
        final String search = keyword == null ? "" : keyword.trim();
        if (currentLoad != null) currentLoad.cancel(true);
        btnSearch.setEnabled(false);
        lblStatus.setForeground(MUTED);
        lblStatus.setText("Đang tải dữ liệu từ SQL Server...");
        currentLoad = new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                StringBuilder sql = new StringBuilder("SELECT * FROM (")
                        .append(query).append(") AS lookupData WHERE (? = N''");
                for (String column : searchColumns) {
                    sql.append(" OR CAST(lookupData.[").append(column)
                            .append("] AS NVARCHAR(4000)) LIKE ?");
                }
                sql.append(" ORDER BY 1");
                List<Object[]> rows = new ArrayList<>();
                try (Connection connection = DBConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                    statement.setString(1, search);
                    for (int i = 0; i < searchColumns.length; i++) {
                        statement.setString(i + 2, "%" + search + "%");
                    }
                    try (ResultSet result = statement.executeQuery()) {
                        int columnCount = tableModel.getColumnCount();
                        while (result.next()) {
                            Object[] row = new Object[columnCount];
                            for (int column = 0; column < columnCount; column++) {
                                row[column] = result.getObject(column + 1);
                            }
                            rows.add(row);
                        }
                    }
                }
                return rows;
            }

            @Override protected void done() {
                if (isCancelled()) return;
                btnSearch.setEnabled(true);
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                    lblStatus.setForeground(MUTED);
                    lblStatus.setText("Tìm thấy " + rows.size() + " kết quả.");
                } catch (Exception ex) {
                    tableModel.setRowCount(0);
                    Throwable cause = ex.getCause();
                    String message = cause instanceof SQLException
                            ? cause.getMessage() : ex.getMessage();
                    lblStatus.setForeground(new Color(180, 45, 55));
                    lblStatus.setText("Không thể tải dữ liệu SQL Server: " + message);
                }
            }
        };
        currentLoad.execute();
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(text.equals("Tra Cứu") ? 90 : 80, 36));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
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
}
