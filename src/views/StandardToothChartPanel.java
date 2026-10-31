
package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.ListSelectionEvent;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StandardToothChartPanel extends JPanel {

    // ================= MÀU SẮC =================
    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color NAVY = new Color(15, 29, 69);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color BORDER = new Color(210, 220, 235);
    private static final Color TEXT = new Color(15, 29, 69);
    private static final Color MUTED = new Color(100, 110, 130);
    private static final Color PASTEL_BLUE_BG = new Color(230, 242, 255);
    private static final Color DARK_PINK_TEXT = new Color(214, 40, 108);

    // ================= DỮ LIỆU =================
    private final List<ToothData> teeth = new ArrayList<>();

    private JComboBox<String> cbJaw, cbPosition, cbType;
    private JTextField txtSearch;

    private JTable toothTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel lblBigToothAvatar;
    private JLabel lblHeaderCodeBadge;
    private JLabel lblHeaderName;

    private JPanel badgePanelType;

    private JLabel lblHeaderType;
    private JLabel lblCode;
    private JLabel lblName;
    private JLabel lblJaw;
    private JLabel lblPosition;
    private JLabel lblType;
    private JLabel lblDescription;
    private JLabel lblDescExtra;
    private JLabel lblSelected;

    private ToothImagePanel imagePanel;
    private ToothData selectedTooth;

    // Nhận dữ liệu răng được chọn từ màn hình khác
    private Consumer<ToothData> toothSelectionListener;

    // ================= CONSTRUCTOR =================
    public StandardToothChartPanel() {

        setLayout(new BorderLayout(0, 14));
        setBackground(BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        loadSampleTeethForDesign();

        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);

        content.add(createFilterCard(), BorderLayout.NORTH);

        JPanel main = new JPanel(new ThreeCardLayout(12));
        main.setOpaque(false);

        main.add(createChartCard());
        main.add(createDetailCard());
        main.add(createTableCard());

        content.add(main, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        applyFilter();
    }

    // ================= THREE CARD LAYOUT =================
    private static class ThreeCardLayout implements LayoutManager {

        private final int gap;

        public ThreeCardLayout(int gap) {
            this.gap = gap;
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(900, 500);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(300, 250);
        }

        @Override
        public void layoutContainer(Container parent) {

            synchronized (parent.getTreeLock()) {

                Insets insets = parent.getInsets();

                int totalWidth = parent.getWidth()
                        - insets.left
                        - insets.right
                        - gap * 2;

                int totalHeight = parent.getHeight()
                        - insets.top
                        - insets.bottom;

                if (parent.getComponentCount() != 3) {
                    return;
                }

                int width1 = (int) (totalWidth * 0.30);
                int width2 = (int) (totalWidth * 0.30);
                int width3 = totalWidth - width1 - width2;

                int x = insets.left;

                parent.getComponent(0).setBounds(
                        x, insets.top, width1, totalHeight
                );

                x += width1 + gap;

                parent.getComponent(1).setBounds(
                        x, insets.top, width2, totalHeight
                );

                x += width2 + gap;

                parent.getComponent(2).setBounds(
                        x, insets.top, width3, totalHeight
                );
            }
        }
    }

    // ================= HEADER =================
    private JPanel createHeader() {

        JPanel card = createCard();

        card.setPreferredSize(new Dimension(0, 100));
        card.setMinimumSize(new Dimension(0, 100));

        JPanel left = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );

        left.setOpaque(false);

        java.net.URL imgUrl =
                getClass().getResource("/img/logo2.png");

        JLabel icon;

        if (imgUrl != null) {

            ImageIcon originalIcon = new ImageIcon(imgUrl);

            Image scaledImage = originalIcon.getImage()
                    .getScaledInstance(
                            75, 75, Image.SCALE_SMOOTH
                    );

            icon = new JLabel(new ImageIcon(scaledImage));

        } else {

            icon = new JLabel("🦷");
        }

        JPanel texts = new JPanel();

        texts.setOpaque(false);

        texts.setLayout(
                new BoxLayout(texts, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Sơ đồ răng chuẩn");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        title.setForeground(
                new Color(53, 121, 168)
        );

        JLabel subtitle = new JLabel(
                "Dữ liệu răng chuẩn dùng chung cho toàn hệ thống - Hệ thống FDI"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 15)
        );

        subtitle.setForeground(MUTED);

        texts.add(title);
        texts.add(Box.createVerticalStrut(4));
        texts.add(subtitle);

        left.add(icon);
        left.add(texts);

        card.add(left, BorderLayout.CENTER);

        return card;
    }

    // ================= FILTER CARD =================
    private JPanel createFilterCard() {

        JPanel card = createCard();

        JPanel filters = new JPanel(
                new BorderLayout(15, 0)
        );

        filters.setOpaque(false);

        cbJaw = new JComboBox<>(
                new String[]{
                    "Tất cả",
                    "Hàm trên",
                    "Hàm dưới"
                }
        );

        cbPosition = new JComboBox<>(
                new String[]{
                    "Tất cả",
                    "Bên phải",
                    "Bên trái"
                }
        );

        cbType = new JComboBox<>(
                new String[]{
                    "Tất cả",
                    "Răng cửa",
                    "Răng nanh",
                    "Răng tiền hàm",
                    "Răng hàm"
                }
        );

        styleCombo(cbJaw);
        styleCombo(cbPosition);
        styleCombo(cbType);

        txtSearch = new JTextField() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                if (getText().isEmpty() && !isFocusOwner()) {

                    Graphics2D g2 =
                            (Graphics2D) g.create();

                    g2.setRenderingHint(
                            RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON
                    );

                    g2.setColor(MUTED);
                    g2.setFont(getFont());

                    FontMetrics fm = g2.getFontMetrics();

                    g2.drawString(
                            "Tìm mã răng, tên răng...",
                            32,
                            (getHeight() + fm.getAscent()
                                    - fm.getDescent()) / 2
                    );

                    g2.dispose();
                }
            }
        };

        txtSearch.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        txtSearch.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 8, 0, 8)
                )
        );

        JButton btnRefresh = new JButton("  Làm mới");

        java.net.URL refreshImgUrl =
                getClass().getResource("/img/refresh.png");

        if (refreshImgUrl != null) {

            ImageIcon icon = new ImageIcon(refreshImgUrl);

            Image scaled = icon.getImage()
                    .getScaledInstance(
                            18, 18, Image.SCALE_SMOOTH
                    );

            btnRefresh.setIcon(new ImageIcon(scaled));

        } else {

            btnRefresh.setText("↻ Làm mới");
        }

        styleRefreshButton(btnRefresh);

        int jawWidth = 140;
        int positionWidth = 140;
        int typeWidth = 160;
        int panelHeight = 60;

        JPanel jawPanel =
                createLabeledField("Loại hàm", cbJaw);

        JPanel positionPanel =
                createLabeledField("Vị trí", cbPosition);

        JPanel typePanel =
                createLabeledField("Phân loại", cbType);

        jawPanel.setPreferredSize(
                new Dimension(jawWidth, panelHeight)
        );

        positionPanel.setPreferredSize(
                new Dimension(positionWidth, panelHeight)
        );

        typePanel.setPreferredSize(
                new Dimension(typeWidth, panelHeight)
        );

        JPanel comboPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 25, 0)
        );

        comboPanel.setOpaque(false);

        comboPanel.add(jawPanel);
        comboPanel.add(positionPanel);
        comboPanel.add(typePanel);

        JPanel searchPanel =
                createSearchFieldWithIcon(txtSearch);

        searchPanel.setPreferredSize(
                new Dimension(250, 60)
        );

        searchPanel.setMinimumSize(
                new Dimension(250, 60)
        );

        JPanel searchContainer = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        searchContainer.setOpaque(false);
        searchContainer.add(searchPanel);

        JPanel refreshPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 12)
        );

        refreshPanel.setOpaque(false);

        refreshPanel.setPreferredSize(
                new Dimension(200, 70)
        );

        refreshPanel.add(btnRefresh);

        filters.add(comboPanel, BorderLayout.WEST);
        filters.add(searchContainer, BorderLayout.CENTER);
        filters.add(refreshPanel, BorderLayout.EAST);

        card.add(filters, BorderLayout.CENTER);

        // Lọc theo hàm
        cbJaw.addActionListener(e -> applyFilter());

        // Lọc theo vị trí
        cbPosition.addActionListener(e -> applyFilter());

        // Lọc theo phân loại
        cbType.addActionListener(e -> applyFilter());

        // Tìm kiếm
        txtSearch.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        // Làm mới
        btnRefresh.addActionListener(e -> {

            cbJaw.setSelectedIndex(0);
            cbPosition.setSelectedIndex(0);
            cbType.setSelectedIndex(0);

            txtSearch.setText("");

            selectTooth(null);

            applyFilter();
        });

        return card;
    }

    // ================= LABELED FIELD =================
    private JPanel createLabeledField(
            String title,
            JComponent field) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 6)
        );

        panel.setOpaque(false);

        JLabel label = new JLabel(title);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        label.setForeground(NAVY);

        field.setPreferredSize(
                new Dimension(100, 36)
        );

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    // ================= SEARCH FIELD =================
    private JPanel createSearchFieldWithIcon(
            JTextField textField) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 6)
        );

        panel.setOpaque(false);

        JLabel label = new JLabel("Tìm kiếm");

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        label.setForeground(NAVY);

        JPanel searchWrapper = new JPanel(
                new BorderLayout(5, 0)
        );

        searchWrapper.setBackground(Color.WHITE);

        searchWrapper.setPreferredSize(
                new Dimension(200, 36)
        );

        searchWrapper.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(0, 8, 0, 8)
                )
        );

        java.net.URL searchImgUrl =
                getClass().getResource("/img/search.png");

        JLabel searchIconLabel;

        if (searchImgUrl != null) {

            ImageIcon icon = new ImageIcon(searchImgUrl);

            Image scaled = icon.getImage()
                    .getScaledInstance(
                            16, 16, Image.SCALE_SMOOTH
                    );

            searchIconLabel =
                    new JLabel(new ImageIcon(scaled));

        } else {

            searchIconLabel = new JLabel("⌕");

            searchIconLabel.setFont(
                    new Font("Segoe UI", Font.PLAIN, 20)
            );
        }

        textField.setBorder(
                new EmptyBorder(0, 0, 0, 0)
        );

        textField.setOpaque(false);

        searchWrapper.add(
                searchIconLabel,
                BorderLayout.WEST
        );

        searchWrapper.add(
                textField,
                BorderLayout.CENTER
        );

        panel.add(label, BorderLayout.NORTH);
        panel.add(searchWrapper, BorderLayout.CENTER);

        return panel;
    }

    // ================= REFRESH BUTTON =================
    private void styleRefreshButton(JButton button) {

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setPreferredSize(
                new Dimension(125, 36)
        );

        button.setBackground(Color.WHITE);
        button.setForeground(NAVY);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(4, 12, 4, 12)
                )
        );
    }

    // ================= CHART CARD =================
    private JPanel createChartCard() {

        JPanel card = createCard();

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setOpaque(false);

        JLabel title = new JLabel(
                "Sơ đồ răng vĩnh viễn"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        title.setForeground(NAVY);

        JLabel standard = new JLabel(
                "FDI / ISO 3950"
        );

        standard.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        standard.setForeground(BLUE);

        header.add(title, BorderLayout.WEST);
        header.add(standard, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        imagePanel = new ToothImagePanel();

        imagePanel.setPreferredSize(
                new Dimension(420, 560)
        );

        imagePanel.setMinimumSize(
                new Dimension(250, 300)
        );

        card.add(imagePanel, BorderLayout.CENTER);

        lblSelected = new JLabel(
                "Bấm vào sơ đồ để chọn răng."
        );

        lblSelected.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        lblSelected.setForeground(MUTED);

        card.add(lblSelected, BorderLayout.SOUTH);

        return card;
    }

    // ================= TOOTH IMAGE PANEL =================
    private class ToothImagePanel extends JPanel {

        private Image image;

        private int imgX = 0;
        private int imgY = 0;
        private int imgW = 0;
        private int imgH = 0;

        ToothImagePanel() {

            setBackground(Color.WHITE);
            setOpaque(true);

            java.net.URL url =
                    getClass().getResource("/img/sodorang.png");

            if (url != null) {

                image = new ImageIcon(url).getImage();

            } else {

                System.err.println(
                        "Không tìm thấy ảnh: /img/sodorang.png"
                );
            }

            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    ToothData tooth = getToothAt(
                            e.getX(),
                            e.getY()
                    );

                    if (tooth != null) {
                        selectTooth(tooth);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            if (image != null) {

                int iw = image.getWidth(this);
                int ih = image.getHeight(this);

                if (iw > 0 && ih > 0) {

                    double scale = Math.min(
                            (double) getWidth() / iw,
                            (double) getHeight() / ih
                    );

                    imgW = (int) (iw * scale);
                    imgH = (int) (ih * scale);

                    imgX = (getWidth() - imgW) / 2;
                    imgY = (getHeight() - imgH) / 2;

                    g2.drawImage(
                            image,
                            imgX,
                            imgY,
                            imgW,
                            imgH,
                            this
                    );
                }

            } else {

                g2.setColor(
                        new Color(245, 248, 255)
                );

                g2.fillRoundRect(
                        10,
                        10,
                        getWidth() - 20,
                        getHeight() - 20,
                        18,
                        18
                );

                g2.setColor(MUTED);

                String message =
                        "Không tìm thấy /img/sodorang.png";

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        message,
                        Math.max(
                                10,
                                (getWidth() - fm.stringWidth(message)) / 2
                        ),
                        getHeight() / 2
                );
            }

            // Vẽ vòng tròn tại răng được chọn
            if (selectedTooth != null
                    && imgW > 0
                    && imgH > 0) {

                Point2D p =
                        getApproximateToothPoint(
                                selectedTooth.code
                        );

                if (p != null) {

                    int x = imgX
                            + (int) (p.getX() * imgW);

                    int y = imgY
                            + (int) (p.getY() * imgH);

                    g2.setColor(BLUE);

                    g2.setStroke(
                            new BasicStroke(2.5f)
                    );

                    g2.drawOval(
                            x - 12,
                            y - 12,
                            24,
                            24
                    );
                }
            }

            g2.dispose();
        }

        // Tìm răng gần vị trí click nhất
        private ToothData getToothAt(int x, int y) {

            if (teeth.isEmpty()
                    || imgW <= 0
                    || imgH <= 0) {

                return null;
            }

            ToothData nearest = null;

            double nearestDistance =
                    Double.MAX_VALUE;

            for (ToothData tooth : teeth) {

                Point2D p =
                        getApproximateToothPoint(
                                tooth.code
                        );

                if (p == null) continue;

                double px = imgX + p.getX() * imgW;
                double py = imgY + p.getY() * imgH;

                double distance =
                        Point2D.distance(
                                x, y, px, py
                        );

                if (distance < nearestDistance) {

                    nearestDistance = distance;
                    nearest = tooth;
                }
            }

            return nearestDistance <= 35.0
                    ? nearest
                    : null;
        }
    }

    // ================= VỊ TRÍ RĂNG FDI =================
    private Point2D getApproximateToothPoint(int code) {

        switch (code) {

            case 18: return new Point2D.Double(0.1514, 0.4459);
            case 17: return new Point2D.Double(0.1602, 0.3913);
            case 16: return new Point2D.Double(0.1729, 0.3359);
            case 15: return new Point2D.Double(0.2002, 0.2780);
            case 14: return new Point2D.Double(0.2383, 0.2285);
            case 13: return new Point2D.Double(0.2939, 0.1797);
            case 12: return new Point2D.Double(0.3623, 0.1426);
            case 11: return new Point2D.Double(0.4492, 0.1172);

            case 21: return new Point2D.Double(0.5527, 0.1172);
            case 22: return new Point2D.Double(0.6387, 0.1426);
            case 23: return new Point2D.Double(0.7061, 0.1797);
            case 24: return new Point2D.Double(0.7617, 0.2285);
            case 25: return new Point2D.Double(0.7979, 0.2780);
            case 26: return new Point2D.Double(0.8252, 0.3359);
            case 27: return new Point2D.Double(0.8379, 0.3913);
            case 28: return new Point2D.Double(0.8467, 0.4459);

            case 48: return new Point2D.Double(0.1729, 0.5514);
            case 47: return new Point2D.Double(0.1865, 0.6055);
            case 46: return new Point2D.Double(0.2100, 0.6576);
            case 45: return new Point2D.Double(0.2402, 0.7064);
            case 44: return new Point2D.Double(0.2783, 0.7500);
            case 43: return new Point2D.Double(0.3232, 0.7904);
            case 42: return new Point2D.Double(0.3760, 0.8223);
            case 41: return new Point2D.Double(0.4531, 0.8392);

            case 31: return new Point2D.Double(0.5469, 0.8392);
            case 32: return new Point2D.Double(0.6221, 0.8223);
            case 33: return new Point2D.Double(0.6768, 0.7904);
            case 34: return new Point2D.Double(0.7217, 0.7500);
            case 35: return new Point2D.Double(0.7598, 0.7064);
            case 36: return new Point2D.Double(0.7891, 0.6576);
            case 37: return new Point2D.Double(0.8125, 0.6055);
            case 38: return new Point2D.Double(0.8271, 0.5514);
        }

        return null;
    }

    // ================= DÙNG SƠ ĐỒ TRONG KHÁM BỆNH =================
    public JPanel createClinicalChartCard(
            Consumer<ToothData> listener) {

        this.toothSelectionListener = listener;

        return createChartCard();
    }

    // ================= DETAIL CARD =================
    private JPanel createDetailCard() {

        JPanel card = createCard();

        card.setLayout(
                new BorderLayout(0, 12)
        );

        JLabel title = new JLabel(
                "Thông tin răng"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        title.setForeground(NAVY);

        JPanel titlePanel = new JPanel(
                new BorderLayout()
        );

        titlePanel.setOpaque(false);

        titlePanel.add(title, BorderLayout.WEST);

        card.add(titlePanel, BorderLayout.NORTH);

        JPanel body = new JPanel();

        body.setOpaque(false);

        body.setLayout(
                new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        // Thông tin đầu
        JPanel topInfoPanel = new JPanel(
                new BorderLayout(14, 0)
        );

        topInfoPanel.setOpaque(false);

        topInfoPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        topInfoPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 90)
        );

        java.net.URL toothUrl =
                getClass().getResource("/img/iconrang.png");

        if (toothUrl != null) {

            ImageIcon icon = new ImageIcon(toothUrl);

            Image scaledImage = icon.getImage()
                    .getScaledInstance(
                            80, 80, Image.SCALE_SMOOTH
                    );

            lblBigToothAvatar =
                    new JLabel(new ImageIcon(scaledImage));

        } else {

            lblBigToothAvatar = new JLabel("🦷");

            lblBigToothAvatar.setFont(
                    new Font("Segoe UI Emoji", Font.PLAIN, 48)
            );
        }

        JPanel nameAndBadge = new JPanel();

        nameAndBadge.setOpaque(false);

        nameAndBadge.setLayout(
                new BoxLayout(nameAndBadge, BoxLayout.Y_AXIS)
        );

        JPanel codeBadgeRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );

        codeBadgeRow.setOpaque(false);

        codeBadgeRow.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel lblCodeTitleText =
                new JLabel("Mã răng:");

        lblCodeTitleText.setFont(
                new Font("Segoe UI", Font.PLAIN, 15)
        );

        lblCodeTitleText.setForeground(MUTED);

        lblHeaderCodeBadge = new JLabel("--");

        lblHeaderCodeBadge.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        lblHeaderCodeBadge.setForeground(BLUE);

        codeBadgeRow.add(lblCodeTitleText);
        codeBadgeRow.add(lblHeaderCodeBadge);

        lblHeaderName = new JLabel(
                "Chưa chọn răng"
        );

        lblHeaderName.setFont(
                new Font("Segoe UI", Font.BOLD, 20)
        );

        lblHeaderName.setForeground(NAVY);

        lblHeaderName.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        badgePanelType = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        badgePanelType.setOpaque(true);

        badgePanelType.setBackground(
                PASTEL_BLUE_BG
        );

        badgePanelType.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        lblHeaderType = new JLabel(" -- ");

        lblHeaderType.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        lblHeaderType.setForeground(BLUE);

        badgePanelType.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(180, 215, 250)
                        ),
                        new EmptyBorder(3, 10, 3, 10)
                )
        );

        badgePanelType.add(lblHeaderType);

        nameAndBadge.add(codeBadgeRow);
        nameAndBadge.add(Box.createVerticalStrut(5));
        nameAndBadge.add(lblHeaderName);
        nameAndBadge.add(Box.createVerticalStrut(7));
        nameAndBadge.add(badgePanelType);

        topInfoPanel.add(
                lblBigToothAvatar,
                BorderLayout.WEST
        );

        topInfoPanel.add(
                nameAndBadge,
                BorderLayout.CENTER
        );

        body.add(topInfoPanel);
        body.add(Box.createVerticalStrut(12));

        // Bảng thuộc tính
        JPanel attrGrid = new JPanel(
                new GridBagLayout()
        );

        attrGrid.setOpaque(false);

        attrGrid.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        attrGrid.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        String[] attrNames = {
            "Mã răng",
            "Tên răng",
            "Hàm",
            "Vị trí",
            "Phân loại"
        };

        lblCode = createAttrLabel("--", false);
        lblName = createAttrLabel("--", false);
        lblJaw = createAttrLabel("--", false);
        lblPosition = createAttrLabel("--", false);
        lblType = createAttrLabel("--", false);

        JLabel[] attrValues = {
            lblCode,
            lblName,
            lblJaw,
            lblPosition,
            lblType
        };

        for (int i = 0; i < attrNames.length; i++) {

            GridBagConstraints gbc =
                    new GridBagConstraints();

            gbc.gridy = i;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1;

            gbc.gridx = 0;
            gbc.weightx = 0.35;
            gbc.insets = new Insets(0, 0, 0, 0);

            JPanel leftCell = createAttrCell();

            leftCell.add(
                    createAttrLabel(attrNames[i], true),
                    BorderLayout.CENTER
            );

            attrGrid.add(leftCell, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;

            JPanel rightCell = createAttrCell();

            rightCell.add(
                    attrValues[i],
                    BorderLayout.CENTER
            );

            attrGrid.add(rightCell, gbc);
        }

        attrGrid.setPreferredSize(
                new Dimension(100, 175)
        );

        attrGrid.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 175)
        );

        body.add(attrGrid);
        body.add(Box.createVerticalStrut(12));

        // Thông tin chung
        JPanel infoBox = new JPanel(
                new BorderLayout(5, 7)
        );

        infoBox.setOpaque(true);

        infoBox.setBackground(
                PASTEL_BLUE_BG
        );

        infoBox.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(180, 215, 250)
                        ),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );

        infoBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        infoBox.setPreferredSize(
                new Dimension(100, 95)
        );

        infoBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 95)
        );

        JLabel infoTitle = new JLabel(
                "ℹ  Thông tin chung"
        );

        infoTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        infoTitle.setForeground(NAVY);

        lblDescription = new JLabel(
                "<html><div style='width:300px'>"
                + "Đây là dữ liệu răng chuẩn, dùng chung cho toàn hệ thống. "
                + "Không phải thông tin tình trạng răng của bệnh nhân."
                + "</div></html>"
        );

        lblDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        lblDescription.setForeground(NAVY);

        infoBox.add(infoTitle, BorderLayout.NORTH);
        infoBox.add(lblDescription, BorderLayout.CENTER);

        body.add(infoBox);
        body.add(Box.createVerticalStrut(10));

        // Mô tả thêm
        JPanel extraBox = new JPanel(
                new BorderLayout(5, 7)
        );

        extraBox.setOpaque(true);
        extraBox.setBackground(Color.WHITE);

        extraBox.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(10, 12, 10, 12)
                )
        );

        extraBox.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        extraBox.setPreferredSize(
                new Dimension(100, 100)
        );

        extraBox.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 100)
        );

        JLabel extraTitle = new JLabel(
                "🦷  Mô tả thêm"
        );

        extraTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        extraTitle.setForeground(NAVY);

        lblDescExtra = new JLabel(
                "<html><div style='width:300px'>"
                + "Chưa có mô tả chi tiết cho răng này."
                + "</div></html>"
        );

        lblDescExtra.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        lblDescExtra.setForeground(MUTED);

        extraBox.add(extraTitle, BorderLayout.NORTH);
        extraBox.add(lblDescExtra, BorderLayout.CENTER);

        body.add(extraBox);

        card.add(body, BorderLayout.CENTER);

        return card;
    }

    // ================= ATTRIBUTE CELL =================
    private JPanel createAttrCell() {

        JPanel cell = new JPanel(
                new BorderLayout()
        );

        cell.setOpaque(false);

        cell.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 1, 1, BORDER
                        ),
                        new EmptyBorder(7, 12, 7, 8)
                )
        );

        return cell;
    }

    // ================= ATTRIBUTE LABEL =================
    private JLabel createAttrLabel(
            String text,
            boolean isTitle) {

        JLabel lbl = new JLabel(text);

        if (isTitle) {

            lbl.setFont(
                    new Font("Segoe UI", Font.PLAIN, 13)
            );

            lbl.setForeground(MUTED);

        } else {

            lbl.setFont(
                    new Font("Segoe UI", Font.BOLD, 13)
            );

            lbl.setForeground(NAVY);
        }

        return lbl;
    }

    // ================= TABLE CARD =================
    private JPanel createTableCard() {

        JPanel card = createCard();

        JPanel top = new JPanel(
                new BorderLayout(0, 10)
        );

        top.setOpaque(false);

        JLabel title = new JLabel(
                "Danh sách răng"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 17)
        );

        title.setForeground(NAVY);

        top.add(title, BorderLayout.NORTH);

        card.add(top, BorderLayout.NORTH);

        String[] columns = {
            "Mã răng",
            "Tên răng",
            "Hàm",
            "Vị trí",
            "Phân loại"
        };

        tableModel = new DefaultTableModel(
                columns, 0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        toothTable = new JTable(tableModel);

        toothTable.setRowHeight(34);

        toothTable.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        toothTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        toothTable.setAutoCreateRowSorter(true);

        toothTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        toothTable.getTableHeader()
                .setReorderingAllowed(false);

        toothTable.setSelectionBackground(
                new Color(210, 232, 255)
        );

        toothTable.setSelectionForeground(NAVY);

        toothTable.setFillsViewportHeight(true);

        toothTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        toothTable.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(70);

        toothTable.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(150);

        toothTable.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(95);

        toothTable.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(90);

        toothTable.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(110);

        sorter = new TableRowSorter<>(tableModel);

        toothTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(toothTable);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new BorderLayout(5, 0)
        );

        bottom.setOpaque(false);

        JLabel rowCount = new JLabel(
                "Hiển thị 0/0 răng"
        );

        rowCount.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        rowCount.setForeground(MUTED);

        bottom.add(rowCount, BorderLayout.WEST);

        card.add(bottom, BorderLayout.SOUTH);

        // Chọn răng trong bảng
        toothTable.getSelectionModel()
                .addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int viewRow = toothTable.getSelectedRow();

            if (viewRow < 0) {
                return;
            }

            int modelRow =
                    toothTable.convertRowIndexToModel(viewRow);

            Object value =
                    tableModel.getValueAt(modelRow, 0);

            if (value == null) {
                return;
            }

            ToothData tooth = findTooth(
                    Integer.parseInt(value.toString())
            );

            if (tooth != null) {
                selectTooth(tooth);
            }
        });

        tableModel.addTableModelListener(e -> {

            rowCount.setText(
                    "Hiển thị "
                    + tableModel.getRowCount()
                    + "/"
                    + teeth.size()
                    + " răng"
            );
        });

        return card;
    }

    // ================= SELECT TOOTH =================
    private void selectTooth(ToothData tooth) {

        selectedTooth = tooth;

        if (tooth == null) {

            lblHeaderCodeBadge.setText("--");
            lblHeaderName.setText("Chưa chọn răng");
            lblHeaderType.setText(" -- ");

            lblCode.setText("--");
            lblName.setText("--");
            lblJaw.setText("--");
            lblPosition.setText("--");
            lblType.setText("--");

            lblDescription.setText(
                    "Đây là dữ liệu răng chuẩn, dùng chung cho toàn hệ thống. "
                    + "Không phải thông tin tình trạng răng của bệnh nhân."
            );

            lblDescExtra.setText(
                    "Chưa có mô tả chi tiết cho răng này."
            );

            lblSelected.setText(
                    "Bấm vào sơ đồ để chọn răng."
            );

            if (toothTable != null) {
                toothTable.clearSelection();
            }

        } else {

            lblHeaderCodeBadge.setText(
                    String.valueOf(tooth.code)
            );

            lblHeaderName.setText(tooth.name);

            lblHeaderType.setText(
                    " " + tooth.type + " "
            );

            lblCode.setText(
                    String.valueOf(tooth.code)
            );

            lblName.setText(tooth.name);
            lblJaw.setText(tooth.jaw);
            lblPosition.setText(tooth.position);
            lblType.setText(tooth.type);

            lblDescription.setText(
                    "Mã răng " + tooth.code
                    + " thuộc hệ thống đánh số FDI. "
                    + "Đây là dữ liệu chuẩn của răng."
            );

            lblDescExtra.setText(
                    tooth.description != null
                    && !tooth.description.isEmpty()
                    ? tooth.description
                    : "Chưa có mô tả chi tiết."
            );

            lblSelected.setText(
                    "Đang chọn răng "
                    + tooth.code
                    + " - "
                    + tooth.name
            );

            // Đồng bộ dòng được chọn trong bảng
            if (tableModel != null && toothTable != null) {

                for (int i = 0;
                        i < tableModel.getRowCount();
                        i++) {

                    int code = Integer.parseInt(
                            tableModel.getValueAt(i, 0)
                                    .toString()
                    );

                    if (code == tooth.code) {

                        int viewRow =
                                toothTable.convertRowIndexToView(i);

                        if (viewRow >= 0) {

                            toothTable.setRowSelectionInterval(
                                    viewRow,
                                    viewRow
                            );

                            toothTable.scrollRectToVisible(
                                    toothTable.getCellRect(
                                            viewRow,
                                            0,
                                            true
                                    )
                            );
                        }

                        break;
                    }
                }
            }
        }

        // Cập nhật sơ đồ
        if (imagePanel != null) {
            imagePanel.repaint();
        }

        // Gửi răng được chọn sang màn hình khám bệnh
        if (toothSelectionListener != null) {
            toothSelectionListener.accept(tooth);
        }
    }

    // ================= FILTER & SEARCH =================
    private void applyFilter() {

        if (tableModel == null
                || cbJaw == null
                || cbPosition == null
                || cbType == null
                || txtSearch == null) {

            return;
        }

        String jaw =
                String.valueOf(cbJaw.getSelectedItem());

        String position =
                String.valueOf(cbPosition.getSelectedItem());

        String type =
                String.valueOf(cbType.getSelectedItem());

        String keyword =
                txtSearch.getText()
                        .trim()
                        .toLowerCase();

        tableModel.setRowCount(0);

        for (ToothData tooth : teeth) {

            if (!"Tất cả".equals(jaw)
                    && !jaw.equals(tooth.jaw)) {
                continue;
            }

            if (!"Tất cả".equals(position)
                    && !position.equals(tooth.position)) {
                continue;
            }

            if (!"Tất cả".equals(type)
                    && !type.equals(tooth.type)) {
                continue;
            }

            String code =
                    String.valueOf(tooth.code);

            if (!keyword.isEmpty()
                    && !code.contains(keyword)
                    && !tooth.name.toLowerCase().contains(keyword)
                    && !tooth.type.toLowerCase().contains(keyword)) {

                continue;
            }

            tableModel.addRow(
                    new Object[]{
                        tooth.code,
                        tooth.name,
                        tooth.jaw,
                        tooth.position,
                        tooth.type
                    }
            );
        }

        if (selectedTooth != null
                && !isToothVisible(selectedTooth.code)) {

            selectTooth(null);
        }
    }

    // ================= CHECK TOOTH VISIBLE =================
    private boolean isToothVisible(int code) {

        for (int i = 0;
                i < tableModel.getRowCount();
                i++) {

            if (Integer.parseInt(
                    tableModel.getValueAt(i, 0).toString()
            ) == code) {

                return true;
            }
        }

        return false;
    }

    // ================= SET TEETH =================
    public void setTeeth(List<ToothData> data) {

        teeth.clear();

        if (data != null) {
            teeth.addAll(data);
        }

        selectTooth(null);

        applyFilter();

        if (imagePanel != null) {
            imagePanel.repaint();
        }
    }

    // ================= SAMPLE DATA =================
    private void loadSampleTeethForDesign() {

        teeth.clear();

        for (int code = 11; code <= 18; code++) {
            addToothSample(
                    code,
                    "Hàm trên",
                    "Bên phải"
            );
        }

        for (int code = 21; code <= 28; code++) {
            addToothSample(
                    code,
                    "Hàm trên",
                    "Bên trái"
            );
        }

        for (int code = 31; code <= 38; code++) {
            addToothSample(
                    code,
                    "Hàm dưới",
                    "Bên trái"
            );
        }

        for (int code = 41; code <= 48; code++) {
            addToothSample(
                    code,
                    "Hàm dưới",
                    "Bên phải"
            );
        }
    }

    // ================= ADD SAMPLE TOOTH =================
    private void addToothSample(
            int code,
            String jaw,
            String position) {

        int n = code % 10;

        String type;
        String name;

        switch (n) {

            case 1:
                type = "Răng cửa";
                name = "Răng cửa giữa";
                break;

            case 2:
                type = "Răng cửa";
                name = "Răng cửa bên";
                break;

            case 3:
                type = "Răng nanh";
                name = "Răng nanh";
                break;

            case 4:
                type = "Răng tiền hàm";
                name = "Răng tiền hàm thứ nhất";
                break;

            case 5:
                type = "Răng tiền hàm";
                name = "Răng tiền hàm thứ hai";
                break;

            case 6:
                type = "Răng hàm";
                name = "Răng hàm lớn thứ nhất";
                break;

            case 7:
                type = "Răng hàm";
                name = "Răng hàm lớn thứ hai";
                break;

            default:
                type = "Răng hàm";
                name = "Răng khôn";
                break;
        }

        String description =
                name
                + " thuộc nhóm "
                + type.toLowerCase()
                + ", vị trí "
                + position.toLowerCase()
                + " "
                + jaw.toLowerCase()
                + ", có các đặc điểm giải phẫu chuẩn.";

        teeth.add(
                new ToothData(
                        code,
                        name,
                        jaw,
                        position,
                        type,
                        description
                )
        );
    }

    // ================= FIND TOOTH =================
    private ToothData findTooth(int code) {

        for (ToothData tooth : teeth) {

            if (tooth.code == code) {
                return tooth;
            }
        }

        return null;
    }

    // ================= TOOTH DATA MODEL =================
    public static class ToothData {

        public final int code;

        public final String name;
        public final String jaw;
        public final String position;
        public final String type;
        public final String description;

        public ToothData(
                int code,
                String name,
                String jaw,
                String position,
                String type,
                String description) {

            this.code = code;

            this.name = name == null ? "" : name;
            this.jaw = jaw == null ? "" : jaw;
            this.position = position == null ? "" : position;
            this.type = type == null ? "" : type;

            this.description =
                    description == null ? "" : description;
        }
    }

    // ================= UI HELPERS =================
    private JPanel createCard() {

        JPanel card = new JPanel(
                new BorderLayout(0, 12)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER, 1
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        );

        return card;
    }

    // ================= COMBO STYLE =================
    private void styleCombo(JComboBox<String> combo) {

        combo.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        combo.setBackground(Color.WHITE);
        combo.setForeground(NAVY);
    }
}
