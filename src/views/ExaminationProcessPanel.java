package views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import models.dao.ChiTietChiDinhDAO;
import models.dao.ChiTietKhamRangDAO;
import models.dao.DichVuDAO;
import models.dao.HoSoBenhAnDAO;
import models.dao.LichHenDAO;
import models.entity.ChiTietChiDinh;
import models.entity.HoSoBenhAn;
import sql.DBConnection;

public class ExaminationProcessPanel extends JPanel {

    private final Color BLUE = new Color(11, 95, 204);
    private final Color NAVY = new Color(15, 29, 69);
    private final Color PINK = new Color(174, 35, 86);
    private final Color BG = new Color(243, 248, 253);
    private final Color BORDER = new Color(224, 233, 243);
    private final Color TEXT = new Color(35, 55, 90);
    private final Color MUTED = new Color(74, 88, 105);

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnReception, btnExamination, btnPhieuKhamList, btnTreatmentPlan;

    private JTable tblReception, tblIndication, tblExamined;
    private DefaultTableModel receptionModel, indicationModel, examinedModel;
    
    private LichHenDAO lichHenDAO;

    private JLabel lblPatientCode, lblPatientName, lblAge, lblGender, lblPhone, lblDate, lblTime;
    private JLabel lblToothCode, lblToothName, lblJaw, lblPosition, lblType;
    private JComboBox<String> cboCondition, cboSeverity;
    private JTextArea txtSymptom, txtDiagnosis, txtNote;
    private StandardToothChartPanel toothSource;
    private StandardToothChartPanel.ToothData selectedTooth;
    
    private JLabel lblToothHeaderCode;
    private JLabel lblToothHeaderName;
    private JLabel lblToothHeaderType;
    
    private JComboBox<String> cboFormLoaiDieuTri;
    private JComboBox<ServiceComboItem> cboFormTenDichVu;
    
    private JLabel lblTotalPatients;
    
    private JPanel cardContainer;
    private JTextField txtSearchPhieuKham;
    private JLabel lblTotalPhieuKhamCards;

    private JLabel lblPlanMaBN, lblPlanTenBN, lblPlanSdt;
    private JLabel lblPlanTongChiPhi, lblPlanTongDot, lblPlanTongDichVu;
    private DefaultTableModel planToothModel, planServiceModel;
    private JTextField txtPlanName;
    private JComboBox<String> cboPlanStatus;
    private JLabel lblPlanBottomCost;
    
    private JPanel stepsContainer;
    private List<RouteStepData> routeStepsList = new ArrayList<>();
    private String currentSelectedMaLichHen = "";

    public ExaminationProcessPanel() {
        lichHenDAO = new LichHenDAO();
        setLayout(new BorderLayout());
        setBackground(BG);
        add(createMenu(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);
        
        cardPanel.add(createReceptionPage(), "RECEPTION");
        cardPanel.add(createExaminationPage(), "EXAMINATION");
        cardPanel.add(createPhieuKhamCardPage(), "PHIEU_KHAM_CARD");
        cardPanel.add(createTreatmentPlanPage(), "TREATMENT_PLAN");
        
        add(cardPanel, BorderLayout.CENTER);
        showReception();
        loadReceptionData();
    }
    
    private ImageIcon loadMenuIcon(String path) {
        java.net.URL url = getClass().getResource(path);

        if (url == null) {
            System.err.println("Không tìm thấy icon: " + path);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);

        Image img = icon.getImage().getScaledInstance(
            20, 20, Image.SCALE_SMOOTH
        );

        return new ImageIcon(img);
    }

    private JPanel createMenu() {

        // Vùng nền bên ngoài tạo khoảng cách với menu chính
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(14, 20, 12, 20));

        // Thanh tab bên trong
        JPanel menu = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 12, 10)
        );
        menu.setBackground(Color.WHITE);
        menu.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(3, 8, 3, 8)
            )
        );

        menu.setPreferredSize(new Dimension(0, 60));

      

     // ảnh icon smile

        btnReception = createButton("Danh sách tiếp nhận", PINK);
        btnReception.setIcon(loadMenuIcon("/img/smile.png"));

        btnExamination = createButton("Khám và chỉ định", BG);
        btnExamination.setForeground(NAVY);
        btnExamination.setIcon(loadMenuIcon("/img/smile.png"));

        btnPhieuKhamList = createButton("Danh sách phiếu khám", BG);
        btnPhieuKhamList.setForeground(NAVY);
        btnPhieuKhamList.setIcon(loadMenuIcon("/img/smile.png"));

        btnTreatmentPlan = createButton("Lập kế hoạch điều trị", BG);
        btnTreatmentPlan.setForeground(NAVY);
        btnTreatmentPlan.setIcon(loadMenuIcon("/img/smile.png"));

        // Căn icon bên trái chữ
        JButton[] buttons = {
            btnReception, btnExamination,
            btnPhieuKhamList, btnTreatmentPlan
        };

        for (JButton btn : buttons) {
            btn.setIconTextGap(8);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setVerticalAlignment(SwingConstants.CENTER);
        }
        
        
        btnTreatmentPlan.setForeground(NAVY);

        btnReception.addActionListener(e -> {
            if (isExaminingInProgress()) return;
            showReception();
        });
        btnExamination.addActionListener(e -> showExamination());
        btnPhieuKhamList.addActionListener(e -> {
            if (isExaminingInProgress()) return;
            showPhieuKhamList();
        });
        btnTreatmentPlan.addActionListener(e -> {
            if (isExaminingInProgress()) {
                String currentMaLH = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
                if (currentMaLH.equals("--") || currentMaLH.isEmpty()) {
                    return;
                }
            }
            showTreatmentPlan();
        });

        menu.add(btnReception);
        menu.add(btnExamination);
        menu.add(btnPhieuKhamList);
        menu.add(btnTreatmentPlan);
        return menu;
    }
    
    
    
    
    private boolean isExaminingInProgress() {
        String currentMaLH = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
        String currentName = lblPatientName != null ? lblPatientName.getText().trim() : "";
        
        if (!currentMaLH.equals("--") && !currentMaLH.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Bạn đang khám dở cho bệnh nhân: " + currentName + " (Mã lịch hẹn: " + currentMaLH + ").\nVui lòng hoàn tất hoặc lưu phiếu khám trước khi rời đi!",
                "Cảnh báo đang khám bệnh",
                JOptionPane.WARNING_MESSAGE
            );
            return true;
        }
        return false;
    }
    
    private void showTreatmentPlan() {
        cardLayout.show(cardPanel, "TREATMENT_PLAN");
        setActiveMenuButton(btnTreatmentPlan);
    }

    private void showTreatmentPlanForPatient(String maLichHen) {
        currentSelectedMaLichHen = maLichHen;
        cardLayout.show(cardPanel, "TREATMENT_PLAN");
        setActiveMenuButton(btnTreatmentPlan);
        if (maLichHen != null && !maLichHen.isEmpty()) {
            loadTreatmentPlanData(maLichHen);
        }
    }

    private void showReception() {
        cardLayout.show(cardPanel, "RECEPTION");
        setActiveMenuButton(btnReception);
        loadReceptionData();
    }

    private void showExamination() {
        cardLayout.show(cardPanel, "EXAMINATION");
        setActiveMenuButton(btnExamination);
    }

    private void showPhieuKhamList() {
        cardLayout.show(cardPanel, "PHIEU_KHAM_CARD");
        setActiveMenuButton(btnPhieuKhamList);
        loadPhieuKhamCards("");
    }

    private void setActiveMenuButton(JButton activeBtn) {
        btnReception.setBackground(BG);
        btnReception.setForeground(NAVY);
        btnExamination.setBackground(BG);
        btnExamination.setForeground(NAVY);
        btnPhieuKhamList.setBackground(BG);
        btnPhieuKhamList.setForeground(NAVY);
        btnTreatmentPlan.setBackground(BG);
        btnTreatmentPlan.setForeground(NAVY);

        activeBtn.setBackground(activeBtn == btnExamination ? BLUE : PINK);
        activeBtn.setForeground(Color.WHITE);
    }
    
   
    
    // TAG 1
    private JPanel createReceptionPage() {
        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(18, 20, 18, 20));



     // card ngang-- ds tiếp nhận 

     JPanel headerCard = new JPanel(new BorderLayout(20, 0)) {
         @Override
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);

             Graphics2D g2 = (Graphics2D) g.create();
             g2.setRenderingHint(
                 RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON
             );

             // Nền hồng pastel
             GradientPaint gradient = new GradientPaint(
                 0, 0, new Color(255, 248, 252),
                 getWidth(), getHeight(), new Color(255, 228, 241)
             );

             g2.setPaint(gradient);
             g2.fillRoundRect(
                 0, 0, getWidth(), getHeight(), 22, 22
             );

             // Viền hồng nhạt
             g2.setColor(new Color(247, 190, 218));
             g2.setStroke(new BasicStroke(1.2f));
             g2.drawRoundRect(
                 0, 0, getWidth() - 1, getHeight() - 1, 22, 22
             );

             g2.dispose();
         }
     };

     headerCard.setOpaque(false);
     headerCard.setBorder(new EmptyBorder(15, 18, 15, 18));
     headerCard.setPreferredSize(new Dimension(0, 125));

     // ảnh list

     JPanel leftPanel = new JPanel(new BorderLayout(15, 0));
     leftPanel.setOpaque(false);

     // Ảnh minh họa
     JLabel lblIllustration = new JLabel();
     lblIllustration.setHorizontalAlignment(SwingConstants.CENTER);

     java.net.URL imageURL =
         getClass().getResource("/img/list.png");

     if (imageURL != null) {
         ImageIcon original = new ImageIcon(imageURL);

         Image scaled = original.getImage().getScaledInstance(
             115, 95, Image.SCALE_SMOOTH
         );

         lblIllustration.setIcon(new ImageIcon(scaled));
     } else {
         System.out.println("Không tìm thấy ảnh /img/list.png");
     }

     lblIllustration.setPreferredSize(new Dimension(125, 95));

     // Đường phân cách
     JPanel separator = new JPanel();
     separator.setBackground(new Color(235, 135, 184));
     separator.setPreferredSize(new Dimension(2, 90));

     // Tiêu đề
     JPanel titlePanel = new JPanel();
     titlePanel.setOpaque(false);
     titlePanel.setLayout(
         new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
     );

     JLabel title = createTitle("Danh sách bệnh nhân chờ khám");
     title.setFont(new Font("Segoe UI", Font.BOLD, 27));
     title.setForeground(NAVY);

     JLabel subtitle = new JLabel(
         "Quản lý tiếp nhận bệnh nhân đã xác nhận lịch hẹn và chuyển sang khám bệnh"
     );
     subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
     subtitle.setForeground(new Color(74, 88, 105));

     titlePanel.add(Box.createVerticalGlue());
     titlePanel.add(title);
     titlePanel.add(Box.createVerticalStrut(8));
     titlePanel.add(subtitle);
     titlePanel.add(Box.createVerticalGlue());

     leftPanel.add(lblIllustration, BorderLayout.WEST);
     leftPanel.add(separator, BorderLayout.CENTER);
     leftPanel.add(titlePanel, BorderLayout.EAST);

     headerCard.add(leftPanel, BorderLayout.WEST);

     //bên phải 

     JPanel filter = new JPanel(
         new FlowLayout(FlowLayout.RIGHT, 10, 0)
     );
     filter.setOpaque(false);

     JLabel lblDate = new JLabel("Ngày khám:");
     JLabel lblKeyword = new JLabel("Từ khóa:");

     Font filterFont = new Font("Segoe UI", Font.BOLD, 16);

     lblDate.setFont(filterFont);
     lblKeyword.setFont(filterFont);
     lblDate.setForeground(NAVY);
     lblKeyword.setForeground(NAVY);

     JTextField txtDate = new JTextField(10);
     JTextField txtSearch = new JTextField(18);

     styleTextField(txtDate);
     styleTextField(txtSearch);

     txtDate.setPreferredSize(new Dimension(145, 42));
     txtSearch.setPreferredSize(new Dimension(230, 42));

     txtSearch.setToolTipText("Tìm mã BN, tên hoặc SĐT");

     // Nút tìm kiếm
     JButton btnSearch = createButton("Tìm kiếm", BLUE);
     btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
     btnSearch.setPreferredSize(new Dimension(110, 42));

     // Nút tải lại
     JButton btnReload = createButton("Tải lại", MUTED);
     btnReload.setFont(new Font("Segoe UI", Font.BOLD, 14));
     btnReload.setPreferredSize(new Dimension(95, 42));

     // Sự kiện tìm kiếm
     btnSearch.addActionListener(e -> {
         performReceptionSearch(
             txtSearch.getText().trim(),
             txtDate.getText().trim()
         );
     });

     // Sự kiện tải lại
     btnReload.addActionListener(e -> {
         txtSearch.setText("");
         txtDate.setText("");
         loadReceptionData();
     });

     filter.add(lblDate);
     filter.add(txtDate);
     filter.add(lblKeyword);
     filter.add(txtSearch);
     filter.add(btnSearch);
     filter.add(btnReload);

     headerCard.add(filter, BorderLayout.EAST);

     main.add(headerCard, BorderLayout.NORTH);

     
// bảng ds tiếp nhận của tag1 


  JPanel tableCard = createCard();
  tableCard.setLayout(new BorderLayout(0, 15));
  tableCard.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(new Color(230, 237, 247)),
      new EmptyBorder(18, 18, 15, 18)
  ));

  // header card

  JPanel cardHeader = new JPanel(new BorderLayout());
  cardHeader.setOpaque(false);

  // Tiêu đề bên trái
  JPanel headingPanel = new JPanel(
      new FlowLayout(FlowLayout.LEFT, 10, 0)
  );
  headingPanel.setOpaque(false);

  JLabel iconPatient = new JLabel("♧");
  iconPatient.setFont(new Font("Segoe UI", Font.BOLD, 25));
  iconPatient.setForeground(PINK);
  iconPatient.setPreferredSize(new Dimension(40, 38));
  iconPatient.setHorizontalAlignment(SwingConstants.CENTER);
  iconPatient.setOpaque(true);
  iconPatient.setBackground(new Color(255, 237, 245));

  JLabel lblHeading = new JLabel("Danh sách bệnh nhân");
  lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 21));
  lblHeading.setForeground(NAVY);

  headingPanel.add(iconPatient);
  headingPanel.add(lblHeading);

  // Bộ lọc trạng thái bên phải
  JPanel status = new JPanel(
      new FlowLayout(FlowLayout.RIGHT, 8, 0)
  );
  status.setOpaque(false);

  JButton btnChoKham = createButton("Chờ khám", PINK);
  JButton btnDangKham = createButton("Đang khám", BLUE);
  JButton btnDaKham = createButton("Đã khám", MUTED);

  btnChoKham.setFont(new Font("Segoe UI", Font.BOLD, 12));
  btnDangKham.setFont(new Font("Segoe UI", Font.BOLD, 12));
  btnDaKham.setFont(new Font("Segoe UI", Font.BOLD, 12));

  status.add(btnChoKham);
  status.add(btnDangKham);
  status.add(btnDaKham);

  btnChoKham.addActionListener(e ->
      loadReceptionDataByStatus("Đã xác nhận")
  );

  btnDangKham.addActionListener(e ->
      loadReceptionDataByStatus("Đang khám")
  );

  btnDaKham.addActionListener(e ->
      loadReceptionDataByStatus("Hoàn thành")
  );

  cardHeader.add(headingPanel, BorderLayout.WEST);
  cardHeader.add(status, BorderLayout.EAST);

  // Tổng số bệnh nhân
  JPanel headingContainer = new JPanel(new BorderLayout(10, 0));
  headingContainer.setOpaque(false);

  headingContainer.add(cardHeader, BorderLayout.CENTER);

  lblTotalPatients = new JLabel("Tổng: 0 bệnh nhân");
  lblTotalPatients.setFont(new Font("Segoe UI", Font.BOLD, 13));
  lblTotalPatients.setForeground(PINK);
  lblTotalPatients.setOpaque(true);
  lblTotalPatients.setBackground(new Color(255, 235, 244));
  lblTotalPatients.setBorder(new EmptyBorder(8, 14, 8, 14));

  JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
  totalPanel.setOpaque(false);
  totalPanel.add(lblTotalPatients);

  JPanel topPanel = new JPanel(new BorderLayout(0, 12));
  topPanel.setOpaque(false);
  topPanel.add(headingContainer, BorderLayout.NORTH);
  topPanel.add(totalPanel, BorderLayout.CENTER);

  tableCard.add(topPanel, BorderLayout.NORTH);

  // ================= CẤU TRÚC BẢNG =================

  String[] columns = {
      "STT", "Mã LH", "Họ và tên", "Tuổi", "Giới tính",
      "SĐT", "Ngày hẹn", "Giờ khám", "Dịch vụ dự kiến",
      "Bác sĩ phụ trách", "Trạng thái", "Thao tác"
  };

  receptionModel = new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
          return column == 11;
      }
  };

  tblReception = new JTable(receptionModel);

  styleTable(tblReception);

  tblReception.setFont(new Font("Segoe UI", Font.PLAIN, 13));
  tblReception.setForeground(NAVY);
  tblReception.setRowHeight(49);
  tblReception.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  tblReception.setSelectionBackground(new Color(255, 232, 243));
  tblReception.setSelectionForeground(NAVY);
  tblReception.setShowVerticalLines(false);
  tblReception.setShowHorizontalLines(true);
  tblReception.setGridColor(new Color(232, 237, 245));
  tblReception.setIntercellSpacing(new Dimension(0, 1));
  tblReception.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

  // ================= HEADER BẢNG =================

  JTableHeader tableHeader = tblReception.getTableHeader();

  tableHeader.setPreferredSize(new Dimension(0, 45));
  tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
  tableHeader.setBackground(new Color(35, 65, 115));
  tableHeader.setForeground(Color.WHITE);
  tableHeader.setReorderingAllowed(false);
  tableHeader.setBorder(
      BorderFactory.createMatteBorder(
          0, 0, 1, 0, new Color(220, 230, 245)
      )
  );

  // Renderer header
  DefaultTableCellRenderer headerRenderer =
      new DefaultTableCellRenderer() {

      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
      ) {
          JLabel label = (JLabel) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column
          );

          label.setBackground(new Color(35, 65, 115));
          label.setForeground(Color.WHITE);
          label.setFont(new Font("Segoe UI", Font.BOLD, 13));
          label.setHorizontalAlignment(SwingConstants.LEFT);
          label.setBorder(new EmptyBorder(0, 8, 0, 5));

          return label;
      }
  };

  for (int i = 0; i < tblReception.getColumnCount(); i++) {
      tblReception.getColumnModel()
          .getColumn(i).setHeaderRenderer(headerRenderer);
  }

  // ================= ĐỘ RỘNG CỘT =================

  int[] widths = {
      55, 90, 190, 65, 90, 120,
      120, 95, 180, 180, 135, 145
  };

  for (int i = 0; i < widths.length; i++) {
      tblReception.getColumnModel()
          .getColumn(i).setPreferredWidth(widths[i]);
  }

  // ================= MÀU NỀN XEN KẼ =================

  DefaultTableCellRenderer rowRenderer =
      new DefaultTableCellRenderer() {

      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
      ) {
          JLabel label = (JLabel) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column
          );

          label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
          label.setForeground(NAVY);
          label.setBorder(new EmptyBorder(0, 8, 0, 5));

          if (!isSelected) {
              label.setBackground(
                  row % 2 == 0
                      ? Color.WHITE
                      : new Color(242, 247, 255)
              );
          }

          return label;
      }
  };

  for (int i = 0; i < tblReception.getColumnCount(); i++) {
      if (i != 10 && i != 11) {
          tblReception.getColumnModel()
              .getColumn(i).setCellRenderer(rowRenderer);
      }
  }

  // ================= BADGE GIỚI TÍNH =================

  DefaultTableCellRenderer genderRenderer =
      new DefaultTableCellRenderer() {

      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
      ) {
          JLabel label = (JLabel) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column
          );

          label.setHorizontalAlignment(SwingConstants.CENTER);
          label.setFont(new Font("Segoe UI", Font.BOLD, 12));
          label.setOpaque(true);

          String gender = value == null ? "" : value.toString();

          if (!isSelected) {
              if (gender.equalsIgnoreCase("Nữ")) {
                  label.setBackground(new Color(255, 232, 243));
                  label.setForeground(new Color(190, 40, 105));
              } else {
                  label.setBackground(new Color(229, 241, 255));
                  label.setForeground(new Color(25, 105, 200));
              }
          }

          label.setBorder(new EmptyBorder(5, 5, 5, 5));
          return label;
      }
  };

  tblReception.getColumnModel()
      .getColumn(4).setCellRenderer(genderRenderer);

  // ================= BADGE TRẠNG THÁI =================

  DefaultTableCellRenderer statusRenderer =
      new DefaultTableCellRenderer() {

      @Override
      public Component getTableCellRendererComponent(
          JTable table, Object value,
          boolean isSelected, boolean hasFocus,
          int row, int column
      ) {
          JLabel label = (JLabel) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column
          );

          label.setHorizontalAlignment(SwingConstants.CENTER);
          label.setFont(new Font("Segoe UI", Font.BOLD, 12));
          label.setOpaque(true);

          String state = value == null ? "" : value.toString();

          if (!isSelected) {
              if (state.equalsIgnoreCase("Đã xác nhận")) {
                  label.setBackground(new Color(224, 248, 232));
                  label.setForeground(new Color(25, 145, 75));
              } else if (state.equalsIgnoreCase("Đang khám")) {
                  label.setBackground(new Color(225, 240, 255));
                  label.setForeground(new Color(25, 105, 200));
              } else if (state.equalsIgnoreCase("Hoàn thành")) {
                  label.setBackground(new Color(235, 245, 230));
                  label.setForeground(new Color(75, 135, 55));
              } else {
                  label.setBackground(new Color(245, 245, 245));
                  label.setForeground(MUTED);
              }
          }

          label.setBorder(new EmptyBorder(5, 4, 5, 4));
          return label;
      }
  };

  tblReception.getColumnModel()
      .getColumn(10).setCellRenderer(statusRenderer);

  // ================= NÚT KHÁM BỆNH =================

  tblReception.getColumnModel()
      .getColumn(11).setCellRenderer(
          new ActionRenderer("Khám bệnh")
      );

  tblReception.getColumnModel()
      .getColumn(11).setCellEditor(
          new ActionEditor(tblReception, row -> {
              processTransferToExamination(row);
          }, "Khám bệnh")
      );

  // ================= SCROLL BẢNG =================

  JScrollPane scroll = new JScrollPane(tblReception);
  scroll.setBorder(BorderFactory.createLineBorder(
      new Color(226, 234, 245)
  ));
  scroll.getViewport().setBackground(Color.WHITE);
  scroll.getVerticalScrollBar().setUnitIncrement(16);

  tableCard.add(scroll, BorderLayout.CENTER);

  // ================= FOOTER =================

  JPanel footer = new JPanel(new BorderLayout());
  footer.setOpaque(false);
  footer.setBorder(new EmptyBorder(5, 0, 0, 0));

  // Tổng số bệnh nhân
  lblTotalPatients.setText("Tổng: 0 bệnh nhân");

  JButton btnStart = createButton(
      "Chuyển sang Khám bệnh  →", PINK
  );

  btnStart.setFont(new Font("Segoe UI", Font.BOLD, 14));
  btnStart.setPreferredSize(new Dimension(235, 45));

  btnStart.addActionListener(e -> {
      int row = tblReception.getSelectedRow();

      if (row < 0) {
          JOptionPane.showMessageDialog(
              this,
              "Vui lòng chọn bệnh nhân trong danh sách!",
              "Thông báo",
              JOptionPane.WARNING_MESSAGE
          );
          return;
      }

      int modelRow = tblReception.convertRowIndexToModel(row);
      processTransferToExamination(modelRow);
  });

  footer.add(lblTotalPatients, BorderLayout.WEST);
  footer.add(btnStart, BorderLayout.EAST);

  tableCard.add(footer, BorderLayout.SOUTH);

  main.add(tableCard, BorderLayout.CENTER);


        return main;
    }

    
    
    
    
    
    
    
    
    // TAG 3

private JPanel createPhieuKhamCardPage() {

    JPanel main = new JPanel(new BorderLayout(10, 12));
    main.setBackground(BG);
    main.setBorder(new EmptyBorder(18, 20, 18, 20));

    // ================= HEADER BANNER =================
    JPanel header = new JPanel(new BorderLayout(20, 0));
    header.setBackground(new Color(255, 250, 225));
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(255, 210, 65), 2),
        new EmptyBorder(15, 18, 15, 18)
    ));
    header.setPreferredSize(new Dimension(0, 112));

    // ===== ICON BÊN TRÁI =====
    JLabel lblIcon = new JLabel();
    lblIcon.setPreferredSize(new Dimension(105, 75));
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

    try {
        java.net.URL iconURL = getClass().getResource("/img/list1.png");
        if (iconURL != null) {
            ImageIcon icon = new ImageIcon(iconURL);
            Image scaled = icon.getImage().getScaledInstance(
                82, 82, Image.SCALE_SMOOTH
            );
            lblIcon.setIcon(new ImageIcon(scaled));
        } else {
            lblIcon.setText("🦷");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }
    } catch (Exception ex) {
        lblIcon.setText("🦷");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
    }

    JPanel iconPanel = new JPanel(new BorderLayout());
    iconPanel.setOpaque(false);
    iconPanel.add(lblIcon, BorderLayout.CENTER);
    iconPanel.setBorder(new MatteBorder(
        0, 0, 0, 3, new Color(255, 210, 65)
    ));

    header.add(iconPanel, BorderLayout.WEST);

    // ===== TIÊU ĐỀ =====
    JPanel titlePanel = new JPanel();
    titlePanel.setOpaque(false);
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

    JLabel title = new JLabel("Quản lý danh sách phiếu khám");
    title.setFont(new Font("Segoe UI", Font.BOLD, 25));
    title.setForeground(new Color(15, 39, 85));
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel subtitle = new JLabel(
        "Hiển thị trực quan thông tin phiếu khám, răng miệng và dịch vụ chỉ định"
    );
    subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    subtitle.setForeground(new Color(65, 78, 100));
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

    titlePanel.add(title);
    titlePanel.add(Box.createVerticalStrut(9));
    titlePanel.add(subtitle);

    header.add(titlePanel, BorderLayout.CENTER);

    // ===== KHU VỰC TÌM KIẾM =====
    JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    filter.setOpaque(false);

    JLabel lblKeyword = new JLabel("Từ khóa:");
    lblKeyword.setFont(new Font("Segoe UI", Font.BOLD, 14));
    lblKeyword.setForeground(new Color(15, 39, 85));

    txtSearchPhieuKham = new JTextField(17);
    styleTextField(txtSearchPhieuKham);
    txtSearchPhieuKham.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    txtSearchPhieuKham.setPreferredSize(new Dimension(220, 38));
    txtSearchPhieuKham.setToolTipText(
        "Tìm kiếm theo mã lịch hẹn, tên hoặc SĐT"
    );

    JButton btnSearch = createButton("⌕  Tìm kiếm", BLUE);
    btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnSearch.setPreferredSize(new Dimension(115, 38));

    JButton btnReload = createButton("⟳  Tải lại", MUTED);
    btnReload.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btnReload.setPreferredSize(new Dimension(100, 38));

    btnReload.addActionListener(e -> {
        txtSearchPhieuKham.setText("");
        loadPhieuKhamCards("");
    });

    btnSearch.addActionListener(e -> {
        loadPhieuKhamCards(txtSearchPhieuKham.getText().trim());
    });

    txtSearchPhieuKham.addActionListener(e -> {
        loadPhieuKhamCards(txtSearchPhieuKham.getText().trim());
    });

    filter.add(lblKeyword);
    filter.add(txtSearchPhieuKham);
    filter.add(btnSearch);
    filter.add(btnReload);

    header.add(filter, BorderLayout.EAST);

    main.add(header, BorderLayout.NORTH);

    // ================= CARD CONTAINER =================
    cardContainer = new JPanel();
    cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
    cardContainer.setBackground(BG);

    JScrollPane scrollPane = new JScrollPane(cardContainer);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setBackground(BG);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    main.add(scrollPane, BorderLayout.CENTER);

    // ================= FOOTER =================
    JPanel footer = new JPanel(new BorderLayout());
    footer.setOpaque(false);
    footer.setBorder(new EmptyBorder(5, 0, 0, 0));

    lblTotalPhieuKhamCards = new JLabel(
        "Tổng số phiếu khám hiển thị: 0"
    );
    lblTotalPhieuKhamCards.setFont(
        new Font("Segoe UI", Font.BOLD, 13)
    );
    lblTotalPhieuKhamCards.setForeground(MUTED);

    footer.add(lblTotalPhieuKhamCards, BorderLayout.WEST);
    main.add(footer, BorderLayout.SOUTH);

    return main;
}

    public void loadPhieuKhamCards(String keyword) {
        if (cardContainer == null) return;
        cardContainer.removeAll();
        
        String sql = 
            "SELECT lh.maLichHen, kh.tenKH, kh.sdt, lh.ngayHen, lh.gioKham, " +
            "       ba.maBenhAn, r.maRang, r.tinhTrang, r.mucDo, r.trieuChung, r.chanDoan, r.ghiChu AS ghiChuRang, " +
            "       dv.tenDichVu, ctd.soLuong, ctd.donVi, ctd.ghiChu AS ghiChuDV, ba.trangThai AS trangThaiBA " +
            "FROM LichHen lh " +
            "JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang " +
            "JOIN HoSoBenhAn ba ON ba.maKhachHang = kh.maKhachHang " +
            "JOIN ChiTietKhamRang r ON ba.maBenhAn = r.maBenhAn " + 
            "LEFT JOIN ChiTietChiDinh ctd ON ba.maBenhAn = ctd.maBenhAn " +
            "LEFT JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu ";

        if (keyword != null && !keyword.isEmpty()) {
            sql += " WHERE lh.maLichHen LIKE ? OR kh.tenKH LIKE ? OR kh.sdt LIKE ?";
        }
        sql += " ORDER BY lh.ngayHen DESC";

        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
                ps.setString(2, "%" + keyword + "%");
                ps.setString(3, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    count++;
                    String maLichHen = rs.getString("maLichHen");
                    String hoTen = rs.getString("tenKH"); 
                    String sdt = rs.getString("sdt"); 
                    String ngayKhám = rs.getString("ngayHen");
                    String hKham = rs.getString("gioKham");
                    
                    String maRang = rs.getString("maRang");
                    String tinhTrang = rs.getString("tinhTrang");
                    String mucDo = rs.getString("mucDo");
                    String trieuChung = rs.getString("trieuChung");
                    String chuanDoan = rs.getString("chanDoan");
                    String ghiChuRang = rs.getString("ghiChuRang");
                    
                    String tenDichVu = rs.getString("tenDichVu");
                    String soLuong = rs.getString("soLuong");
                    String donVi = rs.getString("donVi");
                    String ghiChuDV = rs.getString("ghiChuDV");
                    String trangThaiBA = rs.getString("trangThaiBA");

                    JPanel card = createPatientCard(
                        maLichHen, hoTen, sdt, ngayKhám, hKham, 
                        maRang, tinhTrang, mucDo, trieuChung, chuanDoan, ghiChuRang,
                        tenDichVu, soLuong, donVi, ghiChuDV, trangThaiBA
                    );
                    
                    cardContainer.add(card);
                    cardContainer.add(Box.createVerticalStrut(12));
                }
            }
            if (lblTotalPhieuKhamCards != null) {
                lblTotalPhieuKhamCards.setText("Tổng số phiếu khám hiển thị: " + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cardContainer.revalidate();
        cardContainer.repaint();
    }
    
    

    private JPanel createPatientCard(
            String maLichHen, String hoTen, String sdt, String ngayKhám, String hKham,
            String maRang, String tinhTrang, String mucDo, String trieuChung, String chuanDoan, String ghiChuRang,
            String tenDichVu, String soLuong, String donVi, String ghiChuDV, String trangThaiBA) {
        
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        
        boolean isPlanned = trangThaiBA != null && (trangThaiBA.contains("Đã lập kế hoạch") || trangThaiBA.contains("Đã xác nhận kế hoạch"));
        String statusText = isPlanned ? "  [Đã lập kế hoạch]" : "";

        JLabel lblNameCode = new JLabel("<html><b>[" + (maLichHen != null ? maLichHen : "--") + "]</b> " + (hoTen != null ? hoTen : "Chưa cập nhật") + " (SĐT: " + (sdt != null ? sdt : "--") + ") <font color='" + (isPlanned ? "green" : "gray") + "'><b>" + statusText + "</b></font></html>");
        lblNameCode.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNameCode.setForeground(NAVY);
        
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeaderPanel.setOpaque(false);

        JLabel lblTimeInfo = new JLabel("📅 " + (ngayKhám != null ? ngayKhám : "--") + "   ⏰ " + (hKham != null ? hKham : "--"));
        lblTimeInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTimeInfo.setForeground(MUTED);

        if (isPlanned) {
            JButton btnViewPlan = createButton("👁 Xem kế hoạch", new Color(0, 120, 215));
            btnViewPlan.setPreferredSize(new Dimension(130, 28));
            btnViewPlan.addActionListener(e -> showTreatmentPlanForPatient(maLichHen));
            rightHeaderPanel.add(btnViewPlan);
        } else {
            JButton btnPlanFromCard = createButton("▣ Lập kế hoạch", BLUE);
            btnPlanFromCard.setPreferredSize(new Dimension(125, 28));
            btnPlanFromCard.addActionListener(e -> {
                if (maLichHen != null && !maLichHen.isEmpty()) {
                    showTreatmentPlanForPatient(maLichHen);
                }
            });
            rightHeaderPanel.add(btnPlanFromCard);
        }

        rightHeaderPanel.add(lblTimeInfo);

        cardHeader.add(lblNameCode, BorderLayout.WEST);
        cardHeader.add(rightHeaderPanel, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel cardBody = new JPanel(new GridLayout(1, 2, 15, 0));
        cardBody.setOpaque(false);

        JPanel toothInfoPanel = new JPanel();
        toothInfoPanel.setLayout(new BoxLayout(toothInfoPanel, BoxLayout.Y_AXIS));
        toothInfoPanel.setOpaque(false);
        toothInfoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 225, 245)), "🦷 Khám Răng Miệng", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), BLUE
        ));

        toothInfoPanel.add(new JLabel("• Mã răng: " + (maRang != null ? maRang : "Chưa chọn")));
        toothInfoPanel.add(new JLabel("• Tình trạng: " + (tinhTrang != null ? tinhTrang : "--") + " (" + (mucDo != null ? mucDo : "--") + ")"));
        toothInfoPanel.add(new JLabel("• Triệu chứng: " + (trieuChung != null && !trieuChung.isEmpty() ? trieuChung : "Không có")));
        toothInfoPanel.add(new JLabel("• Chẩn đoán: " + (chuanDoan != null && !chuanDoan.isEmpty() ? chuanDoan : "--")));

        JPanel serviceInfoPanel = new JPanel();
        serviceInfoPanel.setLayout(new BoxLayout(serviceInfoPanel, BoxLayout.Y_AXIS));
        serviceInfoPanel.setOpaque(false);
        serviceInfoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(245, 210, 225)), "💊 Chỉ Định Điều Trị", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), PINK
        ));

        serviceInfoPanel.add(new JLabel("• Dịch vụ: " + (tenDichVu != null ? tenDichVu : "Chưa có chỉ định")));
        serviceInfoPanel.add(new JLabel("• Số lượng: " + (soLuong != null ? soLuong : "0") + " " + (donVi != null ? donVi : "")));
        serviceInfoPanel.add(new JLabel("• Ghi chú: " + (ghiChuRang != null ? ghiChuRang : "") + " | " + (ghiChuDV != null ? ghiChuDV : "")));

        cardBody.add(toothInfoPanel);
        cardBody.add(serviceInfoPanel);

        card.add(cardBody, BorderLayout.CENTER);

        return card;
    }

    private void processTransferToExamination(int modelRow) {
        if (receptionModel == null || modelRow < 0 || modelRow >= receptionModel.getRowCount()) return;
        
        String currentMaLH = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
        String currentName = lblPatientName != null ? lblPatientName.getText().trim() : "";
        if (!currentMaLH.equals("--") && !currentMaLH.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Bạn đang khám dở cho bệnh nhân: " + currentName + " (Mã lịch hẹn: " + currentMaLH + ").\nVui lòng hoàn tất hoặc lưu phiếu khám hiện tại trước khi chuyển sang bệnh nhân mới!",
                "Đang có bệnh nhân khám dở",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Object val = receptionModel.getValueAt(modelRow, 1);
        if (val == null) return;

        String maLichHen = String.valueOf(val);
        boolean updated = lichHenDAO.updateStatus(maLichHen, "Đang khám");
        if (updated) {
            loadPatient(modelRow);
            showExamination();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái lịch hẹn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadReceptionData() {
        loadReceptionDataByStatus("Đã xác nhận");
    }

    private void loadReceptionDataByStatus(String statusFilter) {
        if (receptionModel == null || lichHenDAO == null) return;
        receptionModel.setRowCount(0);
        try {
            List<Object[]> allAppointments = lichHenDAO.getAllAppointmentsWithPatientInfo();
            if (allAppointments == null) return;
            
            int stt = 1;
            for (Object[] row : allAppointments) {
                if (row == null || row.length <= 9) continue;
                String status = String.valueOf(row[9]);
                if (status.equalsIgnoreCase(statusFilter)) {
                    String maLichHen = String.valueOf(row[0]);
                    String tenKH     = String.valueOf(row[1]);
                    String sdt       = String.valueOf(row[2]);
                    String tuoi      = String.valueOf(row[3]);
                    String gioiTinh  = String.valueOf(row[4]);
                    String dichVu    = String.valueOf(row[5]);
                    String bacSi     = String.valueOf(row[6]);
                    String ngayHen   = String.valueOf(row[7]);
                    String gioKham   = String.valueOf(row[8]);

                    receptionModel.addRow(new Object[]{
                        stt++, maLichHen, tenKH, tuoi, gioiTinh, sdt, ngayHen, gioKham, dichVu,
                        bacSi == null || bacSi.isEmpty() ? "Chưa phân công" : bacSi, status, "Khám bệnh"
                    });
                }
            }
            updateReceptionTotalCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performReceptionSearch(String keyword, String dateFilter) {
        if (receptionModel == null || lichHenDAO == null) return;
        receptionModel.setRowCount(0);
        try {
            List<Object[]> allAppointments = lichHenDAO.getAllAppointmentsWithPatientInfo();
            if (allAppointments == null) return;

            int stt = 1;
            String query = keyword != null ? keyword.toLowerCase() : "";
            String dateQ = dateFilter != null ? dateFilter.trim() : "";

            for (Object[] row : allAppointments) {
                if (row == null || row.length <= 9) continue;
                String maLichHen = String.valueOf(row[0]);
                String tenKH = String.valueOf(row[1]);
                String sdt = String.valueOf(row[2]);
                String ngayHen = String.valueOf(row[7]);
                String status = String.valueOf(row[9]);

                boolean matchKeyword = query.isEmpty() || 
                        maLichHen.toLowerCase().contains(query) || 
                        tenKH.toLowerCase().contains(query) || 
                        sdt.contains(query);
                
                boolean matchDate = dateQ.isEmpty() || ngayHen.contains(dateQ);
                if (matchKeyword && matchDate && (status.equalsIgnoreCase("Đã xác nhận") || status.equalsIgnoreCase("Đang khám"))) {
                    receptionModel.addRow(new Object[]{
                        stt++, maLichHen, tenKH, String.valueOf(row[3]), String.valueOf(row[4]), sdt,
                        ngayHen, String.valueOf(row[8]), String.valueOf(row[5]), String.valueOf(row[6]), status, "Khám bệnh"
                    });
                }
            }
            updateReceptionTotalCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateReceptionTotalCount() {
        if (lblTotalPatients != null && receptionModel != null) {
            lblTotalPatients.setText("Tổng số bệnh nhân: " + receptionModel.getRowCount());
        }
    }
    
    
    
    
    // TAG2 
    
    //SƠ ĐỒ RĂNG
    private JPanel createExaminationPage() {
        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(12, 15, 15, 15));

        main.add(createPatientHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 8);

        g.gridx = 0;
        g.weightx = 0.30;
        toothSource = new StandardToothChartPanel();
        content.add(toothSource.createClinicalChartCard(this::onToothSelected), g);

        g.gridx = 1;
        g.weightx = 0.30;
        content.add(createToothForm(), g);

        g.gridx = 2;
        g.weightx = 0.40;
        g.insets = new Insets(0, 0, 0, 0);
        content.add(createRightPanel(), g);

        main.add(content, BorderLayout.CENTER);
        return main;
    }

   
    

private JPanel createPatientHeader() {

    // ================= CARD THÔNG TIN BỆNH NHÂN =================

    JPanel panel = new JPanel(new BorderLayout(20, 0)) {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Nền xanh dương pastel trơn
            g2.setColor(new Color(232, 243, 255));
            g2.fillRoundRect(
                0, 0, getWidth(), getHeight(), 22, 22
            );

            // Viền xanh dương
            g2.setColor(new Color(125, 190, 255));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(
                1, 1, getWidth() - 3, getHeight() - 3, 22, 22
            );

            g2.dispose();
        }
    };

    panel.setOpaque(false);
    panel.setBorder(new EmptyBorder(8, 20, 8, 20));
    panel.setPreferredSize(new Dimension(0, 110));
    panel.setMinimumSize(new Dimension(500, 100));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

    // ================= KHỞI TẠO DỮ LIỆU =================

    lblPatientCode = createValue("--");
    lblPatientName = createValue("--");
    lblAge = createValue("--");
    lblGender = createValue("--");
    lblPhone = createValue("--");
    lblDate = createValue("--");
    lblTime = createValue("--");

 // ================= ICON BÊN TRÁI =================

    JPanel iconPanel = new JPanel(new GridBagLayout());
    iconPanel.setOpaque(false);
    iconPanel.setPreferredSize(new Dimension(130, 85));
    

    try {
        java.net.URL imageURL = getClass().getResource("/img/kham.png");

        if (imageURL != null) {

            ImageIcon originalIcon = new ImageIcon(imageURL);

            Image scaledImage = originalIcon.getImage().getScaledInstance(
                85,
                85,
                Image.SCALE_SMOOTH
            );

            JLabel lblIcon = new JLabel(new ImageIcon(scaledImage));

            iconPanel.add(lblIcon);

        } else {
            System.out.println(
                "Không tìm thấy ảnh: /img/kham.png"
            );
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    // Đường phân cách icon
    JPanel separator = new JPanel();
    separator.setBackground(new Color(115, 185, 245));
    separator.setPreferredSize(new Dimension(2, 85));

    JPanel leftPanel = new JPanel(new BorderLayout(18, 0));
    leftPanel.setOpaque(false);
    leftPanel.add(iconPanel, BorderLayout.CENTER);
    leftPanel.add(separator, BorderLayout.EAST);
    leftPanel.setPreferredSize(new Dimension(150, 85));

    panel.add(leftPanel, BorderLayout.WEST);

    // ================= 7 THÔNG TIN NẰM NGANG =================

    JPanel infoPanel = new JPanel(
        new GridLayout(1, 7, 22, 0)
    );
    infoPanel.setOpaque(false);

    infoPanel.add(infoItem("Mã lịch hẹn", lblPatientCode));
    infoPanel.add(infoItem("Họ tên", lblPatientName));
    infoPanel.add(infoItem("Tuổi", lblAge));
    infoPanel.add(infoItem("Giới tính", lblGender));
    infoPanel.add(infoItem("Số điện thoại", lblPhone));
    infoPanel.add(infoItem("Ngày khám", lblDate));
    infoPanel.add(infoItem("Giờ khám", lblTime));

    panel.add(infoPanel, BorderLayout.CENTER);

    // ================= NÚT TẢI LẠI BÊN PHẢI =================

    JPanel rightPanel = new JPanel(new GridBagLayout());
    rightPanel.setOpaque(false);
    rightPanel.setPreferredSize(new Dimension(130, 85));

    JButton btnReloadExam = new JButton("Tải lại");

    btnReloadExam.setFont(
        new Font("Segoe UI", Font.BOLD, 17)
    );

    btnReloadExam.setForeground(Color.WHITE);
    btnReloadExam.setBackground(new Color(75, 98, 125));
    btnReloadExam.setPreferredSize(new Dimension(140, 40));
    btnReloadExam.setFocusPainted(false);
    btnReloadExam.setBorderPainted(false);
    btnReloadExam.setOpaque(true);
    btnReloadExam.setCursor(
        new Cursor(Cursor.HAND_CURSOR)
    );

    // Hiệu ứng hover, tự trở về màu ban đầu
    btnReloadExam.addMouseListener(
        new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnReloadExam.setBackground(
                    new Color(45, 65, 90)
                );
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnReloadExam.setBackground(
                    new Color(75, 98, 125)
                );
            }
        }
    );

    btnReloadExam.addActionListener(e -> {

        String maLichHen = lblPatientCode.getText().trim();

        if (!maLichHen.equals("--") && !maLichHen.isEmpty()) {

            loadExistingExaminationData(maLichHen);

            JOptionPane.showMessageDialog(
                this,
                "Đã làm mới dữ liệu khám của bệnh nhân!"
            );

        } else {

            JOptionPane.showMessageDialog(
                this,
                "Chưa chọn bệnh nhân nào để tải lại!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );
        }
    });

    rightPanel.add(btnReloadExam);
    panel.add(rightPanel, BorderLayout.EAST);

    return panel;
}



    private JPanel infoItem(String title, JLabel value) {
        JPanel item = new JPanel(new GridLayout(2, 1, 0, 5));
        item.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitle.setForeground(new Color(15, 29, 69));

        value.setFont(new Font("Arial", Font.PLAIN, 14));
        value.setForeground(new Color(15, 29, 69));

        item.add(lblTitle);
        item.add(value);
        return item;
    }

    private JLabel createValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(NAVY);
        return label;
    }

    
    //TAG 2
    private JPanel createToothForm() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(8, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = createTitle("Thông tin răng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        panel.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel topInfoPanel = new JPanel(new BorderLayout(12, 0));
        topInfoPanel.setOpaque(false);
        topInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topInfoPanel.setPreferredSize(new Dimension(400, 85));
        topInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JLabel lblToothImage;
        java.net.URL toothUrl = getClass().getResource("/img/iconrang.png");

        if (toothUrl != null) {
            ImageIcon icon = new ImageIcon(toothUrl);
            Image scaled = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblToothImage = new JLabel(new ImageIcon(scaled));
        } else {
            lblToothImage = new JLabel("🦷");
            lblToothImage.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }

        lblToothImage.setOpaque(true);
        lblToothImage.setBackground(new Color(232, 242, 255));
        lblToothImage.setPreferredSize(new Dimension(80, 80));
        lblToothImage.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel namePanel = new JPanel();
        namePanel.setOpaque(false);
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        codeRow.setOpaque(false);
        codeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCodeTitle = new JLabel("Mã răng:");
        lblCodeTitle.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        lblCodeTitle.setForeground(MUTED);

        lblToothHeaderCode = new JLabel("--");
        lblToothHeaderCode.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblToothHeaderCode.setForeground(BLUE);

        codeRow.add(lblCodeTitle);
        codeRow.add(lblToothHeaderCode);

        lblToothHeaderName = new JLabel("Chưa chọn răng");
        lblToothHeaderName.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblToothHeaderName.setForeground(NAVY);
        lblToothHeaderName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgePanel.setBackground(new Color(228, 241, 255));
        badgePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        badgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblToothHeaderType = new JLabel("--");
        lblToothHeaderType.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblToothHeaderType.setForeground(BLUE);
        badgePanel.add(lblToothHeaderType);

        namePanel.add(codeRow);
        namePanel.add(Box.createVerticalStrut(5));
        namePanel.add(lblToothHeaderName);
        namePanel.add(Box.createVerticalStrut(6));
        namePanel.add(badgePanel);

        topInfoPanel.add(lblToothImage, BorderLayout.WEST);
        topInfoPanel.add(namePanel, BorderLayout.CENTER);

        body.add(topInfoPanel);
        body.add(Box.createVerticalStrut(12));

        JPanel attrGrid = new JPanel(new GridLayout(5, 2, 0, 0));
        attrGrid.setBackground(Color.WHITE);
        attrGrid.setBorder(BorderFactory.createLineBorder(BORDER));
        attrGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblToothCode = createValue("--");
        lblToothName = createValue("--");
        lblJaw = createValue("--");
        lblPosition = createValue("--");
        lblType = createValue("--");

        String[] attrNames = {"Mã răng", "Tên răng", "Hàm", "Vị trí", "Phân loại"};
        JLabel[] attrValues = {lblToothCode, lblToothName, lblJaw, lblPosition, lblType};

        for (int i = 0; i < attrNames.length; i++) {
            JPanel leftCell = new JPanel(new BorderLayout());
            leftCell.setBackground(Color.WHITE);
            leftCell.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 10, 7, 10)));
            JLabel lblName = new JLabel(attrNames[i]);
            lblName.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            lblName.setForeground(MUTED);
            leftCell.add(lblName, BorderLayout.CENTER);

            JPanel rightCell = new JPanel(new BorderLayout());
            rightCell.setBackground(Color.WHITE);
            rightCell.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 10, 7, 10)));
            attrValues[i].setFont(new Font("Segoe UI", Font.BOLD, 16));
            attrValues[i].setForeground(NAVY);
            rightCell.add(attrValues[i], BorderLayout.CENTER);

            attrGrid.add(leftCell);
            attrGrid.add(rightCell);
        }

        attrGrid.setPreferredSize(new Dimension(450, 200));
        attrGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        body.add(attrGrid);
        body.add(Box.createVerticalStrut(12));

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER);
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(separator);
        body.add(Box.createVerticalStrut(15));

       
        body.add(createFieldLabel("Tình trạng răng"));
        body.add(Box.createVerticalStrut(6));

        cboCondition = new JComboBox<>(new String[]{
            "Bình thường",
            "Sâu răng",
            "Viêm nướu",
            "Mẻ răng",
            "Mất răng",
            "Đã điều trị",
            "Viêm tủy",
            "Mòn cổ răng",
            "Răng khôn mọc lệch"
        });

        // Cho phép vừa chọn vừa nhập nội dung
        cboCondition.setEditable(true);

        styleCombo(cboCondition);
        cboCondition.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboCondition.setPreferredSize(new Dimension(300, 34));
        cboCondition.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cboCondition.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel conditionBox = new JPanel(new BorderLayout());
        conditionBox.setOpaque(false);
        conditionBox.setBorder(new EmptyBorder(0, 12, 0, 0));

        conditionBox.add(cboCondition, BorderLayout.CENTER);
        conditionBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        conditionBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(conditionBox);
        body.add(Box.createVerticalStrut(12));

        body.add(createFieldLabel("Mức độ"));
        body.add(Box.createVerticalStrut(6));

        cboSeverity = new JComboBox<>(new String[]{"Nhẹ", "Trung bình", "Nặng"});
        styleCombo(cboSeverity);
        cboSeverity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboSeverity.setPreferredSize(new Dimension(300, 34));
        cboSeverity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JPanel severityBox = new JPanel(new BorderLayout());
        severityBox.setOpaque(false);
        severityBox.setBorder(new EmptyBorder(0, 12, 0, 0));
        severityBox.add(cboSeverity, BorderLayout.CENTER);
        severityBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        severityBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(severityBox);
        body.add(Box.createVerticalStrut(12));

        body.add(createFieldLabel("Triệu chứng"));
        body.add(Box.createVerticalStrut(6));

        txtSymptom = createTextArea(3);
        txtSymptom.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSymptom.setMargin(new Insets(7, 9, 7, 9));

        JScrollPane symptomScroll = createScroll(txtSymptom);
        symptomScroll.setPreferredSize(new Dimension(300, 65));
        symptomScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel symptomBox = new JPanel(new BorderLayout());
        symptomBox.setOpaque(false);
        symptomBox.setBorder(new EmptyBorder(0, 12, 0, 0));
        symptomBox.add(symptomScroll, BorderLayout.CENTER);
        symptomBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        symptomBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(symptomBox);
        body.add(Box.createVerticalStrut(12));

        body.add(createFieldLabel("Chẩn đoán"));
        body.add(Box.createVerticalStrut(6));

        txtDiagnosis = createTextArea(3);
        txtDiagnosis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDiagnosis.setMargin(new Insets(7, 9, 7, 9));

        JScrollPane diagnosisScroll = createScroll(txtDiagnosis);
        diagnosisScroll.setPreferredSize(new Dimension(300, 65));
        diagnosisScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel diagnosisBox = new JPanel(new BorderLayout());
        diagnosisBox.setOpaque(false);
        diagnosisBox.setBorder(new EmptyBorder(0, 12, 0, 0));
        diagnosisBox.add(diagnosisScroll, BorderLayout.CENTER);
        diagnosisBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        diagnosisBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(diagnosisBox);
        body.add(Box.createVerticalStrut(12));

        body.add(createFieldLabel("Ghi chú"));
        body.add(Box.createVerticalStrut(6));

        txtNote = createTextArea(3);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setMargin(new Insets(7, 9, 7, 9));

        JScrollPane noteScroll = createScroll(txtNote);
        noteScroll.setPreferredSize(new Dimension(300, 65));
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel noteBox = new JPanel(new BorderLayout());
        noteBox.setOpaque(false);
        noteBox.setBorder(new EmptyBorder(0, 12, 0, 0));
        noteBox.add(noteScroll, BorderLayout.CENTER);
        noteBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        noteBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(noteBox);
        body.add(Box.createVerticalStrut(15));

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(15);
        bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel actionButtonPanel = new JPanel(new GridLayout(1, 1, 0, 8));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton btnSaveTooth = createButton("▣  Lưu thông tin răng", BLUE);
        btnSaveTooth.addActionListener(e -> saveExaminedTooth());

        actionButtonPanel.add(btnSaveTooth);

        panel.add(bodyScroll, BorderLayout.CENTER);
        panel.add(actionButtonPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    

    private JPanel createRightPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(8, 10));

        JPanel indicationPanel = new JPanel(new BorderLayout(0, 8));
        indicationPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createTitle("Chỉ định điều trị"), BorderLayout.WEST);
        JButton btnAdd = createButton("+ Thêm chỉ định", PINK);
        btnAdd.addActionListener(e -> addIndication());
        header.add(btnAdd, BorderLayout.EAST);
        indicationPanel.add(header, BorderLayout.NORTH);

        String[] indicationColumns = {"STT", "Dịch vụ / Chỉ định", "Số lượng", "Đơn vị", "Ghi chú", "Xóa"};
        indicationModel = new DefaultTableModel(indicationColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
        };
        
        tblIndication = new JTable(indicationModel);
        styleTable(tblIndication);
        
        tblIndication.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer("Xóa"));
        tblIndication.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(tblIndication, row -> {
            if (row >= 0 && row < indicationModel.getRowCount()) {
                indicationModel.removeRow(row);
                updateSTT(indicationModel);
            }
        }, "Xóa"));
        indicationPanel.add(new JScrollPane(tblIndication), BorderLayout.CENTER);

        JPanel examinedPanel = new JPanel(new BorderLayout(0, 8));
        examinedPanel.setOpaque(false);
        examinedPanel.add(createTitle("Danh sách răng đã khám"), BorderLayout.NORTH);

        String[] examinedColumns = {"Mã răng", "Tên răng", "Tình trạng", "Mức độ", "Triệu chứng", "Chẩn đoán", "Ghi chú", "Xóa"};
        examinedModel = new DefaultTableModel(examinedColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 7; }
        };
        
        tblExamined = new JTable(examinedModel);
        styleTable(tblExamined);

        tblExamined.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblExamined.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tblExamined.convertRowIndexToModel(row);
                    String maRang = String.valueOf(examinedModel.getValueAt(modelRow, 0));
                    String tinhTrang = String.valueOf(examinedModel.getValueAt(modelRow, 2));
                    String mucDo = String.valueOf(examinedModel.getValueAt(modelRow, 3));
                    String trieuChung = String.valueOf(examinedModel.getValueAt(modelRow, 4));
                    String chanDoan = String.valueOf(examinedModel.getValueAt(modelRow, 5));
                    String ghiChu = String.valueOf(examinedModel.getValueAt(modelRow, 6));

                    if (lblToothHeaderCode != null) lblToothHeaderCode.setText(maRang);
                    if (cboCondition != null) cboCondition.setSelectedItem(tinhTrang);
                    if (cboSeverity != null) cboSeverity.setSelectedItem(mucDo);
                    if (txtSymptom != null) txtSymptom.setText(trieuChung);
                    if (txtDiagnosis != null) txtDiagnosis.setText(chanDoan);
                    if (txtNote != null) txtNote.setText(ghiChu);

                    loadServiceInfoForTooth(maRang);
                }
            }
        });
        
        tblExamined.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer("Xóa"));
        tblExamined.getColumnModel().getColumn(7).setCellEditor(new ActionEditor(tblExamined, row -> {
            if (row >= 0 && row < examinedModel.getRowCount()) {
                examinedModel.removeRow(row);
            }
        }, "Xóa"));
        examinedPanel.add(new JScrollPane(tblExamined), BorderLayout.CENTER);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 12));
        tables.setOpaque(false);
        tables.add(indicationPanel);
        tables.add(examinedPanel);
        panel.add(tables, BorderLayout.CENTER);

        JPanel footer = new JPanel(new GridLayout(1, 2, 8, 0));
        footer.setOpaque(false);
        JButton btnPrint = createButton("▣ In phiếu khám", Color.WHITE);
        btnPrint.setForeground(BLUE);
        btnPrint.setBorder(BorderFactory.createLineBorder(BLUE));
        btnPrint.addActionListener(e -> {
            try { 
                tblExamined.print(); 
                saveMedicalSessionToDatabaseWithoutConfirm();
            } 
            catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Không thể in phiếu khám!"); 
            }
        });

        JButton btnSaveSession = createButton("✓ Lưu phiếu khám", BLUE);
        btnSaveSession.addActionListener(e -> saveMedicalSessionToDatabase());
        
        footer.add(btnPrint);
        footer.add(btnSaveSession);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void loadServiceInfoForTooth(String maRangStr) {
        String maLichHen = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
        if (maLichHen.equals("--") || maLichHen.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            if (cboFormLoaiDieuTri != null && cboFormLoaiDieuTri.getItemCount() <= 1) {
                cboFormLoaiDieuTri.removeAllItems();
                List<String> listLoai = new DichVuDAO().getDanhSachLoaiDieuTri();
                for (String l : listLoai) cboFormLoaiDieuTri.addItem(l);
            }

            String sql = "SELECT dv.loaiDieuTri, dv.tenDichVu FROM ChiTietChiDinh ctd " +
                         "JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu " +
                         "WHERE ctd.maBenhAn = ? AND (ctd.maRang = ? OR ctd.maRang IS NULL)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maLichHen);
                ps.setInt(2, Integer.parseInt(maRangStr));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String loaiDT = rs.getString("loaiDieuTri");
                        String tenDV = rs.getString("tenDichVu");
                        if (cboFormLoaiDieuTri != null) cboFormLoaiDieuTri.setSelectedItem(loaiDT);
                        if (cboFormTenDichVu != null) {
                            for (int i = 0; i < cboFormTenDichVu.getItemCount(); i++) {
                                ServiceComboItem item = cboFormTenDichVu.getItemAt(i);
                                if (item != null && item.tenDV != null && item.tenDV.equals(tenDV)) {
                                    cboFormTenDichVu.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureHoSoBenhAnExists(String maBenhAn, String maLichHen) {
        try {
            HoSoBenhAnDAO hoSoDAO = new HoSoBenhAnDAO();
            boolean exists = false;
            for (HoSoBenhAn hs : hoSoDAO.getAll()) {
                if (hs.getMaBenhAn().equalsIgnoreCase(maBenhAn)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                String maKhachHang = null;
                String maNhanVien = null;
                String sqlLH = "SELECT maKhachHang, maNhanVien FROM LichHen WHERE maLichHen = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlLH)) {
                    ps.setString(1, maLichHen);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maKhachHang = rs.getString("maKhachHang");
                            maNhanVien = rs.getString("maNhanVien");
                        }
                    }
                }

                HoSoBenhAn newHs = new HoSoBenhAn();
                newHs.setMaBenhAn(maBenhAn);
                newHs.setMaKhachHang(maKhachHang != null ? maKhachHang : maLichHen);
                newHs.setMaNhanVien(maNhanVien);
                newHs.setNgayKham(new java.util.Date());
                newHs.setTrangThai("Đang khám");
                hoSoDAO.insert(newHs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExistingExaminationData(String maLichHen) {
        if (examinedModel != null) examinedModel.setRowCount(0);
        if (indicationModel != null) indicationModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String sqlRang = "SELECT maRang, tinhTrang, mucDo, trieuChung, chanDoan, ghiChu FROM ChiTietKhamRang WHERE maBenhAn = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlRang)) {
                ps.setString(1, maLichHen);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int code = rs.getInt("maRang");
                        String tenRang = ChiTietKhamRangDAO.loadTeethFromDatabase(code);
                        
                        examinedModel.addRow(new Object[]{
                            code,
                            tenRang != null ? tenRang : "Răng số " + code,
                            rs.getString("tinhTrang"),
                            rs.getString("mucDo"),
                            rs.getString("trieuChung"),
                            rs.getString("chanDoan"),
                            rs.getString("ghiChu"),
                            "Xóa"
                        });
                    }
                }
            }

            String sqlCD = "SELECT ctd.maDichVu, dv.tenDichVu, ctd.soLuong, ctd.donVi, ctd.ghiChu " +
                           "FROM ChiTietChiDinh ctd " +
                           "LEFT JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu " +
                           "WHERE ctd.maBenhAn = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCD)) {
                ps.setString(1, maLichHen);
                try (ResultSet rs = ps.executeQuery()) {
                    int stt = 1;
                    while (rs.next()) {
                        String tenDV = rs.getString("tenDichVu");
                        indicationModel.addRow(new Object[]{
                            stt++,
                            tenDV != null ? tenDV : rs.getString("maDichVu"),
                            rs.getInt("soLuong"),
                            rs.getString("donVi"),
                            rs.getString("ghiChu"),
                            "Xóa"
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveExaminedTooth() {
        if (selectedTooth == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn răng trên sơ đồ FDI cần lưu thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String maLichHen = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
        if (maLichHen.equals("--") || maLichHen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn thông tin bệnh nhân!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int code = selectedTooth.code;
        String tinhTrang = cboCondition != null && cboCondition.getSelectedItem() != null ? cboCondition.getSelectedItem().toString() : "";
        String mucDo = cboSeverity != null && cboSeverity.getSelectedItem() != null ? cboSeverity.getSelectedItem().toString() : "";
        String trieuChung = txtSymptom != null ? txtSymptom.getText().trim() : "";
        String chanDoan = txtDiagnosis != null ? txtDiagnosis.getText().trim() : "";
        String ghiChu = txtNote != null ? txtNote.getText().trim() : "";

        try (Connection conn = DBConnection.getConnection()) {
            ensureHoSoBenhAnExists(maLichHen, maLichHen);
            
            String checkSql = "SELECT COUNT(*) FROM ChiTietKhamRang WHERE maBenhAn = ? AND maRang = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setString(1, maLichHen);
                psCheck.setInt(2, code);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        String updateSql = "UPDATE ChiTietKhamRang SET tinhTrang = ?, mucDo = ?, trieuChung = ?, chanDoan = ?, ghiChu = ? WHERE maBenhAn = ? AND maRang = ?";
                        try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                            psUp.setString(1, tinhTrang);
                            psUp.setString(2, mucDo);
                            psUp.setString(3, trieuChung);
                            psUp.setString(4, chanDoan);
                            psUp.setString(5, ghiChu);
                            psUp.setString(6, maLichHen);
                            psUp.setInt(7, code);
                            psUp.executeUpdate();
                        }
                    } else {
                        String insertSql = "INSERT INTO ChiTietKhamRang (maBenhAn, maRang, tinhTrang, mucDo, trieuChung, chanDoan, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                            psIns.setString(1, maLichHen);
                            psIns.setInt(2, code);
                            psIns.setString(3, tinhTrang);
                            psIns.setString(4, mucDo);
                            psIns.setString(5, trieuChung);
                            psIns.setString(6, chanDoan);
                            psIns.setString(7, ghiChu);
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loadExistingExaminationData(maLichHen);
        JOptionPane.showMessageDialog(this, "Đã lưu thông tin răng thành công!");
    }

    private void saveMedicalSessionToDatabase() {
        if (lblPatientCode == null) return;
        String maLichHen = lblPatientCode.getText().trim(); 
        
        if (maLichHen.equals("--") || maLichHen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn thông tin bệnh nhân!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc chắn muốn hoàn tất và lưu phiếu khám này?", 
            "Xác nhận lưu phiếu khám", 
            JOptionPane.YES_NO_OPTION
        );
            
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        saveMedicalSessionToDatabaseWithoutConfirm();
    }

    private void saveMedicalSessionToDatabaseWithoutConfirm() {
        if (lblPatientCode == null) return;
        String maLichHen = lblPatientCode.getText().trim(); 
        if (maLichHen.equals("--") || maLichHen.isEmpty()) return;

        try {
            HoSoBenhAnDAO hoSoDAO = new HoSoBenhAnDAO();
            String maBenhAn = maLichHen;
            
            for (HoSoBenhAn hs : hoSoDAO.getAll()) {
                if (hs.getMaBenhAn().equalsIgnoreCase(maBenhAn)) {
                    hs.setTrangThai("Hoàn thành");
                    break;
                }
            }

            if (lichHenDAO != null) {
                lichHenDAO.updateStatus(maLichHen, "Hoàn thành");
            }

            if (lblPatientCode != null) lblPatientCode.setText("--");
            if (lblPatientName != null) lblPatientName.setText("--");
            if (examinedModel != null) examinedModel.setRowCount(0);
            if (indicationModel != null) indicationModel.setRowCount(0);

            JOptionPane.showMessageDialog(this, "Đã hoàn tất lưu phiếu khám thành công!");
            showPhieuKhamList();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu khám: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToothSelected(StandardToothChartPanel.ToothData tooth) {
        if (tooth == null) return;
        selectedTooth = tooth;
        if (lblToothCode != null) lblToothCode.setText(String.valueOf(tooth.code));
        if (lblToothName != null) lblToothName.setText(tooth.name);
        if (lblJaw != null) lblJaw.setText(tooth.jaw);
        if (lblPosition != null) lblPosition.setText(tooth.position);
        if (lblType != null) lblType.setText(tooth.type);

        if (lblToothHeaderCode != null) lblToothHeaderCode.setText(String.valueOf(tooth.code));
        if (lblToothHeaderName != null) lblToothHeaderName.setText(tooth.name);
        if (lblToothHeaderType != null) lblToothHeaderType.setText(tooth.type);

        if (cboCondition != null && cboCondition.getItemCount() > 0) cboCondition.setSelectedIndex(0);
        if (cboSeverity != null && cboSeverity.getItemCount() > 0) cboSeverity.setSelectedIndex(0);
        if (txtSymptom != null) txtSymptom.setText("");
        if (txtDiagnosis != null) txtDiagnosis.setText("");
        if (txtNote != null) txtNote.setText("");
    }

    
    
    
    
    private void addIndication() {
        Color PINK_BG = new Color(255, 240, 245);
        Color LIGHT_BLUE = new Color(231, 241, 255);
        Color BORDER_COLOR = new Color(190, 205, 230);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Thêm chỉ định điều trị",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setUndecorated(true);
        dialog.setSize(680, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PINK_BG);
        root.setBorder(BorderFactory.createLineBorder(NAVY, 1));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(12, 20, 12, 12));
        header.setPreferredSize(new Dimension(680, 75));

        JLabel lblTitle = new JLabel("Thêm chỉ định điều trị");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(Color.WHITE);

        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleArea.setOpaque(false);
        titleArea.add(lblTitle);

        JButton btnClose = new JButton("×");
        styleIndicationButton(btnClose, NAVY, Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 28));
        btnClose.setPreferredSize(new Dimension(42, 42));
        btnClose.setBorderPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());

        header.add(titleArea, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PINK_BG);
        form.setBorder(new EmptyBorder(20, 25, 20, 25));

        JTextField txtMaDV = createIndicationField();
        txtMaDV.setEditable(false);
        txtMaDV.setBackground(LIGHT_BLUE);
        txtMaDV.setForeground(new Color(25, 75, 145));

        JComboBox<String> cboLoaiDieuTri = new JComboBox<>();
        styleComboDialog(cboLoaiDieuTri);

        JComboBox<ServiceComboItem> cboService = new JComboBox<>();
        styleComboDialog(cboService);

        JTextField txtUnit = createIndicationField();
        txtUnit.setEditable(false);
        txtUnit.setBackground(LIGHT_BLUE);

        List<ServiceComboItem> allServices = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh FROM DichVu");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                allServices.add(new ServiceComboItem(
                    rs.getString("maDichVu"), 
                    rs.getString("tenDichVu"), 
                    rs.getString("loaiDieuTri"),
                    rs.getDouble("donGia"),
                    rs.getString("donViTinh")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Lỗi tải dữ liệu bảng DichVu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        List<String> distinctLoai = new ArrayList<>();
        for (ServiceComboItem s : allServices) {
            if (s.loaiDieuTri != null && !distinctLoai.contains(s.loaiDieuTri)) {
                distinctLoai.add(s.loaiDieuTri);
            }
        }
        for (String loai : distinctLoai) {
            cboLoaiDieuTri.addItem(loai);
        }

        cboLoaiDieuTri.addActionListener(e -> {
            String selectedLoai = (String) cboLoaiDieuTri.getSelectedItem();
            cboService.removeAllItems();
            for (ServiceComboItem s : allServices) {
                if (s.loaiDieuTri != null && s.loaiDieuTri.equals(selectedLoai)) {
                    cboService.addItem(s);
                }
            }
        });

        cboService.addActionListener(e -> {
            ServiceComboItem selected = (ServiceComboItem) cboService.getSelectedItem();
            if (selected != null) {
                txtMaDV.setText(selected.maDV);
                txtUnit.setText(selected.donVi);
            }
        });

        if (cboLoaiDieuTri.getItemCount() > 0) {
            cboLoaiDieuTri.setSelectedIndex(0);
        }

        JTextField txtQuantity = createIndicationField();
        txtQuantity.setText("1");

        JTextArea txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtDescription.setForeground(NAVY);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(9, 12, 9, 12));

        JScrollPane noteScroll = new JScrollPane(txtDescription);
        noteScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        noteScroll.setPreferredSize(new Dimension(390, 65));

        addIndicationRow(form, 0, "▥", "Mã chỉ định :", txtMaDV);
        addIndicationRow(form, 1, "♧", "Loại điều trị :", cboLoaiDieuTri);
        addIndicationRow(form, 2, "🦷", "Tên dịch vụ :", cboService);
        addIndicationRow(form, 3, "▱", "Số lượng :", txtQuantity);
        addIndicationRow(form, 4, "≡", "Đơn vị :", txtUnit);
        addIndicationRow(form, 5, "✎", "Ghi chú :", noteScroll);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 15));
        footer.setBackground(PINK_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 205, 218)));

        JButton btnOK = new JButton("✓   OK");
        btnOK.setPreferredSize(new Dimension(160, 45));
        styleIndicationButton(btnOK, NAVY, Color.WHITE);

        JButton btnCancel = new JButton("×   Cancel");
        btnCancel.setPreferredSize(new Dimension(160, 45));
        styleIndicationButton(btnCancel, Color.WHITE, NAVY);
        btnCancel.setBorder(BorderFactory.createLineBorder(NAVY, 1));

        footer.add(btnOK);
        footer.add(btnCancel);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnOK.addActionListener(e -> {
            ServiceComboItem selected = (ServiceComboItem) cboService.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn dịch vụ!");
                return;
            }

            String soLuong = txtQuantity.getText().trim();
            String ghiChu = txtDescription.getText().trim();

            try {
                int quantity = Integer.parseInt(soLuong);
                if (quantity <= 0) throw new NumberFormatException();

                String maLichHen = lblPatientCode != null ? lblPatientCode.getText().trim() : "";
                if (!maLichHen.equals("--") && !maLichHen.isEmpty()) {
                    ensureHoSoBenhAnExists(maLichHen, maLichHen);
                    
                    ChiTietChiDinhDAO chiDinhDAO = new ChiTietChiDinhDAO();
                    ChiTietChiDinh ctd = new ChiTietChiDinh();
                    ctd.setMaBenhAn(maLichHen);
                    ctd.setMaDichVu(selected.maDV);
                    if (selectedTooth != null) {
                        ctd.setMaRang(selectedTooth.code);
                    }
                    ctd.setSoLuong(quantity);
                    ctd.setDonVi(selected.donVi);
                    ctd.setGhiChu(ghiChu);
                    chiDinhDAO.insert(ctd);
                }

                if (indicationModel != null) {
                    indicationModel.addRow(new Object[]{
                        indicationModel.getRowCount() + 1,
                        selected.tenDV,
                        quantity,
                        selected.donVi,
                        ghiChu,
                        "Xóa"
                    });
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Số lượng phải là số nguyên dương!");
                txtQuantity.requestFocus();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Lỗi khi lưu chỉ định: " + ex.getMessage());
            }
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private void styleComboDialog(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        combo.setBackground(Color.WHITE);
        combo.setForeground(NAVY);
        combo.setPreferredSize(new Dimension(390, 40));
    }

    private static class ServiceComboItem {
        public String maDV, tenDV, loaiDieuTri, donVi;
        public double donGia;

        public ServiceComboItem(String maDV, String tenDV, String donVi) {
            this.maDV = maDV;
            this.tenDV = tenDV;
            this.donVi = donVi;
        }

        public ServiceComboItem(String maDV, String tenDV, String loaiDieuTri, double donGia, String donVi) {
            this.maDV = maDV;
            this.tenDV = tenDV;
            this.loaiDieuTri = loaiDieuTri;
            this.donGia = donGia;
            this.donVi = donVi;
        }

        @Override
        public String toString() {
            return tenDV != null ? tenDV : maDV;
        }
    }

    private JTextField createIndicationField() {
        JTextField field = new JTextField(18);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setForeground(new Color(15, 29, 69));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 205, 230), 1),
                new EmptyBorder(9, 12, 9, 12)
        ));
        field.setPreferredSize(new Dimension(390, 40));
        return field;
    }

    private void addIndicationRow(JPanel form, int row, String icon, String title, JComponent component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(6, 5, 6, 12);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 26));
        lblIcon.setForeground(new Color(15, 29, 69));
        lblIcon.setPreferredSize(new Dimension(42, 38));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(lblIcon, gbc);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 29, 69));
        lblTitle.setPreferredSize(new Dimension(145, 35));

        gbc.gridx = 1;
        form.add(lblTitle, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);
        form.add(component, gbc);
    }

    private void styleIndicationButton(JButton button, Color background, Color foreground) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.equals(Color.WHITE) ? new Color(15, 29, 69) : background, 1),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadPatient(int modelRow) {
        if (receptionModel == null || modelRow < 0 || modelRow >= receptionModel.getRowCount()) return;

        if (lblPatientCode != null) lblPatientCode.setText(String.valueOf(receptionModel.getValueAt(modelRow, 1))); 
        if (lblPatientName != null) lblPatientName.setText(String.valueOf(receptionModel.getValueAt(modelRow, 2))); 
        if (lblAge != null) lblAge.setText(String.valueOf(receptionModel.getValueAt(modelRow, 3)));         
        if (lblGender != null) lblGender.setText(String.valueOf(receptionModel.getValueAt(modelRow, 4)));      
        if (lblPhone != null) lblPhone.setText(String.valueOf(receptionModel.getValueAt(modelRow, 5)));       
        if (lblDate != null) lblDate.setText(String.valueOf(receptionModel.getValueAt(modelRow, 6)));        
        if (lblTime != null) lblTime.setText(String.valueOf(receptionModel.getValueAt(modelRow, 7)));   

        if (examinedModel != null) examinedModel.setRowCount(0);
        if (indicationModel != null) indicationModel.setRowCount(0);
        selectedTooth = null;
        
        String maLichHen = lblPatientCode.getText().trim();
        if (!maLichHen.equals("--") && !maLichHen.isEmpty()) {
            loadExistingExaminationData(maLichHen);
        }

        if (lblToothHeaderCode != null) lblToothHeaderCode.setText("--");
        if (lblToothHeaderName != null) lblToothHeaderName.setText("Chưa chọn răng");
        if (lblToothHeaderType != null) lblToothHeaderType.setText("--");
    }

    private void updateSTT(DefaultTableModel model) {
        if (model == null) return;
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 15);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(5, 6, 5, 6));
        return area;
    }

    private JScrollPane createScroll(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(100, 55));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        return scroll;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 8, 7, 8)));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
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
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setForeground(TEXT);
        table.setRowHeight(34);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(225, 239, 255));
        table.setSelectionForeground(NAVY);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(new Color(238, 245, 253));
        header.setForeground(NAVY);
        header.setPreferredSize(new Dimension(0, 36));
    }

    private class ActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        ActionRenderer(String text) {
            setUI(new BasicButtonUI());
            setText(text);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
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

    private class ActionEditor extends DefaultCellEditor {
        private final JButton button;
        private final JTable table;
        private final java.util.function.IntConsumer action;
        private int selectedRow = -1;

        ActionEditor(JTable table, java.util.function.IntConsumer action, String text) {
            super(new JCheckBox());
            this.table = table;
            this.action = action;

            button = new JButton(text);
            button.setUI(new BasicButtonUI());
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setForeground(PINK);
            button.setBackground(new Color(255, 239, 245));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(false);
            button.setBorder(new EmptyBorder(4, 8, 4, 8));
            button.putClientProperty("JButton.buttonType", "roundRect");

            button.addActionListener(e -> {
                int row = selectedRow;
                fireEditingStopped();
                if (row >= 0 && this.action != null) {
                    this.action.accept(row);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            if (row >= 0 && row < table.getRowCount()) {
                selectedRow = table.convertRowIndexToModel(row);
            } else {
                selectedRow = -1;
            }
            return button;
        }
    }

    private JPanel createTreatmentPlanPage() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(15, 18, 15, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = createTitle("Lập kế hoạch điều trị");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Xây dựng lộ trình, dịch vụ, chi phí và lịch điều trị cho bệnh nhân");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnBack = createButton("↶ Quay lại", Color.WHITE);
        btnBack.addActionListener(e -> showPhieuKhamList());
        actions.add(btnBack);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createPlanRow(createPatientPlanCardPlan(), createPlanSummaryCardPlan(), 0.64, 0.36, 150));
        body.add(Box.createVerticalStrut(12));
        body.add(createPlanRow(createPlanToothCardPlan(), createPlanServiceCardPlan(), 0.37, 0.63, 220));
        body.add(Box.createVerticalStrut(12));
        body.add(createPlanRow(createPlanInformationCardPlan(), createTreatmentRouteCardPlan(), 0.31, 0.69, 220));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    private JPanel createPatientPlanCardPlan() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(15, 0));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel heading = createTitle("Thông tin bệnh nhân");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 15));

        lblPlanMaBN = planText("Mã bệnh nhân: --");
        lblPlanTenBN = planText("Họ và tên: --");
        lblPlanSdt = planText("Số điện thoại: --");

        info.add(heading);
        info.add(Box.createVerticalStrut(7));
        info.add(lblPlanMaBN);
        info.add(lblPlanTenBN);
        info.add(lblPlanSdt);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPlanSummaryCardPlan() {
        JPanel card = createCard();
        card.setLayout(new GridLayout(1, 3, 8, 0));
        card.setBackground(new Color(255, 241, 247));

        lblPlanTongChiPhi = new JLabel("--", JLabel.CENTER);
        lblPlanTongDot = new JLabel("1", JLabel.CENTER);
        lblPlanTongDichVu = new JLabel("0", JLabel.CENTER);

        card.add(createSummaryItemPlan("Tổng chi phí dự kiến", lblPlanTongChiPhi));
        card.add(createSummaryItemPlan("Tổng đợt", lblPlanTongDot));
        card.add(createSummaryItemPlan("Tổng dịch vụ", lblPlanTongDichVu));
        return card;
    }

    private JPanel createSummaryItemPlan(String title, JLabel lblValue) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(NAVY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblValue.setForeground(PINK);
        lblValue.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblValue);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createPlanToothCardPlan() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));

        JLabel lblTitle = createTitle("🦷  Tình trạng răng & chỉ định (Tích chọn để hiện sang kế hoạch)");
        card.add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Chọn", "Răng", "Tình trạng", "Chẩn đoán", "Dịch vụ chỉ định"};
        planToothModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 0; }
            @Override public Class<?> getColumnClass(int column) { return column == 0 ? Boolean.class : String.class; }
        };

        JTable table = new JTable(planToothModel);
        styleTable(table);

        planToothModel.addTableModelListener(e -> {
            if (e.getColumn() == 0) {
                syncToothToPlanServiceTable();
            }
        });

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel createPlanServiceCardPlan() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = createTitle("▤  Kế hoạch điều trị");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttons.setOpaque(false);

        JButton btnAddCustomService = createButton("+ Thêm dịch vụ", PINK);
        btnAddCustomService.addActionListener(e -> openAddServiceDialog());
        buttons.add(btnAddCustomService);

        header.add(title, BorderLayout.WEST);
        header.add(buttons, BorderLayout.EAST);

        String[] columns = {"Dịch vụ điều trị", "Răng / Vị trí", "Số lượng", "Đơn giá", "Thành tiền", "Ngày dự kiến", "Trạng thái"};
        planServiceModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return true; }
        };

        JTable table = new JTable(planServiceModel);
        styleTable(table);

        card.add(header, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel createPlanInformationCardPlan() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        card.add(createTitle("▤  Thông tin kế hoạch"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 5, 7, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtPlanName = new JTextField();
        styleTextField(txtPlanName);

        g.gridx = 0; g.gridy = 0; form.add(planText("Tên kế hoạch:"), g);
        g.gridx = 1; g.weightx = 1; form.add(txtPlanName, g);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTreatmentRouteCardPlan() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 12));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(12, 12, 12, 12)));
        
        JPanel routeHeader = new JPanel(new BorderLayout());
        routeHeader.setOpaque(false);
        routeHeader.add(createTitle("♧  Lộ trình điều trị"), BorderLayout.WEST);
        
        JButton btnAddRouteStep = createButton("+ Thêm đợt", PINK);
        btnAddRouteStep.addActionListener(e -> addNewRouteStep());
        routeHeader.add(btnAddRouteStep, BorderLayout.EAST);
        
        card.add(routeHeader, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 0));
        content.setOpaque(false);

        stepsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        stepsContainer.setOpaque(false);

        routeStepsList.clear();
        routeStepsList.add(new RouteStepData("Đợt 1", LocalDate.now().toString(), "Dịch vụ điều trị tổng hợp"));
        rebuildRouteStepsUI();

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(new Color(250, 252, 255));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblStatus = new JLabel("⚑  Trạng thái");
        cboPlanStatus = new JComboBox<>(new String[]{"Chờ duyệt", "Đã duyệt", "Đang thực hiện", "Hoàn thành"});
        styleCombo(cboPlanStatus);

        lblPlanBottomCost = new JLabel("Tổng chi phí: --");
        lblPlanBottomCost.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPlanBottomCost.setForeground(PINK);

        JButton btnConfirmRoute = createButton("✓ Xác nhận", BLUE);
        btnConfirmRoute.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmRoute.addActionListener(e -> {
            if (currentSelectedMaLichHen == null || currentSelectedMaLichHen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy mã phiếu khám tương ứng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE HoSoBenhAn SET trangThai = N'Đã lập kế hoạch' WHERE maBenhAn = ?")) {
                ps.setString(1, currentSelectedMaLichHen);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã xác nhận kế hoạch điều trị thành công!");
                showPhieuKhamList();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        statusPanel.add(lblStatus);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(cboPlanStatus);
        statusPanel.add(Box.createVerticalStrut(15));
        statusPanel.add(lblPlanBottomCost);
        statusPanel.add(Box.createVerticalStrut(15));
        statusPanel.add(btnConfirmRoute);

        content.add(new JScrollPane(stepsContainer, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        content.add(statusPanel, BorderLayout.EAST);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private static class RouteStepData {
        public String title;
        public String date;
        public String service;

        public RouteStepData(String title, String date, String service) {
            this.title = title;
            this.date = date;
            this.service = service;
        }
    }

    private void addNewRouteStep() {
        int nextIndex = routeStepsList.size() + 1;
        String defaultService = (planServiceModel != null && planServiceModel.getRowCount() > 0) ? 
                String.valueOf(planServiceModel.getValueAt(0, 0)) : "Dịch vụ đợt " + nextIndex;
        routeStepsList.add(new RouteStepData("Đợt " + nextIndex, LocalDate.now().plusDays(nextIndex * 7 - 7).toString(), defaultService));
        rebuildRouteStepsUI();
    }

    private void rebuildRouteStepsUI() {
        if (stepsContainer == null) return;
        stepsContainer.removeAll();

        for (int i = 0; i < routeStepsList.size(); i++) {
            RouteStepData stepData = routeStepsList.get(i);
            stepsContainer.add(createRouteStepCardWidget(stepData, i));
        }

        if (lblPlanTongDot != null) {
            lblPlanTongDot.setText(String.valueOf(routeStepsList.size()));
        }

        stepsContainer.revalidate();
        stepsContainer.repaint();
    }

    private JPanel createRouteStepCardWidget(RouteStepData data, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(235, 244, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 225, 250)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(170, 170));
        panel.setMinimumSize(new Dimension(170, 170));
        panel.setMaximumSize(new Dimension(170, 170));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lblNumber = new JLabel(data.title);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNumber.setForeground(BLUE);
        lblNumber.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField datePickerRoute = new JTextField();
        datePickerRoute.setText(data.date);
        styleTextField(datePickerRoute);
        datePickerRoute.setMaximumSize(new Dimension(150, 30));

        datePickerRoute.addActionListener(e -> {
            data.date = datePickerRoute.getText().trim();
        });

        String patientNameText = lblPlanTenBN != null ? lblPlanTenBN.getText().replace("Họ và tên: ", "") : "--";
        JLabel lblPatient = new JLabel("BN: " + patientNameText);
        lblPatient.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPatient.setForeground(TEXT);
        lblPatient.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblService = new JLabel(data.service);
        lblService.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblService.setForeground(MUTED);
        lblService.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(lblNumber);
        panel.add(Box.createVerticalStrut(6));
        panel.add(datePickerRoute);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblPatient);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblService);
        panel.add(Box.createVerticalGlue());

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openEditRouteStepDialog(data, index);
            }
        });

        return panel;
    }

    private void openEditRouteStepDialog(RouteStepData data, int index) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chỉnh sửa thông tin " + data.title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 310);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        form.setBackground(Color.WHITE);

        JTextField txtTitle = new JTextField(data.title);
        styleTextField(txtTitle);

        JTextField txtDate = new JTextField(data.date);
        styleTextField(txtDate);

        JComboBox<String> cboLoaiDieuTri = new JComboBox<>();
        styleCombo(cboLoaiDieuTri);

        JComboBox<ServiceComboItem> cboServices = new JComboBox<>();
        styleCombo(cboServices);

        List<ServiceComboItem> allServices = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh FROM DichVu");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                allServices.add(new ServiceComboItem(
                    rs.getString("maDichVu"),
                    rs.getString("tenDichVu"),
                    rs.getString("loaiDieuTri"),
                    rs.getDouble("donGia"),
                    rs.getString("donViTinh")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<String> distinctLoai = new ArrayList<>();
        for (ServiceComboItem s : allServices) {
            if (s.loaiDieuTri != null && !distinctLoai.contains(s.loaiDieuTri)) {
                distinctLoai.add(s.loaiDieuTri);
            }
        }
        for (String loai : distinctLoai) {
            cboLoaiDieuTri.addItem(loai);
        }

        cboLoaiDieuTri.addActionListener(e -> {
            String selectedLoai = (String) cboLoaiDieuTri.getSelectedItem();
            cboServices.removeAllItems();
            for (ServiceComboItem s : allServices) {
                if (s.loaiDieuTri != null && s.loaiDieuTri.equals(selectedLoai)) {
                    cboServices.addItem(s);
                }
            }
        });

        for (ServiceComboItem item : allServices) {
            if (item.tenDV != null && item.tenDV.equals(data.service)) {
                cboLoaiDieuTri.setSelectedItem(item.loaiDieuTri);
                cboServices.setSelectedItem(item);
                break;
            }
        }

        form.add(new JLabel("Tên đợt:"));
        form.add(txtTitle);
        form.add(new JLabel("Ngày dự kiến:"));
        form.add(txtDate);
        
        JPanel panelServiceWrapper = new JPanel(new BorderLayout(5, 5));
        panelServiceWrapper.setOpaque(false);
        panelServiceWrapper.add(cboLoaiDieuTri, BorderLayout.NORTH);
        panelServiceWrapper.add(cboServices, BorderLayout.SOUTH);

        form.add(new JLabel("<html>Loại điều trị &<br>Dịch vụ:</html>"));
        form.add(panelServiceWrapper);

        JButton btnSave = createButton("Lưu thay đổi", BLUE);
        btnSave.setPreferredSize(new Dimension(120, 38));
        btnSave.addActionListener(e -> {
            data.title = txtTitle.getText().trim();
            data.date = txtDate.getText().trim();
            ServiceComboItem selected = (ServiceComboItem) cboServices.getSelectedItem();
            if (selected != null) {
                data.service = selected.tenDV;
            }
            rebuildRouteStepsUI();
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openAddServiceDialog() {
        Color PINK_BG = new Color(255, 240, 245);
        Color LIGHT_BLUE = new Color(231, 241, 255);
        Color BORDER_COLOR = new Color(190, 205, 230);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Thêm dịch vụ điều trị",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setUndecorated(true);
        dialog.setSize(680, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PINK_BG);
        root.setBorder(BorderFactory.createLineBorder(NAVY, 1));

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(12, 20, 12, 12));
        header.setPreferredSize(new Dimension(680, 65));

        JLabel lblTitle = new JLabel("Thêm dịch vụ điều trị");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleArea.setOpaque(false);
        titleArea.add(lblTitle);

        JButton btnClose = new JButton("×");
        styleIndicationButton(btnClose, NAVY, Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnClose.setPreferredSize(new Dimension(38, 38));
        btnClose.setBorderPainted(false);
        btnClose.addActionListener(e -> dialog.dispose());

        header.add(titleArea, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PINK_BG);
        form.setBorder(new EmptyBorder(15, 25, 15, 25));

        JComboBox<String> cboLoaiDieuTri = new JComboBox<>();
        styleComboDialog(cboLoaiDieuTri);

        JComboBox<ServiceComboItem> cboServices = new JComboBox<>();
        styleComboDialog(cboServices);

        JTextField txtTooth = createIndicationField();
        JTextField txtQuantity = createIndicationField();
        txtQuantity.setText("1");

        JTextArea txtDescription = new JTextArea(2, 20);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtDescription.setForeground(NAVY);
        txtDescription.setBackground(Color.WHITE);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBorder(new EmptyBorder(7, 10, 7, 10));
        JScrollPane noteScroll = new JScrollPane(txtDescription);
        noteScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        noteScroll.setPreferredSize(new Dimension(390, 55));

        List<ServiceComboItem> allServices = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT maDichVu, tenDichVu, loaiDieuTri, donGia, donViTinh FROM DichVu");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                allServices.add(new ServiceComboItem(
                    rs.getString("maDichVu"), 
                    rs.getString("tenDichVu"), 
                    rs.getString("loaiDieuTri"),
                    rs.getDouble("donGia"),
                    rs.getString("donViTinh")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<String> distinctLoai = new ArrayList<>();
        for (ServiceComboItem s : allServices) {
            if (s.loaiDieuTri != null && !distinctLoai.contains(s.loaiDieuTri)) {
                distinctLoai.add(s.loaiDieuTri);
            }
        }
        for (String loai : distinctLoai) {
            cboLoaiDieuTri.addItem(loai);
        }

        cboLoaiDieuTri.addActionListener(e -> {
            String selectedLoai = (String) cboLoaiDieuTri.getSelectedItem();
            cboServices.removeAllItems();
            for (ServiceComboItem s : allServices) {
                if (s.loaiDieuTri != null && s.loaiDieuTri.equals(selectedLoai)) {
                    cboServices.addItem(s);
                }
            }
        });

        if (cboLoaiDieuTri.getItemCount() > 0) {
            cboLoaiDieuTri.setSelectedIndex(0);
        }

        addIndicationRow(form, 0, "♧", "Loại điều trị :", cboLoaiDieuTri);
        addIndicationRow(form, 1, "🦷", "Tên dịch vụ :", cboServices);
        addIndicationRow(form, 2, "🦷", "Răng (nếu có) :", txtTooth);
        addIndicationRow(form, 3, "▱", "Số lượng :", txtQuantity);
        addIndicationRow(form, 4, "✎", "Ghi chú :", noteScroll);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        footer.setBackground(PINK_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(230, 205, 218)));

        JButton btnOK = new JButton("✓   Xác nhận thêm");
        btnOK.setPreferredSize(new Dimension(170, 42));
        styleIndicationButton(btnOK, NAVY, Color.WHITE);

        JButton btnCancel = new JButton("×   Hủy");
        btnCancel.setPreferredSize(new Dimension(120, 42));
        styleIndicationButton(btnCancel, Color.WHITE, NAVY);
        btnCancel.setBorder(BorderFactory.createLineBorder(NAVY, 1));

        footer.add(btnOK);
        footer.add(btnCancel);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnOK.addActionListener(e -> {
            ServiceComboItem selected = (ServiceComboItem) cboServices.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn dịch vụ!");
                return;
            }

            int qty = 1;
            try {
                qty = Integer.parseInt(txtQuantity.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Số lượng phải là số nguyên dương!");
                txtQuantity.requestFocus();
                return;
            }

            double subTotal = qty * selected.donGia;
            String rangStr = txtTooth.getText().trim();

            if (planServiceModel != null) {
                planServiceModel.addRow(new Object[]{
                    selected.tenDV,
                    rangStr.isEmpty() ? "--" : "Răng " + rangStr,
                    qty,
                    selected.donGia,
                    subTotal,
                    LocalDate.now().toString(),
                    "Chờ thực hiện"
                });
                updatePlanTotalsFromServiceTable();
            }
            dialog.dispose();
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private void loadTreatmentPlanData(String maBenhAn) {
        if (maBenhAn == null || maBenhAn.equals("--") || maBenhAn.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            String sqlInfo = 
                "SELECT kh.tenKH, kh.sdt FROM HoSoBenhAn hs " +
                "JOIN KhachHang kh ON hs.maKhachHang = kh.maKhachHang " +
                "WHERE hs.maBenhAn = ?";
            
            try (PreparedStatement ps = conn.prepareStatement(sqlInfo)) {
                ps.setString(1, maBenhAn);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (lblPlanMaBN != null) lblPlanMaBN.setText("Mã bệnh nhân: " + maBenhAn);
                        if (lblPlanTenBN != null) lblPlanTenBN.setText("Họ và tên: " + rs.getString("tenKH"));
                        if (lblPlanSdt != null) lblPlanSdt.setText("Số điện thoại: " + rs.getString("sdt"));
                    }
                }
            }

            if (planToothModel != null) planToothModel.setRowCount(0);
            if (planServiceModel != null) planServiceModel.setRowCount(0);

            String sqlDetails = 
                "SELECT ctd.maRang, r.tinhTrang, r.chanDoan, dv.tenDichVu " +
                "FROM ChiTietChiDinh ctd " +
                "LEFT JOIN ChiTietKhamRang r ON ctd.maBenhAn = r.maBenhAn AND ctd.maRang = r.maRang " +
                "LEFT JOIN DichVu dv ON ctd.maDichVu = dv.maDichVu " +
                "WHERE ctd.maBenhAn = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlDetails)) {
                ps.setString(1, maBenhAn);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        int maRang = rs.getInt("maRang");
                        String tinhTrang = rs.getString("tinhTrang");
                        String chanDoan = rs.getString("chanDoan");
                        String tenDichVu = rs.getString("tenDichVu");

                        planToothModel.addRow(new Object[]{
                            false,
                            maRang > 0 ? "Răng " + maRang : "Chung / Không gắn răng",
                            tinhTrang != null && !tinhTrang.isEmpty() ? tinhTrang : "Chưa cập nhật tình trạng",
                            chanDoan != null && !chanDoan.isEmpty() ? chanDoan : "Chưa có chẩn đoán",
                            tenDichVu != null ? tenDichVu : "--"
                        });
                    }

                    if (!hasData) {
                        String sqlFallback = "SELECT maRang, tinhTrang, chanDoan FROM ChiTietKhamRang WHERE maBenhAn = ?";
                        try (PreparedStatement psFB = conn.prepareStatement(sqlFallback)) {
                            psFB.setString(1, maBenhAn);
                            try (ResultSet rsFB = psFB.executeQuery()) {
                                while (rsFB.next()) {
                                    planToothModel.addRow(new Object[]{
                                        false,
                                        "Răng " + rsFB.getInt("maRang"),
                                        rsFB.getString("tinhTrang") != null ? rsFB.getString("tinhTrang") : "Chưa cập nhật",
                                        rsFB.getString("chanDoan") != null ? rsFB.getString("chanDoan") : "Chưa có",
                                        "Khám tổng quát"
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncToothToPlanServiceTable() {
        if (planServiceModel != null) planServiceModel.setRowCount(0);
        double totalCost = 0;
        int serviceCount = 0;

        for (int i = 0; i < planToothModel.getRowCount(); i++) {
            Boolean isChecked = (Boolean) planToothModel.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                serviceCount++;
                String rang = (String) planToothModel.getValueAt(i, 1);
                String serviceName = (String) planToothModel.getValueAt(i, 4);
                
                double donGia = getPriceByServiceName(serviceName);
                double thanhTien = donGia * 1;
                totalCost += thanhTien;

                planServiceModel.addRow(new Object[]{
                    serviceName,
                    rang,
                    1,
                    donGia,
                    thanhTien,
                    LocalDate.now().toString(),
                    "Chờ thực hiện"
                });
            }
        }

        if (serviceCount > 0) {
            if (lblPlanTongChiPhi != null) lblPlanTongChiPhi.setText(String.format("%,.0f đ", totalCost));
            if (lblPlanBottomCost != null) lblPlanBottomCost.setText(String.format("Tổng chi phí: %,.0f đ", totalCost));
        } else {
            if (lblPlanTongChiPhi != null) lblPlanTongChiPhi.setText("--");
            if (lblPlanBottomCost != null) lblPlanBottomCost.setText("Tổng chi phí: --");
        }
        if (lblPlanTongDichVu != null) lblPlanTongDichVu.setText(String.valueOf(serviceCount));
    }

    private void updatePlanTotalsFromServiceTable() {
        double totalCost = 0;
        for (int i = 0; i < planServiceModel.getRowCount(); i++) {
            try {
                totalCost += Double.parseDouble(planServiceModel.getValueAt(i, 4).toString());
            } catch (Exception ignored) {}
        }
        if (planServiceModel.getRowCount() > 0) {
            if (lblPlanTongChiPhi != null) lblPlanTongChiPhi.setText(String.format("%,.0f đ", totalCost));
            if (lblPlanBottomCost != null) lblPlanBottomCost.setText(String.format("Tổng chi phí: %,.0f đ", totalCost));
        } else {
            if (lblPlanTongChiPhi != null) lblPlanTongChiPhi.setText("--");
            if (lblPlanBottomCost != null) lblPlanBottomCost.setText("Tổng chi phí: --");
        }
        if (lblPlanTongDichVu != null) lblPlanTongDichVu.setText(String.valueOf(planServiceModel.getRowCount()));
    }

    private double getPriceByServiceName(String serviceName) {
        String sql = "SELECT donGia FROM DichVu WHERE tenDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("donGia");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 150000.0;
    }

    private JPanel createPlanRow(JPanel left, JPanel right, double leftRatio, double rightRatio, int height) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 12);

        gbc.gridx = 0; gbc.weightx = leftRatio;
        row.add(left, gbc);

        gbc.gridx = 1; gbc.weightx = rightRatio; gbc.insets = new Insets(0, 0, 0, 0);
        row.add(right, gbc);

        row.setPreferredSize(new Dimension(1000, height));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setMinimumSize(new Dimension(0, height));
        return row;
    }

    private JLabel planText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(NAVY);
        return label;
    }
}