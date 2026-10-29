package views;

import Audit.AuditLog;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesProcessPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private CardLayout subCardLayout;
    private JPanel contentContainerPanel;

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch, txtInvoiceId, txtCustomerName, txtServiceFee, txtMedFee, txtTotal;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean isEditing = false;
    private int selectedRowIndex = -1;

    public static final String SUB_CREATE = "SUB_CREATE";
    public static final String SUB_LIST = "SUB_LIST";

    public SalesProcessPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. THANH ĐIỀU HƯỚNG TAB Ở TRÊN CÙNG
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        navBar.setOpaque(false);

        navBar.add(createNavButton("1. Lập Hóa Đơn Mới", SUB_CREATE));
        navBar.add(createNavButton("2. Danh Sách Hóa Đơn Đã Lưu", SUB_LIST));

        add(navBar, BorderLayout.NORTH);

        // Khởi tạo bảng dữ liệu dùng chung
        String[] cols = {"Mã HĐ", "Khách Hàng", "Tiền Dịch Vụ", "Tiền Thuốc", "Tổng Tiền (VNĐ)", "Thời Gian Lưu", "Trạng Thái"};
        Object[][] data = {
            {"HD001", "Nguyễn Gia Bằng", "250000", "150000", "400000", "20/10/2026 14:15", "Đã thanh toán"},
            {"HD002", "Nguyễn Hoàng Khải", "500000", "300000", "800000", "20/10/2026 15:30", "Đã thanh toán"},
            {"HD003", "Minh Châu", "1200000", "0", "1200000", "21/10/2026 09:10", "Đã thanh toán"}
        };

        tableModel = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Hóa đơn bán hàng");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        // 2. VÙNG NỘI DUNG CHÍNH (CARDLAYOUT - CÁC TRANG CHỒNG NHAU)
        subCardLayout = new CardLayout();
        contentContainerPanel = new JPanel(subCardLayout);
        contentContainerPanel.setOpaque(false);

        contentContainerPanel.add(createCreateSubPanel(), SUB_CREATE);
        contentContainerPanel.add(createListSubPanel(), SUB_LIST);

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
        btn.setPreferredSize(new Dimension(200, 36));
        btn.addActionListener(e -> showSubView(cardName));
        return btn;
    }

    // --- TRANG 1: LẬP HÓA ĐƠN MỚI ---
    private JPanel createCreateSubPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Lập Hóa Đơn Mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Nhập thông tin dịch vụ, tiền thuốc và lưu hóa đơn trực tiếp vào hệ thống");
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

        // Form Card ở giữa
        JPanel formCard = new JPanel() {
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
        formCard.setOpaque(false);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel formTitle = new JLabel("Thông Tin Hóa Đơn Thanh Toán");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(TEXT_DARK);
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(formTitle);
        formCard.add(Box.createVerticalStrut(20));

        txtInvoiceId = addField(formCard, "Mã Hóa Đơn:");
        txtCustomerName = addField(formCard, "Tên Khách Hàng:");
        txtServiceFee = addField(formCard, "Tiền Dịch Vụ (VNĐ):");
        txtMedFee = addField(formCard, "Tiền Thuốc / Vật Tư (VNĐ):");
        txtTotal = addField(formCard, "Tổng Thành Tiền:");
        txtTotal.setEditable(false);

        KeyAdapter calcListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    double s = Double.parseDouble(txtServiceFee.getText().trim().isEmpty() ? "0" : txtServiceFee.getText().trim());
                    double m = Double.parseDouble(txtMedFee.getText().trim().isEmpty() ? "0" : txtMedFee.getText().trim());
                    txtTotal.setText(String.format("%.0f", s + m));
                } catch (NumberFormatException ignored) {
                    txtTotal.setText("0");
                }
            }
        };
        txtServiceFee.addKeyListener(calcListener);
        txtMedFee.addKeyListener(calcListener);

        formCard.add(Box.createVerticalStrut(10));

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("💾 Lưu Hóa Đơn", GREEN_ACCENT, 150);
        JButton btnClear = createStaticButton("🔄 Làm Mới", PINK_ACCENT, 130);

        btnSave.addActionListener(e -> performSave());
        btnClear.addActionListener(e -> clearForm());

        btnBox.add(btnSave);
        btnBox.add(btnClear);
        formCard.add(btnBox);

        panel.add(formCard, BorderLayout.CENTER);
        return panel;
    }

    // --- TRANG 2: DANH SÁCH HÓA ĐƠN ĐÃ LƯU ---
    private JPanel createListSubPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh Sách Hóa Đơn Đã Lưu & Thanh Toán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Tra cứu và quản lý lịch sử các hóa đơn đã được lưu trữ thành công");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Thanh tìm kiếm
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        txtSearch = new JTextField(14);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JTextField addField(JPanel p, String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setMaximumSize(new Dimension(500, 34));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        p.add(t);
        p.add(Box.createVerticalStrut(12));
        return t;
    }

    private void performSave() {
        String id = txtInvoiceId.getText().trim();
        String name = txtCustomerName.getText().trim();
        String sFee = txtServiceFee.getText().trim();
        String mFee = txtMedFee.getText().trim();
        String total = txtTotal.getText().trim();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã hóa đơn và Tên khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isEditing) {
            tableModel.addRow(new Object[]{id, name, sFee.isEmpty() ? "0" : sFee, mFee.isEmpty() ? "0" : mFee, total.isEmpty() ? "0" : total, currentTime, "Đã thanh toán"});
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn thành công! Đã chuyển sang danh sách hóa đơn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt(name, selectedRowIndex, 1);
            tableModel.setValueAt(sFee, selectedRowIndex, 2);
            tableModel.setValueAt(mFee, selectedRowIndex, 3);
            tableModel.setValueAt(total, selectedRowIndex, 4);
            tableModel.setValueAt(currentTime, selectedRowIndex, 5);
            JOptionPane.showMessageDialog(this, "Cập nhật hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        AuditLog.event(isEditing ? "UPDATE" : "CREATE", "Hóa đơn bán hàng - mã " + id);
        clearForm();
        // Tự động chuyển sang trang danh sách để xem kết quả lưu
        showSubView(SUB_LIST);
    }

    private void clearForm() {
        isEditing = false;
        selectedRowIndex = -1;
        txtInvoiceId.setText("");
        txtInvoiceId.setEnabled(true);
        txtCustomerName.setText("");
        txtServiceFee.setText("");
        txtMedFee.setText("");
        txtTotal.setText("");
        table.clearSelection();
    }

    private void performSearch() {
        String kw = txtSearch.getText().trim();
        rowSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
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
