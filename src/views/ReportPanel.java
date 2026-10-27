package views;

import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ReportPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private CardLayout subCardLayout;
    private JPanel contentContainerPanel;

    public static final String SUB_REVENUE = "SUB_REVENUE";
    public static final String SUB_SERVICE = "SUB_SERVICE";
    public static final String SUB_IMPORT_EXPORT = "SUB_IMPORT_EXPORT";

    public ReportPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. THANH ĐIỀU HƯỚNG CHUYỂN TRANG BÁO BIỂU (TABS)
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navBar.setOpaque(false);

        navBar.add(createNavButton("Thống Kê Doanh Thu", SUB_REVENUE));
        navBar.add(createNavButton("Thống Kê Dịch Vụ", SUB_SERVICE));
        navBar.add(createNavButton("Thống Kê Xuất Nhập Kho", SUB_IMPORT_EXPORT));

        add(navBar, BorderLayout.NORTH);

        // 2. VÙNG NỘI DUNG CHÍNH (CARDLAYOUT CHO CÁC BÁO CÁO)
        subCardLayout = new CardLayout();
        contentContainerPanel = new JPanel(subCardLayout);
        contentContainerPanel.setOpaque(false);

        // Dữ liệu mẫu biểu đồ cho Doanh thu
        String[] chartLabelsRevenue = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        int[] chartValuesRevenue = {400, 800, 1200, 650, 950, 1500, 1800};

        contentContainerPanel.add(createReportWithChartTemplate(
            "Báo Cáo Thống Kê Doanh Thu Phòng Khám", 
            "Tổng hợp doanh thu dịch vụ nha khoa và bán thuốc theo tuần (Đơn vị: Triệu VNĐ)",
            chartLabelsRevenue, chartValuesRevenue,
            new String[]{"Mã HĐ", "Khách Hàng", "Thu Ngân Lập", "Tiền Dịch Vụ (VNĐ)", "Tiền Thuốc (VNĐ)", "Tổng Doanh Thu (VNĐ)", "Thời Gian"},
            new Object[][]{
                {"HD001", "Nguyễn Gia Bằng", "Phan Lê Ngọc Ánh", "250.000", "150.000", "400.000", "20/10/2026 14:15"},
                {"HD002", "Nguyễn Hoàng Khải", "Nguyễn Minh Sỹ Luân", "500.000", "300.000", "800.000", "20/10/2026 15:30"},
                {"HD003", "Minh Châu", "Phan Lê Ngọc Ánh", "1.200.000", "0", "1.200.000", "21/10/2026 09:10"}
            }
        ), SUB_REVENUE);

        // Dữ liệu mẫu biểu đồ cho Dịch vụ
        String[] chartLabelsService = {"Khám Tổng Quát", "Lấy Cao Răng", "Trám Răng", "Niềng Răng", "Bọc Răng Sứ"};
        int[] chartValuesService = {25, 40, 30, 15, 20};

        contentContainerPanel.add(createReportWithChartTemplate(
            "Báo Cáo Thống Kê Dịch Vụ Khám Chữa Bệnh",
            "Thống kê mức độ quan tâm và số lượng sử dụng các dịch vụ nha khoa",
            chartLabelsService, chartValuesService,
            new String[]{"Mã Dịch Vụ", "Tên Dịch Vụ Nha Khoa", "Danh Mục", "Số Lượng Sử Dụng", "Đơn Giá (VNĐ)", "Tổng Thành Tiền (VNĐ)"},
            new Object[][]{
                {"DV001", "Khám và tư vấn tổng quát", "Khám tổng quát", "25", "100.000", "2.500.000"},
                {"DV002", "Lấy cao răng siêu âm", "Nha khoa tổng quát", "40", "150.000", "6.000.000"},
                {"DV003", "Niềng răng mắc cài kim loại", "Chỉnh nha", "15", "15.000.000", "225.000.000"}
            }
        ), SUB_SERVICE);

        // Dữ liệu mẫu biểu đồ cho Xuất Nhập Kho
        String[] chartLabelsInventory = {"Thuốc Tê", "Mắc Cài Sứ", "Bông Băng", "Găng Tay", "Chỉ Nha Khoa"};
        int[] chartValuesInventory = {80, 45, 60, 90, 70};

        contentContainerPanel.add(createReportWithChartTemplate(
            "Báo Cáo Thống Kê Xuất Nhập Kho Vật Tư & Thuốc",
            "Thống kê mức độ tiêu thụ và tồn kho vật tư y tế định mức",
            chartLabelsInventory, chartValuesInventory,
            new String[]{"Mã Phiếu", "Loại Phiếu", "Tên Vật Tư / Thuốc", "Số Lượng", "Đơn Vị Tính", "Người Thực Hiện", "Thời Gian"},
            new Object[][]{
                {"PN001", "Nhập kho", "Thuốc tê Septanest", "200", "Ống", "Nguyễn Gia Bằng", "15/10/2026 08:30"},
                {"PX001", "Xuất kho", "Mắc cài sứ niềng răng", "10", "Bộ", "Nguyễn Hoàng Khải", "18/10/2026 10:00"}
            }
        ), SUB_IMPORT_EXPORT);

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
        btn.setPreferredSize(new Dimension(190, 36));
        btn.addActionListener(e -> showSubView(cardName));
        return btn;
    }

    // --- HÀM TẠO MẪU GIAO DIỆN BÁO BIỂU TÍCH HỢP VẼ BIỂU ĐỒ CỘT ---
    private JPanel createReportWithChartTemplate(String titleText, String subText, String[] chartLabels, int[] chartValues, String[] cols, Object[][] data) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Header báo cáo
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

        panel.add(headerPanel, BorderLayout.NORTH);

        // CONTAINER CHỨA BIỂU ĐỒ VÀ BẢNG
        JPanel centerBody = new JPanel(new BorderLayout(0, 15));
        centerBody.setOpaque(false);

        // 1. PANEL VẼ BIỂU ĐỒ CỘT TRỰC QUAN
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Vẽ nền thẻ Card cho biểu đồ
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, width, height, 16, 16);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);

                if (chartValues == null || chartValues.length == 0) return;

                // Tìm giá trị lớn nhất để tính tỷ lệ chiều cao cột
                int maxVal = 1;
                for (int val : chartValues) {
                    if (val > maxVal) maxVal = val;
                }

                int padding = 45;
                int chartWidth = width - (padding * 2);
                int chartHeight = height - 70;
                int barWidth = chartWidth / chartValues.length - 20;

                // Vẽ tiêu đề nhỏ trong khung biểu đồ
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                g2.setColor(TEXT_DARK);
                g2.drawString("📊 Biểu đồ trực quan thống kê tổng quan", padding, 25);

                // Vẽ các đường kẻ ngang mờ làm mốc
                g2.setColor(new Color(230, 235, 245));
                for (int i = 0; i <= 4; i++) {
                    int y = 45 + (chartHeight * i / 4);
                    g2.drawLine(padding, y, width - padding, y);
                }

                // Vẽ các cột biểu đồ
                for (int i = 0; i < chartValues.length; i++) {
                    int barHeight = (int) ((double) chartValues[i] / maxVal * (chartHeight - 40));
                    int x = padding + 15 + (i * (chartWidth / chartValues.length));
                    int y = 45 + chartHeight - barHeight;

                    // Tô màu cột (Cột cao nhất tô màu xanh lá, các cột khác màu xanh dương)
                    if (chartValues[i] == maxVal) {
                        g2.setColor(GREEN_ACCENT);
                    } else {
                        g2.setColor(BLUE_PRIMARY);
                    }
                    g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

                    // Hiển thị giá trị số trên đầu mỗi cột
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2.setColor(TEXT_DARK);
                    String valStr = String.valueOf(chartValues[i]);
                    int strWidth = g2.getFontMetrics().stringWidth(valStr);
                    g2.drawString(valStr, x + (barWidth - strWidth) / 2, y - 6);

                    // Vẽ nhãn tên danh mục ở trục hoành bên dưới
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.setColor(MUTED);
                    String lbl = chartLabels[i];
                    int lblWidth = g2.getFontMetrics().stringWidth(lbl);
                    g2.drawString(lbl, x + (barWidth - lblWidth) / 2, height - 15);
                }

                g2.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(0, 210));
        chartPanel.setOpaque(false);
        centerBody.add(chartPanel, BorderLayout.NORTH);

        // 2. BẢNG DỮ LIỆU THỐNG KÊ CHI TIẾT BÊN DƯỚI
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        PdfTableExport.install(table, "Báo cáo");
        
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        centerBody.add(scroll, BorderLayout.CENTER);

        panel.add(centerBody, BorderLayout.CENTER);
        return panel;
    }
}