package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class SearchPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private CardLayout subCardLayout;
    private JPanel contentContainerPanel;

    public static final String SUB_CUSTOMER = "SUB_CUSTOMER";
    public static final String SUB_APPOINTMENT = "SUB_APPOINTMENT";
    public static final String SUB_PROMOTION = "SUB_PROMOTION";
    public static final String SUB_SHIFT = "SUB_SHIFT";
    public static final String SUB_INVENTORY = "SUB_INVENTORY";
    public static final String SUB_INVOICE = "SUB_INVOICE";

    public SearchPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. THANH ĐIỀU HƯỚNG CHUYỂN TRANG TRA CỨU (TABS)
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navBar.setOpaque(false);

        navBar.add(createNavButton("Khách Hàng (F6)", SUB_CUSTOMER));
        navBar.add(createNavButton("Lịch Hẹn (F7)", SUB_APPOINTMENT));
        navBar.add(createNavButton("Khuyến Mãi (F8)", SUB_PROMOTION));
        navBar.add(createNavButton("Lịch Làm Việc (F9)", SUB_SHIFT));
        navBar.add(createNavButton("Tồn Kho (F10)", SUB_INVENTORY));
        navBar.add(createNavButton("Hóa Đơn (F11)", SUB_INVOICE));

        add(navBar, BorderLayout.NORTH);

        // 2. VÙNG NỘI DUNG CHÍNH (CARDLAYOUT CHO CÁC TRANG TRA CỨU)
        subCardLayout = new CardLayout();
        contentContainerPanel = new JPanel(subCardLayout);
        contentContainerPanel.setOpaque(false);

        // Trang 1: Khách hàng
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Khách Hàng & Điểm Tích Lũy (F6)", 
            "Tra cứu thông tin chi tiết, ngày tạo tài khoản và hạng thành viên của khách hàng",
            new String[]{"Mã KH", "Họ và Tên", "Số Điện Thoại", "Điểm Tích Lũy", "Hạng", "Ngày Tạo"},
            new Object[][]{
                {"KH001", "Nguyễn Gia Bằng", "0901122334", "1250", "Vàng", "15/01/2026"},
                {"KH002", "Nguyễn Hoàng Khải", "0988776655", "450", "Bạc", "20/02/2026"},
                {"KH003", "Minh Châu", "0912345678", "890", "Vàng", "10/03/2026"}
            }
        ), SUB_CUSTOMER);

        // Trang 2: Lịch hẹn
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Lịch Hẹn Khám (F7)",
            "Tra cứu lịch hẹn khám dịch vụ nha khoa và thời gian lập lịch",
            new String[]{"Mã Lịch", "Khách Hàng", "SĐT", "Dịch Vụ", "Nha Sĩ", "Ngày Hẹn", "Ngày Tạo Lịch", "Trạng Thái"},
            new Object[][]{
                {"LH001", "Nguyễn Gia Bằng", "0901122334", "Khám tổng quát", "BS. Sỹ Luân", "21/10/2026", "18/10/2026", "Đã xác nhận"},
                {"LH002", "Nguyễn Hoàng Khải", "0988776655", "Lấy cao răng", "BS. Ngọc Ánh", "22/10/2026", "19/10/2026", "Chờ khám"}
            }
        ), SUB_APPOINTMENT);

        // Trang 3: Khuyến mãi
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Chương Trình Khuyến Mãi (F8)",
            "Tra cứu các chương trình ưu đãi, giảm giá và thời gian thiết lập",
            new String[]{"Mã KM", "Tên Chương Trình", "Mức Giảm", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Ngày Tạo", "Trạng Thái"},
            new Object[][]{
                {"KM001", "Ưu đãi niềng răng tháng 10", "20%", "01/10/2026", "31/10/2026", "25/09/2026", "Đang áp dụng"},
                {"KM002", "Giảm giá lấy cao răng HSSV", "50%", "05/10/2026", "25/10/2026", "01/10/2026", "Đang áp dụng"}
            }
        ), SUB_PROMOTION);

        // Trang 4: Lịch làm việc
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Lịch Làm Việc Nhân Viên (F9)",
            "Tra cứu lịch phân ca trực và thời gian phân bổ công việc",
            new String[]{"Mã Phân Ca", "Nhân Viên", "Ca Trực", "Ngày Trực", "Ngày Tạo Ca", "Ghi Chú"},
            new Object[][]{
                {"PC001", "Nguyễn Minh Sỹ Luân", "Ca Sáng (08:00 - 12:00)", "21/10/2026", "19/10/2026", "Trực chính"},
                {"PC002", "Phan Lê Ngọc Ánh", "Ca Chiều (13:30 - 17:30)", "21/10/2026", "19/10/2026", "Trực phụ"}
            }
        ), SUB_SHIFT);

        // Trang 5: Tồn kho
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Tồn Kho Vật Tư & Thuốc (F10)",
            "Tra cứu số lượng tồn kho vật tư nha khoa và thời gian cập nhật kho",
            new String[]{"Mã Vật Tư", "Tên Vật Tư / Thuốc", "Danh Mục", "Tồn Kho", "Đơn Vị", "Ngày Cập Nhật"},
            new Object[][]{
                {"VT001", "Thuốc tê Septanest", "Thuốc gây tê", "150", "Ống", "10/10/2026"},
                {"VT002", "Mắc cài sứ niềng răng", "Vật tư nha khoa", "45", "Bộ", "12/10/2026"}
            }
        ), SUB_INVENTORY);

        // Trang 6: Hóa đơn
        contentContainerPanel.add(createSearchTemplate(
            "Tra Cứu Lịch Sử Hóa Đơn Thanh Toán (F11)",
            "Tra cứu danh sách hóa đơn thanh toán dịch vụ và thời gian giao dịch",
            new String[]{"Mã HĐ", "Khách Hàng", "Nhân Viên Lập", "Tổng Tiền (VNĐ)", "Thời Gian Lập"},
            new Object[][]{
                {"HD001", "Nguyễn Gia Bằng", "Phan Lê Ngọc Ánh", "400.000", "20/10/2026 14:15"},
                {"HD002", "Nguyễn Hoàng Khải", "Nguyễn Minh Sỹ Luân", "800.000", "20/10/2026 15:30"}
            }
        ), SUB_INVOICE);

        add(contentContainerPanel, BorderLayout.CENTER);
    }

    public void showSubView(String subCardName) {
        subCardLayout.show(contentContainerPanel, subCardName);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        btn.addActionListener(e -> showSubView(cardName));
        return btn;
    }

    private JPanel createSearchTemplate(String titleText, String subText, String[] cols, Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Header trang
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Thanh công cụ tìm kiếm nhanh (Đã bỏ ComboBox, chỉ giữ lại JTextField và nút Tìm)
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JTextField txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JButton btnSearch = new JButton("Tìm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setPreferredSize(new Dimension(75, 36));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setOpaque(false);
        btnSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        
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

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Xử lý sự kiện tìm kiếm nhanh trên toàn bộ bảng
        Runnable executeSearch = () -> {
            String kw = txtSearch.getText().trim();
            if (kw.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
            }
        };

        txtSearch.addActionListener(e -> executeSearch.run());
        btnSearch.addActionListener(e -> executeSearch.run());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }
}