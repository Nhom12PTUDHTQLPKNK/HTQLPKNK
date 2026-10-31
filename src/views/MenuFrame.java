package views;

import Audit.AuditLog;
import controllers.AuthController;
import models.entity.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import com.google.zxing.WriterException;

public class MenuFrame extends JFrame {

    // màu sắc giao diện
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color NAVY_BG = new Color(15, 29, 69);
    private static final Color TEXT_LIGHT = new Color(240, 244, 248);
    
    private static final Color MENU_BG = new Color(240, 245, 250); 

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private DashboardPanel dashboardPanel;
    private ActivityLogPanel activityLogPanel;

   // hệ thống
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_CHANGE_PASS = "CHANGE_PASS";
    public static final String CARD_HELP = "HELP";
    public static final String CARD_LOGS = "LOGS";
    public static final String CARD_ROLE_PERMISSION = "ROLE_PERMISSION";

   // danh mục
    public static final String CARD_SERVICES = "SERVICES";
    public static final String CARD_PROMOTIONS = "PROMOTIONS";
    public static final String CARD_CUSTOMERS = "CUSTOMERS";
    public static final String CARD_EMPLOYEES = "EMPLOYEES";
    public static final String CARD_MEDICAL_RECORDS = "MEDICAL_RECORDS";
    public static final String CARD_DENTAL_CHART = "DENTAL_CHART";
    public static final String CARD_MEDICINES = "MEDICINES";

   // cập nhật
    public static final String CARD_INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String CARD_APPOINTMENT_UPDATE = "APPOINTMENT_UPDATE";
    public static final String CARD_PERSONNEL_UPDATE = "PERSONNEL_UPDATE";

    // xử lý 
    public static final String CARD_BOOKING = "BOOKING";
    public static final String CARD_SALES_INVOICE = "SALES_INVOICE";
    public static final String CARD_WORK_SHIFT = "WORK_SHIFT";
    public static final String CARD_PROMO_PROCESS = "PROMO_PROCESS";
    public static final String CARD_EXAMINATION = "EXAMINATION";
    public static final String CARD_TREATMENT_PLAN = "TREATMENT_PLAN";
    public static final String CARD_PRESCRIPTION = "PRESCRIPTION";
    public static final String CARD_REEXAMINATION = "REEXAMINATION";

   // tìm kiếm 
    public static final String CARD_SEARCH_CUSTOMER = "SEARCH_CUSTOMER";
    public static final String CARD_SEARCH_APPOINTMENT = "SEARCH_APPOINTMENT";
    public static final String CARD_SEARCH_PROMO = "SEARCH_PROMO";
    public static final String CARD_SEARCH_SHIFT = "SEARCH_SHIFT";
    public static final String CARD_SEARCH_INVENTORY = "SEARCH_INVENTORY";
    public static final String CARD_SEARCH_INVOICE = "SEARCH_INVOICE";

    // báo biểu 
    public static final String CARD_REPORT_REVENUE = "REPORT_REVENUE";
    public static final String CARD_REPORT_SERVICE = "REPORT_SERVICE";
    public static final String CARD_REPORT_WAREHOUSE = "REPORT_WAREHOUSE";
    public static final String CARD_REPORT_EXPORT = "REPORT_EXPORT";

    public MenuFrame() {
        setTitle("SMILE DENTAL MANAGEMENT SYSTEM - HỆ THỐNG QUẢN LÝ PHÒNG KHÁM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 700));
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_CANVAS);

        Account currentUser = AuthController.getCurrentUser();
        String displayName = currentUser == null ? "Nhân viên" : currentUser.getFullName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = currentUser == null ? "Nhân viên" : currentUser.getUsername();
        }

        // thanh menu
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(NAVY_BG);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(true);
        headerBar.setBackground(NAVY_BG);
        headerBar.setBorder(new EmptyBorder(20, 25, 20, 25));
        headerBar.setPreferredSize(new Dimension(0, 105));
        
        // Phần Logo bên trái
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoPanel.setOpaque(false);
        
        JLabel lblLogoIcon = new JLabel();
        lblLogoIcon.setIcon(loadIcon("/img/logo1.png", 68, 68));

        JPanel logoTextPanel = new JPanel();
        logoTextPanel.setLayout(new BoxLayout(logoTextPanel, BoxLayout.Y_AXIS));
        logoTextPanel.setOpaque(false);

        JLabel lblLogoText = new JLabel("SMILE DENTAL");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblLogoText.setForeground(Color.WHITE);

        JLabel lblSubLogo = new JLabel("MANAGEMENT SYSTEM");
        lblSubLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubLogo.setForeground(new Color(150, 170, 200));

        logoTextPanel.add(lblLogoText);
        logoTextPanel.add(lblSubLogo);
        logoPanel.add(lblLogoIcon);
        logoPanel.add(logoTextPanel);

        // Menu Bar chính căn trái gần logo
        JMenuBar menuBar = createMenuBar();
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        centerContainer.setOpaque(false);
        centerContainer.add(menuBar);

        // Phần User & Actions bên phải
        JPanel rightHeaderPanel = new JPanel(new GridBagLayout());
        rightHeaderPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 10);

        // Nút Thông báo
        JButton btnNotification = new JButton();
        ImageIcon bellIcon = loadIcon("/img/bell.png", 42, 42);
        if (bellIcon != null) {
            btnNotification.setIcon(bellIcon);
        } else {
            btnNotification.setText("🔔"); 
        }
        btnNotification.setContentAreaFilled(false);
        btnNotification.setBorderPainted(false);
        btnNotification.setFocusPainted(false);
        btnNotification.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNotification.setToolTipText("Thông báo");
        btnNotification.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Không có thông báo mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        // Popup Menu cho nhân viên
        JPopupMenu userMenu = new JPopupMenu();
        userMenu.setBackground(MENU_BG);
        userMenu.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 1));

        JMenuItem itemLoginQr = new JMenuItem("Tạo / đổi mã QR đăng nhập");
        itemLoginQr.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemLoginQr.setForeground(TEXT_DARK);
        itemLoginQr.setBackground(MENU_BG);
        itemLoginQr.setOpaque(true);
        itemLoginQr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemLoginQr.addActionListener(e -> showLoginQr());

        JMenuItem itemSwitchAccount = new JMenuItem("Đăng nhập bằng tài khoản khác");
        itemSwitchAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemSwitchAccount.setForeground(TEXT_DARK);
        itemSwitchAccount.setBackground(MENU_BG);
        itemSwitchAccount.setOpaque(true);
        itemSwitchAccount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemSwitchAccount.addActionListener(e -> switchAccount());

        JMenuItem itemLogout = new JMenuItem("Đăng xuất");
        itemLogout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemLogout.setForeground(TEXT_DARK);
        itemLogout.setBackground(MENU_BG);
        itemLogout.setOpaque(true);
        itemLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itemLogout.addActionListener(e -> logout());

        for (JMenuItem item : new JMenuItem[]{itemLoginQr, itemSwitchAccount, itemLogout}) {
            item.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    item.setBackground(NAVY_BG);
                    item.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    item.setForeground(PINK_ACCENT);
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    item.setBackground(MENU_BG);
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    item.setForeground(TEXT_DARK);
                }
            });
        }

        userMenu.add(itemLoginQr);
        userMenu.addSeparator();
        userMenu.add(itemSwitchAccount);
        userMenu.add(itemLogout);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userBadge.setOpaque(false);
        userBadge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBadge.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblUserAvatar = new JLabel();
        lblUserAvatar.setIcon(loadIcon("/img/staff.png", 27, 27));

        JLabel lblUserName = new JLabel(displayName + " ▾");
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserName.setForeground(Color.WHITE);

        userBadge.add(lblUserAvatar);
        userBadge.add(lblUserName);

        userBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                userMenu.show(userBadge, 0, userBadge.getHeight() + 5);
            }
        });

        gbc.gridx = 1; rightHeaderPanel.add(btnNotification, gbc);
        gbc.gridx = 2; gbc.insets = new Insets(0, 0, 0, 0); 
        rightHeaderPanel.add(userBadge, gbc);

        headerBar.add(logoPanel, BorderLayout.WEST);
        headerBar.add(centerContainer, BorderLayout.CENTER);
        headerBar.add(rightHeaderPanel, BorderLayout.EAST);

        topContainer.add(headerBar, BorderLayout.CENTER);
        
        JPanel bottomShadow = new JPanel();
        bottomShadow.setBackground(new Color(30, 50, 90));
        bottomShadow.setPreferredSize(new Dimension(0, 2));
        topContainer.add(bottomShadow, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // Menu chính 
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_CANVAS);

       // hệ thống 
        dashboardPanel = new DashboardPanel();
        mainContentPanel.add(dashboardPanel, CARD_DASHBOARD);
        mainContentPanel.add(new ChangePasswordPanel(displayName), CARD_CHANGE_PASS);
        mainContentPanel.add(new HelpPanel(), CARD_HELP);
        activityLogPanel = new ActivityLogPanel();
        mainContentPanel.add(activityLogPanel, CARD_LOGS);
        mainContentPanel.add(new RolePermissionPanel(), CARD_ROLE_PERMISSION);

       // danh mục
        mainContentPanel.add(new ServicePanel(), CARD_SERVICES);
        mainContentPanel.add(new PromotionPanel(), CARD_PROMOTIONS);
        mainContentPanel.add(new CustomerPanel(), CARD_CUSTOMERS);
        mainContentPanel.add(new EmployeePanel(), CARD_EMPLOYEES);
        mainContentPanel.add(new MedicalRecordPanel(), CARD_MEDICAL_RECORDS);

      // cập nhật
        mainContentPanel.add(new InventoryUpdatePanel(), CARD_INVENTORY_UPDATE);
        mainContentPanel.add(new AppointmentUpdatePanel(), CARD_APPOINTMENT_UPDATE);
        mainContentPanel.add(new PersonnelAccountUpdatePanel(), CARD_PERSONNEL_UPDATE);

      // xử lý
        mainContentPanel.add(new BookingProcessPanel(), CARD_BOOKING);
        mainContentPanel.add(new SalesProcessPanel(), CARD_SALES_INVOICE);
        mainContentPanel.add(new WorkProcessPanel(), CARD_WORK_SHIFT);
        mainContentPanel.add(new PromotionProcessPanel(), CARD_PROMO_PROCESS);

        // tìm kiếm 
        addBlankCard(CARD_SEARCH_CUSTOMER);
        addBlankCard(CARD_SEARCH_APPOINTMENT);
        addBlankCard(CARD_SEARCH_PROMO);
        addBlankCard(CARD_SEARCH_SHIFT);
        addBlankCard(CARD_SEARCH_INVENTORY);
        addBlankCard(CARD_SEARCH_INVOICE);

       // báo biểu
        addBlankCard(CARD_REPORT_REVENUE);
        addBlankCard(CARD_REPORT_SERVICE);
        addBlankCard(CARD_REPORT_WAREHOUSE);
        addBlankCard(CARD_REPORT_EXPORT);


      // các màn hình phụ bổ sung
        mainContentPanel.add(new StandardToothChartPanel(), CARD_DENTAL_CHART);
        mainContentPanel.add(new StandardMedicinePanel(), CARD_MEDICINES);
        addBlankCard(CARD_EXAMINATION);

      // CARD TRẮNG ĐÂY NÈ SỬA CÁC CLASS DÁN VÔ ĐÂY
        mainContentPanel.add(new StandardToothChartPanel(), CARD_DENTAL_CHART);
        mainContentPanel.add(new StandardMedicinePanel(), CARD_MEDICINES);
        mainContentPanel.add(new ExaminationProcessPanel(),CARD_EXAMINATION);

        addBlankCard(CARD_TREATMENT_PLAN);
        addBlankCard(CARD_PRESCRIPTION);
        addBlankCard(CARD_REEXAMINATION);

        add(mainContentPanel, BorderLayout.CENTER);
        showView(CARD_DASHBOARD);
        
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void addBlankCard(String cardName) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CANVAS);
        mainContentPanel.add(panel, cardName);
    }

    public void showView(String cardName) {
        if (cardLayout != null && mainContentPanel != null) {
            cardLayout.show(mainContentPanel, cardName);
            AuditLog.event("VIEW", "Mở màn hình " + cardName);
            if (CARD_LOGS.equals(cardName) && activityLogPanel != null) {
                activityLogPanel.refreshTables();
            }
            if (CARD_DASHBOARD.equals(cardName) && dashboardPanel != null) {
                dashboardPanel.refreshDashboardData();
            }
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            AuditLog.logout("LOGOUT", "Người dùng đăng xuất");
            AuthController.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void switchAccount() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có muốn đăng nhập bằng tài khoản khác?",
                "Chuyển tài khoản",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            AuditLog.logout("LOGOUT", "Người dùng chuyển tài khoản");
            AuthController.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void showLoginQr() {
        try {
            byte[] qrImage = new AuthController().regenerateCurrentUserLoginQr();
            if (qrImage == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy tài khoản đang hoạt động để tạo mã QR.",
                        "Không thể tạo mã QR", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.add(new JLabel(new ImageIcon(qrImage), SwingConstants.CENTER), BorderLayout.CENTER);
            content.add(new JLabel("Mã cũ bị vô hiệu hóa. Hãy giữ kín mã QR này.",
                    SwingConstants.CENTER), BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(this, content, "Mã QR đăng nhập",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | IOException | WriterException e) {
            System.err.println("Không thể tạo mã QR đăng nhập: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Không thể tạo mã QR. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                    "Lỗi tạo mã QR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.err.println("Không tìm thấy icon tại đường dẫn: " + path);
        }
        return null;
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY_BG);
        menuBar.setOpaque(false);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        // 1. Hệ thống
        JMenu mnuSystem = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(mnuSystem, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(mnuSystem, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(mnuSystem, "Nhật ký hoạt động", "/img/log.png", CARD_LOGS, KeyEvent.VK_L, ActionEvent.ALT_MASK);
        addItem(mnuSystem, "Phân quyền & Tài khoản", "/img/phanquyen.png", CARD_ROLE_PERMISSION, KeyEvent.VK_U, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuSystem, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        mnuSystem.addSeparator();
        
        JMenuItem itemLogout = createStyledItem("Đăng xuất", "/img/logout.png", 0, 0);
        itemLogout.addActionListener(e -> logout());
        mnuSystem.add(itemLogout);

        // 2. Danh mục
        JMenu mnuCategory = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        addItem(mnuCategory, "Dịch vụ nha khoa", "/img/service.png", CARD_SERVICES, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Chương trình khuyến mãi", "/img/promo.png", CARD_PROMOTIONS, KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Nhân viên", "/img/staff.png", CARD_EMPLOYEES, KeyEvent.VK_E, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Sơ đồ răng chuẩn", "/img/dental.png", CARD_DENTAL_CHART, KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuCategory, "Danh mục thuốc chuẩn", "/img/medicine.png", CARD_MEDICINES, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        // 3. Cập nhật
        JMenu mnuUpdate = createStyledMenu("3. Cập nhật", "/img/capnhat.png");
        addItem(mnuUpdate, "Cập nhật kho vật tư, thiết bị", "/img/warehouse.png", CARD_INVENTORY_UPDATE, KeyEvent.VK_H, ActionEvent.CTRL_MASK);
        addItem(mnuUpdate, "Tiếp nhận lịch hẹn", "/img/appointment.png", CARD_APPOINTMENT_UPDATE, KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        addItem(mnuUpdate, "Cập nhật nhân sự & Tài khoản", "/img/user_update.png", CARD_PERSONNEL_UPDATE, KeyEvent.VK_U, ActionEvent.CTRL_MASK);

        // 4. Xử lý
        JMenu mnuProcess = createStyledMenu("4. Xử lý", "/img/xuly.png");
        addItem(mnuProcess, "Đặt lịch hẹn", "/img/calendar.png", CARD_BOOKING, KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        addItem(mnuProcess, "Khám bệnh & Chỉ định", "/img/medical.png", CARD_EXAMINATION, KeyEvent.VK_F1, 0);
        addItem(mnuProcess, "Lập kế hoạch điều trị", "/img/appointment.png", CARD_TREATMENT_PLAN, KeyEvent.VK_F2, 0);
        addItem(mnuProcess, "Kê đơn thuốc điện tử", "/img/service.png", CARD_PRESCRIPTION, KeyEvent.VK_F3, 0);
        addItem(mnuProcess, "Bán hàng & Hóa đơn (POS)", "/img/billing.png", CARD_SALES_INVOICE, KeyEvent.VK_F4, 0);
        addItem(mnuProcess, "Phân ca lịch làm việc", "/img/shift.png", CARD_WORK_SHIFT, KeyEvent.VK_F5, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuProcess, "Cập nhật khuyến mãi", "/img/discount.png", CARD_PROMO_PROCESS, KeyEvent.VK_F6, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuProcess, "Quản lý & Hẹn tái khám", "/img/appointment.png", CARD_REEXAMINATION, KeyEvent.VK_F7, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        // 5. Tìm kiếm
        JMenu mnuSearch = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(mnuSearch, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(mnuSearch, "Tra cứu lịch hẹn", "/img/timkiem.png", CARD_SEARCH_APPOINTMENT, KeyEvent.VK_F7, 0);
        addItem(mnuSearch, "Tra cứu khuyến mãi", "/img/timkiem.png", CARD_SEARCH_PROMO, KeyEvent.VK_F8, 0);
        addItem(mnuSearch, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(mnuSearch, "Tra cứu tồn kho, thuốc", "/img/timkiem.png", CARD_SEARCH_INVENTORY, KeyEvent.VK_F10, 0);
        addItem(mnuSearch, "Tra cứu hóa đơn, phiếu thu", "/img/timkiem.png", CARD_SEARCH_INVOICE, KeyEvent.VK_F11, 0);

        // 6. Báo biểu
        JMenu mnuReport = createStyledMenu("6. Báo biểu", "/img/baobieu.png");
        addItem(mnuReport, "Doanh thu, chi phí & công nợ", "/img/report_revenue.png", CARD_REPORT_REVENUE, KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuReport, "Thống kê dịch vụ & hiệu suất nha sĩ", "/img/report_service.png", CARD_REPORT_SERVICE, KeyEvent.VK_E, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuReport, "Thống kê xuất nhập tồn kho", "/img/report_warehouse.png", CARD_REPORT_WAREHOUSE, KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuReport, "Kết xuất báo cáo Excel / PDF", "/img/thongke.png", CARD_REPORT_EXPORT, KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        menuBar.add(mnuSystem);
        menuBar.add(mnuCategory);
        menuBar.add(mnuUpdate);
        menuBar.add(mnuProcess);
        menuBar.add(mnuSearch);
        menuBar.add(mnuReport);

        return menuBar;
    }

    private JMenu createStyledMenu(String text, String iconPath) {
        JMenu menu = new JMenu(text);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menu.setForeground(TEXT_LIGHT);
        menu.setBackground(NAVY_BG);
        menu.setOpaque(true);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 7, 10, 7)); 
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon icon = loadIcon(iconPath, 25, 25);
        if (icon != null) {
            menu.setIcon(icon);
            menu.setIconTextGap(6);
        }

        menu.getPopupMenu().setBackground(MENU_BG);
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 1));

        menu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menu.setBackground(new Color(25, 45, 95));
                menu.setForeground(PINK_ACCENT);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                menu.setBackground(NAVY_BG);
                menu.setForeground(TEXT_LIGHT);
            }
        });

        return menu;
    }

    private JMenuItem createStyledItem(String text, String iconPath, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem("     " + text);

        item.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        item.setForeground(TEXT_DARK);
        item.setBackground(MENU_BG);
        item.setOpaque(true);

        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setVerticalAlignment(SwingConstants.CENTER);

        item.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon icon = loadIcon(iconPath, 23, 23);
        if (icon != null) {
            item.setIcon(icon);
            item.setIconTextGap(20);
        }

        if (keyCode != 0) {
            KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers);
            item.setAccelerator(ks);
        }

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(NAVY_BG);
                item.setFont(new Font("Segoe UI", Font.BOLD, 17));
                item.setForeground(PINK_ACCENT);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(MENU_BG);
                item.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                item.setForeground(TEXT_DARK);
            }
        });

        return item;
    }

    private void addItem(JMenu menu, String text, String iconPath, String cardName, int keyCode, int modifiers) {
        JMenuItem item = createStyledItem(text, iconPath, keyCode, modifiers);
        item.addActionListener(e -> showView(cardName));
        menu.add(item);
    }
}