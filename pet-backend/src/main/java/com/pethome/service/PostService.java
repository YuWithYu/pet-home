package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.Post;

import java.util.List;

public interface PostService extends IService<Post> {
    
    /**
     * 获取帖子列表（带分页和筛选）
     */
    Page<Post> getPostList(Page<Post> page, String category, Long userId);
    
    /**
     * 获取帖子详情（包含用户信息和点赞状态）
     */
    Post getPostDetail(Long postId, Long currentUserId);
    
    /**
     * 获取热门帖子
     */
    List<Post> getHotPosts(Integer limit);
    
    /**
     * 获取关注用户的帖子
     */
    Page<Post> getFollowingPosts(Long userId, Page<Post> page);
    
    /**
     * 增加帖子浏览量
     */
    void incrementViews(Long postId);
    
    /**
     * 更新帖子点赞数
     */
    void updateLikesCount(Long postId, int delta);
    
    /**
     * 更新帖子评论数
     */
    void updateCommentsCount(Long postId, int delta);
    
    /**
     * 获取管理员帖子列表（支持搜索和筛选）
     */
    Page<Post> getAdminPostList(Page<Post> page, String keyword, Integer status, String category);
    
    /**
     * 获取用户的所有帖子
     */
    List<Post> getUserPosts(Long userId);
    
    /**
     * 获取置顶帖子列表
     */
    List<Post> getTopPosts(Integer limit);
    
    /**
     * 根据热度分数获取热门帖子列表
     */
    List<Post> getHotPostsByScore(Integer limit);
    
    /**
     * 获取最新帖子列表
     */
    List<Post> getLatestPosts(Integer limit);
    
    /**
     * 计算帖子热度分数
     */
    Double calculateHotnessScore(Post post);
    
    /**
     * 更新帖子曝光分数（互动时调用）
     */
    void updateExposureScoreOnInteraction(Long postId, String interactionType);
    
    /**
     * 获取用户今日已发布的帖子数量
     */
    int getTodayPostCount(Long userId);

    /**
     * 视频推荐流（类抖音）：仅视频帖，按热度排序，分页
     */
    Page<Post> getVideoFeed(Page<Post> page);

    /**
     * 管理员物理删除帖子：删除 post 行并级联清理评论、互动、专题关联与缓存。
     *
     * @return 是否删除成功（帖子存在且已删除）
     */
    boolean deletePostPhysically(Long postId);
}