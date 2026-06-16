package com.pethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Notification;
import com.pethome.entity.Post;
import com.pethome.entity.PostLike;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.service.NotificationService;
import com.pethome.service.PostLikeService;
import com.pethome.service.PostService;
import com.pethome.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostLikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements PostLikeService {
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    private PostService postService;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired
    @Lazy
    private TaskService taskService;
    
    @Override
    @Transactional
    public boolean likePost(Long postId, Long userId) {
        // 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("帖子ID和用户ID不能为空");
        }
        
        // 检查是否已点赞
        PostLike existingLike = postLikeMapper.checkLikeStatus(postId, userId);
        if (existingLike != null) {
            return false; // 已经点赞过了
        }
        
        // 添加点赞记录
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        postLikeMapper.insert(like);
        
        // 更新帖子点赞数
        postService.updateLikesCount(postId, 1);
        // 社区互动通知：通知帖子作者（不通知自己）
        if (notificationService != null) {
            Post post = postService.getById(postId);
            if (post != null && post.getUserId() != null && !post.getUserId().equals(userId)) {
                Notification n = new Notification();
                n.setUserId(post.getUserId());
                n.setTitle("收到新点赞");
                n.setContent("有人赞了您的帖子");
                n.setType("community_like");
                n.setStatus(0);
                n.setRelatedId(postId);
                n.setRelatedType("post");
                n.setCreateTime(LocalDateTime.now());
                n.setUpdateTime(LocalDateTime.now());
                notificationService.createNotification(n);
            }
        }
        // 更新任务进度：点赞内容任务
        try {
            // 获取用户今日已点赞的帖子数量
            int todayLikeCount = postLikeMapper.getTodayLikeCount(userId);
            taskService.updateTaskProgress(userId, "like_content", todayLikeCount);
            System.out.println("更新点赞内容任务进度，用户ID: " + userId + ", 今日点赞数: " + todayLikeCount);
        } catch (Exception e) {
            // 任务进度更新失败不影响点赞流程
            System.err.println("更新点赞内容任务进度失败: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean unlikePost(Long postId, Long userId) {
        // 参数验证
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("帖子ID和用户ID不能为空");
        }
        
        // 查找点赞记录
        PostLike like = postLikeMapper.checkLikeStatus(postId, userId);
        if (like == null) {
            return false; // 没有点赞记录
        }
        
        // 删除点赞记录
        postLikeMapper.deleteById(like.getId());
        
        // 更新帖子点赞数
        postService.updateLikesCount(postId, -1);
        
        return true;
    }
    
    @Override
    public boolean isLiked(Long postId, Long userId) {
        PostLike like = postLikeMapper.checkLikeStatus(postId, userId);
        return like != null;
    }
}
