package validation;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{3,30}$");
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L}\\s'.-]{1,79}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern SAFE_TEXT_PATTERN = Pattern.compile("^[\\p{L}0-9\\s._-]+$");

    private InputValidator() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static String validateRequired(String value, String fieldName) {
        String v = normalize(value);
        if (v.isEmpty()) return fieldName + " không được để trống.";
        return null;
    }

    public static String validateUsername(String value) {
        String v = normalize(value);
        if (v.isEmpty()) return "Tên đăng nhập không được để trống.";
        if (v.length() < 3 || v.length() > 30) {
            return "Tên đăng nhập phải có từ 3 đến 30 ký tự.";
        }
        if (!USERNAME_PATTERN.matcher(v).matches()) {
            return "Tên đăng nhập chỉ được chứa chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.";
        }
        return null;
    }

    public static String validatePassword(String value) {
        String v = normalize(value);
        if (v.isEmpty()) return "Mật khẩu không được để trống.";
        if (v.length() < 6 || v.length() > 32) {
            return "Mật khẩu phải có từ 6 đến 32 ký tự.";
        }
        if (v.contains(" ")) {
            return "Mật khẩu không được chứa khoảng trắng.";
        }
        return null;
    }

    public static String validateFullName(String value) {
        String v = normalize(value);
        if (v.isEmpty()) return "Họ tên không được để trống.";
        if (v.length() < 2 || v.length() > 80) {
            return "Họ tên phải có từ 2 đến 80 ký tự.";
        }
        if (!FULL_NAME_PATTERN.matcher(v).matches()) {
            return "Họ tên không hợp lệ. Chỉ chữ cái, khoảng trắng, dấu chấm, dấu nháy đơn và gạch ngang.";
        }
        return null;
    }

    public static String validatePhone(String value) {
        String v = normalize(value);
        if (v.isEmpty()) return "Số điện thoại không được để trống.";
        if (!PHONE_PATTERN.matcher(v).matches()) {
            return "Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.";
        }
        return null;
    }

    public static String validateBirthDate(LocalDate date) {
        if (date == null) return "Ngày sinh không được để trống.";
        if (!date.isBefore(LocalDate.now())) {
            return "Ngày sinh phải trước ngày hôm nay.";
        }
        return null;
    }

    public static String validateTextOnly(String value, String fieldName, int maxLength) {
        String v = normalize(value);
        if (v.isEmpty()) return fieldName + " không được để trống.";
        if (v.length() > maxLength) return fieldName + " quá dài. Tối đa " + maxLength + " ký tự.";
        if (!SAFE_TEXT_PATTERN.matcher(v).matches()) {
            return fieldName + " chỉ được chứa chữ, số, khoảng trắng, dấu chấm, gạch dưới hoặc gạch ngang.";
        }
        return null;
    }
}
