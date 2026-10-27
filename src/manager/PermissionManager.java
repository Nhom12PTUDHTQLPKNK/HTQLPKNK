package manager;

import entity.Feature;
import models.entity.Role;

import java.util.*;

/**
 * Lớp quản lý quyền hạn và phân quyền người dùng
 */
public class PermissionManager {

    private static final Map<Role, Set<Feature>> ROLE_PERMS = new EnumMap<>(Role.class);
    private static final Set<Feature> COMMON_PERMS = EnumSet.of(Feature.HOME, Feature.ACCOUNT, Feature.HELP);

    static {
        ROLE_PERMS.put(Role.NHA_SI, EnumSet.of(Feature.D_BA, Feature.D_GOI, Feature.D_KM, Feature.D_LLV,
                Feature.BOOK, Feature.S_KH, Feature.S_KM, Feature.S_KHO));
        ROLE_PERMS.put(Role.LE_TAN, EnumSet.of(Feature.D_GOI, Feature.D_KM, Feature.D_LLV,
                Feature.BOOK, Feature.PAY, Feature.S_KH, Feature.S_KM, Feature.ST_DT, Feature.ST_KH));
        ROLE_PERMS.put(Role.Y_TA, EnumSet.of(Feature.D_BA, Feature.D_KHO, Feature.D_LLV,
                Feature.S_KH, Feature.S_KHO));
        ROLE_PERMS.put(Role.QUAN_LY, EnumSet.allOf(Feature.class));
    }

    /**
     * Lấy tập hợp quyền hạn của một vai trò
     */
    public static Set<Feature> getPermissionsOfRole(Role role) {
        EnumSet<Feature> permissions = EnumSet.copyOf(COMMON_PERMS);
        if (role == Role.QUAN_LY) {
            permissions.addAll(EnumSet.allOf(Feature.class));
            return permissions;
        }
        if (role != null && ROLE_PERMS.containsKey(role)) {
            permissions.addAll(ROLE_PERMS.get(role));
        }
        return permissions;
    }

    /**
     * Lấy danh sách tên các chức năng được cấp quyền cho một vai trò
     */
    public static String getPermissionNames(Role role) {
        if (role == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Feature f : getPermissionsOfRole(role)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f.title);
        }
        return sb.toString();
    }

    /**
     * Kiểm tra người dùng có quyền chỉnh sửa một tính năng không
     */
    public static boolean canEdit(Role role, Feature feature) {
        if (role == null) return false;
        switch (role) {
            case QUAN_LY:
                return true;
            case NHA_SI:
                return feature == Feature.D_BA;
            case Y_TA:
                return feature == Feature.D_BA || feature == Feature.D_KHO;
            default:
                return false;
        }
    }

    /**
     * Kiểm tra người dùng có quyền truy cập một tính năng không
     */
    public static boolean hasAccess(Set<Feature> permissions, Feature feature) {
        return permissions.contains(feature);
    }
}