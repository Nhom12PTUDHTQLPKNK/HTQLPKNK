package schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dữ liệu ca làm việc dùng chung cho WorkProcessPanel và chức năng đặt lịch hẹn.
 *
 * Chức năng đặt lịch hẹn chỉ cần gọi:
 *   ShiftStore.getInstance().isStaffAvailable(staffId, date, start, end)
 *   ShiftStore.getInstance().availableStaff(Role.NHA_SI, date, start, end)
 * để biết nha sĩ / phụ tá có ca làm phủ kín khung giờ hẹn hay không
 * (việc kiểm tra trùng lịch hẹn khác vẫn do module đặt lịch đảm nhiệm).
 *
 * Danh sách nhân viên: dùng {@link #setStaff(Collection)} để nạp từ module tài khoản / nhân sự thật.
 */
public final class ShiftStore {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm");

    public enum Role {
        NHA_SI("Nha sĩ"), PHU_TA("Phụ tá"), LE_TAN("Lễ tân");
        private final String label;
        Role(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum Preset {
        SANG("Ca sáng (07:30 - 11:30)", LocalTime.of(7, 30), LocalTime.of(11, 30)),
        CHIEU("Ca chiều (13:30 - 17:30)", LocalTime.of(13, 30), LocalTime.of(17, 30)),
        TOI("Ca tối (17:30 - 20:30)", LocalTime.of(17, 30), LocalTime.of(20, 30)),
        TUY_CHON("Tùy chọn giờ", null, null);
        private final String label;
        private final LocalTime start, end;
        Preset(String label, LocalTime start, LocalTime end) { this.label = label; this.start = start; this.end = end; }
        public LocalTime getStart() { return start; }
        public LocalTime getEnd() { return end; }
        @Override public String toString() { return label; }
        /** Tìm preset khớp giờ; không khớp trả về TUY_CHON. */
        public static Preset of(LocalTime s, LocalTime e) {
            for (Preset p : values()) if (p.start != null && p.start.equals(s) && p.end.equals(e)) return p;
            return TUY_CHON;
        }
    }

    public static final class Staff {
        private final String id, name;
        private final Role role;
        public Staff(String id, String name, Role role) { this.id = id; this.name = name; this.role = role; }
        public String getId() { return id; }
        public String getName() { return name; }
        public Role getRole() { return role; }
        @Override public String toString() { return name + " (" + role.getLabel() + ")"; }
    }

    public static final class Shift {
        private final int id;
        private final String staffId, note;
        private final LocalDate date;
        private final LocalTime start, end;
        Shift(int id, String staffId, LocalDate date, LocalTime start, LocalTime end, String note) {
            this.id = id; this.staffId = staffId; this.date = date; this.start = start; this.end = end; this.note = note;
        }
        public int getId() { return id; }
        public String getStaffId() { return staffId; }
        public LocalDate getDate() { return date; }
        public LocalTime getStart() { return start; }
        public LocalTime getEnd() { return end; }
        public String getNote() { return note; }
        public double getHours() { return (end.toSecondOfDay() - start.toSecondOfDay()) / 3600.0; }
        public String timeRange() { return start.format(T) + " - " + end.format(T); }
    }

    private static final ShiftStore INSTANCE = new ShiftStore();
    public static ShiftStore getInstance() { return INSTANCE; }

    private final List<Staff> staff = new ArrayList<Staff>();
    private final List<Shift> shifts = new ArrayList<Shift>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<Runnable>();
    private int seq = 0;

    private ShiftStore() { seed(); }

    public void addListener(Runnable r) { if (r != null && !listeners.contains(r)) listeners.add(r); }
    public void removeListener(Runnable r) { listeners.remove(r); }
    private void fire() { for (Runnable r : listeners) r.run(); }

    // ------------------------------------------------------------------ nhân viên
    public synchronized void setStaff(Collection<Staff> list) {
        staff.clear();
        if (list != null) staff.addAll(list);
        fire();
    }
    public synchronized List<Staff> staff() { return new ArrayList<Staff>(staff); }
    public synchronized Staff staffById(String id) {
        for (Staff s : staff) if (s.getId().equals(id)) return s;
        return null;
    }

    // ------------------------------------------------------------------ ca làm việc
    public synchronized Shift addShift(String staffId, LocalDate date, LocalTime start, LocalTime end, String note) {
        validate(staffId, date, start, end, -1);
        Shift s = new Shift(++seq, staffId, date, start, end, note == null ? "" : note.trim());
        shifts.add(s);
        fire();
        return s;
    }

    public synchronized Shift updateShift(int id, String staffId, LocalDate date, LocalTime start, LocalTime end, String note) {
        int idx = indexOf(id);
        if (idx < 0) throw bad("Không tìm thấy ca làm việc #" + id + ".");
        validate(staffId, date, start, end, id);
        Shift s = new Shift(id, staffId, date, start, end, note == null ? "" : note.trim());
        shifts.set(idx, s);
        fire();
        return s;
    }

    public synchronized Shift deleteShift(int id) {
        int idx = indexOf(id);
        if (idx < 0) throw bad("Không tìm thấy ca làm việc #" + id + ".");
        Shift s = shifts.remove(idx);
        fire();
        return s;
    }

    public synchronized Shift shift(int id) {
        int i = indexOf(id);
        return i < 0 ? null : shifts.get(i);
    }

    /** Ca của CÙNG nhân viên, CÙNG ngày, giao với khung giờ [start, end) (bỏ qua ca excludeId). Không có → null. */
    public synchronized Shift findConflict(String staffId, LocalDate date, LocalTime start, LocalTime end, int excludeId) {
        for (Shift s : shifts) {
            if (s.getId() == excludeId) continue;
            if (s.getStaffId().equals(staffId) && s.getDate().equals(date)
                    && start.isBefore(s.getEnd()) && s.getStart().isBefore(end)) return s;
        }
        return null;
    }

    /** Ca trong [from, to], lọc theo chức vụ / nhân viên (null = tất cả), sắp theo ngày, giờ. */
    public synchronized List<Shift> shifts(LocalDate from, LocalDate to, Role role, String staffId) {
        List<Shift> out = new ArrayList<Shift>();
        for (Shift s : shifts) {
            if (s.getDate().isBefore(from) || s.getDate().isAfter(to)) continue;
            if (staffId != null && !s.getStaffId().equals(staffId)) continue;
            if (role != null) { Staff st = staffById(s.getStaffId()); if (st == null || st.getRole() != role) continue; }
            out.add(s);
        }
        Collections.sort(out, new Comparator<Shift>() {
            @Override public int compare(Shift a, Shift b) {
                int c = a.getDate().compareTo(b.getDate());
                if (c == 0) c = a.getStart().compareTo(b.getStart());
                if (c == 0) c = a.getStaffId().compareTo(b.getStaffId());
                return c;
            }
        });
        return out;
    }

    // ------------------------------------------------------------------ API cho đặt lịch hẹn
    /** true nếu các ca của nhân viên trong ngày phủ kín toàn bộ khung giờ [start, end]. Ca liền kề được ghép lại. */
    public synchronized boolean isStaffAvailable(String staffId, LocalDate date, LocalTime start, LocalTime end) {
        if (staffId == null || date == null || start == null || end == null || !end.isAfter(start)) return false;
        List<Shift> mine = shifts(date, date, null, staffId);
        LocalTime cursor = start;
        for (Shift s : mine) { // đã sắp theo giờ bắt đầu
            if (!s.getStart().isAfter(cursor) && s.getEnd().isAfter(cursor)) cursor = s.getEnd();
            if (!cursor.isBefore(end)) return true;
        }
        return !cursor.isBefore(end);
    }

    /** Danh sách nhân viên (theo chức vụ nếu role != null) có ca phủ kín khung giờ. */
    public synchronized List<Staff> availableStaff(Role role, LocalDate date, LocalTime start, LocalTime end) {
        List<Staff> out = new ArrayList<Staff>();
        for (Staff s : staff) {
            if (role != null && s.getRole() != role) continue;
            if (isStaffAvailable(s.getId(), date, start, end)) out.add(s);
        }
        return out;
    }

    // ------------------------------------------------------------------ nội bộ
    private void validate(String staffId, LocalDate date, LocalTime start, LocalTime end, int excludeId) {
        Staff st = staffById(staffId);
        if (st == null) throw bad("Vui lòng chọn nhân viên hợp lệ.");
        if (date == null) throw bad("Vui lòng nhập ngày làm việc (dd/MM/yyyy).");
        if (start == null || end == null) throw bad("Vui lòng nhập giờ bắt đầu và giờ kết thúc (HH:mm).");
        if (!end.isAfter(start)) throw bad("Giờ kết thúc phải sau giờ bắt đầu.");
        Shift c = findConflict(staffId, date, start, end, excludeId);
        if (c != null)
            throw bad(st.getName() + " đã có ca " + c.timeRange() + " ngày " + c.getDate().format(D)
                    + " (mã ca " + c.getId() + ") trùng với khung giờ " + start.format(T) + " - " + end.format(T) + ".");
    }

    private int indexOf(int id) {
        for (int i = 0; i < shifts.size(); i++) if (shifts.get(i).getId() == id) return i;
        return -1;
    }

    private static IllegalArgumentException bad(String m) { return new IllegalArgumentException(m); }

    private void seed() {
        staff.add(new Staff("NV01", "BS. Trần Hoàng Nam", Role.NHA_SI));
        staff.add(new Staff("NV02", "BS. Lê Thu Hà", Role.NHA_SI));
        staff.add(new Staff("NV03", "Phạm Thị Lan", Role.PHU_TA));
        staff.add(new Staff("NV04", "Nguyễn Văn Khoa", Role.PHU_TA));
        staff.add(new Staff("NV05", "Võ Thanh Mai", Role.LE_TAN));
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        for (int d = 0; d < 6; d++) {
            LocalDate day = monday.plusDays(d);
            seedShift("NV01", day, Preset.SANG);
            seedShift("NV03", day, Preset.SANG);
            seedShift("NV02", day, Preset.CHIEU);
            seedShift("NV04", day, Preset.CHIEU);
            seedShift("NV05", day, Preset.SANG);
            seedShift("NV05", day, Preset.CHIEU);
        }
    }

    private void seedShift(String id, LocalDate d, Preset p) {
        shifts.add(new Shift(++seq, id, d, p.getStart(), p.getEnd(), ""));
    }
}
