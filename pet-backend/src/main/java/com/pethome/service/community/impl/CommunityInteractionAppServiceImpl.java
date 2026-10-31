package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Comment;
import com.pethome.entity.Post;
import com.pethome.service.community.CommentService;
import com.pethome.service.community.CommunityInteractionAppService;
import com.pethome.service.community.PostCollectService;
import com.pethome.service.community.PostLikeService;
import com.pethome.service.community.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 社区互动应用服务实现。
 * <p>
 * 原 CommunityController 的点赞/收藏/评论编排与归属校验下沉至此，安全修复行为（身份以登录态为准）保持不变。
 */
@Service
public class CommunityInteractionAppServiceImpl implements CommunityInteractionAppService {

    @Autowired
    private PostService postService;

    @Autowired
    private PostLikeService postLikeService;

    @Autowired
    private PostCollectService postCollectService;

    @Autowired
    private CommentService commentService;

    @Override
    public boolean likePost(Long postId, Long userId) {
        // 安全修复：点赞人以登录态为准，防止替他人点赞
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        boolean success = postLikeService.likePost(postId, userId);
        if (success) {
            // 更新曝光分数
            postService.updateExposureScoreOnInteraction(postId, "like");
        }
        return success;
    }

    @Override
    public boolean unlikePost(Long postId, Long userId) {
        // 安全修复：操作人以登录态为准
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        return postLikeService.unlikePost(postId, userId);
    }

    @Override
    public boolean collectPost(Long postId, Long userId) {
        // 安全修复：操作人以登录态为准
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        if (!postCollectService.collectPost(postId, userId)) {
            throw new BusinessException("已经收藏过了");
        }
        return true;
    }

    @Override
    public boolean uncollectPost(Long postId, Long userId) {
        // 安全修复：操作人以登录态为准
        if (postId == null) {
            throw new BusinessException("帖子ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        return postCollectService.uncollectPost(postId, userId);
    }

    @Override
    public boolean isCollected(Long postId, Long userId) {
        // 安全修复：收藏状态属个人数据，以登录态为准（query userId 仅兼容旧前端）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        return postCollectService.isCollected(postId, userId);
    }

    @Override
    public Map<String, Object> getCollectedPosts(Long userId, Integer page, Integer size, String type) {
        // 安全修复：收藏列表属个人数据，以登录态为准（query userId 仅兼容旧前端），防止越权查看他人收藏
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Page<Post> postPage = new Page<>(page, size);
        Page<Post> result = postCollectService.getCollectedPosts(userId, postPage, type);

        // 填充用户信息和点赞状态
        ((PostServiceImpl) postService).fillUserInfoAndLikeStatus(result.getRecords(), userId);

        Map<String, Object> response = new HashMap<>();
        response.put("posts", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }

    @Override
    public boolean likeComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new BusinessException(401, "评论ID不能为空且需登录后操作");
        }
        return commentService.likeComment(commentId, userId);
    }

    @Override
    public boolean unlikeComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new BusinessException(401, "评论ID不能为空且需登录后操作");
        }
        return commentService.unlikeComment(commentId, userId);
    }

    @Override
    public Comment addComment(Long postId, Map<String, Object> data, Long userId) {
        // 安全修复：评论人以登录态为准，忽略请求体中的 userId（防止替他人发评论）
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent((String) data.get("content"));
        // 支持回复功能，如果传入了parentId则作为回复，否则为顶级评论
        Object parentIdObj = data.get("parentId");
        if (parentIdObj != null) {
            comment.setParentId(((Number) parentIdObj).longValue());
        } else {
            comment.setParentId(0L); // 默认0表示顶级评论
        }
        comment.setLikesCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        // 设置位置信息
        Object locationObj = data.get("location");
        if (locationObj != null) {
            comment.setLocation((String) locationObj);
        }
        // CommentService 已同步更新帖子评论数，无需重复调用
        return commentService.addComment(comment);
    }

    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        // 安全修复：userId 从登录态解析，防止伪造 userId 删除他人评论
        if (userId == null) {
            throw new BusinessException(401, "用户未登录或token无效");
        }
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        Post post = postService.getById(comment.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        boolean isCommentAuthor = comment.getUserId() != null && comment.getUserId().equals(userId);
        boolean isPostAuthor = post.getUserId() != null && post.getUserId().equals(userId);
        if (!isCommentAuthor && !isPostAuthor) {
            throw new BusinessException("无权限删除此评论");
        }
        return commentService.deleteComment(commentId, userId);
    }
}
