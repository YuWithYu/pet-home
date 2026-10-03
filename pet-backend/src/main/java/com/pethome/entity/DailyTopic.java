package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日专题实体类
 */
@TableName("daily_topic")
public class DailyTopic {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String description;
    
    /** 正文富内容 JSON：[{t:"text",v:"段落"},{t:"img",v:"url"}] 支持文字+图片混排 */
    private String content;
    
    @TableField("cover_image")
    private String coverImage;
    
    private String theme;
    
    @TableField("publish_date")
    private LocalDate publishDate;
    
    private Integer status; // 0-草稿，1-已发布，2-已下线
    
    @TableField("sort_order")
    private Integer sortOrder = 0;
    
    @TableField("view_count")
    private Integer viewCount = 0;
    
    @TableField("like_count")
    private Integer likeCount = 0;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 关联的帖子列表（不存储到数据库）
    @TableField(exist = false)
    private java.util.List<Post> posts;

    /** 当前用户是否已点赞（接口返回用，不存库） */
    @TableField(exist = false)
    private Boolean isLiked;

    /** 当前用户是否已收藏（接口返回用，不存库） */
    @TableField(exist = false)
    private Boolean isCollected;

    /** 收藏数（接口返回用，不存库） */
    @TableField(exist = false)
    private Integer collectCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public java.util.List<Post> getPosts() {
        return posts;
    }

    public void setPosts(java.util.List<Post> posts) {
        this.posts = posts;
    }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    public Boolean getIsCollected() { return isCollected; }
    public void setIsCollected(Boolean isCollected) { this.isCollected = isCollected; }
    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }
}



