package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import sql.DBConnection;

public class ManHinhKeDonThuocDienTu extends JPanel {

    private final Color BLUE = new Color(11, 95, 204);
    private final Color NAVY = new Color(15, 29, 69);
    private final Color PINK = new Color(174, 35, 86);
    private final Color BG = new Color(243, 248, 253);
    private final Color BORDER = new Color(224, 233, 243);
    private final Color TEXT = new Color(35, 55, 90);
    private final Color MUTED = new Color(74, 88, 105);
    private final Color GREEN = new Color(24, 117, 58);
    private final Color GREEN_BG = new Color(222, 250, 235);

    private static String sharedMaKeHoach = "";
    private Set<String> prescribedPatients = new HashSet<>();

    private CardLayout subCardLayout;
    private JPanel subContainer;
    private JButton btnSubListPlan, btnSubPrescription;

    private JLabel lblPatientCode, lblPatientName, lblPatientPhone, lblPatientDoctor;
    private JTextField txtSoLuong, txtCachDung, txtLieuDung, txtGhiChuThuoc, txtDonViTinh;

    private JComboBox<MucThuocHopChon> cboChonThuoc;
    private JLabel lblImageMed, lblValMaThuoc, lblValTenThuoc, lblValHoatChat, lblValNhomThuoc, lblValDangBaoChe, lblValDuongDung, lblValNhaSX, lblValQuyCach;

    private JTable tblPrescription;
    private DefaultTableModel prescriptionModel;
    private JLabel lblTotalMedicines;

    private DefaultTableModel planListModel;
    private JTable tblPlanList;
    private JLabel lblTotalPlans;

    public static void setSharedMaKeHoach(String ma) {
        sharedMaKeHoach = ma != null ? ma : "";
    }

    public ManHinhKeDonThuocDienTu() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        add(createTopNavigationTab(), BorderLayout.NORTH);

        subCardLayout = new CardLayout();
        subContainer = new JPanel(subCardLayout);
        subContainer.setOpaque(false);

        subContainer.add(createPlanListPage(), "SUB_LIST_PLAN");
        subContainer.add(createPrescriptionMainPage(), "SUB_PRESCRIPTION");

        add(subContainer, BorderLayout.CENTER);

        loadPrescribedStatusFromDB();

        if (sharedMaKeHoach != null && !sharedMaKeHoach.isEmpty() && !sharedMaKeHoach.equals("--")) {
            loadPatientInfoByCode(sharedMaKeHoach);
            subCardLayout.show(subContainer, "SUB_PRESCRIPTION");
            updateTabButtonStyles(btnSubPrescription, btnSubListPlan);
        } else {
            subCardLayout.show(subContainer, "SUB_LIST_PLAN");
            updateTabButtonStyles(btnSubListPlan, btnSubPrescription);
        }
    }

    private void loadPrescribedStatusFromDB() {
        prescribedPatients.clear();
        String sql = "SELECT DISTINCT maBenhAn FROM DonThuoc";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String maBA = rs.getString("maBenhAn");
                if (maBA != null) {
                    prescribedPatients.add(maBA.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createTopNavigationTab() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(14, 20, 12, 20));

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        menu.setBackground(Color.WHITE);
        menu.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(3, 8, 3, 8)
            )
        );
        menu.setPreferredSize(new Dimension(0, 60));

        btnSubListPlan = createButton("📋 Danh sách đã lập kế hoạch", PINK);
        btnSubPrescription = createButton("💊 Trang kê đơn thuốc", BG);
        btnSubPrescription.setForeground(NAVY);

        btnSubListPlan.addActionListener(e -> {
            subCardLayout.show(subContainer, "SUB_LIST_PLAN");
            updateTabButtonStyles(btnSubListPlan, btnSubPrescription);
            loadPlannedDataToTable();
        });

        btnSubPrescription.addActionListener(e -> {
            subCardLayout.show(subContainer, "SUB_PRESCRIPTION");
            updateTabButtonStyles(btnSubPrescription, btnSubListPlan);
            int row = tblPlanList.getSelectedRow();
            if (row >= 0) {
                String maBN = String.valueOf(planListModel.getValueAt(row, 0));
                loadPatientInfoByCode(maBN);
            }
        });

        menu.add(btnSubListPlan);
        menu.add(btnSubPrescription);
        wrapper.add(menu, BorderLayout.CENTER);
        return wrapper;
    }

    private void updateTabButtonStyles(JButton active, JButton inactive) {
        active.setBackground(PINK);
        active.setForeground(Color.WHITE);
        inactive.setBackground(BG);
        inactive.setForeground(NAVY);
    }

    // ================= SUB TAB 1: DANH SÁCH ĐÃ LẬP KẾ HOẠCH =================
    private JPanel createPlanListPage() {
        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel headerCard = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 248, 252),
                    getWidth(), getHeight(), new Color(255, 228, 241)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(247, 190, 218));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };

        headerCard.setOpaque(false);
        headerCard.setBorder(new EmptyBorder(15, 18, 15, 18));
        headerCard.setPreferredSize(new Dimension(0, 125));

        JPanel leftPanel = new JPanel(new BorderLayout(15, 0));
        leftPanel.setOpaque(false);

        JLabel lblIllustration = new JLabel("📋");
        lblIllustration.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblIllustration.setHorizontalAlignment(SwingConstants.CENTER);
        lblIllustration.setPreferredSize(new Dimension(105, 95));

        JPanel separator = new JPanel();
        separator.setBackground(new Color(235, 135, 184));
        separator.setPreferredSize(new Dimension(2, 90));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Danh sách hạng mục đã lập kế hoạch điều trị");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel("Chọn bệnh nhân từ danh sách kế hoạch để tiến hành kê đơn thuốc điện tử");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(MUTED);

        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(8));
        titlePanel.add(subtitle);
        titlePanel.add(Box.createVerticalGlue());

        leftPanel.add(lblIllustration, BorderLayout.WEST);
        leftPanel.add(separator, BorderLayout.CENTER);
        leftPanel.add(titlePanel, BorderLayout.EAST);
        headerCard.add(leftPanel, BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filter.setOpaque(false);

        JButton btnReload = createButton("Tải lại", MUTED);
        btnReload.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReload.setPreferredSize(new Dimension(105, 42));
        btnReload.addActionListener(e -> loadPlannedDataToTable());

        filter.add(btnReload);
        headerCard.add(filter, BorderLayout.EAST);
        main.add(headerCard, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 15));
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 237, 247)),
            new EmptyBorder(18, 18, 15, 18)
        ));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel headingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headingPanel.setOpaque(false);

        JLabel iconPatient = new JLabel("💊");
        iconPatient.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconPatient.setPreferredSize(new Dimension(40, 38));
        iconPatient.setHorizontalAlignment(SwingConstants.CENTER);
        iconPatient.setOpaque(true);
        iconPatient.setBackground(new Color(255, 237, 245));

        JLabel lblHeading = new JLabel("Danh sách bệnh nhân chờ kê đơn");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblHeading.setForeground(NAVY);

        headingPanel.add(iconPatient);
        headingPanel.add(lblHeading);
        cardHeader.add(headingPanel, BorderLayout.WEST);

        tableCard.add(cardHeader, BorderLayout.NORTH);

        String[] columns = {"Mã bệnh nhân", "Tên bệnh nhân", "Dịch vụ điều trị", "Răng", "Số lượng", "Đơn giá", "Đợt điều trị", "Thao tác"};
        planListModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 7; }
        };

        tblPlanList = new JTable(planListModel);
        styleTable(tblPlanList);

        tblPlanList.getColumnModel().getColumn(7).setCellRenderer(new BoVeThaoTacDonThuoc());
        tblPlanList.getColumnModel().getColumn(7).setCellEditor(new BoSuaThaoTacDonThuoc(tblPlanList, maBN -> {
            loadPatientInfoByCode(maBN);
            subCardLayout.show(subContainer, "SUB_PRESCRIPTION");
            updateTabButtonStyles(btnSubPrescription, btnSubListPlan);
        }));

        loadPlannedDataToTable();

        JScrollPane scroll = new JScrollPane(tblPlanList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(226, 234, 245)));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(5, 0, 0, 0));

        lblTotalPlans = new JLabel("Tổng số bệnh nhân đã lập kế hoạch: 0");
        lblTotalPlans.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalPlans.setForeground(PINK);
        lblTotalPlans.setOpaque(true);
        lblTotalPlans.setBackground(new Color(255, 235, 244));
        lblTotalPlans.setBorder(new EmptyBorder(8, 14, 8, 14));

        footer.add(lblTotalPlans, BorderLayout.WEST);
        tableCard.add(footer, BorderLayout.SOUTH);

        main.add(tableCard, BorderLayout.CENTER);
        return main;
    }

    private void loadPlannedDataToTable() {
        if (planListModel == null) return;
        loadPrescribedStatusFromDB();
        planListModel.setRowCount(0);
        String sql = 
            "SELECT hs.maBenhAn, kh.tenKH, dv.tenDichVu, r.maRang, ctd.soLuong, dv.donGia " +
            "FROM HoSoBenhAn hs " +
            "JOIN KhachHang kh ON hs.maKhachHang = kh.maKhachHang " +
            "LEFT JOIN ChiTietKhamRang r ON hs.maBenhAn = r.maBenhAn " +
            "LEFT JOIN ChiTietChiDinh ctd ON hs.maBenhAn = ctd.maBenhAn AND (r.maRang = ctd.maRang OR ctd.maRang IS NULL) " +
            "LEFT JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu " +
            "WHERE hs.trangThai LIKE N'%lập kế hoạch%'";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                count++;
                String maBN = rs.getString("maBenhAn");
                String tenBN = rs.getString("tenKH");
                String tenDV = rs.getString("tenDichVu");
                int maRang = rs.getInt("maRang");
                int soLuong = rs.getInt("soLuong");
                if (soLuong <= 0) soLuong = 1;
                double donGia = rs.getDouble("donGia");

                boolean isPrescribed = prescribedPatients.contains(maBN);

                planListModel.addRow(new Object[]{
                    maBN,
                    tenBN != null ? tenBN : "--",
                    tenDV != null ? tenDV : "Khám tổng quát",
                    maRang > 0 ? "Răng " + maRang : "Chung",
                    soLuong,
                    String.format("%,.0f đ", donGia),
                    "Đợt 1",
                    isPrescribed ? "Đã kê đơn" : "Kê đơn"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lblTotalPlans != null) {
            lblTotalPlans.setText("Tổng số bệnh nhân đã lập kế hoạch: " + count);
        }
    }

    private class BoVeThaoTacDonThuoc implements TableCellRenderer {
        private final JButton btn = new JButton("Kê đơn");
        private final JLabel lbl = new JLabel("Đã kê đơn", SwingConstants.CENTER);

        public BoVeThaoTacDonThuoc() {
            btn.setUI(new BasicButtonUI());
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(BLUE);
            btn.setFocusPainted(false);
            btn.setOpaque(true);

            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(GREEN);
            lbl.setBackground(GREEN_BG);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value != null ? value.toString() : "";
            if ("Đã kê đơn".equals(status)) {
                return lbl;
            } else {
                return btn;
            }
        }
    }

    private class BoSuaThaoTacDonThuoc extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        private final JButton btn = new JButton("Kê đơn");
        private final JLabel lbl = new JLabel("Đã kê đơn", SwingConstants.CENTER);
        private String currentMaBN = "";
        private boolean isPrescribed = false;

        public BoSuaThaoTacDonThuoc(JTable table, java.util.function.Consumer<String> callback) {
            panel.setOpaque(false);

            btn.setUI(new BasicButtonUI());
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(BLUE);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                BoSuaThaoTacDonThuoc.this.fireEditingStopped();
                if (!isPrescribed && callback != null) {
                    callback.accept(currentMaBN);
                }
            });

            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(GREEN);
            lbl.setBackground(GREEN_BG);
            lbl.setOpaque(true);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentMaBN = String.valueOf(table.getValueAt(row, 0));
            String status = value != null ? value.toString() : "";
            isPrescribed = "Đã kê đơn".equals(status);

            panel.removeAll();
            if (isPrescribed) {
                panel.add(lbl);
            } else {
                panel.add(btn);
            }
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return isPrescribed ? "Đã kê đơn" : "Kê đơn";
        }

        @Override
        public boolean isCellEditable(java.util.EventObject e) {
            int row = tblPlanList.getSelectedRow();
            if (row >= 0) {
                String status = String.valueOf(planListModel.getValueAt(row, 7));
                return !"Đã kê đơn".equals(status);
            }
            return true;
        }
    }

    // ================= SUB TAB 2: TRANG KÊ ĐƠN THUỐC =================
    private JPanel createPrescriptionMainPage() {
        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(12, 15, 15, 15));

        main.add(createPatientHeader(), BorderLayout.NORTH);

        JPanel splitPanel = new JPanel(new GridBagLayout());
        splitPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        gbc.gridx = 0; gbc.weightx = 0.46; gbc.insets = new Insets(0, 0, 0, 10);
        splitPanel.add(createMedicineCardPanel(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.54; gbc.insets = new Insets(0, 0, 0, 0);
        splitPanel.add(createTableMedicineCard(), gbc);

        main.add(splitPanel, BorderLayout.CENTER);
        return main;
    }

    private JPanel createPatientHeader() {
        JPanel panel = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(232, 243, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(125, 190, 255));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 22, 22);
                g2.dispose();
            }
        };

        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 20, 8, 20));
        panel.setPreferredSize(new Dimension(0, 110));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblPatientCode = createValue("--");
        lblPatientName = createValue("--");
        lblPatientPhone = createValue("--");
        lblPatientDoctor = createValue("--");

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(130, 85));
        
        JLabel lblIcon = new JLabel("💊");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        iconPanel.add(lblIcon);

        JPanel separator = new JPanel();
        separator.setBackground(new Color(115, 185, 245));
        separator.setPreferredSize(new Dimension(2, 85));

        JPanel leftPanel = new JPanel(new BorderLayout(18, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(iconPanel, BorderLayout.CENTER);
        leftPanel.add(separator, BorderLayout.EAST);
        leftPanel.setPreferredSize(new Dimension(150, 85));

        panel.add(leftPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 22, 0));
        infoPanel.setOpaque(false);

        infoPanel.add(infoItem("Mã Kế hoạch / BN", lblPatientCode));
        infoPanel.add(infoItem("Họ và tên bệnh nhân", lblPatientName));
        infoPanel.add(infoItem("Số điện thoại", lblPatientPhone));
        infoPanel.add(infoItem("Bác sĩ kê đơn", lblPatientDoctor));

        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(130, 85));

        JButton btnBackToList = createButton("← Danh sách", MUTED);
        btnBackToList.setPreferredSize(new Dimension(130, 40));
        btnBackToList.addActionListener(e -> {
            subCardLayout.show(subContainer, "SUB_LIST_PLAN");
            updateTabButtonStyles(btnSubListPlan, btnSubPrescription);
            loadPlannedDataToTable();
        });

        rightPanel.add(btnBackToList);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel infoItem(String title, JLabel value) {
        JPanel item = new JPanel(new GridLayout(2, 1, 0, 5));
        item.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(NAVY);

        value.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        value.setForeground(NAVY);

        item.add(lblTitle);
        item.add(value);
        return item;
    }

    private JLabel createValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(NAVY);
        return label;
    }

    // Thiết kế chuẩn giống hệt khung "Thông tin thuốc" chuẩn ở ảnh 2
    private JPanel createMedicineCardPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        // Tiêu đề card chuẩn
        JLabel title = createTitle("Thông tin thuốc & Kê đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Combobox chọn thuốc
        JPanel selectPanel = new JPanel(new BorderLayout(8, 0));
        selectPanel.setOpaque(false);
        selectPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel lblSelect = new JLabel("Chọn thuốc:");
        lblSelect.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelect.setForeground(NAVY);
        selectPanel.add(lblSelect, BorderLayout.WEST);

        cboChonThuoc = new JComboBox<>();
        styleCombo(cboChonThuoc);
        loadMedicineComboboxData();

        selectPanel.add(cboChonThuoc, BorderLayout.CENTER);
        body.add(selectPanel);
        body.add(Box.createVerticalStrut(10));

        // Khung thông tin chi tiết thuốc chuẩn (Ảnh bên trái, thông tin chi tiết bên phải giống hệt "Danh mục thuốc chuẩn")
        JPanel medInfoCard = new JPanel(new BorderLayout(14, 0));
        medInfoCard.setBackground(Color.WHITE);
        medInfoCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 228, 245)),
            new EmptyBorder(12, 12, 12, 12)
        ));
        medInfoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        medInfoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));

        lblImageMed = new JLabel("Ảnh", SwingConstants.CENTER);
        lblImageMed.setPreferredSize(new Dimension(110, 140));
        lblImageMed.setOpaque(true);
        lblImageMed.setBackground(new Color(250, 252, 255));
        lblImageMed.setBorder(BorderFactory.createLineBorder(BORDER));
        lblImageMed.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImageMed.setForeground(MUTED);

        JPanel infoDetails = new JPanel(new GridLayout(8, 1, 0, 2));
        infoDetails.setOpaque(false);

        lblValTenThuoc = new JLabel("Chưa chọn thuốc");
        lblValTenThuoc.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValTenThuoc.setForeground(BLUE);

        lblValMaThuoc = new JLabel("Mã thuốc: --");
        lblValHoatChat = new JLabel("Hoạt chất chính: --");
        lblValNhomThuoc = new JLabel("Nhóm thuốc: --");
        lblValDangBaoChe = new JLabel("Dạng bào chế: --");
        lblValDuongDung = new JLabel("Đường dùng: --");
        lblValNhaSX = new JLabel("Nhà sản xuất: --");
        lblValQuyCach = new JLabel("Quy cách: --");

        Font detailFont = new Font("Segoe UI", Font.PLAIN, 12);
        lblValMaThuoc.setFont(detailFont);
        lblValHoatChat.setFont(detailFont);
        lblValNhomThuoc.setFont(detailFont);
        lblValDangBaoChe.setFont(detailFont);
        lblValDuongDung.setFont(detailFont);
        lblValNhaSX.setFont(detailFont);
        lblValQuyCach.setFont(detailFont);

        infoDetails.add(lblValTenThuoc);
        infoDetails.add(lblValMaThuoc);
        infoDetails.add(lblValHoatChat);
        infoDetails.add(lblValNhomThuoc);
        infoDetails.add(lblValDangBaoChe);
        infoDetails.add(lblValDuongDung);
        infoDetails.add(lblValNhaSX);
        infoDetails.add(lblValQuyCach);

        medInfoCard.add(lblImageMed, BorderLayout.WEST);
        medInfoCard.add(infoDetails, BorderLayout.CENTER);
        body.add(medInfoCard);
        body.add(Box.createVerticalStrut(12));

        // Form nhập thông tin kê đơn gọn gàng, cân đối
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setOpaque(false);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSoLuong = new JTextField("1");
        styleTextField(txtSoLuong);

        txtDonViTinh = new JTextField("--");
        txtDonViTinh.setEditable(false);
        styleTextField(txtDonViTinh);
        txtDonViTinh.setBackground(new Color(248, 250, 252));

        txtCachDung = new JTextField("Uống sau khi ăn");
        styleTextField(txtCachDung);

        txtLieuDung = new JTextField("Sáng 1 viên, Tối 1 viên");
        styleTextField(txtLieuDung);

        txtGhiChuThuoc = new JTextField();
        styleTextField(txtGhiChuThuoc);

        form.add(createFieldLabel("Số lượng:"));
        form.add(txtSoLuong);
        form.add(createFieldLabel("Đơn vị tính:"));
        form.add(txtDonViTinh);
        form.add(createFieldLabel("Cách dùng:"));
        form.add(txtCachDung);
        form.add(createFieldLabel("Liều dùng:"));
        form.add(txtLieuDung);
        form.add(createFieldLabel("Ghi chú:"));
        form.add(txtGhiChuThuoc);

        body.add(form);
        card.add(body, BorderLayout.CENTER);

        cboChonThuoc.addActionListener(e -> {
            MucThuocHopChon item = (MucThuocHopChon) cboChonThuoc.getSelectedItem();
            if (item != null && item.medicine != null) {
                updateMedicineCardDisplay(item.medicine);
            }
        });

        if (cboChonThuoc.getItemCount() > 0) {
            MucThuocHopChon firstItem = cboChonThuoc.getItemAt(0);
            if (firstItem != null && firstItem.medicine != null) {
                updateMedicineCardDisplay(firstItem.medicine);
            }
        }

        JButton btnAddMedicine = createButton("+ Thêm vào đơn thuốc", BLUE);
        btnAddMedicine.setPreferredSize(new Dimension(0, 38));
        btnAddMedicine.addActionListener(e -> addMedicineToTable());
        card.add(btnAddMedicine, BorderLayout.SOUTH);

        return card;
    }

    private void updateMedicineCardDisplay(DuLieuThuoc m) {
        lblValTenThuoc.setText(safe(m.name));
        lblValMaThuoc.setText("Mã thuốc: " + safe(m.code));
        lblValHoatChat.setText("Hoạt chất chính: " + safe(m.activeIngredient));
        lblValNhomThuoc.setText("Nhóm thuốc: " + safe(m.group));
        lblValDangBaoChe.setText("Dạng bào chế: " + safe(m.form));
        lblValDuongDung.setText("Đường dùng: " + safe(m.route));
        lblValNhaSX.setText("Nhà sản xuất: " + safe(m.manufacturer));
        lblValQuyCach.setText("Quy cách: " + safe(m.packageInfo));

        if (txtDonViTinh != null) {
            String unit = m.packageInfo != null && !m.packageInfo.isEmpty() ? m.packageInfo : m.form;
            txtDonViTinh.setText(unit != null ? unit : "Viên");
        }

        lblImageMed.setIcon(null);
        lblImageMed.setText("Ảnh");
        if (m.imagePath != null && !m.imagePath.trim().isEmpty()) {
            ImageIcon icon = loadImage(m.imagePath, 100, 130);
            if (icon != null) {
                lblImageMed.setIcon(icon);
                lblImageMed.setText("");
            }
        }
    }

    private ImageIcon loadImage(String path, int width, int height) {
        try {
            URL url = getClass().getResource(path);
            ImageIcon original = null;
            if (url != null) {
                original = new ImageIcon(url);
            } else {
                File file = new File(path);
                if (!file.exists()) {
                    file = new File("src/main/resources" + (path.startsWith("/") ? path : "/" + path));
                }
                if (!file.exists()) {
                    file = new File("resources" + (path.startsWith("/") ? path : "/" + path));
                }
                if (file.exists()) {
                    original = new ImageIcon(file.getAbsolutePath());
                }
            }
            if (original == null || original.getIconWidth() <= 0) return null;
            Image image = original.getImage();
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel createTableMedicineCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245)),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = createTitle("Danh sách thuốc đã kê cho bệnh nhân");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(title, BorderLayout.NORTH);

        String[] columns = {"STT", "Mã thuốc", "Tên thuốc", "SL", "Đơn vị tính", "Cách dùng", "Liều dùng", "Xóa"};
        prescriptionModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 7; }
        };

        tblPrescription = new JTable(prescriptionModel);
        styleTable(tblPrescription);

        tblPrescription.getColumnModel().getColumn(7).setCellRenderer(new BoVeThaoTac("Xóa"));
        tblPrescription.getColumnModel().getColumn(7).setCellEditor(new BoSuaThaoTac(tblPrescription, row -> {
            if (row >= 0 && row < prescriptionModel.getRowCount()) {
                prescriptionModel.removeRow(row);
                updateSTT();
            }
        }, "Xóa"));

        card.add(new JScrollPane(tblPrescription), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblTotalMedicines = new JLabel("Tổng số loại thuốc: 0");
        lblTotalMedicines.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalMedicines.setForeground(PINK);
        lblTotalMedicines.setOpaque(true);
        lblTotalMedicines.setBackground(new Color(255, 235, 244));
        lblTotalMedicines.setBorder(new EmptyBorder(6, 12, 6, 12));

        JPanel rightFooter = new JPanel(new GridLayout(1, 2, 8, 0));
        rightFooter.setOpaque(false);

        JButton btnPrint = createButton("🖨 In đơn", Color.WHITE);
        btnPrint.setForeground(BLUE);
        btnPrint.setBorder(BorderFactory.createLineBorder(BLUE));
        btnPrint.addActionListener(e -> {
            try { tblPrescription.print(); } catch (Exception ignored) {}
        });

        JButton btnSavePrescription = createButton("✓ Lưu đơn kê", PINK);
        btnSavePrescription.addActionListener(e -> savePrescriptionToDatabase());

        rightFooter.add(btnPrint);
        rightFooter.add(btnSavePrescription);

        footer.add(lblTotalMedicines, BorderLayout.WEST);
        footer.add(rightFooter, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private void addMedicineToTable() {
        MucThuocHopChon selectedItem = (MucThuocHopChon) cboChonThuoc.getSelectedItem();
        if (selectedItem == null || selectedItem.medicine == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thuốc từ danh mục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DuLieuThuoc med = selectedItem.medicine;
        int soLuong = 1;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            if (soLuong <= 0) soLuong = 1;
        } catch (Exception ignored) {}

        prescriptionModel.addRow(new Object[]{
            prescriptionModel.getRowCount() + 1,
            med.code,
            med.name,
            soLuong,
            txtDonViTinh.getText().trim(),
            txtCachDung.getText().trim(),
            txtLieuDung.getText().trim(),
            "Xóa"
        });
        updateSTT();
    }

    private void updateSTT() {
        for (int i = 0; i < prescriptionModel.getRowCount(); i++) {
            prescriptionModel.setValueAt(i + 1, i, 0);
        }
        lblTotalMedicines.setText("Tổng số loại thuốc: " + prescriptionModel.getRowCount());
    }

    private void savePrescriptionToDatabase() {
        String maBenhAn = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
        if (maBenhAn.isEmpty() || maBenhAn.equals("--")) {
            JOptionPane.showMessageDialog(this, "Chưa có mã bệnh án hoặc mã kế hoạch hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (prescriptionModel == null || prescriptionModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Đơn thuốc chưa có loại thuốc nào để lưu!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO DonThuoc (maDonThuoc, maBenhAn, maNhanVien, maKhachHang, maThuoc, tenThuoc, ngayKe, soLuong, cachDung, ghiChu) " +
                     "VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            String maKH = "";
            String maNV = "";
            String infoSql = "SELECT hs.maKhachHang, hs.maNhanVien FROM HoSoBenhAn hs WHERE hs.maBenhAn = ?";
            try (PreparedStatement psInfo = conn.prepareStatement(infoSql)) {
                psInfo.setString(1, maBenhAn);
                try (ResultSet rs = psInfo.executeQuery()) {
                    if (rs.next()) {
                        maKH = rs.getString("maKhachHang");
                        maNV = rs.getString("maNhanVien");
                    }
                }
            }

            int successCount = 0;
            for (int i = 0; i < prescriptionModel.getRowCount(); i++) {
                String maThuoc = String.valueOf(prescriptionModel.getValueAt(i, 1));
                String tenThuoc = String.valueOf(prescriptionModel.getValueAt(i, 2));
                int soLuong = Integer.parseInt(String.valueOf(prescriptionModel.getValueAt(i, 3)));
                String donVi = String.valueOf(prescriptionModel.getValueAt(i, 4));
                String cachDung = String.valueOf(prescriptionModel.getValueAt(i, 5));
                String lieuDung = String.valueOf(prescriptionModel.getValueAt(i, 6));

                String maDonThuoc = "DT_" + System.currentTimeMillis() + "_" + (i + 1);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maDonThuoc);
                    ps.setString(2, maBenhAn);
                    ps.setString(3, maNV != null ? maNV : "NV002");
                    ps.setString(4, maKH != null ? maKH : "KH001");
                    ps.setString(5, maThuoc);
                    ps.setString(6, tenThuoc);
                    ps.setInt(7, soLuong);
                    ps.setString(8, "ĐVT: " + donVi + " | Cách dùng: " + cachDung + " | Liều dùng: " + lieuDung);
                    ps.setString(9, "Đã kê đơn điện tử");
                    ps.executeUpdate();
                    successCount++;
                }
            }

            prescribedPatients.add(maBenhAn);

            JOptionPane.showMessageDialog(this, "Đã lưu thành công " + successCount + " loại thuốc vào đơn kê của mã bệnh án: " + maBenhAn, "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            prescriptionModel.setRowCount(0);
            updateSTT();
            loadPlannedDataToTable();
            subCardLayout.show(subContainer, "SUB_LIST_PLAN");
            updateTabButtonStyles(btnSubListPlan, btnSubPrescription);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu đơn thuốc vào CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadPatientInfoByCode(String maLH) {
        if (maLH == null || maLH.trim().isEmpty() || maLH.equals("--")) return;
        sharedMaKeHoach = maLH;
        if (lblPatientCode != null) lblPatientCode.setText(maLH);

        String sql = 
            "SELECT kh.tenKH, kh.sdt, nv.tenNhanVien " +
            "FROM HoSoBenhAn hs " +
            "JOIN KhachHang kh ON hs.maKhachHang = kh.maKhachHang " +
            "LEFT JOIN NhanVien nv ON hs.maNhanVien = nv.maNhanVien " +
            "WHERE hs.maBenhAn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (lblPatientName != null) lblPatientName.setText(rs.getString("tenKH"));
                    if (lblPatientPhone != null) lblPatientPhone.setText(rs.getString("sdt"));
                    if (lblPatientDoctor != null) lblPatientDoctor.setText(rs.getString("tenNhanVien") != null ? rs.getString("tenNhanVien") : "Bác sĩ nha khoa");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMedicineComboboxData() {
        cboChonThuoc.removeAllItems();

        String query = 
            "SELECT T.MaThuoc, T.TenThuoc, T.HoatChatChinh, " +
            "N.TenNhomThuoc, DBC.TenDangBaoChe, DD.TenDuongDung, T.NhaSanXuat, T.QuyCachDongGoi, T.HinhAnh " +
            "FROM THUOC T " +
            "LEFT JOIN NHOMTHUOC N ON T.MaNhomThuoc = N.MaNhomThuoc " +
            "LEFT JOIN DANGBAOCHE DBC ON T.MaDangBaoChe = DBC.MaDangBaoChe " +
            "LEFT JOIN DUONGDUNG DD ON T.MaDuongDung = DD.MaDuongDung " +
            "WHERE T.TrangThai = 1 ORDER BY T.TenThuoc";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DuLieuThuoc m = new DuLieuThuoc();
                m.code = rs.getString("MaThuoc");
                m.name = rs.getString("TenThuoc");
                m.activeIngredient = rs.getString("HoatChatChinh");
                m.group = rs.getString("TenNhomThuoc");
                m.form = rs.getString("TenDangBaoChe");
                m.route = rs.getString("TenDuongDung");
                m.manufacturer = rs.getString("NhaSanXuat");
                m.packageInfo = rs.getString("QuyCachDongGoi");
                m.imagePath = rs.getString("HinhAnh");

                cboChonThuoc.addItem(new MucThuocHopChon(m));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class DuLieuThuoc {
        public String code, name, activeIngredient, group, form, route, manufacturer, packageInfo, imagePath;
    }

    public static class MucThuocHopChon {
        public DuLieuThuoc medicine;
        public MucThuocHopChon(DuLieuThuoc medicine) {
            this.medicine = medicine;
        }
        @Override
        public String toString() {
            return medicine.name + " (" + medicine.code + ")";
        }
    }

    private JPanel createCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(NAVY);
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(6, 8, 6, 8)));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        boolean isLight = color.equals(Color.WHITE) || color.equals(BG) || color.equals(new Color(238, 245, 253));
        Color textColor = isLight ? NAVY : Color.WHITE;

        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(textColor);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setEnabled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isLight ? BORDER : color),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setRowHeight(36);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(225, 239, 255));
        table.setSelectionForeground(NAVY);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(238, 245, 253));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 38));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private class BoVeThaoTac extends JButton implements javax.swing.table.TableCellRenderer {
        BoVeThaoTac(String text) {
            setUI(new BasicButtonUI());
            setText(text);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(PINK);
            setBackground(new Color(255, 239, 245));
            setFocusPainted(false);
            setOpaque(true);
            setContentAreaFilled(true);
            setBorderPainted(false);
            setBorder(new EmptyBorder(4, 8, 4, 8));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
            return this;
        }
    }

    private class BoSuaThaoTac extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow = -1;

        BoSuaThaoTac(JTable table, java.util.function.IntConsumer action, String text) {
            super(new JCheckBox());
            button = new JButton(text);
            button.setUI(new BasicButtonUI());
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(PINK);
            button.setBackground(new Color(255, 239, 245));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setBorder(new EmptyBorder(4, 8, 4, 8));

            button.addActionListener(e -> {
                int row = selectedRow;
                fireEditingStopped();
                if (row >= 0) action.accept(row);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            selectedRow = table.convertRowIndexToModel(row);
            return button;
        }
    }
}