package views;

import models.dao.DichVuDAO;
import models.entity.DichVu;
import report.PdfTableExport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class ServicePanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(34, 150, 83);
    private static final Color RED_ACCENT = new Color(217, 45, 32);
    private static final Color PINK_ACCENT = new Color(174, 35, 86);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private JPanel gridCardContainer;
    private JPanel detailPanel;
    private JTextField txtSearch;
    private JLabel lblTotalCount;

    // Các thành phần hiển thị ở Panel chi tiết bên phải
    private JLabel lblDetailId, lblDetailTitle, lblDetailPrice, lblDetailCategory, lblDetailUnit, lblDetailDesc;
    private DichVu selectedService = null;

    public ServicePanel() {
        setLayout(new BorderLayout(20, 0));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // 1. PANEL BÊN TRÁI (CHỨA HEADER VÀ LƯỚI CARD DỊCH VỤ)
        JPanel leftMainPanel = new JPanel(new BorderLayout(0, 20));
        leftMainPanel.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản Lý Danh Mục Dịch Vụ Nha Khoa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Quản lý danh sách dịch vụ, đơn giá và phân loại điều trị");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);
        headerPanel.add(titleBox, BorderLayout.WEST);

        // Thanh tìm kiếm, nút làm mới và nút thêm
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbarPanel.setOpaque(false);

        txtSearch = new JTextField(16);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(200, 38));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm dịch vụ...");
        txtSearch.addActionListener(e -> reloadServiceGrid(txtSearch.getText().trim()));

        JButton btnSearch = createStaticButton("Tìm", BLUE_PRIMARY, 75, 38);
        btnSearch.addActionListener(e -> reloadServiceGrid(txtSearch.getText().trim()));

        JButton btnRefresh = createStaticButton("Làm mới", PINK_ACCENT, 95, 38);
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            selectedService = null;
            detailPanel.setVisible(false);
            reloadServiceGrid("");
        });

        JButton btnAdd = createStaticButton("+ Thêm dịch vụ", GREEN_ACCENT, 130, 38);
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);
        toolbarPanel.add(btnRefresh);
        toolbarPanel.add(btnAdd);
        headerPanel.add(toolbarPanel, BorderLayout.EAST);

        leftMainPanel.add(headerPanel, BorderLayout.NORTH);

        // Lưới hiển thị các thẻ dịch vụ (Grid 3 cột)
        gridCardContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        gridCardContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(gridCardContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_CANVAS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftMainPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer (Tổng số lượng dịch vụ)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        lblTotalCount = new JLabel("Tổng cộng: 0 dịch vụ");
        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalCount.setForeground(TEXT_DARK);
        footerPanel.add(lblTotalCount, BorderLayout.WEST);

        leftMainPanel.add(footerPanel, BorderLayout.SOUTH);
        add(leftMainPanel, BorderLayout.CENTER);

        // 2. PANEL BÊN PHẢI (HIỂN THỊ CHI TIẾT DỊCH VỤ)
        detailPanel = createDetailSidePanel();
        add(detailPanel, BorderLayout.EAST);

        // Tải dữ liệu ban đầu
        reloadServiceGrid("");
    }

    // Tạo Panel Chi Tiết bên phải với khung ảnh rộng rãi và đầy đủ các trường CSDL
    private JPanel createDetailSidePanel() {
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
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tiêu đề và nút đóng (X)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel lblDetailHeaderTitle = new JLabel("Chi tiết dịch vụ");
        lblDetailHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDetailHeaderTitle.setForeground(TEXT_DARK);
        topBar.add(lblDetailHeaderTitle, BorderLayout.WEST);

        JButton btnCloseDetail = new JButton("×");
        btnCloseDetail.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnCloseDetail.setForeground(MUTED);
        btnCloseDetail.setBorderPainted(false);
        btnCloseDetail.setContentAreaFilled(false);
        btnCloseDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCloseDetail.addActionListener(e -> {
            selectedService = null;
            detailPanel.setVisible(false);
        });
        topBar.add(btnCloseDetail, BorderLayout.EAST);
        
        panel.add(topBar);
        panel.add(Box.createVerticalStrut(12));

        // Khung hình ảnh để trống (Chiều cao 170px)
        JPanel imgPlaceholder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 238, 248));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        imgPlaceholder.setOpaque(false);
        imgPlaceholder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));
        imgPlaceholder.setPreferredSize(new Dimension(300, 170));
        imgPlaceholder.setLayout(new GridBagLayout());
        
        JLabel lblIconPlaceholder = new JLabel("🦷");
        lblIconPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        imgPlaceholder.add(lblIconPlaceholder);
        panel.add(imgPlaceholder);
        panel.add(Box.createVerticalStrut(12));

        // Tên dịch vụ (tenDichVu)
        lblDetailTitle = new JLabel("Chọn dịch vụ để xem");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDetailTitle.setForeground(TEXT_DARK);
        lblDetailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDetailTitle);
        panel.add(Box.createVerticalStrut(6));

        // Mô tả dịch vụ (moTa)
        lblDetailDesc = new JLabel("<html>Thông tin chi tiết sẽ hiển thị ở đây.</html>");
        lblDetailDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailDesc.setForeground(MUTED);
        lblDetailDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDetailDesc);
        panel.add(Box.createVerticalStrut(12));

        // Box giá tiền (donGia)
        JPanel priceBox = new JPanel(new BorderLayout());
        priceBox.setBackground(new Color(243, 248, 253));
        priceBox.setBorder(new EmptyBorder(10, 12, 10, 12));
        priceBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        priceBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblPriceTitle = new JLabel("Đơn giá (VNĐ)");
        lblPriceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPriceTitle.setForeground(MUTED);
        
        lblDetailPrice = new JLabel("0 đ");
        lblDetailPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblDetailPrice.setForeground(BLUE_PRIMARY);

        JPanel pBoxInner = new JPanel();
        pBoxInner.setLayout(new BoxLayout(pBoxInner, BoxLayout.Y_AXIS));
        pBoxInner.setOpaque(false);
        pBoxInner.add(lblPriceTitle);
        pBoxInner.add(Box.createVerticalStrut(2));
        pBoxInner.add(lblDetailPrice);
        priceBox.add(pBoxInner, BorderLayout.CENTER);
        
        panel.add(priceBox);
        panel.add(Box.createVerticalStrut(12));

        // Các thuộc tính lấy đầy đủ từ CSDL: maDichVu, loaiDieuTri, donViTinh
        panel.add(createDetailRow("Mã dịch vụ:", lblDetailId = new JLabel("-")));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createDetailRow("Loại điều trị:", lblDetailCategory = new JLabel("-")));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createDetailRow("Đơn vị tính:", lblDetailUnit = new JLabel("-")));
        panel.add(Box.createVerticalStrut(15));

        // Nút Sửa / Xóa thẳng hàng bên dưới
        JPanel actionBox = new JPanel(new GridLayout(1, 2, 10, 0));
        actionBox.setOpaque(false);
        actionBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        actionBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEditSelected = createStaticButton("Sửa", GREEN_ACCENT, Integer.MAX_VALUE, 38);
        JButton btnDeleteSelected = createStaticButton("Xóa", RED_ACCENT, Integer.MAX_VALUE, 38);

        btnEditSelected.addActionListener(e -> {
            if (selectedService != null) showAddEditDialog(selectedService);
        });
        btnDeleteSelected.addActionListener(e -> {
            if (selectedService != null) deleteService(selectedService.getMaDichVu());
        });

        actionBox.add(btnEditSelected);
        actionBox.add(btnDeleteSelected);
        panel.add(actionBox);

        panel.setVisible(false);
        return panel;
    }

    private JPanel createDetailRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKey.setForeground(MUTED);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueLabel.setForeground(TEXT_DARK);

        row.add(lblKey, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // Tải lại lưới card dịch vụ từ CSDL
    public void reloadServiceGrid(String keyword) {
        gridCardContainer.removeAll();
        List<DichVu> list = keyword.isEmpty() ? dichVuDAO.getAllDichVu() : dichVuDAO.search(keyword);
        
        DecimalFormat df = new DecimalFormat("#,###");

        for (DichVu dv : list) {
            JPanel card = createServiceCard(dv, df);
            gridCardContainer.add(card);
        }

        lblTotalCount.setText("Tổng cộng: " + list.size() + " dịch vụ");
        gridCardContainer.revalidate();
        gridCardContainer.repaint();
    }

    // Thiết kế từng thẻ Card dịch vụ
    private JPanel createServiceCard(DichVu dv, DecimalFormat df) {
        JPanel card = new JPanel() {
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
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Khung hình ảnh để trống phía trên
        JPanel topImgBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(235, 242, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        topImgBox.setOpaque(false);
        topImgBox.setPreferredSize(new Dimension(0, 95));
        topImgBox.setLayout(new BorderLayout());
        topImgBox.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel lblToothIcon = new JLabel("🦷", SwingConstants.RIGHT);
        lblToothIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        topImgBox.add(lblToothIcon, BorderLayout.NORTH);
        card.add(topImgBox, BorderLayout.NORTH);

        // Phần giữa: Hiển thị đầy đủ Tên dịch vụ và Mô tả từ CSDL
        JPanel infoBox = new JPanel();
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
        infoBox.setOpaque(false);
        infoBox.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lblName = new JLabel(dv.getTenDichVu());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_DARK);
        infoBox.add(lblName);
        infoBox.add(Box.createVerticalStrut(4));

        String desc = dv.getMoTa() != null && !dv.getMoTa().isEmpty() ? dv.getMoTa() : "Chăm sóc và điều trị chuyên nghiệp.";
        JLabel lblDesc = new JLabel("<html><body style='width: 170px'>" + desc + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(MUTED);
        infoBox.add(lblDesc);
        infoBox.add(Box.createVerticalStrut(10));

        JLabel lblPrice = new JLabel(df.format(dv.getDonGia()) + " đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(BLUE_PRIMARY);
        infoBox.add(lblPrice);

        card.add(infoBox, BorderLayout.CENTER);

        // Phần dưới: Nút "Xem chi tiết"
        JButton btnDetail = new JButton("Xem chi tiết →");
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDetail.setForeground(BLUE_PRIMARY);
        btnDetail.setBorderPainted(false);
        btnDetail.setContentAreaFilled(false);
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDetail.addActionListener(e -> selectServiceForDetail(dv));

        JPanel bottomBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomBox.setOpaque(false);
        bottomBox.add(btnDetail);
        card.add(bottomBox, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectServiceForDetail(dv);
            }
        });

        return card;
    }

    private void selectServiceForDetail(DichVu dv) {
        selectedService = dv;
        lblDetailId.setText(dv.getMaDichVu() != null ? dv.getMaDichVu() : "-");
        lblDetailTitle.setText(dv.getTenDichVu());
        lblDetailPrice.setText(new DecimalFormat("#,###").format(dv.getDonGia()) + " đ");
        lblDetailCategory.setText(dv.getLoaiDieuTri() != null ? dv.getLoaiDieuTri() : "-");
        lblDetailUnit.setText(dv.getDonViTinh() != null ? dv.getDonViTinh() : "-");
        lblDetailDesc.setText("<html><body style='width: 280px'>" + (dv.getMoTa() != null && !dv.getMoTa().isEmpty() ? dv.getMoTa() : "Không có mô tả chi tiết.") + "</body></html>");
        detailPanel.setVisible(true);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // Dialog Thêm hoặc Sửa dịch vụ
    private void showAddEditDialog(DichVu editTarget) {
        boolean isEdit = (editTarget != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                isEdit ? "Chỉnh Sửa Dịch Vụ Nha Khoa" : "Thêm Dịch Vụ Mới", true);
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(25, 25, 25, 25));
        form.setBackground(Color.WHITE);

        JTextField txtId = addFieldToDialog(form, "Mã dịch vụ:");
        JTextField txtName = addFieldToDialog(form, "Tên dịch vụ:");

        form.add(createFieldLabel("Loại điều trị:"));
        form.add(Box.createVerticalStrut(4));
        JComboBox<String> cbCat = new JComboBox<>(new String[]{"Nha khoa tổng quát", "Thẩm mỹ", "Chỉnh nha", "Tiểu phẫu", "Phục hình sứ"});
        cbCat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCat.setBackground(Color.WHITE);
        form.add(cbCat);
        form.add(Box.createVerticalStrut(12));

        JTextField txtPrice = addFieldToDialog(form, "Đơn giá (VNĐ):");
        JTextField txtUnit = addFieldToDialog(form, "Đơn vị tính (VD: Lần, Răng):");
        JTextField txtDesc = addFieldToDialog(form, "Mô tả dịch vụ:");

        if (isEdit) {
            txtId.setText(editTarget.getMaDichVu());
            txtId.setEnabled(false);
            txtName.setText(editTarget.getTenDichVu());
            cbCat.setSelectedItem(editTarget.getLoaiDieuTri());
            txtPrice.setText(String.valueOf(editTarget.getDonGia()));
            txtUnit.setText(editTarget.getDonViTinh());
            txtDesc.setText(editTarget.getMoTa());
        }

        JButton btnSaveDialog = createStaticButton("Lưu thông tin", BLUE_PRIMARY, Integer.MAX_VALUE, 40);
        btnSaveDialog.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = txtName.getText().trim();
            String cat = (String) cbCat.getSelectedItem();
            String priceStr = txtPrice.getText().trim();
            String unit = txtUnit.getText().trim();
            String desc = txtDesc.getText().trim();

            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ Mã, Tên và Đơn giá dịch vụ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceStr.replace(".", "").replace(",", "."));
                if (price < 0) throw new NumberFormatException();

                DichVu dv = new DichVu(id, name, cat, price, unit, desc);
                boolean success = isEdit ? dichVuDAO.update(dv) : dichVuDAO.insert(dv);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, isEdit ? "Cập nhật dịch vụ thành công!" : "Thêm dịch vụ thành công!");
                    dialog.dispose();
                    reloadServiceGrid("");
                    detailPanel.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thực hiện thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Đơn giá phải là số hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(Box.createVerticalStrut(10));
        form.add(btnSaveDialog);
        dialog.add(form, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void deleteService(String maDichVu) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa dịch vụ: " + maDichVu + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (dichVuDAO.delete(maDichVu)) {
                    JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công!");
                    detailPanel.setVisible(false);
                    reloadServiceGrid("");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa dịch vụ này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addFieldToDialog(JPanel form, String labelText) {
        form.add(createFieldLabel(labelText));
        form.add(Box.createVerticalStrut(4));
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
        ));
        form.add(txt);
        form.add(Box.createVerticalStrut(12));
        return txt;
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}