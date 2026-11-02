package views;

import models.dao.PersonnelAccountDAO;
import controllers.PdfTableExport;
import controllers.AccessControl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** SQL Server backed employee and account maintenance screen. */
public class PersonnelAccountUpdatePanel extends JPanel {
    // bảng màu đồng bộ với trang Quản lý tài khoản & phân quyền
    private static final Color BLUE = new Color(11, 95, 204);
    private static final Color GREEN = new Color(34, 150, 83);
    private static final Color RED = new Color(217, 45, 32);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color TEXT = new Color(15, 29, 69);
    private static final Color MUTED = new Color(91, 112, 145);
    private static final Color BORDER = new Color(224, 233, 243);

    private final PersonnelAccountDAO dao = new PersonnelAccountDAO();
    private final DefaultTableModel model = new DefaultTableModel(null, new String[]{
            "Mã nhân viên", "Họ và tên", "Tên đăng nhập", "Quyền", "Số điện thoại", "Mức lương", "Trạng thái tài khoản"
    }) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField();
    private final JTextField id = new JTextField(), name = new JTextField(), username = new JTextField();
    private final JTextField phone = new JTextField(), salary = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JComboBox<String> role = new JComboBox<>(new String[]{"Admin", "Nha sĩ", "Lễ tân", "Y tá"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});
    private boolean editing;
    private String originalUsername;

    // bộ sắp xếp/lọc của bảng, tạo một lần dùng mãi
    private TableRowSorter<DefaultTableModel> sorter;

    // thẻ thống kê
    private final JLabel lblTotalStaff = new JLabel("0 nhân sự");
    private final JLabel lblActiveAccounts = new JLabel("0 tài khoản");
    private final JLabel lblLockedAccounts = new JLabel("0 tài khoản");

    // nhãn cho biết biểu mẫu đang thêm mới hay đang sửa
    private final JLabel lblMode = new JLabel("Đang thêm nhân sự mới");

    public PersonnelAccountUpdatePanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        add(createHeader(), BorderLayout.NORTH);

        // phần thân: thẻ thống kê ở trên, bảng + biểu mẫu ở dưới
        JPanel mainContent = new JPanel(new BorderLayout(0, 20));
        mainContent.setOpaque(false);
        mainContent.add(createStatsRow(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);
        content.add(createTablePanel(), BorderLayout.CENTER);
        content.add(createFormPanel(), BorderLayout.EAST);
        mainContent.add(content, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
        reload();
    }

    // HEADER: card ngang gradient hồng - xanh có logo
    private JPanel createHeader() {
        JPanel headerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // màu hồng pastel và xanh pastel
                Color pinkPastel = new Color(255, 232, 243);
                Color bluePastel = new Color(219, 234, 254);

                // trải màu từ trái sang phải
                g2.setPaint(new GradientPaint(0, 0, pinkPastel, getWidth(), 0, bluePastel));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // viền card
                g2.setColor(BORDER);
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

        // tải và thu nhỏ logo 45x45
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

        JLabel title = new JLabel("Cập Nhật Nhân Sự & Tài Khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT);
        if (logoIcon != null) {
            title.setIcon(logoIcon);
            title.setIconTextGap(14);
        }

        JLabel subtitle = new JLabel("Quản lý hồ sơ nhân viên, tài khoản đăng nhập và quyền truy cập");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        // căn giữa tiêu đề theo chiều dọc trong card
        JPanel westWrapper = new JPanel(new GridBagLayout());
        westWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        westWrapper.add(titleBox, gbc);
        headerCard.add(westWrapper, BorderLayout.CENTER);

        return headerCard;
    }

    // THẺ THỐNG KÊ: 3 thẻ dùng lại ảnh ok.png, yes.png, no.png
    private JPanel createStatsRow() {
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);
        statsRow.add(createStatCard("TỔNG NHÂN SỰ", lblTotalStaff, BLUE, new Color(232, 242, 255), "/img/ok.png"));
        statsRow.add(createStatCard("TÀI KHOẢN HOẠT ĐỘNG", lblActiveAccounts, GREEN, new Color(238, 247, 238), "/img/yes.png"));
        statsRow.add(createStatCard("TÀI KHOẢN TẠM KHÓA", lblLockedAccounts, RED, new Color(254, 242, 242), "/img/no.png"));
        return statsRow;
    }

    private JPanel createStatCard(String title, JLabel valueLbl, Color accentColor, Color bgCard, String imagePath) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgCard);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setLayout(new BorderLayout(18, 0));

        // ảnh bên trái
        JLabel lblIcon = new JLabel();
        try {
            java.net.URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl != null) {
                Image scaledImg = new ImageIcon(imgUrl).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                lblIcon.setIcon(new ImageIcon(scaledImg));
            } else {
                System.out.println("Không tìm thấy ảnh tại: " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        card.add(lblIcon, BorderLayout.WEST);

        // chữ bên phải: tiêu đề + số lượng
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accentColor);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLbl);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // BẢNG: card chứa thanh tìm kiếm và bảng dữ liệu
    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        // thanh công cụ: ô tìm kiếm có icon kính lúp + nút Tìm kiếm
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(340, 42));
        searchBox.setBorder(new EmptyBorder(5, 12, 5, 12));

        // icon kính lúp bên trái
        JLabel lblSearchIcon = new JLabel();
        try {
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

        // ô nhập liệu ở giữa
        search.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        search.setBorder(null);
        search.setOpaque(false);
        search.putClientProperty("JTextField.placeholderText", "Nhập mã NV, họ tên, tài khoản...");
        search.setToolTipText("Tìm theo mã nhân viên, họ tên hoặc tài khoản");
        search.addActionListener(e -> filter());
        searchBox.add(search, BorderLayout.CENTER);

        // Nút tìm kiếm
        JButton find = createButton("Tìm kiếm", BLUE);
        find.setPreferredSize(new Dimension(100, 42));
        find.addActionListener(e -> filter());

        // Nút tải lại
        JButton refresh = createButton("Tải lại", GREEN);
        refresh.setPreferredSize(new Dimension(100, 42));

        refresh.addActionListener(e -> {
            search.setText("");
            sorter.setRowFilter(null);
            reload();
            clearForm();
        });

        // Gom nhóm ô tìm kiếm và các nút
        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchGroup.setOpaque(false);

        searchGroup.add(searchBox);
        searchGroup.add(find);
        searchGroup.add(refresh);

        toolbar.add(searchGroup, BorderLayout.WEST);

        // gợi ý bên phải thanh công cụ
        JLabel hint = new JLabel("Chọn một nhân viên để xem hoặc cập nhật thông tin");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hint.setForeground(MUTED);
        toolbar.add(hint, BorderLayout.EAST);

        // bảng dữ liệu
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(table);
        PdfTableExport.install(table, "Nhân sự và tài khoản");

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        // căn giữa các cột mã, quyền, số điện thoại
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // cột lương hiển thị dạng 5.000.000 đ
        table.getColumnModel().getColumn(5).setCellRenderer(new SalaryRenderer());

        // cột trạng thái hiển thị dạng badge màu
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // FORM: card nhập / cập nhật thông tin nhân sự
    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 18, 20, 18));
        card.setPreferredSize(new Dimension(340, 0));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Thông tin nhân sự");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Nhập hoặc cập nhật dữ liệu SQL Server");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // nhãn trạng thái biểu mẫu (thêm mới / đang sửa)
        lblMode.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMode.setOpaque(true);
        lblMode.setBorder(new EmptyBorder(5, 10, 5, 10));
        lblMode.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateModeLabel();

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(10));
        heading.add(lblMode);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        addField(fields, "Mã nhân viên", id);
        addField(fields, "Họ và tên", name);
        addField(fields, "Tên đăng nhập", username);
        addField(fields, "Mật khẩu (để trống nếu giữ nguyên)", password);
        addField(fields, "Quyền tài khoản", role);
        addField(fields, "Số điện thoại", phone);
        addField(fields, "Mức lương (VNĐ)", salary);
        addField(fields, "Trạng thái tài khoản", status);
        id.setToolTipText("Mã nhân viên mới phải chưa tồn tại trong SQL Server");

        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 9));
        buttons.setOpaque(false);
        JButton save = createButton("Lưu thông tin", GREEN);
        JButton delete = createButton("Xóa nhân sự", RED);
        JButton clear = createButton("Làm mới biểu mẫu", BLUE);
        save.addActionListener(e -> save());
        delete.addActionListener(e -> delete());
        clear.addActionListener(e -> clearForm());
        buttons.add(save);
        buttons.add(delete);
        buttons.add(clear);
        if (!AccessControl.isAdministrator()) {
            save.setEnabled(false);
            delete.setEnabled(false);
        }
        card.add(heading, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel fields, String label, JComponent input) {
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 12));
        caption.setForeground(TEXT);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        input.setPreferredSize(new Dimension(280, 38));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        // ô nhập liệu có viền bo nhẹ, ô chọn (combo) để nền trắng
        if (input instanceof JTextComponent) {
            input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(4, 10, 4, 10)));
        } else {
            input.setBackground(Color.WHITE);
        }
        fields.add(caption);
        fields.add(Box.createVerticalStrut(4));
        fields.add(input);
        fields.add(Box.createVerticalStrut(10));
    }

    // nút bo góc, đổi màu khi rê chuột, xám khi bị vô hiệu hóa
    private JButton createButton(String text, Color color) {
        final boolean[] hover = {false};
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = !isEnabled() ? new Color(176, 188, 205)
                        : hover[0] ? color.darker() : color;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(100, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent event) {
                hover[0] = true;
                button.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent event) {
                hover[0] = false;
                button.repaint();
            }
        });
        return button;
    }

    // kiểu bảng giống trang quản lý tài khoản: tiêu đề xanh đậm, hàng cao
    private void styleTable(JTable table) {
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setGridColor(new Color(235, 240, 248));
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableHeader.setBackground(new Color(38, 56, 101));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(0, 46));
        tableHeader.setOpaque(true);
        tableHeader.setReorderingAllowed(false);

        // renderer tiêu đề căn giữa, chữ trắng nền xanh đậm
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(new Color(38, 56, 101));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(75, 91, 130)));
                return lbl;
            }
        });
    }

    // badge trạng thái tài khoản
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setOpaque(true);

            String st = value == null ? "" : value.toString();
            if ("Hoạt động".equals(st)) {
                lbl.setForeground(new Color(22, 130, 70));
                lbl.setBackground(new Color(225, 245, 230));
            } else if ("Tạm khóa".equals(st)) {
                lbl.setForeground(new Color(217, 45, 32));
                lbl.setBackground(new Color(253, 237, 236));
            } else {
                // trường hợp "Chưa cấp tài khoản" hoặc giá trị khác
                lbl.setForeground(MUTED);
                lbl.setBackground(new Color(240, 244, 248));
            }
            lbl.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            panel.add(lbl);
            return panel;
        }
    }

    // hiển thị mức lương có dấu phân cách hàng nghìn
    private static class SalaryRenderer extends DefaultTableCellRenderer {
        private final NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));

        SalaryRenderer() {
            format.setMaximumFractionDigits(0);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Object shown = value;
            if (value instanceof Number) {
                shown = format.format(((Number) value).doubleValue()) + " đ";
            }
            return super.getTableCellRendererComponent(table, shown, isSelected, hasFocus, row, column);
        }
    }

    // cập nhật nhãn chế độ của biểu mẫu
    private void updateModeLabel() {
        if (editing) {
            lblMode.setText("Đang chỉnh sửa: " + id.getText().trim());
            lblMode.setForeground(new Color(160, 110, 10));
            lblMode.setBackground(new Color(255, 245, 225));
        } else {
            lblMode.setText("Đang thêm nhân sự mới");
            lblMode.setForeground(BLUE);
            lblMode.setBackground(new Color(232, 242, 255));
        }
    }

    private void reload() {
        model.setRowCount(0);
        if (!AccessControl.isAdministrator()) {
            updateStats();
            showError("Chỉ tài khoản admin mới được xem và quản lý nhân sự/tài khoản.");
            return;
        }
        try {
            List<Object[]> rows = dao.getAll();
            for (Object[] row : rows) model.addRow(row);
        } catch (SQLException ex) {
            showError("Không tải được dữ liệu từ SQL Server: " + ex.getMessage());
        }
        updateStats();
    }

    // đếm lại số liệu cho 3 thẻ thống kê
    private void updateStats() {
        int total = model.getRowCount();
        int active = 0;
        int locked = 0;
        for (int i = 0; i < total; i++) {
            String st = String.valueOf(model.getValueAt(i, 6));
            if ("Hoạt động".equals(st)) active++;
            else if ("Tạm khóa".equals(st)) locked++;
        }
        lblTotalStaff.setText(total + " nhân sự");
        lblActiveAccounts.setText(active + " tài khoản");
        lblLockedAccounts.setText(locked + " tài khoản");
    }

    private void filter() {
        String keyword = search.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        editing = true;
        id.setText(String.valueOf(model.getValueAt(row, 0)));
        name.setText(String.valueOf(model.getValueAt(row, 1)));
        originalUsername = (String) model.getValueAt(row, 2);
        username.setText(originalUsername == null ? "" : originalUsername);
        role.setSelectedItem(model.getValueAt(row, 3));
        phone.setText(model.getValueAt(row, 4) == null ? "" : String.valueOf(model.getValueAt(row, 4)));
        salary.setText(model.getValueAt(row, 5) == null ? "" : String.valueOf(model.getValueAt(row, 5)));
        String accountStatus = String.valueOf(model.getValueAt(row, 6));
        status.setSelectedItem("Chưa cấp tài khoản".equals(accountStatus) ? "Hoạt động" : accountStatus);
        password.setText("");
        id.setEditable(false);
        boolean protectedAccount = AccessControl.isProtectedAdministrator(originalUsername, String.valueOf(model.getValueAt(row, 3)));
        username.setEditable(!protectedAccount);
        role.setEnabled(!protectedAccount);
        status.setEnabled(!protectedAccount);
        updateModeLabel();
    }

    private void save() {
        try {
            double amount = salary.getText().trim().isEmpty() ? 0 : Double.parseDouble(salary.getText().trim().replace(",", ""));
            dao.save(id.getText().trim(), name.getText().trim(), username.getText().trim(), new String(password.getPassword()),
                    (String) role.getSelectedItem(), phone.getText().trim(), amount,
                    "Hoạt động".equals(status.getSelectedItem()), editing, originalUsername);
            JOptionPane.showMessageDialog(this, "Đã lưu nhân sự và tài khoản vào SQL Server.");
            reload();
            clearForm();
        } catch (NumberFormatException ex) {
            showError("Mức lương phải là số hợp lệ.");
        } catch (SQLException ex) {
            showError("Không thể lưu vào SQL Server: " + ex.getMessage());
        }
    }

    private void delete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { showError("Chọn nhân viên cần xóa."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        String staffId = String.valueOf(model.getValueAt(row, 0));
        String linkedUsername = (String) model.getValueAt(row, 2);
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + staffId + " và tài khoản liên kết khỏi SQL Server?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(staffId, linkedUsername);
            JOptionPane.showMessageDialog(this, "Đã xóa dữ liệu khỏi SQL Server.");
            reload();
            clearForm();
        } catch (SQLException ex) {
            showError("Không thể xóa: " + ex.getMessage());
        }
    }

    private void clearForm() {
        editing = false;
        originalUsername = null;
        for (JTextField field : new JTextField[]{id, name, username, phone, salary}) field.setText("");
        password.setText("");
        id.setEditable(true);
        username.setEditable(true);
        role.setEnabled(true);
        status.setEnabled(true);
        role.setSelectedIndex(0);
        status.setSelectedIndex(0);
        table.clearSelection();
        updateModeLabel();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}