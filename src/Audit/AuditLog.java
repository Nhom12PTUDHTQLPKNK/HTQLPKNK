package Audit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Audit: ghi lại đăng nhập (thành công / thất bại), đăng xuất, thời gian online của từng phiên
 * và các thao tác quan trọng. Dữ liệu lưu ở logs/audit.log (UTF-8, các cột cách nhau bằng TAB).
 * Không cần thư viện ngoài.
 */
public final class AuditLog {

    public static final Path FILE = Paths.get("logs", "audit.log");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Một dòng nhật ký. seconds = thời lượng phiên (chỉ có ở LOGOUT/EXIT), -1 nếu không áp dụng. */
    public static final class Entry {
        public final LocalDateTime time;
        public final String session, username, role, action, detail;
        public final long seconds;

        Entry(LocalDateTime time, String session, String username, String role,
              String action, String detail, long seconds) {
            this.time = time; this.session = session; this.username = username;
            this.role = role; this.action = action; this.detail = detail; this.seconds = seconds;
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    // phiên đăng nhập hiện tại
    private static String sid, suser, srole;
    private static LocalDateTime sstart;

    static {
        load();
        // Đóng cửa sổ / System.exit / Ctrl+C đều ghi nhận thời gian online của phiên đang mở
        Runtime.getRuntime().addShutdownHook(new Thread(() -> logout("EXIT", "Đóng ứng dụng")));
    }

    private AuditLog() { }

    // ------------------------------------------------------------------ ghi nhật ký
    public static synchronized void login(String username, String fullName, String role) {
        sid = UUID.randomUUID().toString().substring(0, 8);
        suser = username;
        srole = role;
        sstart = LocalDateTime.now();
        log(sid, username, role, "LOGIN_SUCCESS", "Đăng nhập: " + fullName, -1);
    }

    public static synchronized void failed(String username) {
        log("", username == null ? "" : username, "", "LOGIN_FAILED", "Sai tên đăng nhập hoặc mật khẩu", -1);
    }

    /** action: "LOGOUT" (chủ động đăng xuất) hoặc "EXIT" (đóng ứng dụng). */
    public static synchronized void logout(String action, String detail) {
        if (sid == null) return;
        log(sid, suser, srole, action, detail, elapsedSeconds());
        sid = null; suser = null; srole = null; sstart = null;
    }

    /** Ghi một thao tác trong phiên hiện tại (VIEW, CREATE, UPDATE, DELETE, PAYMENT, EXPORT_PDF...). */
    public static synchronized void event(String action, String detail) {
        log(sid == null ? "" : sid, suser == null ? "-" : suser, srole == null ? "" : srole, action, detail, -1);
    }

    private static synchronized void log(String session, String user, String role,
                                         String action, String detail, long secs) {
        Entry e = new Entry(LocalDateTime.now().withNano(0), clean(session), clean(user), clean(role),
                clean(action), clean(detail), secs);
        ENTRIES.add(e);
        try {
            Files.createDirectories(FILE.getParent());
            String line = String.join("\t", e.time.toString(), e.session, e.username, e.role,
                    e.action, e.detail, String.valueOf(e.seconds)) + System.lineSeparator();
            Files.write(FILE, line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("Không ghi được audit log: " + ex.getMessage());
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private static void load() {
        try {
            if (!Files.exists(FILE)) return;
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                String[] p = line.split("\t", -1);
                if (p.length < 7) continue;
                try {
                    ENTRIES.add(new Entry(LocalDateTime.parse(p[0]), p[1], p[2], p[3], p[4], p[5], Long.parseLong(p[6])));
                } catch (RuntimeException ignored) { /* bỏ qua dòng lỗi */ }
            }
        } catch (IOException ex) {
            System.err.println("Không đọc được audit log: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ phiên hiện tại
    public static synchronized long elapsedSeconds() {
        return sstart == null ? 0 : Duration.between(sstart, LocalDateTime.now()).getSeconds();
    }

    public static synchronized String loginTimeText() {
        return sstart == null ? "—" : sstart.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM"));
    }

    /** Tên tài khoản của phiên đang mở ("" nếu chưa đăng nhập). */
    public static synchronized String currentUser() {
        return suser == null ? "" : suser;
    }

    public static synchronized boolean isLoggedIn() {
        return sid != null;
    }

    public static String fmt(long secs) {
        return String.format("%02d:%02d:%02d", secs / 3600, (secs % 3600) / 60, secs % 60);
    }

    // ------------------------------------------------------------------ truy vấn để hiển thị / in báo cáo
    public static synchronized List<Entry> all() {
        return new ArrayList<>(ENTRIES);
    }

    /** Danh sách phiên đăng nhập có ngày bắt đầu trong [a, b], mới nhất trước. */
    public static synchronized List<Object[]> sessionRows(LocalDate a, LocalDate b) {
        Map<String, Object[]> m = new LinkedHashMap<>();   // session -> {user, role, start, end, secs, status}
        for (Entry e : ENTRIES) {
            if ("LOGIN_SUCCESS".equals(e.action)) {
                m.put(e.session, new Object[]{e.username, e.role, e.time, null, -1L, ""});
            } else if (("LOGOUT".equals(e.action) || "EXIT".equals(e.action)) && m.containsKey(e.session)) {
                Object[] r = m.get(e.session);
                r[3] = e.time;
                r[4] = e.seconds;
                r[5] = "LOGOUT".equals(e.action) ? "Đăng xuất" : "Đóng ứng dụng";
            }
        }
        List<Object[]> out = new ArrayList<>();
        for (Map.Entry<String, Object[]> en : m.entrySet()) {
            Object[] r = en.getValue();
            LocalDateTime st = (LocalDateTime) r[2];
            LocalDate d = st.toLocalDate();
            if (d.isBefore(a) || d.isAfter(b)) continue;
            boolean live = en.getKey().equals(sid);
            String end = r[3] == null ? "—" : ((LocalDateTime) r[3]).format(TS);
            long secs = live ? elapsedSeconds() : (Long) r[4];
            String status = live ? "🟢 Đang online"
                    : r[3] == null ? "⚠ Không ghi nhận thoát" : (String) r[5];
            out.add(new Object[]{r[0], r[1], st.format(TS), end, secs >= 0 ? fmt(secs) : "—", status});
        }
        Collections.reverse(out);
        return out;
    }

    private static final class Sum {
        int logins, fails;
        long secs;
        LocalDateTime last;
    }

    /** Tổng hợp theo tài khoản: số lần đăng nhập, số lần thất bại, tổng thời gian online, lần đăng nhập cuối. */
    public static synchronized List<Object[]> userSummary(LocalDate a, LocalDate b) {
        Map<String, Sum> m = new TreeMap<>();
        for (Entry e : ENTRIES) {
            LocalDate d = e.time.toLocalDate();
            if (d.isBefore(a) || d.isAfter(b) || e.username.isEmpty()) continue;
            Sum s = m.computeIfAbsent(e.username, k -> new Sum());
            switch (e.action) {
                case "LOGIN_SUCCESS": s.logins++; s.last = e.time; break;
                case "LOGIN_FAILED":  s.fails++; break;
                case "LOGOUT":
                case "EXIT":          s.secs += Math.max(0, e.seconds); break;
                default:              break;
            }
        }
        if (sid != null && !sstart.toLocalDate().isBefore(a) && !sstart.toLocalDate().isAfter(b))
            m.computeIfAbsent(suser, k -> new Sum()).secs += elapsedSeconds();   // phiên đang online

        List<Object[]> out = new ArrayList<>();
        for (Map.Entry<String, Sum> en : m.entrySet()) {
            Sum s = en.getValue();
            out.add(new Object[]{en.getKey(), s.logins, s.fails, fmt(s.secs),
                    s.last == null ? "—" : s.last.format(TS)});
        }
        return out;
    }

    /** Nhật ký thao tác (mọi dòng) có ngày trong [a, b], mới nhất trước: {thời gian, tài khoản, vai trò, hành động, chi tiết}. */
    public static synchronized List<Object[]> eventRows(LocalDate a, LocalDate b) {
        List<Object[]> out = new ArrayList<>();
        for (int i = ENTRIES.size() - 1; i >= 0; i--) {
            Entry e = ENTRIES.get(i);
            LocalDate d = e.time.toLocalDate();
            if (d.isBefore(a) || d.isAfter(b)) continue;
            out.add(new Object[]{e.time.format(TS), e.username, e.role, e.action, e.detail});
        }
        return out;
    }
}