package com.pethome.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Post;
import com.pethome.entity.UserFollow;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.common.CacheSyncService;
import com.pethome.service.common.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis 缓存同步实现。
 *
 * 性能设计：把原先"每用户 4 次 count"的 N+1 改为 4 条 GROUP BY 聚合查询，
 * 1000 个用户由 4000 次查询降为 4 次。
 */
@Service
public class CacheSyncServiceImpl implements CacheSyncService {

    private static final Logger logger = LoggerFactory.getLogger(CacheSyncServiceImpl.class);

    private static final int ACTIVE_USER_LIMIT = 1000;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public int syncUserStats() {
        List<Long> userIds = userMapper.selectActiveUserIds(ACTIVE_USER_LIMIT);
        if (userIds.isEmpty()) {
            return 0;
        }

        // 4 次 GROUP BY 聚合：分别统计每个用户的关注/粉丝/帖子/获赞
        Map<Long, Integer> followCountMap = batchCountToMap(userMapper.batchCountFollow(userIds));
        Map<Long, Integer> fansCountMap = batchCountToMap(userMapper.batchCountFans(userIds));
        Map<Long, Integer> postCountMap = batchCountToMap(userMapper.batchCountPost(userIds));
        Map<Long, Integer> likeCountMap = batchCountToMap(userMapper.batchCountLikes(userIds));

        int syncCount = 0;
        for (Long userId : userIds) {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("followCount", followCountMap.getOrDefault(userId, 0));
            stats.put("fansCount", fansCountMap.getOrDefault(userId, 0));
            stats.put("postCount", postCountMap.getOrDefault(userId, 0));
            stats.put("likeCount", likeCountMap.getOrDefault(userId, 0));
            // 收藏数暂无统计口径，保持与原实现一致
            stats.put("collectCount", 0);
            redisCacheService.setUserStats(userId, stats);
            syncCount++;
        }
        return syncCount;
    }

    @Override
    public int syncFollowRelations() {
        // 仅取两列：follower_id/following_id，其余字段不参与同步
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.select("follower_id", "following_id");
        List<UserFollow> follows = userFollowMapper.selectList(wrapper);

        Map<Long, Set<Long>> userFollowsMap = new HashMap<>();
        Map<Long, Set<Long>> userFansMap = new HashMap<>();
        for (UserFollow follow : follows) {
            userFollowsMap.computeIfAbsent(follow.getFollowerId(), k -> new HashSet<>())
                .add(follow.getFollowingId());
            userFansMap.computeIfAbsent(follow.getFollowingId(), k -> new HashSet<>())
                .add(follow.getFollowerId());
        }

        int syncCount = 0;
        for (Map.Entry<Long, Set<Long>> entry : userFollowsMap.entrySet()) {
            redisCacheService.setFollowList(entry.getKey(), entry.getValue());
            syncCount++;
        }
        for (Map.Entry<Long, Set<Long>> entry : userFansMap.entrySet()) {
            redisCacheService.setFansList(entry.getKey(), entry.getValue());
        }
        return syncCount;
    }

    @Override
    public int syncHotPosts() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).last("LIMIT 500");

        List<Post> posts = postMapper.selectList(wrapper);

        Map<Long, Double> postScores = new HashMap<>();
        for (Post post : posts) {
            double likesScore = (post.getLikesCount() != null ? post.getLikesCount() : 0) * 0.5;
            double commentsScore = (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * 0.3;
            double viewsScore = (post.getViewsCount() != null ? post.getViewsCount() : 0) * 0.2;
            double exposureScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
            postScores.put(post.getId(), likesScore + commentsScore + viewsScore + exposureScore);
        }

        redisCacheService.setHotPosts(postScores);
        return postScores.size();
    }

    @Override
    public int syncPostStats() {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
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
        return syncCount;
    }

    /**
     * 将 List&lt;Map&lt;userId,count&gt;&gt; 转为 Map&lt;userId,count&gt;，便于按 userId 取值
     */
    private Map<Long, Integer> batchCountToMap(List<Map<String, Object>> rows) {
        Map<Long, Integer> result = new HashMap<>();
        if (rows == null) return result;
        for (Map<String, Object> row : rows) {
            Object idObj = row.get("userId");
            Object cntObj = row.get("cnt");
            if (idObj == null) continue;
            try {
                result.put(Long.valueOf(idObj.toString()), cntObj == null ? 0 : Integer.valueOf(cntObj.toString()));
            } catch (NumberFormatException ignored) {
                // 脏数据跳过，避免整批同步失败
            }
        }
        return result;
    }
}
