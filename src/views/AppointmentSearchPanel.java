package views;
//Tra cứu lịch hẹn
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class AppointmentSearchPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public AppointmentSearchPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER & THANH TÌM KIẾM
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tra Cứu Lịch Hẹn Khám");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Nhập từ khóa (Mã lịch, Tên khách hàng hoặc SĐT) để tra cứu lịch hẹn nhanh chóng");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Ô TÌM KIẾM BÊN PHẢI
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("Tra Cứu", BLUE_PRIMARY, 90);
        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // KHU VỰC HIỂN THỊ BẢNG KẾT QUẢ
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {"Mã Lịch Hẹn", "Mã KH", "Tên Khách Hàng", "Số Điện Thoại", "Dịch Vụ Khám", "Nha Sĩ Phụ Trách", "Ngày Hẹn", "Giờ Hẹn", "Trạng Thái"};
        Object[][] data = {
            {"LH001", "KH001", "Nguyễn Gia Băng", "0901122334", "Khám tổng quát", "BS. Nguyễn Minh Sỹ Luân", "21/10/2026", "09:00", "Đã xác nhận"},
            {"LH002", "KH002", "Nguyễn Hoàng Khải", "0988776655", "Lấy cao răng", "BS. Phan Lê Ngọc Ánh", "21/10/2026", "10:30", "Chờ khám"},
            {"LH003", "KH003", "Nguyễn Ngọc Minh Châu", "0912345678", "Niềng răng", "BS. Nguyễn Hoàng Khải", "22/10/2026", "14:00", "Đã xác nhận"}
        };

        tableModel = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        
        // Tích hợp sẵn tính năng xuất PDF sẵn có trong project của nhóm
        PdfTableExport.install(table, "Danh sách tra cứu lịch hẹn");
        
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        centerContainer.add(scroll, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            // Lọc không phân biệt hoa thường theo từ khóa nhập vào
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
        }
    }

    private JButton createStaticButton(String text, Color bg, int width) {
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
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_DARK);
        t.setGridColor(new Color(240, 244, 248));
        t.setSelectionBackground(new Color(225, 240, 255));
        t.setSelectionForeground(TEXT_DARK);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(245, 248, 253));
        t.getTableHeader().setForeground(TEXT_DARK);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }
}