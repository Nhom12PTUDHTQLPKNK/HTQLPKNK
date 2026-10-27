package views;

import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    // ================= MÀU SẮC GIAO DIỆN =================
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color PINK_ACCENT = new Color(239, 63, 131);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color MUTED = new Color(91, 112, 145);
    private static final Color GREEN = new Color(40, 167, 69);
    private static final Locale VI = new Locale("vi", "VN");

    // ================= CÁC THẺ THỐNG KÊ =================
    private JLabel dayLabelRef;
    private JLabel dateLabelRef;
    private JLabel timeLabelRef;

    private JLabel totalPatientsValue;
    private JLabel appointmentsValue;
    private JLabel revenueValue;
    private JLabel alertsValue;

    // ================= DỮ LIỆU BIỂU ĐỒ =================
    private final List<String> chartDays = new ArrayList<>();
    private final List<Double> chartRevenue = new ArrayList<>();

    private final List<String> serviceNames = new ArrayList<>();
    private final List<Double> serviceRevenue = new ArrayList<>();

    // ================= BẢNG BỆNH NHÂN =================
    private DefaultTableModel patientTableModel;

    // ================= THÔNG BÁO =================
    private JPanel notificationContent;

    private SwingWorker<Void, Void> dashboardWorker;

    private final NumberFormat moneyFormat =
            NumberFormat.getNumberInstance(VI);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_CANVAS);

        JScrollPane mainScroll = new JScrollPane(createContent());
        mainScroll.setBorder(null);
        mainScroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getViewport().setBackground(BG_CANVAS);

        add(mainScroll, BorderLayout.CENTER);

        initClockTimer();
    }

    // =====================================================
    // TẠO GIAO DIỆN CHÍNH
    // =====================================================
    private JComponent createContent() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(BG_CANVAS);
        page.setBorder(new EmptyBorder(20, 30, 25, 30));

        // ================= HERO BANNER =================
        JPanel hero = new JPanel(new GridBagLayout());
        hero.setPreferredSize(new Dimension(1340, 300));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        hero.setBackground(Color.WHITE);
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT),
                new EmptyBorder(16, 20, 16, 20)));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel intro = new JLabel("Chào mừng trở lại với");
        intro.setFont(new Font("Segoe UI", Font.BOLD, 28));
        intro.setForeground(TEXT_DARK);

        JLabel name = new JLabel(
                "<html><b><font color='#007EFF'>SMILE</font>"
                + "<font color='#EF3F83'>DENTAL</font></b></html>");
        name.setFont(new Font("Segoe UI", Font.BOLD, 40));

        JLabel tagline = new JLabel(
                "Hệ thống quản lý phòng khám chuyên nghiệp");
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tagline.setForeground(new Color(60, 82, 119));

        JLabel desc = new JLabel(
                "<html>Quản lý lịch hẹn, doanh thu, hồ sơ bệnh án "
                + "và vận hành phòng khám một cách hiệu quả, an toàn.</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        desc.setForeground(MUTED);

        copy.add(Box.createVerticalGlue());
        copy.add(intro);
        copy.add(Box.createVerticalStrut(2));
        copy.add(name);
        copy.add(Box.createVerticalStrut(2));
        copy.add(tagline);
        copy.add(Box.createVerticalStrut(12));
        copy.add(desc);
        copy.add(Box.createVerticalGlue());

        // ================= THẺ NGÀY GIỜ =================
        JPanel calendarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(255, 220, 238),
                        getWidth(), getHeight(),
                        new Color(190, 224, 255));

                g2.setPaint(gradient);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 35, 35);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(
                        1, 1, getWidth() - 3,
                        getHeight() - 3, 35, 35);

                g2.dispose();
            }
        };

        calendarPanel.setOpaque(false);
        calendarPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        calendarPanel.setPreferredSize(new Dimension(560, 260));

        JPanel rightContent = new JPanel(new GridBagLayout());
        rightContent.setOpaque(false);

        JPanel welcomeSubPanel = new JPanel();
        welcomeSubPanel.setOpaque(false);
        welcomeSubPanel.setLayout(
                new BoxLayout(welcomeSubPanel, BoxLayout.Y_AXIS));

        JLabel greetingLabel = new JLabel(
                "<html><div style='white-space:nowrap;'>Hôm nay,"
                + "<br><font color='#EF3F83'>Quản trị viên! 👋</font>"
                + "</div></html>");
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        greetingLabel.setForeground(TEXT_DARK);

        JLabel descLabel = new JLabel(
                "<html>Hệ thống đang hoạt động ổn định"
                + "<br>với cơ sở dữ liệu SQL Server.</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(MUTED);

        welcomeSubPanel.add(greetingLabel);
        welcomeSubPanel.add(Box.createVerticalStrut(8));
        welcomeSubPanel.add(descLabel);

        JPanel dateTimePanel = new JPanel();
        dateTimePanel.setOpaque(false);
        dateTimePanel.setLayout(
                new BoxLayout(dateTimePanel, BoxLayout.Y_AXIS));

        dayLabelRef = new JLabel();
        dayLabelRef.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dayLabelRef.setForeground(MUTED);

        dateLabelRef = new JLabel();
        dateLabelRef.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dateLabelRef.setForeground(TEXT_DARK);

        timeLabelRef = new JLabel();
        timeLabelRef.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabelRef.setForeground(TEXT_DARK);

        JPanel dateCard = createCardWidget(
                "\u25A6", dayLabelRef, dateLabelRef);

        JPanel timeCard = createTimeCardWidget(
                "\u25F7", timeLabelRef);

        dateTimePanel.add(dateCard);
        dateTimePanel.add(Box.createVerticalStrut(6));
        dateTimePanel.add(timeCard);

        GridBagConstraints gcLeft = new GridBagConstraints();
        gcLeft.gridx = 0;
        gcLeft.weightx = 0.55;
        gcLeft.fill = GridBagConstraints.BOTH;
        rightContent.add(welcomeSubPanel, gcLeft);

        GridBagConstraints gcRight = new GridBagConstraints();
        gcRight.gridx = 1;
        gcRight.weightx = 0.45;
        gcRight.fill = GridBagConstraints.BOTH;
        rightContent.add(dateTimePanel, gcRight);

        calendarPanel.add(rightContent, BorderLayout.CENTER);

        c.gridx = 0;
        c.weightx = 0.52;
        hero.add(copy, c);

        c.gridx = 1;
        c.weightx = 0.48;
        hero.add(calendarPanel, c);

        page.add(hero);
        page.add(Box.createVerticalStrut(20));

        // ================= THẺ THỐNG KÊ =================
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 15, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        statsRow.add(createStatCard(
                "Tổng bệnh nhân", "Đang tải...",
                "Tổng số khách hàng", BLUE,
                new Color(232, 242, 255)));

        statsRow.add(createStatCard(
                "Lịch hẹn hôm nay", "Đang tải...",
                "Lịch hẹn trong ngày", PINK_ACCENT,
                new Color(254, 242, 246)));

        statsRow.add(createStatCard(
                "Doanh thu tháng này", "Đang tải...",
                "Tiền đã thanh toán", GREEN,
                new Color(238, 247, 238)));

        statsRow.add(createStatCard(
                "Cần chú ý", "Đang tải...",
                "Lịch hẹn chờ xác nhận", new Color(220, 120, 0),
                new Color(255, 248, 235)));

        page.add(statsRow);
        page.add(Box.createVerticalStrut(20));

        // ================= BIỂU ĐỒ VÀ THÔNG BÁO =================
        JPanel chartsAndNotiRow = new JPanel(new BorderLayout(15, 0));
        chartsAndNotiRow.setOpaque(false);
        chartsAndNotiRow.setPreferredSize(new Dimension(0, 280));
        chartsAndNotiRow.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 280));

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        chartsRow.setOpaque(false);

        chartsRow.add(new RevenueBarChartPanel());
        chartsRow.add(new RevenueDonutChartPanel());

        JPanel notiBox = createNotiBoxStyled("Thông báo nhanh");
        notiBox.setPreferredSize(new Dimension(310, 0));

        chartsAndNotiRow.add(chartsRow, BorderLayout.CENTER);
        chartsAndNotiRow.add(notiBox, BorderLayout.EAST);

        page.add(chartsAndNotiRow);
        page.add(Box.createVerticalStrut(20));

        // ================= BẢNG BỆNH NHÂN =================
        page.add(createRecentPatientsPanel());

        return page;
    }

    // =====================================================
    // THẺ THỐNG KÊ
    // =====================================================
    private JPanel createStatCard(
            String title, String value, String sub,
            Color textColor, Color bgColor) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblVal.setForeground(textColor);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(MUTED);

        switch (title) {
            case "Tổng bệnh nhân":
                totalPatientsValue = lblVal;
                break;
            case "Lịch hẹn hôm nay":
                appointmentsValue = lblVal;
                break;
            case "Doanh thu tháng này":
                revenueValue = lblVal;
                break;
            case "Cần chú ý":
                alertsValue = lblVal;
                break;
            default:
                break;
        }

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblVal);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);

        return card;
    }

    // =====================================================
    // THẺ NGÀY
    // =====================================================
    private JPanel createCardWidget(
            String symbol, JLabel lblDay, JLabel lblDate) {

        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setMaximumSize(new Dimension(240, 56));
        card.setPreferredSize(new Dimension(240, 56));
        card.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel iconLabel = new JLabel(symbol);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        iconLabel.setForeground(PINK_ACCENT);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        lblDay.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblDay);
        textPanel.add(lblDate);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // =====================================================
    // THẺ GIỜ
    // =====================================================
    private JPanel createTimeCardWidget(
            String symbol, JLabel lblTime) {

        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        card.setOpaque(false);
        card.setMaximumSize(new Dimension(240, 50));
        card.setPreferredSize(new Dimension(240, 50));
        card.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel iconLabel = new JLabel(symbol);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        iconLabel.setForeground(PINK_ACCENT);

        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(lblTime, BorderLayout.CENTER);

        return card;
    }

    // =====================================================
    // ĐỒNG HỒ THỜI GIAN THỰC
    // =====================================================
    private void initClockTimer() {
        DateTimeFormatter dayFormat =
                DateTimeFormatter.ofPattern("EEEE", VI);

        DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        DateTimeFormatter timeFormat =
                DateTimeFormatter.ofPattern("HH:mm:ss");

        Runnable updateDateTime = () -> {
            LocalDateTime now = LocalDateTime.now();

            String day = dayFormat.format(now);
            day = day.substring(0, 1).toUpperCase(VI)
                    + day.substring(1);

            if (dayLabelRef != null) {
                dayLabelRef.setText(day);
            }

            if (dateLabelRef != null) {
                dateLabelRef.setText(dateFormat.format(now));
            }

            if (timeLabelRef != null) {
                timeLabelRef.setText(timeFormat.format(now));
            }
        };

        updateDateTime.run();

        Timer clockTimer = new Timer(
                1000, e -> updateDateTime.run());

        clockTimer.start();
    }

    // =====================================================
    // ĐỊNH DẠNG TIỀN
    // =====================================================
    private String formatMoney(double amount) {
        return moneyFormat.format(amount) + " đ";
    }

    // =====================================================
    // TẢI TOÀN BỘ DỮ LIỆU TỪ SQL SERVER
    // =====================================================
    public void refreshDashboardData() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshDashboardData);
            return;
        }

        if (dashboardWorker != null && !dashboardWorker.isDone()) {
            dashboardWorker.cancel(true);
        }

        totalPatientsValue.setText("Đang tải...");
        appointmentsValue.setText("Đang tải...");
        revenueValue.setText("Đang tải...");
        alertsValue.setText("Đang tải...");
        chartDays.clear();
        chartRevenue.clear();
        serviceNames.clear();
        serviceRevenue.clear();
        refreshPatientTable(new ArrayList<>());
        notificationContent.removeAll();
        JLabel loading = new JLabel("Đang tải thông báo...");
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loading.setForeground(MUTED);
        notificationContent.add(loading);
        notificationContent.revalidate();
        notificationContent.repaint();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            private int totalPatients;
            private int appointmentsToday;
            private int alerts;
            private double monthlyRevenue;

            private final List<Object[]> patients = new ArrayList<>();
            private final List<Object[]> notifications = new ArrayList<>();

            private final List<String> days = new ArrayList<>();
            private final List<Double> revenues = new ArrayList<>();

            private final List<String> services = new ArrayList<>();
            private final List<Double> serviceValues = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {

                try (Connection conn = DBConnection.getConnection()) {

                    // 1. THỐNG KÊ TỔNG QUAN
                    String statsSql =
                            "SELECT "
                            + "(SELECT COUNT(*) FROM KhachHang) AS TongBenhNhan, "
                            + "(SELECT COUNT(*) FROM LichHen "
                            + " WHERE ngayHen = CAST(GETDATE() AS DATE)) "
                            + " AS LichHenHomNay, "
                            + "(SELECT COALESCE(SUM(daThanhToan), 0) "
                            + " FROM PhieuThanhToan "
                            + " WHERE ngayThanhToan >= "
                            + " DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) "
                            + " AND ngayThanhToan < "
                            + " DATEADD(DAY, 1, CAST(GETDATE() AS DATE))) "
                            + " AS DoanhThuThang, "
                            + "(SELECT COUNT(*) FROM LichHen "
                            + " WHERE trangThai = N'Chờ xác nhận' "
                            + " AND ngayHen >= CAST(GETDATE() AS DATE)) "
                            + " AS CanChuY";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(statsSql);
                         ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {
                            totalPatients = rs.getInt("TongBenhNhan");
                            appointmentsToday = rs.getInt("LichHenHomNay");
                            monthlyRevenue = rs.getDouble("DoanhThuThang");
                            alerts = rs.getInt("CanChuY");
                        }
                    }

                    // 2. BIỂU ĐỒ DOANH THU 7 NGÀY GẦN NHẤT
                    String revenueSql =
                            "WITH Ngay AS ( "
                            + " SELECT CAST(GETDATE() AS DATE) AS NgayDoanhThu "
                            + " UNION ALL "
                            + " SELECT DATEADD(DAY, -1, NgayDoanhThu) FROM Ngay "
                            + " WHERE NgayDoanhThu > DATEADD(DAY, -6, "
                            + " CAST(GETDATE() AS DATE)) "
                            + ") "
                            + "SELECT n.NgayDoanhThu, "
                            + "COALESCE(SUM(p.daThanhToan), 0) AS DoanhThu "
                            + "FROM Ngay n "
                            + "LEFT JOIN PhieuThanhToan p "
                            + "ON p.ngayThanhToan = n.NgayDoanhThu "
                            + "GROUP BY n.NgayDoanhThu "
                            + "ORDER BY n.NgayDoanhThu "
                            + "OPTION (MAXRECURSION 7)";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(revenueSql);
                         ResultSet rs = ps.executeQuery()) {

                        DateTimeFormatter fmt =
                                DateTimeFormatter.ofPattern("dd/MM");

                        while (rs.next()) {
                            days.add(rs.getDate("NgayDoanhThu")
                                    .toLocalDate().format(fmt));

                            revenues.add(
                                    rs.getDouble("DoanhThu"));
                        }
                    }

                    // 3. CƠ CẤU DOANH THU THEO DỊCH VỤ
                    String serviceSql =
                            "SELECT TOP 5 "
                            + "dv.tenDichVu, "
                            + "SUM(ct.soLuong * ct.donGia) AS DoanhThu "
                            + "FROM ChiTietHoaDon ct "
                            + "INNER JOIN DichVu dv "
                            + "ON ct.maDichVu = dv.maDichVu "
                            + "INNER JOIN HoaDon hd "
                            + "ON ct.maHoaDon = hd.maHoaDon "
                            + "WHERE hd.ngayLap >= "
                            + "DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) "
                            + "AND hd.ngayLap < "
                            + "DATEADD(MONTH, 1, "
                            + "DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) "
                            + "GROUP BY dv.tenDichVu "
                            + "ORDER BY SUM(ct.soLuong * ct.donGia) DESC";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(serviceSql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                            services.add(rs.getString("tenDichVu"));
                            serviceValues.add(
                                    rs.getDouble("DoanhThu"));
                        }
                    }

                    // 4. BỆNH NHÂN GẦN ĐÂY
                    String patientSql =
                            "SELECT TOP 10 "
                            + "ba.maBenhAn, "
                            + "kh.tenKH, "
                            + "kh.sdt, "
                            + "COALESCE(dv.tenDichVu, ba.chuanDoan, N'Chưa cập nhật') "
                            + "AS DichVuGanNhat, "
                            + "ba.ngayKham, "
                            + "COALESCE(ba.trangThai, N'Chưa cập nhật') "
                            + "AS TrangThai "
                            + "FROM HoSoBenhAn ba "
                            + "INNER JOIN KhachHang kh "
                            + "ON ba.maKhachHang = kh.maKhachHang "
                            + "LEFT JOIN KeHoachDieuTri khdt "
                            + "ON khdt.maBenhAn = ba.maBenhAn "
                            + "LEFT JOIN DichVu dv "
                            + "ON dv.maDichVu = khdt.maDichVu "
                            + "WHERE ba.ngayKham IS NOT NULL "
                            + "ORDER BY ba.ngayKham DESC, ba.maBenhAn DESC";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(patientSql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                            Date date = rs.getDate("ngayKham");

                            String dateText = date == null
                                    ? ""
                                    : date.toLocalDate().format(
                                            DateTimeFormatter.ofPattern(
                                                    "dd/MM/yyyy"));

                            patients.add(new Object[]{
                                    rs.getString("maBenhAn"),
                                    rs.getString("tenKH"),
                                    rs.getString("sdt"),
                                    rs.getString("DichVuGanNhat"),
                                    dateText,
                                    rs.getString("TrangThai")
                            });
                        }
                    }

                    // 5. THÔNG BÁO LỊCH HẸN MỚI
                    String appointmentNotiSql =
                            "SELECT TOP 5 "
                            + "lh.maLichHen, kh.tenKH, "
                            + "lh.ngayHen, lh.trangThai "
                            + "FROM LichHen lh "
                            + "LEFT JOIN KhachHang kh "
                            + "ON lh.maKhachHang = kh.maKhachHang "
                            + "ORDER BY lh.ngayHen DESC, lh.maLichHen DESC";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(appointmentNotiSql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                            notifications.add(new Object[]{
                                    "📅",
                                    "Lịch hẹn: "
                                            + safeText(rs.getString("tenKH")),
                                    "Ngày: "
                                            + safeText(rs.getString("ngayHen")),
                                    safeText(rs.getString("trangThai")),
                                    BLUE,
                                    new Color(232, 242, 255)
                            });
                        }
                    }

                    // 6. THÔNG BÁO THANH TOÁN
                    String paymentNotiSql =
                            "SELECT TOP 3 "
                            + "pt.maPhieu, kh.tenKH, "
                            + "pt.daThanhToan, pt.ngayThanhToan "
                            + "FROM PhieuThanhToan pt "
                            + "LEFT JOIN KhachHang kh "
                            + "ON pt.maKhachHang = kh.maKhachHang "
                            + "ORDER BY pt.ngayThanhToan DESC, pt.maPhieu DESC";

                    try (PreparedStatement ps =
                                 conn.prepareStatement(paymentNotiSql);
                         ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {
                            notifications.add(new Object[]{
                                    "💳",
                                    "Thanh toán",
                                    safeText(rs.getString("tenKH"))
                                            + " - "
                                            + formatMoney(
                                                    rs.getDouble("daThanhToan")),
                                    safeText(rs.getString("ngayThanhToan")),
                                    GREEN,
                                    new Color(238, 247, 238)
                            });
                        }
                    }
                }

                return null;
            }

            @Override
            protected void done() {
                if (DashboardPanel.this.dashboardWorker != this || isCancelled()) {
                    return;
                }
                try {
                    get();

                    totalPatientsValue.setText(
                            String.valueOf(totalPatients));

                    appointmentsValue.setText(
                            appointmentsToday + " lịch");

                    revenueValue.setText(
                            formatMoney(monthlyRevenue));

                    alertsValue.setText(
                            alerts + " cảnh báo");

                    chartDays.clear();
                    chartDays.addAll(days);

                    chartRevenue.clear();
                    chartRevenue.addAll(revenues);

                    serviceNames.clear();
                    serviceNames.addAll(services);

                    serviceRevenue.clear();
                    serviceRevenue.addAll(serviceValues);

                    refreshPatientTable(patients);
                    refreshNotifications(notifications);

                    repaint();

                } catch (Exception e) {
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(
                            DashboardPanel.this,
                            "Không thể tải dữ liệu từ SQL Server.\n"
                            + "Vui lòng kiểm tra kết nối và dữ liệu.\n\n"
                            + e.getMessage(),
                            "Lỗi tải dữ liệu",
                            JOptionPane.ERROR_MESSAGE);

                    totalPatientsValue.setText("--");
                    appointmentsValue.setText("--");
                    revenueValue.setText("--");
                    alertsValue.setText("--");
                }
            }
        };

        dashboardWorker = worker;
        worker.execute();
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty()
                ? "Chưa cập nhật"
                : value;
    }

    // =====================================================
    // CẬP NHẬT BẢNG BỆNH NHÂN
    // =====================================================
    private void refreshPatientTable(List<Object[]> data) {
        if (patientTableModel == null) {
            return;
        }

        patientTableModel.setRowCount(0);

        for (Object[] row : data) {
            patientTableModel.addRow(row);
        }
    }

    // =====================================================
    // TẠO HỘP THÔNG BÁO
    // =====================================================
    private JPanel createNotiBoxStyled(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        panel.add(lbl, BorderLayout.NORTH);

        notificationContent = new JPanel();
        notificationContent.setLayout(
                new BoxLayout(notificationContent, BoxLayout.Y_AXIS));
        notificationContent.setBackground(CARD_BG);

        JLabel loading = new JLabel("Đang tải thông báo...");
        loading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loading.setForeground(MUTED);
        notificationContent.add(loading);

        JScrollPane scrollPane = new JScrollPane(notificationContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshNotifications(List<Object[]> notifications) {
        if (notificationContent == null) {
            return;
        }

        notificationContent.removeAll();

        if (notifications.isEmpty()) {
            JLabel empty = new JLabel("Chưa có thông báo.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            empty.setForeground(MUTED);
            notificationContent.add(empty);
        } else {
            int count = 0;

            for (Object[] n : notifications) {
                if (count > 0) {
                    notificationContent.add(
                            Box.createVerticalStrut(8));
                }

                notificationContent.add(createModernNotiItem(
                        (String) n[0],
                        (String) n[1],
                        (String) n[2],
                        (String) n[3],
                        (Color) n[4],
                        (Color) n[5]));

                count++;
            }
        }

        notificationContent.revalidate();
        notificationContent.repaint();
    }

    private JPanel createModernNotiItem(
            String iconSymbol,
            String title,
            String desc,
            String timeAgo,
            Color iconColor,
            Color bgItemColor) {

        JPanel item = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgItemColor);
                g2.fillRoundRect(
                        0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 8, 8, 8));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        lblIcon.setForeground(iconColor);
        lblIcon.setPreferredSize(new Dimension(25, 28));

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(
                new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblDesc = new JLabel(
                "<html><body style='width:145px'>"
                + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDesc.setForeground(MUTED);

        textGroup.add(lblTitle);
        textGroup.add(Box.createVerticalStrut(3));
        textGroup.add(lblDesc);

        JLabel lblTime = new JLabel(timeAgo);
        lblTime.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        lblTime.setForeground(MUTED);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(textGroup, BorderLayout.CENTER);
        item.add(lblTime, BorderLayout.SOUTH);

        return item;
    }

    // =====================================================
    // BẢNG BỆNH NHÂN GẦN ĐÂY
    // =====================================================
    private JPanel createRecentPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        panel.setPreferredSize(new Dimension(1000, 300));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(
                "Danh sách bệnh nhân gần đây");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblViewAll = new JLabel("Dữ liệu từ hồ sơ bệnh án");
        lblViewAll.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblViewAll.setForeground(BLUE);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblViewAll, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        String[] columns = {
                "Mã bệnh án",
                "Tên bệnh nhân",
                "SĐT",
                "Dịch vụ gần nhất",
                "Ngày khám",
                "Trạng thái"
        };

        patientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(patientTableModel);
        PdfTableExport.install(table, "Bệnh nhân gần đây");
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(240, 244, 248));
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(
                new Color(245, 248, 253));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 32));

        // CĂN GIỮA NGÀY KHÁM VÀ TRẠNG THÁI
        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();

        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0)
                .setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(4)
                .setCellRenderer(centerRenderer);

        // MÀU TRẠNG THÁI BỆNH ÁN
        table.getColumnModel().getColumn(5)
                .setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {

                        JLabel label = (JLabel) super
                                .getTableCellRendererComponent(
                                        table, value, isSelected,
                                        hasFocus, row, column);

                        label.setHorizontalAlignment(
                                SwingConstants.CENTER);
                        label.setOpaque(true);

                        String status = value == null
                                ? ""
                                : value.toString();

                        if (status.contains("Hoàn thành")) {
                            label.setForeground(
                                    new Color(40, 140, 65));
                            label.setBackground(
                                    new Color(225, 245, 230));
                        } else if (status.contains("Đang điều trị")) {
                            label.setForeground(
                                    new Color(0, 102, 204));
                            label.setBackground(
                                    new Color(225, 240, 255));
                        } else {
                            label.setForeground(PINK_ACCENT);
                            label.setBackground(
                                    new Color(255, 235, 240));
                        }

                        return label;
                    }
                });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT));
        tableScroll.getViewport().setBackground(Color.WHITE);

        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    // BIỂU ĐỒ CỘT: DOANH THU 7 NGÀY
    // =====================================================
    private class RevenueBarChartPanel extends JPanel {

        public RevenueBarChartPanel() {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            BORDER_LIGHT, 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(TEXT_DARK);
            g2.drawString(
                    "DOANH THU 7 NGÀY GẦN NHẤT", 12, 22);

            int left = 48;
            int top = 48;
            int bottom = getHeight() - 45;
            int chartHeight = Math.max(30, bottom - top);
            int chartWidth = Math.max(100, getWidth() - left - 15);

            int count = chartDays.size();

            if (count == 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(MUTED);
                g2.drawString(
                        "Chưa có dữ liệu doanh thu.",
                        left, top + 50);
                g2.dispose();
                return;
            }

            double max = 0;

            for (double value : chartRevenue) {
                max = Math.max(max, value);
            }

            if (max <= 0) {
                max = 1;
            }

            // CÁC ĐƯỜNG LƯỚI
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));

            for (int i = 0; i <= 4; i++) {
                int y = bottom - (chartHeight * i / 4);

                g2.setColor(new Color(240, 244, 250));
                g2.drawLine(left, y, left + chartWidth, y);

                g2.setColor(MUTED);
                String label = moneyFormat.format(max * i / 4 / 1000000.0)
                        + "tr";

                g2.drawString(label, 3, y + 4);
            }

            int slot = Math.max(1, chartWidth / count);
            int barWidth = Math.max(8, Math.min(28, slot - 10));

            for (int i = 0; i < count; i++) {
                double value = chartRevenue.get(i);

                int height = (int) (value / max * chartHeight);
                int x = left + i * slot + (slot - barWidth) / 2;
                int y = bottom - height;

                Color topColor = i == count - 1
                        ? PINK_ACCENT : BLUE;

                Color bottomColor = i == count - 1
                        ? new Color(255, 160, 195)
                        : new Color(130, 190, 255);

                g2.setPaint(new GradientPaint(
                        x, y, topColor,
                        x, bottom, bottomColor));

                g2.fill(new RoundRectangle2D.Double(
                        x, y, barWidth, Math.max(2, height),
                        6, 6));

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(MUTED);

                String day = chartDays.get(i);
                int textWidth = g2.getFontMetrics()
                        .stringWidth(day);

                g2.drawString(
                        day,
                        x + (barWidth - textWidth) / 2,
                        bottom + 16);
            }

            g2.dispose();
        }
    }

    // =====================================================
    // BIỂU ĐỒ TRÒN: CƠ CẤU DOANH THU DỊCH VỤ
    // =====================================================
    private class RevenueDonutChartPanel extends JPanel {

        private final Color[] chartColors = {
                BLUE,
                PINK_ACCENT,
                GREEN,
                new Color(111, 66, 193),
                new Color(255, 159, 28)
        };

        public RevenueDonutChartPanel() {
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            BORDER_LIGHT, 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2.setColor(TEXT_DARK);
            g2.drawString(
                    "CƠ CẤU DOANH THU DỊCH VỤ",
                    12, 22);

            int count = serviceNames.size();

            if (count == 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(MUTED);
                g2.drawString(
                        "Chưa có dữ liệu dịch vụ trong tháng.",
                        15, 90);
                g2.dispose();
                return;
            }

            double total = 0;

            for (double value : serviceRevenue) {
                total += value;
            }

            if (total <= 0) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.setColor(MUTED);
                g2.drawString("Doanh thu hiện bằng 0.", 15, 90);
                g2.dispose();
                return;
            }

            int diameter = Math.min(
                    125, Math.max(75, getHeight() - 65));

            int cx = 20;
            int cy = 45;

            double startAngle = 90;

            for (int i = 0; i < count; i++) {
                double value = serviceRevenue.get(i);
                double angle = value / total * 360.0;

                g2.setColor(chartColors[i % chartColors.length]);

                g2.fill(new Arc2D.Double(
                        cx, cy, diameter, diameter,
                        startAngle, -angle, Arc2D.PIE));

                startAngle -= angle;
            }

            // TẠO LỖ TRÒN Ở GIỮA
            int hole = diameter / 2;

            g2.setColor(CARD_BG);
            g2.fillOval(
                    cx + (diameter - hole) / 2,
                    cy + (diameter - hole) / 2,
                    hole, hole);

            int legendX = cx + diameter + 12;
            int legendY = 53;

            int availableWidth = Math.max(
                    80, getWidth() - legendX - 8);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            for (int i = 0; i < count; i++) {
                int y = legendY + i * 34;

                if (y > getHeight() - 25) {
                    break;
                }

                g2.setColor(chartColors[i % chartColors.length]);
                g2.fillRoundRect(legendX, y - 8, 9, 9, 3, 3);

                double percent = serviceRevenue.get(i) / total * 100;

                String service = serviceNames.get(i);

                if (service.length() > 18) {
                    service = service.substring(0, 16) + "...";
                }

                g2.setColor(TEXT_DARK);

                String legend = service + " ("
                        + String.format(VI, "%.1f", percent) + "%)";

                // Giới hạn chiều dài chú thích
                FontMetrics fm = g2.getFontMetrics();

                while (fm.stringWidth(legend) > availableWidth
                        && legend.length() > 5) {
                    legend = legend.substring(0, legend.length() - 4)
                            + "...";
                }

                g2.drawString(legend, legendX + 14, y);
            }

            g2.dispose();
        }
    }
}