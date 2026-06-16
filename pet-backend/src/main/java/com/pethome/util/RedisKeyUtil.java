package com.pethome.util;

/**
 * @author linyuhong
 * @date 2019/9/1
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_USER = "user";
    private static String PREFIX_EVENTQUEUE = "EVENT_QUEUE";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";
    private static final String PREFIX_USER_STATS = "user_stats";
    private static final String PREFIX_POST_STATS = "post_stats";
    private static final String PREFIX_HOT_POSTS = "hot_posts";
    private static final String PREFIX_USER_FOLLOW = "user_follow";
    private static final String PREFIX_USER_FANS = "user_fans";


    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
    
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

    // 事件主题
    public static String getEventqueueKey() {
        return PREFIX_EVENTQUEUE;
    }

    // 用户
    public static String getUserKey(Long userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)  zset()以关注时间进行排序
    public static String getFolloweeKey(Long userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, Long entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 生成登录凭证
     * @param ticket  UUID
     * @return  ticket:UUID
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 某个实体的赞
    // like:entity:entityType:entityId : userId
    // entityType 分为文章的赞和评论的赞两类
    public static String getEntityLikeKey(int entityType, Long entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 用户收到的赞
    // like:user:userId ---> int
    public static String getUserLikeKey(Long userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }



    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

}




