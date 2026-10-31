package inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InventoryStore {

    public static final String NO_LOT = "-";
    public static final int NEAR_EXPIRY_DAYS = 30;
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public enum Category {
        THUOC("Thuốc"), VAT_TU("Vật tư"), THIET_BI("Thiết bị");
        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum TxnType {
        NHAP("Nhập kho"), XUAT("Xuất kho"), DIEU_CHINH("Điều chỉnh");
        private final String label;
        TxnType(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum TxnStatus {
        CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_HUY("Đã hủy");
        private final String label;
        TxnStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    public enum StockStatus {
        BINH_THUONG("Bình thường"), SAP_HET("Sắp hết"), HET_HANG("Hết hàng"),
        GAN_HET_HAN("Gần hết hạn"), HET_HAN("Hết hạn");
        private final String label;
        StockStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    // ------------------------------------------------------------------ model
    public static final class Item {
        private final String code, name, unit;
        private final Category category;
        private final int minStock;
        Item(String code, String name, Category category, String unit, int minStock) {
            this.code = code; this.name = name; this.category = category; this.unit = unit; this.minStock = minStock;
        }
        public String getCode() { return code; }
        public String getName() { return name; }
        public Category getCategory() { return category; }
        public String getUnit() { return unit; }
        public int getMinStock() { return minStock; }
        @Override public String toString() { return code + " - " + name; }
    }

    public static final class Txn {
        private final String id, itemCode, lotNo, reason, createdBy;
        private final TxnType type;
        private final LocalDate expiry;
        private final int qty;
        private final LocalDateTime createdAt;
        private TxnStatus status = TxnStatus.CHO_XAC_NHAN;
        private LocalDateTime confirmedAt;
        private String confirmedBy = "";

        Txn(String id, TxnType type, String itemCode, String lotNo, LocalDate expiry, int qty,
            String reason, String createdBy, LocalDateTime createdAt) {
            this.id = id; this.type = type; this.itemCode = itemCode; this.lotNo = lotNo; this.expiry = expiry;
            this.qty = qty; this.reason = reason; this.createdBy = createdBy; this.createdAt = createdAt;
        }
        void markConfirmed(LocalDateTime at, String by) { status = TxnStatus.DA_XAC_NHAN; confirmedAt = at; confirmedBy = by; }
        void markCancelled(LocalDateTime at, String by) { status = TxnStatus.DA_HUY; confirmedAt = at; confirmedBy = by; }

        public String getId() { return id; }
        public TxnType getType() { return type; }
        public String getItemCode() { return itemCode; }
        public String getLotNo() { return lotNo; }
        public LocalDate getExpiry() { return expiry; }
        public int getQty() { return qty; }
        public String getReason() { return reason; }
        public String getCreatedBy() { return createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public TxnStatus getStatus() { return status; }
        public LocalDateTime getConfirmedAt() { return confirmedAt; }
        public String getConfirmedBy() { return confirmedBy; }
        public int getDelta() { return type == TxnType.XUAT ? -qty : qty; }
    }

    public static final class StockRow {
        private final Item item;
        private final String lotNo;
        private final LocalDate expiry;
        private final int lotQty, totalQty;
        private final StockStatus status;
        StockRow(Item item, String lotNo, LocalDate expiry, int lotQty, int totalQty, StockStatus status) {
            this.item = item; this.lotNo = lotNo; this.expiry = expiry;
            this.lotQty = lotQty; this.totalQty = totalQty; this.status = status;
        }
        public Item getItem() { return item; }
        public String getLotNo() { return lotNo; }
        public LocalDate getExpiry() { return expiry; }
        public int getLotQty() { return lotQty; }
        public int getTotalQty() { return totalQty; }
        public StockStatus getStatus() { return status; }
    }

    public static final class FlowRow {
        private final Item item;
        private final int opening, in, out, adjust;
        FlowRow(Item item, int opening, int in, int out, int adjust) {
            this.item = item; this.opening = opening; this.in = in; this.out = out; this.adjust = adjust;
        }
        public Item getItem() { return item; }
        public int getOpening() { return opening; }
        public int getIn() { return in; }
        public int getOut() { return out; }
        public int getAdjust() { return adjust; }
        public int getClosing() { return opening + in - out + adjust; }
    }

    public static final class AlertSummary {
        public final int outOfStock, lowStock, nearExpiry, expired;
        AlertSummary(int outOfStock, int lowStock, int nearExpiry, int expired) {
            this.outOfStock = outOfStock; this.lowStock = lowStock; this.nearExpiry = nearExpiry; this.expired = expired;
        }
        public boolean isEmpty() { return outOfStock + lowStock + nearExpiry + expired == 0; }
    }

    private static final InventoryStore INSTANCE = new InventoryStore();
    public static InventoryStore getInstance() { return INSTANCE; }

    private final Map<String, Item> items = new LinkedHashMap<String, Item>();
    private final Map<String, LocalDate> lotExpiry = new HashMap<String, LocalDate>();
    private final List<Txn> txns = new ArrayList<Txn>();
    private final Map<TxnType, Integer> counters = new EnumMap<TxnType, Integer>(TxnType.class);
    private final List<Runnable> listeners = new CopyOnWriteArrayList<Runnable>();

    private InventoryStore() { seed(); }

    public void addListener(Runnable r) { if (r != null && !listeners.contains(r)) listeners.add(r); }
    public void removeListener(Runnable r) { listeners.remove(r); }
    private void fire() { for (Runnable r : listeners) r.run(); }

    public synchronized Item addItem(String code, String name, Category cat, String unit, int minStock) {
        String c = normCode(code);
        if (c.isEmpty() || name == null || name.trim().isEmpty() || unit == null || unit.trim().isEmpty() || cat == null)
            throw bad("Mã, tên, loại và đơn vị tính không được để trống.");
        if (items.containsKey(c)) throw bad("Mã hàng đã tồn tại: " + c);
        Item it = new Item(c, name.trim(), cat, unit.trim(), Math.max(0, minStock));
        items.put(c, it);
        fire();
        return it;
    }

    public synchronized List<Item> items() { return new ArrayList<Item>(items.values()); }
    public synchronized Item item(String code) { return items.get(normCode(code)); }

    public synchronized Txn create(TxnType type, String itemCode, String lotNo, LocalDate expiry,
                                   int qty, String reason, String user) {
        if (type == null) throw bad("Chưa chọn loại giao dịch.");
        Item it = requireItem(itemCode);
        String lot = normLot(lotNo);
        String why = reason == null ? "" : reason.trim();
        if (type == TxnType.DIEU_CHINH) {
            if (qty == 0) throw bad("Số lượng điều chỉnh phải khác 0.");
            if (why.isEmpty()) throw bad("Điều chỉnh tồn kho bắt buộc phải có lý do.");
        } else {
            if (qty <= 0) throw bad("Số lượng phải lớn hơn 0.");
            if (type == TxnType.XUAT && why.isEmpty()) throw bad("Xuất kho bắt buộc phải ghi lý do / bộ phận nhận.");
        }
        Txn t = new Txn(nextId(type), type, it.getCode(), lot, expiry, qty, why, nonEmpty(user), LocalDateTime.now());
        validate(t);
        txns.add(t);
        fire();
        return t;
    }

    public synchronized Txn confirm(String id, String user) {
        Txn t = requireTxn(id);
        if (t.getStatus() != TxnStatus.CHO_XAC_NHAN)
            throw bad("Giao dịch " + t.getId() + " đang ở trạng thái \"" + t.getStatus().getLabel() + "\", không thể xác nhận.");
        validate(t); 
        String k = key(t.getItemCode(), t.getLotNo());
        if (t.getExpiry() != null && !lotExpiry.containsKey(k)) lotExpiry.put(k, t.getExpiry());
        t.markConfirmed(LocalDateTime.now(), nonEmpty(user));
        fire();
        return t;
    }

    public synchronized Txn cancel(String id, String user) {
        Txn t = requireTxn(id);
        if (t.getStatus() != TxnStatus.CHO_XAC_NHAN)
            throw bad("Chỉ hủy được giao dịch đang chờ xác nhận.");
        t.markCancelled(LocalDateTime.now(), nonEmpty(user));
        fire();
        return t;
    }

    public synchronized List<Txn> transactions() { return new ArrayList<Txn>(txns); }

    public synchronized Txn transaction(String id) {
        for (Txn t : txns) if (t.getId().equalsIgnoreCase(id)) return t;
        return null;
    }

    public synchronized List<Txn> confirmedBetween(LocalDate from, LocalDate to, String itemCode) {
        String c = itemCode == null ? null : normCode(itemCode);
        List<Txn> out = new ArrayList<Txn>();
        for (Txn t : txns) {
            if (t.getStatus() != TxnStatus.DA_XAC_NHAN) continue;
            if (c != null && !t.getItemCode().equals(c)) continue;
            LocalDate d = t.getConfirmedAt().toLocalDate();
            if (!d.isBefore(from) && !d.isAfter(to)) out.add(t);
        }
        Collections.sort(out, new Comparator<Txn>() {
            @Override public int compare(Txn a, Txn b) { return a.getConfirmedAt().compareTo(b.getConfirmedAt()); }
        });
        return out;
    }

    public synchronized int lotQty(String itemCode, String lotNo) {
        String c = normCode(itemCode), l = normLot(lotNo);
        int sum = 0;
        for (Txn t : txns)
            if (t.getStatus() == TxnStatus.DA_XAC_NHAN && t.getItemCode().equals(c) && t.getLotNo().equals(l))
                sum += t.getDelta();
        return sum;
    }

    public synchronized int totalQty(String itemCode) {
        String c = normCode(itemCode);
        int sum = 0;
        for (Txn t : txns)
            if (t.getStatus() == TxnStatus.DA_XAC_NHAN && t.getItemCode().equals(c)) sum += t.getDelta();
        return sum;
    }

    public synchronized LocalDate expiryOf(String itemCode, String lotNo) {
        return lotExpiry.get(key(normCode(itemCode), normLot(lotNo)));
    }

    public synchronized List<String> lotsOf(String itemCode, boolean onlyInStock) {
        final String c = normCode(itemCode);
        Map<String, Integer> byLot = new LinkedHashMap<String, Integer>();
        for (Txn t : txns)
            if (t.getStatus() == TxnStatus.DA_XAC_NHAN && t.getItemCode().equals(c)) merge(byLot, t.getLotNo(), t.getDelta());
        List<String> lots = new ArrayList<String>();
        for (Map.Entry<String, Integer> e : byLot.entrySet())
            if (!onlyInStock || e.getValue() > 0) lots.add(e.getKey());
        Collections.sort(lots, new Comparator<String>() {
            @Override public int compare(String a, String b) { return compareLots(lotExpiry.get(key(c, a)), a, lotExpiry.get(key(c, b)), b); }
        });
        return lots;
    }

    public synchronized List<StockRow> stock() {
        LocalDate today = LocalDate.now();
        Map<String, Map<String, Integer>> byItem = new HashMap<String, Map<String, Integer>>();
        for (Txn t : txns) {
            if (t.getStatus() != TxnStatus.DA_XAC_NHAN) continue;
            Map<String, Integer> lots = byItem.get(t.getItemCode());
            if (lots == null) { lots = new LinkedHashMap<String, Integer>(); byItem.put(t.getItemCode(), lots); }
            merge(lots, t.getLotNo(), t.getDelta());
        }
        List<StockRow> rows = new ArrayList<StockRow>();
        for (final Item it : items.values()) {
            Map<String, Integer> lots = byItem.get(it.getCode());
            int total = 0;
            if (lots != null) for (int v : lots.values()) total += v;
            List<StockRow> part = new ArrayList<StockRow>();
            if (lots != null) {
                for (Map.Entry<String, Integer> e : lots.entrySet()) {
                    if (e.getValue() <= 0) continue;
                    LocalDate exp = lotExpiry.get(key(it.getCode(), e.getKey()));
                    part.add(new StockRow(it, e.getKey(), exp, e.getValue(), total, statusOf(it, total, e.getValue(), exp, today)));
                }
            }
            Collections.sort(part, new Comparator<StockRow>() {
                @Override public int compare(StockRow a, StockRow b) { return compareLots(a.getExpiry(), a.getLotNo(), b.getExpiry(), b.getLotNo()); }
            });
            if (part.isEmpty()) part.add(new StockRow(it, "", null, 0, total, statusOf(it, total, 0, null, today)));
            rows.addAll(part);
        }
        return rows;
    }

    public synchronized AlertSummary alerts() {
        int out = 0, low = 0, near = 0, exp = 0;
        java.util.Set<String> seenOut = new java.util.HashSet<String>(), seenLow = new java.util.HashSet<String>();
        for (StockRow r : stock()) {
            if (r.getStatus() == StockStatus.HET_HAN) exp++;
            else if (r.getStatus() == StockStatus.GAN_HET_HAN) near++;
            String code = r.getItem().getCode();
            if (r.getTotalQty() <= 0) { if (seenOut.add(code)) out++; }
            else if (r.getTotalQty() <= r.getItem().getMinStock()) { if (seenLow.add(code)) low++; }
        }
        return new AlertSummary(out, low, near, exp);
    }

    public synchronized List<FlowRow> flow(LocalDate from, LocalDate to, String itemCode) {
        if (from == null || to == null || to.isBefore(from)) throw bad("Khoảng thời gian không hợp lệ.");
        String c = itemCode == null ? null : normCode(itemCode);
        Map<String, int[]> acc = new LinkedHashMap<String, int[]>();
        for (Item it : items.values()) if (c == null || it.getCode().equals(c)) acc.put(it.getCode(), new int[4]);
        for (Txn t : txns) {
            if (t.getStatus() != TxnStatus.DA_XAC_NHAN) continue;
            int[] a = acc.get(t.getItemCode());
            if (a == null) continue;
            LocalDate d = t.getConfirmedAt().toLocalDate();
            if (d.isBefore(from)) a[0] += t.getDelta();
            else if (!d.isAfter(to)) {
                if (t.getType() == TxnType.NHAP) a[1] += t.getQty();
                else if (t.getType() == TxnType.XUAT) a[2] += t.getQty();
                else a[3] += t.getQty();
            }
        }
        List<FlowRow> rows = new ArrayList<FlowRow>();
        for (Map.Entry<String, int[]> e : acc.entrySet()) {
            int[] a = e.getValue();
            rows.add(new FlowRow(items.get(e.getKey()), a[0], a[1], a[2], a[3]));
        }
        return rows;
    }

    private void validate(Txn t) {
        LocalDate today = LocalDate.now();
        String code = t.getItemCode(), lot = t.getLotNo();
        LocalDate known = lotExpiry.get(key(code, lot));
        int delta = t.getDelta();
        if (delta > 0) {
            if (known != null && t.getExpiry() != null && !known.equals(t.getExpiry()))
                throw bad("Lô " + lotLabel(lot) + " đã có hạn sử dụng " + known.format(D) + ", không khớp với " + t.getExpiry().format(D) + ".");
            LocalDate eff = known != null ? known : t.getExpiry();
            if (t.getType() == TxnType.NHAP && eff != null && eff.isBefore(today))
                throw bad("Không thể nhập lô đã quá hạn sử dụng (" + eff.format(D) + ").");
        } else {
            int have = lotQty(code, lot);
            if (have + delta < 0)
                throw bad("Lô " + lotLabel(lot) + " của " + code + " chỉ còn " + have + ", không đủ để "
                        + (t.getType() == TxnType.XUAT ? "xuất " : "giảm ") + (-delta) + ".");
            if (t.getType() == TxnType.XUAT && known != null && known.isBefore(today))
                throw bad("Lô " + lotLabel(lot) + " đã hết hạn từ " + known.format(D)
                        + ", không được xuất dùng. Hãy dùng \"Điều chỉnh\" giảm kèm lý do hủy hàng.");
        }
    }

    private static String lotLabel(String lot) { return NO_LOT.equals(lot) ? "(không có lô)" : lot; }
    private static IllegalArgumentException bad(String msg) { return new IllegalArgumentException(msg); }
    private static String key(String itemCode, String lot) { return itemCode + "|" + lot; }
    private static String normCode(String s) { return s == null ? "" : s.trim().toUpperCase(); }
    private static String normLot(String s) { String v = s == null ? "" : s.trim().toUpperCase(); return v.isEmpty() ? NO_LOT : v; }
    private static String nonEmpty(String s) { return s == null || s.trim().isEmpty() ? "Hệ thống" : s.trim(); }
    private static void merge(Map<String, Integer> m, String k, int v) { Integer o = m.get(k); m.put(k, (o == null ? 0 : o) + v); }

    private static int compareLots(LocalDate ea, String la, LocalDate eb, String lb) {
        if (ea == null && eb != null) return 1;
        if (ea != null && eb == null) return -1;
        if (ea != null) { int c = ea.compareTo(eb); if (c != 0) return c; }
        return la.compareTo(lb);
    }

    private static StockStatus statusOf(Item it, int total, int lotQty, LocalDate exp, LocalDate today) {
        if (lotQty > 0 && exp != null) {
            if (exp.isBefore(today)) return StockStatus.HET_HAN;
            if (!exp.isAfter(today.plusDays(NEAR_EXPIRY_DAYS))) return StockStatus.GAN_HET_HAN;
        }
        if (total <= 0) return StockStatus.HET_HANG;
        if (total <= it.getMinStock()) return StockStatus.SAP_HET;
        return StockStatus.BINH_THUONG;
    }

    private Item requireItem(String code) {
        Item it = items.get(normCode(code));
        if (it == null) throw bad("Không tìm thấy mặt hàng: " + code);
        return it;
    }

    private Txn requireTxn(String id) {
        Txn t = transaction(id);
        if (t == null) throw bad("Không tìm thấy giao dịch: " + id);
        return t;
    }

    private String nextId(TxnType type) {
        Integer n = counters.get(type);
        int next = (n == null ? 0 : n) + 1;
        counters.put(type, next);
        String prefix = type == TxnType.NHAP ? "PN" : type == TxnType.XUAT ? "PX" : "DC";
        return String.format("%s%04d", prefix, next);
    }

    private void seed() {
        LocalDate today = LocalDate.now();
        put("TH001", "Amoxicillin 500mg", Category.THUOC, "Viên", 200);
        put("TH002", "Metronidazole 250mg", Category.THUOC, "Viên", 150);
        put("TH004", "Paracetamol 500mg", Category.THUOC, "Viên", 300);
        put("TH009", "Lidocaine 2%", Category.THUOC, "Ống tiêm", 50);
        put("VT001", "Găng tay y tế", Category.VAT_TU, "Hộp", 20);
        put("VT002", "Khẩu trang y tế", Category.VAT_TU, "Hộp", 10);
        put("VT003", "Bông gòn nha khoa", Category.VAT_TU, "Gói", 30);
        put("TB001", "Máy lấy cao răng siêu âm", Category.THIET_BI, "Cái", 1);
        put("TB002", "Đèn trám quang trùng hợp", Category.THIET_BI, "Cái", 1);

        seedTxn(TxnType.NHAP, "TH001", "A101", today.plusDays(400), 1200, "Công ty Dược phẩm ABC", 60, true);
        seedTxn(TxnType.NHAP, "TH001", "A102", today.plusDays(20), 100, "Công ty Dược phẩm ABC", 45, true);
        seedTxn(TxnType.NHAP, "TH002", "MT2301", today.plusDays(300), 220, "Công ty Dược phẩm ABC", 60, true);
        seedTxn(TxnType.XUAT, "TH002", "MT2301", null, 100, "Phòng khám nha sĩ 1", 20, true);
        seedTxn(TxnType.NHAP, "TH004", "PA0907", today.minusDays(10), 40, "Công ty Dược phẩm XYZ", 90, true);
        seedTxn(TxnType.NHAP, "TH004", "PA1105", today.plusDays(500), 500, "Công ty Dược phẩm XYZ", 40, true);
        seedTxn(TxnType.NHAP, "TH009", "LD77", today.plusDays(200), 60, "Công ty Dược phẩm ABC", 50, true);
        seedTxn(TxnType.XUAT, "TH009", "LD77", null, 60, "Phòng khám nha sĩ 2", 15, true);
        seedTxn(TxnType.NHAP, "VT001", "GT01", today.plusDays(365), 55, "Công ty Vật tư Y tế Việt", 60, true);
        seedTxn(TxnType.XUAT, "VT001", "GT01", null, 5, "Sử dụng khám bệnh", 10, true);
        seedTxn(TxnType.NHAP, "VT002", "KT05", today.plusDays(25), 8, "Công ty Vật tư Y tế Việt", 30, true);
        seedTxn(TxnType.NHAP, "VT003", "", null, 100, "Công ty Vật tư Y tế Việt", 35, true);
        seedTxn(TxnType.NHAP, "TB001", "", null, 3, "Mua mới thiết bị", 80, true);
        seedTxn(TxnType.NHAP, "TB002", "", null, 2, "Mua mới thiết bị", 80, true);
        seedTxn(TxnType.DIEU_CHINH, "TB002", "", null, -1, "Hỏng, thanh lý", 5, true);
        seedTxn(TxnType.NHAP, "TH002", "MT2302", today.plusDays(400), 200, "Công ty Dược phẩm ABC", 0, false);
    }

    private void put(String code, String name, Category cat, String unit, int min) {
        items.put(code, new Item(code, name, cat, unit, min));
    }

    private void seedTxn(TxnType type, String code, String lot, LocalDate exp, int qty, String reason, int daysAgo, boolean confirmed) {
        LocalDateTime at = LocalDateTime.now().minusDays(daysAgo).withHour(9).withMinute(0).withSecond(0).withNano(0);
        if (daysAgo == 0) at = LocalDateTime.now().minusMinutes(30);
        Txn t = new Txn(nextId(type), type, code, normLot(lot), exp, qty, reason, "Hệ thống", at);
        txns.add(t);
        if (confirmed) {
            String k = key(code, t.getLotNo());
            if (exp != null && !lotExpiry.containsKey(k)) lotExpiry.put(k, exp);
            t.markConfirmed(at.plusMinutes(5), "Hệ thống");
        }
    }
}
