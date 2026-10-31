package com.pethome.service.common;

/**
 * Redis 缓存同步（DB → Redis 的批量回填）。
 *
 * 只负责读库与回填缓存，不做任何写库操作。
 */
public interface CacheSyncService {

    /**
     * 同步活跃用户的关注/粉丝/帖子/获赞统计（4 次 GROUP BY 聚合，避免 N+1）。
     *
     * @return 同步的用户数
     */
    int syncUserStats();

    /**
     * 同步关注与粉丝关系集合。
     *
     * @return 同步的关注列表用户数
     */
    int syncFollowRelations();

    /**
     * 计算并回填热门帖子榜单。
     *
     * @return 回填的帖子数
     */
    int syncHotPosts();

    /**
     * 同步最近帖子的互动统计。
     *
     * @return 同步的帖子数
     */
    int syncPostStats();
}
