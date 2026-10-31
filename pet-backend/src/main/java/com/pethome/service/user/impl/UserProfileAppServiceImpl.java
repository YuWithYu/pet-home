package com.pethome.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Admin;
import com.pethome.entity.AdminPermission;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.admin.AdminPermissionService;
import com.pethome.service.admin.AdminService;
import com.pethome.service.order.PointsRecordService;
import com.pethome.service.user.UserProfileAppService;
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

@Service
public class UserProfileAppServiceImpl implements UserProfileAppService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileAppServiceImpl.class);

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
    private PointsRecordService pointsRecordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
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
        }

        // 2. 如果 admin 表中没有，再查询 user 表（普通用户）
        User user;
        if (tokenUsername != null) {
            // 优先使用 token 中的用户名/手机号查询（微信/手机登录时 subject 可能是手机号）
            user = userService.getUserByUsername(tokenUsername);
            if (user == null) {
                user = userService.getUserByPhone(tokenUsername);
            }
        } else if (userId != null) {
            user = userService.getUserById(userId);
        } else if (username != null && !username.isEmpty()) {
            user = userService.getUserByUsername(username);
        } else {
            // 默认返回ID为1的用户（用于测试）
            user = userService.getUserById(1L);
        }

        if (user == null) {
            if (tokenUsername != null) {
                throw new BusinessException(401,
                        "用户不存在: " + tokenUsername + "。请检查账号是否正确或联系管理员");
            }
            throw new BusinessException(401, "未登录，请先登录");
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

    private boolean isAuthenticationFailure(Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        return exceptionMessage.contains("token")
                || exceptionMessage.contains("jwt")
                || exceptionMessage.contains("authentication")
                || exceptionMessage.contains("unauthorized")
                || e.getClass().getName().contains("JwtException")
                || e.getClass().getName().contains("Token");
    }

    @Override
    public User getUserDetail(String token, Long userId, Long loginUserId, boolean staffOrAdmin) {
        // 安全修复保留：匿名不可达；查他人仅返回脱敏公开字段
        Long currentUserId = userIdentityResolver.resolveUserId(token);
        if (currentUserId == null) {
            currentUserId = loginUserId;
        }
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        Long targetId = userId != null ? userId : currentUserId;
        User user = userService.getUserById(targetId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 隐藏密码
        user.setPassword(null);
        if (!staffOrAdmin && !currentUserId.equals(targetId)) {
            maskUserForPublic(user);
        }
        return user;
    }

    @Override
    public Map<String, Object> getUserAmount() {
        Map<String, Object> amount = new HashMap<>();
        amount.put("balance", 0.0);
        amount.put("points", 0);
        return amount;
    }

    @Override
    public Map<String, Object> getUserList(Integer page, Integer size, String keyword, boolean staffOrAdmin) {
        // 安全修复保留：用户列表含手机号等 PII，仅限 staff/admin
        if (!staffOrAdmin) {
            throw new BusinessException(403, "无权获取用户列表");
        }
        Page<User> userPage = new Page<>(page, size);
        IPage<User> result = userService.getUserList(userPage, keyword);

        // 统一补充状态文案，前端不再自行猜测
        List<Map<String, Object>> users = new ArrayList<>();
        if (result.getRecords() != null) {
            for (User u : result.getRecords()) {
                if (u == null) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", u.getId());
                item.put("username", u.getUsername());
                item.put("nickname", u.getNickname());
                item.put("avatar", u.getAvatar());
                item.put("phone", u.getPhone());
                item.put("gender", u.getGender());
                item.put("role", u.getRole());
                item.put("points", u.getPoints());
                item.put("memberLevel", u.getMemberLevel());
                item.put("charm", u.getCharm());
                item.put("status", u.getStatus());
                item.put("statusText", (u.getStatus() != null && u.getStatus() == 1) ? "正常" : "禁用");
                item.put("createTime", u.getCreateTime());
                item.put("updateTime", u.getUpdateTime());
                users.add(item);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @Override
    public User getUserById(Long id, Long currentUserId, boolean staffOrAdmin) {
        // 安全修复保留：本人或 staff/admin 可看完整资料，查他人仅返回脱敏公开字段
        User user = userService.getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 隐藏密码
        user.setPassword(null);
        boolean self = currentUserId != null && currentUserId.equals(id);
        if (!staffOrAdmin && !self) {
            maskUserForPublic(user);
        }
        return user;
    }

    @Override
    public User updateUser(User user, Long loginUserId, boolean staffOrAdmin) {
        // 安全修复：管理端分支的身份校验已由 Controller 按 isAdmin 完成（管理端 token 无 userId）；
        // C 端自更新仍要求登录态，防止匿名写入
        if (loginUserId == null && !staffOrAdmin) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        try {
            User target = user;
            if (staffOrAdmin) {
                // 管理端：保留原逻辑（管理后台编辑用户）
                if (target.getId() == null) {
                    throw new BusinessException("用户ID不能为空");
                }
            } else {
                // 普通用户：只能改自己的资料，且仅限安全字段
                User safe = new User();
                safe.setId(loginUserId);
                safe.setNickname(user.getNickname());
                safe.setAvatar(user.getAvatar());
                safe.setAvatarUrl(user.getAvatarUrl());
                safe.setBackgroundImage(user.getBackgroundImage());
                safe.setGender(user.getGender());
                safe.setSignature(user.getSignature());
                safe.setInterestTags(user.getInterestTags());
                target = safe;
            }

            if (target.getId() == null) {
                throw new BusinessException("用户ID不能为空");
            }

            restoreTemporaryAvatar(target);

            if (!userService.updateUser(target)) {
                throw new BusinessException("更新用户信息失败");
            }

            // 重新获取更新后的用户信息
            User updatedUser = userService.getUserById(target.getId());
            if (updatedUser != null) {
                updatedUser.setPassword(null);
            }
            return updatedUser;
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("更新用户信息失败", e);
            // 昵称重复等业务异常
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            throw new BusinessException("更新用户信息失败，请稍后重试");
        }
    }

    /** 临时头像路径（小程序选择后未上传）不更新头像，保留原头像 */
    private void restoreTemporaryAvatar(User user) {
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return;
        }
        String avatar = user.getAvatar();
        if (avatar.contains("/tmp/") || avatar.contains("/__tmp__/")
                || avatar.startsWith("http://tmp/") || avatar.startsWith("http://__tmp__/")
                || avatar.startsWith("https://tmp/") || avatar.startsWith("https://__tmp__/")) {
            User existingUser = userService.getUserById(user.getId());
            user.setAvatar(existingUser != null ? existingUser.getAvatar() : null);
        }
    }

    @Override
    public User updateUserById(Long id, User user, boolean staffOrAdmin) {
        // 安全修复保留：更新任意用户属管理端能力
        if (!staffOrAdmin) {
            throw new BusinessException(403, "无权更新用户信息");
        }
        try {
            user.setId(id);
            // 强制归一化：只接受 0/1，避免前端异常值导致"禁用后回弹正常"
            if (user.getStatus() != null) {
                user.setStatus(user.getStatus() == 1 ? 1 : 0);
            }
            if (!userService.updateUser(user)) {
                throw new BusinessException("更新用户信息失败");
            }
            // 重新获取更新后的用户信息
            User updatedUser = userService.getUserById(id);
            if (updatedUser != null) {
                updatedUser.setPassword(null);
            }
            return updatedUser;
        } catch (BusinessException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("操作失败", e);
            // 昵称重复等业务异常
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            throw new BusinessException("更新用户信息失败，请稍后重试");
        }
    }

    @Override
    public boolean updateUserStatus(Long id, Integer status, boolean staffOrAdmin) {
        // 安全修复保留：禁用/启用账号属管理端能力
        if (!staffOrAdmin) {
            throw new BusinessException(403, "无权更新用户状态");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态参数错误，仅支持0(禁用)/1(正常)");
        }
        User existing = userService.getUserById(id);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        existing.setStatus(status);
        if (!userService.updateUser(existing)) {
            throw new BusinessException("更新用户状态失败");
        }
        return true;
    }

    @Override
    public String deleteAccount(String authorization) {
        try {
            // 从 token 中获取用户ID（缓存优先，其次解析 JWT subject）
            User user = userIdentityResolver.resolveUser(authorization);
            Long userId = user != null ? user.getId() : null;
            if (userId == null) {
                throw new BusinessException("无法获取用户信息，请重新登录");
            }

            boolean deleted = userService.deleteUser(userId);
            if (!deleted) {
                throw new BusinessException("注销失败，用户不存在");
            }
            // 清除用户缓存
            userService.clearUserCache(userId);
            return "账号注销成功";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("注销账号失败", e);
            throw new BusinessException("注销失败，请稍后重试");
        }
    }

    @Override
    public Map<String, Object> getUserStats(Long userId, String authorization) {
        try {
            Long targetUserId = userId;
            if (targetUserId == null) {
                targetUserId = userIdentityResolver.resolveUserId(authorization);
            }
            if (targetUserId == null) {
                throw new BusinessException(1, "请先登录");
            }

            Map<String, Integer> stats = userService.getUserStats(targetUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("followCount", stats.get("followCount"));
            result.put("fansCount", stats.get("fansCount"));
            result.put("postCount", stats.get("postCount"));
            result.put("likeCount", stats.get("likeCount"));

            result.put("follows", stats.get("followCount"));
            result.put("fans", stats.get("fansCount"));
            result.put("dynamics", stats.get("postCount"));
            result.put("likes", stats.get("likeCount"));
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            throw new BusinessException("获取用户统计失败，请稍后重试");
        }
    }

    @Override
    public Map<String, Object> getPointsRecords(String authorization, Long userId, String type, boolean staffOrAdmin) {
        // 从 token 获取用户ID
        Long currentUserId = userIdentityResolver.resolveUserId(authorization);
        if (currentUserId == null && userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 安全修复保留：积分记录属个人数据，非管理端强制以登录态为准
        Long targetUserId = staffOrAdmin ? (userId != null ? userId : currentUserId) : currentUserId;

        Map<String, Object> result = new HashMap<>();
        List<PointsRecord> records;
        if ("earn".equals(type)) {
            records = pointsRecordService.getEarnRecords(targetUserId);
        } else if ("spend".equals(type)) {
            records = pointsRecordService.getSpendRecords(targetUserId);
        } else {
            throw new BusinessException("类型参数错误，只能是 'earn' 或 'spend'");
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (PointsRecord record : records) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("description", record.getDescription());
            recordMap.put("amount", record.getPoints() != null ? record.getPoints() : 0); // 使用points字段，返回给前端时用amount
            recordMap.put("type", record.getType());
            recordMap.put("source", record.getSource() != null ? record.getSource() : ""); // source可能为null
            recordMap.put("createTime", record.getCreateTime());
            recordMap.put("time", record.getCreateTime()); // 兼容前端
            recordList.add(recordMap);
        }

        result.put("records", recordList);
        result.put("statistics", pointsRecordService.getPointsStatistics(targetUserId));
        return result;
    }

    @Override
    public Map<String, Object> getPointsStatistics(String authorization, Long userId, boolean staffOrAdmin) {
        // 从 token 获取用户ID
        Long currentUserId = userIdentityResolver.resolveUserId(authorization);
        if (currentUserId == null && userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 安全修复保留：积分余额/统计属个人数据，非管理端强制以登录态为准
        Long targetUserId = staffOrAdmin ? (userId != null ? userId : currentUserId) : currentUserId;

        // 先清除用户缓存，再查库，保证与签到/任务中心显示的余额一致（避免 Redis 缓存旧积分）
        userService.clearUserCache(targetUserId);
        User user = userService.getUserById(targetUserId);
        Integer currentBalance = user != null ? user.getPoints() : 0;

        // 获取统计信息（来自 points_record 的累计）
        Map<String, Object> statistics = pointsRecordService.getPointsStatistics(targetUserId);
        statistics.put("currentBalance", currentBalance);

        // 无流水时保持逻辑一致：当前剩余有值则累计获取至少等于当前剩余
        Object te = statistics.get("totalEarned");
        Object ts = statistics.get("totalSpent");
        int totalEarned = (te instanceof Number) ? ((Number) te).intValue() : 0;
        int totalSpent = (ts instanceof Number) ? ((Number) ts).intValue() : 0;
        if (totalEarned == 0 && currentBalance != null && currentBalance > 0 && totalSpent == 0) {
            statistics.put("totalEarned", currentBalance);
        }
        return statistics;
    }

    @Override
    public Map<String, Object> updateCharmAndLevel(String authorization) {
        Long userId = userIdentityResolver.resolveUserId(authorization);
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录信息无效");
        }

        // 调用服务更新魅力值和等级
        userService.updateCharmAndLevel(userId);

        // 获取更新后的用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("charm", user.getCharm() != null ? user.getCharm() : 0);
        result.put("level", user.getMemberLevel() != null ? user.getMemberLevel() : 0);
        result.put("points", user.getPoints() != null ? user.getPoints() : 0);

        // 计算总历史积分
        Map<String, Object> statistics = pointsRecordService.getPointsStatistics(userId);
        result.put("totalEarned", statistics.get("totalEarned"));
        result.put("totalSpent", statistics.get("totalSpent"));
        return result;
    }

    /** 非管理端视角的用户脱敏：抹掉手机号/邮箱/openid/激活码/积分/状态等敏感字段 */
    private void maskUserForPublic(User user) {
        if (user == null) {
            return;
        }
        user.setPassword(null);
        user.setPhone(null);
        user.setPhoneNumber(null);
        user.setEmail(null);
        user.setOpenid(null);
        user.setUnionid(null);
        user.setActivationCode(null);
        user.setPoints(null);
        user.setStatus(null);
        user.setType(null);
    }
}
