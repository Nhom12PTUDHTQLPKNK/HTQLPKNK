package controllers;

import controllers.AuthController;
import models.entity.Account;
import models.entity.Role;

/** Authorization checks shared by the UI and persistence layer. */
public final class AccessControl {
    private AccessControl() { }

    public static boolean isAdministrator() {
        Account account = AuthController.getCurrentUser();
        return account != null && ("admin".equalsIgnoreCase(account.getUsername())
                || account.role == Role.QUAN_LY);
    }

    public static boolean isCurrentAccount(String username) {
        Account account = AuthController.getCurrentUser();
        return account != null && account.getUsername() != null
                && account.getUsername().equalsIgnoreCase(username == null ? "" : username.trim());
    }

    public static boolean isProtectedAdministrator(String username, String role) {
        if ("admin".equalsIgnoreCase(username == null ? "" : username.trim())) return true;
        return Role.fromString(role) == Role.QUAN_LY;
    }
}
