package com.pethome.service.user.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.User;
import com.pethome.service.user.UserProfileAppService;
import com.pethome.service.user.UserService;
import com.pethome.service.user.support.CurrentUserViewAssembler;
import com.pethome.service.user.support.UserIdentityResolver;
import com.pethome.service.user.support.UserPointsViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户资料应用服务实现。
 * <p>
 * 当前用户视图与积分视图分别下沉到 {@link CurrentUserViewAssembler}、{@link UserPointsViewService}，
 * 本类只负责资料读写的权限校验与领域调用编排。
 */
@Service
public class UserProfileAppServiceImpl implements UserProfileAppService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileAppServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Autowired
    private CurrentUserViewAssembler currentUserViewAssembler;

    @Autowired
    private UserPointsViewService userPointsViewService;

    @Override
    public Map<String, Object> getCurrentUser(String authorization, Long userId, String username, Long loginUserId) {
        return currentUserViewAssembler.getCurrentUser(authorization, userId, username, loginUserId);
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
        return userPointsViewService.getPointsRecords(authorization, userId, type, staffOrAdmin);
    }

    @Override
    public Map<String, Object> getPointsStatistics(String authorization, Long userId, boolean staffOrAdmin) {
        return userPointsViewService.getPointsStatistics(authorization, userId, staffOrAdmin);
    }

    @Override
    public Map<String, Object> updateCharmAndLevel(String authorization) {
        return userPointsViewService.updateCharmAndLevel(authorization);
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
