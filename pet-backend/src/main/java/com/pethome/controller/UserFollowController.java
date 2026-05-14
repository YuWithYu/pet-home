package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.UserFollow;
import com.pethome.service.UserFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@Api(tags = "用户关注管理")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @Autowired
    private com.pethome.mapper.UserMapper userMapper;

    @PostMapping("/follow")
    @ApiOperation("关注用户")
    public Result<Boolean> followUser(@RequestParam Long followingId, @RequestParam Long followerId) {
        try {
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
            return Result.error("关注用户失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/follow")
    @ApiOperation("取消关注用户")
    public Result<Boolean> unfollowUser(@RequestParam Long followingId, @RequestParam Long followerId) {
        try {
            boolean success = userFollowService.unfollowUser(followerId, followingId);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("取消关注失败，可能没有关注过");
            }
        } catch (Exception e) {
            return Result.error("取消关注用户失败: " + e.getMessage());
        }
    }

    @GetMapping("/follow/status")
    @ApiOperation("检查关注状态")
    public Result<Boolean> checkFollowStatus(@RequestParam Long followerId, @RequestParam Long followingId) {
        try {
            boolean isFollowing = userFollowService.isFollowing(followerId, followingId);
            return Result.success(isFollowing);
        } catch (Exception e) {
            return Result.error("检查关注状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/following")
    @ApiOperation("获取用户关注列表")
    public Result<List<UserFollow>> getFollowingList(@PathVariable Long userId) {
        try {
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            return Result.success(followingList);
        } catch (Exception e) {
            return Result.error("获取关注列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/followers")
    @ApiOperation("获取用户粉丝列表")
    public Result<List<UserFollow>> getFollowerList(@PathVariable Long userId) {
        try {
            List<UserFollow> followerList = userFollowService.getFollowerList(userId);
            return Result.success(followerList);
        } catch (Exception e) {
            return Result.error("获取粉丝列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/follow-stats")
    @ApiOperation("获取用户关注统计")
    public Result<Map<String, Object>> getFollowStats(@PathVariable Long userId) {
        try {
            int followingCount = userFollowService.getFollowingCount(userId);
            int followerCount = userFollowService.getFollowerCount(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("followingCount", followingCount);
            stats.put("followerCount", followerCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取关注统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/followed")
    @ApiOperation("获取用户关注的用户列表（前端调用）")
    public Result<List<Map<String, Object>>> getFollowedUsers(@PathVariable Long userId) {
        try {
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
        } catch (Exception e) {
            return Result.error("获取关注用户列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/recommended")
    @ApiOperation("获取推荐用户列表（前端调用）")
    public Result<List<Map<String, Object>>> getRecommendedUsers(@PathVariable Long userId) {
        try {
            // 排除已关注与自身
            List<UserFollow> followingList = userFollowService.getFollowingList(userId);
            java.util.Set<Long> excluded = new java.util.HashSet<>();
            for (UserFollow f : followingList) {
                excluded.add(f.getFollowingId());
                System.out.println("已关注用户ID: " + f.getFollowingId());
            }
            excluded.add(userId);
            System.out.println("排除的用户ID集合: " + excluded);

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            int targetCount = 10; // 目标返回10个用户
            
            // 首先尝试查询status=1的用户（正常状态的用户），排除已关注和自身
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.pethome.entity.User> qw1 =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            qw1.eq("status", 1)
               .ne("id", userId) // 排除自身
               .orderByDesc("id")
               .last("LIMIT 100"); // 增加查询数量，确保有足够的选择
            java.util.List<com.pethome.entity.User> users1 = userMapper.selectList(qw1);
            System.out.println("查询到status=1的用户数: " + users1.size());
            
            for (com.pethome.entity.User u : users1) {
                if (excluded.contains(u.getId())) {
                    System.out.println("跳过已关注的用户ID: " + u.getId());
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
                System.out.println("用户" + u.getId() + "原始头像: " + avatarUrl);
                // 检查是否是有效的URL（包含http://或https://或/开头），排除临时文件标识符
                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    System.out.println("用户" + u.getId() + "头像为空，使用默认头像");
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                } else if (avatarUrl.length() < 10) {
                    System.out.println("用户" + u.getId() + "头像URL太短(" + avatarUrl.length() + ")，可能是临时标识符，使用默认头像");
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                } else if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://") && !avatarUrl.startsWith("/")) {
                    System.out.println("用户" + u.getId() + "头像URL格式无效，使用默认头像");
                    avatarUrl = "/static/images/garfield-default-avatar.png";
                } else {
                    System.out.println("用户" + u.getId() + "头像URL有效: " + avatarUrl);
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
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.pethome.entity.User> qw2 =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                qw2.ne("id", userId) // 排除自身
                   .orderByDesc("id")
                   .last("LIMIT 100"); // 查询更多用户
                java.util.List<com.pethome.entity.User> users2 = userMapper.selectList(qw2);
                System.out.println("查询到所有用户数（不限制status）: " + users2.size());
                
                for (com.pethome.entity.User u : users2) {
                    if (excluded.contains(u.getId())) {
                        System.out.println("跳过已关注的用户ID: " + u.getId());
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
            System.out.println("========== 开始最终过滤 ==========");
            System.out.println("初步筛选后的用户列表ID: " + result.stream().map(u -> ((Number) u.get("id")).longValue()).collect(java.util.stream.Collectors.toList()));
            for (Map<String, Object> user : result) {
                Long userIdInResult = ((Number) user.get("id")).longValue();
                String userName = (String) user.get("name");
                if (excluded.contains(userIdInResult)) {
                    System.out.println("过滤掉已关注的用户ID: " + userIdInResult + ", 用户名: " + userName);
                    continue;
                }
                System.out.println("保留推荐用户ID: " + userIdInResult + ", 用户名: " + userName);
                finalResult.add(user);
            }
            
            System.out.println("========== 推荐用户查询 ==========");
            System.out.println("当前用户ID: " + userId);
            System.out.println("已关注用户数: " + followingList.size());
            System.out.println("排除的用户ID: " + excluded);
            System.out.println("初步筛选后的用户数: " + result.size());
            System.out.println("最终过滤后的用户数: " + finalResult.size());
            System.out.println("最终推荐用户ID列表: " + finalResult.stream().map(u -> ((Number) u.get("id")).longValue()).collect(java.util.stream.Collectors.toList()));
            System.out.println("====================================");

            return Result.success(finalResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取推荐用户列表失败: " + e.getMessage());
        }
    }

    /** 判断是否为账号型用户名（如 wx_xxx、微信用户xxx），不应直接作为展示昵称 */
    private static boolean isAccountLikeUsername(String username) {
        if (username == null || username.isEmpty()) return true;
        String s = username.trim();
        return s.startsWith("wx_") || s.startsWith("微信用户") || s.startsWith("wechat_");
    }

}
