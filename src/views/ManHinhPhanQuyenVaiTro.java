package views;

import models.dao.NhanSuTaiKhoanDAO;
import controllers.KiemSoatTruyCap;
import controllers.XuatBangPDF;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ManHinhPhanQuyenVaiTro extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(34, 150, 83);
    private static final Color RED_ACCENT = new Color(217, 45, 32);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private final NhanSuTaiKhoanDAO personnelAccountDAO = new NhanSuTaiKhoanDAO();

    // Thẻ thống kê
    private JLabel lblTotalAccounts, lblActiveAccounts, lblLockedAccounts;

    // Bảng Tài Khoản & Phân Quyền
    private DefaultTableModel accountTableModel;
    private JTable accountTable;
    private JTextField txtAccountSearch;
    private TableRowSorter<DefaultTableModel> accountRowSorter;

    public ManHinhPhanQuyenVaiTro() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(25, 30, 25, 30));

     // 1. HEADER DẠNG CARD NGANG (GRADIENT HỒNG <-> XANH + LOGO)
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Định nghĩa màu hồng pastel và xanh pastel
                Color pinkPastel = new Color(255, 232, 243); 
                Color bluePastel = new Color(219, 234, 254); 
                
                // Hiệu ứng trải màu từ trái sang phải
                GradientPaint gradient = new GradientPaint(0, 0, pinkPastel, getWidth(), 0, bluePastel);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Viền card
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        headerCard.setLayout(new BorderLayout());
        headerCard.setPreferredSize(new Dimension(0, 110));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        
        // Tải và thu nhỏ logo cho cân đối (Ví dụ: 45x45 pixel)
        ImageIcon logoIcon = null;
        try {
            java.net.URL imgUrl = getClass().getResource("/img/logo5.png"); 
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(scaledImg);
            } else {
                System.out.println("Không tìm thấy file ảnh tại: /img/logo5.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblTitle = new JLabel("Quản Lý Tài Khoản & Phân Quyền");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(TEXT_DARK);

        
        if (logoIcon != null) {
            lblTitle.setIcon(logoIcon);
            lblTitle.setIconTextGap(14); // Khoảng cách giữa ảnh và chữ tiêu đề
        }
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        // Căn giữa tiêu đề theo chiều dọc trong card
        JPanel headerWestWrapper = new JPanel(new GridBagLayout());
        headerWestWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        headerWestWrapper.add(titleBox, gbc);

        headerCard.add(headerWestWrapper, BorderLayout.CENTER);
        
        add(headerCard, BorderLayout.NORTH);

        // 2. CONTAINER CHÍNH
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);

     // 3. THẺ THỐNG KÊ (3 STATS CARDS VỚI 3 ẢNH RIÊNG BIỆT)
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        lblTotalAccounts = new JLabel("0 tài khoản");
        lblActiveAccounts = new JLabel("0 tài khoản");
        lblLockedAccounts = new JLabel("0 tài khoản");

        // TheThongTin 1: Ảnh tổng tài khoản (Ví dụ: /img/total.png)
        statsRow.add(createStatCard("TỔNG TÀI KHOẢN HỆ THỐNG", lblTotalAccounts, BLUE_PRIMARY, new Color(232, 242, 255), "/img/ok.png"));
        
        // TheThongTin 2: Ảnh đang hoạt động (Ví dụ: /img/active.png)
        statsRow.add(createStatCard("ĐANG HOẠT ĐỘNG", lblActiveAccounts, GREEN_ACCENT, new Color(238, 247, 238), "/img/yes.png"));
        
        // TheThongTin 3: Ảnh đã khóa (Ví dụ: /img/locked.png)
        statsRow.add(createStatCard("ĐÃ KHÓA", lblLockedAccounts, RED_ACCENT, new Color(254, 242, 242), "/img/no.png"));

        mainContent.add(statsRow);
        mainContent.add(Box.createVerticalStrut(30));
     // 4. THANH TOOLBAR & BẢNG DỮ LIỆU
        JPanel contentCard = createCardContainer();
        contentCard.setLayout(new BorderLayout(0, 15));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        // --- KHUNG Ô TÌM KIẾM TÙY CHỈNH (CÓ ICON KÍNH LÚP & PLACEHOLDER) ---
        JPanel searchBox = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(380, 42)); // Chiều dài ô tìm kiếm
        searchBox.setBorder(new EmptyBorder(5, 12, 5, 12));

        // 1. Tải và gắn icon kính lúp vào bên trái (WEST)
        JLabel lblSearchIcon = new JLabel();
        try {
            // HÃY ĐẢM BẢO TÊN FILE TRÙNG KHỚP VỚI FILE TRONG THƯ MỤC IMG CỦA BẠN (VD: search_icon.png)
            java.net.URL imgUrl = getClass().getResource("/img/search_icon.png"); 
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                lblSearchIcon.setIcon(new ImageIcon(scaledImg));
            } else {
                System.out.println("CẢNH BÁO: Không tìm thấy file ảnh kính lúp tại /img/search_icon.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        searchBox.add(lblSearchIcon, BorderLayout.WEST);

        // 2. Ô nhập liệu JTextField nằm ở giữa (CENTER)
        txtAccountSearch = new JTextField();
        txtAccountSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAccountSearch.setBorder(null);
        txtAccountSearch.setOpaque(false);
        txtAccountSearch.putClientProperty("JTextField.placeholderText", "Nhập mã NV, họ tên...");
        searchBox.add(txtAccountSearch, BorderLayout.CENTER);

     // 3-Nút tìm kiếm
        JButton btnSearch = createStaticButton(
                "Tìm kiếm", BLUE_PRIMARY, 100, 42);

        btnSearch.addActionListener(e -> {
            String kw = txtAccountSearch.getText().trim();

            if (kw.isEmpty() || kw.equals("Nhập mã NV, họ tên...")) {
                accountRowSorter.setRowFilter(null);
            } else {
                accountRowSorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + kw));
            }
        });

        // 4 - Nút tải lại dữ liệu
        JButton btnReload = createStaticButton(
                "Tải lại", new Color(34, 150, 83), 90, 42);

        btnReload.addActionListener(e -> {
            txtAccountSearch.setText("");
            accountRowSorter.setRowFilter(null);
            loadAccountsFromDB();

            JOptionPane.showMessageDialog(
                    this,
                    "Đã tải lại danh sách tài khoản!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // 4. Gom nhóm ô tìm kiếm và nút vào toolbar
        JPanel searchGroup = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0));

        searchGroup.setOpaque(false);

        searchGroup.add(searchBox);
        searchGroup.add(btnSearch);
        searchGroup.add(btnReload);

        toolbar.add(searchGroup, BorderLayout.WEST);

        contentCard.add(toolbar, BorderLayout.NORTH);
        

        

        contentCard.add(toolbar, BorderLayout.NORTH);

        // Bảng dữ liệu chuẩn theo giao diện mẫu
        String[] cols = {"Mã NV", "Tên Đăng Nhập", "Họ Tên Nhân Sự", "Vai Trò / Phân Quyền", "Trạng Thái", "Thao Tác"};
        accountTableModel = new DefaultTableModel(null, cols) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        accountTable = new JTable(accountTableModel);
        XuatBangPDF.install(accountTable, "Danh sách tài khoản hệ thống");
        accountRowSorter = new TableRowSorter<>(accountTableModel);
        accountTable.setRowSorter(accountRowSorter);
        styleTable(accountTable);

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        accountTable.getColumnModel().getColumn(4).setCellRenderer(new BoVeHuyHieuTrangThai());
        accountTable.getColumnModel().getColumn(5).setCellRenderer(new BoVeNutThaoTac());
        accountTable.getColumnModel().getColumn(5).setCellEditor(new BoSuaNutThaoTac(new JCheckBox(), this));

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
    private JPanel createStatCard(String title, JLabel valueLbl, Color accentColor, Color bgCard, String imagePath) {
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
        card.setBorder(new EmptyBorder(25, 25, 25, 25)); // Tăng padding để card cao ráo, dày dặn
        card.setLayout(new BorderLayout(18, 0)); // Khoảng cách ngang 18px giữa ảnh và chữ

        // 1. Tải ảnh từ thư mục img đặt ở bên trái (WEST)
        JLabel lblIcon = new JLabel();
        try {
            java.net.URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                lblIcon.setIcon(new ImageIcon(scaledImg));
            } else {
                System.out.println("Không tìm thấy ảnh tại: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        card.add(lblIcon, BorderLayout.WEST);

        // 2. Panel chứa chữ (Tiêu đề + Số lượng) ở giữa (CENTER)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Chữ số to rõ, nổi bật
        valueLbl.setForeground(accentColor);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(valueLbl);

        card.add(textPanel, BorderLayout.CENTER);
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

        // ===== TÙY CHỈNH NỘI DUNG BẢNG =====
        table.setRowHeight(55);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(235, 240, 248));
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        // ===== TIÊU ĐỀ BẢNG =====
        JTableHeader header = table.getTableHeader();

        header.setFont(new Font("Segoe UI", Font.BOLD, 17));
        header.setBackground(new Color(38, 56, 101));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 50));
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        // ===== RENDERER TIÊU ĐỀ =====
        DefaultTableCellRenderer headerRenderer =
                new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                JLabel lbl = new JLabel(
                        value == null ? "" : value.toString(),
                        SwingConstants.CENTER
                );

                lbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(new Color(38, 56, 101));
                lbl.setOpaque(true);

                lbl.setBorder(BorderFactory.createMatteBorder(
                        0, 0, 1, 1, new Color(75, 91, 130)
                ));

                return lbl;
            }
        };

        header.setDefaultRenderer(headerRenderer);
    }
        
    private static class BoVeHuyHieuTrangThai extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel lbl = new JLabel(
                    value == null ? "" : value.toString(),
                    SwingConstants.CENTER
            );

            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setOpaque(true);

            String status = value == null ? "" : value.toString();

            if ("Hoạt động".equals(status)) {
                lbl.setForeground(new Color(22, 130, 70));
                lbl.setBackground(new Color(225, 245, 230));
            } else if ("Tạm khóa".equals(status)) {
                lbl.setForeground(new Color(217, 45, 32));
                lbl.setBackground(new Color(253, 237, 236));
            } else {
                lbl.setForeground(MUTED);
                lbl.setBackground(new Color(240, 244, 248));
            }

            lbl.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(
                    isSelected ? table.getSelectionBackground() : Color.WHITE
            );
            panel.add(lbl);

            return panel;
        }
    }

    private static class BoVeNutThaoTac extends JPanel
    implements javax.swing.table.TableCellRenderer {

private final JButton btnEdit = new JButton("Sửa");
private final JButton btnLock = new JButton("Khóa");

public BoVeNutThaoTac() {

    setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
    setOpaque(true);

    styleButton(btnEdit, new Color(240, 244, 248), TEXT_DARK);
    styleButton(btnLock, new Color(254, 243, 199), new Color(146, 64, 14));

    add(btnEdit);
    add(btnLock);
}

private void styleButton(JButton btn, Color bg, Color fg) {

    btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btn.setPreferredSize(new Dimension(72, 36));
    btn.setMinimumSize(new Dimension(72, 36));

    btn.setBackground(bg);
    btn.setForeground(fg);

    btn.setFocusPainted(false);
    btn.setBorderPainted(true);
    btn.setContentAreaFilled(true);
    btn.setOpaque(true);

    btn.setMargin(new Insets(5, 12, 5, 12));
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}

@Override
public Component getTableCellRendererComponent(
        JTable table, Object value,
        boolean isSelected, boolean hasFocus,
        int row, int column) {

    setBackground(
        isSelected ? table.getSelectionBackground() : Color.WHITE
    );

    return this;
}
}
    

    private static class BoSuaNutThaoTac extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        private final JButton btnEdit = new JButton("Sửa");
        private final JButton btnLock = new JButton("Khóa");
        private String currentStaffId;
        private String currentStaffName;
        private String currentUsername;
        private String currentRole;
        private String currentStatus;
        private final ManHinhPhanQuyenVaiTro outer;

        public BoSuaNutThaoTac(JCheckBox checkBox, ManHinhPhanQuyenVaiTro outerInstance) {
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