package views;

import models.dao.ChucVuDAO;
import models.entity.TinhNang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Màn hình danh mục chức vụ và quyền truy cập. */
public class ManHinhChucVu extends JPanel {
    private static final Color BLUE = new Color(11, 95, 204);
    private static final Color BUTTON_BLUE = new Color(11, 75, 160);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color TEXT = new Color(15, 29, 69);
    private static final Color BORDER = new Color(224, 233, 243);

    private final ChucVuDAO dao = new ChucVuDAO();
    private final Runnable afterChange;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Chức vụ", "Mô tả", "Trạng thái", "Số quyền", "Quản lý"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JButton editButton = new FilledButton("Sửa chức vụ & quyền", BUTTON_BLUE);
    private final JButton deleteButton = new FilledButton("Xóa chức vụ", BUTTON_BLUE);
    private final JLabel totalLabel = new JLabel("0 chức vụ");
    private final JLabel permissionLabel = new JLabel("0 quyền được cấu hình");

    public ManHinhChucVu() { this(null); }

    public ManHinhChucVu(Runnable afterChange) {
        this.afterChange = afterChange;
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 30, 25, 30));
        setBackground(BG);

        add(createHeader(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
        load();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 232, 243), getWidth(), 0, new Color(219, 234, 254)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));
        header.setPreferredSize(new Dimension(0, 118));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Quản lý chức vụ & quyền");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Thiết lập chức vụ và quyền truy cập các chức năng trong menu.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(91, 112, 145));
        titles.add(title);
        titles.add(Box.createVerticalStrut(7));
        titles.add(subtitle);
        header.add(titles, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(2, 1, 0, 5));
        stats.setOpaque(false);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLabel.setForeground(BLUE);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        permissionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        permissionLabel.setForeground(new Color(91, 112, 145));
        permissionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        stats.add(totalLabel);
        stats.add(permissionLabel);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(18, 20, 18, 20)));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel section = new JLabel("DANH SÁCH CHỨC VỤ");
        section.setFont(new Font("Segoe UI", Font.BOLD, 13));
        section.setForeground(TEXT);
        toolbar.add(section, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addButton = styledButton("+ Thêm chức vụ", BUTTON_BLUE, Color.WHITE);
        styleButton(editButton, BUTTON_BLUE, Color.WHITE);
        styleButton(deleteButton, BUTTON_BLUE, Color.WHITE);
        JButton reloadButton = styledButton("Tải lại", BUTTON_BLUE, Color.WHITE);
        addButton.addActionListener(e -> edit(null));
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) edit((String) model.getValueAt(table.convertRowIndexToModel(row), 0));
        });
        deleteButton.addActionListener(e -> deleteSelected());
        reloadButton.addActionListener(e -> load());
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(reloadButton);
        toolbar.add(actions, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setForeground(TEXT);
        tableHeader.setBackground(new Color(247, 250, 253));
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(320);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(190);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateActionState(); });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        if (button instanceof FilledButton) ((FilledButton) button).setFillColor(background);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        // Paint the blue fill ourselves so the active Look & Feel cannot replace it with white.
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(true);
        button.setRolloverEnabled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()), new EmptyBorder(8, 12, 8, 12)));
    }

    private JButton styledButton(String text, Color background, Color foreground) {
        JButton button = new FilledButton(text, background);
        styleButton(button, background, foreground);
        return button;
    }

    private static final class FilledButton extends JButton {
        private Color fillColor;

        FilledButton(String text, Color fillColor) {
            super(text);
            this.fillColor = fillColor;
        }

        void setFillColor(Color fillColor) { this.fillColor = fillColor; }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setFont(getFont());
            g2.setColor(Color.WHITE);
            FontMetrics metrics = g2.getFontMetrics();
            String label = getText();
            int x = Math.max(0, (getWidth() - metrics.stringWidth(label)) / 2);
            int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(label, x, y);
            g2.dispose();
        }
    }

    private void load() {
        model.setRowCount(0);
        try {
            int permissions = 0;
            for (Object[] row : dao.getAll()) {
                boolean fixedAdmin = "Admin".equalsIgnoreCase(String.valueOf(row[0]));
                int rolePermissions = fixedAdmin ? TinhNang.values().length : ((Number) row[3]).intValue();
                model.addRow(new Object[]{row[0], row[1], Boolean.TRUE.equals(row[2]) ? "Hoạt động" : "Ngừng hoạt động",
                        rolePermissions, fixedAdmin ? "Cố định · không thể sửa/xóa" : "Có thể quản lý"});
                permissions += rolePermissions;
            }
            totalLabel.setText(model.getRowCount() + " chức vụ");
            permissionLabel.setText(permissions + " quyền được cấu hình");
            updateActionState();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void updateActionState() {
        int row = table.getSelectedRow();
        boolean fixedAdmin = row >= 0 && "Admin".equalsIgnoreCase(
                String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0)));
        editButton.setEnabled(row >= 0 && !fixedAdmin);
        deleteButton.setEnabled(row >= 0 && !fixedAdmin);
    }

    private void edit(String oldName) {
        if (oldName != null && "Admin".equalsIgnoreCase(oldName)) {
            showError(new SQLException("Chức vụ Admin được cố định và không thể chỉnh sửa."));
            return;
        }
        String oldDescription = "";
        if (oldName != null) {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Object value = model.getValueAt(table.convertRowIndexToModel(row), 1);
                oldDescription = value == null ? "" : value.toString();
            }
        }
        JTextField name = new JTextField(oldName == null ? "" : oldName, 25);
        JTextField description = new JTextField(oldDescription, 25);
        Set<String> selected = new HashSet<>();
        try {
            if (oldName != null) selected = dao.getPermissions(oldName);
        } catch (SQLException ex) {
            showError(ex);
            return;
        }

        JPanel fields = new JPanel(new GridLayout(0, 1, 5, 5));
        fields.add(new JLabel("Tên chức vụ"));
        fields.add(name);
        fields.add(new JLabel("Mô tả"));
        fields.add(description);
        JPanel rights = new JPanel(new GridLayout(0, 2, 8, 5));
        Map<String, JCheckBox> boxes = new LinkedHashMap<>();
        for (TinhNang feature : TinhNang.values()) {
            JCheckBox box = new JCheckBox(feature.title, selected.contains(feature.name()));
            box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            box.setBackground(Color.WHITE);
            boxes.put(feature.name(), box);
            rights.add(box);
        }
        JPanel permissionPanel = new JPanel(new BorderLayout(5, 7));
        permissionPanel.setBackground(Color.WHITE);
        permissionPanel.add(new JLabel("Quyền truy cập chức năng"), BorderLayout.NORTH);
        permissionPanel.add(new JScrollPane(rights), BorderLayout.CENTER);
        JPanel form = new JPanel(new BorderLayout(8, 14));
        form.setBackground(Color.WHITE);
        form.add(fields, BorderLayout.NORTH);
        form.add(permissionPanel, BorderLayout.CENTER);
        form.setPreferredSize(new Dimension(500, 470));
        int result = JOptionPane.showConfirmDialog(this, form,
                oldName == null ? "Thêm chức vụ" : "Sửa chức vụ & quyền",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        Set<String> permissions = new HashSet<>();
        for (Map.Entry<String, JCheckBox> entry : boxes.entrySet())
            if (entry.getValue().isSelected()) permissions.add(entry.getKey());
        try {
            dao.save(oldName, name.getText(), description.getText(), permissions);
            load();
            if (afterChange != null) afterChange.run();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String role = String.valueOf(model.getValueAt(table.convertRowIndexToModel(row), 0));
        if ("Admin".equalsIgnoreCase(role)) {
            showError(new SQLException("Chức vụ Admin được cố định và không thể xóa."));
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xóa chức vụ ‘" + role + "’?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(role);
            load();
            if (afterChange != null) afterChange.run();
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
