package controllers;

import controllers.DieuKhienXacThuc;
import models.entity.TaiKhoanThongTin;
import models.entity.VaiTro;

/** Authorization checks shared by the UI and persistence layer. */
public final class KiemSoatTruyCap {
    private KiemSoatTruyCap() { }

    public static boolean isAdministrator() {
        TaiKhoanThongTin account = DieuKhienXacThuc.getCurrentUser();
        return account != null && ("admin".equalsIgnoreCase(account.getUsername())
                || account.role == VaiTro.QUAN_LY);
    }

    public static boolean isCurrentAccount(String username) {
        TaiKhoanThongTin account = DieuKhienXacThuc.getCurrentUser();
        return account != null && account.getUsername() != null
                && account.getUsername().equalsIgnoreCase(username == null ? "" : username.trim());
    }

    public static boolean isProtectedAdministrator(String username, String role) {
        if ("admin".equalsIgnoreCase(username == null ? "" : username.trim())) return true;
        return VaiTro.fromString(role) == VaiTro.QUAN_LY;
    }
}
