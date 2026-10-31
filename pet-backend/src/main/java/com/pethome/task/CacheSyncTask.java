package com.pethome.task;

import com.pethome.service.CacheSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Redis 缓存同步调度入口。
 *
 * DB → Redis 的回填逻辑由 {@link CacheSyncService} 实现；
 * 本类仅保留调度与纯 Redis 操作（热搜降权不涉及数据库）。
 */
@Component
public class CacheSyncTask {

    private static final Logger logger = LoggerFactory.getLogger(CacheSyncTask.class);

    private static final String HOT_KEYWORDS_KEY = "hot_keywords";

    private final CacheSyncService cacheSyncService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public CacheSyncTask(CacheSyncService cacheSyncService) {
        this.cacheSyncService = cacheSyncService;
    }

    /**
     * 同步用户统计数据（每小时整点执行）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncUserStats() {
        try {
            int syncCount = cacheSyncService.syncUserStats();
            logger.info("用户统计数据同步完成，共同步 {} 个用户（4 次 GROUP BY 查询）", syncCount);
        } catch (Exception e) {
            logger.error("同步用户统计数据失败", e);
        }
    }

    /**
     * 同步关注/粉丝关系数据（每30分钟执行一次）
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void syncFollowRelations() {
        try {
            int syncCount = cacheSyncService.syncFollowRelations();
            logger.info("关注关系数据同步完成，共同步 {} 个用户的关注列表", syncCount);
        } catch (Exception e) {
            logger.error("同步关注关系数据失败", e);
        }
    }

    /**
     * 更新热门帖子列表（每15分钟执行一次）
     */
    @Scheduled(cron = "0 */15 * * * ?")
    public void updateHotPosts() {
        try {
            int size = cacheSyncService.syncHotPosts();
            logger.info("热门帖子列表更新完成，共更新 {} 个帖子", size);
        } catch (Exception e) {
            logger.error("更新热门帖子列表失败", e);
        }
    }

    /**
     * 同步帖子统计数据（每小时30分执行）
     */
    @Scheduled(cron = "0 30 * * * ?")
    public void syncPostStats() {
        try {
            int syncCount = cacheSyncService.syncPostStats();
            logger.info("帖子统计数据同步完成，共同步 {} 个帖子", syncCount);
        } catch (Exception e) {
            logger.error("同步帖子统计数据失败", e);
        }
    }

    /**
     * 衰减热搜关键词热度（每天凌晨2点执行）
     * 对热词的score进行衰减，避免过时热词长期占据榜单
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void decayHotKeywords() {
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
                if (keyword == null || currentScore == null) continue;

                double newScore = currentScore * 0.9; // 衰减到原来的90%
                // 如果分数过低（小于1），直接删除
                if (newScore < 1.0) {
                    redisTemplate.opsForZSet().remove(HOT_KEYWORDS_KEY, keyword);
                } else {
                    redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, newScore - currentScore);
                }
                decayCount++;
            }

            logger.info("热搜关键词衰减完成，共处理 {} 个关键词", decayCount);
        } catch (Exception e) {
            logger.error("衰减热搜关键词失败", e);
        }
    }
}
