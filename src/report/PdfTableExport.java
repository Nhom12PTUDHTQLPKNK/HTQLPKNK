package report;

import controllers.AuthController;
import models.entity.Account;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public final class PdfTableExport {

    private static final String INSTALLED_PROPERTY = PdfTableExport.class.getName() + ".installed";
    private static final String BUTTONS_ATTACHED_PROPERTY = PdfTableExport.class.getName() + ".buttonsAttached";

    private PdfTableExport() { }

    public static void install(JTable table, String title) {
        if (table == null || Boolean.TRUE.equals(table.getClientProperty(INSTALLED_PROPERTY))) {
            return;
        }
        table.putClientProperty(INSTALLED_PROPERTY, Boolean.TRUE);

        JMenuItem exportItem = new JMenuItem("Xuất bảng ra PDF...");
        exportItem.addActionListener(event -> export(table, title));
        JPopupMenu popup = table.getComponentPopupMenu();
        if (popup == null) {
            popup = new JPopupMenu();
            table.setComponentPopupMenu(popup);
        }
        popup.add(exportItem);

        JMenuItem printItem = new JMenuItem("In bảng...");
        printItem.addActionListener(event -> print(table, title));
        popup.add(printItem);

        String actionKey = "exportTableToPdf";
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), actionKey);
        table.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                export(table, title);
            }
        });

        String printActionKey = "printTable";
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), printActionKey);
        table.getActionMap().put(printActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                print(table, title);
            }
        });

        table.addHierarchyListener(event -> {
            if ((event.getChangeFlags() & java.awt.event.HierarchyEvent.PARENT_CHANGED) != 0) {
                SwingUtilities.invokeLater(() -> attachButton(table, title));
            }
        });
        SwingUtilities.invokeLater(() -> attachButton(table, title));
    }

    private static void attachButton(JTable table, String title) {
        JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
        if (scroll == null || Boolean.TRUE.equals(scroll.getClientProperty(BUTTONS_ATTACHED_PROPERTY))) {
            return;
        }
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        buttons.setOpaque(false);

        JButton printButton = new JButton("In");
        printButton.setToolTipText("In dữ liệu đang hiển thị (Ctrl+Shift+P)");
        printButton.setFocusable(false);
        printButton.setMargin(new Insets(2, 8, 2, 8));
        printButton.addActionListener(event -> print(table, title));
        buttons.add(printButton);

        JButton pdfButton = new JButton("In PDF");
        pdfButton.setToolTipText("Lưu dữ liệu đang hiển thị thành tệp PDF (Ctrl+P)");
        pdfButton.setFocusable(false);
        pdfButton.setMargin(new Insets(2, 8, 2, 8));
        pdfButton.addActionListener(event -> export(table, title));
        buttons.add(pdfButton);

        Container parent = scroll.getParent();
        if (parent != null && parent.getLayout() instanceof BorderLayout) {
            BorderLayout layout = (BorderLayout) parent.getLayout();
            Object constraint = layout.getConstraints(scroll);
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(buttons, BorderLayout.NORTH);
            wrapper.add(scroll, BorderLayout.CENTER);

            parent.remove(scroll);
            parent.add(wrapper, constraint);
            scroll.putClientProperty(BUTTONS_ATTACHED_PROPERTY, Boolean.TRUE);
            parent.revalidate();
            parent.repaint();
        } else {
            JViewport columnHeader = scroll.getColumnHeader();
            Component header = columnHeader == null ? null : columnHeader.getView();
            JPanel headerWithButtons = new JPanel(new BorderLayout());
            headerWithButtons.add(header, BorderLayout.CENTER);
            headerWithButtons.add(buttons, BorderLayout.EAST);
            scroll.setColumnHeaderView(headerWithButtons);
            scroll.putClientProperty(BUTTONS_ATTACHED_PROPERTY, Boolean.TRUE);
            scroll.revalidate();
        }
    }

    private static void print(JTable table, String title) {
        String printTitle = title == null || title.trim().isEmpty() ? "Bảng dữ liệu" : title;
        try {
            table.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(printTitle),
                    new MessageFormat("Trang {0}"),
                    true, null, true);
        } catch (PrinterException e) {
            System.err.println("Không thể in bảng: " + e.getMessage());
            JOptionPane.showMessageDialog(table,
                    "Không thể in bảng: " + e.getMessage(),
                    "Lỗi in", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void export(JTable table, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu bảng PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(fileName(title)));
        if (chooser.showSaveDialog(table) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(table,
                "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String[] columns = visibleColumnNames(table);
            List<Object[]> rows = visibleRows(table);
            Account user = AuthController.getCurrentUser();
            String username = user == null || user.getUsername() == null
                    ? "Người dùng" : user.getUsername();
            ReportServices.exportTable(file, title,
                    "Dữ liệu đang hiển thị: " + rows.size() + " dòng",
                    columns, rows, "Tổng số dòng: " + rows.size(), username);
            JOptionPane.showMessageDialog(table,
                    "Đã xuất PDF thành công:\n" + file.getAbsolutePath(),
                    "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.err.println("Không thể xuất bảng PDF: " + e.getMessage());
            JOptionPane.showMessageDialog(table,
                    "Không thể xuất PDF: " + e.getMessage(),
                    "Lỗi xuất PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String[] visibleColumnNames(JTable table) {
        String[] columns = new String[table.getColumnCount()];
        TableModel model = table.getModel();
        for (int viewColumn = 0; viewColumn < columns.length; viewColumn++) {
            columns[viewColumn] = String.valueOf(
                    model.getColumnName(table.convertColumnIndexToModel(viewColumn)));
        }
        return columns;
    }

    private static List<Object[]> visibleRows(JTable table) {
        List<Object[]> rows = new ArrayList<>();
        TableModel model = table.getModel();
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            Object[] row = new Object[table.getColumnCount()];
            int modelRow = table.convertRowIndexToModel(viewRow);
            for (int viewColumn = 0; viewColumn < row.length; viewColumn++) {
                int modelColumn = table.convertColumnIndexToModel(viewColumn);
                row[viewColumn] = model.getValueAt(modelRow, modelColumn);
            }
            rows.add(row);
        }
        return rows;
    }

    private static String fileName(String title) {
        String safe = title == null ? "bang-du-lieu" : title
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9\\p{L}]+", "-")
                .replaceAll("^-|-$", "");
        return (safe.isEmpty() ? "bang-du-lieu" : safe) + ".pdf";
    }
}
