package views;

import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class HelpPanel extends JPanel {
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color NAVY = new Color(15, 29, 69);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    public HelpPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // =========================================================================
        // 1. HEADER TIÊU ĐỀ & THANH CÔNG CỤ TÌM KIẾM NHANH
        // =========================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("📖 Trợ Giúp & Hướng Dẫn Phím Tắt Hệ Thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Tra cứu nhanh các nhóm menu hệ thống và tổ hợp phím tắt thao tác nhanh cho người dùng");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSubtitle);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // Ô tìm kiếm nhanh phím tắt
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchBar.setOpaque(false);

        JTextField txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        searchBar.add(new JLabel("Tra cứu:"));
        searchBar.add(txtSearch);
        headerPanel.add(searchBar, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // =========================================================================
        // 2. DỮ LIỆU BẢNG (GIỮ NGUYÊN 100% DỮ LIỆU CŨ CỦA BẠN)
        // =========================================================================
        String[] columns = {"Nhóm Menu Hệ Thống", "Chức năng chi tiết", "Phím tắt nhanh"};
        Object[][] data = {
            // 1. Hệ thống
            {"1. HỆ THỐNG", "Bảng điều khiển (Dashboard)", "F5"},
            {"1. HỆ THỐNG", "Đổi mật khẩu", "Ctrl + M"},
            {"1. HỆ THỐNG", "Tài khoản", "Ctrl + P"},
            {"1. HỆ THỐNG", "Trợ giúp", "Shift + F1"},
            {"1. HỆ THỐNG", "Nhật kí hoạt động", "Alt + L"},
            {"1. HỆ THỐNG", "Đăng xuất", "Ctrl + Q"},
            {"1. HỆ THỐNG", "Thoát ứng dụng", "Alt + F4"},

            // 2. Danh mục
            {"2. DANH MỤC", "Dịch vụ", "Ctrl + D"},
            {"2. DANH MỤC", "Khuyến mãi", "Ctrl + O"},
            {"2. DANH MỤC", "Khách hàng", "Ctrl + K"},
            {"2. DANH MỤC", "Nhân viên", "Ctrl + E"},
            {"2. DANH MỤC", "Hồ sơ Bệnh án", "Ctrl + T"},

            // 3. Cập nhật
            {"3. CẬP NHẬT", "Cập nhật kho", "Ctrl + H"},
            {"3. CẬP NHẬT", "Tiếp nhận lịch hẹn mới", "Ctrl + N"},
            {"3. CẬP NHẬT", "Cập nhật nhân sự, Tài khoản", "Ctrl + U"},

            // 4. Xử lý
            {"4. XỬ LÝ", "Đặt lịch", "Ctrl + B"},
            {"4. XỬ LÝ", "Bán hàng, Lập hóa đơn", "F1"},
            {"4. XỬ LÝ", "Phân ca lịch làm việc", "F2"},
            {"4. XỬ LÝ", "Cập nhật khuyến mãi", "F3"},

            // 5. Tìm kiếm
            {"5. TÌM KIẾM", "Tra cứu khách hàng, Điểm tích lũy", "F6"},
            {"5. TÌM KIẾM", "Tra cứu Lịch hẹn", "F7"},
            {"5. TÌM KIẾM", "Tra cứu khuyến mãi", "F8"},
            {"5. TÌM KIẾM", "Tra cứu lịch làm việc", "F9"},
            {"5. TÌM KIẾM", "Tra cứu Tồn kho", "F10"},
            {"5. TÌM KIẾM", "Tra cứu Hóa đơn", "F11"},

            // 6. Báo biểu
            {"6. BÁO BIỂU", "Thống kê doanh thu", "Ctrl + Shift + R"},
            {"6. BÁO BIỂU", "Thống kê dịch vụ", "Ctrl + Shift + E"},
            {"6. BÁO BIỂU", "Thống kê xuất nhập kho", "Ctrl + Shift + A"},
            {"6. BÁO BIỂU", "Lịch làm việc", "Ctrl + Shift + Y"}
        };

        DefaultTableModel tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        PdfTableExport.install(table, "Bảng hướng dẫn");

        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 253));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Căn chỉnh các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Căn giữa nhóm menu
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Căn giữa phím tắt

        // Thêm bộ lọc tìm kiếm nhanh cho bảng
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        txtSearch.addActionListener(e -> {
            String kw = txtSearch.getText().trim();
            if (kw.isEmpty()) rowSorter.setRowFilter(null);
            else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        // =========================================================================
        // 3. THIẾT KẾ KHUNG CARD BỌC NGOÀI ĐỒNG BỘ VỚI TOÀN HỆ THỐNG
        // =========================================================================
        JPanel centerWrap = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(15, 15, 15, 15));
        centerWrap.add(scrollPane, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.CENTER);
    }
}