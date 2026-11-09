package controllers;

import controllers.DieuKhienXacThuc;
import models.entity.TaiKhoanThongTin;
import models.entity.TinhNang;
import models.dao.ChucVuDAO;
import java.sql.SQLException;
import java.util.Set;

/** Authorization checks shared by the UI and persistence layer. */
public final class KiemSoatTruyCap {
    private KiemSoatTruyCap() { }
    private static String cachedRole;
    private static Set<String> cachedPermissions = new java.util.HashSet<>();

    public static boolean isAdministrator() {
        TaiKhoanThongTin account = DieuKhienXacThuc.getCurrentUser();
        return account != null && "admin".equalsIgnoreCase(account.getUsername());
    }

    public static boolean hasPermission(TinhNang permission) {
        if (isAdministrator()) return true;
        TaiKhoanThongTin account = DieuKhienXacThuc.getCurrentUser();
        if (account == null || permission == null) return false;
        if (permission == TinhNang.HOME || permission == TinhNang.ACCOUNT || permission == TinhNang.HELP) return true;
        String role = account.getRole();
        synchronized (KiemSoatTruyCap.class) {
            if (role == null) return false;
            String cacheKey = account.getUsername() + "|" + role;
            if (!cacheKey.equals(cachedRole)) {
                cachedRole = cacheKey;
                try { cachedPermissions = new ChucVuDAO().getPermissions(role); }
                catch (SQLException ex) { cachedPermissions = new java.util.HashSet<>(); }
            }
            return cachedPermissions.contains(permission.name());
        }
    }

    public static synchronized void invalidatePermissionCache() {
        cachedRole = null;
        cachedPermissions = new java.util.HashSet<>();
    }

    public static boolean canAccessCard(String card) {
        if (card == null) return false;
        if ("ROLE_PERMISSION".equals(card) || "PERSONNEL_UPDATE".equals(card) || "JOB_TITLES".equals(card)) return isAdministrator();
        if ("DASHBOARD".equals(card)) return hasPermission(TinhNang.HOME);
        if ("CHANGE_PASS".equals(card)) return hasPermission(TinhNang.ACCOUNT);
        if ("HELP".equals(card)) return hasPermission(TinhNang.HELP);
        if ("LOGS".equals(card)) return isAdministrator();
        TinhNang feature = cardPermission(card);
        return feature != null && hasPermission(feature);
    }

    private static TinhNang cardPermission(String card) {
        switch (card) {
            case "SERVICES": case "SERVICE_TYPES": return TinhNang.D_GOI;
            case "PROMOTIONS": case "PROMO_PROCESS": case "SEARCH_PROMO": return card.equals("SEARCH_PROMO") ? TinhNang.S_KM : TinhNang.D_KM;
            case "CUSTOMERS": case "SEARCH_CUSTOMER": return TinhNang.S_KH;
            case "EMPLOYEES": return TinhNang.D_NV;
            case "MEDICAL_RECORDS": case "EXAMINATION": case "TREATMENT_PLAN": case "PRESCRIPTION": case "DENTAL_CHART": return TinhNang.D_BA;
            case "MEDICINES": case "MEDICINE_GROUPS": case "MEDICINE_FORMS": case "MEDICINE_ROUTES": case "INVENTORY_UPDATE": return TinhNang.D_KHO;
            case "APPOINTMENT_UPDATE": case "WORK_SHIFT": case "SEARCH_SHIFT": return TinhNang.D_LLV;
            case "BOOKING": case "SEARCH_APPOINTMENT": return TinhNang.BOOK;
            case "SALES_INVOICE": case "SEARCH_INVOICE": return TinhNang.PAY;
            case "SEARCH_INVENTORY": return TinhNang.S_KHO;
            case "REPORT_REVENUE": return TinhNang.ST_DT;
            case "REPORT_SERVICE": return TinhNang.ST_DV;
            case "REPORT_WAREHOUSE": return TinhNang.ST_KHO;
            default: return null;
        }
    }

    public static boolean isCurrentAccount(String username) {
        TaiKhoanThongTin account = DieuKhienXacThuc.getCurrentUser();
        return account != null && account.getUsername() != null
                && account.getUsername().equalsIgnoreCase(username == null ? "" : username.trim());
    }

    public static boolean isProtectedAdministrator(String username, String role) {
        if ("admin".equalsIgnoreCase(username == null ? "" : username.trim())) return true;
        return "admin".equalsIgnoreCase(role == null ? "" : role.trim());
    }
}
