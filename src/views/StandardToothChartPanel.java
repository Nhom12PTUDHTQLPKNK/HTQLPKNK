
package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class StandardToothChartPanel extends JPanel {

    private static final Color BLUE = new Color(0, 126, 255);
    private static final Color NAVY = new Color(15, 29, 69);
    private static final Color BG = new Color(243, 248, 253);
    private static final Color BORDER = new Color(210, 220, 235);
    private static final Color TEXT = new Color(15, 29, 69);
    private static final Color MUTED = new Color(100, 110, 130);

    private final List<ToothData> teeth = new ArrayList<>();

    private JComboBox<String> cbJaw;
    private JComboBox<String> cbPosition;
    private JComboBox<String> cbType;
    private JTextField txtSearch;

    private JTable toothTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel lblCode;
    private JLabel lblName;
    private JLabel lblJaw;
    private JLabel lblPosition;
    private JLabel lblType;
    private JLabel lblDescription;
    private JLabel lblSelected;

    private ToothImagePanel imagePanel;
    private ToothData selectedTooth;

    public StandardToothChartPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Dữ liệu mẫu để kiểm tra giao diện.
        // Khi kết nối SQL, có thể thay bằng setTeeth(danhSachTuSQL).
        loadSampleTeeth();

        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        content.add(createFilterCard(), BorderLayout.NORTH);

        // Ba card chính: ảnh răng, thông tin răng, bảng danh sách.
        JPanel main = new JPanel(new GridBagLayout());
        main.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 10);

        // Card trái: ảnh sơ đồ răng.
        g.gridx = 0;
        g.weightx = 0.34;
        main.add(createChartCard(), g);

        // Card giữa: thông tin chi tiết.
        g.gridx = 1;
        g.weightx = 0.27;
        main.add(createDetailCard(), g);

        // Card phải: bảng danh sách, tăng diện tích bảng.
        g.gridx = 2;
        g.weightx = 0.39;
        g.insets = new Insets(0, 0, 0, 0);
        main.add(createTableCard(), g);

        content.add(main, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        applyFilter();
    }

    // ================= HEADER =================

    private JPanel createHeader() {
        JPanel card = createCard();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🦷");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Sơ đồ răng chuẩn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Dữ liệu răng chuẩn dùng chung cho toàn hệ thống - Hệ thống FDI"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        texts.add(title);
        texts.add(Box.createVerticalStrut(3));
        texts.add(subtitle);

        left.add(icon);
        left.add(texts);

        card.add(left, BorderLayout.CENTER);
        return card;
    }

    // ================= CARD BỘ LỌC NGANG =================

    private JPanel createFilterCard() {
        JPanel card = createCard();

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);

        cbJaw = new JComboBox<>(new String[]{
                "Tất cả", "Hàm trên", "Hàm dưới"
        });

        cbPosition = new JComboBox<>(new String[]{
                "Tất cả", "Bên phải", "Bên trái"
        });

        cbType = new JComboBox<>(new String[]{
                "Tất cả", "Răng cửa", "Răng nanh",
                "Răng tiền hàm", "Răng hàm"
        });

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setToolTipText("Nhập mã răng hoặc tên răng");

        styleCombo(cbJaw);
        styleCombo(cbPosition);
        styleCombo(cbType);

        JButton btnRefresh = new JButton("↻ Làm mới");
        styleButton(btnRefresh, false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 4, 0, 4);
        g.weighty = 1;

        g.gridx = 0;
        g.weightx = 0.16;
        filters.add(createLabeledField("Loại hàm", cbJaw), g);

        g.gridx = 1;
        g.weightx = 0.16;
        filters.add(createLabeledField("Vị trí", cbPosition), g);

        g.gridx = 2;
        g.weightx = 0.19;
        filters.add(createLabeledField("Phân loại", cbType), g);

        g.gridx = 3;
        g.weightx = 0.39;
        filters.add(createLabeledField("Tìm kiếm", txtSearch), g);

        g.gridx = 4;
        g.weightx = 0;
        filters.add(btnRefresh, g);

        card.add(filters, BorderLayout.CENTER);

        cbJaw.addActionListener(e -> applyFilter());
        cbPosition.addActionListener(e -> applyFilter());
        cbType.addActionListener(e -> applyFilter());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
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

    private JPanel createLabeledField(String title, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT);

        field.setPreferredSize(new Dimension(100, 30));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    // ================= CARD TRÁI: ẢNH SƠ ĐỒ RĂNG =================

    private JPanel createChartCard() {
        JPanel card = createCard();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Sơ đồ răng vĩnh viễn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        JLabel standard = new JLabel("FDI / ISO 3950");
        standard.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        standard.setForeground(BLUE);

        header.add(title, BorderLayout.WEST);
        header.add(standard, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        imagePanel = new ToothImagePanel();
        imagePanel.setPreferredSize(new Dimension(330, 450));
        imagePanel.setMinimumSize(new Dimension(200, 250));

        card.add(imagePanel, BorderLayout.CENTER);

        lblSelected = new JLabel("Bấm vào sơ đồ để chọn răng.");
        lblSelected.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSelected.setForeground(MUTED);

        card.add(lblSelected, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Hiển thị ảnh sodorang.png.
     * Có thể bấm vào ảnh để chọn răng gần vị trí được bấm.
     */
    private class ToothImagePanel extends JPanel {

        private Image image;

        ToothImagePanel() {
            setBackground(Color.WHITE);
            setOpaque(true);

            java.net.URL url = getClass().getResource("/img/sodorang.png");

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
                    ToothData tooth = getToothAt(e.getX(), e.getY());

                    if (tooth != null) {
                        selectTooth(tooth);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

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

                    int w = (int) (iw * scale);
                    int h = (int) (ih * scale);
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2.drawImage(image, x, y, w, h, this);
                }
            } else {
                g2.setColor(new Color(245, 248, 255));
                g2.fillRoundRect(
                        10, 10,
                        getWidth() - 20,
                        getHeight() - 20,
                        18, 18
                );

                g2.setColor(MUTED);
                String message = "Không tìm thấy /img/sodorang.png";
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

            // Đánh dấu vị trí răng đang chọn.
            if (selectedTooth != null) {
                Point2D p = getApproximateToothPoint(
                        selectedTooth.code
                );

                if (p != null) {
                    int x = (int) (p.getX() * getWidth());
                    int y = (int) (p.getY() * getHeight());

                    g2.setColor(BLUE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(x - 10, y - 10, 20, 20);
                }
            }

            g2.dispose();
        }

        private ToothData getToothAt(int x, int y) {
            if (teeth.isEmpty()) {
                return null;
            }

            ToothData nearest = null;
            double nearestDistance = Double.MAX_VALUE;

            for (ToothData tooth : teeth) {
                Point2D p = getApproximateToothPoint(tooth.code);

                if (p == null) {
                    continue;
                }

                double px = p.getX() * getWidth();
                double py = p.getY() * getHeight();

                double distance = Point2D.distance(
                        x, y, px, py
                );

                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = tooth;
                }
            }

            double maxDistance = Math.max(
                    22,
                    Math.min(getWidth(), getHeight()) * 0.075
            );

            return nearestDistance <= maxDistance ? nearest : null;
        }
    }

    /**
     * Tọa độ bấm gần đúng cho ảnh có hai hàng răng.
     * Nếu ảnh sodorang.png có bố cục khác, cần chỉnh tọa độ theo ảnh đó.
     */
    private Point2D getApproximateToothPoint(int code) {
        int[] upper = {
                18, 17, 16, 15, 14, 13, 12, 11,
                21, 22, 23, 24, 25, 26, 27, 28
        };

        int[] lower = {
                48, 47, 46, 45, 44, 43, 42, 41,
                31, 32, 33, 34, 35, 36, 37, 38
        };

        int index = indexOf(upper, code);

        if (index >= 0) {
            return new Point2D.Double(
                    0.08 + index * (0.84 / 15.0),
                    0.29
            );
        }

        index = indexOf(lower, code);

        if (index >= 0) {
            return new Point2D.Double(
                    0.08 + index * (0.84 / 15.0),
                    0.70
            );
        }

        return null;
    }

    private int indexOf(int[] values, int code) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == code) {
                return i;
            }
        }

        return -1;
    }

    // ================= CARD GIỮA: THÔNG TIN RĂNG =================

    private JPanel createDetailCard() {
        JPanel card = createCard();

        JLabel title = new JLabel("Thông tin răng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🦷");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblCode = createValueLabel("--", 22);
        lblName = createValueLabel("Chưa chọn răng", 14);
        lblJaw = createValueLabel("--", 12);
        lblPosition = createValueLabel("--", 12);
        lblType = createValueLabel("--", 12);

        lblDescription = createValueLabel(
                "Chọn một răng trên ảnh hoặc trong bảng để xem thông tin.",
                11
        );
        lblDescription.setForeground(MUTED);

        body.add(Box.createVerticalStrut(10));
        body.add(icon);
        body.add(Box.createVerticalStrut(12));

        body.add(createDetailRow("Mã răng", lblCode));
        body.add(createDetailRow("Tên răng", lblName));
        body.add(createDetailRow("Loại hàm", lblJaw));
        body.add(createDetailRow("Vị trí", lblPosition));
        body.add(createDetailRow("Phân loại", lblType));

        body.add(Box.createVerticalStrut(18));

        JPanel infoBox = new JPanel(new BorderLayout(8, 4));
        infoBox.setBackground(new Color(230, 242, 255));
        infoBox.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(180, 215, 250)
                        ),
                        new EmptyBorder(10, 10, 10, 10)
                )
        );

        JLabel infoTitle = new JLabel("Thông tin chung");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setForeground(TEXT);

        infoBox.add(infoTitle, BorderLayout.NORTH);
        infoBox.add(lblDescription, BorderLayout.CENTER);

        body.add(infoBox);
        body.add(Box.createVerticalGlue());

        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JPanel createDetailRow(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(7, 0, 7, 0));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(MUTED);

        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        value.setForeground(TEXT);

        row.add(label, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);

        return row;
    }

    private JLabel createValueLabel(String value, int size) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, size));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ================= CARD PHẢI: BẢNG DANH SÁCH RĂNG =================

    private JPanel createTableCard() {
        JPanel card = createCard();

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);

        JLabel title = new JLabel("Danh sách răng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("Bấm một dòng để xem chi tiết");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitle.setForeground(MUTED);

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        titleBox.add(title, BorderLayout.NORTH);
        titleBox.add(subtitle, BorderLayout.SOUTH);

        top.add(titleBox, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);

        String[] columns = {
                "Mã răng", "Tên răng", "Hàm", "Vị trí", "Phân loại"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        toothTable = new JTable(tableModel);
        toothTable.setRowHeight(30);
        toothTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toothTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        toothTable.setAutoCreateRowSorter(true);
        toothTable.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        toothTable.getTableHeader().setReorderingAllowed(false);
        toothTable.setSelectionBackground(
                new Color(210, 232, 255)
        );
        toothTable.setSelectionForeground(TEXT);
        toothTable.setFillsViewportHeight(true);
        toothTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        toothTable.getColumnModel().getColumn(0).setPreferredWidth(58);
        toothTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        toothTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        toothTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        toothTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        sorter = new TableRowSorter<>(tableModel);
        toothTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(toothTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));

        card.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5, 0));
        bottom.setOpaque(false);

        JLabel rowCount = new JLabel("0 răng");
        rowCount.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        rowCount.setForeground(MUTED);

        JButton btnClearSelection = new JButton("Bỏ chọn");
        styleButton(btnClearSelection, false);

        btnClearSelection.addActionListener(e -> {
            toothTable.clearSelection();
            selectTooth(null);
        });

        bottom.add(rowCount, BorderLayout.WEST);
        bottom.add(btnClearSelection, BorderLayout.EAST);

        card.add(bottom, BorderLayout.SOUTH);

        toothTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            int viewRow = toothTable.getSelectedRow();

            if (viewRow < 0) {
                return;
            }

            int modelRow = toothTable.convertRowIndexToModel(viewRow);
            Object value = tableModel.getValueAt(modelRow, 0);

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

        tableModel.addTableModelListener(e ->
                rowCount.setText(tableModel.getRowCount() + " răng")
        );

        return card;
    }

    // ================= CHỌN RĂNG =================

    private void selectTooth(ToothData tooth) {
        selectedTooth = tooth;

        if (tooth == null) {
            lblCode.setText("--");
            lblName.setText("Chưa chọn răng");
            lblJaw.setText("--");
            lblPosition.setText("--");
            lblType.setText("--");

            lblDescription.setText(
                    "Chọn một răng trên ảnh hoặc trong bảng để xem thông tin."
            );

            lblSelected.setText("Bấm vào sơ đồ để chọn răng.");

            if (toothTable != null) {
                toothTable.clearSelection();
            }
        } else {
            lblCode.setText(String.valueOf(tooth.code));
            lblName.setText(tooth.name);
            lblJaw.setText(tooth.jaw);
            lblPosition.setText(tooth.position);
            lblType.setText(tooth.type);

            lblDescription.setText(
                    "Mã răng " + tooth.code
                    + " thuộc hệ thống đánh số FDI. "
                    + "Đây là dữ liệu chuẩn của răng, "
                    + "không phải tình trạng bệnh lý của bệnh nhân."
            );

            lblSelected.setText(
                    "Đang chọn răng " + tooth.code + " - " + tooth.name
            );

            // Đồng bộ dòng được chọn trong bảng.
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int code = Integer.parseInt(
                        tableModel.getValueAt(i, 0).toString()
                );

                if (code == tooth.code) {
                    int viewRow = toothTable.convertRowIndexToView(i);

                    if (viewRow >= 0) {
                        toothTable.getSelectionModel()
                                .setValueIsAdjusting(true);

                        toothTable.setRowSelectionInterval(
                                viewRow, viewRow
                        );

                        toothTable.getSelectionModel()
                                .setValueIsAdjusting(false);

                        toothTable.scrollRectToVisible(
                                toothTable.getCellRect(viewRow, 0, true)
                        );
                    }

                    break;
                }
            }
        }

        if (imagePanel != null) {
            imagePanel.repaint();
        }
    }

    // ================= LỌC VÀ TÌM KIẾM =================

    private void applyFilter() {
        if (tableModel == null
                || cbJaw == null
                || cbPosition == null
                || cbType == null
                || txtSearch == null) {
            return;
        }

        String jaw = String.valueOf(cbJaw.getSelectedItem());
        String position = String.valueOf(cbPosition.getSelectedItem());
        String type = String.valueOf(cbType.getSelectedItem());
        String keyword = txtSearch.getText().trim().toLowerCase();

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

            String code = String.valueOf(tooth.code);

            if (!keyword.isEmpty()
                    && !code.contains(keyword)
                    && !tooth.name.toLowerCase().contains(keyword)
                    && !tooth.type.toLowerCase().contains(keyword)) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    tooth.code,
                    tooth.name,
                    tooth.jaw,
                    tooth.position,
                    tooth.type
            });
        }

        // Nếu răng đã chọn không còn nằm trong kết quả lọc thì bỏ chọn.
        if (selectedTooth != null
                && !isToothVisible(selectedTooth.code)) {

            selectedTooth = null;

            lblCode.setText("--");
            lblName.setText("Chưa chọn răng");
            lblJaw.setText("--");
            lblPosition.setText("--");
            lblType.setText("--");

            lblDescription.setText(
                    "Răng đang chọn không nằm trong kết quả lọc."
            );

            lblSelected.setText("Không có răng được chọn.");

            imagePanel.repaint();
        }
    }

    private boolean isToothVisible(int code) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Integer.parseInt(
                    tableModel.getValueAt(i, 0).toString()
            ) == code) {
                return true;
            }
        }

        return false;
    }

    // ================= DỮ LIỆU MẪU FDI =================

    private void loadSampleTeeth() {
        teeth.clear();

        for (int code = 11; code <= 18; code++) {
            addTooth(code, "Hàm trên", "Bên phải");
        }

        for (int code = 21; code <= 28; code++) {
            addTooth(code, "Hàm trên", "Bên trái");
        }

        for (int code = 31; code <= 38; code++) {
            addTooth(code, "Hàm dưới", "Bên trái");
        }

        for (int code = 41; code <= 48; code++) {
            addTooth(code, "Hàm dưới", "Bên phải");
        }
    }

    private void addTooth(int code, String jaw, String position) {
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

        teeth.add(new ToothData(
                code,
                name,
                jaw,
                position,
                type,
                "Răng theo hệ thống đánh số FDI."
        ));
    }

    /**
     * Dùng hàm này khi đã lấy danh sách răng từ SQL Server.
     *
     * Ví dụ:
     * panel.setTeeth(danhSachRangDocTuSQL);
     */
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

    private ToothData findTooth(int code) {
        for (ToothData tooth : teeth) {
            if (tooth.code == code) {
                return tooth;
            }
        }

        return null;
    }

    // ================= ĐỐI TƯỢNG DỮ LIỆU RĂNG =================

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
            this.description = description == null ? "" : description;
        }
    }

    // ================= HÀM HỖ TRỢ GIAO DIỆN =================

    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER, 1),
                        new EmptyBorder(12, 12, 12, 12)
                )
        );

        return card;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        button.setPreferredSize(new Dimension(95, 30));

        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setBorder(BorderFactory.createLineBorder(BORDER));
    }
}
