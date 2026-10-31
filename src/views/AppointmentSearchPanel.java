
package views;

// Tra cứu lịch hẹn

import report.PdfTableExport;
import sql.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AppointmentSearchPanel extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(0, 126, 255);
    private static final Color BG_CANVAS = new Color(243, 248, 253);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(15, 29, 69);
    private static final Color BORDER_LIGHT = new Color(224, 233, 243);
    private static final Color MUTED = new Color(91, 112, 145);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public AppointmentSearchPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(BG_CANVAS);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Tra Cứu Lịch Hẹn Khám");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel(
            "Nhập từ khóa (Mã lịch, Tên khách hàng hoặc SĐT) để tra cứu lịch hẹn nhanh chóng"
        );

        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(MUTED);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(lblSub);

        headerPanel.add(titleBox, BorderLayout.WEST);

        // THANH TÌM KIẾM
        JPanel toolbarPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        toolbarPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(TEXT_DARK);

        txtSearch = new JTextField(18);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(180, 36));

        txtSearch.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                new EmptyBorder(4, 10, 4, 10)
            )
        );

        txtSearch.addActionListener(e -> performSearch());

        JButton btnSearch = createStaticButton(
            "Tra Cứu", BLUE_PRIMARY, 90
        );

        btnSearch.addActionListener(e -> performSearch());

        toolbarPanel.add(lblSearch);
        toolbarPanel.add(txtSearch);
        toolbarPanel.add(btnSearch);

        headerPanel.add(toolbarPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // BẢNG KẾT QUẢ
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        String[] cols = {
            "Mã Lịch Hẹn",
            "Mã KH",
            "Tên Khách Hàng",
            "Số Điện Thoại",
            "Dịch Vụ Khám",
            "Nha Sĩ Phụ Trách",
            "Ngày Hẹn",
            "Giờ Hẹn",
            "Trạng Thái"
        };

        tableModel = new DefaultTableModel(null, cols) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);

        // XUẤT PDF
        PdfTableExport.install(
            table,
            "Danh sách tra cứu lịch hẹn"
        );

        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);

        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1)
        );

        scroll.getViewport().setBackground(CARD_BG);

        centerContainer.add(scroll, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);

        // TẢI DỮ LIỆU TỪ DATABASE
        loadAppointments();
    }

    // =====================================================
    // TẢI DANH SÁCH LỊCH HẸN TỪ SQL SERVER
    // =====================================================

    private void loadAppointments() {

        tableModel.setRowCount(0);

        String sql =
            "SELECT "
            + "L.maLichHen, "
            + "K.maKhachHang, "
            + "K.tenKH, "
            + "K.sdt, "
            + "L.ghiChu AS dichVu, "
            + "N.tenNhanVien AS nhaSi, "
            + "CONVERT(VARCHAR(10), L.ngayHen, 103) AS ngayHen, "
            + "CAST(N'Chưa cập nhật' AS NVARCHAR(30)) AS gioHen, "
            + "L.trangThai "
            + "FROM LichHen L "
            + "INNER JOIN KhachHang K "
            + "ON L.maKhachHang = K.maKhachHang "
            + "LEFT JOIN NhanVien N "
            + "ON L.maNhanVien = N.maNhanVien "
            + "ORDER BY L.ngayHen DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Object[] row = {
                    rs.getString("maLichHen"),
                    rs.getString("maKhachHang"),
                    rs.getString("tenKH"),
                    rs.getString("sdt"),
                    rs.getString("dichVu"),
                    rs.getString("nhaSi"),
                    rs.getString("ngayHen"),
                    rs.getString("gioHen"),
                    rs.getString("trangThai")
                };

                tableModel.addRow(row);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                this,
                "Không thể tải danh sách lịch hẹn!\n"
                + e.getMessage(),
                "Lỗi SQL",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // TÌM KIẾM LỊCH HẸN
    // =====================================================

    private void performSearch() {

        String kw = txtSearch.getText().trim();

        if (kw.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {

            // Tìm không phân biệt chữ hoa, chữ thường
            // Không bị lỗi khi nhập các ký tự đặc biệt
            rowSorter.setRowFilter(
                RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(kw)
                )
            );
        }
    }

    // =====================================================
    // TẠO BUTTON
    // =====================================================

    private JButton createStaticButton(
            String text, Color bg, int width) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(bg);

                g2.fillRoundRect(
                    0, 0, getWidth(), getHeight(), 8, 8
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);

        if (width != Integer.MAX_VALUE) {
            btn.setPreferredSize(new Dimension(width, 36));
        }

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return btn;
    }

    // =====================================================
    // ĐỊNH DẠNG BẢNG
    // =====================================================

    private void styleTable(JTable t) {

        t.setRowHeight(38);

        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT_DARK);
        t.setGridColor(new Color(240, 244, 248));

        t.setSelectionBackground(
            new Color(225, 240, 255)
        );

        t.setSelectionForeground(TEXT_DARK);

        t.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );

        t.getTableHeader().setBackground(
            new Color(245, 248, 253)
        );

        t.getTableHeader().setForeground(TEXT_DARK);

        t.getTableHeader().setPreferredSize(
            new Dimension(0, 36)
        );
    }
}
