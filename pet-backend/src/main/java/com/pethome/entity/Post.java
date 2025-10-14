package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Integer userId;
    private String title;
    private String content;
    private String category;
    private String images;
    @TableField("likes_count")
    private Integer likesCount;
    @TableField("comments_count")
    private Integer commentsCount;
    private Integer status;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
