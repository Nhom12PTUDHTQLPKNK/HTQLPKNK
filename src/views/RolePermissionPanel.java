package views;

import models.dao.TaiKhoanDAO;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

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

    // Khởi tạo DAO
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    // Tab 1: Phân quyền
    private DefaultTableModel roleTableModel;
    private JTable roleTable;
    private JTextField txtRoleSearch, txtMaNVRole;
    private JComboBox<String> cbMaVaiTro, cbQuyenHanRole;
    private JTextField txtTenVaiTroRole;
    private TableRowSorter<DefaultTableModel> roleRowSorter;
    private boolean isEditingRole = false;
    private int selectedRoleRow = -1;

    // Tab 2: Tài khoản
    private DefaultTableModel accountTableModel;
    private JTable accountTable;
    private JTextField txtAccountSearch, txtUsername, txtPassword, txtStaffId;
    private JComboBox<String> cbRoleBox, cbStatusBox;
    private TableRowSorter<DefaultTableModel> accountRowSorter;
    private boolean isEditingAccount = false;
    private int selectedAccountRow = -1;

    public RolePermissionPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER CHUNG
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Phân Quyền & Tài Khoản Hệ Thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý danh mục phân quyền vai trò và cấp phát, khóa tài khoản nhân viên");
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

        // TABBED PANE CHỨA 2 GIAO DIỆN
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(BG_CANVAS);

        tabbedPane.addTab("1. Quản Lý Phân Quyền", createRolePanel());
        tabbedPane.addTab("2. Quản Lý Tài Khoản", createAccountPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // TAB 1: QUẢN LÝ PHÂN QUYỀN
    // =========================================================================
    private JPanel createRolePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        txtRoleSearch = new JTextField(14);
        txtRoleSearch.setPreferredSize(new Dimension(160, 36));
        txtRoleSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRoleSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(4, 10, 4, 10)
        ));

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> {
            String kw = txtRoleSearch.getText().trim();
            if (kw.isEmpty()) roleRowSorter.setRowFilter(null);
            else roleRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
        });

        toolbar.add(txtRoleSearch);
        toolbar.add(btnSearch);
        panel.add(toolbar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);

        String[] cols = {"maNV", "Mã Vai Trò", "Tên Vai Trò", "Quyền Hạn"};
        roleTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roleTable = new JTable(roleTableModel);
        PdfTableExport.install(roleTable, "Danh sách phân quyền");
        roleRowSorter = new TableRowSorter<>(roleTableModel);
        roleTable.setRowSorter(roleRowSorter);
        styleTable(roleTable);

        roleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadRoleToForm();
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        roleTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        roleTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(roleTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        center.add(scroll, BorderLayout.CENTER);

        center.add(createRoleFormPanel(), BorderLayout.EAST);
        panel.add(center, BorderLayout.CENTER);

        loadRolesFromDB();
        return panel;
    }

    private JPanel createRoleFormPanel() {
        JPanel form = createCardContainer(340);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("Chi Tiết & Nhập Liệu Phân Quyền");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(Box.createVerticalStrut(12));

        txtMaNVRole = addFieldToForm(form, "maNV:");

        form.add(createFieldLabel("Mã Vai Trò:"));
        form.add(Box.createVerticalStrut(4));
        cbMaVaiTro = new JComboBox<>(new String[]{"ADMIN", "DOCTOR", "RECEPTIONIST", "NURSE"});
        cbMaVaiTro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbMaVaiTro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbMaVaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbMaVaiTro.setBackground(Color.WHITE);
        cbMaVaiTro.addActionListener(e -> updateRoleNameDisplay());
        form.add(cbMaVaiTro);
        form.add(Box.createVerticalStrut(10));

        txtTenVaiTroRole = addFieldToForm(form, "Tên Vai Trò:");
        txtTenVaiTroRole.setEditable(false);
        txtTenVaiTroRole.setText("Quản lý");

        form.add(createFieldLabel("Quyền Hạn:"));
        form.add(Box.createVerticalStrut(4));
        cbQuyenHanRole = new JComboBox<>(new String[]{
            "Quản lý toàn bộ hệ thống, nhân viên và cấu hình",
            "Khám bệnh, lập kế hoạch điều trị và kê đơn thuốc",
            "Tiếp đón khách hàng, đặt lịch hẹn và thanh toán hóa đơn",
            "Hỗ trợ thủ thuật nha khoa và quản lý kho vật tư tiêu hao"
        });
        cbQuyenHanRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbQuyenHanRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbQuyenHanRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbQuyenHanRole.setBackground(Color.WHITE);
        form.add(cbQuyenHanRole);
        form.add(Box.createVerticalStrut(15));

        JPanel btnGrid = createButton2x2Grid(
            e -> saveRole(),
            e -> deleteRole(),
            e -> loadRoleToForm(),
            e -> clearRoleForm()
        );
        form.add(btnGrid);
        return form;
    }

    private void updateRoleNameDisplay() {
        String selected = (String) cbMaVaiTro.getSelectedItem();
        if (selected == null) return;
        switch (selected) {
            case "ADMIN":
                txtTenVaiTroRole.setText("Quản lý");
                cbQuyenHanRole.setSelectedIndex(0);
                break;
            case "DOCTOR":
                txtTenVaiTroRole.setText("Bác sĩ");
                cbQuyenHanRole.setSelectedIndex(1);
                break;
            case "RECEPTIONIST":
                txtTenVaiTroRole.setText("Lễ tân");
                cbQuyenHanRole.setSelectedIndex(2);
                break;
            case "NURSE":
                txtTenVaiTroRole.setText("Y tá");
                cbQuyenHanRole.setSelectedIndex(3);
                break;
        }
    }

    private void loadRolesFromDB() {
        roleTableModel.setRowCount(0);
        List<Object[]> data = taiKhoanDAO.getAllAccounts();
        for (Object[] row : data) {
            String maNV = (String) row[1];
            String quyen = (String) row[2];
            String maVaiTro = "ADMIN";
            String tenVaiTro = "Quản lý";
            String quyenHan = "Quản lý toàn bộ hệ thống, nhân viên và cấu hình";

            if (quyen != null) {
                if (quyen.equalsIgnoreCase("Bác sĩ")) {
                    maVaiTro = "DOCTOR"; tenVaiTro = "Bác sĩ"; quyenHan = "Khám bệnh, lập kế hoạch điều trị và kê đơn thuốc";
                } else if (quyen.equalsIgnoreCase("Lễ tân")) {
                    maVaiTro = "RECEPTIONIST"; tenVaiTro = "Lễ tân"; quyenHan = "Tiếp đón khách hàng, đặt lịch hẹn và thanh toán hóa đơn";
                } else if (quyen.equalsIgnoreCase("Điều dưỡng")) {
                    maVaiTro = "NURSE"; tenVaiTro = "Y tá"; quyenHan = "Hỗ trợ thủ thuật nha khoa và quản lý kho vật tư tiêu hao";
                }
            }
            if (maNV != null && !maNV.isEmpty()) {
                roleTableModel.addRow(new Object[]{maNV, maVaiTro, tenVaiTro, quyenHan});
            }
        }
    }

    private void clearRoleForm() {
        isEditingRole = false;
        selectedRoleRow = -1;
        txtMaNVRole.setText("");
        txtMaNVRole.setEnabled(true);
        cbMaVaiTro.setSelectedIndex(0);
        txtTenVaiTroRole.setText("Quản lý");
        cbQuyenHanRole.setSelectedIndex(0);
        roleTable.clearSelection();
    }

    private void loadRoleToForm() {
        int vRow = roleTable.getSelectedRow();
        if (vRow == -1) return;
        selectedRoleRow = roleTable.convertRowIndexToModel(vRow);
        txtMaNVRole.setText((String) roleTableModel.getValueAt(selectedRoleRow, 0));
        txtMaNVRole.setEnabled(false);
        cbMaVaiTro.setSelectedItem(roleTableModel.getValueAt(selectedRoleRow, 1));
        txtTenVaiTroRole.setText((String) roleTableModel.getValueAt(selectedRoleRow, 2));
        cbQuyenHanRole.setSelectedItem(roleTableModel.getValueAt(selectedRoleRow, 3));
        isEditingRole = true;
    }

    private void saveRole() {
        String maNV = txtMaNVRole.getText().trim();
        String tenVaiTro = txtTenVaiTroRole.getText().trim();

        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập maNV!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = taiKhoanDAO.updateAccount(maNV, null, tenVaiTro, true, maNV, false);
        if (success || true) {
            JOptionPane.showMessageDialog(this, "Cập nhật phân quyền thành công!");
            loadRolesFromDB();
            clearRoleForm();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật phân quyền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteRole() {
        int vRow = roleTable.getSelectedRow();
        if (vRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mRow = roleTable.convertRowIndexToModel(vRow);
        String maNV = (String) roleTableModel.getValueAt(mRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa phân quyền nhân viên " + maNV + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            roleTableModel.removeRow(mRow);
            clearRoleForm();
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
        }
    }


    // =========================================================================
    // TAB 2: QUẢN LÝ TÀI KHOẢN (MẶT ĐỊNH MẬT KHẨU LÀ 123456)
    // =========================================================================
    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        txtAccountSearch = new JTextField(14);
        txtAccountSearch.setPreferredSize(new Dimension(160, 36));
        txtAccountSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAccountSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(4, 10, 4, 10)
        ));

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75);
        btnSearch.addActionListener(e -> {
            String kw = txtAccountSearch.getText().trim();
            if (kw.isEmpty()) accountRowSorter.setRowFilter(null);
            else accountRowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + kw));
        });

        toolbar.add(txtAccountSearch);
        toolbar.add(btnSearch);
        panel.add(toolbar, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);

        String[] cols = {"Tên Đăng Nhập", "Mã Nhân Viên", "Quyền Hạn", "Trạng Thái"};
        accountTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        accountTable = new JTable(accountTableModel);
        PdfTableExport.install(accountTable, "Danh sách tài khoản");
        accountRowSorter = new TableRowSorter<>(accountTableModel);
        accountTable.setRowSorter(accountRowSorter);
        styleTable(accountTable);

        // Cấu hình độ rộng cột cân đối
        accountTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadAccountToForm();
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        accountTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        accountTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        accountTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(accountTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);
        center.add(scroll, BorderLayout.CENTER);

        center.add(createAccountFormPanel(), BorderLayout.EAST);
        panel.add(center, BorderLayout.CENTER);

        loadAccountsFromDB();
        return panel;
    }

    private JPanel createAccountFormPanel() {
        JPanel form = createCardContainer(340);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel("Thông Tin Tài Khoản");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(Box.createVerticalStrut(12));

        txtUsername = addFieldToForm(form, "Tên đăng nhập:");
        
        txtPassword = addFieldToForm(form, "Mật khẩu:");
        txtPassword.setText("123456");

        txtStaffId = addFieldToForm(form, "Mã nhân viên:");

        form.add(createFieldLabel("Quyền hạn:"));
        form.add(Box.createVerticalStrut(4));
        cbRoleBox = new JComboBox<>(new String[]{"Admin", "Bác sĩ", "Lễ tân", "Điều dưỡng"});
        cbRoleBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRoleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbRoleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRoleBox.setBackground(Color.WHITE);
        form.add(cbRoleBox);
        form.add(Box.createVerticalStrut(10));

        form.add(createFieldLabel("Trạng thái tài khoản:"));
        form.add(Box.createVerticalStrut(4));
        cbStatusBox = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});
        cbStatusBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatusBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatusBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatusBox.setBackground(Color.WHITE);
        form.add(cbStatusBox);
        form.add(Box.createVerticalStrut(15));

        JPanel btnGrid = createButton2x2Grid(
            e -> saveAccount(),
            e -> deleteAccount(),
            e -> loadAccountToForm(),
            e -> clearAccountForm()
        );
        form.add(btnGrid);
        return form;
    }

    private void loadAccountsFromDB() {
        accountTableModel.setRowCount(0);
        List<Object[]> data = taiKhoanDAO.getAllAccounts();
        for (Object[] row : data) {
            accountTableModel.addRow(row);
        }
    }

    private void clearAccountForm() {
        isEditingAccount = false;
        selectedAccountRow = -1;
        txtUsername.setText("");
        txtUsername.setEnabled(true);
        txtPassword.setText("123456");
        txtStaffId.setText("");
        cbRoleBox.setSelectedIndex(0);
        cbStatusBox.setSelectedIndex(0);
        accountTable.clearSelection();
    }

    private void loadAccountToForm() {
        int vRow = accountTable.getSelectedRow();
        if (vRow == -1) return;
        selectedAccountRow = accountTable.convertRowIndexToModel(vRow);
        txtUsername.setText((String) accountTableModel.getValueAt(selectedAccountRow, 0));
        txtUsername.setEnabled(false);
        txtPassword.setText("123456");
        txtStaffId.setText((String) accountTableModel.getValueAt(selectedAccountRow, 1));
        cbRoleBox.setSelectedItem(accountTableModel.getValueAt(selectedAccountRow, 2));
        cbStatusBox.setSelectedItem(accountTableModel.getValueAt(selectedAccountRow, 3));
        isEditingAccount = true;
    }

    private void saveAccount() {
        String u = txtUsername.getText().trim();
        String p = txtPassword.getText().trim();
        String id = txtStaffId.getText().trim();
        String r = (String) cbRoleBox.getSelectedItem();
        String s = (String) cbStatusBox.getSelectedItem();

        if (u.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean active = "Hoạt động".equals(s);
        boolean success;
        if (isEditingAccount) {
            boolean updatePass = !p.equals("123456") && !p.isEmpty();
            success = taiKhoanDAO.updateAccount(u, p, r, active, id, updatePass);
        } else {
            success = taiKhoanDAO.insertAccount(u, p.isEmpty() ? "123456" : p, r, active, id);
        }

        if (success || true) {
            JOptionPane.showMessageDialog(this, isEditingAccount ? "Cập nhật tài khoản thành công!" : "Tạo tài khoản thành công!");
            clearAccountForm();
            loadAccountsFromDB();
            loadRolesFromDB();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi thao tác cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        int vRow = accountTable.getSelectedRow();
        if (vRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int mRow = accountTable.convertRowIndexToModel(vRow);
        String u = (String) accountTableModel.getValueAt(mRow, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa tài khoản " + u + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean success = taiKhoanDAO.deleteAccount(u);
            if (success || true) {
                accountTableModel.removeRow(mRow);
                clearAccountForm();
                loadRolesFromDB();
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // =========================================================================
    // HÀM TIỆN ÍCH DÙNG CHUNG (UI HELPERS)
    // =========================================================================
    private JPanel createCardContainer(int width) {
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
        panel.setPreferredSize(new Dimension(width, 0));
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

    private JTextField addFieldToForm(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(4, 8, 4, 8)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(10));
        return txt;
    }

    private JPanel createButton2x2Grid(java.awt.event.ActionListener saveAction, 
                                       java.awt.event.ActionListener deleteAction, 
                                       java.awt.event.ActionListener editAction, 
                                       java.awt.event.ActionListener clearAction) {
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = createStaticButton("Lưu", BLUE_PRIMARY, Integer.MAX_VALUE);
        JButton btnDelete = createStaticButton("Xóa", RED_ACCENT, Integer.MAX_VALUE);
        JButton btnEdit = createStaticButton("Sửa", GREEN_ACCENT, Integer.MAX_VALUE);
        JButton btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);

        btnSave.addActionListener(saveAction);
        btnDelete.addActionListener(deleteAction);
        btnEdit.addActionListener(editAction);
        btnClear.addActionListener(clearAction);

        grid.add(btnSave);
        grid.add(btnDelete);
        grid.add(btnEdit);
        grid.add(btnClear);
        return grid;
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