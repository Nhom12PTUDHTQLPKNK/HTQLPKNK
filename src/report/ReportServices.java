package report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import Audit.AuditLog;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Xuất báo cáo / hóa đơn ra PDF bằng iText 5 (com.itextpdf:itextpdf:5.5.13.3).
 */
public final class ReportServices {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BaseColor BLUE = new BaseColor(24, 144, 255);
    private static final BaseColor ZEBRA = new BaseColor(243, 246, 250);
    private static final BaseColor LINE = new BaseColor(210, 215, 225);
    private static BaseFont cached;

    private ReportServices() { }

    private static synchronized BaseFont baseFont() throws Exception {
        if (cached != null) return cached;

        String[] paths = {
                "fonts/arial.ttf", "fonts/Arial.ttf", "fonts/DejaVuSans.ttf",
                "C:/Windows/Fonts/arial.ttf", "C:/Windows/Fonts/tahoma.ttf", "C:/Windows/Fonts/times.ttf",
                "/Library/Fonts/Arial.ttf", "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };

        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) {
                cached = BaseFont.createFont(p, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return cached;
            }
        }

        String[] resourcePaths = {"/fonts/arial.ttf", "/fonts/Arial.ttf", "/fonts/DejaVuSans.ttf"};
        for (String res : resourcePaths) {
            try (InputStream is = ReportServices.class.getResourceAsStream(res)) {
                if (is != null) {
                    byte[] buffer = readAllBytesJava8(is);
                    cached = BaseFont.createFont(res, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, buffer, null);
                    return cached;
                }
            } catch (Exception ignored) { }
        }

        throw new IOException("Không tìm thấy font Unicode. Hãy chép arial.ttf vào thư mục fonts/ cạnh chương trình hoặc src/main/resources/fonts/.");
    }

    private static byte[] readAllBytesJava8(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private static class Footer extends PdfPageEventHelper {
        private final BaseFont bf;
        private final String text;
        Footer(BaseFont bf, String text) { this.bf = bf; this.text = text; }

        @Override 
        public void onEndPage(PdfWriter w, Document d) {
            PdfContentByte cb = w.getDirectContent();
            cb.beginText();
            cb.setFontAndSize(bf, 9);
            cb.showTextAligned(Element.ALIGN_LEFT, text, d.left(), d.bottom() - 18, 0);
            cb.showTextAligned(Element.ALIGN_RIGHT, "Trang " + w.getPageNumber(), d.right(), d.bottom() - 18, 0);
            cb.endText();
        }
    }

    private static PdfPCell cell(String text, Font f, int align, BaseColor bg, float pad) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(pad);
        c.setBorderColor(LINE);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    private static String text(Object o) {
        if (o == null) return "";
        if (o instanceof Number) {
            double v = ((Number) o).doubleValue();
            if (v == Math.rint(v)) return String.format("%,d", ((Number) o).longValue());
        }
        return String.valueOf(o).replaceAll("[\\x{1F000}-\\x{1FFFF}\\x{2600}-\\x{27BF}\\x{FE0F}\\x{200D}]", "").trim();
    }

    public static String money(int v) { return String.format("%,d đ", v); }

    // =================================================================== BÁO CÁO DẠNG BẢNG
    public static void exportTable(File file, String title, String subtitle, String[] cols,
                                   List<Object[]> rows, String summary, String user) throws Exception {
        BaseFont bf = baseFont();
        Font fClinic = new Font(bf, 12, Font.BOLD, new BaseColor(0, 102, 204));
        Font fTitle = new Font(bf, 18, Font.BOLD, BaseColor.BLACK);
        Font fSub = new Font(bf, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        Font fHead = new Font(bf, 10, Font.BOLD, BaseColor.WHITE);
        Font fBody = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);
        Font fBold = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);

        Document doc = new Document(cols.length > 5 ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 48);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter w = PdfWriter.getInstance(doc, fos);
            w.setPageEvent(new Footer(bf, "Phòng khám nha khoa — " + title));
            doc.open();

            doc.addTitle(title);
            doc.addAuthor(user);
            doc.addCreator("Hệ thống quản lý phòng khám nha khoa");

            Paragraph clinic = new Paragraph("PHÒNG KHÁM NHA KHOA", fClinic);
            clinic.setAlignment(Element.ALIGN_CENTER);
            doc.add(clinic);

            Paragraph t = new Paragraph(title.toUpperCase(), fTitle);
            t.setAlignment(Element.ALIGN_CENTER);
            t.setSpacingBefore(6);
            doc.add(t);

            if (subtitle != null && !subtitle.isEmpty()) {
                Paragraph s = new Paragraph(subtitle, fSub);
                s.setAlignment(Element.ALIGN_CENTER);
                doc.add(s);
            }
            Paragraph meta = new Paragraph("Người lập: " + user + "   •   Ngày lập: " + LocalDateTime.now().format(DT), fSub);
            meta.setAlignment(Element.ALIGN_CENTER);
            doc.add(meta);

            PdfPTable tb = new PdfPTable(cols.length);
            tb.setWidthPercentage(100);
            tb.setSpacingBefore(14);
            tb.setHeaderRows(1);
            float[] widths = new float[cols.length];
            for (int c = 0; c < cols.length; c++) {
                int len = cols[c].length();
                for (Object[] r : rows) {
                    if (r != null && c < r.length) {
                        len = Math.max(len, text(r[c]).length());
                    }
                }
                widths[c] = Math.max(6, Math.min(40, len));
            }
            tb.setWidths(widths);

            for (String c : cols) tb.addCell(cell(c, fHead, Element.ALIGN_CENTER, BLUE, 6));
            if (rows.isEmpty()) {
                PdfPCell none = cell("Không có dữ liệu", fBody, Element.ALIGN_CENTER, null, 8);
                none.setColspan(cols.length);
                tb.addCell(none);
            }
            int i = 0;
            for (Object[] r : rows) {
                BaseColor bg = (i++ % 2 == 1) ? ZEBRA : null;
                for (int c = 0; c < cols.length; c++) {
                    Object val = (r != null && c < r.length) ? r[c] : "";
                    tb.addCell(cell(text(val), fBody,
                            val instanceof Number ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, bg, 5));
                }
            }
            doc.add(tb);

            if (summary != null && !summary.isEmpty()) {
                Paragraph sm = new Paragraph(summary, fBold);
                sm.setSpacingBefore(10);
                doc.add(sm);
            }

            PdfPTable sig = new PdfPTable(2);
            sig.setWidthPercentage(100);
            sig.setSpacingBefore(26);
            sig.setKeepTogether(true);
            PdfPCell left = cell("Người lập biểu\n(Ký, ghi rõ họ tên)", fBody, Element.ALIGN_CENTER, null, 4);
            PdfPCell right = cell("Chủ phòng khám\n(Ký, đóng dấu)", fBody, Element.ALIGN_CENTER, null, 4);
            left.setBorder(0);
            right.setBorder(0);
            sig.addCell(left);
            sig.addCell(right);
            doc.add(sig);

            // Đóng Document NGAY TRONG khối try để hoàn tất ghi toàn bộ cấu trúc PDF
            doc.close();
        }

        AuditLog.event("EXPORT_PDF", "Báo cáo \"" + title + "\" -> " + file.getName());
    }

    // =================================================================== HÓA ĐƠN
    public static void exportInvoice(File file, String code, String date, String customer, String service,
                                     int price, String promo, int discount, int total,
                                     String method, String staff) throws Exception {
        BaseFont bf = baseFont();
        Font fClinic = new Font(bf, 14, Font.BOLD, new BaseColor(0, 102, 204));
        Font fTitle = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
        Font fBody = new Font(bf, 10, Font.NORMAL, BaseColor.BLACK);
        Font fBold = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);
        Font fHead = new Font(bf, 10, Font.BOLD, BaseColor.WHITE);

        String d = date;
        try { d = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
        catch (RuntimeException ignored) { }

        Document doc = new Document(PageSize.A5, 30, 30, 30, 40);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter w = PdfWriter.getInstance(doc, fos);
            w.setPageEvent(new Footer(bf, "Cảm ơn quý khách — Hóa đơn " + code));
            doc.open();

            doc.addTitle("Hóa đơn " + code);
            doc.addAuthor(staff);

            Paragraph clinic = new Paragraph("PHÒNG KHÁM NHA KHOA", fClinic);
            clinic.setAlignment(Element.ALIGN_CENTER);
            doc.add(clinic);
            Paragraph t = new Paragraph("HÓA ĐƠN THANH TOÁN", fTitle);
            t.setAlignment(Element.ALIGN_CENTER);
            t.setSpacingBefore(6);
            t.setSpacingAfter(10);
            doc.add(t);

            doc.add(new Paragraph("Mã hóa đơn: " + code, fBody));
            doc.add(new Paragraph("Ngày: " + d, fBody));
            doc.add(new Paragraph("Khách hàng: " + customer, fBody));
            doc.add(new Paragraph("Thu ngân: " + staff, fBody));
            doc.add(new Paragraph("Phương thức: " + method, fBody));

            PdfPTable tb = new PdfPTable(new float[]{3f, 1.5f});
            tb.setWidthPercentage(100);
            tb.setSpacingBefore(12);
            tb.addCell(cell("Nội dung", fHead, Element.ALIGN_CENTER, BLUE, 6));
            tb.addCell(cell("Số tiền", fHead, Element.ALIGN_CENTER, BLUE, 6));
            tb.addCell(cell(service, fBody, Element.ALIGN_LEFT, null, 6));
            tb.addCell(cell(money(price), fBody, Element.ALIGN_RIGHT, null, 6));
            tb.addCell(cell("Khuyến mãi: " + promo, fBody, Element.ALIGN_LEFT, ZEBRA, 6));
            tb.addCell(cell("- " + money(discount), fBody, Element.ALIGN_RIGHT, ZEBRA, 6));
            tb.addCell(cell("THÀNH TIỀN", fBold, Element.ALIGN_LEFT, null, 7));
            tb.addCell(cell(money(total), fBold, Element.ALIGN_RIGHT, null, 7));
            doc.add(tb);

            Paragraph thanks = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!", fBody);
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingBefore(18);
            doc.add(thanks);

            // Đóng Document NGAY TRONG khối try
            doc.close();
        }

        AuditLog.event("EXPORT_PDF", "Hóa đơn " + code + " -> " + file.getName());
    }

    // =================================================================== THẺ NHÂN VIÊN
    public static void exportEmployeeCard(File file, String username, String fullName, String role,
                                         String qrPayload, BufferedImage qrImage) throws Exception {
        BaseFont bf = baseFont();
        Font titleFont = new Font(bf, 18, Font.BOLD, BaseColor.BLACK);
        Font labelFont = new Font(bf, 10, Font.NORMAL, BaseColor.DARK_GRAY);
        Font valueFont = new Font(bf, 12, Font.BOLD, new BaseColor(0, 102, 204));
        Font smallFont = new Font(bf, 9, Font.NORMAL, BaseColor.BLACK);

        Document doc = new Document(PageSize.A6, 20, 20, 20, 20);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Paragraph clinic = new Paragraph("PHÒNG KHÁM NHA KHOA", titleFont);
            clinic.setAlignment(Element.ALIGN_CENTER);
            doc.add(clinic);
            doc.add(new Paragraph("THẺ NHÂN VIÊN", new Font(bf, 16, Font.BOLD, BaseColor.BLACK)));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            float[] widths = {2.3f, 1.7f};
            table.setWidths(widths);

            table.addCell(cell("Tên đăng nhập", labelFont, Element.ALIGN_LEFT, null, 5));
            table.addCell(cell(username, valueFont, Element.ALIGN_LEFT, null, 5));
            table.addCell(cell("Họ tên", labelFont, Element.ALIGN_LEFT, null, 5));
            table.addCell(cell(fullName, valueFont, Element.ALIGN_LEFT, null, 5));
            table.addCell(cell("Vai trò", labelFont, Element.ALIGN_LEFT, null, 5));
            table.addCell(cell(role, valueFont, Element.ALIGN_LEFT, null, 5));
            doc.add(table);

            PdfPTable qrTable = new PdfPTable(1);
            qrTable.setWidthPercentage(100);
            qrTable.setSpacingBefore(10);
            qrTable.setHorizontalAlignment(Element.ALIGN_CENTER);

            ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", qrOut);
            Image qr = Image.getInstance(qrOut.toByteArray());
            qr.scaleToFit(130f, 130f);
            PdfPCell qrCell = new PdfPCell(qr, true);
            qrCell.setBorder(PdfPCell.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrTable.addCell(qrCell);
            doc.add(qrTable);

            Paragraph payload = new Paragraph("QR Payload: " + qrPayload, smallFont);
            payload.setAlignment(Element.ALIGN_CENTER);
            doc.add(payload);
            doc.close();
        }
        AuditLog.event("EXPORT_PDF", "Thẻ nhân viên " + username + " -> " + file.getName());
    }
}