
package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ElectronicPrescriptionPanel extends JPanel {

    // ==================== COLORS ====================

    private static final Color PRIMARY = new Color(15, 45, 90);
    private static final Color BUTTON_DARK = new Color(0, 0, 110);
    private static final Color BUTTON_DARK_ALT = BUTTON_DARK;
    private static final Color BLUE = new Color(45, 115, 205);
    private static final Color PINK = new Color(205, 65, 125);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color BORDER = new Color(220, 230, 242);
    private static final Color TEXT = new Color(35, 52, 78);
    private static final Color MUTED = new Color(115, 132, 155);
    private static final Color LIGHT_PINK = new Color(255, 241, 247);
    private static final Color LIGHT_BLUE = new Color(240, 247, 255);
    private static final Color WHITE = Color.WHITE;

    // ==================== COMPONENTS ====================

    // Card 1: Thông tin bệnh nhân
    private final JLabel lblMaKH = new JLabel("-");
    private final JLabel lblTenKH = new JLabel("-");
    private final JLabel lblNgaySinh = new JLabel("-");
    private final JLabel lblGioiTinh = new JLabel("-");
    private final JLabel lblSDT = new JLabel("-");

    // Card 2: Thông tin kế hoạch
    private final JLabel lblMaKeHoach = new JLabel("-");
    private final JLabel lblMaBenhAn = new JLabel("-");
    private final JLabel lblDichVu = new JLabel("-");
    private final JLabel lblBacSi = new JLabel("-");

    // Card 3: Đợt điều trị
    private final JComboBox<String> cbDotDieuTri =
            new JComboBox<>();

    private final JTextField txtNgayDuKien = new JTextField();

    private final JLabel lblTrangThai =
            new JLabel("Đang điều trị", SwingConstants.CENTER);

    // Card 4: Tổng quan
    private final JLabel lblTongChiPhi = new JLabel("-");
    private final JLabel lblTongDot = new JLabel("-");
    private final JLabel lblTongDichVu = new JLabel("-");

    // Bảng thuốc
    private final JTextField txtSearchThuoc = new JTextField();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[][]{},
                    new String[]{
                            "STT",
                            "Mã thuốc",
                            "Tên thuốc",
                            "Dạng bào chế",
                            "Liều dùng",
                            "Tần suất",
                            "Thời gian sử dụng",
                            "Số lượng",
                            "Đơn vị",
                            "Cách dùng",
                            "Thao tác"
                    }
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 7 || column == 10;
                }
            };

    private final JTable table = new JTable(tableModel);

    // Hướng dẫn và ghi chú
    private final JTextArea txtHuongDan = new JTextArea();
    private final JTextArea txtGhiChu = new JTextArea();

    // ==================== CONSTRUCTOR ====================

    public ElectronicPrescriptionPanel() {

        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ==================== HEADER ====================

    private JPanel createHeaderPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(
                titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel(
                "💊  Kê đơn thuốc điện tử");

        title.setFont(new Font(
                "Segoe UI", Font.BOLD, 19));
        title.setForeground(PINK);

        JLabel subtitle = new JLabel(
                "Lập đơn thuốc theo kế hoạch điều trị của bệnh nhân");

        subtitle.setFont(new Font(
                "Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(3));
        titlePanel.add(subtitle);

        JButton btnQuayLai = new JButton("← Quay lại");
        styleButton(btnQuayLai, BUTTON_DARK, WHITE);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(btnQuayLai, BorderLayout.EAST);

        return panel;
    }

    // ==================== MAIN CONTENT ====================

    private JPanel createMainContent() {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(
                content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createTopCardsPanel());
        content.add(Box.createVerticalStrut(10));

        content.add(createTableSectionPanel());
        content.add(Box.createVerticalStrut(10));

        content.add(createBottomNotesPanel());

        JScrollPane scrollPane = new JScrollPane(content);

        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    // ==================== FOUR TOP CARDS ====================

    private JPanel createTopCardsPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);

        panel.setPreferredSize(new Dimension(0, 155));
        panel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 155));

        // CARD 1
        RoundedPanel card1 = createCardBase(
                "👤  Thông tin bệnh nhân",
                new Color(255, 247, 250));

        JPanel infoPatient = createInfoPanel();

        addInfoRow(infoPatient, "Mã khách hàng:", lblMaKH);
        addInfoRow(infoPatient, "Họ và tên:", lblTenKH);
        addInfoRow(infoPatient, "Ngày sinh:", lblNgaySinh);
        addInfoRow(infoPatient, "Giới tính:", lblGioiTinh);
        addInfoRow(infoPatient, "Số điện thoại:", lblSDT);

        card1.add(infoPatient, BorderLayout.CENTER);

        // CARD 2
        RoundedPanel card2 = createCardBase(
                "📅  Thông tin kế hoạch điều trị",
                LIGHT_BLUE);

        JPanel infoPlan = createInfoPanel();

        addInfoRow(infoPlan, "Mã kế hoạch:", lblMaKeHoach);
        addInfoRow(infoPlan, "Mã bệnh án:", lblMaBenhAn);
        addInfoRow(infoPlan, "Dịch vụ điều trị:", lblDichVu);
        addInfoRow(infoPlan, "Bác sĩ phụ trách:", lblBacSi);

        card2.add(infoPlan, BorderLayout.CENTER);

        // CARD 3
        RoundedPanel card3 = createCardBase(
                "📚  Đợt điều trị",
                new Color(255, 244, 248));

        JPanel dotPanel = new JPanel(new GridLayout(3, 2, 5, 8));
        dotPanel.setOpaque(false);

        styleComboBox(cbDotDieuTri);

        txtNgayDuKien.setFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        txtNgayDuKien.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(4, 6, 4, 6)
                ));

        lblTrangThai.setOpaque(true);
        lblTrangThai.setBackground(new Color(225, 240, 255));
        lblTrangThai.setForeground(BLUE);
        lblTrangThai.setFont(
                new Font("Segoe UI", Font.BOLD, 10));
        lblTrangThai.setBorder(
                new EmptyBorder(4, 8, 4, 8));

        dotPanel.add(createSmallLabel("Chọn đợt điều trị:"));
        dotPanel.add(cbDotDieuTri);

        dotPanel.add(createSmallLabel("Ngày dự kiến:"));
        dotPanel.add(txtNgayDuKien);

        dotPanel.add(createSmallLabel("Trạng thái:"));
        dotPanel.add(lblTrangThai);

        card3.add(dotPanel, BorderLayout.CENTER);

        // CARD 4
        RoundedPanel card4 = createCardBase(
                "🦷  Tổng quan kế hoạch",
                new Color(255, 247, 250));

        JPanel metricPanel = new JPanel(
                new GridLayout(1, 3, 5, 0));

        metricPanel.setOpaque(false);

        metricPanel.add(createMetricBox(
                "Tổng chi phí", lblTongChiPhi, PINK));

        metricPanel.add(createMetricBox(
                "Tổng đợt", lblTongDot, PRIMARY));

        metricPanel.add(createMetricBox(
                "Tổng dịch vụ", lblTongDichVu, PRIMARY));

        card4.add(metricPanel, BorderLayout.CENTER);

        panel.add(card1);
        panel.add(card2);
        panel.add(card3);
        panel.add(card4);

        return panel;
    }

    // ==================== INFO PANEL ====================

    private JPanel createInfoPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.setOpaque(false);

        return panel;
    }

    private void addInfoRow(
            JPanel panel,
            String key,
            JLabel value) {

        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);

        JLabel lblKey = new JLabel(key);

        lblKey.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        lblKey.setForeground(MUTED);

        value.setFont(new Font(
                "Segoe UI", Font.BOLD, 10));

        value.setForeground(TEXT);

        row.add(lblKey, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);

        panel.add(row);
    }

    // ==================== METRIC BOX ====================

    private JPanel createMetricBox(
            String title,
            JLabel value,
            Color color) {

        JPanel panel = new JPanel();

        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(
                panel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);

        lblTitle.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        lblTitle.setForeground(MUTED);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        value.setFont(new Font(
                "Segoe UI", Font.BOLD, 14));

        value.setForeground(color);
        value.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(18));
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(7));
        panel.add(value);

        return panel;
    }

    // ==================== TABLE SECTION ====================

    private JPanel createTableSectionPanel() {

        RoundedPanel card = new RoundedPanel(12, WHITE);

        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        // TOP BAR
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel(
                "💊  Danh sách thuốc kê đơn");

        title.setFont(new Font(
                "Segoe UI", Font.BOLD, 14));

        title.setForeground(TEXT);

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        searchPanel.setOpaque(false);

        txtSearchThuoc.setPreferredSize(
                new Dimension(230, 29));

        txtSearchThuoc.setFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        txtSearchThuoc.setToolTipText(
                "Tìm thuốc theo mã hoặc tên thuốc");

        txtSearchThuoc.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(4, 8, 4, 8)
                ));

        JButton btnThemThuoc = new JButton("+ Thêm thuốc");

        styleButton(btnThemThuoc, BUTTON_DARK, WHITE);

        searchPanel.add(txtSearchThuoc);
        searchPanel.add(btnThemThuoc);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);

        // TABLE
        configureTable();

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER));

        scrollPane.getViewport().setBackground(WHITE);

        card.add(topBar, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // ==================== CONFIGURE TABLE ====================

    private void configureTable() {

        table.setRowHeight(34);

        table.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        table.setForeground(TEXT);

        table.setBackground(WHITE);

        table.setGridColor(BORDER);

        table.setShowGrid(true);

        table.setSelectionBackground(
                new Color(235, 243, 255));

        table.setSelectionForeground(TEXT);

        table.setFillsViewportHeight(true);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();

        header.setFont(new Font(
                "Segoe UI", Font.BOLD, 10));

        header.setBackground(PRIMARY);
        header.setForeground(WHITE);

        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        int[] widths = {
                40, 75, 145, 95, 105, 95,
                110, 75, 65, 110, 100
        };

        for (int i = 0; i < widths.length; i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Căn giữa các cột STT, mã thuốc, số lượng, đơn vị
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER);

        int[] centerColumns = {0, 1, 7, 8};

        for (int col : centerColumns) {

            table.getColumnModel()
                    .getColumn(col)
                    .setCellRenderer(centerRenderer);
        }

        // Spinner số lượng
        table.getColumnModel()
                .getColumn(7)
                .setCellRenderer(new SpinnerTableCellRenderer());

        table.getColumnModel()
                .getColumn(7)
                .setCellEditor(new SpinnerTableCellEditor());

        // Nút sửa và xóa
        table.getColumnModel()
                .getColumn(10)
                .setCellRenderer(new ActionButtonRenderer());

        table.getColumnModel()
                .getColumn(10)
                .setCellEditor(new ActionButtonEditor());
    }

    // ==================== SPINNER RENDERER ====================

    private static class SpinnerTableCellRenderer
            extends JSpinner implements TableCellRenderer {

        public SpinnerTableCellRenderer() {

            setModel(new SpinnerNumberModel(1, 1, 1000, 1));

            setFont(new Font(
                    "Segoe UI", Font.PLAIN, 10));

            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            if (value instanceof Number) {
                setValue(value);
            }

            setBackground(isSelected
                    ? table.getSelectionBackground()
                    : WHITE);

            return this;
        }
    }

    // ==================== SPINNER EDITOR ====================

    private static class SpinnerTableCellEditor
            extends AbstractCellEditor
            implements TableCellEditor {

        private final JSpinner spinner =
                new JSpinner(
                        new SpinnerNumberModel(1, 1, 1000, 1));

        public SpinnerTableCellEditor() {

            spinner.setFont(new Font(
                    "Segoe UI", Font.PLAIN, 10));
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            if (value instanceof Number) {
                spinner.setValue(value);
            }

            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }

    // ==================== ACTION BUTTON RENDERER ====================

    private static class ActionButtonRenderer
            extends JPanel implements TableCellRenderer {

        private final JButton btnEdit =
                new JButton("✎");

        private final JButton btnDelete =
                new JButton("▣");

        public ActionButtonRenderer() {

            setLayout(new FlowLayout(
                    FlowLayout.CENTER, 5, 3));

            setOpaque(true);

            styleActionButton(btnEdit,
                    BUTTON_DARK, WHITE);

            styleActionButton(btnDelete,
                    BUTTON_DARK_ALT, WHITE);

            add(btnEdit);
            add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            setBackground(isSelected
                    ? table.getSelectionBackground()
                    : WHITE);

            return this;
        }
    }

    // ==================== ACTION BUTTON EDITOR ====================

    private class ActionButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 5, 3));

        private final JButton btnEdit = new JButton("✎");
        private final JButton btnDelete = new JButton("▣");

        private int selectedRow;

        public ActionButtonEditor() {

            styleActionButton(btnEdit,
                    BUTTON_DARK, WHITE);

            styleActionButton(btnDelete,
                    BUTTON_DARK_ALT, WHITE);

            panel.setOpaque(true);

            panel.add(btnEdit);
            panel.add(btnDelete);

            btnEdit.addActionListener(e -> {

                int row = table.convertRowIndexToModel(selectedRow);

                fireEditingStopped();

                JOptionPane.showMessageDialog(
                        ElectronicPrescriptionPanel.this,
                        "Chức năng sửa thuốc tại dòng " + (row + 1),
                        "Sửa thuốc",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });

            btnDelete.addActionListener(e -> {

                int row = table.convertRowIndexToModel(selectedRow);

                fireEditingStopped();

                int confirm = JOptionPane.showConfirmDialog(
                        ElectronicPrescriptionPanel.this,
                        "Bạn có muốn xóa dòng thuốc này không?",
                        "Xác nhận xóa",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(row);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {

            selectedRow = row;

            panel.setBackground(
                    table.getSelectionBackground());

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // ==================== BOTTOM NOTES ====================

    private JPanel createBottomNotesPanel() {

        JPanel panel = new JPanel(
                new GridLayout(1, 2, 10, 0));

        panel.setOpaque(false);

        // HƯỚNG DẪN
        RoundedPanel cardHuongDan = createCardBase(
                "📝  Hướng dẫn sử dụng",
                LIGHT_PINK);

        txtHuongDan.setFont(new Font(
                "Segoe UI", Font.PLAIN, 11));

        txtHuongDan.setLineWrap(true);
        txtHuongDan.setWrapStyleWord(true);

        txtHuongDan.setForeground(TEXT);
        txtHuongDan.setBackground(LIGHT_PINK);

        txtHuongDan.setBorder(
                new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrollHuongDan =
                new JScrollPane(txtHuongDan);

        scrollHuongDan.setBorder(
                BorderFactory.createLineBorder(
                        new Color(250, 220, 232)));

        scrollHuongDan.getViewport()
                .setBackground(LIGHT_PINK);

        cardHuongDan.add(
                scrollHuongDan, BorderLayout.CENTER);

        // GHI CHÚ
        RoundedPanel cardGhiChu = createCardBase(
                "📝  Ghi chú",
                WHITE);

        txtGhiChu.setFont(new Font(
                "Segoe UI", Font.PLAIN, 11));

        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        txtGhiChu.setForeground(TEXT);
        txtGhiChu.setBackground(WHITE);

        txtGhiChu.setBorder(
                new EmptyBorder(6, 8, 6, 8));

        JScrollPane scrollGhiChu =
                new JScrollPane(txtGhiChu);

        scrollGhiChu.setBorder(
                BorderFactory.createLineBorder(BORDER));

        cardGhiChu.add(
                scrollGhiChu, BorderLayout.CENTER);

        panel.add(cardHuongDan);
        panel.add(cardGhiChu);

        panel.setPreferredSize(new Dimension(0, 115));
        panel.setMaximumSize(new Dimension(
                Integer.MAX_VALUE, 115));

        return panel;
    }

    // ==================== FOOTER ====================

    private JPanel createFooterPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0));

        panel.setOpaque(false);

        JButton btnIn = new JButton("▣  In đơn thuốc");
        JButton btnLamMoi = new JButton("↻  Làm mới");
        JButton btnLuu = new JButton("▣  Lưu đơn thuốc");

        styleButton(btnIn, BUTTON_DARK, WHITE);
        styleButton(btnLamMoi, BUTTON_DARK_ALT, WHITE);
        styleButton(btnLuu, BUTTON_DARK, WHITE);

        btnLamMoi.addActionListener(e -> {

            txtSearchThuoc.setText("");
            txtHuongDan.setText("");
            txtGhiChu.setText("");

            cbDotDieuTri.setSelectedIndex(-1);
            txtNgayDuKien.setText("");

            tableModel.setRowCount(0);

            JOptionPane.showMessageDialog(
                    this,
                    "Đã làm mới giao diện.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        btnIn.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Chức năng in đơn thuốc sẽ được bổ sung sau.",
                    "In đơn thuốc",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        btnLuu.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Giao diện hiện tại chưa kết nối chức năng lưu dữ liệu.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        panel.add(btnIn);
        panel.add(btnLamMoi);
        panel.add(btnLuu);

        return panel;
    }

    // ==================== CARD BASE ====================

    private RoundedPanel createCardBase(
            String titleText,
            Color background) {

        RoundedPanel card = new RoundedPanel(
                12, background);

        card.setLayout(new BorderLayout(0, 9));

        card.setBorder(
                new EmptyBorder(10, 11, 10, 11));

        JLabel title = new JLabel(titleText);

        title.setFont(new Font(
                "Segoe UI", Font.BOLD, 12));

        title.setForeground(PINK);

        card.add(title, BorderLayout.NORTH);

        return card;
    }

    // ==================== SMALL LABEL ====================

    private JLabel createSmallLabel(String text) {

        JLabel label = new JLabel(text);

        label.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        label.setForeground(MUTED);

        return label;
    }

    // ==================== COMBO STYLE ====================

    private void styleComboBox(JComboBox<String> combo) {

        combo.setFont(new Font(
                "Segoe UI", Font.PLAIN, 10));

        combo.setBackground(WHITE);

        combo.setForeground(TEXT);

        combo.setBorder(
                BorderFactory.createLineBorder(BORDER));
    }

    // ==================== BUTTON STYLE ====================

    private void styleButton(
            JButton button,
            Color background,
            Color foreground) {

        button.setFont(new Font(
                "Segoe UI", Font.BOLD, 10));

        button.setBackground(BUTTON_DARK);
        button.setForeground(BUTTON_DARK);

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(6, 11, 6, 11)
                ));
    }

    private static void styleActionButton(
            JButton button,
            Color background,
            Color foreground) {

        button.setPreferredSize(new Dimension(29, 25));

        button.setFont(new Font(
                "Segoe UI", Font.BOLD, 11));

        button.setBackground(BUTTON_DARK);
        button.setForeground(BUTTON_DARK);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));
    }

    // ==================== ROUNDED PANEL ====================

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color background;

        public RoundedPanel(
                int radius,
                Color background) {

            this.radius = radius;
            this.background = background;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(background);

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius, radius);

            g2.setColor(BORDER);

            g2.drawRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius, radius);

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
