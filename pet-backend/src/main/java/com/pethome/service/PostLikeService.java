package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.PostLike;

public interface PostLikeService extends IService<PostLike> {
    
    /**
     * 点赞帖子
     */
    boolean likePost(Long postId, Long userId);
    
    /**
     * 取消点赞帖子
     */
    boolean unlikePost(Long postId, Long userId);
    
    /**
     * 检查用户是否已点赞帖子
     */
    boolean isLiked(Long postId, Long userId);
}
