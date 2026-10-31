package views;

	import javax.swing.*;
	import javax.swing.table.DefaultTableModel;
	import java.awt.*;

	public class WorkProcessPanel extends JPanel {

	    private JTable tblSchedule;
	    private DefaultTableModel tableModel;

	    public WorkProcessPanel() {
	        setLayout(new BorderLayout(10, 10));
	        setBackground(Color.WHITE);
	        initUI();
	    }

	    private void initUI() {
	        JPanel pnlForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
	        pnlForm.setBorder(BorderFactory.createTitledBorder("Phân ca làm việc & Kiểm tra trùng lịch"));

	        pnlForm.add(new JLabel("Nhân viên/Nha sĩ:"));
	        pnlForm.add(new JComboBox<>(new String[]{"BS. Nguyễn Văn A (Nha sĩ)", "NV. Trần Văn Luân (Phụ tá)"}));
	        pnlForm.add(new JLabel("Ngày làm:"));
	        pnlForm.add(new JTextField("2026-11-01", 8));
	        pnlForm.add(new JLabel("Ca làm:"));
	        pnlForm.add(new JComboBox<>(new String[]{"Ca Sáng (08:00 - 12:00)", "Ca Chiều (13:30 - 17:30)"}));

	        JButton btnCheck = new JButton("🔍 Kiểm tra trùng ca");
	        JButton btnAssign = new JButton("➕ Phân ca");
	        btnAssign.setBackground(new Color(0, 123, 255));
	        btnAssign.setForeground(Color.WHITE);

	        pnlForm.add(btnCheck);
	        pnlForm.add(btnAssign);

	        add(pnlForm, BorderLayout.NORTH);

	        String[] cols = {"Mã NV", "Họ và Tên", "Chức vụ", "Ngày làm", "Ca làm", "Thời gian", "Phòng / Ghế nha", "Trạng thái"};
	        tableModel = new DefaultTableModel(cols, 0);
	        tblSchedule = new JTable(tableModel);
	        tblSchedule.setRowHeight(26);

	        tableModel.addRow(new Object[]{"NV001", "BS. Nguyễn Văn A", "Nha sĩ", "2026-11-01", "Ca Sáng", "08:00 - 12:00", "Phòng khám 1 - Ghế 1", "Đã phân ca"});
	        tableModel.addRow(new Object[]{"NV002", "Trần Văn Luân", "Phụ tá", "2026-11-01", "Ca Sáng", "08:00 - 12:00", "Phòng khám 1 - Ghế 1", "Đã phân ca"});

	        add(new JScrollPane(tblSchedule), BorderLayout.CENTER);
	    }
	}
