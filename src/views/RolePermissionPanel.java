package views;

import Audit.AuditLog;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class RolePermissionPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color GREEN_ACCENT = new Color(40, 167, 69);
    private static final Color RED_ACCENT = new Color(220, 53, 69);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    public RolePermissionPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // Tiêu đề phân hệ
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Hệ Thống: Phân Quyền & Tài Khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý danh mục nhóm quyền truy cập và danh sách tài khoản nhân viên hệ thống");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Tạo JTabbedPane chia ra 2 Tab: Phân Quyền & Tài Khoản
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("  Phân Quyền Truy Cập  ", createRolePanel());
        tabbedPane.addTab("  Quản Lý Tài Khoản  ", createAccountPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ================= TAB 1: PHÂN QUYỀN =================
    private JPanel createRolePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] columns = {"Mã Quyền", "Tên Vai Trò / Nhóm Quyền", "Mức Độ Truy Cập", "Trạng Thái"};
        DefaultTableModel roleModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(roleModel);
        PdfTableExport.install(table, "Danh sách phân quyền");
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(roleModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Form nhập liệu bên phải cho Phân quyền
        JPanel formPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(330, 0));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblFormTitle = new JLabel("Thông Tin Nhóm Quyền");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblFormTitle);
        formPanel.add(Box.createVerticalStrut(14));

        JTextField txtRoleId = addFieldToForm(formPanel, "Mã quyền:");
        JTextField txtRoleName = addFieldToForm(formPanel, "Tên vai trò / Nhóm:");
        
        formPanel.add(createFieldLabel("Mức độ truy cập:"));
        formPanel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbLevel = new JComboBox<>(new String[]{"Toàn quyền (Full Access)", "Khám bệnh & Điều trị", "Lễ tân & Đặt lịch", "Kho & Vật tư"});
        cbLevel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLevel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbLevel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbLevel.setBackground(Color.WHITE);
        formPanel.add(cbLevel);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(createFieldLabel("Trạng thái:"));
        formPanel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang áp dụng", "Tạm khóa"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setBackground(Color.WHITE);
        formPanel.add(cbStatus);
        formPanel.add(Box.createVerticalStrut(20));

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> {
            String id = txtRoleId.getText().trim();
            String name = txtRoleName.getText().trim();
            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vui lòng nhập đầy đủ thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            roleModel.addRow(new Object[]{id, name, cbLevel.getSelectedItem(), cbStatus.getSelectedItem()});
            AuditLog.event("CREATE", "Phân quyền - mã " + id);
            txtRoleId.setText(""); txtRoleName.setText("");
            JOptionPane.showMessageDialog(panel, "Thêm nhóm quyền thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        btnClear.addActionListener(e -> {
            txtRoleId.setText("");
            txtRoleName.setText("");
            table.clearSelection();
        });

        btnBox.add(btnSave);
        btnBox.add(btnClear);
        formPanel.add(btnBox);

        panel.add(formPanel, BorderLayout.EAST);
        return panel;
    }

    // ================= TAB 2: QUẢN LÝ TÀI KHOẢN =================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] columns = {"Mã TK", "Tên Đăng Nhập", "Họ Tên Nhân Viên", "Nhóm Quyền", "Trạng Thái"};
        DefaultTableModel accModel = new DefaultTableModel(null, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(accModel);
        PdfTableExport.install(table, "Danh sách tài khoản hệ thống");
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(accModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Form nhập liệu bên phải cho Tài khoản
        JPanel formPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(330, 0));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblFormTitle = new JLabel("Thông Tin Tài Khoản");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblFormTitle.setForeground(TEXT_DARK);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblFormTitle);
        formPanel.add(Box.createVerticalStrut(14));

        JTextField txtAccId = addFieldToForm(formPanel, "Mã tài khoản:");
        JTextField txtUsername = addFieldToForm(formPanel, "Tên đăng nhập:");
        JTextField txtFullName = addFieldToForm(formPanel, "Họ tên nhân viên:");
        
        formPanel.add(createFieldLabel("Nhóm quyền:"));
        formPanel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Quản trị hệ thống", "Nha sĩ", "Lễ tân", "Y tá"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRole.setBackground(Color.WHITE);
        formPanel.add(cbRole);
        formPanel.add(Box.createVerticalStrut(12));

        formPanel.add(createFieldLabel("Trạng thái:"));
        formPanel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Hoạt động", "Khóa"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setBackground(Color.WHITE);
        formPanel.add(cbStatus);
        formPanel.add(Box.createVerticalStrut(20));

        JPanel btnBox = new JPanel(new GridLayout(1, 2, 10, 0));
        btnBox.setOpaque(false);
        btnBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(e -> {
            String id = txtAccId.getText().trim();
            String user = txtUsername.getText().trim();
            String name = txtFullName.getText().trim();
            if (id.isEmpty() || user.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vui lòng nhập đầy đủ thông tin tài khoản!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            accModel.addRow(new Object[]{id, user, name, cbRole.getSelectedItem(), cbStatus.getSelectedItem()});
            AuditLog.event("CREATE", "Tài khoản - mã " + id);
            txtAccId.setText(""); txtUsername.setText(""); txtFullName.setText("");
            JOptionPane.showMessageDialog(panel, "Thêm tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        btnClear.addActionListener(e -> {
            txtAccId.setText("");
            txtUsername.setText("");
            txtFullName.setText("");
            table.clearSelection();
        });

        btnBox.add(btnSave);
        btnBox.add(btnClear);
        formPanel.add(btnBox);

        panel.add(formPanel, BorderLayout.EAST);
        return panel;
    }

    // Các hàm tiện ích giao diện
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToForm(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
    }

    private JButton createStaticButton(String text, Color bg, int width) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
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