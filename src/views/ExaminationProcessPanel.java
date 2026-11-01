package views;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.plaf.basic.BasicButtonUI;
import views.MedicalRecordPanel;

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
    private JButton btnReception, btnExamination;

    private JTable tblReception, tblIndication, tblExamined;
    private DefaultTableModel receptionModel, indicationModel, examinedModel;

    private JLabel lblPatientCode, lblPatientName, lblAge, lblGender, lblPhone, lblDate;
    private JLabel lblToothCode, lblToothName, lblJaw, lblPosition, lblType;
    private JComboBox<String> cboCondition, cboSeverity;
    private JTextArea txtSymptom, txtDiagnosis, txtNote;
    private StandardToothChartPanel toothSource;
    private StandardToothChartPanel.ToothData selectedTooth;
	private JLabel lblTime;
	
	
	private JLabel lblToothHeaderCode;
	private JLabel lblToothHeaderName;
	private JLabel lblToothHeaderType;

    public ExaminationProcessPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(createMenu(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);
        cardPanel.add(createReceptionPage(), "RECEPTION");
        cardPanel.add(createExaminationPage(), "EXAMINATION");

        add(cardPanel, BorderLayout.CENTER);
        showReception();
    }

    private JPanel createMenu() {
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        menu.setBackground(Color.WHITE);
        menu.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        btnReception = createButton("☷  Danh sách tiếp nhận", PINK);
        btnExamination = createButton("✚  Khám và chỉ định", BG);
        btnExamination.setForeground(NAVY);

        btnReception.addActionListener(e -> showReception());
        btnExamination.addActionListener(e -> showExamination());

        menu.add(btnReception);
        menu.add(btnExamination);
        return menu;
    }

    private void showReception() {
        cardLayout.show(cardPanel, "RECEPTION");
        btnReception.setBackground(PINK);
        btnReception.setForeground(Color.WHITE);
        btnExamination.setBackground(BG);
        btnExamination.setForeground(NAVY);
    }

    private void showExamination() {
        cardLayout.show(cardPanel, "EXAMINATION");
        btnExamination.setBackground(BLUE);
        btnExamination.setForeground(Color.WHITE);
        btnReception.setBackground(BG);
        btnReception.setForeground(NAVY);
    }
    
    //TAG 1 Ở ĐÂY NÈ ÁNH -><-

    private JPanel createReceptionPage() {
        JPanel main = new JPanel(new BorderLayout(10, 12));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = createTitle("Danh sách bệnh nhân chờ khám");
        JLabel subtitle = new JLabel("Quản lý tiếp nhận bệnh nhân và chuyển sang khám bệnh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filter.setOpaque(false);
        JTextField txtDate = new JTextField(11);
        JTextField txtSearch = new JTextField(20);
        styleTextField(txtDate);
        styleTextField(txtSearch);
        txtSearch.setToolTipText("Tìm mã bệnh nhân hoặc họ tên");
        JButton btnSearch = createButton("⌕ Tìm kiếm", BLUE);
        filter.add(new JLabel("Ngày khám:"));
        filter.add(txtDate);
        filter.add(txtSearch);
        filter.add(btnSearch);
        header.add(filter, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(15, 15, 12, 15));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        status.setOpaque(false);
        status.add(createButton("Chờ khám", PINK));
        status.add(createButton("Đang khám", BLUE));
        status.add(createButton("Đã khám", MUTED));
        tableCard.add(status, BorderLayout.NORTH);

        String[] columns = {"STT", "Giờ hẹn", "Mã BN", "Họ và tên", "Tuổi", "Giới tính", "Dịch vụ dự kiến", "Trạng thái", "Thao tác"};
        receptionModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 8; }
        };

        tblReception = new JTable(receptionModel);
        styleTable(tblReception);
        tblReception.setRowHeight(43);
        tblReception.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblReception.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer("Khám"));
        tblReception.getColumnModel().getColumn(8).setCellEditor(new ActionEditor(tblReception, row -> {
            loadPatient(row);
            showExamination();
        }));

        int[] widths = {45, 75, 85, 145, 50, 65, 150, 100, 85};
        for (int i = 0; i < widths.length; i++) {
            tblReception.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(tblReception);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel total = new JLabel("Tổng số bệnh nhân: 0");
        total.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        total.setForeground(MUTED);

        JButton btnStart = createButton("Khám bệnh →", PINK);
        btnStart.addActionListener(e -> {
            int row = tblReception.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            loadPatient(tblReception.convertRowIndexToModel(row));
            showExamination();
        });

        footer.add(total, BorderLayout.WEST);
        footer.add(btnStart, BorderLayout.EAST);
        tableCard.add(footer, BorderLayout.SOUTH);
        main.add(tableCard, BorderLayout.CENTER);

        return main;
    }

    
    
    
    
    
    
    
    
    
    //tag2 
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

        // CARD 1: Sơ đồ răng - 30%
        g.gridx = 0;
        g.weightx = 0.30;
        toothSource = new StandardToothChartPanel();
        content.add(toothSource.createClinicalChartCard(this::onToothSelected), g);

        // CARD 2: Thông tin răng - 30%
        g.gridx = 1;
        g.weightx = 0.30;
        content.add(createToothForm(), g);

        // CARD 3: Chỉ định điều trị - 40%
        g.gridx = 2;
        g.weightx = 0.40;
        g.insets = new Insets(0, 0, 0, 0);
        content.add(createRightPanel(), g);

        main.add(content, BorderLayout.CENTER);

        return main;
    }

    private JPanel createPatientHeader() {

        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBorder(new EmptyBorder(12, 30, 12, 30));

        // Khởi tạo thông tin bệnh nhân
        lblPatientCode = createValue("--");
        lblPatientName = createValue("--");
        lblAge = createValue("--");
        lblGender = createValue("--");
        lblPhone = createValue("--");
        lblDate = createValue("--");
        lblTime = createValue("--");

        // Khu vực thông tin bên trái
        JPanel infoPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 45, 0
        ));
        infoPanel.setOpaque(false);

        infoPanel.add(infoItem("Mã bệnh nhân", lblPatientCode));
        infoPanel.add(infoItem("Họ tên", lblPatientName));
        infoPanel.add(infoItem("Tuổi", lblAge));
        infoPanel.add(infoItem("Giới tính", lblGender));
        infoPanel.add(infoItem("Số điện thoại", lblPhone));
        infoPanel.add(infoItem("Ngày khám", lblDate));
        infoPanel.add(infoItem("Giờ khám", lblTime));

        // Nút xem hồ sơ bệnh nhân
        JButton btnDetail = new JButton("ⓘ Chi tiết");
        btnDetail.setFont(new Font("Arial", Font.BOLD, 13));
        btnDetail.setForeground(NAVY);
        btnDetail.setBackground(Color.WHITE);
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.setBorder(
                BorderFactory.createLineBorder(new Color(224, 233, 243))
        );
        btnDetail.setPreferredSize(new Dimension(110, 34));

        btnDetail.addActionListener(e -> {

            JFrame frame = new JFrame("Hồ sơ bệnh nhân");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(900, 650);
            frame.setLocationRelativeTo(this);

            // Sau này thêm giao diện và dữ liệu hồ sơ bệnh nhân tại đây

            frame.setVisible(true);
        });

        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(btnDetail, BorderLayout.EAST);

        return panel;
    }
    
    

    private JPanel infoItem(String title, JLabel value) {

        JPanel item = new JPanel(new GridLayout(2, 10, 0, 15));
        item.setOpaque(false);

        // Tiêu đề xanh navy đậm
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 29, 69));

        // Dữ liệu
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
    
    
    
    
    


private JPanel createToothForm() {

    JPanel panel = createCard();
    panel.setLayout(new BorderLayout(8, 10));
    panel.setBorder(new EmptyBorder(12, 12, 12, 12));

    // ================= TIÊU ĐỀ =================
    JLabel title = createTitle("Thông tin răng");
    title.setFont(new Font("Segoe UI", Font.BOLD, 25));

    panel.add(title, BorderLayout.NORTH);

    // ================= KHU VỰC NỘI DUNG =================
    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

    // ================= THÔNG TIN RĂNG ĐƯỢC CHỌN =================
    JPanel topInfoPanel = new JPanel(new BorderLayout(12, 0));
    topInfoPanel.setOpaque(false);
    topInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    topInfoPanel.setPreferredSize(new Dimension(400, 85));
    topInfoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

    // Hình răng
    JLabel lblToothImage;

    java.net.URL toothUrl = getClass().getResource("/img/iconrang.png");

    if (toothUrl != null) {
        ImageIcon icon = new ImageIcon(toothUrl);
        Image scaled = icon.getImage().getScaledInstance(
                90, 90, Image.SCALE_SMOOTH
        );
        lblToothImage = new JLabel(new ImageIcon(scaled));
    } else {
        lblToothImage = new JLabel("🦷");
        lblToothImage.setFont(
                new Font("Segoe UI Emoji", Font.PLAIN, 48)
        );
    }

    lblToothImage.setOpaque(true);
    lblToothImage.setBackground(new Color(232, 242, 255));
    lblToothImage.setPreferredSize(new Dimension(80, 80));
    lblToothImage.setHorizontalAlignment(SwingConstants.CENTER);

    // Thông tin phía bên phải hình răng
    JPanel namePanel = new JPanel();
    namePanel.setOpaque(false);
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

    JPanel codeRow = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 4, 0)
    );
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

    JPanel badgePanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 0, 0)
    );
    badgePanel.setBackground(new Color(228, 241, 255));
    badgePanel.setBorder(new EmptyBorder(5, 10, 5, 10));
    badgePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    lblToothHeaderType = new JLabel("--");
    lblToothHeaderType.setFont(
            new Font("Segoe UI", Font.BOLD, 16)
    );
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

    // ================= BẢNG THÔNG TIN CHI TIẾT =================
    JPanel attrGrid = new JPanel(new GridLayout(5, 2, 0, 0));
    attrGrid.setBackground(Color.WHITE);
    attrGrid.setBorder(
            BorderFactory.createLineBorder(BORDER)
    );
    attrGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

    lblToothCode = createValue("--");
    lblToothName = createValue("--");
    lblJaw = createValue("--");
    lblPosition = createValue("--");
    lblType = createValue("--");

    String[] attrNames = {
        "Mã răng",
        "Tên răng",
        "Hàm",
        "Vị trí",
        "Phân loại"
    };

    JLabel[] attrValues = {
        lblToothCode,
        lblToothName,
        lblJaw,
        lblPosition,
        lblType
    };

    for (int i = 0; i < attrNames.length; i++) {

        JPanel leftCell = new JPanel(new BorderLayout());
        leftCell.setBackground(Color.WHITE);
        leftCell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 10, 7, 10)
        ));

        JLabel lblName = new JLabel(attrNames[i]);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblName.setForeground(MUTED);

        leftCell.add(lblName, BorderLayout.CENTER);

        JPanel rightCell = new JPanel(new BorderLayout());
        rightCell.setBackground(Color.WHITE);
        rightCell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 10, 7, 10)
        ));

        attrValues[i].setFont(
                new Font("Segoe UI", Font.BOLD, 16)
        );
        attrValues[i].setForeground(NAVY);
        
        rightCell.add(attrValues[i], BorderLayout.CENTER);

        attrGrid.add(leftCell);
        attrGrid.add(rightCell);
    }

    attrGrid.setPreferredSize(new Dimension(450, 200));
    attrGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

    body.add(attrGrid);
    body.add(Box.createVerticalStrut(12));

   

 // ================= TÌNH TRẠNG RĂNG =================

    JSeparator separator = new JSeparator();
    separator.setForeground(BORDER);
    separator.setAlignmentX(Component.LEFT_ALIGNMENT);

    body.add(separator);
    body.add(Box.createVerticalStrut(15));

    // Tình trạng răng
    body.add(createFieldLabel("Tình trạng răng"));
    body.add(Box.createVerticalStrut(6));

    cboCondition = new JComboBox<>(new String[]{
        "Bình thường",
        "Sâu răng",
        "Viêm nướu",
        "Mẻ răng",
        "Mất răng",
        "Đã điều trị"
    });

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

    // Mức độ
    body.add(createFieldLabel("Mức độ"));
    body.add(Box.createVerticalStrut(6));

    cboSeverity = new JComboBox<>(new String[]{
        "Nhẹ",
        "Trung bình",
        "Nặng"
    });

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

    // Triệu chứng
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

    // Chẩn đoán
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

    // Ghi chú
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
    
    
    

 // ================= SCROLL NỘI DUNG =================

 JScrollPane bodyScroll = new JScrollPane(body);
 bodyScroll.setBorder(null);
 bodyScroll.setOpaque(false);
 bodyScroll.getViewport().setOpaque(false);
 bodyScroll.getVerticalScrollBar().setUnitIncrement(15);
 bodyScroll.setHorizontalScrollBarPolicy(
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
 );

 // ================= NÚT LƯU =================

 JButton btnSave = createButton(
         "▣  Lưu thông tin răng", BLUE
 );

 btnSave.setPreferredSize(new Dimension(200, 42));

 btnSave.addActionListener(e -> saveExaminedTooth());

 panel.add(bodyScroll, BorderLayout.CENTER);
 panel.add(btnSave, BorderLayout.SOUTH);

 return panel;
}


    
    

    private JPanel createDetailRow(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(5, 3));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(MUTED);
        row.add(label, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
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
            indicationModel.removeRow(row);
            updateSTT(indicationModel);
        }, "Xóa"));
        indicationPanel.add(new JScrollPane(tblIndication), BorderLayout.CENTER);

        JPanel examinedPanel = new JPanel(new BorderLayout(0, 8));
        examinedPanel.setOpaque(false);
        examinedPanel.add(createTitle("Danh sách răng đã khám"), BorderLayout.NORTH);

        String[] examinedColumns = {"Mã răng", "Tên răng", "Tình trạng", "Chẩn đoán", "Ghi chú", "Xóa"};
        examinedModel = new DefaultTableModel(examinedColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 5; }
        };
        tblExamined = new JTable(examinedModel);
        styleTable(tblExamined);
        tblExamined.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer("Xóa"));
        tblExamined.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(tblExamined, row -> examinedModel.removeRow(row), "Xóa"));
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
            try { tblExamined.print(); } 
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Không thể in phiếu khám!"); }
        });

        JButton btnSave = createButton("✓ Lưu phiếu khám", BLUE);
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Giao diện sẵn sàng kết nối SQL Server."));
        footer.add(btnPrint);
        footer.add(btnSave);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }


private void onToothSelected(StandardToothChartPanel.ToothData tooth) {

    selectedTooth = tooth;

    // Cập nhật bảng chi tiết
    lblToothCode.setText(String.valueOf(tooth.code));
    lblToothName.setText(tooth.name);
    lblJaw.setText(tooth.jaw);
    lblPosition.setText(tooth.position);
    lblType.setText(tooth.type);

    // Cập nhật phần thông tin phía trên
    lblToothHeaderCode.setText(String.valueOf(tooth.code));
    lblToothHeaderName.setText(tooth.name);
    lblToothHeaderType.setText(tooth.type);

    // Đưa các trường khám về mặc định
    cboCondition.setSelectedIndex(0);
    cboSeverity.setSelectedIndex(0);

    txtSymptom.setText("");
    txtDiagnosis.setText("");
    txtNote.setText("");
}



    private void saveExaminedTooth() {
        if (selectedTooth == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn răng trên sơ đồ FDI!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int code = selectedTooth.code;
        for (int i = 0; i < examinedModel.getRowCount(); i++) {
            if (Integer.parseInt(examinedModel.getValueAt(i, 0).toString()) == code) {
                examinedModel.setValueAt(selectedTooth.name, i, 1);
                examinedModel.setValueAt(cboCondition.getSelectedItem(), i, 2);
                examinedModel.setValueAt(txtDiagnosis.getText(), i, 3);
                examinedModel.setValueAt(txtNote.getText(), i, 4);
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin răng!");
                return;
            }
        }
        examinedModel.addRow(new Object[]{code, selectedTooth.name, cboCondition.getSelectedItem(), txtDiagnosis.getText(), txtNote.getText(), "Xóa"});
        JOptionPane.showMessageDialog(this, "Đã thêm thông tin răng!");
    }

    private void addIndication() {
        JTextField txtService = new JTextField();
        JTextField txtQuantity = new JTextField("1");
        JTextField txtUnit = new JTextField("Lần");
        JTextField txtDescription = new JTextField();

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.add(new JLabel("Dịch vụ / Chỉ định:")); form.add(txtService);
        form.add(new JLabel("Số lượng:")); form.add(txtQuantity);
        form.add(new JLabel("Đơn vị:")); form.add(txtUnit);
        form.add(new JLabel("Ghi chú:")); form.add(txtDescription);

        int result = JOptionPane.showConfirmDialog(this, form, "Thêm chỉ định điều trị", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;
        if (txtService.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập dịch vụ!");
            return;
        }
        try {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity <= 0) throw new NumberFormatException();
            indicationModel.addRow(new Object[]{indicationModel.getRowCount() + 1, txtService.getText().trim(), quantity, txtUnit.getText().trim(), txtDescription.getText().trim(), "Xóa"});
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên dương!");
        }
    }

    private void loadPatient(int row) {
        if (row < 0 || row >= receptionModel.getRowCount()) return;
        lblPatientCode.setText(String.valueOf(receptionModel.getValueAt(row, 2)));
        lblPatientName.setText(String.valueOf(receptionModel.getValueAt(row, 3)));
        lblAge.setText(String.valueOf(receptionModel.getValueAt(row, 4)));
        lblGender.setText(String.valueOf(receptionModel.getValueAt(row, 5)));
        lblPhone.setText("--");
        lblDate.setText(String.valueOf(receptionModel.getValueAt(row, 1)));
        examinedModel.setRowCount(0);
        indicationModel.setRowCount(0);
        selectedTooth = null;
    }

    private void updateSTT(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(i + 1, i, 0);
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

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        boolean isLight = color.equals(Color.WHITE)
                || color.equals(BG)
                || color.equals(new Color(238, 245, 253));

        Color textColor = isLight ? NAVY : Color.WHITE;

        // Sử dụng giao diện nút cơ bản để tránh FlatLaf ghi đè màu
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
                BorderFactory.createLineBorder(
                        isLight ? BORDER : color
                ),
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

    private class ActionRenderer extends JButton
    implements javax.swing.table.TableCellRenderer {

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
public Component getTableCellRendererComponent(
        JTable table, Object value,
        boolean selected, boolean focus,
        int row, int column) {

    return this;
}
}

    private class ActionEditor extends DefaultCellEditor {
        private final JButton button;
        private final JTable table;
        private final java.util.function.IntConsumer action;
        private int selectedRow;

        ActionEditor(JTable table, java.util.function.IntConsumer action) {
            this(table, action, "Khám");
        }

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
            
            
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setForeground(PINK);
            button.setBackground(new Color(255, 239, 245));
            button.setFocusPainted(false);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.putClientProperty("JButton.buttonType", "roundRect"); // Tương thích FlatLaf
            button.addActionListener(e -> {
                int row = selectedRow;
                fireEditingStopped();
                if (row >= 0) this.action.accept(row);
            });
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) {
            selectedRow = table.convertRowIndexToModel(row);
            return button;
        }

        @Override public Object getCellEditorValue() { return button.getText(); }
    }
} 
