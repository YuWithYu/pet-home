package com.pethome.service.community.support;

import com.pethome.entity.Post;
import com.pethome.mapper.PostMapper;
import com.pethome.mq.BehaviorEventPublisher;
import com.pethome.service.common.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 帖子计数维护组件。
 * <p>
 * 负责浏览量、点赞数、评论数与曝光分数的 Redis 缓存更新与数据库回写，不参与帖子查询与视图装配。
 */
@Service
public class PostCounterService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private BehaviorEventPublisher behaviorEventPublisher;

    /**
     * 增加帖子浏览量（Redis 计数 + DB 回写）。
     */
    public void incrementViews(Long postId) {
        Long viewCount = redisCacheService.incrementPostView(postId);

        // 旁路埋点：Kafka 未启用时为空操作，不影响浏览量主链路
        behaviorEventPublisher.publishPostView(postId, null);

        if (viewCount == null || viewCount == 1) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                Integer currentViews = post.getViewsCount() != null ? post.getViewsCount() : 0;
                redisCacheService.setPostViewCount(postId, currentViews + 1);
                viewCount = (long) (currentViews + 1);
            }
        }

        if (viewCount != null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setViewsCount(viewCount.intValue());
                postMapper.updateById(post);
            }
        }
    }

    /**
     * 更新帖子点赞数（Redis + 立即回写数据库）。
     */
    public void updateLikesCount(Long postId, int delta) {
        // 1. 更新 Redis 缓存
        if (delta > 0) {
            redisCacheService.incrementPostLike(postId);
        } else if (delta < 0) {
            redisCacheService.decrementPostLike(postId);
        }

        Integer newCount = redisCacheService.getPostLikeCount(postId);
        if (newCount == null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                Integer currentLikes = post.getLikesCount() != null ? post.getLikesCount() : 0;
                newCount = Math.max(0, currentLikes + delta);
                redisCacheService.setPostLikeCount(postId, newCount);
            }
        }

        // 2. 立即回写数据库，避免仅依赖定时同步导致刷新后点赞数丢失
        if (newCount != null) {
            Post post = postMapper.selectById(postId);
            if (post != null) {
                post.setLikesCount(newCount);
                postMapper.updateById(post);
            }
        }
    }

    /**
     * 更新帖子评论数（DB + Redis 统计）。
     */
    public void updateCommentsCount(Long postId, int delta) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setCommentsCount(Math.max(0, post.getCommentsCount() + delta));
            postMapper.updateById(post);

            // 更新Redis缓存
            redisCacheService.incrementPostStat(postId, "commentCount", delta);
        }
    }

    /**
     * 互动时更新帖子曝光分数。
     */
    public void updateExposureScoreOnInteraction(Long postId, String interactionType) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }

        // 根据不同互动类型增加曝光分数
        double increment = 0.0;
        switch (interactionType.toLowerCase()) {
            case "like":
                increment = 2.0; // 点赞增加2分
                break;
            case "comment":
                increment = 3.0; // 评论增加3分
                break;
            case "view":
                increment = 0.1; // 浏览增加0.1分
                break;
            default:
                increment = 1.0; // 其他互动增加1分
        }

        double currentScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
        post.setExposureScore(currentScore + increment);
        postMapper.updateById(post);
    }
}
