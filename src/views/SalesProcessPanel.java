package views;

import controllers.AuditLog;
import controllers.PdfTableExport;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SalesProcessPanel extends JPanel {
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
    private JComboBox<PatientComboItem> cboChonBenhAn;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private DefaultTableModel serviceTableModel;
    private JTable serviceTable;

    private JLabel lblStatTotalInvoices, lblStatTotalRevenue, lblStatMonthInvoices, lblStatPaidInvoices;

    public SalesProcessPanel() {
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

        JLabel lblSub = new JLabel("Tạo hóa đơn mới từ dịch vụ khám bệnh và tra cứu lịch sử hóa đơn toàn hệ thống");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(MUTED);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);
        
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
            PatientComboItem selected = (PatientComboItem) cboChonBenhAn.getSelectedItem();
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

        cboPaymentMethod = new JComboBox<>(new String[]{"Tiền Mặt", "Chuyển Khoản Ngân Hàng", "Thẻ Tín Dụng"});
        cboPaymentMethod.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboPaymentMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cboPaymentMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboPaymentMethod.setBackground(Color.WHITE);
        formBody.add(cboPaymentMethod);
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
            saveInvoiceToDatabase();
            try { table.print(); } catch (Exception ignored) {}
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
        PdfTableExport.install(table, "Danh_Sach_Hoa_Don");
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scroll.getViewport().setBackground(CARD_BG);

        JPanel centerListContainer = new JPanel(new BorderLayout(0, 12));
        centerListContainer.setOpaque(false);
        centerListContainer.add(statsPanel, BorderLayout.NORTH);
        centerListContainer.add(scroll, BorderLayout.CENTER);

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
        cboChonBenhAn.addItem(new PatientComboItem("", "-- Bấm vào để chọn mã bệnh án --"));

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
                cboChonBenhAn.addItem(new PatientComboItem(maBA, maBA + " - " + tenKH));
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

    private static class PatientComboItem {
        String maBenhAn, displayText;
        public PatientComboItem(String maBenhAn, String displayText) {
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
                     "ISNULL((SELECT SUM(d.donGia * c.soLuong) FROM ChiTietChiDinh c JOIN DichVu d ON c.maDichVu = d.maDichVu WHERE c.maBenhAn = h.maBenhAn), 0) AS tongTien " +
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
                paidCount++;

                tableModel.addRow(new Object[]{
                    maHD,
                    tenKH,
                    dateStr,
                    df.format(tongTien),
                    "Tiền Mặt",
                    "Đã thanh toán"
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

    private void saveInvoiceToDatabase() {
        PatientComboItem selectedPatient = (PatientComboItem) cboChonBenhAn.getSelectedItem();
        if (selectedPatient == null || selectedPatient.maBenhAn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mã bệnh án trước khi lưu hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maBenhAn = selectedPatient.maBenhAn;
        String maHoaDon = "HD" + System.currentTimeMillis();

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

        String insertSql = "INSERT INTO HoaDon (maHoaDon, maBenhAn, maKhachHang, maNhanVien, ngayLap) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maBenhAn);
            ps.setString(3, maKH);
            ps.setString(4, maNV);
            ps.executeUpdate();

            AuditLog.event("CREATE", "Hóa đơn " + maHoaDon + " cho bệnh án " + maBenhAn);
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn thành công!");
            
            clearForm();
            loadInvoiceDataFromDatabase();
            loadPatientMedicalRecords();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn vào CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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