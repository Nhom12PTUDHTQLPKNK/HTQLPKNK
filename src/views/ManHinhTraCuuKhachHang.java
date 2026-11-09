package views;

import controllers.QuanLyKhachHang;
import models.dao.TraCuuKhachHangDAO;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.plaf.basic.BasicProgressBarUI;

public class ManHinhTraCuuKhachHang extends JPanel {

    private static final Color PINK = new Color(0xE91E78), PINK_SOFT = new Color(0xFDE8F1), BLUE = new Color(0x1E6FD9), BLUE_SOFT = new Color(0xE6F0FD), BG = new Color(0xF4F7FC), TEXT = new Color(0x1F2A44), MUTED = new Color(0x6B7794), LINE = new Color(0xE3E8F2), GOLD_BG = new Color(0xFFF5D9), GOLD_TEXT = new Color(0xB77916), SILVER_BG = new Color(0xE6F0FD), SILVER_TEXT = new Color(0x2874D0), MEMBER_BG = new Color(0xEEF0F4), MEMBER_TEXT = new Color(0x687386);
    private static final Font F = new Font("Segoe UI", Font.PLAIN, 13), FB = F.deriveFont(Font.BOLD);
    private static final int SILVER = QuanLyKhachHang.SILVER_POINTS, GOLD = QuanLyKhachHang.GOLD_POINTS;
    private final QuanLyKhachHang customerRules = new QuanLyKhachHang();
    private static final String[] COLS = {"Mã KH", "Họ tên", "Giới tính", "Số điện thoại", "Ngày sinh", "Điểm", "Hạng"};
    private static final int[] WIDTHS = {80, 210, 80, 130, 100, 75, 110};
    private static final String[] KEYS = {"Số điện thoại", "Ngày sinh", "Giới tính", "Địa chỉ", "Số hồ sơ khám", "Lần khám gần nhất", "Lịch hẹn sắp tới", "Tổng giá trị dịch vụ", "Tiền sử bệnh"};

    private final JLabel[] statVals = new JLabel[4], vals = new JLabel[KEYS.length];
    private final TraCuuKhachHangDAO customerDAO = new TraCuuKhachHangDAO();
    private JLabel lblName, lblTier, lblPoints, lblNext, lblCount, lblAvatar;
    private JTextField txtSearch;
    private JComboBox<String> cboSort;
    private JTable table;
    private DefaultTableModel model;
    private JProgressBar bar;
    private JButton btnAdd, btnSub, btnAddCustomer;
    private JButton[] tierButtons = new JButton[4];
    private JPanel tableContent, profileHeader;
    private CardLayout tableLayout;
    private int selectedTier = 0;
    private String selectedId;
    private int seq, detSeq;

    public ManHinhTraCuuKhachHang() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        // 1. HEADER DẠNG CARD NGANG (GRADIENT HỒNG <-> XANH + LOGO)
        JPanel head = new JPanel() {
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
                g2.setColor(LINE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(20, 20, 20, 20));
        head.setLayout(new BorderLayout());
        head.setPreferredSize(new Dimension(0, 110));
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel ttl = new JPanel();
        ttl.setOpaque(false);
        ttl.setLayout(new BoxLayout(ttl, BoxLayout.Y_AXIS));

        JLabel lblTitle = lbl("Tra Cứu Khách Hàng & Điểm Tích Lũy", F.deriveFont(Font.BOLD, 25f), TEXT);

        // Tải và thu nhỏ logo cho cân đối (45x45 pixel)
        if (imageExists("logo5.png")) {
            lblTitle.setIcon(loadImage("logo5.png", 45, 45));
            lblTitle.setIconTextGap(14); // Khoảng cách giữa ảnh và chữ tiêu đề
        }

        ttl.add(lblTitle);
        ttl.add(Box.createVerticalStrut(4));
        ttl.add(lbl("Tìm theo mã, họ tên hoặc số điện thoại; dữ liệu lấy trực tiếp từ SQL Server", F.deriveFont(15f), MUTED));

        // Căn giữa tiêu đề theo chiều dọc trong card
        JPanel headWestWrapper = new JPanel(new GridBagLayout());
        headWestWrapper.setOpaque(false);
        GridBagConstraints gbcWest = new GridBagConstraints();
        gbcWest.anchor = GridBagConstraints.WEST;
        gbcWest.weightx = 1.0;
        headWestWrapper.add(ttl, gbcWest);

        JButton bRefresh = btn("Làm mới", PINK, Color.WHITE);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acts.setOpaque(false);
        acts.add(bRefresh);

        // Các nút nằm ở góc phải của TheThongTin Header, căn giữa theo chiều dọc
        JPanel headRight = new JPanel(new GridBagLayout());
        headRight.setOpaque(false);
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.anchor = GridBagConstraints.EAST;
        headRight.add(acts, gbcRight);

        head.add(headWestWrapper, BorderLayout.CENTER);
        head.add(headRight, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(stat("Tổng khách hàng", PINK, 0, "tongKH.png", PINK_SOFT, null));
        stats.add(stat("Tổng điểm đã tích lũy", BLUE, 1, "tongdiemTL.png", BLUE_SOFT, null));
        stats.add(stat("Điểm trung bình", PINK, 2, "average-score.png", PINK_SOFT, null));
        stats.add(stat("Khách hàng VIP", GOLD_TEXT, 3, "gold-KH.png", GOLD_BG, "vip.png"));

        txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(MUTED);
                    g2.setFont(F);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Nhập mã khách hàng, họ tên hoặc số điện thoại...", getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            }
        };

        txtSearch.setFont(F);
        txtSearch.setForeground(TEXT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(9, 12, 9, 12)));

        cboSort = new JComboBox<>(new String[]{"Sắp xếp: Mã KH", "Sắp xếp: Tên A - Z", "Sắp xếp: Điểm cao nhất", "Sắp xếp: Điểm thấp nhất"});
        cboSort.setFont(F);
        cboSort.setBackground(Color.WHITE);

        JButton bSearch = btn("Tìm kiếm", BLUE, Color.WHITE);

        TheThongTin filter = new TheThongTin(null);
        filter.setLayout(new BorderLayout(12, 0));
        filter.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel fr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        fr.setOpaque(false);
        fr.add(cboSort);
        fr.add(bSearch);

        filter.add(txtSearch, BorderLayout.CENTER);
        filter.add(fr, BorderLayout.EAST);

        JPanel quickFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        quickFilter.setOpaque(false);

        String[] tierNames = {"Tất cả", "Hạng Vàng", "Hạng Bạc", "Thành viên"};
        for (int i = 0; i < tierNames.length; i++) {
            final int index = i;
            tierButtons[i] = createChip(tierNames[i], i);
            tierButtons[i].addActionListener(e -> {
                selectedTier = index;
                updateChips();
                load();
            });
            quickFilter.add(tierButtons[i]);
        }

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(head);
        north.add(Box.createVerticalStrut(16));
        north.add(stats);
        north.add(Box.createVerticalStrut(14));
        north.add(filter);
        north.add(Box.createVerticalStrut(12));
        north.add(quickFilter);

        add(north, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 5 ? Integer.class : String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(F);
        table.setSelectionBackground(BLUE_SOFT);
        table.setSelectionForeground(TEXT);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setFont(F);
                setForeground(TEXT);
                setBackground(sel ? BLUE_SOFT : (r % 2 == 0 ? Color.WHITE : new Color(0xFAFBFE)));

                if (c == 0 || c == 2 || c == 3 || c == 4) setHorizontalAlignment(CENTER);
                else if (c == 5) {
                    setHorizontalAlignment(RIGHT);
                    setFont(FB);
                    setForeground(BLUE);
                } else setHorizontalAlignment(LEFT);

                return this;
            }
        };

        DefaultTableCellRenderer hr = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setOpaque(true);
                setBackground(PINK);
                setForeground(Color.WHITE);
                setFont(FB);
                setHorizontalAlignment(c == 0 || c == 2 || c == 3 || c == 4 ? CENTER : c == 5 ? RIGHT : LEFT);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        };

        for (int i = 0; i < COLS.length; i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            tc.setPreferredWidth(WIDTHS[i]);
            tc.setCellRenderer(i == 6 ? new BoVeHangThanhVien() : cr);
            tc.setHeaderRenderer(hr);
        }

        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);

        lblCount = lbl("", F, MUTED);
        lblCount.setBorder(new EmptyBorder(8, 8, 2, 0));

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(Color.WHITE);
        dataPanel.add(sp, BorderLayout.CENTER);
        dataPanel.add(lblCount, BorderLayout.SOUTH);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        emptyPanel.setBorder(new EmptyBorder(45, 15, 35, 15));

        JLabel emptyTitle = lbl("Không tìm thấy khách hàng phù hợp", F.deriveFont(Font.BOLD, 17f), TEXT);
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySub = lbl("Thử thay đổi từ khóa hoặc bộ lọc tìm kiếm", F, MUTED);
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAddCustomer = btn("Thêm khách hàng mới", PINK, Color.WHITE);
        btnAddCustomer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAddCustomer.addActionListener(e -> JOptionPane.showMessageDialog(this, "Vui lòng kết nối nút này với form tạo khách hàng của chức năng Quản lý khách hàng.", "Thêm khách hàng", JOptionPane.INFORMATION_MESSAGE));

        emptyPanel.add(Box.createVerticalGlue());
        emptyPanel.add(emptyTitle);
        emptyPanel.add(Box.createVerticalStrut(9));
        emptyPanel.add(emptySub);
        emptyPanel.add(Box.createVerticalStrut(18));
        emptyPanel.add(btnAddCustomer);
        emptyPanel.add(Box.createVerticalGlue());

        tableLayout = new CardLayout();
        tableContent = new JPanel(tableLayout);
        tableContent.setOpaque(false);
        tableContent.add(dataPanel, "DATA");
        tableContent.add(emptyPanel, "EMPTY");

        TheThongTin tableCard = new TheThongTin(null);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(8, 8, 8, 8));
        tableCard.add(tableContent, BorderLayout.CENTER);

        // Khung nội dung chi tiết: tự co theo chiều rộng panel nên không bị tràn ngang
        KhungCuon box = new KhungCuon();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(5, 0, 5, 10));

        // Phần đầu hồ sơ: avatar bên trái, tên và hạng bên phải, căn trái đồng bộ với các nhãn bên dưới
        profileHeader = new JPanel(new BorderLayout(12, 0));
        profileHeader.setOpaque(false);
        profileHeader.setBorder(new EmptyBorder(5, 0, 14, 0));
        profileHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblAvatar = new JLabel("?");
        lblAvatar.setPreferredSize(new Dimension(58, 58));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setFont(F.deriveFont(Font.BOLD, 23f));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setOpaque(false);
        lblAvatar.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PINK);
                g2.fillOval(0, 0, c.getWidth(), c.getHeight());
                g2.dispose();
                super.paint(g, c);
            }
        });

        // Bọc avatar để giữ đúng kích thước 58x58, không bị kéo giãn theo chiều cao
        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(lblAvatar);

        JPanel profileInfo = new JPanel();
        profileInfo.setOpaque(false);
        profileInfo.setLayout(new BoxLayout(profileInfo, BoxLayout.Y_AXIS));

        lblName = lbl("Chọn một khách hàng", F.deriveFont(Font.BOLD, 17f), TEXT);
        lblTier = lbl("Chọn từ danh sách để xem chi tiết", F, MUTED);
        profileInfo.add(Box.createVerticalGlue());
        profileInfo.add(lblName);
        profileInfo.add(Box.createVerticalStrut(5));
        profileInfo.add(lblTier);
        profileInfo.add(Box.createVerticalGlue());

        profileHeader.add(avatarWrap, BorderLayout.WEST);
        profileHeader.add(profileInfo, BorderLayout.CENTER);

        lblPoints = lbl("0", F.deriveFont(Font.BOLD, 40f), PINK);
        lblNext = lbl(" ", F.deriveFont(12f), MUTED);

        bar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(PINK_SOFT);
                g2.fillRoundRect(0, 0, w, h, h, h);
                int fill = (int) (w * getPercentComplete());
                if (fill > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, PINK, Math.max(fill, 1), 0, new Color(0xFF9BC4));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, fill, h, h, h);
                }
                g2.dispose();
            }
        };

        bar.setBorderPainted(false);
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 11));
        bar.setPreferredSize(new Dimension(100, 11));

        box.add(profileHeader);
        box.add(lblPoints);
        box.add(lbl("điểm tích lũy", F, MUTED));
        box.add(Box.createVerticalStrut(12));
        box.add(bar);
        box.add(Box.createVerticalStrut(7));
        box.add(lblNext);
        box.add(Box.createVerticalStrut(18));

        // Các dòng thông tin: để nhãn tự xuống dòng và tự tính chiều cao, địa chỉ dài vẫn hiện đủ
        for (int i = 0; i < KEYS.length; i++) {
            JLabel keyLabel = lbl(KEYS[i], F.deriveFont(11.5f), MUTED);
            box.add(keyLabel);
            box.add(Box.createVerticalStrut(2));

            vals[i] = lbl("-", F, TEXT);
            box.add(vals[i]);

            box.add(Box.createVerticalStrut(11));
        }

        JScrollPane ds = new JScrollPane(box);
        ds.setBorder(null);
        ds.setOpaque(false);
        ds.getViewport().setOpaque(false);
        ds.getVerticalScrollBar().setUnitIncrement(14);
        ds.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        btnAdd = btn("+  Cộng điểm", PINK, Color.WHITE);
        btnSub = btn("-  Trừ điểm", BLUE_SOFT, BLUE);
        btnAdd.setEnabled(false);
        btnSub.setEnabled(false);
        btnAdd.setPreferredSize(new Dimension(140, 46));
        btnSub.setPreferredSize(new Dimension(140, 46));

        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0));
        bp.setOpaque(false);
        bp.add(btnAdd);
        bp.add(btnSub);

        TheThongTin detail = new TheThongTin(PINK);
        detail.setLayout(new BorderLayout(0, 10));
        detail.setPreferredSize(new Dimension(340, 0));
        detail.setMinimumSize(new Dimension(320, 0));
        detail.add(ds, BorderLayout.CENTER);
        detail.add(bp, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(14, 0));
        center.setOpaque(false);
        center.add(tableCard, BorderLayout.CENTER);
        center.add(detail, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);

        Timer debounce = new Timer(300, e -> load());
        debounce.setRepeats(false);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounce.restart(); }
            public void removeUpdate(DocumentEvent e) { debounce.restart(); }
            public void changedUpdate(DocumentEvent e) { debounce.restart(); }
        });

        txtSearch.addActionListener(e -> load());
        bSearch.addActionListener(e -> load());
        cboSort.addActionListener(e -> load());

        bRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboSort.setSelectedIndex(0);
            selectedTier = 0;
            updateChips();
            load();
        });

        btnAdd.addActionListener(e -> adjust(1));
        btnSub.addActionListener(e -> adjust(-1));

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0) {
                selectedId = (String) table.getValueAt(r, 0);
                loadDetail(selectedId);
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) load();
        });
    }

    private void load() {
        final int id = ++seq, lv = selectedTier, sort = cboSort.getSelectedIndex();
        final String kw = txtSearch.getText().trim();

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                TraCuuKhachHangDAO.CustomerRows result = customerDAO.search(kw, lv, sort, SILVER, GOLD);
                return new Object[]{result.rows, result.statistics};
            }

            @Override
            protected void done() {
                if (id != seq) return;

                try {
                    Object[] res = get();
                    @SuppressWarnings("unchecked")
                    List<Object[]> rows = (List<Object[]>) res[0];
                    double[] st = (double[]) res[1];

                    model.setRowCount(0);
                    int sel = -1;

                    for (Object[] raw : rows) {
                        Object[] r = new Object[]{raw[0], raw[1], raw[2], raw[3], raw[4], raw[5], customerRules.rank(((Number) raw[5]).intValue())};
                        if (r[0].equals(selectedId)) sel = model.getRowCount();
                        model.addRow(r);
                    }

                    if (sel >= 0) table.setRowSelectionInterval(sel, sel);
                    else if (table.getRowCount() > 0 && selectedId == null) table.setRowSelectionInterval(0, 0);

                    tableLayout.show(tableContent, rows.isEmpty() ? "EMPTY" : "DATA");

                    DecimalFormat df = new DecimalFormat("#,##0");
                    statVals[0].setText(df.format(st[0]));
                    statVals[1].setText(df.format(st[1]));
                    statVals[2].setText(new DecimalFormat("#,##0.#").format(st[2]));
                    statVals[3].setText(df.format(st[3]));

                    lblCount.setText("Hiển thị " + rows.size() + " khách hàng");
                } catch (Exception e) {
                    err(e);
                }
            }
        }.execute();
    }

    private void loadDetail(String ma) {
        final int id = ++detSeq;

        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String[] data = customerDAO.getDetails(ma);
                data[9] = new DecimalFormat("#,##0").format(Long.parseLong(data[9].isEmpty() ? "0" : data[9])) + " đ";
                data[1] = data[1].isEmpty() ? "0" : data[1];
                return data;
            }

            @Override
            protected void done() {
                if (id != detSeq) return;

                try {
                    String[] d = get();
                    int p = Integer.parseInt(d[1]);
                    String rk = customerRules.rank(p);

                    // Tên dài tự xuống dòng thay vì bị cắt
                    lblName.setText(wrap(d[0], 190));
                    lblAvatar.setText(getInitials(d[0]));
                    lblPoints.setText(String.valueOf(p));
                    lblTier.setText("Mã " + ma + "  |  Hạng " + rk);
                    lblTier.setForeground(tierColor(rk));

                    int target = customerRules.nextTierTarget(p);
                    bar.setValue(target == 0 ? 100 : Math.min(100, p * 100 / target));

                    lblNext.setText(target == 0 ? "Đã đạt hạng cao nhất" : "Còn " + customerRules.pointsToNextTier(p) + " điểm để lên hạng " + customerRules.rank(target));

                    for (int i = 0; i < vals.length; i++) {
                        vals[i].setText(wrap(d[i + 2].isEmpty() ? "Chưa có" : d[i + 2], 250));
                    }

                    btnAdd.setEnabled(true);
                    btnSub.setEnabled(true);
                } catch (Exception e) {
                    err(e);
                }
            }
        }.execute();
    }

    private void adjust(int sign) {
        if (selectedId == null) return;

        JSpinner sp = new JSpinner(new SpinnerNumberModel(10, 1, 100000, 5));

        if (JOptionPane.showConfirmDialog(this, new Object[]{sign > 0 ? "Số điểm cộng thêm:" : "Số điểm trừ đi:", sp}, "Điều chỉnh điểm - " + selectedId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            sp.commitEdit();
        } catch (Exception ignored) {
        }

        final int pts = (Integer) sp.getValue() * sign;
        final String ma = selectedId;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                customerDAO.adjustPoints(ma, pts);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    load();
                    loadDetail(ma);
                } catch (Exception e) {
                    err(e);
                }
            }
        }.execute();
    }


    private JButton createChip(String text, int index) {
        JButton b = new JButton(text);
        b.setFont(FB);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 17, 8, 17));
        b.setBackground(Color.WHITE);
        b.setForeground(MUTED);
        return b;
    }

    private void updateChips() {
        for (int i = 0; i < tierButtons.length; i++) {
            boolean active = selectedTier == i;
            tierButtons[i].setBackground(active ? PINK_SOFT : Color.WHITE);
            tierButtons[i].setForeground(active ? PINK : MUTED);
            tierButtons[i].setBorder(BorderFactory.createCompoundBorder(new LineBorder(active ? PINK : LINE, 1, true), new EmptyBorder(7, 16, 7, 16)));
        }
    }

    private JPanel stat(String title, Color accent, int i, String image, Color background, String vipImage) {
        TheThongTin c = new TheThongTin(accent);
        c.setLayout(new BorderLayout(12, 0));
        c.setBackground(background);
        c.setBorder(new EmptyBorder(13, 15, 13, 13));

        JLabel icon = new JLabel(loadImage(image, 48, 48));
        icon.setPreferredSize(new Dimension(48, 48));
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        titlePanel.setOpaque(false);
        JLabel titleLabel = lbl(title, F, MUTED);
        titlePanel.add(titleLabel);

        if (vipImage != null) {
            JLabel vip = new JLabel(loadImage(vipImage, 37, 25));
            titlePanel.add(vip);
        }

        Color[] numberColors = {
                new Color(0xC2185B),
                new Color(0x0D47A1),
                new Color(0xC2185B),
                new Color(0x8A5700)
        };

        statVals[i] = lbl("0", F.deriveFont(Font.BOLD, 42f), numberColors[i]);
        statVals[i].setAlignmentX(Component.LEFT_ALIGNMENT);
        statVals[i].setHorizontalAlignment(SwingConstants.LEFT);
        statVals[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        info.add(titlePanel);
        info.add(Box.createVerticalStrut(3));
        info.add(statVals[i]);

        c.add(icon, BorderLayout.WEST);
        c.add(info, BorderLayout.CENTER);

        return c;
    }

    private class BoVeHangThanhVien extends JLabel implements TableCellRenderer {
        BoVeHangThanhVien() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(FB);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 8, 5, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String tier = String.valueOf(v);
            setText(tier);
            setForeground(tierText(tier));
            setBackground(tierBackground(tier));
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(9, 14, 9, 14), new LineBorder(tierBackground(tier), 1, true)));

            if (sel) {
                setForeground(tierText(tier));
            }

            return this;
        }
    }

    private static Color tierBackground(String t) {
        return t.equals("Vàng") ? GOLD_BG : t.equals("Bạc") ? SILVER_BG : MEMBER_BG;
    }

    private static Color tierText(String t) {
        return t.equals("Vàng") ? GOLD_TEXT : t.equals("Bạc") ? SILVER_TEXT : MEMBER_TEXT;
    }

    // Kiểm tra file ảnh có tồn tại không (trong resources hoặc src/img)
    private static boolean imageExists(String name) {
        return ManHinhTraCuuKhachHang.class.getResource("/img/" + name) != null
                || new File("src/img/" + name).exists();
    }

    private static ImageIcon loadImage(String name, int width, int height) {
        java.net.URL url = ManHinhTraCuuKhachHang.class.getResource("/img/" + name);

        if (url != null) {
            ImageIcon original = new ImageIcon(url);
            Image image = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        }

        File file = new File("src/img/" + name);

        if (file.exists()) {
            ImageIcon original = new ImageIcon(file.getAbsolutePath());
            Image image = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        }

        return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private static Color tierColor(String t) {
        return tierText(t);
    }

    // Bọc chữ trong khung có chiều rộng cố định để tự xuống dòng (width tính bằng px)
    private static String wrap(String s, int width) {
        return "<html><div style='width:" + width + "px; text-align:left;'>"
                + s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                + "</div></html>";
    }

    private void err(Exception e) {
        Throwable t = e.getCause() != null ? e.getCause() : e;
        JOptionPane.showMessageDialog(this, "Không thể truy vấn SQL Server:\n" + t.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = !isEnabled() ? LINE : (getModel().isRollover() ? getBackground().darker() : getBackground());
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FB);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return b;
    }

    // Khung cuộn tự co theo chiều rộng vùng nhìn, chỉ cuộn dọc
    private static class KhungCuon extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 14;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class TheThongTin extends JPanel {
        private final Color accent;

        TheThongTin(Color accent) {
            this.accent = accent;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 18, 14, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 1;
            int h = getHeight() - 1;

            g2.setColor(getBackground() == null || getBackground().equals(new Color(0, 0, 0, 0)) ? Color.WHITE : getBackground());
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            if (accent != null) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 16, 16));
                g2.setColor(accent);
                g2.fillRect(0, 0, 5, h);
                g2.setClip(null);
            }

            g2.setColor(LINE);
            g2.drawRoundRect(0, 0, w, h, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
