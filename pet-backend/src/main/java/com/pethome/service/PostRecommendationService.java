package com.pethome.service;

import com.pethome.entity.Post;

import java.util.List;

/**
 * 帖子推荐服务接口
 * 提供关注区和发现区的个性化推荐算法
 */
public interface PostRecommendationService {
    
    /**
     * 获取关注区帖子（个性化排序）
     * @param userId 当前用户ID
     * @param limit 返回数量限制
     * @return 排序后的帖子列表
     */
    List<Post> getFollowFeedPosts(Long userId, Integer limit);
    
    /**
     * 获取发现区帖子（智能推荐）
     * @param userId 当前用户ID
     * @param limit 返回数量限制
     * @return 排序后的帖子列表
     */
    List<Post> getExplorePosts(Long userId, Integer limit);
    
    /**
     * 计算关注区帖子的最终分数
     * base_hot_score = like*0.4 + comment*0.3 + view*0.2 + exposure_score*0.1
     * final_score = base_hot_score * (1 + α * recent_interaction + β * tag_match)
     * 
     * @param post 帖子
     * @param userId 当前用户ID
     * @return 最终分数
     */
    Double calculateFollowFeedScore(Post post, Long userId);
    
    /**
     * 计算发现区帖子的最终分数（带时间衰减）
     * base_score = like*0.5 + comment*0.3 + view*0.2 + exposure_score
     * final_score = base_score * e^(-λ * hours_since_posted) * tag_match_bonus
     * 
     * @param post 帖子
     * @param userId 当前用户ID
     * @return 最终分数
     */
    Double calculateExploreScore(Post post, Long userId);
    
    /**
     * 获取用户兴趣标签（基于历史行为）
     * @param userId 用户ID
     * @return 兴趣标签列表
     */
    List<String> getUserInterestTags(Long userId);
    
    /**
     * 计算标签匹配分数
     * @param postTags 帖子标签
     * @param userTags 用户兴趣标签
     * @return 匹配分数（0-1）
     */
    Double calculateTagMatchScore(String postTags, List<String> userTags);

    /**
     * 发现页推荐：按热度分综合排序
     *
     * @param userId 当前用户ID（可为null，未登录则无兴趣加分）
     * @param page   页码，从1开始
     * @param size   每页条数
     * @return 按热度分倒序的帖子列表
     */
    List<Post> getRecommendPosts(Long userId, Integer page, Integer size, String tag);

    /**
     * 热度推荐：按点赞、评论、收藏、浏览量综合排序
     * 可直接用于发现页
     *
     * @param limit 返回数量
     * @return 热度排序后的帖子列表
     */
    List<Post> getHotPosts(Integer limit);

    /**
     * 基于用户的协同过滤推荐（User-Based CF）
     * 使用用户-帖子互动向量计算余弦相似度，找到相似用户后进行加权推荐。
     * 冷启动或数据不足时自动回退到热度推荐。
     *
     * @param userId 目标用户ID
     * @param limit 返回数量
     * @return 推荐帖子列表
     */
    List<Post> getCollaborativeRecommendations(Long userId, int limit);
}

