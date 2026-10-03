package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("follower_id")
    private Long followerId; // 关注者ID
    
    @TableField("following_id")
    private Long followingId; // 被关注者ID
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // 关联用户信息（不存储到数据库）
    @TableField(exist = false)
    private String followerName;
    
    @TableField(exist = false)
    private String followerAvatar;
    
    @TableField(exist = false)
    private String followingName;
    
    @TableField(exist = false)
    private String followingAvatar;
    
    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    
    public Long getFollowingId() { return followingId; }
    public void setFollowingId(Long followingId) { this.followingId = followingId; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public String getFollowerName() { return followerName; }
    public void setFollowerName(String followerName) { this.followerName = followerName; }
    
    public String getFollowerAvatar() { return followerAvatar; }
    public void setFollowerAvatar(String followerAvatar) { this.followerAvatar = followerAvatar; }
    
    public String getFollowingName() { return followingName; }
    public void setFollowingName(String followingName) { this.followingName = followingName; }
    
    public String getFollowingAvatar() { return followingAvatar; }
    public void setFollowingAvatar(String followingAvatar) { this.followingAvatar = followingAvatar; }
}
