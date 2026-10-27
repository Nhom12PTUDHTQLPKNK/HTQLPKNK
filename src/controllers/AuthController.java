package controllers;

import models.entity.Account;
import models.entity.Role;

import java.util.ArrayList;
import java.util.List;

public class AuthController {

    private static Account currentUser;
    private static final List<Account> MOCK_ACCOUNTS = new ArrayList<>();

    static {
        // Khởi tạo sẵn tài khoản mẫu để test trực tiếp không cần CSDL
        MOCK_ACCOUNTS.add(new Account("admin", "123456", "NV001", Role.QUAN_LY));
        MOCK_ACCOUNTS.add(new Account("bansi01", "123456", "NV002", Role.NHA_SI));
        MOCK_ACCOUNTS.add(new Account("letan01", "123456", "NV003", Role.LE_TAN));
    }

    public boolean login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        String cleanUser = username.trim();
        String cleanPass = password.trim();

        for (Account acc : MOCK_ACCOUNTS) {
            if (acc.getUsername() != null && ((String) acc.getUsername()).equalsIgnoreCase(cleanUser)) {
                if (cleanPass.equals(acc.getPassword())) {
                    currentUser = acc;
                    return true;
                }
            }
        }

        // Chế độ dự phòng: Nhập bất kỳ tài khoản/mật khẩu nào khác cũng cho phép đăng nhập thành công
        currentUser = new Account(cleanUser, cleanPass, "NV999", Role.QUAN_LY);
        return true;
    }

    public static Account getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Account user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}