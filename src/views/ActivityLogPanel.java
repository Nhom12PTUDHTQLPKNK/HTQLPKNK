package views;

import Audit.AuditLog;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;

public class ActivityLogPanel extends JPanel {
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color GRAY_BTN = new Color(108, 117, 125);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);

    private JTextField txtSearch;
    private JTable actionTable, sessionTable;
    private DefaultTableModel actionTableModel, sessionTableModel;
    private TableRowSorter<DefaultTableModel> actionRowSorter, sessionRowSorter;

    public ActivityLogPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // =========================================================================
        // 1. HEADER & THANH CÔNG CỤ (TÌM KIẾM & LỌC)
        // =========================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Nhật Ký Hoạt Động");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Theo dõi chi tiết lịch sử thao tác nghiệp vụ và phiên đăng nhập của nhân sự");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // Thanh công cụ tìm kiếm bên phải
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Từ khóa:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("🔍 Tìm", BLUE);
        btnSearch.setPreferredSize(new Dimension(80, 36));
        btnSearch.addActionListener(e -> performSearch());

        JButton btnRefresh = createStaticButton("🔄 Làm mới", GRAY_BTN);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.addActionListener(e -> resetFilter());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(Box.createHorizontalStrut(5));
        toolbarPanel.add(btnRefresh);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // =========================================================================
        // 2. CÁC TAB NỘI DUNG NHẬT KÝ
        // =========================================================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("Nhật ký thao tác nghiệp vụ", createActionLogTab());
        tabbedPane.addTab("Phiên đăng nhập & Thời gian online", createSessionLogTab());

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(5, 0, 0, 0));
        centerWrap.add(tabbedPane, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);
    }

    /**
     * Tab 1: Nhật ký thao tác nghiệp vụ (Thêm, Sửa, Xóa, Lập hóa đơn, Đặt lịch...)
     */
    private JPanel createActionLogTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Mã Log", "Thời gian", "Tài khoản", "Vai trò", "Hành động", "Chi tiết nghiệp vụ"};
        Object[][] data = {
            {"LOG001", "19/10/2026 08:30:15", "admin01", "NV Quản lý", "THÊM TÀI KHOẢN", "Tạo mới tài khoản nhân viên letan02"},
            {"LOG002", "19/10/2026 09:15:22", "letan01", "Lễ tân", "ĐẶT LỊCH HẸN", "Đăng ký lịch khám tổng quát cho bệnh nhân Nguyễn Văn A"},
            {"LOG003", "19/10/2026 10:05:40", "bacsi01", "Nha sĩ", "CẬP NHẬT BỆNH ÁN", "Thêm kết quả điều trị răng số 16 cho mã BN0012"},
            {"LOG004", "19/10/2026 11:20:05", "letan01", "Lễ tân", "LẬP HÓA ĐƠN", "Thanh toán dịch vụ cạo vôi răng (320.000 đ)"}
        };

        actionTableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        actionTable = new JTable(actionTableModel);
        PdfTableExport.install(actionTable, "Nhật ký hoạt động");
        actionRowSorter = new TableRowSorter<>(actionTableModel);
        actionTable.setRowSorter(actionRowSorter);

        styleTable(actionTable);

        // Căn giữa cột Mã log và Thời gian
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        actionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        actionTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(actionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Tab 2: Quản lý phiên đăng nhập và thời gian online
     */
    private JPanel createSessionLogTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] columns = {"Tài khoản", "Vai trò", "Thời điểm đăng nhập", "Thời điểm đăng xuất", "Thời gian online", "Trạng thái"};
        Object[][] data = {
            {"admin01", "NV Quản lý", "19/10/2026 23:45:01", "—", "00:15:24", "Đang online"},
            {"letan01", "Lễ tân", "19/10/2026 08:00:00", "19/10/2026 17:00:00", "09:00:00", "Đăng xuất"},
            {"bacsi01", "Nha sĩ", "19/10/2026 08:15:10", "19/10/2026 12:00:00", "03:44:50", "Đóng ứng dụng"}
        };

        sessionTableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        sessionTable = new JTable(sessionTableModel);
        PdfTableExport.install(sessionTable, "Nhật ký phiên đăng nhập");
        sessionRowSorter = new TableRowSorter<>(sessionTableModel);
        sessionTable.setRowSorter(sessionRowSorter);

        styleTable(sessionTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        sessionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        sessionTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        sessionTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // Màu sắc trạng thái phiên
        sessionTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                String status = (String) value;
                if ("Đang online".equals(status)) {
                    c.setForeground(GREEN_ACCENT);
                } else {
                    c.setForeground(MUTED);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(sessionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }


    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        RowFilter<DefaultTableModel, Object> rf = null;
        if (!keyword.isEmpty()) {
            try {
                // Tìm kiếm không phân biệt hoa thường trên toàn bộ các cột
                rf = RowFilter.regexFilter("(?i)" + keyword);
            } catch (java.util.regex.PatternSyntaxException e) {
                rf = null;
            }
        }
        if (actionRowSorter != null) actionRowSorter.setRowFilter(rf);
        if (sessionRowSorter != null) sessionRowSorter.setRowFilter(rf);
    }

    /**
     * Xóa bộ lọc và làm mới lại dữ liệu hiển thị
     */
    private void resetFilter() {
        txtSearch.setText("");
        if (actionRowSorter != null) actionRowSorter.setRowFilter(null);
        if (sessionRowSorter != null) sessionRowSorter.setRowFilter(null);
        refreshTables();
    }

    public void refreshTables() {
        if (actionTableModel == null || sessionTableModel == null) return;
        actionTableModel.setRowCount(0);
        for (Object[] row : AuditLog.eventRows(LocalDate.MIN, LocalDate.MAX)) {
            actionTableModel.addRow(row);
        }
        sessionTableModel.setRowCount(0);
        for (Object[] row : AuditLog.sessionRows(LocalDate.MIN, LocalDate.MAX)) {
            sessionTableModel.addRow(row);
        }
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

    private JButton createStaticButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
