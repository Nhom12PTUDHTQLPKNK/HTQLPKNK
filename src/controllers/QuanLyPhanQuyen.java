package controllers;

import models.entity.TinhNang;
import models.entity.VaiTro;

import java.util.*;

/**
 * Lớp quản lý quyền hạn và phân quyền người dùng
 */
public class QuanLyPhanQuyen {

    private static final Map<VaiTro, Set<TinhNang>> ROLE_PERMS = new EnumMap<>(VaiTro.class);
    private static final Set<TinhNang> COMMON_PERMS = EnumSet.of(TinhNang.HOME, TinhNang.ACCOUNT, TinhNang.HELP);

    static {
        ROLE_PERMS.put(VaiTro.NHA_SI, EnumSet.of(TinhNang.D_BA, TinhNang.D_GOI, TinhNang.D_KM, TinhNang.D_LLV,
                TinhNang.BOOK, TinhNang.S_KH, TinhNang.S_KM, TinhNang.S_KHO));
        ROLE_PERMS.put(VaiTro.LE_TAN, EnumSet.of(TinhNang.D_GOI, TinhNang.D_KM, TinhNang.D_LLV,
                TinhNang.BOOK, TinhNang.PAY, TinhNang.S_KH, TinhNang.S_KM, TinhNang.ST_DT, TinhNang.ST_KH));
        ROLE_PERMS.put(VaiTro.Y_TA, EnumSet.of(TinhNang.D_BA, TinhNang.D_KHO, TinhNang.D_LLV,
                TinhNang.S_KH, TinhNang.S_KHO));
        ROLE_PERMS.put(VaiTro.QUAN_LY, EnumSet.allOf(TinhNang.class));
    }

    /**
     * Lấy tập hợp quyền hạn của một vai trò
     */
    public static Set<TinhNang> getPermissionsOfRole(VaiTro role) {
        EnumSet<TinhNang> permissions = EnumSet.copyOf(COMMON_PERMS);
        if (role == VaiTro.QUAN_LY) {
            permissions.addAll(EnumSet.allOf(TinhNang.class));
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
    public static String getPermissionNames(VaiTro role) {
        if (role == null) return "";
        StringBuilder sb = new StringBuilder();
        for (TinhNang f : getPermissionsOfRole(role)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(f.title);
        }
        return sb.toString();
    }

    /**
     * Kiểm tra người dùng có quyền chỉnh sửa một tính năng không
     */
    public static boolean canEdit(VaiTro role, TinhNang feature) {
        if (role == null) return false;
        switch (role) {
            case QUAN_LY:
                return true;
            case NHA_SI:
                return feature == TinhNang.D_BA;
            case Y_TA:
                return feature == TinhNang.D_BA || feature == TinhNang.D_KHO;
            default:
                return false;
        }
    }

    /**
     * Kiểm tra người dùng có quyền truy cập một tính năng không
     */
    public static boolean hasAccess(Set<TinhNang> permissions, TinhNang feature) {
        return permissions.contains(feature);
    }
}
