package Audit;

import sql.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
 * Nhật ký đăng nhập và thao tác được lưu trong bảng dbo.AuditLog.
 */
public final class AuditLog {

    private static final String TABLE = "dbo.AuditLog";
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
        ensureTable();
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
        String sql = "INSERT INTO " + TABLE +
                " (OccurredAt, SessionId, Username, UserRole, ActionName, Detail, DurationSeconds) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(e.time));
            ps.setString(2, e.session);
            ps.setString(3, e.username);
            ps.setString(4, e.role);
            ps.setString(5, e.action);
            ps.setString(6, e.detail);
            ps.setLong(7, e.seconds);
            ps.executeUpdate();
            ENTRIES.add(e);
        } catch (SQLException ex) {
            System.err.println("Không ghi được audit vào SQL Server: " + ex.getMessage());
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }

    private static void load() {
        ENTRIES.clear();
        String sql = "SELECT OccurredAt, SessionId, Username, UserRole, ActionName, Detail, DurationSeconds " +
                "FROM " + TABLE + " ORDER BY AuditId";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ENTRIES.add(new Entry(rs.getTimestamp("OccurredAt").toLocalDateTime(),
                        rs.getString("SessionId"), rs.getString("Username"), rs.getString("UserRole"),
                        rs.getString("ActionName"), rs.getString("Detail"), rs.getLong("DurationSeconds")));
            }
        } catch (SQLException ex) {
            System.err.println("Không tải được audit từ SQL Server: " + ex.getMessage());
        }
    }

    private static void ensureTable() {
        String sql = "IF OBJECT_ID('dbo.AuditLog', 'U') IS NULL BEGIN " +
                "CREATE TABLE dbo.AuditLog (" +
                "AuditId BIGINT IDENTITY(1,1) PRIMARY KEY, " +
                "OccurredAt DATETIME2 NOT NULL, SessionId VARCHAR(36) NOT NULL, " +
                "Username NVARCHAR(100) NOT NULL, UserRole NVARCHAR(100) NOT NULL, " +
                "ActionName NVARCHAR(80) NOT NULL, Detail NVARCHAR(MAX) NOT NULL, " +
                "DurationSeconds BIGINT NOT NULL) END";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException ex) {
            System.err.println("Không tạo/kiểm tra được bảng dbo.AuditLog: " + ex.getMessage());
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
        load();
        return new ArrayList<>(ENTRIES);
    }

    /** Danh sách phiên đăng nhập có ngày bắt đầu trong [a, b], mới nhất trước. */
    public static synchronized List<Object[]> sessionRows(LocalDate a, LocalDate b) {
        load();
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
        load();
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
        load();
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
