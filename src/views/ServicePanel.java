	package views;
	
	import java.awt.BasicStroke;
	import java.awt.BorderLayout;
	import java.awt.Color;
	import java.awt.Component;
	import java.awt.Cursor;
	import java.awt.Dimension;
	import java.awt.FlowLayout;
	import java.awt.Font;
	import java.awt.Frame;
	import java.awt.Graphics;
	import java.awt.Graphics2D;
	import java.awt.GridLayout;
	import java.awt.Image;
	import java.awt.LinearGradientPaint;
	import java.awt.RenderingHints;
	import java.awt.event.MouseAdapter;
	import java.awt.event.MouseEvent;
	import java.sql.SQLException;
	import java.text.DecimalFormat;
	import java.util.List;
	
	import javax.swing.BorderFactory;
	import javax.swing.Box;
	import javax.swing.BoxLayout;
	import javax.swing.ImageIcon;
	import javax.swing.JButton;
	import javax.swing.JComboBox;
	import javax.swing.JDialog;
	import javax.swing.JFileChooser;
	import javax.swing.JLabel;
	import javax.swing.JOptionPane;
	import javax.swing.JPanel;
	import javax.swing.JScrollPane;
	import javax.swing.JTextField;
	import javax.swing.SwingConstants;
	import javax.swing.SwingUtilities;
	import javax.swing.border.EmptyBorder;
	
	import models.dao.DichVuDAO;
	import models.entity.DichVu;
	
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
	
	    private JLabel lblDetailId, lblDetailTitle, lblDetailPrice,
	            lblDetailCategory, lblDetailUnit, lblDetailDesc;
	
	    private JLabel lblDetailImage;
	
	    private DichVu selectedService;
	
	
	
	    public ServicePanel() {
	        setLayout(new BorderLayout(20, 0));
	        setBackground(BG_CANVAS);
	        setBorder(new EmptyBorder(25, 30, 25, 30));
	        
	        
	        
	
	     // ================= PANEL BÊN TRÁI =================
	        JPanel leftMainPanel = new JPanel(new BorderLayout(0, 20));
	        leftMainPanel.setOpaque(false);
	
	        // ================= HEADER CARD =================
	        JPanel headerPanel = new JPanel(new BorderLayout(20, 0)) {
	
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	
	                Graphics2D g2 = (Graphics2D) g.create();
	                g2.setRenderingHint(
	                        RenderingHints.KEY_ANTIALIASING,
	                        RenderingHints.VALUE_ANTIALIAS_ON
	                );
	
	                int w = getWidth();
	                int h = getHeight();
	
	                float[] fractions = {0f, 0.55f, 1f};
	
	                Color[] colors = {
	                    new Color(255, 255, 255), // Đầu: trắng tinh
	                    new Color(225, 242, 255), // Giữa: xanh rất nhẹ
	                    new Color(155, 205, 245)  // Cuối: xanh đậm hơn
	                };
	                
	                
	                
	
	                g2.setPaint(new LinearGradientPaint(
	                        0, 0, w, 0,
	                        fractions,
	                        colors
	                ));
	
	                g2.fillRoundRect(0, 0, w, h, 24, 24);
	
	                // Viền xanh nhạt
	                g2.setColor(new Color(220, 232, 246));
	                g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);
	
	                g2.dispose();
	            }
	        };
	
	        headerPanel.setOpaque(false);
	        headerPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
	        headerPanel.setPreferredSize(new Dimension(0, 105));
	
	     // ================= TIÊU ĐỀ =================
	        JLabel lblTitle = new JLabel(
	                "Quản Lý Danh Mục Dịch Vụ Nha Khoa"
	        );
	        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
	        lblTitle.setForeground(TEXT_DARK);
	
	        JLabel lblSub = new JLabel(
	                "Quản lý danh sách dịch vụ, đơn giá và phân loại điều trị"
	        );
	        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
	        lblSub.setForeground(MUTED);
	
	        // ================= ICON TIÊU ĐỀ =================
	        JLabel lblIcon = new JLabel();
	
	        java.net.URL iconURL = getClass().getResource("/img/icon5.png");
	
	        if (iconURL != null) {
	            ImageIcon originalIcon = new ImageIcon(iconURL);
	            Image scaledImage = originalIcon.getImage().getScaledInstance(
	                    75, 75, Image.SCALE_SMOOTH
	            );
	            lblIcon.setIcon(new ImageIcon(scaledImage));
	        }
	
	        // ================= TITLE BOX =================
	        JPanel titleBox = new JPanel(new BorderLayout(14, 0));
	        titleBox.setOpaque(false);
	
	        JPanel textBox = new JPanel();
	        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
	        textBox.setOpaque(false);
	
	        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
	        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	        textBox.add(lblTitle);
	        textBox.add(Box.createVerticalStrut(5));
	        textBox.add(lblSub);
	
	        titleBox.add(lblIcon, BorderLayout.WEST);
	        titleBox.add(textBox, BorderLayout.CENTER);
	
	        headerPanel.add(titleBox, BorderLayout.WEST);
	
	        // ================= THANH TÌM KIẾM =================
	        JPanel toolbarPanel = new JPanel(
	                new FlowLayout(FlowLayout.RIGHT, 10, 0)
	        );
	        toolbarPanel.setOpaque(false);
	
	        txtSearch = new JTextField(16);
	        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	        txtSearch.setPreferredSize(new Dimension(200, 38));
	
	        txtSearch.setBorder(BorderFactory.createCompoundBorder(
	                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
	                new EmptyBorder(4, 12, 4, 12)
	        ));
	
	        txtSearch.putClientProperty(
	                "JTextField.placeholderText",
	                "Tìm kiếm dịch vụ..."
	        );
	
	        txtSearch.addActionListener(
	                e -> reloadServiceGrid(txtSearch.getText().trim())
	        );
	
	        JButton btnSearch = createStaticButton(
	                "Tìm", BLUE_PRIMARY, 75, 38
	        );
	
	        btnSearch.addActionListener(
	                e -> reloadServiceGrid(txtSearch.getText().trim())
	        );
	
	        JButton btnRefresh = createStaticButton(
	                "Làm mới", PINK_ACCENT, 95, 38
	        );
	
	        btnRefresh.addActionListener(e -> {
	            txtSearch.setText("");
	            selectedService = null;
	            detailPanel.setVisible(false);
	            reloadServiceGrid("");
	        });
	
	        toolbarPanel.add(txtSearch);
	        toolbarPanel.add(btnSearch);
	        toolbarPanel.add(btnRefresh);
	
	        headerPanel.add(toolbarPanel, BorderLayout.EAST);
	
	        leftMainPanel.add(headerPanel, BorderLayout.NORTH);
	
	        // ================= LƯỚI CARD DỊCH VỤ =================
	        gridCardContainer = new JPanel(
	                new GridLayout(0, 4, 20, 20)
	        );
	
	        gridCardContainer.setOpaque(false);
	
	        JScrollPane scrollPane = new JScrollPane(gridCardContainer);
	        scrollPane.setBorder(BorderFactory.createEmptyBorder());
	        scrollPane.getViewport().setBackground(BG_CANVAS);
	
	        scrollPane.setHorizontalScrollBarPolicy(
	                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	        );
	
	        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	
	        leftMainPanel.add(scrollPane, BorderLayout.CENTER);
	
	        // ================= FOOTER =================
	        JPanel footerPanel = new JPanel(new BorderLayout());
	        footerPanel.setOpaque(false);
	        footerPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
	
	        lblTotalCount = new JLabel("Tổng cộng: 0 dịch vụ");
	        lblTotalCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
	        lblTotalCount.setForeground(TEXT_DARK);
	
	        // Nút thêm dịch vụ chuyển xuống dưới
	        JButton btnAdd = createStaticButton(
	                "+ Thêm dịch vụ", GREEN_ACCENT, 145, 40
	        );
	
	        btnAdd.addActionListener(e -> showAddEditDialog(null));
	
	        footerPanel.add(lblTotalCount, BorderLayout.WEST);
	        footerPanel.add(btnAdd, BorderLayout.EAST);
	
	        leftMainPanel.add(footerPanel, BorderLayout.SOUTH);
	
	        add(leftMainPanel, BorderLayout.CENTER);
	        
	
	        // 2. PANEL BÊN PHẢI (HIỂN THỊ CHI TIẾT DỊCH VỤ)
	        detailPanel = createDetailSidePanel();
	        add(detailPanel, BorderLayout.EAST);
	
	        // Tải dữ liệu ban đầu
	        reloadServiceGrid("");
	    }
	    
	
	
	private String getServiceImagePath(String maDichVu) {
	
	    switch (maDichVu) {
	
	        case "DV001":
	            return "/img/DICHVU/laycaorang.png";
	
	        case "DV002":
	            return "/img/DICHVU/tramrang.png";
	
	        case "DV003":
	            return "/img/DICHVU/taytrang.png";
	
	        case "DV004":
	            return "/img/DICHVU/rang.png";
	
	        case "DV005":
	            return "/img/DICHVU/khamtongquat.png";
	
	        case "DV006":
	            return "/img/DICHVU/niengrang.png";
	
	        default:
	            return null;
	    }
	}
	
	// Ưu tiên đường dẫn ảnh lưu trong SQL.
	// Nếu SQL chưa có đường dẫn thì lấy ảnh theo mã dịch vụ.
	private String getServiceImage(DichVu dv) {
	
	    String imagePath = dv.getHinhAnh();
	
	    if (imagePath != null && !imagePath.trim().isEmpty()) {
	        return imagePath.trim();
	    }
	
	    return getServiceImagePath(dv.getMaDichVu());
	}
	
	
	
	
	    
	 
	//=================================================
	//HIỂN THỊ HÌNH ẢNH DỊCH VỤ
	//=================================================
	
	
	
	private ImageIcon createImageIcon(String imagePath, int width, int height) {
	    if (imagePath == null || imagePath.trim().isEmpty()) {
	        return null;
	    }
	
	    try {
	        String path = imagePath.trim().replace("\\", "/");
	        Image originalImage = null;
	
	        // 1. Thử đọc từ Classpath (trong thư mục tài nguyên / src)
	        String resourcePath = path;
	        if (!resourcePath.startsWith("/")) {
	            resourcePath = "/" + resourcePath;
	        }
	        
	        java.net.URL resource = getClass().getResource(resourcePath);
	        if (resource != null) {
	            originalImage = new ImageIcon(resource).getImage();
	        } else {
	            // 2. Nếu không tìm thấy trong resource, thử đọc trực tiếp từ ổ cứng (File System)
	            java.io.File file = new java.io.File(path);
	            if (file.exists()) {
	                originalImage = javax.imageio.ImageIO.read(file);
	            } else {
	                // Thử tìm trong thư mục dự án hiện tại
	                java.io.File projectFile = new java.io.File("src" + path);
	                if (projectFile.exists()) {
	                    originalImage = javax.imageio.ImageIO.read(projectFile);
	                }
	            }
	        }
	
	        if (originalImage == null || originalImage.getWidth(null) <= 0) {
	            System.err.println("KHÔNG TÌM THẤY ẢNH HOẶC ẢNH LỖI: " + imagePath);
	            return null;
	        }
	
	        Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	        return new ImageIcon(scaledImage);
	
	    } catch (Exception e) {
	        System.err.println("Lỗi đọc ảnh: " + imagePath);
	        e.printStackTrace();
	        return null;
	    }
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
	             // Kiểm tra trạng thái rê chuột
	                boolean hovered = Boolean.TRUE.equals(
	                    getClientProperty("hovered")
	                );
	
	                // Viền xanh khi hover, viền nhạt khi bình thường
	                g2.setColor(hovered ? BLUE_PRIMARY : BORDER_LIGHT);
	                g2.setStroke(new java.awt.BasicStroke(hovered ? 2f : 1f));
	
	                g2.drawRoundRect(
	                    1, 1,
	                    getWidth() - 3,
	                    getHeight() - 3,
	                    16, 16
	                );
	                
	                g2.dispose();
	                super.paintComponent(g);
	            }
	        };
	        panel.setOpaque(false);
	        panel.setPreferredSize(new Dimension(390, 0));
	        panel.setMinimumSize(new Dimension(340, 0));
	        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
	        
	     // Panel chứa toàn bộ nội dung chi tiết
	        JPanel contentPanel = new JPanel();
	        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
	        contentPanel.setOpaque(false);
	        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	        
	        panel.setAlignmentY(Component.TOP_ALIGNMENT);
	        
	        
	
	        // Tiêu đề và nút đóng (X)
	        JPanel topBar = new JPanel(new BorderLayout());
	        topBar.setOpaque(false);
	        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
	        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);
	        
	        JLabel lblDetailHeaderTitle = new JLabel("Chi tiết dịch vụ");
	        lblDetailHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        lblDetailHeaderTitle.setForeground(TEXT_DARK);
	        topBar.add(lblDetailHeaderTitle, BorderLayout.WEST);
	
	        JButton btnCloseDetail = new JButton("×");
	        btnCloseDetail.setFont(new Font("Segoe UI", Font.BOLD, 23));
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
	
	     // Khung hình ảnh dịch vụ
	        JPanel imgPlaceholder = new JPanel(new BorderLayout()) {
	            @Override
	            protected void paintComponent(Graphics g) {
	                super.paintComponent(g);
	
	                Graphics2D g2 = (Graphics2D) g.create();
	                g2.setRenderingHint(
	                        RenderingHints.KEY_ANTIALIASING,
	                        RenderingHints.VALUE_ANTIALIAS_ON
	                );
	
	                g2.setColor(new Color(230, 238, 248));
	                g2.fillRoundRect(
	                        0, 0, getWidth(), getHeight(), 12, 12
	                );
	
	                g2.dispose();
	            }
	        };
	
	        imgPlaceholder.setOpaque(false);
	        imgPlaceholder.setMaximumSize(
	        	    new Dimension(Integer.MAX_VALUE, 200)
	        	);
	        	imgPlaceholder.setPreferredSize(new Dimension(300, 200));
	        	imgPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);
	        	
	        	
	
	        lblDetailImage = new JLabel("🦷", SwingConstants.CENTER);
	        lblDetailImage.setFont(new Font("Segoe UI", Font.PLAIN, 50));
	
	        imgPlaceholder.add(lblDetailImage, BorderLayout.CENTER);
	
	        panel.add(imgPlaceholder);
	        panel.add(Box.createVerticalStrut(12));
	        
	        
	     // ================= HÀNG 1: ICON + TÊN DỊCH VỤ =================
	        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
	        titleRow.setOpaque(false);
	        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	        // Icon chiếc răng
	        JLabel lblToothIcon = new JLabel("♧");
	        lblToothIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 17));
	        lblToothIcon.setForeground(PINK_ACCENT);
	        lblToothIcon.setHorizontalAlignment(SwingConstants.CENTER);
	        lblToothIcon.setPreferredSize(new Dimension(28, 28));
	        lblToothIcon.setOpaque(true);
	        lblToothIcon.setBackground(new Color(255, 229, 239));
	
	     // ================= TÊN DỊCH VỤ =================
	        lblDetailTitle = new JLabel("Chọn dịch vụ để xem");
	        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        lblDetailTitle.setForeground(TEXT_DARK);
	        lblDetailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
	        lblDetailTitle.setMaximumSize(
	            new Dimension(Integer.MAX_VALUE, 35)
	        );
	
	        panel.add(lblDetailTitle);
	        panel.add(Box.createVerticalStrut(8));
	
	
	        // ================= MÔ TẢ DỊCH VỤ =================
	        lblDetailDesc = new JLabel(
	            "<html>Thông tin chi tiết sẽ hiển thị ở đây.</html>"
	        );
	        lblDetailDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
	        lblDetailDesc.setForeground(MUTED);
	        lblDetailDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
	        lblDetailDesc.setMaximumSize(
	            new Dimension(Integer.MAX_VALUE, 45)
	        );
	
	        panel.add(lblDetailDesc);
	        panel.add(Box.createVerticalStrut(12));
	
	
	        // ================= GIÁ TIỀN =================
	        JPanel priceBox = new JPanel(new BorderLayout());
	        priceBox.setBackground(new Color(243, 248, 253));
	        priceBox.setBorder(new EmptyBorder(10, 12, 10, 12));
	        priceBox.setMaximumSize(
	            new Dimension(Integer.MAX_VALUE, 76)
	        );
	        priceBox.setPreferredSize(new Dimension(300, 76));
	        priceBox.setAlignmentX(Component.LEFT_ALIGNMENT);
	        priceBox.setMaximumSize(
	        	    new Dimension(Integer.MAX_VALUE, 76)
	        	);
	        
	
	        JLabel lblPriceTitle = new JLabel("Đơn giá (VNĐ)");
	        lblPriceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	        lblPriceTitle.setForeground(MUTED);
	
	        lblDetailPrice = new JLabel("0 đ");
	        lblDetailPrice.setFont(new Font("Segoe UI", Font.BOLD, 21));
	        lblDetailPrice.setForeground(PINK_ACCENT);
	
	        JPanel pBoxInner = new JPanel();
	        pBoxInner.setLayout(new BoxLayout(pBoxInner, BoxLayout.Y_AXIS));
	        pBoxInner.setOpaque(false);
	
	        pBoxInner.add(lblPriceTitle);
	        pBoxInner.add(Box.createVerticalStrut(3));
	        pBoxInner.add(lblDetailPrice);
	
	        priceBox.add(pBoxInner, BorderLayout.CENTER);
	
	        panel.add(priceBox);
	        panel.add(Box.createVerticalStrut(14));
	
	
	        // ================= THÔNG TIN DỊCH VỤ =================
	        lblDetailId = new JLabel("-");
	        lblDetailCategory = new JLabel("-");
	        lblDetailUnit = new JLabel("-");
	
	        panel.add(createDetailRow("Mã dịch vụ:", lblDetailId));
	        panel.add(Box.createVerticalStrut(9));
	
	        panel.add(createDetailRow("Loại điều trị:", lblDetailCategory));
	        panel.add(Box.createVerticalStrut(9));
	
	        panel.add(createDetailRow("Đơn vị tính:", lblDetailUnit));
	        panel.add(Box.createVerticalStrut(16));
	
	
	        // ================= NÚT SỬA / XÓA =================
	        JPanel actionBox = new JPanel(new GridLayout(1, 2, 12, 0));
	        actionBox.setOpaque(false);
	        actionBox.setMaximumSize(
	            new Dimension(Integer.MAX_VALUE, 46)
	        );
	        actionBox.setPreferredSize(new Dimension(300, 46));
	        actionBox.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	        JButton btnEditSelected = createStaticButton(
	            "Sửa", GREEN_ACCENT, Integer.MAX_VALUE, 46
	        );
	
	        JButton btnDeleteSelected = createStaticButton(
	            "Xóa", RED_ACCENT, Integer.MAX_VALUE, 46
	        );
	
	        btnEditSelected.setFont(new Font("Segoe UI", Font.BOLD, 14));
	        btnDeleteSelected.setFont(new Font("Segoe UI", Font.BOLD, 14));
	
	        btnEditSelected.addActionListener(e -> {
	            if (selectedService != null) {
	                showAddEditDialog(selectedService);
	            }
	        });
	
	        btnDeleteSelected.addActionListener(e -> {
	            if (selectedService != null) {
	                deleteService(selectedService.getMaDichVu());
	            }
	        });
	
	        actionBox.add(btnEditSelected);
	        actionBox.add(btnDeleteSelected);
	
	        panel.add(actionBox);
	        
	        
	
	     
	
	        panel.setVisible(false);
	
	        return panel;
	    }
	
	    private JPanel createDetailRow(String label, JLabel valueLabel) {

	        JPanel row = new JPanel(new BorderLayout(5, 0));
	        row.setOpaque(false);

	        row.setAlignmentX(Component.LEFT_ALIGNMENT);
	        row.setMaximumSize(
	            new Dimension(Integer.MAX_VALUE, 30)
	        );
	        row.setPreferredSize(new Dimension(300, 30));

	        JLabel lblKey = new JLabel(label);
	        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 14));
	        lblKey.setForeground(MUTED);

	        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
	        valueLabel.setForeground(TEXT_DARK);
	        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

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
	                boolean hovered = Boolean.TRUE.equals(
	                	    getClientProperty("hovered")
	                	);
	
	                	g2.setColor(hovered ? BLUE_PRIMARY : BORDER_LIGHT);
	                	g2.setStroke(new BasicStroke(hovered ? 2f : 1f));
	
	                	g2.drawRoundRect(
	                	    1, 1,
	                	    getWidth() - 3,
	                	    getHeight() - 3,
	                	    16, 16
	                	);
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
	        topImgBox.setPreferredSize(new Dimension(0, 125));
	        topImgBox.setLayout(new BorderLayout());
	        topImgBox.setBorder(new EmptyBorder(8, 8, 8, 8));
	
	     // Hiển thị ảnh dịch vụ lấy từ SQL Server
	        JLabel lblServiceImage = new JLabel("", SwingConstants.CENTER);
	        lblServiceImage.setOpaque(false);
	        ImageIcon serviceIcon = createImageIcon(getServiceImage(dv), 500, 200);
	        
	
	        if (serviceIcon != null) {
	            lblServiceImage.setIcon(serviceIcon);
	        } else {
	            lblServiceImage.setText("🦷");
	            lblServiceImage.setFont(
	                    new Font("Segoe UI", Font.PLAIN, 35)
	            );
	        }
	
	        topImgBox.add(lblServiceImage, BorderLayout.CENTER);
	        card.add(topImgBox, BorderLayout.NORTH);
	        
	        
	
	        // Phần giữa: Hiển thị đầy đủ Tên dịch vụ và Mô tả từ CSDL
	        JPanel infoBox = new JPanel();
	        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));
	        infoBox.setOpaque(false);
	        infoBox.setBorder(new EmptyBorder(10, 0, 10, 0));
	
	        JLabel lblName = new JLabel(dv.getTenDichVu());
	        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        lblName.setForeground(TEXT_DARK);
	        infoBox.add(lblName);
	        infoBox.add(Box.createVerticalStrut(4));
	
	        String desc = dv.getMoTa() != null && !dv.getMoTa().isEmpty() ? dv.getMoTa() : "Chăm sóc và điều trị chuyên nghiệp.";
	        JLabel lblDesc = new JLabel("<html><body style='width: 170px'>" + desc + "</body></html>");
	        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
	        lblDesc.setForeground(MUTED);
	        infoBox.add(lblDesc);
	        infoBox.add(Box.createVerticalStrut(10));
	
	        JLabel lblPrice = new JLabel(df.format(dv.getDonGia()) + " đ");
	        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        lblPrice.setForeground(BLUE_PRIMARY);
	        infoBox.add(lblPrice);
	
	        card.add(infoBox, BorderLayout.CENTER);
	
	        // Phần dưới: Nút "Xem chi tiết"
	        JButton btnDetail = new JButton("Xem chi tiết →");
	        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 16));
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
	
	     // Click vào card
	        card.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                selectServiceForDetail(dv);
	            }
	
	            @Override
	            public void mouseEntered(MouseEvent e) {
	                card.putClientProperty("hovered", true);
	                card.repaint();
	            }
	
	            @Override
	            public void mouseExited(MouseEvent e) {
	                card.putClientProperty("hovered", false);
	                card.repaint();
	            }
	        });
	
	        return card;
	    }
	
	    private void selectServiceForDetail(DichVu dv) {
	
	        selectedService = dv;
	
	        lblDetailId.setText(
	                dv.getMaDichVu() != null ? dv.getMaDichVu() : "-"
	        );
	
	        lblDetailTitle.setText(dv.getTenDichVu());
	
	        lblDetailPrice.setText(
	                new DecimalFormat("#,###").format(dv.getDonGia()) + " đ"
	        );
	
	        lblDetailCategory.setText(
	                dv.getLoaiDieuTri() != null ? dv.getLoaiDieuTri() : "-"
	        );
	
	        lblDetailUnit.setText(
	                dv.getDonViTinh() != null ? dv.getDonViTinh() : "-"
	        );
	
	        lblDetailDesc.setText(
	                "<html><body style='width: 280px'>"
	                + (dv.getMoTa() != null && !dv.getMoTa().isEmpty()
	                ? dv.getMoTa() : "Không có mô tả chi tiết.")
	                + "</body></html>"
	        );
	
	        // Hiển thị ảnh từ SQL
	        ImageIcon icon = createImageIcon(
	        	    getServiceImage(dv), 340, 165
	        	);
	        
	        if (icon != null) {
	            lblDetailImage.setText("");
	            lblDetailImage.setIcon(icon);
	        } else {
	            lblDetailImage.setIcon(null);
	            lblDetailImage.setText("🦷");
	            lblDetailImage.setFont(
	                    new Font("Segoe UI", Font.PLAIN, 50)
	            );
	        }
	
	        detailPanel.setVisible(true);
	        detailPanel.revalidate();
	        detailPanel.repaint();
	    }
	
	    // Dialog Thêm hoặc Sửa dịch vụ
	    private void showAddEditDialog(DichVu editTarget) {
	
	        boolean isEdit = (editTarget != null);
	
	        JDialog dialog = new JDialog(
	                (Frame) SwingUtilities.getWindowAncestor(this),
	                isEdit ? "Chỉnh Sửa Dịch Vụ Nha Khoa"
	                       : "Thêm Dịch Vụ Mới",
	                true
	        );
	
	        dialog.setSize(500, 680);
	        dialog.setLocationRelativeTo(this);
	        dialog.setLayout(new BorderLayout());
	
	        JPanel form = new JPanel();
	        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
	        form.setBorder(new EmptyBorder(25, 25, 25, 25));
	        form.setBackground(Color.WHITE);
	
	        // Các trường thông tin
	        JTextField txtId = addFieldToDialog(form, "Mã dịch vụ:");
	        JTextField txtName = addFieldToDialog(form, "Tên dịch vụ:");
	
	        form.add(createFieldLabel("Loại điều trị:"));
	        form.add(Box.createVerticalStrut(4));
	
	        JComboBox<String> cbCat = new JComboBox<>(
	                new String[]{
	                    "Nha khoa tổng quát",
	                    "Thẩm mỹ",
	                    "Chỉnh nha",
	                    "Tiểu phẫu",
	                    "Phục hình sứ"
	                }
	        );
	
	        cbCat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
	        cbCat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
	        cbCat.setAlignmentX(Component.LEFT_ALIGNMENT);
	        cbCat.setBackground(Color.WHITE);
	
	        form.add(cbCat);
	        form.add(Box.createVerticalStrut(12));
	
	        JTextField txtPrice = addFieldToDialog(form, "Đơn giá (VNĐ):");
	        JTextField txtUnit = addFieldToDialog(form, "Đơn vị tính (VD: Lần, Răng):");
	        JTextField txtDesc = addFieldToDialog(form, "Mô tả dịch vụ:");
	
	        // =================================================
	        // CHỌN HÌNH ẢNH
	        // =================================================
	
	        form.add(createFieldLabel("Hình ảnh dịch vụ:"));
	        form.add(Box.createVerticalStrut(6));
	
	
	     // =================================================
	     // CHỌN HÌNH ẢNH DỊCH VỤ
	     // =================================================
	
	     // Lưu đường dẫn ảnh dưới dạng String
	     final String[] imagePath = {
	         isEdit ? editTarget.getHinhAnh() : null
	     };
	
	     // Khung xem trước ảnh
	     JLabel lblImagePreview = new JLabel(
	             "Chưa chọn hình ảnh",
	             SwingConstants.CENTER
	     );
	
	     lblImagePreview.setPreferredSize(new Dimension(160, 110));
	     lblImagePreview.setMinimumSize(new Dimension(160, 110));
	     lblImagePreview.setBorder(
	             BorderFactory.createLineBorder(BORDER_LIGHT)
	     );
	     lblImagePreview.setOpaque(true);
	     lblImagePreview.setBackground(new Color(243, 248, 253));
	
	     // Hiển thị ảnh cũ khi chỉnh sửa
	     if (imagePath[0] != null && !imagePath[0].trim().isEmpty()) {
	
	         ImageIcon oldIcon = createImageIcon(
	                 imagePath[0], 150, 100
	         );
	
	         if (oldIcon != null) {
	             lblImagePreview.setText("");
	             lblImagePreview.setIcon(oldIcon);
	         }
	     }
	
	     // Nút chọn ảnh
	     JButton btnChooseImage = createStaticButton(
	             "Chọn ảnh", PINK_ACCENT, 120, 38
	     );
	
	     btnChooseImage.addActionListener(e -> {
	
	         JFileChooser chooser = new JFileChooser();
	
	         chooser.setDialogTitle("Chọn hình ảnh dịch vụ");
	
	         javax.swing.filechooser.FileNameExtensionFilter filter =
	                 new javax.swing.filechooser.FileNameExtensionFilter(
	                         "Hình ảnh JPG, JPEG, PNG",
	                         "jpg", "jpeg", "png"
	                 );
	
	         chooser.setFileFilter(filter);
	
	         if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
	
	             try {
	
	                 // Lấy file ảnh được chọn
	                 java.io.File file = chooser.getSelectedFile();
	
	                 // Kiểm tra dung lượng ảnh
	                 if (file.length() > 5 * 1024 * 1024) {
	
	                     JOptionPane.showMessageDialog(
	                             dialog,
	                             "Hình ảnh không được vượt quá 5MB!",
	                             "Cảnh báo",
	                             JOptionPane.WARNING_MESSAGE
	                     );
	
	                     return;
	                 }
	
	                 // Kiểm tra nội dung file có phải ảnh hợp lệ không
	                 java.awt.image.BufferedImage bufferedImage =
	                         javax.imageio.ImageIO.read(file);
	
	                 if (bufferedImage == null) {
	
	                     JOptionPane.showMessageDialog(
	                             dialog,
	                             "File được chọn không phải hình ảnh hợp lệ!",
	                             "Lỗi",
	                             JOptionPane.ERROR_MESSAGE
	                     );
	
	                     return;
	                 }
	
	                 // Tạo thư mục lưu ảnh trong dự án
	                 java.nio.file.Path imageFolder =
	                         java.nio.file.Paths.get("src", "img", "dichvu");
	
	                 java.nio.file.Files.createDirectories(imageFolder);
	
	                 // Đường dẫn lưu ảnh
	                 java.nio.file.Path destination =
	                         imageFolder.resolve(file.getName());
	
	                 // Sao chép ảnh vào thư mục dự án
	                 if (!file.toPath().toAbsolutePath().normalize()
	                         .equals(destination.toAbsolutePath().normalize())) {
	
	                     java.nio.file.Files.copy(
	                             file.toPath(),
	                             destination,
	                             java.nio.file.StandardCopyOption.REPLACE_EXISTING
	                     );
	                 }
	
	                 // Lưu đường dẫn tương đối để đưa vào SQL
	                 imagePath[0] = "/img/dichvu/" + file.getName();
	
	                 // Hiển thị ảnh xem trước
	                 ImageIcon preview = createImageIcon(
	                         imagePath[0], 150, 100
	                 );
	
	                 if (preview != null) {
	
	                     lblImagePreview.setText("");
	                     lblImagePreview.setIcon(preview);
	
	                 } else {
	
	                     lblImagePreview.setIcon(null);
	                     lblImagePreview.setText("Không thể hiển thị ảnh");
	                 }
	
	             } catch (Exception ex) {
	
	                 ex.printStackTrace();
	
	                 JOptionPane.showMessageDialog(
	                         dialog,
	                         "Không thể lưu hoặc đọc hình ảnh: " + ex.getMessage(),
	                         "Lỗi",
	                         JOptionPane.ERROR_MESSAGE
	                 );
	             }
	         }
	     });
	
	     // Panel chứa ảnh và nút chọn ảnh
	     JPanel imagePanel = new JPanel(
	             new FlowLayout(FlowLayout.LEFT, 12, 0)
	     );
	
	     imagePanel.setOpaque(false);
	     imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	     imagePanel.add(lblImagePreview);
	     imagePanel.add(btnChooseImage);
	
	     form.add(imagePanel);
	     form.add(Box.createVerticalStrut(15));
	
	     
	        form.add(imagePanel);
	        form.add(Box.createVerticalStrut(15));
	
	        // =================================================
	        // ĐỔ DỮ LIỆU KHI CHỈNH SỬA
	        // =================================================
	
	        if (isEdit) {
	
	            txtId.setText(editTarget.getMaDichVu());
	            txtId.setEnabled(false	);
	
	            txtName.setText(editTarget.getTenDichVu());
	            cbCat.setSelectedItem(editTarget.getLoaiDieuTri());
	
	            txtPrice.setText(String.valueOf(editTarget.getDonGia()));
	            txtUnit.setText(editTarget.getDonViTinh());
	            txtDesc.setText(editTarget.getMoTa());
	        }
	
	        // =================================================
	        // NÚT LƯU
	        // =================================================
	
	        JButton btnSaveDialog = createStaticButton(
	                "Lưu thông tin",
	                BLUE_PRIMARY,
	                Integer.MAX_VALUE,
	                40
	        );
	
	        btnSaveDialog.addActionListener(e -> {
	
	            String id = txtId.getText().trim();
	            String name = txtName.getText().trim();
	            String cat = (String) cbCat.getSelectedItem();
	            String priceStr = txtPrice.getText().trim();
	            String unit = txtUnit.getText().trim();
	            String desc = txtDesc.getText().trim();
	
	            if (id.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
	
	                JOptionPane.showMessageDialog(
	                        dialog,
	                        "Vui lòng điền đầy đủ Mã, Tên và Đơn giá dịch vụ!",
	                        "Cảnh báo",
	                        JOptionPane.WARNING_MESSAGE
	                );
	
	                return;
	            }
	
	            try {
	
	                double price = Double.parseDouble(
	                        priceStr.replace(".", "").replace(",", ".")
	                );
	
	                if (price < 0) {
	                    throw new NumberFormatException();
	                }
	
	                // Tạo đối tượng dịch vụ có hình ảnh
	                DichVu dv = new DichVu(
	                        id,
	                        name,
	                        cat,
	                        price,
	                        unit,
	                        desc,
	                        imagePath[0]
	                );
	                
	
	                boolean success = isEdit
	                        ? dichVuDAO.update(dv)
	                        : dichVuDAO.insert(dv);
	
	                if (success) {
	
	                    JOptionPane.showMessageDialog(
	                            dialog,
	                            isEdit
	                            ? "Cập nhật dịch vụ thành công!"
	                            : "Thêm dịch vụ thành công!"
	                    );
	
	                    dialog.dispose();
	
	                    selectedService = null;
	
	                    detailPanel.setVisible(false);
	
	                    reloadServiceGrid("");
	
	                } else {
	
	                    JOptionPane.showMessageDialog(
	                            dialog,
	                            "Thực hiện thất bại!",
	                            "Lỗi",
	                            JOptionPane.ERROR_MESSAGE
	                    );
	                }
	
	            } catch (NumberFormatException ex) {
	
	                JOptionPane.showMessageDialog(
	                        dialog,
	                        "Đơn giá phải là số hợp lệ!",
	                        "Lỗi",
	                        JOptionPane.WARNING_MESSAGE
	                );
	
	            } catch (SQLException ex) {
	
	                ex.printStackTrace();
	
	                JOptionPane.showMessageDialog(
	                        dialog,
	                        "Lỗi cơ sở dữ liệu: " + ex.getMessage(),
	                        "Lỗi",
	                        JOptionPane.ERROR_MESSAGE
	                );
	            }
	        });
	
	        form.add(btnSaveDialog);
	
	        // Cuộn nội dung nếu cửa sổ không đủ chiều cao
	        JScrollPane scrollPane = new JScrollPane(form);
	
	        scrollPane.setBorder(null);
	
	        scrollPane.setHorizontalScrollBarPolicy(
	                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	        );
	
	        dialog.add(scrollPane, BorderLayout.CENTER);
	
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