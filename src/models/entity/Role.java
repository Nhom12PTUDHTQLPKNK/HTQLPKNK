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
        if (text == null) return LE_TAN;
        String normalized = text.trim();
        for (Role r : Role.values()) {
            if (r.label.equalsIgnoreCase(normalized) || r.name().equalsIgnoreCase(normalized)) {
                return r;
            }
        }
        if (normalized.equalsIgnoreCase("Admin") || normalized.equalsIgnoreCase("Quản lý")) {
            return QUAN_LY;
        }
        if (normalized.equalsIgnoreCase("Bác sĩ") || normalized.equalsIgnoreCase("Bác sĩ nha khoa")) {
            return NHA_SI;
        }
        if (normalized.equalsIgnoreCase("Y tá") || normalized.equalsIgnoreCase("Điều dưỡng")) {
            return Y_TA;
        }
        return LE_TAN;
    }
}
