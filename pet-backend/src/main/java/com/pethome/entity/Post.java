package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    private String title;
    private String content;
    private String category;
    private String tags; // 标签列表，用逗号分隔（如：宠物,猫咪,日常）
    private String images;
    
    @TableField("likes_count")
    private Integer likesCount = 0;
    
    @TableField("comments_count")
    private Integer commentsCount = 0;
    
    @TableField("views_count")
    private Integer viewsCount = 0;
    
    @TableField("shares_count")
    private Integer sharesCount = 0;
    
    @TableField("image_count")
    private Integer imageCount = 0;
    
    @TableField("cover_image")
    private String coverImage;
    
    @TableField(value = "thumbnail_image", exist = false, select = false)
    private String thumbnailImage; // 缩略图URL（数据库中没有此字段，仅用于业务逻辑）
    
    @TableField(value = "image_urls", exist = false, select = false)
    private String imageUrls; // 完整图片URL列表（JSON格式，包含原始图和缩略图）（数据库中没有此字段，仅用于业务逻辑）
    
    @TableField("videos")
    private String videos; // 视频列表（JSON格式，包含视频URL和封面）
    
    private Integer status = 1; // 0:已删除 1:正常 2:待审核
    
    @TableField("is_top")
    private Boolean isTop = false; // 是否置顶
    
    @TableField("is_hot")
    private Boolean isHot = false; // 是否热门
    
    @TableField("exposure_score")
    private Double exposureScore = 0.0; // 曝光分数，用于热度算法
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 关联用户信息（不存储到数据库）
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String userAvatar;
    
    @TableField(exist = false)
    @JsonProperty("isLiked")
    private Boolean isLiked = false; // 当前用户是否已点赞（序列化固定为 isLiked，避免部分 Jackson 配置成 liked）
    
    @TableField(exist = false)
    private Double recommendationScore = 0.0; // 推荐分数（用于个性化排序）
    
    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    
    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }
    
    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }
    
    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }
    
    public Integer getSharesCount() { return sharesCount; }
    public void setSharesCount(Integer sharesCount) { this.sharesCount = sharesCount; }
    
    public Integer getImageCount() { return imageCount; }
    public void setImageCount(Integer imageCount) { this.imageCount = imageCount; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public String getThumbnailImage() { return thumbnailImage; }
    public void setThumbnailImage(String thumbnailImage) { this.thumbnailImage = thumbnailImage; }
    
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Boolean getIsTop() { return isTop; }
    public void setIsTop(Boolean isTop) { this.isTop = isTop; }
    
    public Boolean getIsHot() { return isHot; }
    public void setIsHot(Boolean isHot) { this.isHot = isHot; }
    
    public Double getExposureScore() { return exposureScore; }
    public void setExposureScore(Double exposureScore) { this.exposureScore = exposureScore; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    
    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }
    
    public Double getRecommendationScore() { return recommendationScore; }
    public void setRecommendationScore(Double recommendationScore) { this.recommendationScore = recommendationScore; }
    
    public String getVideos() { return videos; }
    public void setVideos(String videos) { this.videos = videos; }
}
