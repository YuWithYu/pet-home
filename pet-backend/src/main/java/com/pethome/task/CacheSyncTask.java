package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Redis缓存同步任务
 * 定期将数据库数据同步到Redis，保证缓存一致性
 */
@Component
public class CacheSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(CacheSyncTask.class);
    
    private static final String HOT_KEYWORDS_KEY = "hot_keywords";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisCacheService redisCacheService;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 同步用户统计数据（每小时执行一次）
     * 定时将数据库的用户统计同步到Redis
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时整点执行
    public void syncUserStats() {
        logger.info("开始同步用户统计数据到Redis");
        
        try {
            // 获取所有活跃用户（简化：这里可以优化为只同步活跃用户）
            // 实际项目中应该只同步最近活跃的用户，避免全量同步
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.pethome.entity.User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("status", 1).last("LIMIT 1000"); // 限制1000个用户避免负载过大
            
            List<com.pethome.entity.User> users = userMapper.selectList(wrapper);
            
            int syncCount = 0;
            for (com.pethome.entity.User user : users) {
                Map<String, Integer> stats = new HashMap<>();
                
                // 关注数
                Integer followCount = userMapper.countFollow(user.getId());
                stats.put("followCount", followCount != null ? followCount : 0);
                
                // 粉丝数
                Integer fansCount = userMapper.countFans(user.getId());
                stats.put("fansCount", fansCount != null ? fansCount : 0);
                
                // 动态数
                Integer postCount = userMapper.countPost(user.getId());
                stats.put("postCount", postCount != null ? postCount : 0);
                
                // 获赞数
                Integer likeCount = userMapper.countLikes(user.getId());
                stats.put("likeCount", likeCount != null ? likeCount : 0);
                
                stats.put("collectCount", 0); // 收藏数，如果后续有收藏功能再实现
                
                redisCacheService.setUserStats(user.getId(), stats);
                syncCount++;
            }
            
            logger.info("用户统计数据同步完成，共同步 {} 个用户", syncCount);
        } catch (Exception e) {
            logger.error("同步用户统计数据失败", e);
        }
    }

    /**
     * 同步关注/粉丝关系数据（每30分钟执行一次）
     */
    @Scheduled(cron = "0 */30 * * * ?") // 每30分钟执行
    public void syncFollowRelations() {
        logger.info("开始同步关注关系数据到Redis");
        
        try {
            // 获取所有关注关系
            QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
            List<UserFollow> follows = userFollowMapper.selectList(wrapper);
            
            // 按用户ID分组
            Map<Long, Set<Long>> userFollowsMap = new HashMap<>();
            Map<Long, Set<Long>> userFansMap = new HashMap<>();
            
            for (UserFollow follow : follows) {
                // 关注列表
                userFollowsMap.computeIfAbsent(follow.getFollowerId(), k -> new HashSet<>())
                    .add(follow.getFollowingId());
                
                // 粉丝列表
                userFansMap.computeIfAbsent(follow.getFollowingId(), k -> new HashSet<>())
                    .add(follow.getFollowerId());
            }
            
            // 写入Redis
            int syncCount = 0;
            for (Map.Entry<Long, Set<Long>> entry : userFollowsMap.entrySet()) {
                redisCacheService.setFollowList(entry.getKey(), entry.getValue());
                syncCount++;
            }
            
            for (Map.Entry<Long, Set<Long>> entry : userFansMap.entrySet()) {
                redisCacheService.setFansList(entry.getKey(), entry.getValue());
            }
            
            logger.info("关注关系数据同步完成，共同步 {} 个用户的关注列表", syncCount);
        } catch (Exception e) {
            logger.error("同步关注关系数据失败", e);
        }
    }

    /**
     * 更新热门帖子列表（每15分钟执行一次）
     */
    @Scheduled(cron = "0 */15 * * * ?") // 每15分钟执行
    public void updateHotPosts() {
        logger.info("开始更新热门帖子列表");
        
        try {
            // 查询所有正常状态的帖子
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Post> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("status", 1).last("LIMIT 500"); // 限制500个帖子
            
            List<Post> posts = postMapper.selectList(wrapper);
            
            // 计算热度分数并排序
            Map<Long, Double> postScores = new HashMap<>();
            for (Post post : posts) {
                double likesScore = (post.getLikesCount() != null ? post.getLikesCount() : 0) * 0.5;
                double commentsScore = (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * 0.3;
                double viewsScore = (post.getViewsCount() != null ? post.getViewsCount() : 0) * 0.2;
                double exposureScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
                
                double hotScore = likesScore + commentsScore + viewsScore + exposureScore;
                postScores.put(post.getId(), hotScore);
            }
            
            // 更新到Redis
            redisCacheService.setHotPosts(postScores);
            
            logger.info("热门帖子列表更新完成，共更新 {} 个帖子", postScores.size());
        } catch (Exception e) {
            logger.error("更新热门帖子列表失败", e);
        }
    }

    /**
     * 同步帖子统计数据（每小时执行一次）
     */
    @Scheduled(cron = "0 30 * * * ?") // 每小时30分执行
    public void syncPostStats() {
        logger.info("开始同步帖子统计数据到Redis");
        
        try {
            // 获取最近活跃的帖子
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Post> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("status", 1)
                   .orderByDesc("create_time")
                   .last("LIMIT 1000");
            
            List<Post> posts = postMapper.selectList(wrapper);
            
            int syncCount = 0;
            for (Post post : posts) {
                Map<String, Integer> stats = new HashMap<>();
                stats.put("likeCount", post.getLikesCount() != null ? post.getLikesCount() : 0);
                stats.put("commentCount", post.getCommentsCount() != null ? post.getCommentsCount() : 0);
                stats.put("collectCount", 0);
                
                redisCacheService.setPostStats(post.getId(), stats);
                syncCount++;
            }
            
            logger.info("帖子统计数据同步完成，共同步 {} 个帖子", syncCount);
        } catch (Exception e) {
            logger.error("同步帖子统计数据失败", e);
        }
    }
    
    /**
     * 衰减热搜关键词热度（每天凌晨2点执行）
     * 对热词的score进行衰减，避免过时热词长期占据榜单
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void decayHotKeywords() {
        logger.info("开始衰减热搜关键词热度");
        
        if (redisTemplate == null) {
            logger.warn("Redis不可用，跳过热搜关键词衰减");
            return;
        }
        
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                    .rangeWithScores(HOT_KEYWORDS_KEY, 0, -1);
            
            if (tuples == null || tuples.isEmpty()) {
                logger.info("没有热搜关键词需要衰减");
                return;
            }
            
            int decayCount = 0;
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Object keyword = tuple.getValue();
                Double currentScore = tuple.getScore();
                
                if (keyword != null && currentScore != null) {
                    double newScore = currentScore * 0.9; // 衰减到原来的90%
                    
                    // 如果分数过低（小于1），直接删除
                    if (newScore < 1.0) {
                        redisTemplate.opsForZSet().remove(HOT_KEYWORDS_KEY, keyword);
                    } else {
                        redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, newScore - currentScore);
                    }
                    decayCount++;
                }
            }
            
            logger.info("热搜关键词衰减完成，共处理 {} 个关键词", decayCount);
        } catch (Exception e) {
            logger.error("衰减热搜关键词失败", e);
        }
    }
}

