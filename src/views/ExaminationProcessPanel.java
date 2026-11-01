package views;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.util.List;
import models.dao.LichHenDAO;

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
    
    // Khai báo nhãn tổng số bệnh nhân toàn cục để cập nhật an toàn không sợ lỗi ArrayIndexOutOfBoundsException
    private JLabel lblTotalPatients;

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

        add(cardPanel, BorderLayout.CENTER);
        showReception();
        
        loadReceptionData();
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
        loadReceptionData();
    }

    private void showExamination() {
        cardLayout.show(cardPanel, "EXAMINATION");
        btnExamination.setBackground(BLUE);
        btnExamination.setForeground(Color.WHITE);
        btnReception.setBackground(BG);
        btnReception.setForeground(NAVY);
    }
    
    // ==================== TRANG DANH SÁCH TIẾP NHẬN ====================
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
        JLabel subtitle = new JLabel("Quản lý tiếp nhận bệnh nhân đã xác nhận lịch hẹn và chuyển sang khám bệnh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filter.setOpaque(false);
        JTextField txtDate = new JTextField(10);
        JTextField txtSearch = new JTextField(18);
        styleTextField(txtDate);
        styleTextField(txtSearch);
        txtSearch.setToolTipText("Tìm mã BN, tên hoặc SĐT");
        JButton btnSearch = createButton("⌕ Tìm kiếm", BLUE);
        JButton btnReload = createButton("Tải lại", MUTED);
        
        btnReload.addActionListener(e -> {
            txtSearch.setText("");
            txtDate.setText("");
            loadReceptionData();
        });
        
        btnSearch.addActionListener(e -> {
            performReceptionSearch(txtSearch.getText().trim(), txtDate.getText().trim());
        });

        filter.add(new JLabel("Ngày khám:"));
        filter.add(txtDate);
        filter.add(new JLabel("Từ khóa:"));
        filter.add(txtSearch);
        filter.add(btnSearch);
        filter.add(btnReload);
        header.add(filter, BorderLayout.EAST);
        main.add(header, BorderLayout.NORTH);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(0, 12));
        tableCard.setBorder(new EmptyBorder(15, 15, 12, 15));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        status.setOpaque(false);
        JButton btnChoKham = createButton("Chờ khám", PINK);
        JButton btnDangKham = createButton("Đang khám", BLUE);
        JButton btnDaKham = createButton("Đã khám", MUTED);
        status.add(btnChoKham);
        status.add(btnDangKham);
        status.add(btnDaKham);
        
        btnChoKham.addActionListener(e -> loadReceptionDataByStatus("Đã xác nhận"));
        btnDangKham.addActionListener(e -> loadReceptionDataByStatus("Đang khám"));
        btnDaKham.addActionListener(e -> loadReceptionDataByStatus("Hoàn thành"));

        String[] columns = {"STT", "Mã LH", "Ngày hẹn", "Giờ", "Họ và tên", "SĐT", "Dịch vụ dự kiến", "Bác sĩ phụ trách", "Trạng thái", "Thao tác"};
        receptionModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 9; }
        };

        tblReception = new JTable(receptionModel);
        styleTable(tblReception);
        tblReception.setRowHeight(43);
        tblReception.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tblReception.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer("Khám bệnh"));
        tblReception.getColumnModel().getColumn(9).setCellEditor(new ActionEditor(tblReception, row -> {
            processTransferToExamination(row);
        }, "Khám bệnh"));

        int[] widths = {40, 75, 85, 55, 140, 90, 130, 110, 95, 85};
        for (int i = 0; i < widths.length; i++) {
            tblReception.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(tblReception);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        
        lblTotalPatients = new JLabel("Tổng số bệnh nhân: 0");
        lblTotalPatients.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTotalPatients.setForeground(MUTED);

        JButton btnStart = createButton("Chuyển sang Khám bệnh →", PINK);
        btnStart.addActionListener(e -> {
            int row = tblReception.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân trong danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            processTransferToExamination(tblReception.convertRowIndexToModel(row));
        });

        footer.add(lblTotalPatients, BorderLayout.WEST);
        footer.add(btnStart, BorderLayout.EAST);
        tableCard.add(footer, BorderLayout.SOUTH);
        main.add(tableCard, BorderLayout.CENTER);

        return main;
    }

    private void processTransferToExamination(int row) {
        if (row < 0 || row >= receptionModel.getRowCount()) return;
        
        String maLichHen = String.valueOf(receptionModel.getValueAt(row, 1));
        
        boolean updated = lichHenDAO.updateStatus(maLichHen, "Đang khám");
        if (updated) {
            loadPatient(row);
            showExamination();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái lịch hẹn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadReceptionData() {
        loadReceptionDataByStatus("Đã xác nhận");
    }

    private void loadReceptionDataByStatus(String statusFilter) {
        if (receptionModel == null) return; // Tránh lỗi null khi khởi tạo
        receptionModel.setRowCount(0);
        try {
            List<Object[]> allAppointments = lichHenDAO.getAllAppointments();
            int stt = 1;
            for (Object[] row : allAppointments) {
                String status = String.valueOf(row[7]);
                if (status.equalsIgnoreCase(statusFilter)) {
                    String maLichHen = String.valueOf(row[0]);
                    String tenKH = String.valueOf(row[1]);
                    String sdt = String.valueOf(row[2]);
                    String dichVu = String.valueOf(row[3]);
                    String bacSi = String.valueOf(row[4]);
                    String ngayHen = String.valueOf(row[5]);
                    String gioKham = String.valueOf(row[6]);

                    receptionModel.addRow(new Object[]{
                        stt++,
                        maLichHen,
                        ngayHen,
                        gioKham,
                        tenKH,
                        sdt,
                        dichVu,
                        bacSi.isEmpty() ? "Chưa phân công" : bacSi,
                        status,
                        "Khám bệnh"
                    });
                }
            }
            updateReceptionTotalCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performReceptionSearch(String keyword, String dateFilter) {
        if (receptionModel == null) return;
        receptionModel.setRowCount(0);
        try {
            List<Object[]> allAppointments = lichHenDAO.getAllAppointments();
            int stt = 1;
            String query = keyword.toLowerCase();
            for (Object[] row : allAppointments) {
                String maLichHen = String.valueOf(row[0]);
                String tenKH = String.valueOf(row[1]);
                String sdt = String.valueOf(row[2]);
                String ngayHen = String.valueOf(row[5]);
                String status = String.valueOf(row[7]);

                boolean matchKeyword = query.isEmpty() || 
                        maLichHen.toLowerCase().contains(query) || 
                        tenKH.toLowerCase().contains(query) || 
                        sdt.contains(query);
                
                boolean matchDate = dateFilter.isEmpty() || ngayHen.contains(dateFilter);

                if (matchKeyword && matchDate && (status.equalsIgnoreCase("Đã xác nhận") || status.equalsIgnoreCase("Đang khám"))) {
                    receptionModel.addRow(new Object[]{
                        stt++,
                        maLichHen,
                        ngayHen,
                        String.valueOf(row[6]),
                        tenKH,
                        sdt,
                        String.valueOf(row[3]),
                        String.valueOf(row[4]),
                        status,
                        "Khám bệnh"
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

    // ==================== TRANG KHÁM VÀ CHỈ ĐỊNH ====================
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
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(15, 0));
        panel.setBorder(new EmptyBorder(12, 30, 12, 30));

        lblPatientCode = createValue("--");
        lblPatientName = createValue("--");
        lblAge = createValue("--");
        lblGender = createValue("--");
        lblPhone = createValue("--");
        lblDate = createValue("--");
        lblTime = createValue("--");

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 35, 0));
        infoPanel.setOpaque(false);

        infoPanel.add(infoItem("Mã lịch hẹn", lblPatientCode));
        infoPanel.add(infoItem("Họ tên", lblPatientName));
        infoPanel.add(infoItem("Tuổi", lblAge));
        infoPanel.add(infoItem("Giới tính", lblGender));
        infoPanel.add(infoItem("Số điện thoại", lblPhone));
        infoPanel.add(infoItem("Ngày khám", lblDate));
        infoPanel.add(infoItem("Giờ khám", lblTime));

        JButton btnDetail = new JButton("ⓘ Chi tiết");
        btnDetail.setFont(new Font("Arial", Font.BOLD, 13));
        btnDetail.setForeground(NAVY);
        btnDetail.setBackground(Color.WHITE);
        btnDetail.setFocusPainted(false);
        btnDetail.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetail.setBorder(BorderFactory.createLineBorder(new Color(224, 233, 243)));
        btnDetail.setPreferredSize(new Dimension(110, 34));

        btnDetail.addActionListener(e -> {
            JFrame frame = new JFrame("Hồ sơ bệnh nhân");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(900, 650);
            frame.setLocationRelativeTo(this);
            frame.setVisible(true);
        });

        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(btnDetail, BorderLayout.EAST);

        return panel;
    }

    private JPanel infoItem(String title, JLabel value) {
        JPanel item = new JPanel(new GridLayout(2, 10, 0, 5));
        item.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
        lblTitle.setForeground(new Color(15, 29, 69));

        value.setFont(new Font("Arial", Font.PLAIN, 13));
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
            "Bình thường", "Sâu răng", "Viêm nướu", "Mẻ răng", "Mất răng", "Đã điều trị", "Viêm tủy", "Mòn cổ răng", "Răng khôn mọc lệch"
        });
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

        JButton btnSave = createButton("▣  Lưu thông tin răng", BLUE);
        btnSave.setPreferredSize(new Dimension(200, 42));
        btnSave.addActionListener(e -> saveExaminedTooth());

        panel.add(bodyScroll, BorderLayout.CENTER);
        panel.add(btnSave, BorderLayout.SOUTH);

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
            indicationModel.removeRow(row);
            updateSTT(indicationModel);
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
        
        tblExamined.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer("Xóa"));
        tblExamined.getColumnModel().getColumn(7).setCellEditor(new ActionEditor(tblExamined, row -> examinedModel.removeRow(row), "Xóa"));
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
        btnSave.addActionListener(e -> saveMedicalSessionToDatabase());
        
        footer.add(btnPrint);
        footer.add(btnSave);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private void saveMedicalSessionToDatabase() {
        String maLichHen = lblPatientCode.getText().trim(); 
        
        if (maLichHen.equals("--") || maLichHen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa chọn thông tin bệnh nhân!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        models.dao.ChiTietKhamRangDAO rangDAO = new models.dao.ChiTietKhamRangDAO();
        models.dao.ChiTietChiDinhDAO chiDinhDAO = new models.dao.ChiTietChiDinhDAO();

        try {
            for (int i = 0; i < examinedModel.getRowCount(); i++) {
                models.entity.ChiTietKhamRang ct = new models.entity.ChiTietKhamRang();
                ct.setMaBenhAn(maLichHen);
                ct.setMaRang(Integer.parseInt(examinedModel.getValueAt(i, 0).toString())); 
                ct.setTinhTrang(examinedModel.getValueAt(i, 2).toString());            
                ct.setMucDo(examinedModel.getValueAt(i, 3).toString());                  
                ct.setTrieuChung(examinedModel.getValueAt(i, 4).toString());             
                ct.setChanDoan(examinedModel.getValueAt(i, 5).toString());               
                ct.setGhiChu(examinedModel.getValueAt(i, 6).toString());                 
                rangDAO.insert(ct);
            }

            for (int i = 0; i < indicationModel.getRowCount(); i++) {
                models.entity.ChiTietChiDinh ctd = new models.entity.ChiTietChiDinh();
                ctd.setMaBenhAn(maLichHen);
                Object val = indicationModel.getValueAt(i, 1);
                ctd.setMaDichVu(val instanceof ServiceItem ? ((ServiceItem)val).getMaDV() : val.toString()); 
                
                if (selectedTooth != null) {
                    ctd.setMaRang(selectedTooth.code);
                } else {
                    ctd.setMaRang(null);
                }
                
                ctd.setSoLuong(Integer.parseInt(indicationModel.getValueAt(i, 2).toString()));
                ctd.setDonVi(indicationModel.getValueAt(i, 3).toString());
                ctd.setGhiChu(indicationModel.getValueAt(i, 4).toString());
                chiDinhDAO.insert(ctd);
            }

            lichHenDAO.updateStatus(maLichHen, "Hoàn thành");
            JOptionPane.showMessageDialog(this, "Đã lưu thông tin khám bệnh thành công!");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu vào CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToothSelected(StandardToothChartPanel.ToothData tooth) {
        selectedTooth = tooth;
        lblToothCode.setText(String.valueOf(tooth.code));
        lblToothName.setText(tooth.name);
        lblJaw.setText(tooth.jaw);
        lblPosition.setText(tooth.position);
        lblType.setText(tooth.type);

        lblToothHeaderCode.setText(String.valueOf(tooth.code));
        lblToothHeaderName.setText(tooth.name);
        lblToothHeaderType.setText(tooth.type);

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
        
        String tinhTrang = cboCondition.getSelectedItem().toString();
        String mucDo = cboSeverity.getSelectedItem().toString();
        String trieuChung = txtSymptom.getText().trim();
        String chanDoan = txtDiagnosis.getText().trim();
        String ghiChu = txtNote.getText().trim();

        for (int i = 0; i < examinedModel.getRowCount(); i++) {
            if (Integer.parseInt(examinedModel.getValueAt(i, 0).toString()) == code) {
                examinedModel.setValueAt(selectedTooth.name, i, 1);
                examinedModel.setValueAt(tinhTrang, i, 2);
                examinedModel.setValueAt(mucDo, i, 3);
                examinedModel.setValueAt(trieuChung, i, 4);
                examinedModel.setValueAt(chanDoan, i, 5);
                examinedModel.setValueAt(ghiChu, i, 6);
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin răng!");
                return;
            }
        }
        
        examinedModel.addRow(new Object[]{code, selectedTooth.name, tinhTrang, mucDo, trieuChung, chanDoan, ghiChu, "Xóa"});
        JOptionPane.showMessageDialog(this, "Đã thêm thông tin răng!");
    }

    private static class ServiceItem {
        private String maDV;
        private String tenDV;
        public ServiceItem(String maDV, String tenDV) { this.maDV = maDV; this.tenDV = tenDV; }
        public String getMaDV() { return maDV; }
        @Override public String toString() { return tenDV; }
    }

<<<<<<< HEAD
   
=======
    private void addIndication() {
        String autoMaDV = "DV" + System.currentTimeMillis() % 100000;
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git

<<<<<<< HEAD
private void addIndication() {
=======
        JTextField txtMaDV = new JTextField(autoMaDV, 15);
        txtMaDV.setEditable(false);
        
        JTextField txtService = new JTextField(15);
        JTextField txtQuantity = new JTextField("1", 15);
        JComboBox<String> cboUnit = new JComboBox<>(new String[]{"Lần", "Răng", "Chiếc", "Bộ", "Hàm", "Liều"});
        cboUnit.setEditable(true);
        JTextField txtDescription = new JTextField(15);
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git

<<<<<<< HEAD
    String autoMaDV = "DV" + (System.currentTimeMillis() % 100000);
=======
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        form.add(new JLabel("Mã dịch vụ:")); form.add(txtMaDV);
        form.add(new JLabel("Tên dịch vụ:")); form.add(txtService);
        form.add(new JLabel("Số lượng:")); form.add(txtQuantity);
        form.add(new JLabel("Đơn vị:")); form.add(cboUnit); 
        form.add(new JLabel("Ghi chú:")); form.add(txtDescription);
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git

    Color NAVY = new Color(15, 29, 69);
    Color PINK_BG = new Color(255, 240, 245);
    Color LIGHT_BLUE = new Color(231, 241, 255);
    Color BORDER = new Color(190, 205, 230);

    JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "Thêm chỉ định điều trị",
            Dialog.ModalityType.APPLICATION_MODAL
    );

    dialog.setUndecorated(true);
    dialog.setSize(680, 510);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(PINK_BG);
    root.setBorder(BorderFactory.createLineBorder(NAVY, 1));

    // ================= HEADER =================
    JPanel header = new JPanel(new BorderLayout(15, 0));
    header.setBackground(NAVY);
    header.setBorder(new EmptyBorder(12, 20, 12, 12));
    header.setPreferredSize(new Dimension(680, 75));

    // Icon lấy từ thư mục img
    JLabel lblIcon = new JLabel();
    lblIcon.setPreferredSize(new Dimension(48, 48));

    java.net.URL iconURL = getClass().getResource("/img/logo4.png");

    if (iconURL != null) {
        ImageIcon original = new ImageIcon(iconURL);
        Image scaled = original.getImage().getScaledInstance(
                50, 50, Image.SCALE_SMOOTH);
        lblIcon.setIcon(new ImageIcon(scaled));
    } else {
        lblIcon.setText("✚");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblIcon.setForeground(Color.WHITE);
    }

    JLabel lblTitle = new JLabel("Thêm chỉ định điều trị");
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
    lblTitle.setForeground(Color.WHITE);

    JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    titleArea.setOpaque(false);
    titleArea.add(lblIcon);
    titleArea.add(lblTitle);

    ImageIcon iconClose = new ImageIcon(
    	    new ImageIcon(getClass().getResource("/img/close.png"))
    	        .getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)
    	);

    	JButton btnClose = new JButton(iconClose);
    	btnClose.setPreferredSize(new Dimension(42, 42));
    	btnClose.setBorderPainted(false);
    	btnClose.setContentAreaFilled(false);
    	btnClose.setFocusPainted(false);
    	btnClose.addActionListener(e -> dialog.dispose());
    	
    	
    styleIndicationButton(btnClose, NAVY, Color.WHITE);
    btnClose.setFont(new Font("Segoe UI", Font.BOLD, 28));
    btnClose.setPreferredSize(new Dimension(42, 42));
    btnClose.setBorderPainted(false);
    btnClose.addActionListener(e -> dialog.dispose());

    header.add(titleArea, BorderLayout.WEST);
    header.add(btnClose, BorderLayout.EAST);

    // ================= FORM =================
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(PINK_BG);
    form.setBorder(new EmptyBorder(25, 25, 25, 25));

    JTextField txtMaDV = createIndicationField();
    txtMaDV.setText(autoMaDV);
    txtMaDV.setEditable(false);
    txtMaDV.setBackground(LIGHT_BLUE);
    txtMaDV.setForeground(new Color(25, 75, 145));

    JTextField txtService = createIndicationField();

    JTextField txtQuantity = createIndicationField();
    txtQuantity.setText("1");

    JTextField txtUnit = createIndicationField();
    txtUnit.setText("Lần");

    JTextArea txtDescription = new JTextArea(3, 20);
    txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    txtDescription.setForeground(NAVY);
    txtDescription.setBackground(Color.WHITE);
    txtDescription.setLineWrap(true);
    txtDescription.setWrapStyleWord(true);
    txtDescription.setBorder(new EmptyBorder(9, 12, 9, 12));

    JScrollPane noteScroll = new JScrollPane(txtDescription);
    noteScroll.setBorder(BorderFactory.createLineBorder(BORDER));
    noteScroll.setPreferredSize(new Dimension(390, 75));

    addIndicationRow(form, 0, "▥", "Mã chỉ định :", txtMaDV);
    addIndicationRow(form, 1, "♧", "Tên dịch vụ :", txtService);
    addIndicationRow(form, 2, "▱", "Số lượng :", txtQuantity);
    addIndicationRow(form, 3, "≡", "Đơn vị :", txtUnit);
    addIndicationRow(form, 4, "✎", "Ghi chú :", noteScroll);

    // ================= FOOTER =================
    JPanel footer = new JPanel(new FlowLayout(
            FlowLayout.CENTER, 22, 18));
    footer.setBackground(PINK_BG);
    footer.setBorder(new MatteBorder(1, 0, 0, 0,
            new Color(230, 205, 218)));

    JButton btnOK = new JButton("✓   OK");
    btnOK.setPreferredSize(new Dimension(160, 48));
    styleIndicationButton(btnOK, NAVY, Color.WHITE);

    JButton btnCancel = new JButton("×   Cancel");
    btnCancel.setPreferredSize(new Dimension(160, 48));
    styleIndicationButton(btnCancel, Color.WHITE, NAVY);
    btnCancel.setBorder(BorderFactory.createLineBorder(NAVY, 1));

    footer.add(btnOK);
    footer.add(btnCancel);

    // ================= SỰ KIỆN =================
    btnCancel.addActionListener(e -> dialog.dispose());

    btnOK.addActionListener(e -> {

        String maDV = txtMaDV.getText().trim();
        String tenDV = txtService.getText().trim();
        String soLuong = txtQuantity.getText().trim();
        String donVi = txtUnit.getText().trim();
        String ghiChu = txtDescription.getText().trim();

        if (tenDV.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Vui lòng nhập tên dịch vụ!");
            txtService.requestFocus();
            return;
        }

        if (donVi.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Vui lòng nhập đơn vị!");
            txtUnit.requestFocus();
            return;
        }

        try {
            int quantity = Integer.parseInt(soLuong);

            if (quantity <= 0) {
                throw new NumberFormatException();
            }

            ServiceItem item = new ServiceItem(maDV, tenDV);

            indicationModel.addRow(new Object[]{
                indicationModel.getRowCount() + 1,
                item,
                quantity,
<<<<<<< HEAD
                donVi,
                ghiChu,
=======
                cboUnit.getSelectedItem().toString().trim(), 
                txtDescription.getText().trim(),
>>>>>>> branch 'master' of https://github.com/Nhom12PTUDHTQLPKNK/HTQLPKNK.git
                "Xóa"
            });

            dialog.dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog,
                    "Số lượng phải là số nguyên dương!");
            txtQuantity.requestFocus();
        }
    });

    root.add(header, BorderLayout.NORTH);
    root.add(form, BorderLayout.CENTER);
    root.add(footer, BorderLayout.SOUTH);

    dialog.add(root);
    dialog.setVisible(true);
}


private JTextField createIndicationField() {

    JTextField field = new JTextField(18);

    field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    field.setForeground(new Color(15, 29, 69));
    field.setBackground(Color.WHITE);

    field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    new Color(190, 205, 230), 1),
            new EmptyBorder(9, 12, 9, 12)
    ));

    field.setPreferredSize(new Dimension(390, 43));

    return field;
}



private void addIndicationRow(
        JPanel form,
        int row,
        String icon,
        String title,
        JComponent component) {

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = row;
    gbc.insets = new Insets(10, 5, 10, 12);
    gbc.anchor = GridBagConstraints.CENTER;

    // ICON
    JLabel lblIcon = new JLabel(icon);
    lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 28));
    lblIcon.setForeground(new Color(15, 29, 69));
    lblIcon.setPreferredSize(new Dimension(42, 42));
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    form.add(lblIcon, gbc);

    // LABEL
    JLabel lblTitle = new JLabel(title);
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
    lblTitle.setForeground(new Color(15, 29, 69));
    lblTitle.setPreferredSize(new Dimension(145, 35));

    gbc.gridx = 1;
    form.add(lblTitle, gbc);

    // INPUT
    gbc.gridx = 2;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 5, 10, 5);

    form.add(component, gbc);
}


private void styleIndicationButton(
        JButton button,
        Color background,
        Color foreground) {

    button.setUI(new BasicButtonUI());

    button.setFont(new Font("Segoe UI", Font.BOLD, 16));
    button.setBackground(background);
    button.setForeground(foreground);

    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(true);
    button.setFocusPainted(false);

    button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    background.equals(Color.WHITE)
                            ? new Color(15, 29, 69)
                            : background, 1),
            new EmptyBorder(8, 15, 8, 15)
    ));

    button.setCursor(Cursor.getPredefinedCursor(
            Cursor.HAND_CURSOR));
}



    private void loadPatient(int row) {
        if (row < 0 || row >= receptionModel.getRowCount()) return;
        lblPatientCode.setText(String.valueOf(receptionModel.getValueAt(row, 1))); // Mã lịch hẹn
        lblDate.setText(String.valueOf(receptionModel.getValueAt(row, 2)));       // Ngày hẹn
        lblTime.setText(String.valueOf(receptionModel.getValueAt(row, 3)));       // Giờ hẹn
        lblPatientName.setText(String.valueOf(receptionModel.getValueAt(row, 4)));// Họ tên
        lblPhone.setText(String.valueOf(receptionModel.getValueAt(row, 5)));      // SĐT
        lblAge.setText("--");
        lblGender.setText("--");
        
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
        private int selectedRow;

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