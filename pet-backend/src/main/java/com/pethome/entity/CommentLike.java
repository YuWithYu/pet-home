package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("comment_like")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("comment_id")
    private Long commentId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
