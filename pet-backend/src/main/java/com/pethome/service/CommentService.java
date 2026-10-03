package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    
    /**
     * 添加评论
     */
    Comment addComment(Comment comment);
    
    /**
     * 获取帖子的评论列表
     */
    List<Comment> getCommentsByPostId(Long postId);
    
    /**
     * 获取帖子的评论列表（带用户点赞状态）
     */
    List<Comment> getCommentsByPostId(Long postId, Long userId);
    
    /**
     * 获取专题的评论列表（带用户点赞状态）
     */
    List<Comment> getCommentsByTopicId(Long topicId, Long userId);
    
    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Long userId);
    
    /**
     * 点赞评论
     */
    boolean likeComment(Long commentId, Long userId);
    
    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Long commentId, Long userId);
    
    /**
     * 获取管理员评论列表（支持搜索和筛选）
     */
    Page<Comment> getAdminCommentList(Page<Comment> page, String keyword, Integer status, Long postId);
    
    /**
     * 获取用户的所有评论
     */
    List<Comment> getUserComments(Long userId);
    
    /**
     * 获取评论的回复列表（带用户点赞状态）
     */
    List<Comment> getRepliesByCommentId(Long commentId, Long userId);
}
