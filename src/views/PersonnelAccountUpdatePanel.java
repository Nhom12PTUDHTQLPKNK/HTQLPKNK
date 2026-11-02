package views;

import models.dao.PersonnelAccountDAO;
import controllers.PdfTableExport;
import controllers.AccessControl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/** SQL Server backed employee and account maintenance screen. */
public class PersonnelAccountUpdatePanel extends JPanel {
    private static final Color BLUE = new Color(7, 65, 150);
    private static final Color GREEN = new Color(20, 92, 45);
    private static final Color RED = new Color(153, 27, 27);
    private static final Color BG = new Color(244, 248, 253);
    private static final Color TEXT = new Color(35, 52, 78);
    private static final Color MUTED = new Color(119, 135, 157);
    private static final Color BORDER = new Color(225, 233, 243);

    private final PersonnelAccountDAO dao = new PersonnelAccountDAO();
    private final DefaultTableModel model = new DefaultTableModel(null, new String[]{
            "Mã nhân viên", "Họ và tên", "Tên đăng nhập", "Quyền", "Số điện thoại", "Mức lương", "Trạng thái tài khoản"
    }) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField();
    private final JTextField id = new JTextField(), name = new JTextField(), username = new JTextField();
    private final JTextField phone = new JTextField(), salary = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JComboBox<String> role = new JComboBox<>(new String[]{"Admin", "Nha sĩ", "Lễ tân", "Y tá"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});
    private boolean editing;
    private String originalUsername;

    public PersonnelAccountUpdatePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(BG);
        setBorder(new EmptyBorder(22, 25, 22, 25));
        add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);
        content.add(createTablePanel(), BorderLayout.CENTER);
        content.add(createFormPanel(), BorderLayout.EAST);
        add(content, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedRow();
        });
        reload();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Cập nhật nhân sự & tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Quản lý hồ sơ nhân viên, tài khoản đăng nhập và quyền truy cập");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(5));
        titleBox.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setOpaque(false);
        search.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        search.setPreferredSize(new Dimension(250, 38));
        search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(5, 12, 5, 12)));
        search.setToolTipText("Tìm theo mã nhân viên, họ tên hoặc tài khoản");
        search.addActionListener(e -> filter());
        JButton find = button("Tìm kiếm", BLUE);
        JButton refresh = button("Tải lại", new Color(74, 88, 105));
        find.addActionListener(e -> filter());
        refresh.addActionListener(e -> { search.setText(""); reload(); clearForm(); });
        actions.add(search);
        actions.add(find);
        actions.add(refresh);
        header.add(titleBox, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createTablePanel() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Danh sách nhân sự & tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        JLabel hint = new JLabel("Chọn một nhân viên để xem hoặc cập nhật thông tin");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(hint);

        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setGridColor(new Color(238, 243, 248));
        table.setSelectionBackground(new Color(225, 240, 255));
        table.setSelectionForeground(TEXT);
        table.setAutoCreateRowSorter(true);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableHeader.setBackground(new Color(245, 248, 253));
        tableHeader.setForeground(TEXT);
        tableHeader.setPreferredSize(new Dimension(0, 40));
        PdfTableExport.install(table, "Nhân sự và tài khoản");

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormPanel() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 18, 20, 18));
        card.setPreferredSize(new Dimension(330, 0));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Thông tin nhân sự");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Nhập hoặc cập nhật dữ liệu SQL Server");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        addField(fields, "Mã nhân viên", id);
        addField(fields, "Họ và tên", name);
        addField(fields, "Tên đăng nhập", username);
        addField(fields, "Mật khẩu (để trống nếu giữ nguyên)", password);
        addField(fields, "Quyền tài khoản", role);
        addField(fields, "Số điện thoại", phone);
        addField(fields, "Mức lương (VNĐ)", salary);
        addField(fields, "Trạng thái tài khoản", status);
        id.setToolTipText("Mã nhân viên mới phải chưa tồn tại trong SQL Server");

        JScrollPane formScroll = new JScrollPane(fields);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 0, 9));
        buttons.setOpaque(false);
        JButton save = button("Lưu thông tin", GREEN);
        JButton delete = button("Xóa nhân sự", RED);
        JButton clear = button("Làm mới biểu mẫu", BLUE);
        save.addActionListener(e -> save());
        delete.addActionListener(e -> delete());
        clear.addActionListener(e -> clearForm());
        buttons.add(save);
        buttons.add(delete);
        buttons.add(clear);
        if (!AccessControl.isAdministrator()) {
            save.setEnabled(false);
            delete.setEnabled(false);
        }
        card.add(heading, BorderLayout.NORTH);
        card.add(formScroll, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel fields, String label, JComponent input) {
        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.BOLD, 12));
        caption.setForeground(TEXT);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        input.setPreferredSize(new Dimension(280, 36));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(4, 9, 4, 9)));
        fields.add(caption);
        fields.add(Box.createVerticalStrut(4));
        fields.add(input);
        fields.add(Box.createVerticalStrut(10));
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1), new EmptyBorder(6, 12, 6, 12)));
        button.setPreferredSize(new Dimension(100, 38));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent event) {
                if (button.isEnabled()) button.setBackground(color.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent event) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private void reload() {
        model.setRowCount(0);
        if (!AccessControl.isAdministrator()) {
            showError("Chỉ tài khoản admin mới được xem và quản lý nhân sự/tài khoản.");
            return;
        }
        try {
            List<Object[]> rows = dao.getAll();
            for (Object[] row : rows) model.addRow(row);
        } catch (SQLException ex) {
            showError("Không tải được dữ liệu từ SQL Server: " + ex.getMessage());
        }
    }

    private void filter() {
        String keyword = search.getText().trim();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(keyword.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
    }

    private void loadSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int row = table.convertRowIndexToModel(viewRow);
        editing = true;
        id.setText(String.valueOf(model.getValueAt(row, 0)));
        name.setText(String.valueOf(model.getValueAt(row, 1)));
        originalUsername = (String) model.getValueAt(row, 2);
        username.setText(originalUsername == null ? "" : originalUsername);
        role.setSelectedItem(model.getValueAt(row, 3));
        phone.setText(model.getValueAt(row, 4) == null ? "" : String.valueOf(model.getValueAt(row, 4)));
        salary.setText(model.getValueAt(row, 5) == null ? "" : String.valueOf(model.getValueAt(row, 5)));
        String accountStatus = String.valueOf(model.getValueAt(row, 6));
        status.setSelectedItem("Chưa cấp tài khoản".equals(accountStatus) ? "Hoạt động" : accountStatus);
        password.setText("");
        id.setEditable(false);
        boolean protectedAccount = AccessControl.isProtectedAdministrator(originalUsername, String.valueOf(model.getValueAt(row, 3)));
        username.setEditable(!protectedAccount);
        role.setEnabled(!protectedAccount);
        status.setEnabled(!protectedAccount);
    }

    private void save() {
        try {
            double amount = salary.getText().trim().isEmpty() ? 0 : Double.parseDouble(salary.getText().trim().replace(",", ""));
            dao.save(id.getText().trim(), name.getText().trim(), username.getText().trim(), new String(password.getPassword()),
                    (String) role.getSelectedItem(), phone.getText().trim(), amount,
                    "Hoạt động".equals(status.getSelectedItem()), editing, originalUsername);
            JOptionPane.showMessageDialog(this, "Đã lưu nhân sự và tài khoản vào SQL Server.");
            reload();
            clearForm();
        } catch (NumberFormatException ex) {
            showError("Mức lương phải là số hợp lệ.");
        } catch (SQLException ex) {
            showError("Không thể lưu vào SQL Server: " + ex.getMessage());
        }
    }

    private void delete() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { showError("Chọn nhân viên cần xóa."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        String staffId = String.valueOf(model.getValueAt(row, 0));
        String linkedUsername = (String) model.getValueAt(row, 2);
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa nhân viên " + staffId + " và tài khoản liên kết khỏi SQL Server?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(staffId, linkedUsername);
            JOptionPane.showMessageDialog(this, "Đã xóa dữ liệu khỏi SQL Server.");
            reload();
            clearForm();
        } catch (SQLException ex) {
            showError("Không thể xóa: " + ex.getMessage());
        }
    }

    private void clearForm() {
        editing = false;
        originalUsername = null;
        for (JTextField field : new JTextField[]{id, name, username, phone, salary}) field.setText("");
        password.setText("");
        id.setEditable(true);
        username.setEditable(true);
        role.setEnabled(true);
        status.setEnabled(true);
        role.setSelectedIndex(0);
        status.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
    }

    private static final class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        RoundedPanel(int radius, Color fill) { this.radius = radius; this.fill = fill; setOpaque(false); }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
