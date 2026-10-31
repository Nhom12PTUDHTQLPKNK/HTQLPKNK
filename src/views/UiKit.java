package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

/**
 * Tiện ích giao diện dùng chung cho các panel của Luân (cùng bảng màu / font với InventoryUpdatePanel cũ).
 * Không dùng ký tự emoji vì font Segoe UI trên một số máy hiển thị thành ô vuông.
 */
public final class UiKit {
    private UiKit() { }

    public static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    public static final Color GREEN_ACCENT = new Color(40, 167, 69);
    public static final Color RED_ACCENT = new Color(220, 53, 69);
    public static final Color PINK_ACCENT = new Color(239, 63, 131);
    public static final Color ORANGE_ACCENT = new Color(230, 126, 34);
    public static final Color BG_CANVAS = new Color(243, 248, 253);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 29, 69);
    public static final Color BORDER_LIGHT = new Color(224, 233, 243);
    public static final Color MUTED = new Color(91, 112, 145);

    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ---------- định dạng / phân tích ----------
    public static Font font(int style, int size) { return new Font("Segoe UI", style, size); }

    public static String fmt(LocalDate d) { return d == null ? "" : d.format(DATE_FMT); }

    /** Trả về null nếu rỗng; ném DateTimeParseException nếu sai định dạng dd/MM/yyyy. */
    public static LocalDate parseDate(String s) throws DateTimeParseException {
        if (s == null || s.trim().isEmpty()) return null;
        return LocalDate.parse(s.trim(), DATE_FMT);
    }

    /** Trả về null nếu rỗng; ném DateTimeParseException nếu sai định dạng HH:mm. */
    public static LocalTime parseTime(String s) throws DateTimeParseException {
        if (s == null || s.trim().isEmpty()) return null;
        return LocalTime.parse(s.trim(), TIME_FMT);
    }

    public static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) r.run(); else SwingUtilities.invokeLater(r);
    }

    // ---------- hộp thoại ----------
    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Xác nhận", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ---------- bố cục ----------
    public static JPanel pageRoot() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(BG_CANVAS);
        p.setBorder(new EmptyBorder(20, 30, 20, 30));
        return p;
    }

    public static JPanel pageHeader(String title, String subtitle, JComponent east) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(font(Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(font(Font.PLAIN, 13));
        lblSub.setForeground(MUTED);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.add(lblTitle);
        box.add(Box.createVerticalStrut(2));
        box.add(lblSub);
        header.add(box, BorderLayout.WEST);
        if (east != null) header.add(east, BorderLayout.EAST);
        return header;
    }

    public static JPanel toolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        return p;
    }

    /** Khung bo góc màu trắng. */
    public static JPanel card() {
        JPanel p = new JPanel() {
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
        p.setOpaque(false);
        return p;
    }

    // ---------- control ----------
    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font(Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public static JTextField textField() {
        JTextField txt = new JTextField();
        txt.setFont(font(Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txt.setPreferredSize(new Dimension(140, 34));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(3, 8, 3, 8)));
        return txt;
    }

    public static void styleCombo(JComboBox<?> cb) {
        cb.setFont(font(Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cb.setPreferredSize(new Dimension(150, 34));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static JButton button(String text, final Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 90));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(font(Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Hàng nút nằm ngang chia đều, dùng trong form bên phải. */
    public static JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel(new GridLayout(1, buttons.length, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JButton b : buttons) row.add(b);
        return row;
    }

    // ---------- bảng ----------
    /** Model chỉ đọc; types cho phép sắp xếp đúng với cột số (Integer.class). */
    public static DefaultTableModel readOnlyModel(final String[] cols, final Class<?>... types) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (types != null && c < types.length && types[c] != null) ? types[c] : Object.class;
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(font(Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 253));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }

    /** Đặt độ rộng ưu tiên cho các cột đầu tiên (các cột còn lại giữ mặc định). */
    public static void setColumnWidths(JTable t, int... widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /** Tô màu chữ theo giá trị ô (ví dụ cột "Trạng thái"). */
    public static class ColorTextRenderer extends DefaultTableCellRenderer {
        private final Map<String, Color> colors;
        public ColorTextRenderer(Map<String, Color> colors) { this.colors = colors; }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            Color col = v == null ? null : colors.get(v.toString());
            if (!sel) comp.setForeground(col != null ? col : TEXT_DARK);
            comp.setFont(font(Font.BOLD, 12));
            return comp;
        }
    }
}
