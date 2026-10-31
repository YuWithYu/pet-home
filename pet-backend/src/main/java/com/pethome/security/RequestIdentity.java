package com.pethome.security;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前请求身份读取工具
 *
 * 身份属性由 RoleInterceptor 校验后写入 request，这里只做读取，不再重复解析 token 或查库。
 * 约定 role 取值：admin（含 super_admin 归一）、store_admin、staff、user。
 */
public final class RequestIdentity {

    private RequestIdentity() {
    }

    public static String role(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute("role");
        return value == null ? "" : value.toString();
    }

    /** 平台/店铺管理员 */
    public static boolean isAdmin(HttpServletRequest request) {
        String role = role(request);
        return "admin".equals(role) || "store_admin".equals(role);
    }

    public static boolean isStaff(HttpServletRequest request) {
        return "staff".equals(role(request));
    }

    /** staff 或管理员（核销、排班等门店运营操作） */
    public static boolean isAdminOrStaff(HttpServletRequest request) {
        return isAdmin(request) || isStaff(request);
    }

    /** 普通 C 端用户：已通过拦截器校验但不属于任何管理端角色 */
    public static boolean isEndUser(HttpServletRequest request) {
        return "user".equals(role(request));
    }

    public static Long userId(HttpServletRequest request) {
        return toLong(request == null ? null : request.getAttribute("userId"));
    }

    public static Long adminId(HttpServletRequest request) {
        return toLong(request == null ? null : request.getAttribute("adminId"));
    }

    public static Long serviceStoreId(HttpServletRequest request) {
        return toLong(request == null ? null : request.getAttribute("serviceStoreId"));
    }

    public static Long departmentId(HttpServletRequest request) {
        return toLong(request == null ? null : request.getAttribute("departmentId"));
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
