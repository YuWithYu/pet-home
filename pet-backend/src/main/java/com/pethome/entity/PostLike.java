package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("post_like")
public class PostLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("post_id")
    private Long postId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
