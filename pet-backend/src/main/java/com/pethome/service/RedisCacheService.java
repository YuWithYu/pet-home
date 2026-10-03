package com.pethome.service;

import com.pethome.common.Const;
import com.pethome.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== 用户统计缓存 ==========
    
    /**
     * 获取用户统计数据
     */
    public Map<String, Integer> getUserStats(Long userId) {
        String key = RedisKeyUtil.getUserStatsKey(userId);
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        
        if (map.isEmpty()) {
            return null;
        }
        
        Map<String, Integer> stats = new HashMap<>();
        stats.put("followCount", getIntValue(map.get("followCount")));
        stats.put("fansCount", getIntValue(map.get("fansCount")));
        stats.put("postCount", getIntValue(map.get("postCount")));
        stats.put("likeCount", getIntValue(map.get("likeCount")));
        stats.put("collectCount", getIntValue(map.get("collectCount")));
        
        return stats;
    }
    
    /**
     * 设置用户统计数据
     */
    public void setUserStats(Long userId, Map<String, Integer> stats) {
        String key = RedisKeyUtil.getUserStatsKey(userId);
        Map<String, Object> hashMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        redisTemplate.opsForHash().putAll(key, hashMap);
        redisTemplate.expire(key, 24, TimeUnit.HOURS); // 24小时过期
    }
    
    /**
     * 更新用户统计（增量）
     */
    public void incrementUserStat(Long userId, String field, int delta) {
        String key = RedisKeyUtil.getUserStatsKey(userId);
        redisTemplate.opsForHash().increment(key, field, delta);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    /**
     * 清除用户统计缓存（强制重新从数据库查询）
     */
    public void clearUserStats(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String key = RedisKeyUtil.getUserStatsKey(userId);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("清除用户统计缓存失败，userId: {}", userId);
        }
    }
    
    // ========== 帖子统计缓存 ==========
    
    /**
     * 获取帖子浏览量
     */
    public Integer getPostViewCount(Long postId) {
        String key = "post:view:" + postId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value.toString()) : null;
    }
    
    /**
     * 增加帖子浏览量
     */
    public Long incrementPostView(Long postId) {
        String key = "post:view:" + postId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS); // 7天过期
        return count;
    }
    
    /**
     * 设置帖子浏览量
     */
    public void setPostViewCount(Long postId, Integer count) {
        String key = "post:view:" + postId;
        redisTemplate.opsForValue().set(key, count);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 获取帖子点赞量
     */
    public Integer getPostLikeCount(Long postId) {
        String key = "post:like:" + postId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value.toString()) : null;
    }
    
    /**
     * 增加帖子点赞量
     */
    public Long incrementPostLike(Long postId) {
        String key = "post:like:" + postId;
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        return count;
    }
    
    /**
     * 减少帖子点赞量
     */
    public Long decrementPostLike(Long postId) {
        String key = "post:like:" + postId;
        Long count = redisTemplate.opsForValue().decrement(key);
        if (count != null && count < 0) {
            redisTemplate.opsForValue().set(key, 0);
            return 0L;
        }
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        return count != null ? count : 0L;
    }
    
    /**
     * 设置帖子点赞量
     */
    public void setPostLikeCount(Long postId, Integer count) {
        String key = "post:like:" + postId;
        redisTemplate.opsForValue().set(key, count);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 获取帖子统计数据
     */
    public Map<String, Integer> getPostStats(Long postId) {
        String key = RedisKeyUtil.getPostStatsKey(postId);
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        
        if (map.isEmpty()) {
            return null;
        }
        
        Map<String, Integer> stats = new HashMap<>();
        stats.put("likeCount", getIntValue(map.get("likeCount")));
        stats.put("collectCount", getIntValue(map.get("collectCount")));
        stats.put("commentCount", getIntValue(map.get("commentCount")));
        
        return stats;
    }
    
    /**
     * 设置帖子统计数据
     */
    public void setPostStats(Long postId, Map<String, Integer> stats) {
        String key = RedisKeyUtil.getPostStatsKey(postId);
        Map<String, Object> hashMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
        redisTemplate.opsForHash().putAll(key, hashMap);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    /**
     * 更新帖子统计（增量）
     */
    public void incrementPostStat(Long postId, String field, int delta) {
        String key = RedisKeyUtil.getPostStatsKey(postId);
        redisTemplate.opsForHash().increment(key, field, delta);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    
    /**
     * 获取所有需要同步的帖子ID（浏览量和点赞量）
     */
    public Set<String> getPostKeysToSync(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys : new HashSet<>();
    }
    
    // ========== 关注/粉丝关系缓存 ==========
    
    /**
     * 获取用户的关注列表（从Redis）
     */
    public Set<Long> getUserFollowIds(Long userId) {
        String key = RedisKeyUtil.getUserFollowKey(userId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        
        Set<Long> ids = new HashSet<>();
        if (members != null) {
            for (Object member : members) {
                if (member instanceof Long) {
                    ids.add((Long) member);
                } else if (member instanceof String) {
                    ids.add(Long.parseLong((String) member));
                }
            }
        }
        return ids;
    }
    
    /**
     * 获取用户的粉丝列表（从Redis）
     */
    public Set<Long> getUserFansIds(Long userId) {
        String key = RedisKeyUtil.getUserFansKey(userId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        
        Set<Long> ids = new HashSet<>();
        if (members != null) {
            for (Object member : members) {
                if (member instanceof Long) {
                    ids.add((Long) member);
                } else if (member instanceof String) {
                    ids.add(Long.parseLong((String) member));
                }
            }
        }
        return ids;
    }
    
    /**
     * 添加关注关系
     */
    public void addFollow(Long userId, Long followUserId) {
        String followKey = RedisKeyUtil.getUserFollowKey(userId);
        String fansKey = RedisKeyUtil.getUserFansKey(followUserId);
        
        redisTemplate.opsForSet().add(followKey, followUserId);
        redisTemplate.opsForSet().add(fansKey, userId);
        
        redisTemplate.expire(followKey, 7, TimeUnit.DAYS);
        redisTemplate.expire(fansKey, 7, TimeUnit.DAYS);
    }
    
    /**
     * 删除关注关系
     */
    public void removeFollow(Long userId, Long followUserId) {
        String followKey = RedisKeyUtil.getUserFollowKey(userId);
        String fansKey = RedisKeyUtil.getUserFansKey(followUserId);
        
        redisTemplate.opsForSet().remove(followKey, followUserId);
        redisTemplate.opsForSet().remove(fansKey, userId);
    }
    
    /**
     * 批量设置关注列表
     */
    public void setFollowList(Long userId, Set<Long> followIds) {
        String key = RedisKeyUtil.getUserFollowKey(userId);
        if (followIds.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }
        redisTemplate.delete(key);
        for (Long id : followIds) {
            redisTemplate.opsForSet().add(key, id);
        }
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 批量设置粉丝列表
     */
    public void setFansList(Long userId, Set<Long> fansIds) {
        String key = RedisKeyUtil.getUserFansKey(userId);
        if (fansIds.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }
        redisTemplate.delete(key);
        for (Long id : fansIds) {
            redisTemplate.opsForSet().add(key, id);
        }
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 检查是否已关注
     */
    public boolean isFollowing(Long userId, Long followUserId) {
        String key = RedisKeyUtil.getUserFollowKey(userId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, followUserId));
    }
    
    // ========== 热门帖子缓存 ==========
    
    /**
     * 获取热门帖子ID列表
     */
    public List<Long> getHotPostIds(int limit) {
        String key = RedisKeyUtil.getHotPostsKey();
        Set<Object> postIds = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        
        List<Long> ids = new ArrayList<>();
        if (postIds != null) {
            for (Object id : postIds) {
                if (id instanceof Long) {
                    ids.add((Long) id);
                } else if (id instanceof String) {
                    ids.add(Long.parseLong((String) id));
                }
            }
        }
        return ids;
    }
    
    /**
     * 更新帖子热度分数
     */
    public void updatePostHotScore(Long postId, double score) {
        String key = RedisKeyUtil.getHotPostsKey();
        redisTemplate.opsForZSet().add(key, postId, score);
        redisTemplate.expire(key, 1, TimeUnit.HOURS); // 1小时过期，需要定时更新
    }
    
    /**
     * 批量设置热门帖子
     */
    public void setHotPosts(Map<Long, Double> postScores) {
        String key = RedisKeyUtil.getHotPostsKey();
        redisTemplate.delete(key);
        for (Map.Entry<Long, Double> entry : postScores.entrySet()) {
            redisTemplate.opsForZSet().add(key, entry.getKey(), entry.getValue());
        }
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    /**
     * 帖子从库中删除后，清理与该帖相关的 Redis 键（浏览/点赞/统计/热门榜/实体赞集合）
     */
    public void evictPostCache(Long postId) {
        if (postId == null) {
            return;
        }
        try {
            redisTemplate.delete("post:view:" + postId);
            redisTemplate.delete("post:like:" + postId);
            redisTemplate.delete(RedisKeyUtil.getPostStatsKey(postId));
            redisTemplate.opsForZSet().remove(RedisKeyUtil.getHotPostsKey(), postId);
            redisTemplate.delete(RedisKeyUtil.getEntityLikeKey(Const.like.ENTITY_TYPE_POST, postId));
        } catch (Exception e) {
            log.warn("清除帖子缓存失败 postId={}", postId);
        }
    }
    
    // ========== 通用缓存操作 ==========
    
    /**
     * 获取缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getCache(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 设置缓存值（带过期时间，单位：分钟）
     */
    public void setCache(String key, Object value, long minutes) {
        try {
            redisTemplate.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
        } catch (Exception e) {
        }
    }
    
    /**
     * 设置缓存值（带过期时间，自定义时间单位）
     */
    public void setCache(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
        }
    }
    
    /**
     * 如果 key 不存在则设置（分布式锁）
     * @return true 如果设置成功（key不存在），false 如果key已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
            if (Boolean.TRUE.equals(result)) {
                redisTemplate.expire(key, timeout, unit);
            }
            return result;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 设置缓存值（不过期）
     */
    public void setCache(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
        }
    }
    
    /**
     * 删除缓存
     */
    public void deleteCache(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
        }
    }
    
    /**
     * 删除缓存（别名方法，保持兼容）
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 批量删除缓存
     */
    public void deleteKeys(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    // ========== 辅助方法 ==========
    
    private int getIntValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return 0;
    }
}

