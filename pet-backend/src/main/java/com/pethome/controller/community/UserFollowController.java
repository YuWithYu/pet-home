package com.pethome.controller.community;


import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.UserFollow;
import com.pethome.service.user.UserFollowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/community")
@Tag(name = "用户关注管理")
public class UserFollowController {

    private static final Logger log = LoggerFactory.getLogger(UserFollowController.class);

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private com.pethome.service.user.UserService userService;

    @PostMapping("/follow")
    @Operation(summary = "关注用户")
    public Result<Boolean> followUser(@RequestParam Long followingId, @RequestParam Long followerId,
                                      HttpServletRequest request) {
        try {
            // 安全修复：关注人以登录态为准（原从 query 取，可替他人批量关注刷粉）
            Object attrId = request.getAttribute("userId");
            if (attrId instanceof Number) {
                followerId = ((Number) attrId).longValue();
            }
            if (followerId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            // 不能关注自己
            if (followerId.equals(followingId)) {
                return Result.error("不能关注自己");
            }

            // 检查是否已经关注
            boolean isFollowing = userFollowService.isFollowing(followerId, followingId);
            if (isFollowing) {
                // 已经关注过了，返回成功（避免前端显示错误）
                return Result.success(true);
            }

            boolean success = userFollowService.followUser(followerId, followingId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("关注失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("关注用户失败", e);
            return Result.error("关注用户失败，请稍后重试");
        }
    }

    @DeleteMapping("/follow")
    @Operation(summary = "取消关注用户")
    public Result<Boolean> unfollowUser(@RequestParam Long followingId, @RequestParam Long followerId,
                                        HttpServletRequest request) {
        try {
            // 安全修复：取关人以登录态为准（原从 query 取，可强制取关他人）
            Object attrId = request.getAttribute("userId");
            if (attrId instanceof Number) {
                followerId = ((Number) attrId).longValue();
            }
            if (followerId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean success = userFollowService.unfollowUser(followerId, followingId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("取消关注失败，可能没有关注过");
            }
        } catch (Exception e) {
            log.error("取消关注用户失败", e);
            return Result.error("取消关注用户失败，请稍后重试");
        }
    }

    @GetMapping("/follow/status")
    @Operation(summary = "检查关注状态")
    public Result<Boolean> checkFollowStatus(@RequestParam Long followerId, @RequestParam Long followingId,
                                             HttpServletRequest request) {
        return com.pethome.common.SafeCall.of(() -> {
            // 安全修复：关注状态以登录态为准（原从 query 取，可探测任意两人的关注关系）
            Object attrId = request.getAttribute("userId");
            final Long targetFollowerId = attrId instanceof Number ? ((Number) attrId).longValue() : null;
            if (targetFollowerId == null) {
                return Result.error(401, "用户未登录或token无效");
            }
            boolean isFollowing = userFollowService.isFollowing(targetFollowerId, followingId);
            return Result.success(isFollowing);
        }, "检查关注状态失败，请稍后重试", log);
    }

    @GetMapping("/users/{userId}/following")
    @Operation(summary = "获取用户关注列表")
    public Result<List<UserFollow>> getFollowingList(@PathVariable Long userId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            return Result.success(followingList);
        }, "获取关注列表失败，请稍后重试", log);
    }

    @GetMapping("/users/{userId}/followers")
    @Operation(summary = "获取用户粉丝列表")
    public Result<List<UserFollow>> getFollowerList(@PathVariable Long userId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<UserFollow> followerList = userFollowService.getFollowerList(userId);
            return Result.success(followerList);
        }, "获取粉丝列表失败，请稍后重试", log);
    }

    @GetMapping("/users/{userId}/follow-stats")
    @Operation(summary = "获取用户关注统计")
    public Result<Map<String, Object>> getFollowStats(@PathVariable Long userId) {
        return com.pethome.common.SafeCall.of(() -> {
            int followingCount = userFollowService.getFollowingCount(userId);
            int followerCount = userFollowService.getFollowerCount(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("followingCount", followingCount);
            stats.put("followerCount", followerCount);
            
            return Result.success(stats);
        }, "获取关注统计失败，请稍后重试", log);
    }

    @GetMapping("/users/{userId}/followed")
    @Operation(summary = "获取用户关注的用户列表（前端调用）")
    public Result<List<Map<String, Object>>> getFollowedUsers(@PathVariable Long userId) {
        return com.pethome.common.SafeCall.of(() -> {
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (UserFollow follow : followingList) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", follow.getFollowingId());
                userInfo.put("nickname", follow.getFollowingName() != null ? follow.getFollowingName() : "用户" + follow.getFollowingId());
                userInfo.put("avatar", follow.getFollowingAvatar() != null ? follow.getFollowingAvatar() : "/static/images/garfield-default-avatar.png");
                userInfo.put("followTime", follow.getCreateTime());
                result.add(userInfo);
            }
            
            return Result.success(result);
        }, "获取关注用户列表失败，请稍后重试", log);
    }

    @GetMapping("/users/{userId}/recommended")
    @Operation(summary = "获取推荐用户列表（前端调用）")
    public Result<List<Map<String, Object>>> getRecommendedUsers(@PathVariable Long userId) {
        try {
            // 排除已关注与自身
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            java.util.Set<Long> excluded = new java.util.HashSet<>();
            for (UserFollow f : followingList) {
                excluded.add(f.getFollowingId());
                log.debug("已关注用户ID: {}", f.getFollowingId());
            }
            excluded.add(userId);
            log.debug("排除的用户ID集合: {}", excluded);

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            int targetCount = 10; // 目标返回10个用户
            
            // 首先尝试查询status=1的用户（正常状态的用户），排除已关注和自身
            // 优先取正常状态用户，查询上限 100 以保证有足够的可推荐候选
            java.util.List<com.pethome.entity.User> users1 = userService.getRecentUsers(userId, true, 100);
            log.debug("查询到status=1的用户数: {}", users1.size());
            
            for (com.pethome.entity.User u : users1) {
                if (excluded.contains(u.getId())) {
                    log.debug("跳过已关注的用户ID: {}", u.getId());
                    continue;
                }
                if (result.size() >= targetCount) break;
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                // 优先昵称；无昵称时若用户名像账号ID（wx_、微信用户等）则显示「用户+ID」，否则用用户名
                String displayName = u.getNickname();
                if (displayName == null || displayName.isEmpty()) {
                    String uname = u.getUsername();
                    if (uname != null && !uname.isEmpty() && !isAccountLikeUsername(uname)) {
                        displayName = uname;
                    } else {
                        displayName = "用户" + u.getId();
                    }
                }
                if (displayName == null || displayName.isEmpty()) {
                    displayName = "用户" + u.getId();
                }
                m.put("name", displayName);
                m.put("nickname", u.getNickname() != null ? u.getNickname() : "");
                m.put("username", u.getUsername() != null ? u.getUsername() : "");
                // 确保头像URL不为空且有效
                String avatarUrl = u.getAvatar();
                // 检查是否是有效的URL（包含http://或https://或/开头），排除临时文件标识符
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                } else if (avatarUrl.length() < 10) {
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                } else if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/")) {
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                }
                m.put("avatar", avatarUrl);
                m.put("avatarUrl", avatarUrl); // 同时提供avatarUrl字段以兼容前端
                // 获取粉丝数
                int fansCount = userFollowService.getFollowerCount(u.getId());
                m.put("fansCount", fansCount);
                result.add(m);
            }
            
            // 如果还不够，查询所有用户（不限制status），排除已关注和自身
            if (result.size() < targetCount) {
            // 不足时放开状态限制，仅排除自身
            java.util.List<com.pethome.entity.User> users2 = userService.getRecentUsers(userId, false, 100);
                log.debug("查询到所有用户数（不限制status）: {}", users2.size());
                
                for (com.pethome.entity.User u : users2) {
                    if (excluded.contains(u.getId())) {
                        log.debug("跳过已关注的用户ID: {}", u.getId());
                        continue;
                    }
                    if (result.size() >= targetCount) break;
                    // 检查是否已添加（避免重复）
                    boolean alreadyAdded = result.stream().anyMatch(m -> m.get("id").equals(u.getId()));
                    if (alreadyAdded) continue;
                    
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    // 优先昵称；无昵称时若用户名像账号ID则显示「用户+ID」，否则用用户名
                    String displayName = u.getNickname();
                    if (displayName == null || displayName.isEmpty()) {
                        String uname = u.getUsername();
                        if (uname != null && !uname.isEmpty() && !isAccountLikeUsername(uname)) {
                            displayName = uname;
                        } else {
                            displayName = "用户" + u.getId();
                        }
                    }
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = "用户" + u.getId();
                    }
                    m.put("name", displayName);
                    m.put("nickname", u.getNickname() != null ? u.getNickname() : "");
                    m.put("username", u.getUsername() != null ? u.getUsername() : "");
                    // 确保头像URL不为空且有效
                    String avatarUrl = u.getAvatar();
                    // 检查是否是有效的URL（包含http://或https://或/开头），排除临时文件标识符
                    if (avatarUrl == null || avatarUrl.isEmpty() || 
                        avatarUrl.length() < 10 || 
                        (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/"))) {
                        avatarUrl = "/static/images/garfield-default-avatar.png";
                    }
                    m.put("avatar", avatarUrl);
                    m.put("avatarUrl", avatarUrl); // 同时提供avatarUrl字段以兼容前端
                    // 获取粉丝数
                    int fansCount = userFollowService.getFollowerCount(u.getId());
                    m.put("fansCount", fansCount);
                    result.add(m);
                }
            }

            // 再次过滤，确保已关注的用户不在推荐列表中
            List<Map<String, Object>> finalResult = new java.util.ArrayList<>();
            for (Map<String, Object> user : result) {
                Long userIdInResult = ((Number) user.get("id")).longValue();
                String userName = (String) user.get("name");
                if (excluded.contains(userIdInResult)) {
                    log.debug("过滤掉已关注的用户ID: {}, 用户名: {}", userIdInResult, userName);
                    continue;
                }
                log.debug("保留推荐用户ID: {}, 用户名: {}", userIdInResult, userName);
                finalResult.add(user);
            }
            
            log.debug("推荐用户查询: 当前用户ID={}, 已关注用户数={}, 初步筛选用户数={}, 最终用户数={}",
                userId, followingList.size(), result.size(), finalResult.size());

            return Result.success(finalResult);
        } catch (Exception e) {
            log.error("获取推荐用户列表失败", e);
            return Result.error("获取推荐用户列表失败，请稍后重试");
        }
    }

    /** 判断是否为账号型用户名（如 wx_xxx、微信用户xxx），不应直接作为展示昵称 */
    private static boolean isAccountLikeUsername(String username) {
        if (username == null || username.isEmpty()) return true;
        String s = username.trim();
        return s.startsWith("wx_") || s.startsWith("微信用户") || s.startsWith("wechat_");
    }

}
