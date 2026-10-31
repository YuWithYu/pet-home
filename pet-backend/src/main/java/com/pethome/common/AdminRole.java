package com.pethome.common;

/**
 * 后台角色归一与判定。
 *
 * <p>历史数据中存在 super_admin 等别名，若各控制器自行实现归一逻辑容易出现口径不一致，
 * 这里统一收敛为 admin / store_admin / staff 三种角色。</p>
 */
public final class AdminRole {

    public static final String ADMIN = "admin";
    public static final String STORE_ADMIN = "store_admin";
    public static final String STAFF = "staff";

    private AdminRole() {
    }

    /** 归一到标准角色值，未知角色一律按 staff 处理（最小权限） */
    public static String normalize(String role) {
        if (role == null) {
            return STAFF;
        }
        String normalized = role.trim().toLowerCase();
        if ("super_admin".equals(normalized)) {
            return ADMIN;
        }
        if (!ADMIN.equals(normalized) && !STAFF.equals(normalized) && !STORE_ADMIN.equals(normalized)) {
            return STAFF;
        }
        return normalized;
    }

    /** 平台管理员 */
    public static boolean isAdmin(String role) {
        return ADMIN.equals(normalize(role));
    }

    /** 门店管理员 */
    public static boolean isStoreAdmin(String role) {
        return STORE_ADMIN.equals(normalize(role));
    }

    /** 服务人员 */
    public static boolean isStaff(String role) {
        return STAFF.equals(normalize(role));
    }
}
