package views;

import models.dao.NhanVienDAO;
import models.entity.NhanVien;
import report.PdfTableExport;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeePanel extends JPanel {
    // màu chủ đạo 
    private static final Color PINK = new Color(0xE91E78), PINK_SOFT = new Color(0xFDE8F1), PINK_DEEP = new Color(0xB10C55), BLUE = new Color(0x1E6FD9), BLUE_SOFT = new Color(0xE6F0FD), BLUE_DEEP = new Color(0x0A3A8C), BG = new Color(0xF4F7FC), TEXT = new Color(0x1F2A44), MUTED = new Color(0x6B7794), LINE = new Color(0xE3E8F2),
            RED = new Color(0xD92D3F), RED_SOFT = new Color(0xFDECEE), GREEN = new Color(0x1B8F4C), GREEN_SOFT = new Color(0xE3F6EA), ORANGE = new Color(0xC76A0B), ORANGE_SOFT = new Color(0xFFF0DC), GRAY_SOFT = new Color(0xEEF0F4);
    private static final Font F = new Font("Segoe UI", Font.PLAIN, 13), FB = F.deriveFont(Font.BOLD);
    private static final String[] COLS = {"Mã NV", "Họ và tên", "Chức vụ", "Trình độ", "Số điện thoại", "Mức lương"};
    private static final int[] WIDTHS = {80, 200, 150, 210, 120, 140};
    // Ảnh đại diện theo mã nhân viên (tròn)
    private static final Map<String, String> PHOTOS = new HashMap<>();
    private static final Map<String, Image> IMG_CACHE = new HashMap<>();
    static {
        PHOTOS.put("NV001", "doctortruongTuan.png"); 
        PHOTOS.put("NV002", "doctorMai.png"); 
        PHOTOS.put("NV003", "doctorNam.png");
        PHOTOS.put("NV004", "letanHa.png"); 
        PHOTOS.put("NV005", "nurseBao.png");
    }
    private final NhanVienDAO dao = new NhanVienDAO();
    private final JLabel[] statVals = new JLabel[4];
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch, txtEmpId, txtEmpName, txtPhone, txtSalary, txtTrinhDo;
    private JComboBox<String> cbRole;
    private JLabel lblCount, lblFormName, lblFormSub;
    private Avatar avatar;
    private boolean isEditing, mute;
    private int seq;

    public EmployeePanel() {
        setLayout(new BorderLayout(0, 16)); setBackground(BG); setBorder(new EmptyBorder(22, 26, 22, 26));
        // Tiêu đề có ảnh bên trái và thanh công cụ bên phải
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        JPanel txt = new JPanel(); 
        txt.setOpaque(false); 
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(Box.createVerticalGlue()); 
        txt.add(lbl("Quản Lý Danh Mục Nhân Viên", F.deriveFont(Font.BOLD, 24f), TEXT));
        txt.add(lbl("Quản lý hồ sơ nha sĩ, điều dưỡng, lễ tân của phòng khám; dữ liệu lấy trực tiếp từ SQL Server", F, MUTED)); txt.add(Box.createVerticalGlue());
        JPanel ttl = new JPanel(new BorderLayout(14, 0)); 
        ttl.setOpaque(false); 
        ttl.add(new JLabel(icon("rang-tiepnhan.png", 56, 56)), BorderLayout.WEST); ttl.add(txt, BorderLayout.CENTER);
        txtSearch = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create(); 
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(MUTED); 
                    g2.setFont(F); FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Tìm theo tên hoặc số điện thoại...", getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2); g2.dispose();
                }
            }
        };
        txtSearch.setFont(F); txtSearch.setForeground(TEXT); txtSearch.setPreferredSize(new Dimension(280, 40)); border(txtSearch, LINE);
        JButton bSearch = btn("Tìm", BLUE, Color.WHITE), bNew = btn("Thêm mới", PINK, Color.WHITE), bEdit = btn("Sửa", BLUE_SOFT, BLUE), bDel = btn("Xóa", RED_SOFT, RED);
        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); tools.setOpaque(false);
        tools.add(txtSearch); tools.add(bSearch); tools.add(bNew); tools.add(bEdit); tools.add(bDel);
        head.add(ttl, BorderLayout.WEST); head.add(tools, BorderLayout.EAST);
        
        // Thẻ thống kê tổng quan 
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0)); 
        stats.setOpaque(false);
        stats.add(stat("Tổng nhân viên", PINK_DEEP, 0, "tongKH.png")); 
        stats.add(stat("Bác sĩ / Nha sĩ", BLUE_DEEP, 1, "bacsi-nhasi.png"));
        stats.add(stat("Lễ tân / Điều dưỡng", PINK_DEEP, 2, "letan.png")); 
        stats.add(stat("Quỹ lương hàng tháng", BLUE_DEEP, 3, "quyluong.png"));
        JPanel north = new JPanel(); north.setOpaque(false); 
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(head); north.add(Box.createVerticalStrut(16)); 
        north.add(stats);
        add(north, BorderLayout.NORTH);
        
        // Bảng danh sách nhân viên
        tableModel = new DefaultTableModel(null, COLS) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 5 ? Double.class : String.class; }
        };
        table = new JTable(tableModel);
        PdfTableExport.install(table, "Danh sách nhân viên");
        table.setRowSorter(new TableRowSorter<>(tableModel));
        table.setRowHeight(44); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0)); table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setFont(F); table.setSelectionBackground(BLUE_SOFT); table.setSelectionForeground(TEXT);
        
        // căn giữa Mã NV, SĐT, Mức lương; căn trái Họ tên
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, false, r, c);
                setBorder(new EmptyBorder(0, 12, 0, 12)); 
                setFont(F); setForeground(TEXT); 
                setHorizontalAlignment(c == 1 ? LEFT : CENTER);
                setBackground(sel ? BLUE_SOFT : (r % 2 == 0 ? Color.WHITE : new Color(0xFAFBFE)));
                if (c == 5) { setText(money(v instanceof Number ? ((Number) v).doubleValue() : 0) + " đ"); 
                setFont(FB); setForeground(BLUE); }
                return this;
            }
        };
        DefaultTableCellRenderer hr = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, false, false, r, c);
                setOpaque(true); setBackground(PINK); 
                setForeground(Color.WHITE); 
                setFont(FB); setHorizontalAlignment(c == 0 || c == 4 || c == 5 ? CENTER : LEFT); 
                setBorder(new EmptyBorder(0, 12, 0, 12)); 
                return this;
            }
        };
        for (int i = 0; i < COLS.length; i++) { 
        	TableColumn tc = table.getColumnModel().getColumn(i); 
        	tc.setPreferredWidth(WIDTHS[i]); 
        	tc.setCellRenderer(i == 2 || i == 3 ? new Badge() : cr); 
        	tc.setHeaderRenderer(hr); 
        	}
        table.getTableHeader().setPreferredSize(new Dimension(0, 42)); 
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(table); 
        sp.setBorder(null); 
        sp.getViewport().setBackground(Color.WHITE);
        lblCount = lbl("", F, MUTED); lblCount.setBorder(new EmptyBorder(8, 8, 2, 0));
        Card tableCard = new Card(null); tableCard.setLayout(new BorderLayout()); 
        tableCard.setBorder(new EmptyBorder(8, 8, 8, 8));
        tableCard.add(sp, BorderLayout.CENTER); 
        tableCard.add(lblCount, BorderLayout.SOUTH);
        
        // Khung thông tin nhân viên bên phải
        avatar = new Avatar();
        lblFormName = lbl("Nhân viên mới", F.deriveFont(Font.BOLD, 17f), TEXT); lblFormSub = lbl("Nhập thông tin để thêm mới", F, MUTED);
        JPanel who = new JPanel(); 
        who.setOpaque(false); 
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.add(Box.createVerticalGlue()); 
        who.add(lblFormName); 
        who.add(Box.createVerticalStrut(4)); 
        who.add(lblFormSub); 
        who.add(Box.createVerticalGlue());
        JPanel aw = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
        aw.setOpaque(false); aw.add(avatar);
        JPanel fh = new JPanel(new BorderLayout(12, 0)); 
        fh.setOpaque(false); fh.setBorder(new EmptyBorder(2, 0, 14, 0)); 
        fh.setAlignmentX(Component.LEFT_ALIGNMENT);
        fh.add(aw, BorderLayout.WEST); 
        fh.add(who, BorderLayout.CENTER);
        ScrollBox box = new ScrollBox(); 
        box.setOpaque(false); 
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS)); 
        box.setBorder(new EmptyBorder(2, 0, 2, 10));
        box.add(fh);
        txtEmpId = field(box, "Mã nhân viên"); 
        txtEmpName = field(box, "Họ và tên");
        box.add(fieldLabel("Chức vụ")); 
        box.add(Box.createVerticalStrut(4));
        cbRole = new JComboBox<>(new String[]{"Bác sĩ trưởng", "Nha sĩ", "Điều dưỡng", "Y tá", "Lễ tân", "Quản lý"});
        cbRole.setFont(F); cbRole.setBackground(Color.WHITE); 
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT); 
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.add(cbRole); box.add(Box.createVerticalStrut(12));
        txtTrinhDo = field(box, "Trình độ"); 
        txtPhone = field(box, "Số điện thoại"); 
        txtSalary = field(box, "Mức lương (đ)");
        
        // Tự thêm dấu chấm ngăn cách hàng nghìn khi nhập lương, chỉ nhận chữ số
        ((AbstractDocument) txtSalary.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = new StringBuilder(cur).replace(off, off + len, s == null ? "" : s).toString().replaceAll("\\D", "");
                if (next.length() > 12) next = next.substring(0, 12);
                fb.replace(0, cur.length(), next.isEmpty() ? "" : money(Long.parseLong(next)), null);
            }
            @Override public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException { replace(fb, off, 0, s, a); }
            @Override public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (len == 1 && off > 0 && cur.charAt(off) == '.') off--; // xóa luôn chữ số đứng trước dấu chấm
                replace(fb, off, len, "", null);
            }
        });
        JScrollPane ds = new JScrollPane(box); 
        ds.setBorder(null); ds.setOpaque(false); 
        ds.getViewport().setOpaque(false); 
        ds.getVerticalScrollBar().setUnitIncrement(14); 
        ds.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JButton bSave = btn("Lưu", BLUE, Color.WHITE), 
        		bClear = btn("Làm mới", PINK, Color.WHITE);
        bSave.setPreferredSize(new Dimension(130, 44)); 
        bClear.setPreferredSize(new Dimension(130, 44));
        JPanel bp = new JPanel(new GridLayout(1, 2, 10, 0)); bp.setOpaque(false); 
        bp.add(bSave); 
        bp.add(bClear);
        Card form = new Card(PINK); form.setLayout(new BorderLayout(0, 10)); 
        form.setPreferredSize(new Dimension(340, 0)); 
        form.setMinimumSize(new Dimension(320, 0));
        form.add(ds, BorderLayout.CENTER); form.add(bp, BorderLayout.SOUTH);
        JPanel center = new JPanel(new BorderLayout(14, 0)); 
        center.setOpaque(false); center.add(tableCard, BorderLayout.CENTER); 
        center.add(form, BorderLayout.EAST);
        add(center, BorderLayout.CENTER);
        
        // Sự kiện: tìm kiếm tự động sau khi ngừng gõ 300ms
        Timer debounce = new Timer(300, e -> load()); debounce.setRepeats(false);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
            	if (!mute) debounce.restart(); 
            	}
            public void removeUpdate(DocumentEvent e) { 
            	if (!mute) debounce.restart(); 
            	}
            public void changedUpdate(DocumentEvent e) {
            	if (!mute) debounce.restart(); 
            	}
        });
        // Đổi mã hoặc tên thì cập nhật ngay ảnh và tiêu đề của form
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
            	refreshHeader(); 
            	}
            public void removeUpdate(DocumentEvent e) { 
            	refreshHeader(); 
            	}
            public void changedUpdate(DocumentEvent e) { 
            	refreshHeader(); 
            	}
        };
        txtEmpId.getDocument().addDocumentListener(dl); 
        txtEmpName.getDocument().addDocumentListener(dl);
        cbRole.addActionListener(e -> refreshHeader());
        txtSearch.addActionListener(e -> load()); 
        bSearch.addActionListener(e -> load());
        bNew.addActionListener(e -> { clearForm(); 
        txtEmpId.requestFocus(); });
        bEdit.addActionListener(e -> {
            if (table.getSelectedRow() == -1) { 
            	JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
            loadDataToFormForEdit(); 
            txtEmpName.requestFocus();
        });
        bDel.addActionListener(e -> performDelete()); 
        bSave.addActionListener(e -> performSave()); 
        bClear.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadDataToFormForEdit(); });
        // Tự tải lại dữ liệu mỗi khi trang được hiển thị
        addHierarchyListener(e -> { if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) load(); });
    }

    // Tải dữ liệu từ SQL (chạy nền, không làm đơ giao diện); thẻ thống kê luôn tính trên toàn bộ nhân viên
    private void load() {
        final int id = ++seq; final String kw = txtSearch.getText().trim();
        new SwingWorker<Object[], Void>() {
            @Override protected Object[] doInBackground() { List<NhanVien> all = dao.getAllNhanVien(); return new Object[]{kw.isEmpty() ? all : dao.search(kw), all}; }
            @Override protected void done() {
                if (id != seq) return; // bỏ qua kết quả của lần tìm đã cũ
                try {
                    Object[] r = get(); @SuppressWarnings("unchecked") List<NhanVien> rows = (List<NhanVien>) r[0], all = (List<NhanVien>) r[1];
                    fill(rows, all);
                } catch (Exception e) { JOptionPane.showMessageDialog(EmployeePanel.this, "Không thể tải dữ liệu nhân viên:\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }

    // Đổ dữ liệu lên bảng và cập nhật 4 thẻ thống kê
    private void fill(List<NhanVien> rows, List<NhanVien> all) {
        tableModel.setRowCount(0);
        for (NhanVien n : rows) tableModel.addRow(new Object[]{n.getMaNhanVien(), n.getTenNhanVien(), n.getChucVu(), n.getTrinhDo(), n.getSdt(), sal(n)});
        int doc = 0, sup = 0; double sum = 0;
        for (NhanVien n : all) {
            String r = n.getChucVu() == null ? "" : n.getChucVu().toLowerCase();
            if (r.contains("bác sĩ") || r.contains("nha sĩ")) doc++; else if (r.contains("lễ tân") || r.contains("điều dưỡng") || r.contains("y tá")) sup++;
            sum += sal(n);
        }
        statVals[0].setText(String.valueOf(all.size())); statVals[1].setText(String.valueOf(doc)); statVals[2].setText(String.valueOf(sup)); statVals[3].setText(money(sum) + " đ");
        lblCount.setText("Hiển thị " + rows.size() + " / " + all.size() + " nhân viên");
    }

    private void clearForm() {
        isEditing = false; 
        txtEmpId.setText(""); 
        txtEmpId.setEnabled(true); 
        txtEmpName.setText(""); 
        cbRole.setSelectedIndex(0); 
        txtTrinhDo.setText(""); 
        txtPhone.setText(""); 
        txtSalary.setText("");
        table.clearSelection(); 
        mute = true; txtSearch.setText(""); 
        mute = false; 
        refreshHeader(); 
        load();
    }

    private void loadDataToFormForEdit() {
        int vr = table.getSelectedRow(); 
        if (vr == -1) return;
        int mr = table.convertRowIndexToModel(vr);
        isEditing = true; txtEmpId.setText(str(tableModel.getValueAt(mr, 0))); 
        txtEmpId.setEnabled(false); 
        txtEmpName.setText(str(tableModel.getValueAt(mr, 1)));
        selectRole(tableModel.getValueAt(mr, 2)); 
        txtTrinhDo.setText(str(tableModel.getValueAt(mr, 3))); txtPhone.setText(str(tableModel.getValueAt(mr, 4)));
        Object s = tableModel.getValueAt(mr, 5); 
        txtSalary.setText(s instanceof Number ? String.valueOf(((Number) s).longValue()) : "");
        refreshHeader();
    }

    // Chọn chức vụ trong combobox, nếu chức vụ lạ (có trong SQL) thì tự thêm vào danh sách
    private void selectRole(Object r) {
        if (r == null) return;
        for (int i = 0; i < cbRole.getItemCount(); i++) if (cbRole.getItemAt(i).equals(r.toString())) { cbRole.setSelectedIndex(i); return; }
        cbRole.addItem(r.toString()); cbRole.setSelectedItem(r.toString());
    }

    // Cập nhật ảnh đại diện và dòng giới thiệu ở đầu form
    private void refreshHeader() {
        String n = txtEmpName.getText().trim();
        lblFormName.setText(wrap(n.isEmpty() ? "Nhân viên mới" : n, 190)); avatar.set(txtEmpId.getText().trim(), n);
        lblFormSub.setText(isEditing ? "Mã " + txtEmpId.getText() + "  |  " + cbRole.getSelectedItem() : "Nhập thông tin để thêm mới");
    }

    private void performSave() {
        String id = txtEmpId.getText().trim(), name = txtEmpName.getText().trim(), role = (String) cbRole.getSelectedItem(), trinhDo = txtTrinhDo.getText().trim(), phone = txtPhone.getText().trim();
        String digits = txtSalary.getText().replaceAll("\\D", ""); 
        double salary = digits.isEmpty() ? 0.0 : Double.parseDouble(digits);
        if (id.isEmpty() || name.isEmpty()) { 
        	JOptionPane.showMessageDialog(this, "Vui lòng nhập Mã và Họ tên nhân viên!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; 
        	}
        if (!phone.isEmpty() && !phone.matches("\\d{9,11}")) { 
        	JOptionPane.showMessageDialog(this, "Số điện thoại chỉ gồm 9 - 11 chữ số!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; 
        	}
        try {
            // Khởi tạo đối tượng NhanVien khớp với constructor đầy đủ của Entity NhanVien
            NhanVien nv = new NhanVien(id, name, null, phone, role, null, trinhDo, salary);
            boolean ok = isEditing ? dao.update(nv) : dao.insert(nv);
            if (!ok) { JOptionPane.showMessageDialog(this, isEditing ? "Cập nhật thất bại!" : "Thêm thất bại (Trùng mã nhân viên hoặc lỗi CSDL)!", "Lỗi", JOptionPane.ERROR_MESSAGE); return; }
            JOptionPane.showMessageDialog(this, isEditing ? "Cập nhật nhân viên thành công!" : "Thêm nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (SQLException ex) { 
        	JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); ex.printStackTrace(); 
        	}
    }

    private void performDelete() {
        int vr = table.getSelectedRow();
        if (vr == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE); return; }
        String id = str(tableModel.getValueAt(table.convertRowIndexToModel(vr), 0));
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên " + id + "?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            if (dao.delete(id)) { 
            	JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE); clearForm(); 
            	}
            else JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên này (đang có dữ liệu liên quan)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) { 
        	JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); ex.printStackTrace(); 
        	}
    }

    // Các hàm hỗ trợ
    private static String money(double v) { 
    	return String.format(Locale.GERMANY, "%,.0f", v); 
    	} // định dạng vd 30.000.000
    private static double sal(NhanVien n) { 
    	Object o = n.getMucLuong(); 
    	return o instanceof Number ? ((Number) o).doubleValue() : 0; 
    	}
    private static String str(Object o) { 
    	return o == null ? "" : o.toString(); 
    	}
    private static String wrap(String s, int w) { 
    	return "<html><div style='width:" + w + "px'>" + s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</div></html>"; }
    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] p = name.trim().split("\\s+");
        return (p.length == 1 ? p[0].substring(0, 1) : p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }
    
    // Đọc ảnh img
    private static Image img(String name) {
        return IMG_CACHE.computeIfAbsent(name, n -> {
            java.net.URL u = EmployeePanel.class.getResource("/img/" + n);
            if (u != null) 
            	return new ImageIcon(u).getImage();
            File f = new File("src/img/" + n);
            return f.exists() ? new ImageIcon(f.getAbsolutePath()).getImage() : null;
        });
    }
    private static ImageIcon icon(String n, int w, int h) {
        Image i = img(n);
        return i == null ? new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)) : new ImageIcon(i.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
    private static void border(JTextField t, Color c) { 
    	t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(c, 1, true), new EmptyBorder(7, 10, 7, 10))); 
    	}
    private static JLabel lbl(String t, Font f, Color c) { 
    	JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); l.setAlignmentX(Component.LEFT_ALIGNMENT); 
    	return l; 
    	}
    private static JLabel fieldLabel(String t) { 
    	return lbl(t, F.deriveFont(Font.BOLD, 12f), TEXT); 
    }
    
    // Ô nhập liệu bo góc, viền đổi sang xanh khi đang focus
    private JTextField field(JPanel box, String label) {
        box.add(fieldLabel(label)); 
        box.add(Box.createVerticalStrut(4));
        JTextField t = new JTextField(); t.setFont(F); 
        t.setForeground(TEXT); t.setDisabledTextColor(MUTED); 
        t.setAlignmentX(Component.LEFT_ALIGNMENT); 
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); 
        border(t, LINE);
        t.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { 
            	border(t, BLUE); 
            	}
            @Override public void focusLost(FocusEvent e) { 
            	border(t, LINE); 
            	}
        });
        box.add(t); box.add(Box.createVerticalStrut(12)); return t;
    }
    
    // Thẻ thống kê: ảnh bên trái, tiêu đề và số liệu lớn bên phải
    private Card stat(String title, Color color, int i, String image) {
        Card c = new Card(color); 
        c.setLayout(new BorderLayout(14, 0)); 
        c.setBorder(new EmptyBorder(14, 20, 14, 14));
        JLabel ic = new JLabel(icon(image, 52, 52)); 
        ic.setPreferredSize(new Dimension(52, 52));
        JPanel info = new JPanel(); info.setOpaque(false); 
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        statVals[i] = lbl("0", F.deriveFont(Font.BOLD, 30f), color);
        info.add(lbl(title, F, MUTED)); 
        info.add(Box.createVerticalStrut(4)); 
        info.add(statVals[i]);
        c.add(ic, BorderLayout.WEST); 
        c.add(info, BorderLayout.CENTER); 
        return c;
    }
    
    // Màu huy hiệu theo cột 
    private static Color[] badgeColors(int col, String t) {
        String s = t.toLowerCase();
        if (col == 2) {
            if (s.contains("bác sĩ") || s.contains("nha sĩ")) return new Color[]{BLUE, BLUE_SOFT};
            if (s.contains("điều dưỡng") || s.contains("y tá")) return new Color[]{GREEN, GREEN_SOFT};
            if (s.contains("lễ tân")) return new Color[]{ORANGE, ORANGE_SOFT};
            return new Color[]{PINK, PINK_SOFT};
        }
        if (s.contains("thạc sĩ") || s.contains("tiến sĩ")) return new Color[]{PINK, PINK_SOFT};
        if (s.contains("đại học")) return new Color[]{BLUE, BLUE_SOFT};
        if (s.contains("cao đẳng")) return new Color[]{GREEN, GREEN_SOFT};
        return new Color[]{MUTED, GRAY_SOFT};
    }
    
    // Nút bo góc, đổi sắc khi rê chuột
    private static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(!isEnabled() ? LINE : (getModel().isRollover() ? getBackground().darker() : getBackground())); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false); 
        b.setBorderPainted(false); 
        b.setFocusPainted(false); 
        b.setOpaque(false); 
        b.setRolloverEnabled(true);
        b.setBackground(bg); 
        b.setForeground(fg); 
        b.setFont(FB); 
        b.setBorder(new EmptyBorder(10, 18, 10, 18)); 
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return b;
    }

    // Huy hiệu bo tròn cho cột Chức vụ và Trình độ
    private static class Badge extends JComponent implements TableCellRenderer {
        private String text = ""; 
        private Color fg = TEXT, bg = LINE, row = Color.WHITE;
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            text = v == null ? "" : v.toString(); Color[] k = badgeColors(c, text); fg = k[0]; bg = k[1];
            row = sel ? BLUE_SOFT : (r % 2 == 0 ? Color.WHITE : new Color(0xFAFBFE)); 
            return this;
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(row); g2.fillRect(0, 0, getWidth(), getHeight());
            if (!text.isEmpty()) {
                g2.setFont(FB.deriveFont(12f)); 
                FontMetrics fm = g2.getFontMetrics();
                int h = 26, w = Math.min(fm.stringWidth(text) + 24, getWidth() - 16), y = (getHeight() - h) / 2;
                g2.setColor(bg); g2.fillRoundRect(12, y, w, h, h, h);
                g2.clipRect(16, y, w - 8, h); 
                g2.setColor(fg); 
                g2.drawString(text, 24, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    // Ảnh đại diện tròn của nhân viên; không có ảnh thì hiện chữ cái viết tắt
    private static class Avatar extends JComponent {
        private String text = "?"; 
        private BufferedImage round;
        Avatar() { setPreferredSize(new Dimension(58, 58)); }
        
        // Cắt ảnh thành hình tròn ở độ phân giải gấp đôi để khi thu nhỏ vẫn sắc nét, ưu tiên giữ phần đầu người
        void set(String id, String name) {
            text = initials(name); 
            round = null;
            String f = PHOTOS.get(id.toUpperCase()); Image p = f == null ? null : img(f);
            if (p != null && p.getWidth(null) > 0) {
                int S = 116, pw = p.getWidth(null), ph = p.getHeight(null); 
                double k = Math.max((double) S / pw, (double) S / ph);
                int dw = (int) Math.round(pw * k), dh = (int) Math.round(ph * k), ox = (S - dw) / 2, oy = dh > S ? -(dh - S) / 5 : (S - dh) / 2;
                round = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB); 
                Graphics2D g = round.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); 
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.fill(new Ellipse2D.Float(0, 0, S, S)); 
                g.setComposite(AlphaComposite.SrcIn); 
                g.drawImage(p, ox, oy, dw, dh, null); 
                g.dispose();
            }
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); 
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            int s = getWidth();
            if (round != null) { g2.drawImage(round, 0, 0, s, s, this); 
            g2.setColor(PINK); 
            g2.setStroke(new BasicStroke(2f)); 
            g2.drawOval(1, 1, s - 3, s - 3); }
            else {
                g2.setColor(PINK); 
                g2.fillOval(0, 0, s, s); 
                g2.setColor(Color.WHITE); 
                g2.setFont(F.deriveFont(Font.BOLD, 22f));
                FontMetrics fm = g2.getFontMetrics(); 
                g2.drawString(text, (s - fm.stringWidth(text)) / 2, (s + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    // Khung cuộn tự co theo chiều rộng, chỉ cuộn dọc
    private static class ScrollBox extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { 
        	return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { 
        	return 14; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { 
        	return 80; }
        @Override public boolean getScrollableTracksViewportWidth() { 
        	return true; }
        @Override public boolean getScrollableTracksViewportHeight() { 
        	return false; }
    }

    // Thẻ nền trắng bo góc, thanh màu nhấn bên trái
    private static class Card extends JPanel {
        private final Color accent;
        Card(Color accent) { 
        	this.accent = accent; 
        setOpaque(false); 
        setBorder(new EmptyBorder(14, 18, 14, 16)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            g2.setColor(Color.WHITE); 
            g2.fillRoundRect(0, 0, w, h, 16, 16);
            if (accent != null) { 
            	g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 16, 16)); 
            g2.setColor(accent); 
            g2.fillRect(0, 0, 5, h); g2.setClip(null); }
            g2.setColor(LINE); 
            g2.drawRoundRect(0, 0, w, h, 16, 16); 
            g2.dispose(); 
            super.paintComponent(g);
        }
    }
}