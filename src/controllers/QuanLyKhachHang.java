package controllers;

/** Quy tắc nghiệp vụ xếp hạng và tích điểm khách hàng. */
public class QuanLyKhachHang {
    public static final int SILVER_POINTS = 100;
    public static final int GOLD_POINTS = 200;
    public QuanLyKhachHang() { }

    public String rank(int points) {
        return points >= GOLD_POINTS ? "Vàng" : points >= SILVER_POINTS ? "Bạc" : "Thành viên";
    }

    public int nextTierTarget(int points) {
        return points < SILVER_POINTS ? SILVER_POINTS : points < GOLD_POINTS ? GOLD_POINTS : 0;
    }

    public int pointsToNextTier(int points) {
        int target=nextTierTarget(points);
        return target==0?0:target-points;
    }
}
