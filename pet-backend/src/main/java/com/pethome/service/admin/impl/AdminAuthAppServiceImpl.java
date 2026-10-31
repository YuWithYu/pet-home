package com.pethome.service.admin.impl;

import com.pethome.common.AdminRole;
import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.AdminPermission;
import com.pethome.security.JwtUtil;
import com.pethome.service.admin.AdminAuthAppService;
import com.pethome.service.admin.AdminPermissionService;
import com.pethome.service.admin.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAuthAppServiceImpl implements AdminAuthAppService {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthAppServiceImpl.class);

    /** 管理员登录防爆破：失败次数 key 前缀 */
    private static final String ADMIN_LOGIN_FAIL_PREFIX = "admin:login:fail:";
    /** 连续失败锁定阈值 */
    private static final int MAX_LOGIN_FAILS = 5;
    /** 锁定时长（分钟） */
    private static final long LOGIN_LOCK_MINUTES = 15;

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private AdminPermissionService adminPermissionService;

    @Override
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        try {
            return doLogin(username.trim(), password);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, e.getMessage());
        }
    }

    private Map<String, Object> doLogin(String username, String password) {
        String failKey = ADMIN_LOGIN_FAIL_PREFIX + username;
        int failCount = readFailCount(failKey);
        if (failCount >= MAX_LOGIN_FAILS) {
            log.warn("管理员登录被锁定（连续失败 {} 次）: username={}", failCount, username);
            throw new BusinessException("登录失败次数过多，账号已锁定 15 分钟，请稍后再试");
        }

        // 兼容历史 super_admin 账号可用 admin 登录
        Admin admin = adminService.getByUsername(username);
        if (admin == null && "admin".equalsIgnoreCase(username)) {
            admin = adminService.getByUsername("super_admin");
        }
        if (admin == null) {
            recordLoginFailure(failKey, failCount);
            throw new BusinessException("账号或密码错误");
        }
        if (!admin.isEnabled()) {
            throw new BusinessException("账号已被禁用");
        }
        if (!adminService.verifyPassword(password, admin.getPassword())) {
            recordLoginFailure(failKey, failCount);
            throw new BusinessException("账号或密码错误");
        }

        stringRedisTemplate.delete(failKey);
        upgradeLegacyPassword(admin, password);

        String role = AdminRole.normalize(admin.getRole());
        if (!role.equals(admin.getRole())) {
            admin.setRole(role);
            adminService.updateAdmin(admin);
        }
        String token = jwtUtil.generateToken(admin.getUsername(), role, null);

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", token);
        loginInfo.put("adminId", admin.getId());
        loginInfo.put("username", admin.getUsername());
        loginInfo.put("name", admin.getName());
        loginInfo.put("role", role);
        loginInfo.put("department", admin.getDepartment());
        loginInfo.put("departmentId", null);
        loginInfo.put("storeId", admin.getStoreId());
        loginInfo.put("serviceStoreId", admin.getServiceStoreId());
        loginInfo.put("avatar", admin.getAvatar());
        return loginInfo;
    }

    private int readFailCount(String failKey) {
        try {
            String value = stringRedisTemplate.opsForValue().get(failKey);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 记录登录失败次数（15 分钟窗口，达到阈值后锁定） */
    private void recordLoginFailure(String failKey, int currentFails) {
        try {
            stringRedisTemplate.opsForValue().set(failKey, String.valueOf(currentFails + 1),
                    LOGIN_LOCK_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: key={}", failKey, e);
        }
    }

    /** 历史明文密码在首次登录成功后自动升级为 BCrypt（与用户端 MD5→BCrypt 迁移策略一致） */
    private void upgradeLegacyPassword(Admin admin, String rawPassword) {
        String storedPassword = admin.getPassword();
        if (storedPassword == null || storedPassword.startsWith("$2a$")) {
            return;
        }
        try {
            admin.setPassword(adminService.encodePassword(rawPassword));
            adminService.updateAdmin(admin);
            log.info("管理员明文密码已自动升级为 BCrypt: username={}", admin.getUsername());
        } catch (Exception e) {
            // 升级失败不影响本次登录，但必须留痕
            log.warn("管理员密码 BCrypt 升级失败: username={}", admin.getUsername(), e);
        }
    }

    @Override
    public Map<String, Object> getCurrentAdminInfo(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("未登录");
        }
        String username;
        try {
            username = jwtUtil.getUsernameFromToken(authorization.substring(BEARER_PREFIX.length()));
        } catch (Exception e) {
            log.warn("解析 Token 失败", e);
            throw new BusinessException(401, "Token无效或已过期");
        }
        if (username == null) {
            throw new BusinessException("Token无效或已过期");
        }
        Admin admin = adminService.getByUsername(username);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        admin.setPassword(null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("name", admin.getName());
        result.put("role", AdminRole.normalize(admin.getRole()));
        result.put("department", admin.getDepartment());
        result.put("storeId", admin.getStoreId());
        result.put("serviceStoreId", admin.getServiceStoreId());
        result.put("phone", admin.getPhone());
        result.put("email", admin.getEmail());
        result.put("avatar", admin.getAvatar());
        result.put("status", admin.getStatus());
        result.put("permissions", resolvePermissions(admin));
        return result;
    }

    private List<String> resolvePermissions(Admin admin) {
        List<String> permissionCodes = new ArrayList<>();
        try {
            if (adminPermissionService != null) {
                permissionCodes = loadConfiguredPermissions(admin.getId());
            } else {
                log.warn("adminPermissionService 未注入，无法查询权限");
            }
            if (permissionCodes.isEmpty()) {
                applyRoleDefaults(admin, permissionCodes);
            }
            // 门店管理员自动拥有门店客服权限
            if (admin.getServiceStoreId() != null && !permissionCodes.contains("/services/outlet-customer-chat")) {
                permissionCodes.add("/services/outlet-customer-chat");
            }
            if (admin.getStoreId() != null && !permissionCodes.contains("/mall/store-customer-chat")) {
                permissionCodes.add("/mall/store-customer-chat");
            }
            if (AdminRole.isAdmin(admin.getRole()) && permissionCodes.contains("/my-schedule")) {
                permissionCodes.remove("/my-schedule");
            }
        } catch (Exception e) {
            // 权限表不可用时的兜底：管理员保留 admin 权限，其他角色需人工分配
            log.warn("获取权限失败（可能是表不存在）: adminId={}", admin.getId(), e);
            if (AdminRole.isAdmin(admin.getRole())) {
                permissionCodes.add("admin");
            }
        }
        return permissionCodes;
    }

    private List<String> loadConfiguredPermissions(Long adminId) {
        try {
            List<AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(adminId);
            if (permissions == null || permissions.isEmpty()) {
                return new ArrayList<>();
            }
            return permissions.stream()
                    .filter(p -> p != null && p.getStatus() != null && p.getStatus() == 1)
                    .map(AdminPermission::getPermissionCode)
                    .filter(code -> code != null && !code.trim().isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("查询权限时出错: adminId={}", adminId, e);
            return new ArrayList<>();
        }
    }

    private void applyRoleDefaults(Admin admin, List<String> permissionCodes) {
        String normalizedRole = AdminRole.normalize(admin.getRole());
        if (AdminRole.ADMIN.equals(normalizedRole)) {
            // 管理员角色未设置权限时默认拥有全部权限（兼容旧系统）
            permissionCodes.add("admin");
        } else if (AdminRole.STORE_ADMIN.equals(normalizedRole)) {
            // 分店管理员默认菜单权限，数据范围仍由 serviceStoreId 在后端强制约束
            permissionCodes.add("/service-staff");
            permissionCodes.add("/my-schedule");
            permissionCodes.add("/service-orders/appointment-orders");
            permissionCodes.add("/verify");
            permissionCodes.add("/outlet-customer-chat");
            permissionCodes.add("/users/permissions");
        }
    }

    @Override
    public String updateMyAvatar(String authorization, String avatar) {
        Admin admin = requireCurrentAdmin(authorization);
        if (avatar == null || avatar.trim().isEmpty()) {
            throw new BusinessException("头像地址不能为空");
        }
        String normalized = avatar.trim();
        // 存库使用相对路径，便于多环境一致
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            int pathStart = normalized.indexOf("/", normalized.indexOf("://") + 3);
            if (pathStart > 0) {
                normalized = normalized.substring(pathStart);
                if (!normalized.startsWith("/")) {
                    normalized = "/" + normalized;
                }
            }
        }
        admin.setAvatar(normalized);
        if (!adminService.updateAdmin(admin)) {
            throw new BusinessException("更新失败");
        }
        return admin.getAvatar();
    }

    @Override
    public String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        return adminService.encodePassword(rawPassword);
    }

    private Admin requireCurrentAdmin(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("请先登录");
        }
        try {
            String username = jwtUtil.getUsernameFromToken(authorization.substring(BEARER_PREFIX.length()));
            if (username == null) {
                throw new BusinessException("请先登录");
            }
            Admin admin = adminService.getByUsername(username);
            if (admin == null) {
                throw new BusinessException("请先登录");
            }
            return admin;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解析 token 失败", e);
            throw new BusinessException("请先登录");
        }
    }
}
