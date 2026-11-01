package views;

import models.dao.PersonnelAccountDAO;
import security.AccessControl;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RolePermissionPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(34, 150, 83);
    private static final Color RED_ACCENT = new Color(217, 45, 32);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private final PersonnelAccountDAO personnelAccountDAO = new PersonnelAccountDAO();

    // Thẻ thống kê
    private JLabel lblTotalAccounts, lblActiveAccounts, lblLockedAccounts;

    // Bảng Tài Khoản & Phân Quyền
    private DefaultTableModel accountTableModel;
    private JTable accountTable;
    private JTextField txtAccountSearch;
    private TableRowSorter<DefaultTableModel> accountRowSorter;

    public RolePermissionPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // 1. HEADER CHUNG
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Tài Khoản & Phân Quyền");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý tên đăng nhập, cấp quyền hệ thống và trạng thái hoạt động của nhân sự");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Nút thêm tài khoản mới góc phải Header
        JButton btnAddAccount = createPrimaryButton("+ Thêm tài khoản mới", BLUE_PRIMARY);
        btnAddAccount.addActionListener(e -> showAccountDialog(null, null, null, null, null, null, true));
        
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        headerRight.setOpaque(false);
        headerRight.add(btnAddAccount);
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CONTAINER CHÍNH
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

        // 3. THẺ THỐNG KÊ (3 STATS CARDS)
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblTotalAccounts = new JLabel("0 tài khoản");
        lblActiveAccounts = new JLabel("0 tài khoản");
        lblLockedAccounts = new JLabel("0 tài khoản");

        statsRow.add(createStatCard("TỔNG TÀI KHOẢN HỆ THỐNG", lblTotalAccounts, BLUE_PRIMARY, new Color(232, 242, 255)));
        statsRow.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActiveAccounts, GREEN_ACCENT, new Color(238, 247, 238)));
        statsRow.add(createStatCard("ĐÃ KHÓA", lblLockedAccounts, RED_ACCENT, new Color(254, 242, 242)));

        mainContent.add(statsRow);
        mainContent.add(Box.createVerticalStrut(20));

        // 4. THANH TOOLBAR & BẢNG DỮ LIỆU
        JPanel contentCard = createCardContainer();
        contentCard.setLayout(new BorderLayout(0, 15));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        txtAccountSearch = new JTextField(20);
        txtAccountSearch.setPreferredSize(new Dimension(280, 40));
        txtAccountSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAccountSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(5, 12, 5, 12)
        ));
        txtAccountSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên đăng nhập, họ tên...");

        JButton btnSearch = createStaticButton("Tìm kiếm", BLUE_PRIMARY, 90, 40);
        btnSearch.addActionListener(e -> {
            String kw = txtAccountSearch.getText().trim();
            if (kw.isEmpty() || kw.equals("Tìm theo tên đăng nhập, họ tên...")) {
                accountRowSorter.setRowFilter(null);
            } else {
                accountRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
            }
        });

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setOpaque(false);
        searchGroup.add(txtAccountSearch);
        searchGroup.add(btnSearch);
        toolbar.add(searchGroup, BorderLayout.WEST);

        contentCard.add(toolbar, BorderLayout.NORTH);

        // Bảng dữ liệu chuẩn theo giao diện mẫu
        String[] cols = {"Mã NV", "Tên Đăng Nhập", "Họ Tên Nhân Sự", "Vai Trò / Phân Quyền", "Trạng Thái", "Thao Tác"};
        accountTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        accountTable = new JTable(accountTableModel);
        PdfTableExport.install(accountTable, "Danh sách tài khoản hệ thống");
        accountRowSorter = new TableRowSorter<>(accountTableModel);
        accountTable.setRowSorter(accountRowSorter);
        styleTable(accountTable);

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        accountTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        accountTable.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        accountTable.getColumnModel().getColumn(5).setCellEditor(new ActionButtonEditor(new JCheckBox(), this));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        accountTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(accountTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        contentCard.add(scroll, BorderLayout.CENTER);

        mainContent.add(contentCard);
        add(mainContent, BorderLayout.CENTER);

        loadAccountsFromDB();
    }

    public void loadAccountsFromDB() {
        accountTableModel.setRowCount(0);
        try {
            List<Object[]> data = personnelAccountDAO.getAll();
            int total = data.size();
            int activeCount = 0;
            int lockedCount = 0;

            for (Object[] row : data) {
                String maNV = (String) row[0];
                String tenNV = (String) row[1];
                String tenDN = (String) row[2];
                String vaiTro = (String) row[3];
                String trangThai = (String) row[6];

                if ("Hoạt động".equals(trangThai)) activeCount++;
                else if ("Tạm khóa".equals(trangThai)) lockedCount++;

                if (tenDN == null) tenDN = "(Chưa cấp)";

                Object[] tableRow = {maNV, tenDN, tenNV != null ? tenNV : "", vaiTro, trangThai, "ThaoTac"};
                accountTableModel.addRow(tableRow);
            }

            lblTotalAccounts.setText(total + " tài khoản");
            lblActiveAccounts.setText(activeCount + " tài khoản");
            lblLockedAccounts.setText(lockedCount + " tài khoản");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách tài khoản: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // DIALOG THÊM / SỬA TÀI KHOẢN
    // =========================================================================
    private void showAccountDialog(String staffId, String staffName, String username, String role, String status, String salaryStr, boolean isNew) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                isNew ? "Thêm Nhân Sự & Tài Khoản Mới" : "Chỉnh Sửa Thông Tin Nhân Sự & Tài Khoản", true);
        dialog.setSize(460, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        panel.setBackground(Color.WHITE);

        JTextField txtId = addFieldToDialog(panel, "Mã nhân viên (VD: NV001):");
        JTextField txtName = addFieldToDialog(panel, "Họ tên nhân sự:");
        JTextField txtUser = addFieldToDialog(panel, "Tên đăng nhập:");
        JPasswordField txtPass = addFieldPasswordToDialog(panel, isNew ? "Mật khẩu:" : "Mật khẩu mới (để trống nếu không đổi):");

        panel.add(createFieldLabel("Vai trò / Phân quyền:"));
        panel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Admin", "Nha sĩ", "Lễ tân", "Y tá"});
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRole.setBackground(Color.WHITE);
        panel.add(cbRole);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createFieldLabel("Trạng thái tài khoản:"));
        panel.add(Box.createVerticalStrut(4));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setBackground(Color.WHITE);
        panel.add(cbStatus);
        panel.add(Box.createVerticalStrut(20));

        if (!isNew) {
            if (staffId != null) { txtId.setText(staffId); txtId.setEnabled(false); }
            if (staffName != null) txtName.setText(staffName);
            if (username != null && !username.equals("(Chưa cấp)")) txtUser.setText(username);
            if (role != null) cbRole.setSelectedItem(role);
            if (status != null) cbStatus.setSelectedItem(status);
        }

        JButton btnSave = createStaticButton("Lưu thông tin", BLUE_PRIMARY, Integer.MAX_VALUE, 40);
        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String r = (String) cbRole.getSelectedItem();
            boolean active = "Hoạt động".equals(cbStatus.getSelectedItem());

            if (id.isEmpty() || name.isEmpty() || u.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ Mã nhân viên, Họ tên và Tên đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean success = personnelAccountDAO.save(
                        id, name, u, p, r, null, 0.0, active, !isNew, username != null && !username.equals("(Chưa cấp)") ? username : u
                );
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Lưu thông tin xuống CSDL thành công!");
                    dialog.dispose();
                    loadAccountsFromDB();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Không thể lưu vào CSDL!\nLỗi SQL: " + ex.getMessage(), "Lỗi Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(btnSave);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // =========================================================================
    // UI HELPERS & COMPONENTS
    // =========================================================================
    private JPanel createStatCard(String title, JLabel valueLbl, Color accentColor, Color bgCard) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCard);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLbl.setForeground(accentColor);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLbl);
        return card;
    }

    private JPanel createCardContainer() {
        JPanel panel = new JPanel() {
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
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToDialog(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(4, 10, 4, 10)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
    }

    private JPasswordField addFieldPasswordToDialog(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JPasswordField txt = new JPasswordField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(4, 10, 4, 10)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
    }

    private JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(170, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createStaticButton(String text, Color bg, int width, int height) {
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
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(245, 248, 253));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
    }

    // =========================================================================
    // CUSTOM RENDERER & EDITOR CHO TRẠNG THÁI VÀ NÚT THAO TÁC
    // =========================================================================
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel((String) value, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setOpaque(true);
            
            String status = value == null ? "" : value.toString();
            if ("Hoạt động".equals(status)) {
                lbl.setForeground(GREEN_ACCENT);
                lbl.setBackground(new Color(225, 245, 230));
            } else if ("Tạm khóa".equals(status)) {
                lbl.setForeground(RED_ACCENT);
                lbl.setBackground(new Color(253, 237, 236));
            } else {
                lbl.setForeground(MUTED);
                lbl.setBackground(new Color(240, 244, 248));
            }
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(lbl);
            return panel;
        }
    }

    private static class ActionButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JButton btnEdit = new JButton("Sửa");
        private final JButton btnLock = new JButton("Khóa");

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            setOpaque(true);
            
            styleActionButton(btnEdit, new Color(240, 244, 248), TEXT_DARK);
            styleActionButton(btnLock, new Color(254, 243, 199), new Color(146, 64, 14));

            add(btnEdit);
            add(btnLock);
        }

        private void styleActionButton(JButton btn, Color bg, Color fg) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    private static class ActionButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        private final JButton btnEdit = new JButton("Sửa");
        private final JButton btnLock = new JButton("Khóa");
        private String currentStaffId;
        private String currentStaffName;
        private String currentUsername;
        private String currentRole;
        private String currentStatus;
        private final RolePermissionPanel outer;

        public ActionButtonEditor(JCheckBox checkBox, RolePermissionPanel outerInstance) {
            super(checkBox);
            this.outer = outerInstance;
            panel.setOpaque(true);

            styleEditButton(btnEdit);
            styleLockButton(btnLock);

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                outer.showAccountDialog(currentStaffId, currentStaffName, currentUsername, currentRole, currentStatus, null, false);
            });

            btnLock.addActionListener(e -> {
                fireEditingStopped();
                int confirm = JOptionPane.showConfirmDialog(panel, 
                        "Bạn có chắc muốn thay đổi trạng thái tài khoản của nhân viên: " + currentStaffName + "?", 
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean targetActive = !"Hoạt động".equals(currentStatus);
                        boolean success = outer.personnelAccountDAO.save(
                                currentStaffId, currentStaffName, currentUsername, null, currentRole, null, 0.0, targetActive, true, currentUsername
                        );
                        if (success) {
                            JOptionPane.showMessageDialog(panel, "Đã cập nhật trạng thái xuống CSDL thành công!");
                            outer.loadAccountsFromDB();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(panel, "Thực hiện thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panel.add(btnEdit);
            panel.add(btnLock);
        }

        private void styleEditButton(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBackground(new Color(240, 244, 248));
            btn.setForeground(TEXT_DARK);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void styleLockButton(JButton btn) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBackground(new Color(254, 243, 199));
            btn.setForeground(new Color(146, 64, 14));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentStaffId = (String) table.getValueAt(row, 0);
            currentUsername = (String) table.getValueAt(row, 1);
            currentStaffName = (String) table.getValueAt(row, 2);
            currentRole = (String) table.getValueAt(row, 3);
            currentStatus = (String) table.getValueAt(row, 4);
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "ThaoTac";
        }
    }
}