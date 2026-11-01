package com.pethome.service.user.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.AdminPermission;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.admin.AdminPermissionService;
import com.pethome.service.admin.AdminService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 当前登录用户视图装配组件。
 * <p>
 * 负责按 token 解析身份并组装“当前用户/管理员”资料结构（含权限、敏感字段可见性控制），
 * 从 {@code UserProfileAppServiceImpl} 中独立出来，避免资料读写逻辑与身份展示耦合。
 */
@Service
public class CurrentUserViewAssembler {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserViewAssembler.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String DEFAULT_AVATAR = "/static/images/garfield-default-avatar.png";

    @Autowired
    private UserService userService;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Autowired
    private AdminService adminService;

    @Autowired(required = false)
    private AdminPermissionService adminPermissionService;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> getCurrentUser(String authorization, Long userId, String username, Long loginUserId) {
        try {
            return doGetCurrentUser(authorization, userId, username, loginUserId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            // 认证相关异常（token 解析失败、JWT 异常等）统一返回 401
            if (isAuthenticationFailure(e)) {
                throw new BusinessException(401, "认证失败，请重新登录");
            }
            throw new BusinessException(500, "获取用户信息失败，请稍后重试");
        }
    }

    /**
     * 安全修复保留：前端传入 userId 查询他人资料时仅返回公开字段，
     * 手机号与积分只在本人查询时返回，避免越权读取与敏感信息入日志。
     */
    private Map<String, Object> doGetCurrentUser(String authorization, Long userId, String username, Long loginUserId) {
        // 解析当前登录用户身份（用于判定是自查还是查他人）；subject 可能是手机号，须走反查链
        Long currentUserId = userIdentityResolver.resolveUserId(authorization);
        // 也尝试从拦截器写入的 request 属性中获取
        if (currentUserId == null) {
            currentUserId = loginUserId;
        }

        if (userId != null && userId > 0) {
            // 查询他人资料：仅返回公开字段，不返回手机号/邮箱/密码等敏感信息
            User targetUser = userService.getUserById(userId);
            if (targetUser == null) {
                throw new BusinessException("用户不存在或已被禁用");
            }

            // 确保memberLevel有默认值
            if (targetUser.getMemberLevel() == null) {
                targetUser.setMemberLevel(1);
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", targetUser.getId());
            userInfo.put("uid", targetUser.getId());
            userInfo.put("username", targetUser.getUsername() != null ? targetUser.getUsername() : "");
            userInfo.put("nickname", targetUser.getNickname() != null ? targetUser.getNickname() : "用户");
            userInfo.put("avatar", targetUser.getAvatar() != null ? targetUser.getAvatar() : DEFAULT_AVATAR);
            userInfo.put("backgroundImage", targetUser.getBackgroundImage());
            userInfo.put("memberLevel", targetUser.getMemberLevel());
            userInfo.put("level", targetUser.getMemberLevel());
            userInfo.put("gender", targetUser.getGender());
            userInfo.put("signature", targetUser.getSignature() != null ? targetUser.getSignature() : "");
            userInfo.put("bio", targetUser.getSignature() != null ? targetUser.getSignature() : "");
            userInfo.put("charm", targetUser.getCharm() != null ? targetUser.getCharm() : 0);
            userInfo.put("role", "user");
            userInfo.put("permissions", new String[]{"user"});
            userInfo.put("isAdmin", false);
            // 仅当查询的是自己时才返回 points 与手机号；查询他人时不返回
            if (currentUserId != null && currentUserId.equals(userId)) {
                userInfo.put("points", targetUser.getPoints() != null ? targetUser.getPoints() : 0);
                userInfo.put("phone", targetUser.getPhone() != null ? targetUser.getPhone() : "");
            }
            return userInfo;
        }

        // 未指定 userId 或 userId 无效，继续使用 token 查询当前用户

        // 优先从 token 中获取用户名
        String token = authorization;
        String tokenUsername = null;
        if (token != null && token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
            try {
                tokenUsername = jwtUtil.getUsernameFromToken(token);
            } catch (Exception e) {
                // token 解析失败不打印敏感细节
            }
        }

        // 1. 先尝试从 admin 表查询（管理员或服务人员）
        if (tokenUsername != null) {
            Admin admin = adminService.getByUsername(tokenUsername);
            if (admin != null) {
                return buildAdminInfo(admin);
            }
        }

        // 2. 如果 admin 表中没有，再查询 user 表（普通用户）
        // 安全修复：此前匿名请求未带 userId/username 时会 fallback 到「用户ID=1」，并把该用户的
        // 手机号、积分等完整资料返回；传 username 还可枚举任意账号。现要求必须持有可解析的登录态。
        User user;
        if (tokenUsername != null) {
            // 优先使用 token 中的用户名/手机号查询（微信/手机登录时 subject 可能是手机号）
            user = userService.getUserByUsername(tokenUsername);
            if (user == null) {
                user = userService.getUserByPhone(tokenUsername);
            }
        } else if (loginUserId != null) {
            user = userService.getUserById(loginUserId);
        } else {
            throw new BusinessException(401, "请先登录");
        }

        if (user == null) {
            throw new BusinessException(401, "未登录，请先登录");
        }
        // 兼容旧前端仍传 username：仅允许查询当前登录账号，禁止枚举他人资料
        if (username != null && !username.isEmpty()
                && !username.equals(user.getUsername())
                && !username.equals(user.getPhone())
                && !username.equals(tokenUsername)) {
            throw new BusinessException(403, "无权查看其他用户信息");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "该账号已被封禁");
        }

        // 隐藏密码
        user.setPassword(null);

        // 确保memberLevel有默认值（如果为null，设为1）
        if (user.getMemberLevel() == null) {
            user.setMemberLevel(1);
        }

        // 转换为 Map 格式，保持与前端期望的格式一致
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("uid", user.getId()); // 兼容前端使用的uid字段
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone() != null ? user.getPhone() : ""); // 添加手机号字段
        userInfo.put("backgroundImage", user.getBackgroundImage()); // 添加背景图字段
        userInfo.put("memberLevel", user.getMemberLevel());
        userInfo.put("level", user.getMemberLevel()); // 兼容前端使用的level字段
        userInfo.put("points", user.getPoints() != null ? user.getPoints() : 0); // 积分（罐头数量）
        userInfo.put("charm", user.getCharm() != null ? user.getCharm() : 0); // 魅力值
        userInfo.put("role", "user"); // 普通用户
        userInfo.put("permissions", new String[]{"user"});
        userInfo.put("isAdmin", false);
        return userInfo;
    }

    /**
     * 管理员/服务人员资料结构（含权限装配）。
     */
    private Map<String, Object> buildAdminInfo(Admin admin) {
        // 即使未启用也返回，由前端根据 status 判断
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("name", admin.getName());
        adminInfo.put("avatar", admin.getAvatar());
        // 统一 role 为小写，便于前端判断
        String role = admin.getRole();
        if (role != null) {
            role = role.toLowerCase();
        } else {
            role = "staff"; // 默认角色
        }
        adminInfo.put("role", role);
        adminInfo.put("department", admin.getDepartment());

        // 获取权限列表（从数据库查询）
        List<String> permissionCodes = new ArrayList<>();
        try {
            if (adminPermissionService != null) {
                try {
                    List<AdminPermission> permissions = adminPermissionService.getPermissionsByAdminId(admin.getId());
                    if (permissions != null && !permissions.isEmpty()) {
                        permissionCodes = permissions.stream()
                                .filter(p -> p != null && p.getStatus() != null && p.getStatus() == 1)
                                .map(AdminPermission::getPermissionCode)
                                .filter(code -> code != null && !code.trim().isEmpty())
                                .collect(Collectors.toList());
                    }
                } catch (Exception e) {
                    log.error("查询权限失败, adminId={}", admin.getId(), e);
                }
            }

            // 如果权限表中没有记录，根据角色决定默认权限
            if (permissionCodes.isEmpty()) {
                if (admin.isSuperAdmin() && adminPermissionService != null) {
                    // 超级管理员且没有设置权限，默认拥有所有权限
                    List<AdminPermission> allPermissions = adminPermissionService.getAllAvailablePermissions();
                    permissionCodes = allPermissions.stream()
                            .map(AdminPermission::getPermissionCode)
                            .collect(Collectors.toList());
                } else if (admin.isSuperAdmin()) {
                    permissionCodes.add("admin");
                }
            }
            // 门店管理员自动拥有门店客服权限（把门店管理员当成客服）
            if (admin.getServiceStoreId() != null && !permissionCodes.contains("/services/outlet-customer-chat")) {
                permissionCodes.add("/services/outlet-customer-chat");
            }
            // 店铺管理员自动拥有店铺客服权限
            if (admin.getStoreId() != null && !permissionCodes.contains("/mall/store-customer-chat")) {
                permissionCodes.add("/mall/store-customer-chat");
            }
        } catch (Exception permError) {
            // 如果权限表不存在或其他错误，根据角色使用默认权限
            log.error("获取权限失败（可能是表不存在）, adminId={}", admin.getId(), permError);
            if (admin.isSuperAdmin()) {
                permissionCodes.add("admin");
            }
            // 员工等角色不自动加 admin，需在权限管理中手动分配
        }

        adminInfo.put("permissions", permissionCodes.toArray(new String[0]));
        adminInfo.put("memberLevel", admin.isSuperAdmin() ? 10 : 5); // 管理员等级更高
        adminInfo.put("isAdmin", true); // 标记为管理员
        adminInfo.put("status", admin.getStatus()); // 状态信息
        adminInfo.put("storeId", admin.getStoreId());
        adminInfo.put("serviceStoreId", admin.getServiceStoreId()); // 所属服务门店，用于排班/预约按门店过滤

        return adminInfo;
    }

    private boolean isAuthenticationFailure(Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return exceptionMessage.contains("token")
                || exceptionMessage.contains("jwt")
                || exceptionMessage.contains("authentication")
                || exceptionMessage.contains("unauthorized")
                || e.getClass().getName().contains("JwtException")
                || e.getClass().getName().contains("Token");
    }
}
