package com.pethome.util;

/**
 * Redis 缓存 key 统一管理
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_STATS = "user_stats";
    private static final String PREFIX_POST_STATS = "post_stats";
    private static final String PREFIX_HOT_POSTS = "hot_posts";
    private static final String PREFIX_USER_FOLLOW = "user_follow";
    private static final String PREFIX_USER_FANS = "user_fans";

    // 用户统计数据
    public static String getUserStatsKey(Long userId) {
        return PREFIX_USER_STATS + SPLIT + userId;
    }

    // 帖子统计数据
    public static String getPostStatsKey(Long postId) {
        return PREFIX_POST_STATS + SPLIT + postId;
    }

    // 热门帖子列表
    public static String getHotPostsKey() {
        return PREFIX_HOT_POSTS;
    }

    // 用户关注列表
    public static String getUserFollowKey(Long userId) {
        return PREFIX_USER_FOLLOW + SPLIT + userId;
    }

    // 用户粉丝列表
    public static String getUserFansKey(Long userId) {
        return PREFIX_USER_FANS + SPLIT + userId;
    }

    // 某个实体的赞
    // like:entity:entityType:entityId : userId
    public static String getEntityLikeKey(int entityType, Long entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

}
