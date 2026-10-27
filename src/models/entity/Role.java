package models.entity;

/**
 * Enum định nghĩa các vai trò người dùng trong hệ thống
 */
public enum Role {
    NHA_SI("Nha sĩ"),
    LE_TAN("Lễ tân"),
    Y_TA("Y tá"),
    QUAN_LY("Chủ phòng khám (Quản lý)");

    final String label;

    Role(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.label.equalsIgnoreCase(text) || r.name().equalsIgnoreCase(text)) {
                return r;
            }
        }
        return LE_TAN;
    }
}
