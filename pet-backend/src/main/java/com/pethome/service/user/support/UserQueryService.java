package com.pethome.service.user.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.User;
import com.pethome.service.common.RedisCacheService;
import com.pethome.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户查询与统计相关逻辑，从 UserServiceImpl 抽取而来。
 */
@Component
public class UserQueryService {

    private static final Logger logger = LoggerFactory.getLogger(UserQueryService.class);

    // Redis 缓存 key 前缀
    private static final String USER_INFO_PREFIX = "user:info:";
    // 缓存过期时间
    private static final long USER_INFO_EXPIRE_MINUTES = 30; // 用户信息 30分钟过期

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }

        // 优先从 Redis 缓存获取
        if (stringRedisTemplate != null) {
            try {
                String userInfoKey = USER_INFO_PREFIX + id;
                String userJson = stringRedisTemplate.opsForValue().get(userInfoKey);
                if (StringUtils.hasText(userJson)) {
                    logger.info("从 Redis 缓存获取用户信息，userId: {}, key: {}", id, userInfoKey);
                    return JSONUtil.parseObject(userJson, User.class);
                }
            } catch (Exception e) {
                logger.warn("从 Redis 获取用户信息失败，降级到数据库查询，userId: {}, 错误: {}", id, e.getMessage());
            }
        }

        // 从数据库查询
        User user = mpUserMapper.selectById(id);

        if (user == null) {
            logger.warn("数据库中未找到用户，userId: {}", id);
        } else {
            logger.info("从数据库查询到用户，userId: {}, username: {}, nickname: {}, status: {}",
                id, user.getUsername(), user.getNickname(), user.getStatus());
        }

        // 写入 Redis 缓存
        if (user != null && stringRedisTemplate != null) {
            try {
                String userInfoKey = USER_INFO_PREFIX + id;
                String userJson = JSONUtil.toJSONString(user);
                stringRedisTemplate.opsForValue().set(userInfoKey, userJson, USER_INFO_EXPIRE_MINUTES, TimeUnit.MINUTES);
                logger.info("成功写入用户信息到 Redis 缓存，userId: {}, key: {}, 过期时间: {} 分钟", id, userInfoKey, USER_INFO_EXPIRE_MINUTES);
            } catch (Exception e) {
                logger.error("写入用户信息到 Redis 缓存失败，userId: {}, 错误: {}", id, e.getMessage(), e);
            }
        }

        return user;
    }

    public User getUserByUsername(String username) {
        // 使用MyBatis-Plus的查询，自动包含所有字段
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    public User getUserByPhone(String phone) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("phone", phone).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    public User getUserByNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("nickname", nickname.trim()).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("email", email).last("limit 1");
        return mpUserMapper.selectOne(qw);
    }

    public List<User> getUsersByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return mpUserMapper.selectBatchIds(ids);
    }

    public List<User> getRecentUsers(Long excludeUserId, boolean onlyActive, int limit) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (onlyActive) {
            qw.eq("status", 1);
        }
        if (excludeUserId != null) {
            qw.ne("id", excludeUserId);
        }
        qw.orderByDesc("id").last("LIMIT " + limit);
        return mpUserMapper.selectList(qw);
    }

    public IPage<User> getUserList(Page<User> page) {
        return mpUserMapper.selectPage(page, null);
    }

    public IPage<User> getUserList(Page<User> page, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return mpUserMapper.selectPage(page, null);
        }
        String kw = keyword.trim();
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.and(w -> w.like("nickname", kw)
                .or().like("phone", kw)
                .or().like("username", kw));
        return mpUserMapper.selectPage(page, qw);
    }

    public Map<String, Integer> getUserStats(Long userId) {
        Map<String, Integer> cachedStats = redisCacheService.getUserStats(userId);
        if (cachedStats != null && !cachedStats.isEmpty()) {
            Integer cachedFansCount = cachedStats.get("fansCount");
            if (cachedFansCount != null && cachedFansCount == 0) {
                Integer actualFansCount = mpUserMapper.countFans(userId);
                if (actualFansCount != null && actualFansCount > 0) {
                    redisCacheService.clearUserStats(userId);
                    cachedStats = null;
                }
            }
            if (cachedStats != null && !cachedStats.isEmpty()) {
                return cachedStats;
            }
        }

        Map<String, Integer> stats = new HashMap<>();

        Integer followCount = mpUserMapper.countFollow(userId);
        stats.put("followCount", followCount != null ? followCount : 0);

        Integer fansCount = mpUserMapper.countFans(userId);
        stats.put("fansCount", fansCount != null ? fansCount : 0);
        stats.put("fans", fansCount != null ? fansCount : 0);

        Integer postCount = mpUserMapper.countPost(userId);
        stats.put("postCount", postCount != null ? postCount : 0);

        Integer likeCount = mpUserMapper.countLikes(userId);
        stats.put("likeCount", likeCount != null ? likeCount : 0);

        redisCacheService.setUserStats(userId, stats);

        return stats;
    }

    public String getNicknameSafely(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            User user = getUserById(userId);
            return user != null ? user.getNickname() : null;
        } catch (Exception e) {
            logger.warn("查询用户昵称失败，降级为脱敏占位，userId: {}, 错误: {}", userId, e.getMessage());
            return null;
        }
    }

    public boolean isNicknameExists(String nickname, Long excludeUserId) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return false; // 空昵称不检查
        }
        Integer count = mpUserMapper.checkNicknameExists(nickname.trim(), excludeUserId);
        return count != null && count > 0;
    }
}
