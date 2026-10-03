package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Comment;
import com.pethome.entity.CommentLike;
import com.pethome.entity.Notification;
import com.pethome.entity.Post;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.CommentLikeMapper;
import com.pethome.service.CommentService;
import com.pethome.service.NotificationService;
import com.pethome.service.PostService;
import com.pethome.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private PostService postService;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    @Lazy
    private TaskService taskService;

    @Override
    public Comment addComment(Comment comment) {
        this.save(comment);
        
        // 更新帖子的评论数和曝光分数（使用postId字段）
        if (comment.getPostId() != null) {
            Long postId = comment.getPostId();
            postService.updateCommentsCount(postId, 1);
            postService.updateExposureScoreOnInteraction(postId, "comment");
        }
        
        // 更新任务进度：评论内容任务（只统计顶级评论，不统计回复）
        if (comment.getUserId() != null && (comment.getParentId() == null || comment.getParentId() == 0)) {
            try {
                int todayCommentCount = this.getTodayCommentCount(comment.getUserId());
                taskService.updateTaskProgress(comment.getUserId(), "comment_content", todayCommentCount);
                log.debug("更新评论内容任务进度，用户ID: {}, 今日评论数: {}", comment.getUserId(), todayCommentCount);
            } catch (Exception e) {
                log.error("更新评论内容任务进度失败", e);
            }
        }
        // 社区互动通知：通知帖子作者或回复对象（不通知自己）
        if (notificationService != null && comment.getPostId() != null && comment.getUserId() != null) {
            Post post = postService.getById(comment.getPostId());
            if (post != null && post.getUserId() != null && !post.getUserId().equals(comment.getUserId())) {
                Notification n = new Notification();
                n.setUserId(post.getUserId());
                n.setTitle("收到新评论");
                n.setContent("有人评论了您的帖子");
                n.setType("community_comment");
                n.setStatus(0);
                n.setRelatedId(comment.getPostId());
                n.setRelatedType("post");
                n.setCreateTime(LocalDateTime.now());
                n.setUpdateTime(LocalDateTime.now());
                notificationService.createNotification(n);
            }
            // 若是回复，再通知被回复的评论作者
            if (comment.getParentId() != null && comment.getParentId() != 0) {
                Comment parent = this.getById(comment.getParentId());
                if (parent != null && parent.getUserId() != null && !parent.getUserId().equals(comment.getUserId())) {
                    Notification n2 = new Notification();
                    n2.setUserId(parent.getUserId());
                    n2.setTitle("收到回复");
                    n2.setContent("有人回复了您的评论");
                    n2.setType("community_comment");
                    n2.setStatus(0);
                    n2.setRelatedId(comment.getPostId());
                    n2.setRelatedType("post");
                    n2.setCreateTime(LocalDateTime.now());
                    n2.setUpdateTime(LocalDateTime.now());
                    notificationService.createNotification(n2);
                }
            }
        }
        return comment;
    }
    
    /**
     * 获取用户今日已发布的顶级评论数量
     */
    private int getTodayCommentCount(Long userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", 0)); // 只统计顶级评论
        wrapper.apply("DATE(create_time) = CURDATE()");
        return (int) this.count(wrapper);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        return this.baseMapper.getCommentsByPostId(postId);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId, Long userId) {
        List<Comment> comments = this.baseMapper.getCommentsByPostId(postId);
        // 回复数已由 SQL 中的递归 CTE（reply_counts）计算并映射到 replyCount 字段，
        // 无需 Java 侧逐条递归查询重算（原实现为 N+1 递归树查询）
        if (comments != null && userId != null && !comments.isEmpty()) {
            fillLikeStatus(comments, userId);
        }
        return comments;
    }

    @Override
    public List<Comment> getCommentsByTopicId(Long topicId, Long userId) {
        List<Comment> comments = this.baseMapper.getCommentsByTopicId(topicId);
        if (comments != null && userId != null && !comments.isEmpty()) {
            fillLikeStatus(comments, userId);
        }
        return comments;
    }

    /**
     * 批量填充点赞状态：单次 IN 查询替代逐条 checkLikeStatus，消除 N+1
     */
    private void fillLikeStatus(List<Comment> comments, Long userId) {
        List<Long> commentIds = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getId() != null) {
                commentIds.add(comment.getId());
            }
        }
        if (commentIds.isEmpty()) {
            return;
        }
        Set<Long> likedIds = commentLikeMapper.selectList(
                        new QueryWrapper<CommentLike>().eq("user_id", userId).in("comment_id", commentIds))
                .stream()
                .map(CommentLike::getCommentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        for (Comment comment : comments) {
            comment.setIsLiked(likedIds.contains(comment.getId()));
        }
    }

    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        // 获取评论信息，以便更新帖子统计
        Comment comment = this.getById(commentId);
        if (comment == null) {
            return false;
        }
        
        // 删除评论
        boolean success = this.removeById(commentId);
        
        // 更新帖子的评论数（减1）
        if (success && comment.getPostId() != null) {
            Long postId = comment.getPostId();
            postService.updateCommentsCount(postId, -1);
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        // 参数验证
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }
        
        // 检查是否已点赞
        CommentLike existingLike = commentLikeMapper.checkLikeStatus(commentId, userId);
        if (existingLike != null) {
            return false; // 已经点赞过了
        }
        
        // 添加点赞记录
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        commentLikeMapper.insert(like);
        
        // 更新评论点赞数
        Comment comment = this.getById(commentId);
        if (comment != null) {
            comment.setLikesCount((comment.getLikesCount() == null ? 0 : comment.getLikesCount()) + 1);
            this.updateById(comment);
        }
        
        // 更新任务进度：点赞评论任务
        try {
            // 获取用户今日已点赞的评论数量
            int todayCommentLikeCount = commentLikeMapper.getTodayLikeCount(userId);
            taskService.updateTaskProgress(userId, "like_comment", todayCommentLikeCount);
            log.debug("更新点赞评论任务进度，用户ID: {}, 今日点赞评论数: {}", userId, todayCommentLikeCount);
        } catch (Exception e) {
            // 任务进度更新失败不影响点赞流程
            log.error("更新点赞评论任务进度失败", e);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId, Long userId) {
        // 参数验证
        if (commentId == null || userId == null) {
            throw new IllegalArgumentException("评论ID和用户ID不能为空");
        }
        
        // 查找点赞记录
        CommentLike like = commentLikeMapper.checkLikeStatus(commentId, userId);
        if (like == null) {
            return false; // 没有点赞记录
        }
        
        // 删除点赞记录
        commentLikeMapper.deleteById(like.getId());
        
        // 更新评论点赞数
        Comment comment = this.getById(commentId);
        if (comment != null) {
            int currentLikes = comment.getLikesCount() == null ? 0 : comment.getLikesCount();
            comment.setLikesCount(Math.max(0, currentLikes - 1));
            this.updateById(comment);
        }
        
        return true;
    }

    @Override
    public Page<Comment> getAdminCommentList(Page<Comment> page, String keyword, Integer status, Long postId) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("content", keyword);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        if (postId != null) {
            qw.eq("post_id", postId);
        }
        qw.orderByDesc("create_time");
        return this.page(page, qw);
    }

    @Override
    public List<Comment> getUserComments(Long userId) {
        return this.list(new QueryWrapper<Comment>().eq("user_id", userId));
    }

    @Override
    public List<Comment> getRepliesByCommentId(Long commentId, Long userId) {
        // 递归加载所有嵌套的回复
        List<Comment> allReplies = new ArrayList<>();
        loadRepliesRecursively(commentId, allReplies);

        // 批量填充点赞状态：单次 IN 查询替代递归内逐条 checkLikeStatus，消除 N+1
        if (userId != null && !allReplies.isEmpty()) {
            fillLikeStatus(allReplies, userId);
        }

        return allReplies;
    }

    /**
     * 递归加载所有嵌套的回复
     */
    private void loadRepliesRecursively(Long parentId, List<Comment> allReplies) {
        // 获取直接回复
        List<Comment> directReplies = this.baseMapper.getRepliesByCommentId(parentId);

        if (directReplies != null && !directReplies.isEmpty()) {
            for (Comment reply : directReplies) {
                // 添加到结果列表
                allReplies.add(reply);

                // 递归加载该回复的回复
                loadRepliesRecursively(reply.getId(), allReplies);
            }
        }
    }
}


