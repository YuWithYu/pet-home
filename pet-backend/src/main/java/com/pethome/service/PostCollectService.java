package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Post;

import java.util.List;

/**
 * 帖子收藏服务接口
 */
public interface PostCollectService {
    
    /**
     * 收藏帖子
     */
    boolean collectPost(Long postId, Long userId);
    
    /**
     * 取消收藏帖子
     */
    boolean uncollectPost(Long postId, Long userId);
    
    /**
     * 检查用户是否已收藏帖子
     */
    boolean isCollected(Long postId, Long userId);
    
    /**
     * 获取用户收藏的帖子列表
     */
    Page<Post> getCollectedPosts(Long userId, Page<Post> page, String type);
}

