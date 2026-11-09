package views;

import controllers.NhatKyKiemToan;
import controllers.XuatBangPDF;
import controllers.DichVuBaoCao;
import controllers.DieuKhienMaQR;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ManHinhXuLyBanHang extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(34, 150, 83);
    private static final Color PINK_ACCENT = new Color(174, 35, 86);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch, txtCustomerName, txtPhone, txtNote;
    private JComboBox<String> cboPaymentMethod;
    private JComboBox<String> cboInvoiceType;
    private JComboBox<MucBenhNhanHopChon> cboChonBenhAn;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;

    private JLabel lblStatTotalInvoices, lblStatTotalRevenue, lblStatMonthInvoices, lblStatPaidInvoices;
    private String maHoaDonMoiNhat;

    public ManHinhXuLyBanHang() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- 1. HEADER DẠNG CARD NGANG ---
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color pinkPastel = new Color(255, 232, 243); 
                Color bluePastel = new Color(219, 234, 254); 
                
                GradientPaint gradient = new GradientPaint(0, 0, pinkPastel, getWidth(), 0, bluePastel);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
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
        
        ImageIcon logoIcon = null;
        try {
            java.net.URL imgUrl = getClass().getResource("/img/logo5.png"); 
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                logoIcon = new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblTitle = new JLabel("Lập Hóa Đơn & Quản Lý Doanh Thu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(TEXT_DARK);

        if (logoIcon != null) {
            lblTitle.setIcon(logoIcon);
            lblTitle.setIconTextGap(14);
        }
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        JPanel headerWestWrapper = new JPanel(new GridBagLayout());
        headerWestWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        headerWestWrapper.add(titleBox, gbc);

        headerCard.add(headerWestWrapper, BorderLayout.CENTER);
        add(headerCard, BorderLayout.NORTH);

        // --- 2. CONTAINER CHÍNH CHIA 2 CỘT ---
        JPanel mainContentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainContentPanel.setOpaque(false);

        mainContentPanel.add(createLeftCreateFormPanel());
        mainContentPanel.add(createRightListPanel());

        add(mainContentPanel, BorderLayout.CENTER);

        // Tải dữ liệu ban đầu
        loadPatientMedicalRecords();
        loadInvoiceDataFromDatabase();
    }

    // --- CỘT TRÁI: FORM LẬP HÓA ĐƠN MỚI ---
    private JPanel createLeftCreateFormPanel() {
        JPanel card = createCardContainer();
        card.setLayout(new BorderLayout());

        JPanel formBody = new JPanel();
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));
        formBody.setOpaque(false);

        JLabel subTitleCard = new JLabel("Lập Hóa Đơn Mới");
        subTitleCard.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subTitleCard.setForeground(BLUE_PRIMARY);
        subTitleCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBody.add(subTitleCard);
        formBody.add(Box.createVerticalStrut(15));

        // Chọn Bệnh Án / Khám Bệnh
        formBody.add(createFieldLabel("Chọn Mã Bệnh Án / Khám Bệnh *"));
        formBody.add(Box.createVerticalStrut(6));

        cboChonBenhAn = new JComboBox<>();
        cboChonBenhAn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboChonBenhAn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cboChonBenhAn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboChonBenhAn.setBackground(Color.WHITE);
        cboChonBenhAn.addActionListener(e -> {
            MucBenhNhanHopChon selected = (MucBenhNhanHopChon) cboChonBenhAn.getSelectedItem();
            if (selected != null && !selected.maBenhAn.isEmpty()) {
                loadServicesByMedicalRecord(selected.maBenhAn);
            }
        });
        formBody.add(cboChonBenhAn);
        formBody.add(Box.createVerticalStrut(14));

        // Thông tin khách hàng
        txtCustomerName = addFieldToForm(formBody, "Tên Khách Hàng *");
        txtPhone = addFieldToForm(formBody, "Số Điện Thoại");

        // Bảng dịch vụ đã khám (Làm to, rộng rãi)
        formBody.add(createFieldLabel("Thông Tin Dịch Vụ Đã Khám"));
        formBody.add(Box.createVerticalStrut(6));

        String[] servCols = {"STT", "Dịch Vụ", "Đơn Giá", "SL", "Thành Tiền"};
        serviceTableModel = new DefaultTableModel(servCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        serviceTable = new JTable(serviceTableModel);
        styleTable(serviceTable);

        JScrollPane servScroll = new JScrollPane(serviceTable);
        servScroll.setPreferredSize(new Dimension(0, 160)); // Tăng chiều cao bảng dịch vụ cho thoáng
        servScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        servScroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        servScroll.getViewport().setBackground(CARD_BG);
        formBody.add(servScroll);
        formBody.add(Box.createVerticalStrut(14));

        // Phương thức thanh toán
        formBody.add(createFieldLabel("Phương Thức Thanh Toán"));
        formBody.add(Box.createVerticalStrut(6));

        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        cboPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboPaymentMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cboPaymentMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboPaymentMethod.setBackground(Color.WHITE);
        formBody.add(cboPaymentMethod);
        formBody.add(Box.createVerticalStrut(14));

        formBody.add(createFieldLabel("Loại hóa đơn"));
        formBody.add(Box.createVerticalStrut(6));
        cboInvoiceType = new JComboBox<>(new String[]{"Hóa đơn khám chữa bệnh", "Hóa đơn thuốc", "Hóa đơn vật tư"});
        cboInvoiceType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboInvoiceType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cboInvoiceType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboInvoiceType.setBackground(Color.WHITE);
        formBody.add(cboInvoiceType);
        formBody.add(Box.createVerticalStrut(14));

        txtNote = addFieldToForm(formBody, "Ghi Chú (nếu có)");
        formBody.add(Box.createVerticalStrut(20));

        // Nút hành động to đẹp
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnBox.setOpaque(false);
        btnBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnReset = createStaticButton("Đặt Lại", new Color(240, 244, 248), 110, 44);
        btnReset.setForeground(TEXT_DARK);
        btnReset.addActionListener(e -> clearForm());

        JButton btnSaveOnly = createStaticButton("Lưu Hóa Đơn", GREEN_ACCENT, 145, 44);
        btnSaveOnly.addActionListener(e -> saveInvoiceToDatabase());

        JButton btnPrintInvoice = createStaticButton("In Hóa Đơn", BLUE_PRIMARY, 135, 44);
        btnPrintInvoice.addActionListener(e -> {
            if (saveInvoiceToDatabase()) exportSavedInvoice(maHoaDonMoiNhat);
        });

        btnBox.add(btnReset);
        btnBox.add(btnSaveOnly);
        btnBox.add(btnPrintInvoice);
        formBody.add(btnBox);

        JScrollPane mainFormScroll = new JScrollPane(formBody);
        mainFormScroll.setBorder(null);
        mainFormScroll.setOpaque(false);
        mainFormScroll.getViewport().setOpaque(false);

        card.add(mainFormScroll, BorderLayout.CENTER);
        return card;
    }

    // --- CỘT PHẢI: THỐNG KÊ & DANH SÁCH HÓA ĐƠN ---
    private JPanel createRightListPanel() {
        JPanel card = createCardContainer();
        card.setLayout(new BorderLayout(0, 15));

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);

        JLabel subTitleList = new JLabel("Danh Sách Hóa Đơn Đã Lưu");
        subTitleList.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subTitleList.setForeground(TEXT_DARK);
        rightHeader.add(subTitleList, BorderLayout.WEST);

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
        searchBox.setPreferredSize(new Dimension(260, 42));
        searchBox.setBorder(new EmptyBorder(6, 12, 6, 12));

        JLabel lblSearchIcon = new JLabel();
        try {
            java.net.URL imgUrl = getClass().getResource("/img/search_icon.png"); 
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                lblSearchIcon.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        searchBox.add(lblSearchIcon, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm mã HĐ, tên khách...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String kw = txtSearch.getText().trim();
                rowSorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
            }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchGroup.setOpaque(false);
        searchGroup.add(searchBox);
        rightHeader.add(searchGroup, BorderLayout.EAST);

        card.add(rightHeader, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 80));

        lblStatTotalInvoices = new JLabel("0");
        lblStatTotalRevenue = new JLabel("0 đ");
        lblStatMonthInvoices = new JLabel("0");
        lblStatPaidInvoices = new JLabel("0");

        statsPanel.add(createMiniStatCard("Tổng HĐ", lblStatTotalInvoices, BLUE_PRIMARY, new Color(232, 242, 255)));
        statsPanel.add(createMiniStatCard("Doanh Thu", lblStatTotalRevenue, GREEN_ACCENT, new Color(238, 247, 238)));
        statsPanel.add(createMiniStatCard("Tháng Này", lblStatMonthInvoices, TEXT_DARK, new Color(240, 244, 248)));
        statsPanel.add(createMiniStatCard("Đã Xong", lblStatPaidInvoices, PINK_ACCENT, new Color(255, 232, 243)));

        String[] cols = {"Mã HĐ", "Khách Hàng", "Ngày Lập", "Tổng Tiền", "Thanh Toán", "Trạng Thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        XuatBangPDF.install(table, "Danh_Sach_Hoa_Don");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);
        JButton btnExportInvoice = createStaticButton("In hóa đơn PDF", BLUE_PRIMARY, 145, 38);
        btnExportInvoice.addActionListener(e -> exportSelectedInvoice());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);

        JPanel centerListContainer = new JPanel(new BorderLayout(0, 12));
        centerListContainer.setOpaque(false);
        centerListContainer.add(statsPanel, BorderLayout.NORTH);
        JPanel tableArea = new JPanel(new BorderLayout(0, 8));
        tableArea.setOpaque(false);
        JPanel exportBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        exportBar.setOpaque(false);
        exportBar.add(btnExportInvoice);
        tableArea.add(exportBar, BorderLayout.NORTH);
        tableArea.add(scroll, BorderLayout.CENTER);
        centerListContainer.add(tableArea, BorderLayout.CENTER);

        card.add(centerListContainer, BorderLayout.CENTER);
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
        panel.setBorder(new EmptyBorder(22, 22, 22, 22));
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToForm(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(6));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); // Tăng chiều cao ô nhập liệu
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1), new EmptyBorder(6, 12, 6, 12)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(14));
        return txt;
    }

    private JPanel createMiniStatCard(String title, JLabel valLabel, Color accentColor, Color bgCard) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCard);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setLayout(new BorderLayout(0, 4));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(bg.equals(new Color(240, 244, 248)) ? TEXT_DARK : Color.WHITE);
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadPatientMedicalRecords() {
        if (cboChonBenhAn == null) return;
        cboChonBenhAn.removeAllItems();
        cboChonBenhAn.addItem(new MucBenhNhanHopChon("", "-- Bấm vào để chọn mã bệnh án --"));

        String sql = "SELECT ba.maBenhAn, k.tenKH " +
                     "FROM HoSoBenhAn ba " +
                     "JOIN KhachHang k ON ba.maKhachHang = k.maKhachHang " +
                     "WHERE ba.maBenhAn NOT IN (SELECT maBenhAn FROM HoaDon WHERE maBenhAn IS NOT NULL) " +
                     "ORDER BY ba.maBenhAn DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maBA = rs.getString("maBenhAn");
                String tenKH = rs.getString("tenKH");
                cboChonBenhAn.addItem(new MucBenhNhanHopChon(maBA, maBA + " - " + tenKH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadServicesByMedicalRecord(String maBenhAn) {
        serviceTableModel.setRowCount(0);
        if (maBenhAn == null || maBenhAn.isEmpty()) {
            txtCustomerName.setText("");
            txtPhone.setText("");
            return;
        }

        String sqlCust = "SELECT k.tenKH, k.sdt FROM HoSoBenhAn ba JOIN KhachHang k ON ba.maKhachHang = k.maKhachHang WHERE ba.maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCust)) {
            ps.setString(1, maBenhAn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtCustomerName.setText(rs.getString("tenKH"));
                    txtPhone.setText(rs.getString("sdt") != null ? rs.getString("sdt") : "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlServ = "SELECT d.tenDichVu, d.donGia, c.soLuong " +
                         "FROM ChiTietChiDinh c " +
                         "JOIN DichVu d ON c.maDichVu = d.maDichVu " +
                         "WHERE c.maBenhAn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlServ)) {
            ps.setString(1, maBenhAn);
            try (ResultSet rs = ps.executeQuery()) {
                int stt = 1;
                DecimalFormat df = new DecimalFormat("#,### đ");
                while (rs.next()) {
                    String tenDV = rs.getString("tenDichVu");
                    double donGia = rs.getDouble("donGia");
                    int soLuong = rs.getInt("soLuong");
                    if (soLuong <= 0) soLuong = 1;
                    double thanhTien = donGia * soLuong;

                    serviceTableModel.addRow(new Object[]{
                        stt++,
                        tenDV,
                        df.format(donGia),
                        soLuong,
                        df.format(thanhTien)
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MucBenhNhanHopChon {
        String maBenhAn, displayText;
        public MucBenhNhanHopChon(String maBenhAn, String displayText) {
            this.maBenhAn = maBenhAn;
            this.displayText = displayText;
        }
        @Override
        public String toString() {
            return displayText;
        }
    }

    // --- TẢI DANH SÁCH HÓA ĐƠN VÀ TÍNH TỔNG TIỀN THỰC TẾ TỪ CSDL ---
    private void loadInvoiceDataFromDatabase() {
        tableModel.setRowCount(0);
        int totalCount = 0;
        double totalRev = 0;
        int monthCount = 0;
        int paidCount = 0;

        // Truy vấn lấy hóa đơn kết hợp với bảng ChiTietChiDinh và DichVu để tính đúng tổng tiền thực tế
        String sql = "SELECT h.maHoaDon, k.tenKH, h.ngayLap, " +
                     "ISNULL((SELECT SUM(d.donGia * c.soLuong) FROM ChiTietChiDinh c JOIN DichVu d ON c.maDichVu = d.maDichVu WHERE c.maBenhAn = h.maBenhAn), 0) " +
                     "+ ISNULL((SELECT SUM(ct.donGia * ct.soLuong) FROM ChiTietHoaDon ct WHERE ct.maHoaDon = h.maHoaDon AND ct.maVatTu IS NOT NULL), 0) AS tongTien, " +
                     "h.loaiHoaDon, (SELECT TOP 1 p.hinhThucThanhToan FROM PhieuThanhToan p WHERE p.maHoaDon=h.maHoaDon ORDER BY p.ngayThanhToan DESC) AS hinhThucThanhToan " +
                     "FROM HoaDon h " +
                     "LEFT JOIN KhachHang k ON h.maKhachHang = k.maKhachHang " +
                     "ORDER BY h.ngayLap DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            DecimalFormat df = new DecimalFormat("#,### đ");
            LocalDate now = LocalDate.now();

            while (rs.next()) {
                totalCount++;
                String maHD = rs.getString("maHoaDon");
                String tenKH = rs.getString("tenKH");
                if (tenKH == null) tenKH = "Khách lẻ";
                Date ngayLap = rs.getDate("ngayLap");
                String dateStr = ngayLap != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(ngayLap) : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                double tongTien = rs.getDouble("tongTien"); // Lấy tổng tiền thực tế của bệnh án tương ứng
                totalRev += tongTien;

                if (ngayLap != null) {
                    LocalDate ld = ngayLap.toLocalDate();
                    if (ld.getMonth() == now.getMonth() && ld.getYear() == now.getYear()) {
                        monthCount++;
                    }
                }
                String method = rs.getString("hinhThucThanhToan");
                if (method != null) paidCount++;

                tableModel.addRow(new Object[]{
                    maHD,
                    tenKH,
                    dateStr,
                    df.format(tongTien),
                    method == null ? "Chưa thanh toán" : method,
                    method == null ? "Chưa thanh toán" : "Đã thanh toán"
                });
            }

            if (lblStatTotalInvoices != null) lblStatTotalInvoices.setText(String.valueOf(totalCount));
            if (lblStatTotalRevenue != null) lblStatTotalRevenue.setText(String.format("%,.0f đ", totalRev));
            if (lblStatMonthInvoices != null) lblStatMonthInvoices.setText(String.valueOf(monthCount));
            if (lblStatPaidInvoices != null) lblStatPaidInvoices.setText(String.valueOf(paidCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean saveInvoiceToDatabase() {
        MucBenhNhanHopChon selectedPatient = (MucBenhNhanHopChon) cboChonBenhAn.getSelectedItem();
        if (selectedPatient == null || selectedPatient.maBenhAn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mã bệnh án trước khi lưu hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String maBenhAn = selectedPatient.maBenhAn;
        String loaiHoaDon = String.valueOf(cboInvoiceType.getSelectedItem());
        String maLoai = loaiHoaDon.contains("thuốc") ? "T" : loaiHoaDon.contains("vật tư") ? "V" : "K";
        String maHoaDon = "HD" + maLoai + System.currentTimeMillis();
        String hinhThuc = String.valueOf(cboPaymentMethod.getSelectedItem());

        String maKH = "";
        String maNV = "NV001";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maKhachHang, maNhanVien FROM HoSoBenhAn WHERE maBenhAn = ?")) {
            ps.setString(1, maBenhAn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maKH = rs.getString("maKhachHang");
                    String nv = rs.getString("maNhanVien");
                    if (nv != null) maNV = nv;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            double amount = calculateInvoiceTotal(conn, maBenhAn, null);
            if ("Chuyển khoản".equalsIgnoreCase(hinhThuc) && !showTransferQr(amount, maHoaDon)) {
                conn.rollback(); conn.setAutoCommit(true); return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO HoaDon (maHoaDon, maBenhAn, maKhachHang, maNhanVien, ngayLap, loaiHoaDon) VALUES (?, ?, ?, ?, GETDATE(), ?)")) {
                ps.setString(1, maHoaDon); ps.setString(2, maBenhAn); ps.setString(3, maKH); ps.setString(4, maNV); ps.setString(5, loaiHoaDon);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO PhieuThanhToan (maPhieu, maHoaDon, maKhachHang, maNhanVien, ngayThanhToan, hinhThucThanhToan, daThanhToan, soTienConLai, ghiChu) VALUES (?, ?, ?, ?, GETDATE(), ?, ?, 0, ?)")) {
                ps.setString(1, "PT" + System.currentTimeMillis()); ps.setString(2, maHoaDon); ps.setString(3, maKH); ps.setString(4, maNV);
                ps.setString(5, hinhThuc); ps.setDouble(6, amount); ps.setString(7, loaiHoaDon); ps.executeUpdate();
            }
            conn.commit(); conn.setAutoCommit(true);
            maHoaDonMoiNhat = maHoaDon;

            NhatKyKiemToan.event("CREATE", "Hóa đơn " + maHoaDon + " cho bệnh án " + maBenhAn);
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn thành công!");
            
            clearForm();
            loadInvoiceDataFromDatabase();
            loadPatientMedicalRecords();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn vào CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private double calculateInvoiceTotal(Connection conn, String maBenhAn, String maHoaDon) throws SQLException {
        String sql = "SELECT ISNULL((SELECT SUM(d.donGia*c.soLuong) FROM ChiTietChiDinh c JOIN DichVu d ON c.maDichVu=d.maDichVu WHERE c.maBenhAn=?),0) " +
                "+ ISNULL((SELECT SUM(ct.donGia*ct.soLuong) FROM ChiTietHoaDon ct WHERE ct.maHoaDon=?),0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maBenhAn); ps.setString(2, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getDouble(1) : 0d; }
        }
    }

    private boolean showTransferQr(double amount, String maHoaDon) {
        String payload = buildVietQrPayload((long)Math.ceil(amount), maHoaDon);
        byte[] png = DieuKhienMaQR.generateQrCodeBytes(payload);
        if (png == null) {
            JOptionPane.showMessageDialog(this, "Không thể tạo mã QR chuyển khoản.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        JLabel qr = new JLabel(new ImageIcon(png));
        qr.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel details = new JPanel(); details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(new JLabel("BIDV • 8822902096"));
        details.add(new JLabel("NGUYEN MINH SY LUAN"));
        details.add(new JLabel(String.format(java.util.Locale.forLanguageTag("vi-VN"), "Số tiền: %,.0f đ", amount)));
        details.add(new JLabel("Nội dung: " + maHoaDon));
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.add(qr, BorderLayout.CENTER); body.add(details, BorderLayout.SOUTH);
        return JOptionPane.showConfirmDialog(this, body, "Quét QR và xác nhận đã nhận chuyển khoản", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private String buildVietQrPayload(long amount, String note) {
        String merchantInfo = tlv("00", "A000000727") + tlv("01", "970418") + tlv("02", "8822902096");
        String additional = tlv("08", note.length() > 25 ? note.substring(0, 25) : note);
        String payload = tlv("00", "01") + tlv("01", "12") + tlv("26", merchantInfo) +
                tlv("52", "0000") + tlv("53", "704") + tlv("54", String.valueOf(amount)) +
                tlv("58", "VN") + tlv("59", "NGUYEN MINH SY LUAN") + tlv("60", "HO CHI MINH") +
                tlv("62", additional) + "6304";
        return payload + String.format("%04X", crc16(payload));
    }

    private String tlv(String tag, String value) {
        int length = value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        return tag + String.format("%02d", length) + value;
    }

    private int crc16(String value) {
        int crc = 0xFFFF;
        for (byte b : value.getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
        }
        return crc & 0xFFFF;
    }

    private void exportSelectedInvoice() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn trong danh sách.", "In hóa đơn PDF", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        exportSavedInvoice(String.valueOf(tableModel.getValueAt(modelRow, 0)));
    }

    private void exportSavedInvoice(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setSelectedFile(new File("HoaDon_" + maHoaDon + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        String sql = "SELECT h.maHoaDon, h.ngayLap, k.tenKH, h.loaiHoaDon, " +
                "ISNULL((SELECT SUM(d.donGia*c.soLuong) FROM ChiTietChiDinh c JOIN DichVu d ON c.maDichVu=d.maDichVu WHERE c.maBenhAn=h.maBenhAn),0) " +
                "+ ISNULL((SELECT SUM(ct.donGia*ct.soLuong) FROM ChiTietHoaDon ct WHERE ct.maHoaDon=h.maHoaDon AND ct.maVatTu IS NOT NULL),0) tongTien " +
                ",(SELECT TOP 1 p.hinhThucThanhToan FROM PhieuThanhToan p WHERE p.maHoaDon=h.maHoaDon ORDER BY p.ngayThanhToan DESC) hinhThucThanhToan " +
                "FROM HoaDon h LEFT JOIN KhachHang k ON h.maKhachHang=k.maKhachHang WHERE h.maHoaDon=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy hóa đơn " + maHoaDon);
                int total = (int) Math.round(rs.getDouble("tongTien"));
                java.sql.Date date = rs.getDate("ngayLap");
                String dateText = date == null ? "" : date.toLocalDate().toString();
                String customer = rs.getString("tenKH");
                String method = rs.getString("hinhThucThanhToan");
                String invoiceType = rs.getString("loaiHoaDon");
                DichVuBaoCao.exportInvoice(file, maHoaDon, dateText,
                        customer == null ? "Khách lẻ" : customer,
                        invoiceType == null ? "Chi phí khám và thuốc/vật tư" : invoiceType, total, "Không áp dụng", 0, total,
                        method == null ? "Chưa thanh toán" : method, "Nhân viên phòng khám");
            }
            JOptionPane.showMessageDialog(this, "Đã xuất hóa đơn PDF:\n" + file.getAbsolutePath(), "In hóa đơn PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể xuất hóa đơn PDF: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        if (cboChonBenhAn != null && cboChonBenhAn.getItemCount() > 0) {
            cboChonBenhAn.setSelectedIndex(0);
        }
        if (txtCustomerName != null) txtCustomerName.setText("");
        if (txtPhone != null) txtPhone.setText("");
        if (txtNote != null) txtNote.setText("");
        if (serviceTableModel != null) {
            serviceTableModel.setRowCount(0);
        }
    }

    private void styleTable(JTable table) {
        table.setRowHeight(48); // Tăng chiều cao dòng bảng cho cân đối, dễ nhìn
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(235, 240, 248));
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(38, 56, 101));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 48)); // Tăng chiều cao header bảng
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(new Color(38, 56, 101));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(75, 91, 130)));
                return lbl;
            }
        });
    }
}
