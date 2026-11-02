package views;

import datepicker.DatePickerField;
import models.dao.LichLamViecDAO;
import models.entity.LichLamViec;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

public class WorkProcessPanel extends JPanel {
    private static final Color BLUE_PRIMARY = new Color(11, 95, 204);
    private static final Color GREEN_ACCENT = new Color(24, 117, 58);
    private static final Color ORANGE_ACCENT = new Color(210, 130, 0);
    private static final Color RED_ACCENT = new Color(180, 35, 45);
    private static final Color PINK_ACCENT = new Color(174, 35, 86);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private JTextField txtShiftId, txtStaffName;
    private DatePickerField txtDate; // Sử dụng DatePickerField thay cho JTextField
    private JComboBox<ShiftItem> cbShiftTime;
    private JButton btnCreate, btnUpdate, btnDelete, btnClear;
    private final LichLamViecDAO scheduleDAO = new LichLamViecDAO();

    private LocalDate currentWeekStart;
    private JLabel lblWeekTitle;
    private JPanel gridWeekPanel;
    private boolean isEditing = false;

    public static class ShiftItem {
        private String gioBatDau;
        private String gioKetThuc;

        public ShiftItem(String gioBatDau, String gioKetThuc) {
            this.gioBatDau = gioBatDau;
            this.gioKetThuc = gioKetThuc;
        }

        public String getGioBatDau() { return gioBatDau; }
        public String getGioKetThuc() { return gioKetThuc; }

        @Override
        public String toString() {
            return "Ca trực: " + gioBatDau + " - " + gioKetThuc;
        }
    }

    public WorkProcessPanel() {
        currentWeekStart = LocalDate.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        
        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Phân Ca Lịch Làm Việc (Trong Tuần)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(lblTitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        JPanel navWeekPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navWeekPanel.setOpaque(false);

        JButton btnPrev = createStaticButton("<", BLUE_PRIMARY, 40);
        btnPrev.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekHeader();
            renderWeekGrid();
        });

        lblWeekTitle = new JLabel();
        lblWeekTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblWeekTitle.setForeground(TEXT_DARK);
        updateWeekHeader();

        JButton btnNext = createStaticButton(">", BLUE_PRIMARY, 40);
        btnNext.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekHeader();
            renderWeekGrid();
        });

        JButton btnToday = createStaticButton("Tuần này", PINK_ACCENT, 95);
        btnToday.addActionListener(e -> {
            currentWeekStart = LocalDate.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
            updateWeekHeader();
            renderWeekGrid();
        });

        navWeekPanel.add(btnPrev);
        navWeekPanel.add(lblWeekTitle);
        navWeekPanel.add(btnNext);
        navWeekPanel.add(Box.createHorizontalStrut(15));
        navWeekPanel.add(btnToday);

        headerPanel.add(navWeekPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        JPanel weekContainerCard = createCardPanel();
        weekContainerCard.setLayout(new BorderLayout());
        weekContainerCard.setBorder(new EmptyBorder(10, 10, 10, 10));

        gridWeekPanel = new JPanel(new GridLayout(1, 7, 8, 0));
        gridWeekPanel.setOpaque(false);
        weekContainerCard.add(gridWeekPanel, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem(new Color(210, 235, 255), "Đã xếp lịch (Có thể sửa/xóa)"));
        legendPanel.add(createLegendItem(new Color(230, 230, 230), "Đã làm / Đang làm (Khóa CRUD)"));
        weekContainerCard.add(legendPanel, BorderLayout.SOUTH);

        centerContainer.add(weekContainerCard, BorderLayout.CENTER);
        centerContainer.add(createFormPanel(), BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
        renderWeekGrid();
        clearForm();
    }

    private JPanel createCardPanel() {
        return new JPanel() {
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
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);
        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_DARK);
        item.add(colorBox);
        item.add(lbl);
        return item;
    }

    private void updateWeekHeader() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblWeekTitle.setText("Tuần: " + currentWeekStart.format(fmt) + " - " + weekEnd.format(fmt));
    }

    private void renderWeekGrid() {
        gridWeekPanel.removeAll();

        List<LichLamViec> schedules = scheduleDAO.getAllLichLamViec();
        String[] weekDays = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            JPanel dayColumn = createDayColumn(weekDays[i], date, schedules);
            gridWeekPanel.add(dayColumn);
        }

        gridWeekPanel.revalidate();
        gridWeekPanel.repaint();
    }

    private JPanel createDayColumn(String dayName, LocalDate date, List<LichLamViec> allSchedules) {
        JPanel column = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LocalDate.now().equals(date) ? new Color(230, 242, 255) : new Color(248, 250, 253));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        column.setOpaque(false);
        column.setLayout(new BorderLayout());
        column.setBorder(new EmptyBorder(10, 8, 10, 8));

        JPanel colHeader = new JPanel();
        colHeader.setLayout(new BoxLayout(colHeader, BoxLayout.Y_AXIS));
        colHeader.setOpaque(false);

        JLabel lblName = new JLabel(dayName, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(MUTED);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNum = new JLabel(date.format(DateTimeFormatter.ofPattern("dd/MM")), SwingConstants.CENTER);
        lblNum.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNum.setForeground(LocalDate.now().equals(date) ? BLUE_PRIMARY : TEXT_DARK);
        lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        colHeader.add(lblName);
        colHeader.add(Box.createVerticalStrut(2));
        colHeader.add(lblNum);
        colHeader.add(Box.createVerticalStrut(8));
        column.add(colHeader, BorderLayout.NORTH);

        JPanel shiftsBox = new JPanel();
        shiftsBox.setLayout(new BoxLayout(shiftsBox, BoxLayout.Y_AXIS));
        shiftsBox.setOpaque(false);

        for (LichLamViec s : allSchedules) {
            if (s.getNgayLamViec() != null && s.getNgayLamViec().toLocalDate().equals(date)) {
                JComponent shiftChip = createShiftChip(s);
                shiftsBox.add(Box.createVerticalStrut(6));
                shiftsBox.add(shiftChip);
            }
        }

        JScrollPane shiftScroll = new JScrollPane(shiftsBox);
        shiftScroll.setBorder(null);
        shiftScroll.setOpaque(false);
        shiftScroll.getViewport().setOpaque(false);
        column.add(shiftScroll, BorderLayout.CENTER);

        column.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        column.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                LocalDate ngayMai = LocalDate.now().plusDays(1);
                if (!isEditing && (date.isEqual(ngayMai) || date.isAfter(ngayMai))) {
                    txtDate.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        return column;
    }

    private String[] getStaffInfo(String maNhanVien) {
        String tenNV = maNhanVien;
        String chucVu = "Nhân viên";
        
        String sql = "SELECT tenNhanVien, chucVu FROM NhanVien WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maNhanVien);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tenNV = rs.getString("tenNhanVien");
                    chucVu = rs.getString("chucVu");
                }
            }
        } catch (Exception ignored) {}
        return new String[]{tenNV, chucVu};
    }

    private JComponent createShiftChip(LichLamViec s) {
        JPanel chipPanel = new JPanel(new BorderLayout());
        chipPanel.setOpaque(true);
        chipPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        chipPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        
        String status = s.getTrangThai() != null ? s.getTrangThai() : "";
        if (status.equals("Đã làm") || status.equals("Đang làm")) {
            chipPanel.setBackground(new Color(230, 230, 230)); 
        } else {
            chipPanel.setBackground(new Color(210, 235, 255)); 
        }

        String[] staffInfo = getStaffInfo(s.getMaNhanVien());
        String hoTen = staffInfo[0];
        String chucVu = staffInfo[1];
        String thoiGian = s.getGioBatDau() + " - " + s.getGioKetThuc();

        JLabel lblInfo = new JLabel("<html><center><b>" + hoTen + "</b><br><font size='2' color='#333333'>" + chucVu + "</font><br><font size='2' color='#004080'><b>" + thoiGian + "</b></font><br><font size='2' color='#555555'>[" + status + "]</font></center></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (status.equals("Đã làm") || status.equals("Đang làm")) {
            lblInfo.setForeground(new Color(110, 110, 110));
        } else {
            lblInfo.setForeground(new Color(10, 80, 160));
        }
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        chipPanel.add(lblInfo, BorderLayout.CENTER);

        chipPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chipPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtShiftId.setText(s.getMaLichLamViec());
                txtShiftId.setEnabled(false);
                txtStaffName.setText(s.getMaNhanVien());
                
                for (int i = 0; i < cbShiftTime.getItemCount(); i++) {
                    ShiftItem item = cbShiftTime.getItemAt(i);
                    if (item.getGioBatDau().trim().equalsIgnoreCase(s.getGioBatDau().trim()) && 
                        item.getGioKetThuc().trim().equalsIgnoreCase(s.getGioKetThuc().trim())) {
                        cbShiftTime.setSelectedIndex(i);
                        break;
                    }
                }

                if (s.getNgayLamViec() != null) {
                    txtDate.setText(s.getNgayLamViec().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }

                isEditing = true;
                btnCreate.setEnabled(false);   

                if (status.equals("Đã làm") || status.equals("Đang làm")) {
                    txtStaffName.setEnabled(false);
                    cbShiftTime.setEnabled(false);
                    txtDate.setEditable(false);
                    btnUpdate.setEnabled(false);
                    btnDelete.setEnabled(false);
                } else {
                    txtStaffName.setEnabled(true);
                    cbShiftTime.setEnabled(true);
                    txtDate.setEditable(true);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                }
            }
        });

        return chipPanel;
    }

    private void loadShiftTimeComboBox(JComboBox<ShiftItem> comboBox) {
        comboBox.removeAllItems();
        boolean loadedFromDB = false;
        
        String sql = "SELECT DISTINCT gioBatDau, gioKetThuc FROM LichLamViec WHERE gioBatDau IS NOT NULL AND gioKetThuc IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String gbd = rs.getString("gioBatDau");
                String gkt = rs.getString("gioKetThuc");
                comboBox.addItem(new ShiftItem(gbd, gkt));
                loadedFromDB = true;
            }
        } catch (Exception ignored) {}

        if (!loadedFromDB) {
            comboBox.addItem(new ShiftItem("08:00", "12:00"));
            comboBox.addItem(new ShiftItem("13:30", "17:30"));
            comboBox.addItem(new ShiftItem("18:00", "21:00"));
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = createCardPanel();
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Quản lý phân công lịch");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        txtShiftId = addField(panel, "Mã phân ca (Tự động):");
        txtShiftId.setEnabled(false);

        txtStaffName = addField(panel, "Mã nhân viên:");

        JLabel lblShift = createFieldLabel("Chọn ca trực (Giờ BĐ - KT):");
        panel.add(lblShift);
        panel.add(Box.createVerticalStrut(3));
        
        cbShiftTime = new JComboBox<>();
        loadShiftTimeComboBox(cbShiftTime);
        cbShiftTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbShiftTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbShiftTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbShiftTime.setBackground(Color.WHITE);
        panel.add(cbShiftTime);
        panel.add(Box.createVerticalStrut(12));

        // Tích hợp DatePickerField vào form panel
        txtDate = addDatePickerField(panel, "Ngày trực (dd/MM/yyyy):");

        btnCreate = createStaticButton("Tạo lịch", GREEN_ACCENT, Integer.MAX_VALUE);
        btnCreate.addActionListener(e -> performCreate());
        panel.add(btnCreate);
        panel.add(Box.createVerticalStrut(8));

        btnUpdate = createStaticButton("Cập nhật", ORANGE_ACCENT, Integer.MAX_VALUE);
        btnUpdate.addActionListener(e -> performUpdate());
        panel.add(btnUpdate);
        panel.add(Box.createVerticalStrut(8));

        btnDelete = createStaticButton("Xóa lịch", RED_ACCENT, Integer.MAX_VALUE);
        btnDelete.addActionListener(e -> performDelete());
        panel.add(btnDelete);
        panel.add(Box.createVerticalStrut(8));

        btnClear = createStaticButton("Làm mới", PINK_ACCENT, Integer.MAX_VALUE);
        btnClear.addActionListener(e -> clearForm());
        panel.add(btnClear);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField addField(JPanel p, String labelText) {
        p.add(createFieldLabel(labelText));
        p.add(Box.createVerticalStrut(3));
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(3, 8, 3, 8)
        ));
        p.add(t);
        p.add(Box.createVerticalStrut(12));
        return t;
    }

    private DatePickerField addDatePickerField(JPanel p, String labelText) {
        p.add(createFieldLabel(labelText));
        p.add(Box.createVerticalStrut(3));
        DatePickerField dp = new DatePickerField();
        dp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        dp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(dp);
        p.add(Box.createVerticalStrut(12));
        return dp;
    }

    private void performCreate() {
        String id = txtShiftId.getText().trim();
        String employeeId = txtStaffName.getText().trim();
        ShiftItem selectedShift = (ShiftItem) cbShiftTime.getSelectedItem();
        String date = txtDate.getText().trim();

        if (employeeId.isEmpty() || date.isEmpty() || selectedShift == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin nhân viên và ngày trực!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate workDate = UiKit.parseDate(date);
            if (workDate == null) throw new DateTimeParseException("empty", date, 0);
            
            LocalDate ngayMai = LocalDate.now().plusDays(1);
            if (workDate.isBefore(ngayMai)) {
                JOptionPane.showMessageDialog(this, "Chỉ được phép xếp lịch từ ngày hôm sau trở đi (" + ngayMai.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " trở đi)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LichLamViec schedule = new LichLamViec(id, employeeId, java.sql.Date.valueOf(workDate),
                    selectedShift.getGioBatDau(), selectedShift.getGioKetThuc(), "Đã xếp lịch");
            
            boolean saved = scheduleDAO.insert(schedule);
            if (!saved) throw new SQLException("Không thể lưu lịch làm việc vào cơ sở dữ liệu.");
            
            clearForm();
            renderWeekGrid(); 
            JOptionPane.showMessageDialog(this, "Đã tạo lịch phân công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày trực phải đúng định dạng dd/MM/yyyy.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performUpdate() {
        String id = txtShiftId.getText().trim();
        String employeeId = txtStaffName.getText().trim();
        ShiftItem selectedShift = (ShiftItem) cbShiftTime.getSelectedItem();
        String date = txtDate.getText().trim();

        if (employeeId.isEmpty() || date.isEmpty() || selectedShift == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lịch cần cập nhật thông tin!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate workDate = UiKit.parseDate(date);
            if (workDate == null) throw new DateTimeParseException("empty", date, 0);
            
            LocalDate ngayMai = LocalDate.now().plusDays(1);
            if (workDate.isBefore(ngayMai)) {
                JOptionPane.showMessageDialog(this, "Ngày trực cập nhật phải từ ngày hôm sau trở đi!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LichLamViec schedule = new LichLamViec(id, employeeId, java.sql.Date.valueOf(workDate),
                    selectedShift.getGioBatDau(), selectedShift.getGioKetThuc(), "Đã xếp lịch");
            
            boolean updated = scheduleDAO.update(schedule);
            if (!updated) throw new SQLException("Không thể cập nhật lịch làm việc.");
            
            clearForm();
            renderWeekGrid();
            JOptionPane.showMessageDialog(this, "Đã cập nhật lịch làm việc thành công!");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày trực phải đúng định dạng dd/MM/yyyy.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performDelete() {
        String id = txtShiftId.getText().trim();
        if (id.isEmpty() || !isEditing) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca trực cần xóa trên lịch!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa ca trực này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = scheduleDAO.delete(id);
                if (!deleted) throw new SQLException("Không tìm thấy lịch làm việc để xóa.");
                clearForm();
                renderWeekGrid();
                JOptionPane.showMessageDialog(this, "Đã xóa lịch làm việc thành công!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa lịch: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        isEditing = false;
        txtShiftId.setText(scheduleDAO.generateNextId());
        txtShiftId.setEnabled(false);
        txtStaffName.setText("");
        txtStaffName.setEnabled(true);
        if (cbShiftTime.getItemCount() > 0) cbShiftTime.setSelectedIndex(0);
        cbShiftTime.setEnabled(true);
        txtDate.setText("");
        txtDate.setEditable(true);
        
        btnCreate.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private JButton createStaticButton(String text, Color bg, int width) {
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
        if (width != Integer.MAX_VALUE) btn.setPreferredSize(new Dimension(width, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}