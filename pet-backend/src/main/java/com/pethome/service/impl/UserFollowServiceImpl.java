package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Notification;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.service.NotificationService;
import com.pethome.service.UserFollowService;
import com.pethome.service.RedisCacheService;
import com.pethome.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired
    private RedisCacheService redisCacheService;
    
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    @Transactional
    public boolean followUser(Long followerId, Long followingId) {
        // 不能关注自己
        if (followerId.equals(followingId)) {
            return false;
        }
        
        // 检查是否已关注
        UserFollow existingFollow = userFollowMapper.checkFollowStatus(followerId, followingId);
        if (existingFollow != null) {
            return false; // 已经关注过了
        }
        
        // 添加关注记录
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreateTime(LocalDateTime.now());
        userFollowMapper.insert(follow);
        // 社区互动通知：通知被关注者
        if (notificationService != null) {
            Notification n = new Notification();
            n.setUserId(followingId);
            n.setTitle("新粉丝");
            n.setContent("有人关注了您");
            n.setType("community_follow");
            n.setStatus(0);
            n.setRelatedId(followerId);
            n.setRelatedType("user");
            n.setCreateTime(LocalDateTime.now());
            n.setUpdateTime(LocalDateTime.now());
            notificationService.createNotification(n);
        }
        // 更新Redis缓存
        redisCacheService.addFollow(followerId, followingId);
        redisCacheService.incrementUserStat(followerId, "followCount", 1);
        redisCacheService.incrementUserStat(followingId, "fansCount", 1);
        
        // 清除用户统计缓存，强制重新从数据库查询（确保数据准确性）
        redisCacheService.clearUserStats(followerId);
        redisCacheService.clearUserStats(followingId);
        
        // 更新任务进度：关注用户任务
        try {
            // 获取用户今日已关注的用户数量
            int todayFollowCount = this.getTodayFollowCount(followerId);
            taskService.updateTaskProgress(followerId, "follow_user", todayFollowCount);
            System.out.println("更新关注用户任务进度，用户ID: " + followerId + ", 今日关注数: " + todayFollowCount);
        } catch (Exception e) {
            // 任务进度更新失败不影响关注流程
            System.err.println("更新关注用户任务进度失败: " + e.getMessage());
        }
        
        System.out.println("关注成功 - 关注者ID: " + followerId + ", 被关注者ID: " + followingId);
        System.out.println("已清除两个用户的统计缓存，下次查询将从数据库重新获取");
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean unfollowUser(Long followerId, Long followingId) {
        // 查找关注记录
        UserFollow follow = userFollowMapper.checkFollowStatus(followerId, followingId);
        if (follow == null) {
            return false; // 没有关注记录
        }
        
        // 删除关注记录
        userFollowMapper.deleteById(follow.getId());
        
        // 更新Redis缓存
        redisCacheService.removeFollow(followerId, followingId);
        redisCacheService.incrementUserStat(followerId, "followCount", -1);
        redisCacheService.incrementUserStat(followingId, "fansCount", -1);
        
        // 清除用户统计缓存，强制重新从数据库查询（确保数据准确性）
        redisCacheService.clearUserStats(followerId);
        redisCacheService.clearUserStats(followingId);
        
        System.out.println("取消关注成功 - 关注者ID: " + followerId + ", 被关注者ID: " + followingId);
        System.out.println("已清除两个用户的统计缓存，下次查询将从数据库重新获取");
        
        return true;
    }
    
    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        // 先从Redis缓存查询
        if (redisCacheService.isFollowing(followerId, followingId)) {
            return true;
        }
        
        // 缓存未命中，从数据库查询
        UserFollow follow = userFollowMapper.checkFollowStatus(followerId, followingId);
        return follow != null;
    }
    
    @Override
    public List<UserFollow> getFollowingList(Long userId) {
        // 注意：这里从数据库查询完整列表（包含用户信息），不适合全量缓存
        // Redis主要用于缓存关注关系Set，完整列表包含用户详细信息，变化频繁
        return userFollowMapper.getFollowingList(userId);
    }
    
    @Override
    public List<UserFollow> getFollowerList(Long userId) {
        // 注意：这里从数据库查询完整列表（包含用户信息），不适合全量缓存
        System.out.println("========== 查询粉丝列表 ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("SQL查询: SELECT uf.*, ... FROM user_follow uf WHERE uf.following_id = " + userId);
        
        List<UserFollow> followers = userFollowMapper.getFollowerList(userId);
        System.out.println("查询结果 - 粉丝数量: " + (followers != null ? followers.size() : 0));
        if (followers != null && !followers.isEmpty()) {
            for (UserFollow follow : followers) {
                System.out.println("  - 记录ID: " + follow.getId() + ", 粉丝ID: " + follow.getFollowerId() + ", 被关注用户ID: " + follow.getFollowingId());
            }
        } else {
            System.out.println("警告: 未找到任何粉丝记录，请检查数据库中是否有 following_id = " + userId +  " 的记录");
        }
        System.out.println("====================================");
        return followers;
    }
    
    @Override
    public int getFollowingCount(Long userId) {
        // 先尝试从Redis缓存获取
        Map<String, Integer> stats = redisCacheService.getUserStats(userId);
        if (stats != null && stats.containsKey("followCount")) {
            return stats.get("followCount");
        }
        
        // 缓存未命中，从数据库查询
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("follower_id", userId);
        return Math.toIntExact(userFollowMapper.selectCount(queryWrapper));
    }
    
    @Override
    public int getFollowerCount(Long userId) {
        // 先尝试从Redis缓存获取
        Map<String, Integer> stats = redisCacheService.getUserStats(userId);
        if (stats != null && stats.containsKey("fansCount")) {
            return stats.get("fansCount");
        }
        
        // 缓存未命中，从数据库查询
        QueryWrapper<UserFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("following_id", userId);
        return Math.toIntExact(userFollowMapper.selectCount(queryWrapper));
    }
    
    /**
     * 获取用户今日已关注的用户数量
     */
    private int getTodayFollowCount(Long userId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        wrapper.apply("DATE(create_time) = CURDATE()");
        return (int) this.count(wrapper);
    }
}
