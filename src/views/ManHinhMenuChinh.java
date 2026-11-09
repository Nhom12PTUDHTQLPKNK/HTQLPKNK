package views;

import controllers.TruongChonNgayCu;
import java.time.LocalDate;
import controllers.NhatKyKiemToan;
import controllers.DieuKhienXacThuc;
import models.entity.TaiKhoanThongTin;
import models.entity.VaiTro;
import controllers.KiemSoatTruyCap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.sql.SQLException;

import com.google.zxing.WriterException;

public class ManHinhMenuChinh extends JFrame {

    // màu sắc giao diện[cite: 48, 52]
    private static final Color BLUE = new Color(11, 95, 204);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color NAVY_BG = new Color(15, 29, 69);
    private static final Color TEXT_LIGHT = new Color(240, 244, 248);

    private static final Color MENU_BG = new Color(240, 245, 250);

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private ManHinhBangDieuKhien dashboardPanel;
    private ManHinhNhatKyHoatDong activityLogPanel;
    private ManHinhDatLichHen appointmentBookingPanel;
    private ManHinhXuLyLichHen bookingPanel;
    private JPanel roleMenuContainer;

   // hệ thống[cite: 48, 52]
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_CHANGE_PASS = "CHANGE_PASS";
    public static final String CARD_HELP = "HELP";
    public static final String CARD_LOGS = "LOGS";
    public static final String CARD_ROLE_PERMISSION = "ROLE_PERMISSION";

   // danh mục[cite: 48, 52]
    public static final String CARD_SERVICES = "SERVICES";
    public static final String CARD_PROMOTIONS = "PROMOTIONS";
    public static final String CARD_CUSTOMERS = "CUSTOMERS";
    public static final String CARD_EMPLOYEES = "EMPLOYEES";
    public static final String CARD_MEDICAL_RECORDS = "MEDICAL_RECORDS";
    public static final String CARD_DENTAL_CHART = "DENTAL_CHART";
    public static final String CARD_MEDICINES = "MEDICINES";
    public static final String CARD_MEDICINE_GROUPS = "MEDICINE_GROUPS";
    public static final String CARD_MEDICINE_FORMS = "MEDICINE_FORMS";
    public static final String CARD_MEDICINE_ROUTES = "MEDICINE_ROUTES";
    public static final String CARD_SERVICE_TYPES = "SERVICE_TYPES";
    public static final String CARD_JOB_TITLES = "JOB_TITLES";

   // cập nhật[cite: 48, 52]
    public static final String CARD_INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String CARD_APPOINTMENT_UPDATE = "APPOINTMENT_UPDATE";
    public static final String CARD_PERSONNEL_UPDATE = "PERSONNEL_UPDATE";

    // xử lý[cite: 48, 52]
    public static final String CARD_BOOKING = "BOOKING";
    public static final String CARD_SALES_INVOICE = "SALES_INVOICE";
    public static final String CARD_WORK_SHIFT = "WORK_SHIFT";
    public static final String CARD_PROMO_PROCESS = "PROMO_PROCESS";
    public static final String CARD_EXAMINATION = "EXAMINATION";
    public static final String CARD_TREATMENT_PLAN = "TREATMENT_PLAN";
    public static final String CARD_PRESCRIPTION = "PRESCRIPTION";

   // tìm kiếm[cite: 48, 52]
    public static final String CARD_SEARCH_CUSTOMER = "SEARCH_CUSTOMER";
    public static final String CARD_SEARCH_APPOINTMENT = "SEARCH_APPOINTMENT";
    public static final String CARD_SEARCH_PROMO = "SEARCH_PROMO";
    public static final String CARD_SEARCH_SHIFT = "SEARCH_SHIFT";
    public static final String CARD_SEARCH_INVENTORY = "SEARCH_INVENTORY";
    public static final String CARD_SEARCH_INVOICE = "SEARCH_INVOICE";

    // báo biểu[cite: 48, 52]
    public static final String CARD_REPORT_REVENUE = "REPORT_REVENUE";
    public static final String CARD_REPORT_SERVICE = "REPORT_SERVICE";
    public static final String CARD_REPORT_WAREHOUSE = "REPORT_WAREHOUSE";

    public ManHinhMenuChinh() {
        setTitle("SMILE DENTAL MANAGEMENT SYSTEM - HỆ THỐNG QUẢN LÝ PHÒNG KHÁM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));
        setSize(1900, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_CANVAS);

        TaiKhoanThongTin currentUser = DieuKhienXacThuc.getCurrentUser();
        VaiTro userRole = (currentUser != null) ? currentUser.role : VaiTro.LE_TAN; // Lấy VaiTro trực tiếp từ tài khoản đăng nhập[cite: 32, 42, 48, 52]
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

        // Phần Logo bên trái[cite: 48, 52]
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

        // PHÂN QUYỀN GIAO DIỆN: Gọi MenuBar tương ứng với từng VaiTro
        JMenuBar menuBar = createMenuBarForRole(userRole);
        roleMenuContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        roleMenuContainer.setOpaque(false);
        roleMenuContainer.add(menuBar);

        // Phần User & Actions bên phải[cite: 48, 52]
        JPanel rightHeaderPanel = new JPanel(new GridBagLayout());
        rightHeaderPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 10, 0, 10);

        // Nút Thông báo[cite: 48, 52]
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

        // Popup Menu cho nhân viên[cite: 48, 52]
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

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userBadge.setOpaque(false);
        userBadge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userBadge.setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel lblUserAvatar = new JLabel();
        lblUserAvatar.setIcon(loadIcon("/img/staff.png", 27, 27));

        JLabel lblUserName = new JLabel(displayName + " (" + userRole.toString() + ") ▾");
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
        headerBar.add(roleMenuContainer, BorderLayout.CENTER);
        headerBar.add(rightHeaderPanel, BorderLayout.EAST);

        topContainer.add(headerBar, BorderLayout.CENTER);

        JPanel bottomShadow = new JPanel();
        bottomShadow.setBackground(new Color(30, 50, 90));
        bottomShadow.setPreferredSize(new Dimension(0, 2));
        topContainer.add(bottomShadow, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                adaptHeader(getWidth(), menuBar, headerBar, roleMenuContainer, logoPanel,
                        lblLogoIcon, lblLogoText, lblSubLogo, btnNotification, lblUserAvatar, lblUserName);
            }
        });

        // Menu chính (Giữ nguyên toàn bộ card layout đã đăng ký)[cite: 48, 52]
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_CANVAS);

        dashboardPanel = new ManHinhBangDieuKhien();
        mainContentPanel.add(dashboardPanel, CARD_DASHBOARD);
        mainContentPanel.add(new ManHinhDoiMatKhau(displayName), CARD_CHANGE_PASS);
        mainContentPanel.add(new ManHinhTroGiup(), CARD_HELP);

        activityLogPanel = new ManHinhNhatKyHoatDong();
        mainContentPanel.add(activityLogPanel, CARD_LOGS);
        mainContentPanel.add(new ManHinhPhanQuyenVaiTro(), CARD_ROLE_PERMISSION);
        mainContentPanel.add(new ManHinhChucVu(this::refreshRoleMenu), CARD_JOB_TITLES);

        mainContentPanel.add(new ManHinhDichVu(), CARD_SERVICES);
        mainContentPanel.add(new ManHinhKhuyenMai(), CARD_PROMOTIONS);
        mainContentPanel.add(new ManHinhKhachHang(), CARD_CUSTOMERS);
        mainContentPanel.add(new ManHinhNhanVien(), CARD_EMPLOYEES);
        mainContentPanel.add(new ManHinhHoSoBenhAn(), CARD_MEDICAL_RECORDS);

        mainContentPanel.add(new ManHinhCapNhatKho(), CARD_INVENTORY_UPDATE);
        bookingPanel = new ManHinhXuLyLichHen();
        mainContentPanel.add(bookingPanel, CARD_APPOINTMENT_UPDATE);
        mainContentPanel.add(new ManHinhCapNhatNhanSuTaiKhoan(), CARD_PERSONNEL_UPDATE);

        appointmentBookingPanel = new ManHinhDatLichHen();
        mainContentPanel.add(appointmentBookingPanel, CARD_BOOKING);
        mainContentPanel.add(new ManHinhXuLyBanHang(), CARD_SALES_INVOICE);
        mainContentPanel.add(new ManHinhPhanCaLamViec(), CARD_WORK_SHIFT);
        mainContentPanel.add(new ManHinhXuLyKhuyenMai(), CARD_PROMO_PROCESS);

        mainContentPanel.add(new ManHinhTraCuuKhachHang(), CARD_SEARCH_CUSTOMER);
        mainContentPanel.add(new ManHinhTraCuuLichHen(), CARD_SEARCH_APPOINTMENT);
        mainContentPanel.add(new ManHinhTraCuuKhuyenMai(), CARD_SEARCH_PROMO);
        mainContentPanel.add(new ManHinhTraCuuLichLamViec(), CARD_SEARCH_SHIFT);
        mainContentPanel.add(new ManHinhTraCuuKho(), CARD_SEARCH_INVENTORY);
        mainContentPanel.add(new ManHinhTraCuuHoaDonPhieuThu(), CARD_SEARCH_INVOICE);

        mainContentPanel.add(new ManHinhBaoCaoDoanhThuCongNo(), CARD_REPORT_REVENUE);
        mainContentPanel.add(new ManHinhBaoCaoDichVuNhaSi(), CARD_REPORT_SERVICE);
        mainContentPanel.add(new ManHinhBaoCaoNhapXuatKho(), CARD_REPORT_WAREHOUSE);

        mainContentPanel.add(new ManHinhSoDoRangChuan(), CARD_DENTAL_CHART);
        ManHinhDanhMucThuoc medicineCatalogPanel = new ManHinhDanhMucThuoc();
        mainContentPanel.add(medicineCatalogPanel, CARD_MEDICINES);
        mainContentPanel.add(medicineCatalogPanel.createReferenceManagementScreen("Danh mục nhóm thuốc", "Quản lý nhóm phân loại thuốc.", "NHOMTHUOC", "MaNhomThuoc", "TenNhomThuoc", "MoTa"), CARD_MEDICINE_GROUPS);
        mainContentPanel.add(medicineCatalogPanel.createReferenceManagementScreen("Danh mục dạng bào chế", "Quản lý các dạng bào chế thuốc.", "DANGBAOCHE", "MaDangBaoChe", "TenDangBaoChe", "MoTa"), CARD_MEDICINE_FORMS);
        mainContentPanel.add(medicineCatalogPanel.createReferenceManagementScreen("Danh mục đường dùng", "Quản lý đường sử dụng thuốc.", "DUONGDUNG", "MaDuongDung", "TenDuongDung", "MoTa"), CARD_MEDICINE_ROUTES);
        mainContentPanel.add(medicineCatalogPanel.createReferenceManagementScreen("Danh mục loại điều trị", "Quản lý loại điều trị dùng cho dịch vụ nha khoa.", "DanhMucLoaiDieuTri", "maLoaiDieuTri", "tenLoaiDieuTri", "moTa"), CARD_SERVICE_TYPES);
        mainContentPanel.add(new ManHinhXuLyKhamBenh(), CARD_EXAMINATION);
        mainContentPanel.add(new ManHinhKeHoachDieuTri(), CARD_TREATMENT_PLAN);
        mainContentPanel.add(new ManHinhKeDonThuocDienTu(), CARD_PRESCRIPTION);

        add(mainContentPanel, BorderLayout.CENTER);
        showView(CARD_DASHBOARD);

        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    public void showView(String cardName) {
        if (cardLayout == null || mainContentPanel == null) {
            return;
        }

        if (!KiemSoatTruyCap.canAccessCard(cardName)) {
            JOptionPane.showMessageDialog(this, "Chức vụ của bạn chưa được cấp quyền truy cập chức năng này.", "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cardLayout.show(mainContentPanel, cardName);

        if (CARD_LOGS.equals(cardName) && activityLogPanel != null) {
            activityLogPanel.reloadSource();
        }
        if (CARD_DASHBOARD.equals(cardName) && dashboardPanel != null) {
            dashboardPanel.refreshDashboardData();
        }
        if (CARD_APPOINTMENT_UPDATE.equals(cardName) && bookingPanel != null) {
            bookingPanel.refreshData();
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            NhatKyKiemToan.logout("LOGOUT", "Người dùng đăng xuất");
            DieuKhienXacThuc.logout();
            new ManHinhDangNhap().setVisible(true);
            dispose();
        }
    }

    private void switchAccount() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Bạn có muốn đăng nhập bằng tài khoản khác?",
                "Chuyển tài khoản", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            NhatKyKiemToan.logout("LOGOUT", "Người dùng chuyển tài khoản");
            DieuKhienXacThuc.logout();
            new ManHinhDangNhap().setVisible(true);
            dispose();
        }
    }

    private void showLoginQr() {
        try {
            byte[] qrImage = new DieuKhienXacThuc().regenerateCurrentUserLoginQr();
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

    // =========================================================================
    // HỆ THỐNG PHÂN QUYỀN 4 THANH MENU RIÊNG BIỆT (GIỮ NGUYÊN GIAO DIỆN GỐC)
    // =========================================================================
    public JMenuBar createMenuBarForRole(VaiTro role) {
        return KiemSoatTruyCap.isAdministrator() ? createAdminMenuBar() : createPermissionMenuBar();
    }

    private void refreshRoleMenu() {
        if (roleMenuContainer == null) return;
        TaiKhoanThongTin current = DieuKhienXacThuc.getCurrentUser();
        roleMenuContainer.removeAll();
        roleMenuContainer.add(createMenuBarForRole(current == null ? VaiTro.LE_TAN : current.role));
        roleMenuContainer.revalidate();
        roleMenuContainer.repaint();
    }

    /** Dựng toàn bộ menu chức năng rồi chỉ thêm mục được quyền trong ChucVuQuyen. */
    private JMenuBar createPermissionMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(NAVY_BG);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder());

        JMenu system = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(system, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(system, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(system, "Nhật ký hoạt động", "/img/log.png", CARD_LOGS, KeyEvent.VK_L, ActionEvent.ALT_MASK);
        addItem(system, "Phân quyền & Tài khoản", "/img/phanquyen.png", CARD_ROLE_PERMISSION, KeyEvent.VK_U, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(system, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        bar.add(system);

        JMenu category = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        addItem(category, "Chức vụ & quyền", "/img/phanquyen.png", CARD_JOB_TITLES, 0, 0);
        addItem(category, "Dịch vụ nha khoa", "/img/service.png", CARD_SERVICES, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        addItem(category, "Chương trình khuyến mãi", "/img/promo.png", CARD_PROMOTIONS, KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        addItem(category, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(category, "Nhân viên", "/img/staff.png", CARD_EMPLOYEES, KeyEvent.VK_E, ActionEvent.CTRL_MASK);
        addItem(category, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        addItem(category, "Sơ đồ răng chuẩn", "/img/dental.png", CARD_DENTAL_CHART, 0, 0);
        addItem(category, "Danh mục thuốc chuẩn", "/img/medicine.png", CARD_MEDICINES, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addReferenceCategoryItems(category);
        bar.add(category);

        JMenu update = createStyledMenu("3. Cập nhật", "/img/capnhat.png");
        addItem(update, "Cập nhật kho vật tư, thiết bị", "/img/warehouse.png", CARD_INVENTORY_UPDATE, KeyEvent.VK_H, ActionEvent.CTRL_MASK);
        addItem(update, "Tiếp nhận lịch hẹn", "/img/appointment.png", CARD_APPOINTMENT_UPDATE, KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        addItem(update, "Cập nhật nhân sự & Tài khoản", "/img/user_update.png", CARD_PERSONNEL_UPDATE, KeyEvent.VK_U, ActionEvent.CTRL_MASK);
        bar.add(update);

        JMenu process = createStyledMenu("4. Xử lý", "/img/xuly.png");
        addItem(process, "Đặt lịch hẹn", "/img/calendar.png", CARD_BOOKING, KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        addItem(process, "Khám bệnh & Chỉ định", "/img/medical.png", CARD_EXAMINATION, KeyEvent.VK_F1, 0);
        addItem(process, "Lập kế hoạch điều trị", "/img/medical.png", CARD_TREATMENT_PLAN, 0, 0);
        addItem(process, "Kê đơn thuốc điện tử", "/img/service.png", CARD_PRESCRIPTION, KeyEvent.VK_F3, 0);
        addItem(process, "Bán hàng & Hóa đơn (POS)", "/img/billing.png", CARD_SALES_INVOICE, KeyEvent.VK_F4, 0);
        addItem(process, "Phân ca lịch làm việc", "/img/shift.png", CARD_WORK_SHIFT, KeyEvent.VK_F5, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(process, "Xử lý khuyến mãi", "/img/promo.png", CARD_PROMO_PROCESS, 0, 0);
        bar.add(process);

        JMenu search = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(search, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(search, "Tra cứu lịch hẹn", "/img/timkiem.png", CARD_SEARCH_APPOINTMENT, KeyEvent.VK_F7, 0);
        addItem(search, "Tra cứu khuyến mãi", "/img/timkiem.png", CARD_SEARCH_PROMO, KeyEvent.VK_F8, 0);
        addItem(search, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(search, "Tra cứu tồn kho, thuốc", "/img/timkiem.png", CARD_SEARCH_INVENTORY, KeyEvent.VK_F10, 0);
        addItem(search, "Tra cứu hóa đơn, phiếu thu", "/img/timkiem.png", CARD_SEARCH_INVOICE, KeyEvent.VK_F11, 0);
        bar.add(search);

        JMenu reports = createStyledMenu("6. Báo cáo", "/img/report.png");
        addItem(reports, "Doanh thu, chi phí & công nợ", "/img/report_revenue.png", CARD_REPORT_REVENUE, KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(reports, "Thống kê dịch vụ & hiệu suất nha sĩ", "/img/report_service.png", CARD_REPORT_SERVICE, KeyEvent.VK_E, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(reports, "Thống kê xuất nhập tồn kho", "/img/report_warehouse.png", CARD_REPORT_WAREHOUSE, KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        bar.add(reports);

        for (int i = bar.getMenuCount() - 1; i >= 0; i--) {
            JMenu menu = bar.getMenu(i);
            if (menu != null && menu.getItemCount() == 0) bar.remove(i);
        }
        return bar;
    }

    // 1. Menu Quản lý / Admin 
    private void addReferenceCategoryItems(JMenu menu) {
        addItem(menu, "Nhóm thuốc", "/img/medicine.png", CARD_MEDICINE_GROUPS, 0, 0);
        addItem(menu, "Dạng bào chế", "/img/medicine.png", CARD_MEDICINE_FORMS, 0, 0);
        addItem(menu, "Đường dùng", "/img/medicine.png", CARD_MEDICINE_ROUTES, 0, 0);
        addItem(menu, "Loại điều trị", "/img/service.png", CARD_SERVICE_TYPES, 0, 0);
    }

    private JMenuBar createAdminMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY_BG);
        menuBar.setOpaque(false);
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        JMenu mnuSystem = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(mnuSystem, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(mnuSystem, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(mnuSystem, "Nhật ký hoạt động", "/img/log.png", CARD_LOGS, KeyEvent.VK_L, ActionEvent.ALT_MASK);
        if (KiemSoatTruyCap.isAdministrator()) addItem(mnuSystem, "Phân quyền & Tài khoản", "/img/phanquyen.png", CARD_ROLE_PERMISSION, KeyEvent.VK_U, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuSystem, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        mnuSystem.addSeparator();
        JMenuItem itemLogout = createStyledItem("Đăng xuất", "/img/logout.png", 0, 0);
        itemLogout.addActionListener(e -> logout());
        mnuSystem.add(itemLogout);

        JMenu mnuCategory = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        if (KiemSoatTruyCap.isAdministrator()) addItem(mnuCategory, "Chức vụ & quyền", "/img/phanquyen.png", CARD_JOB_TITLES, 0, 0);
        addItem(mnuCategory, "Dịch vụ nha khoa", "/img/service.png", CARD_SERVICES, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Chương trình khuyến mãi", "/img/promo.png", CARD_PROMOTIONS, KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Nhân viên", "/img/staff.png", CARD_EMPLOYEES, KeyEvent.VK_E, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Sơ đồ răng chuẩn", "/img/dental.png", CARD_DENTAL_CHART, KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuCategory, "Danh mục thuốc chuẩn", "/img/medicine.png", CARD_MEDICINES, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addReferenceCategoryItems(mnuCategory);

        JMenu mnuUpdate = createStyledMenu("3. Cập nhật", "/img/capnhat.png");
        addItem(mnuUpdate, "Cập nhật kho vật tư, thiết bị", "/img/warehouse.png", CARD_INVENTORY_UPDATE, KeyEvent.VK_H, ActionEvent.CTRL_MASK);
        addItem(mnuUpdate, "Tiếp nhận lịch hẹn", "/img/appointment.png", CARD_APPOINTMENT_UPDATE, KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        if (KiemSoatTruyCap.isAdministrator()) addItem(mnuUpdate, "Cập nhật nhân sự & Tài khoản", "/img/user_update.png", CARD_PERSONNEL_UPDATE, KeyEvent.VK_U, ActionEvent.CTRL_MASK);

        JMenu mnuProcess = createStyledMenu("4. Xử lý", "/img/xuly.png");
        addItem(mnuProcess, "Đặt lịch hẹn", "/img/calendar.png", CARD_BOOKING, KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        addItem(mnuProcess, "Khám bệnh & Chỉ định", "/img/medical.png", CARD_EXAMINATION, KeyEvent.VK_F1, 0);
        addItem(mnuProcess, "Kê đơn thuốc điện tử", "/img/service.png", CARD_PRESCRIPTION, KeyEvent.VK_F3, 0);
        addItem(mnuProcess, "Bán hàng & Hóa đơn (POS)", "/img/billing.png", CARD_SALES_INVOICE, KeyEvent.VK_F4, 0);
        addItem(mnuProcess, "Phân ca lịch làm việc", "/img/shift.png", CARD_WORK_SHIFT, KeyEvent.VK_F5, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        JMenu mnuSearch = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(mnuSearch, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(mnuSearch, "Tra cứu lịch hẹn", "/img/timkiem.png", CARD_SEARCH_APPOINTMENT, KeyEvent.VK_F7, 0);
        addItem(mnuSearch, "Tra cứu khuyến mãi", "/img/timkiem.png", CARD_SEARCH_PROMO, KeyEvent.VK_F8, 0);
        addItem(mnuSearch, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(mnuSearch, "Tra cứu tồn kho, thuốc", "/img/timkiem.png", CARD_SEARCH_INVENTORY, KeyEvent.VK_F10, 0);
        addItem(mnuSearch, "Tra cứu hóa đơn, phiếu thu", "/img/timkiem.png", CARD_SEARCH_INVOICE, KeyEvent.VK_F11, 0);

        JMenu mnuReport = createStyledMenu("6. Báo biểu", "/img/baobieu.png");
        addItem(mnuReport, "Doanh thu, chi phí & công nợ", "/img/report_revenue.png", CARD_REPORT_REVENUE, KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuReport, "Thống kê dịch vụ & hiệu suất nha sĩ", "/img/report_service.png", CARD_REPORT_SERVICE, KeyEvent.VK_E, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuReport, "Thống kê xuất nhập tồn kho", "/img/report_warehouse.png", CARD_REPORT_WAREHOUSE, KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        menuBar.add(mnuSystem);
        menuBar.add(mnuCategory);
        menuBar.add(mnuUpdate);
        menuBar.add(mnuProcess);
        menuBar.add(mnuSearch);
        menuBar.add(mnuReport);
        return menuBar;
    }

    // 2. Menu Nha sĩ
    private JMenuBar createDentistMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY_BG);
        menuBar.setOpaque(false);

        JMenu mnuSystem = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(mnuSystem, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(mnuSystem, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(mnuSystem, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        mnuSystem.addSeparator();
        JMenuItem itemLogout = createStyledItem("Đăng xuất", "/img/logout.png", 0, 0);
        itemLogout.addActionListener(e -> logout());
        mnuSystem.add(itemLogout);

        JMenu mnuCategory = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        addItem(mnuCategory, "Dịch vụ nha khoa", "/img/service.png", CARD_SERVICES, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Sơ đồ răng chuẩn", "/img/dental.png", CARD_DENTAL_CHART, KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addItem(mnuCategory, "Danh mục thuốc chuẩn", "/img/medicine.png", CARD_MEDICINES, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addReferenceCategoryItems(mnuCategory);
        
        JMenu mnuUpdate = createStyledMenu("3. Cập nhật", "/img/capnhat.png");

        JMenu mnuProcess = createStyledMenu("4. Xử lý", "/img/xuly.png");
        addItem(mnuProcess, "Khám bệnh & Chỉ định", "/img/medical.png", CARD_EXAMINATION, KeyEvent.VK_F1, 0);
        addItem(mnuProcess, "Kê đơn thuốc điện tử", "/img/service.png", CARD_PRESCRIPTION, KeyEvent.VK_F3, 0);

        JMenu mnuSearch = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(mnuSearch, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(mnuSearch, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(mnuSearch, "Tra cứu tồn kho, thuốc", "/img/timkiem.png", CARD_SEARCH_INVENTORY, KeyEvent.VK_F10, 0);

        JMenu mnuReport = createStyledMenu("6. Báo biểu", "/img/baobCARD_SEARCH_APPOINTMENTieu.png");

        menuBar.add(mnuSystem);
        menuBar.add(mnuCategory);
        menuBar.add(mnuUpdate);
        menuBar.add(mnuProcess);
        menuBar.add(mnuSearch);
        menuBar.add(mnuReport);
        return menuBar;
    }

    // 3. Menu Y tá
    private JMenuBar createNurseMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY_BG);
        menuBar.setOpaque(false);

        JMenu mnuSystem = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(mnuSystem, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(mnuSystem, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(mnuSystem, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        mnuSystem.addSeparator();
        JMenuItem itemLogout = createStyledItem("Đăng xuất", "/img/logout.png", 0, 0);
        itemLogout.addActionListener(e -> logout());
        mnuSystem.add(itemLogout);

        JMenu mnuCategory = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        addItem(mnuCategory, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Danh mục thuốc chuẩn", "/img/medicine.png", CARD_MEDICINES, KeyEvent.VK_M, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        addReferenceCategoryItems(mnuCategory);

        JMenu mnuUpdate = createStyledMenu("3. Cập nhật", "/img/capnhat.png");
        addItem(mnuUpdate, "Cập nhật kho vật tư, thiết bị", "/img/warehouse.png", CARD_INVENTORY_UPDATE, KeyEvent.VK_H, ActionEvent.CTRL_MASK);

        JMenu mnuProcess = createStyledMenu("4. Xử lý", "/img/xuly.png");

        JMenu mnuSearch = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(mnuSearch, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(mnuSearch, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(mnuSearch, "Tra cứu tồn kho, thuốc", "/img/timkiem.png", CARD_SEARCH_INVENTORY, KeyEvent.VK_F10, 0);

        JMenu mnuReport = createStyledMenu("6. Báo biểu", "/img/baobieu.png");

        menuBar.add(mnuSystem);
        menuBar.add(mnuCategory);
        menuBar.add(mnuUpdate);
        menuBar.add(mnuProcess);
        menuBar.add(mnuSearch);
        menuBar.add(mnuReport);
        return menuBar;
    }

    // 4. Menu Lễ tân 
    private JMenuBar createReceptionistMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY_BG);
        menuBar.setOpaque(false);

        JMenu mnuSystem = createStyledMenu("1. Hệ thống", "/img/hethong.png");
        addItem(mnuSystem, "Bảng điều khiển", "/img/control.png", CARD_DASHBOARD, KeyEvent.VK_F5, 0);
        addItem(mnuSystem, "Đổi mật khẩu", "/img/lock.png", CARD_CHANGE_PASS, KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        addItem(mnuSystem, "Trợ giúp", "/img/help.png", CARD_HELP, KeyEvent.VK_F1, ActionEvent.SHIFT_MASK);
        mnuSystem.addSeparator();
        JMenuItem itemLogout = createStyledItem("Đăng xuất", "/img/logout.png", 0, 0);
        itemLogout.addActionListener(e -> logout());
        mnuSystem.add(itemLogout);

        JMenu mnuCategory = createStyledMenu("2. Danh mục", "/img/danhmuc.png");
        addItem(mnuCategory, "Dịch vụ nha khoa", "/img/service.png", CARD_SERVICES, KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Chương trình khuyến mãi", "/img/promo.png", CARD_PROMOTIONS, KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Khách hàng / Bệnh nhân", "/img/customer.png", CARD_CUSTOMERS, KeyEvent.VK_K, ActionEvent.CTRL_MASK);
        addItem(mnuCategory, "Hồ sơ bệnh án", "/img/medical.png", CARD_MEDICAL_RECORDS, KeyEvent.VK_T, ActionEvent.CTRL_MASK);


        JMenu mnuUpdate = createStyledMenu("3. Cập nhật", "/img/capnhat.png");
        addItem(mnuUpdate, "Tiếp nhận lịch hẹn", "/img/appointment.png", CARD_APPOINTMENT_UPDATE, KeyEvent.VK_N, ActionEvent.CTRL_MASK);

        JMenu mnuProcess = createStyledMenu("4. Xử lý", "/img/xuly.png");
        addItem(mnuProcess, "Đặt lịch hẹn", "/img/calendar.png", CARD_BOOKING, KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        addItem(mnuProcess, "Bán hàng & Hóa đơn (POS)", "/img/billing.png", CARD_SALES_INVOICE, KeyEvent.VK_F4, 0);


        JMenu mnuSearch = createStyledMenu("5. Tìm kiếm", "/img/timkiem.png");
        addItem(mnuSearch, "Tra cứu khách hàng, điểm tích lũy", "/img/timkiem.png", CARD_SEARCH_CUSTOMER, KeyEvent.VK_F6, 0);
        addItem(mnuSearch, "Tra cứu lịch hẹn", "/img/timkiem.png", CARD_SEARCH_APPOINTMENT, KeyEvent.VK_F7, 0);
        addItem(mnuSearch, "Tra cứu khuyến mãi", "/img/timkiem.png", CARD_SEARCH_PROMO, KeyEvent.VK_F8, 0);
        addItem(mnuSearch, "Tra cứu lịch làm việc", "/img/timkiem.png", CARD_SEARCH_SHIFT, KeyEvent.VK_F9, 0);
        addItem(mnuSearch, "Tra cứu hóa đơn, phiếu thu", "/img/timkiem.png", CARD_SEARCH_INVOICE, KeyEvent.VK_F11, 0);

        JMenu mnuReport = createStyledMenu("6. Báo biểu", "/img/baobieu.png");
        addItem(mnuReport, "Doanh thu, chi phí & công nợ", "/img/report_revenue.png", CARD_REPORT_REVENUE, KeyEvent.VK_R, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);

        menuBar.add(mnuSystem);
        menuBar.add(mnuCategory);
        menuBar.add(mnuUpdate);
        menuBar.add(mnuProcess);
        menuBar.add(mnuSearch);
        menuBar.add(mnuReport);
        return menuBar;
    }
    // =========================================================================

    private void adaptHeader(int width, JMenuBar menuBar, JPanel headerBar, JPanel centerContainer,
                             JPanel logoPanel, JLabel logoIcon, JLabel logoText, JLabel subLogo,
                             JButton notification, JLabel userAvatar, JLabel userName) {
        double scale = Math.max(0.70, Math.min(1.15, width / 1400.0));
        int fontSize = Math.max(11, (int) Math.round(16 * scale));
        int menuIconSize = Math.max(15, (int) Math.round(22 * scale));

        centerContainer.setLayout(new FlowLayout(FlowLayout.LEFT,
                Math.max(2, (int) Math.round(5 * scale)), 0));
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT,
                Math.max(4, (int) Math.round(8 * scale)), 0));
        for (Component component : menuBar.getComponents()) {
            if (!(component instanceof JMenu)) continue;
            JMenu menu = (JMenu) component;
            menu.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            int verticalInset = Math.max(4, (int) Math.round(9 * scale));
            int horizontalInset = Math.max(2, (int) Math.round(4 * scale));
            menu.setBorder(BorderFactory.createEmptyBorder(verticalInset, horizontalInset,
                    verticalInset, horizontalInset));
            Object iconPath = menu.getClientProperty("sourceIconPath");
            if (iconPath instanceof String) menu.setIcon(loadIcon((String) iconPath, menuIconSize, menuIconSize));
        }
        logoIcon.setIcon(loadIcon("/img/logo1.png", Math.max(48, (int) Math.round(68 * scale)),
                Math.max(48, (int) Math.round(68 * scale))));
        logoText.setFont(new Font("Segoe UI", Font.BOLD,
                Math.max(17, (int) Math.round(25 * scale))));
        subLogo.setFont(new Font("Segoe UI", Font.PLAIN,
                Math.max(9, (int) Math.round(11 * scale))));
        notification.setIcon(loadIcon("/img/bell.png", Math.max(30, (int) Math.round(42 * scale)),
                Math.max(30, (int) Math.round(42 * scale))));
        userAvatar.setIcon(loadIcon("/img/staff.png", Math.max(19, (int) Math.round(25 * scale)),
                Math.max(19, (int) Math.round(25 * scale))));
        userName.setFont(new Font("Segoe UI", Font.BOLD,
                Math.max(9, (int) Math.round(11 * scale))));
        int verticalInset = Math.max(8, (int) Math.round(20 * scale));
        int horizontalInset = Math.max(10, (int) Math.round(25 * scale));
        headerBar.setBorder(new EmptyBorder(verticalInset, horizontalInset, verticalInset, horizontalInset));
        headerBar.setPreferredSize(new Dimension(0, (int) Math.round(105 * scale)));
        headerBar.revalidate();
        headerBar.repaint();
    }

    private JMenu createStyledMenu(String text, String iconPath) {
        JMenu menu = new JMenu(text);
        menu.putClientProperty("sourceIconPath", iconPath);
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
        if (!KiemSoatTruyCap.canAccessCard(cardName)) return;
        JMenuItem item = createStyledItem(text, iconPath, keyCode, modifiers);
        item.addActionListener(e -> showView(cardName));
        menu.add(item);
    }
}
