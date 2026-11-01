package views;

import datepicker.DatePickerField;
import models.dao.KhuyenMaiDAO;
import models.entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class PromotionPanel extends JPanel {

    // ==================== COLORS & STYLES ====================
    private static final Color BLUE_PRIMARY = new Color(13, 102, 218);
    private static final Color BLUE_LIGHT = new Color(230, 240, 255);
    private static final Color GREEN_ACCENT = new Color(22, 130, 60);
    private static final Color GREEN_LIGHT = new Color(225, 247, 235);
    private static final Color RED_ACCENT = new Color(190, 38, 48);
    private static final Color RED_LIGHT = new Color(255, 232, 235);

    private static final Color BG_CANVAS = new Color(245, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(25, 42, 68);
    private static final Color TEXT_GRAY = new Color(105, 120, 140);
    private static final Color BORDER_LIGHT = new Color(218, 226, 238);

    private final KhuyenMaiDAO promotionDAO = new KhuyenMaiDAO();

    // ==================== FORM FIELDS ====================
    private final JTextField txtSearch = new JTextField();
    private final JTextField txtId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JTextField txtDiscount = new JTextField();
    private final DatePickerField dateFrom = new DatePickerField();
    private final DatePickerField dateTo = new DatePickerField();
    private final JCheckBox chkActive = new JCheckBox("Đang áp dụng", true);

    // ==================== CONTAINERS ====================
    private JPanel gridCardContainer;
    private JLabel lblTotalCount;
    private JLabel lblStatActiveCount, lblStatUpcomingCount, lblStatExpiredCount;
    
    private List<KhuyenMai> allPromotionsList;
    private boolean editing = false;

    public PromotionPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.add(createHeaderPanel());
        topPanel.add(Box.createVerticalStrut(16));
        topPanel.add(createStatisticsPanel());
        topPanel.add(Box.createVerticalStrut(16));
        topPanel.add(createFilterPanel());

        add(topPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createCardGridArea(), BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        reload();
    }

    // ==================== HEADER ====================
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Quản Lý Chương Trình Khuyến Mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Thêm, cập nhật, xóa và theo dõi chương trình khuyến mãi trực tiếp trong cơ sở dữ liệu");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(TEXT_GRAY);

        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }

    // ==================== STATISTICS ====================
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 18, 0));
        panel.setOpaque(false);

        JPanel activeCard = createStatCard("Đang diễn ra", "0", BLUE_PRIMARY, BLUE_LIGHT, "⚡");
        JPanel upcomingCard = createStatCard("Sắp diễn ra", "0", GREEN_ACCENT, GREEN_LIGHT, "⏳");
        JPanel expiredCard = createStatCard("Đã kết thúc", "0", RED_ACCENT, RED_LIGHT, "✔");

        lblStatActiveCount = (JLabel) activeCard.getClientProperty("countLabel");
        lblStatUpcomingCount = (JLabel) upcomingCard.getClientProperty("countLabel");
        lblStatExpiredCount = (JLabel) expiredCard.getClientProperty("countLabel");

        panel.add(activeCard);
        panel.add(upcomingCard);
        panel.add(expiredCard);

        return panel;
    }

    private JPanel createStatCard(String title, String number, Color accent, Color lightColor, String symbol) {
        RoundedPanel card = new RoundedPanel(14, CARD_BG);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(18, 22, 18, 22));
        card.setPreferredSize(new Dimension(220, 95));

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(true);
        iconPanel.setBackground(lightColor);
        iconPanel.setPreferredSize(new Dimension(50, 50));

        JLabel icon = new JLabel(symbol);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(accent);
        iconPanel.add(icon);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_GRAY);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblNumber.setForeground(TEXT_DARK);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblNumber);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        card.putClientProperty("countLabel", lblNumber);

        return card;
    }

    // ==================== FILTER & FORM PANEL ====================
    private JPanel createFilterPanel() {
        RoundedPanel panel = new RoundedPanel(14, CARD_BG);
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblFilterTitle = new JLabel("Thông Tin Chi Tiết & Bộ Lọc Tìm Kiếm");
        lblFilterTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFilterTitle.setForeground(TEXT_DARK);
        panel.add(lblFilterTitle, BorderLayout.NORTH);

        JPanel centerForm = new JPanel(new GridBagLayout());
        centerForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 0.25;

        // Dòng 1
        gbc.gridy = 0;
        gbc.gridx = 0; centerForm.add(createInputGroup("Mã khuyến mãi", txtId), gbc);
        gbc.gridx = 1; centerForm.add(createInputGroup("Tên chương trình", txtName), gbc);
        gbc.gridx = 2; centerForm.add(createInputGroup("Mức giảm (%)", txtDiscount), gbc);
        
        gbc.gridx = 3;
        JPanel statusBox = new JPanel(new BorderLayout());
        statusBox.setOpaque(false);
        statusBox.add(createFieldLabel("Trạng thái"), BorderLayout.NORTH);
        chkActive.setOpaque(false);
        chkActive.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusBox.add(chkActive, BorderLayout.CENTER);
        centerForm.add(statusBox, gbc);

        // Dòng 2
        gbc.gridy = 1;
        gbc.gridx = 0; centerForm.add(createInputGroup("Ngày bắt đầu", dateFrom), gbc);
        gbc.gridx = 1; centerForm.add(createInputGroup("Ngày kết thúc", dateTo), gbc);
        gbc.gridx = 2; centerForm.add(createInputGroup("Tìm kiếm nhanh (Mã/Tên)", txtSearch), gbc);

        // Cụm nút bấm to rõ, cân đối
        gbc.gridx = 3;
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionButtons.setOpaque(false);

        JButton btnSearch = createButton("Tìm", BLUE_PRIMARY, 75, 42);
        JButton btnClear = createButton("Làm mới", new Color(100, 115, 130), 90, 42);
        JButton btnDelete = createButton("Xóa", RED_ACCENT, 70, 42);
        JButton btnSave = createButton("Lưu / Thêm", GREEN_ACCENT, 110, 42);

        btnSearch.addActionListener(e -> applyFilter());
        btnClear.addActionListener(e -> clearForm());
        btnDelete.addActionListener(e -> deletePromotion());
        btnSave.addActionListener(e -> savePromotion());

        actionButtons.add(btnSearch);
        actionButtons.add(btnClear);
        actionButtons.add(btnDelete);
        actionButtons.add(btnSave);

        centerForm.add(actionButtons, gbc);

        panel.add(centerForm, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInputGroup(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(createFieldLabel(label), BorderLayout.NORTH);
        if (comp instanceof JTextField && !(comp instanceof DatePickerField)) {
            JTextField tf = (JTextField) comp;
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 42));
            tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_LIGHT), new EmptyBorder(8, 12, 8, 12)));
        }
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    // ==================== CARD GRID CONTAINER ====================
    private JPanel createCardGridArea() {
        RoundedPanel panel = new RoundedPanel(14, CARD_BG);
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh Sách Chương Trình Khuyến Mãi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_DARK);
        headerRow.add(lblTitle, BorderLayout.WEST);

        lblTotalCount = new JLabel("Tổng cộng: 0 chương trình");
        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCount.setForeground(TEXT_GRAY);
        headerRow.add(lblTotalCount, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);

        gridCardContainer = new JPanel(new GridLayout(0, 3, 18, 18));
        gridCardContainer.setOpaque(false);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(gridCardContainer, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scroll.getViewport().setBackground(BG_CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ==================== TẠO THẺ CARD KHUYẾN MÃI (PHÓNG TO TOÀN DIỆN) ====================
    private JPanel createPromotionCard(KhuyenMai km) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(0, 165)); // Phóng to chiều cao card
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Top: Mã & Tên + Badge trạng thái
        JPanel topBox = new JPanel(new BorderLayout(10, 0));
        topBox.setOpaque(false);

        JLabel lblIdName = new JLabel("<html><b>" + km.getMaKhuyenMai() + "</b> - " + km.getTenKhuyenMai() + "</html>");
        lblIdName.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Phóng to chữ tiêu đề card
        lblIdName.setForeground(TEXT_DARK);
        topBox.add(lblIdName, BorderLayout.CENTER);

        boolean active = Boolean.TRUE.equals(km.getTrangThai());
        JLabel badge = new JLabel(active ? "Đang áp dụng" : "Ngừng áp dụng");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Phóng to chữ badge
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        if (active) {
            badge.setBackground(GREEN_LIGHT);
            badge.setForeground(GREEN_ACCENT);
        } else {
            badge.setBackground(RED_LIGHT);
            badge.setForeground(RED_ACCENT);
        }
        topBox.add(badge, BorderLayout.EAST);
        card.add(topBox, BorderLayout.NORTH);

        // Center: Thời gian & Mức giảm (Phóng to cỡ chữ)
        JPanel centerBox = new JPanel();
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setOpaque(false);

        String dateStr = "Thời gian: " + (km.getNgayBatDau() != null ? km.getNgayBatDau().toString() : "") + 
                         " → " + (km.getNgayKetThuc() != null ? km.getNgayKetThuc().toString() : "");
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDate.setForeground(TEXT_GRAY);

        JLabel lblDiscount = new JLabel("Mức giảm: " + km.getGiaTriGiam() + "% tổng dịch vụ");
        lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Chữ mức giảm to đậm hơn
        lblDiscount.setForeground(BLUE_PRIMARY);

        centerBox.add(Box.createVerticalStrut(6));
        centerBox.add(lblDate);
        centerBox.add(Box.createVerticalStrut(6));
        centerBox.add(lblDiscount);
        card.add(centerBox, BorderLayout.CENTER);

        // Bottom: Chỉnh sửa nhanh
        JPanel bottomBox = new JPanel(new BorderLayout());
        bottomBox.setOpaque(false);

        JLabel lblAction = new JLabel("Chỉnh sửa thông tin ➔");
        lblAction.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAction.setForeground(BLUE_PRIMARY);
        bottomBox.add(lblAction, BorderLayout.WEST);

        card.add(bottomBox, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loadSelection(km);
            }
        });

        return card;
    }

    // ==================== LOGIC XỬ LÝ ====================
    private void reload() {
        try {
            allPromotionsList = promotionDAO.getAll();
            renderCards(allPromotionsList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderCards(List<KhuyenMai> list) {
        gridCardContainer.removeAll();
        int activeCount = 0;
        int upcomingCount = 0;
        int expiredCount = 0;

        LocalDate today = LocalDate.now();

        for (KhuyenMai km : list) {
            gridCardContainer.add(createPromotionCard(km));

            if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null) {
                LocalDate start = new java.sql.Date(km.getNgayBatDau().getTime()).toLocalDate();
                LocalDate end = new java.sql.Date(km.getNgayKetThuc().getTime()).toLocalDate();

                if (today.isBefore(start)) {
                    upcomingCount++;
                } else if (!today.isAfter(end)) {
                    activeCount++;
                } else {
                    expiredCount++;
                }
            }
        }

        lblStatActiveCount.setText(String.valueOf(activeCount));
        lblStatUpcomingCount.setText(String.valueOf(upcomingCount));
        lblStatExpiredCount.setText(String.valueOf(expiredCount));
        lblTotalCount.setText("Tổng cộng: " + list.size() + " chương trình");

        gridCardContainer.revalidate();
        gridCardContainer.repaint();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (allPromotionsList == null) return;

        if (keyword.isEmpty()) {
            renderCards(allPromotionsList);
            return;
        }

        List<KhuyenMai> filtered = allPromotionsList.stream()
                .filter(km -> km.getMaKhuyenMai().toLowerCase().contains(keyword) || 
                              km.getTenKhuyenMai().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        renderCards(filtered);
    }

    private void savePromotion() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String discountText = txtDiscount.getText().trim().replace("%", "").replace(",", ".");

        if (id.isEmpty() || name.isEmpty()) {
            warn("Vui lòng nhập Mã khuyến mãi và Tên chương trình!");
            return;
        }

        try {
            LocalDate from = parseDate(dateFrom, "Ngày bắt đầu");
            LocalDate to = parseDate(dateTo, "Ngày kết thúc");
            if (to.isBefore(from)) {
                warn("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
                return;
            }

            double discount = Double.parseDouble(discountText);
            if (discount < 0 || discount > 100) {
                warn("Mức giảm phải là số từ 0 đến 100.");
                return;
            }

            KhuyenMai promotion = new KhuyenMai();
            promotion.setMaKhuyenMai(id);
            promotion.setTenKhuyenMai(name);
            promotion.setGiaTriGiam(discount);
            promotion.setNgayBatDau(java.sql.Date.valueOf(from));
            promotion.setNgayKetThuc(java.sql.Date.valueOf(to));
            promotion.setTrangThai(chkActive.isSelected());

            boolean wasEditing = editing;
            boolean saved = wasEditing ? promotionDAO.update(promotion) : promotionDAO.insert(promotion);

            if (!saved) {
                throw new SQLException("SQL Server không lưu được thay đổi.");
            }

            clearForm();
            reload();
            info(wasEditing ? "Đã cập nhật chương trình khuyến mãi thành công!" : "Đã thêm chương trình khuyến mãi mới!");

        } catch (NumberFormatException ex) {
            warn("Mức giảm giá không hợp lệ (phải là số).");
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            warn(ex.getMessage() != null ? ex.getMessage() : "Vui lòng kiểm tra lại định dạng ngày tháng (dd/MM/yyyy).");
        } catch (SQLException ex) {
            warn("Lỗi cơ sở dữ liệu: " + ex.getMessage());
        }
    }

    private LocalDate parseDate(DatePickerField field, String label) {
        if (!field.isValidDate()) throw new IllegalArgumentException(label + " không hợp lệ.");
        LocalDate date = parseCustomDate(field.getText());
        if (date == null) throw new IllegalArgumentException("Vui lòng chọn " + label.toLowerCase() + ".");
        return date;
    }

    private LocalDate parseCustomDate(String text) {
        try {
            if (text == null || text.trim().isEmpty()) return null;
            String[] parts = text.split("/");
            if (parts.length == 3) {
                return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void deletePromotion() {
        if (!editing || txtId.getText().trim().isEmpty()) {
            warn("Vui lòng chọn chương trình cần xóa từ danh sách thẻ bên dưới.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa chương trình: " + txtId.getText().trim() + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (!promotionDAO.delete(txtId.getText().trim())) {
                throw new SQLException("Không tìm thấy mã chương trình trong CSDL.");
            }
            clearForm();
            reload();
            info("Đã xóa chương trình khuyến mãi thành công.");
        } catch (SQLException ex) {
            warn("Không thể xóa chương trình: " + ex.getMessage());
        }
    }

    private void loadSelection(KhuyenMai km) {
        txtId.setText(km.getMaKhuyenMai());
        txtId.setEnabled(false);
        txtName.setText(km.getTenKhuyenMai());
        txtDiscount.setText(String.valueOf(km.getGiaTriGiam()));
        
        if (km.getNgayBatDau() != null) {
            dateFrom.setText(new java.sql.Date(km.getNgayBatDau().getTime()).toLocalDate().toString());
        }
        if (km.getNgayKetThuc() != null) {
            dateTo.setText(new java.sql.Date(km.getNgayKetThuc().getTime()).toLocalDate().toString());
        }
        chkActive.setSelected(Boolean.TRUE.equals(km.getTrangThai()));
        editing = true;
    }

    private void clearForm() {
        editing = false;
        txtId.setEnabled(true);
        txtId.setText("");
        txtName.setText("");
        txtDiscount.setText("");
        dateFrom.setText(LocalDate.now().toString());
        dateTo.setText(LocalDate.now().plusMonths(1).toString());
        chkActive.setSelected(true);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createButton(String text, Color background, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? background.darker() : (getModel().isRollover() ? background.brighter() : background);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Chữ trên nút to hơn
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        public RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(BORDER_LIGHT);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}