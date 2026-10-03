package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上门清理服务 Banner
 * Lombok @Data 已生成 getter/setter/toString/equals/hashCode，无需手动添加
 */
@Data
@TableName("litter_service_banners")
public class LitterServiceBanner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    @TableField("image_url")
    private String imageUrl;
    private String position;
    private String status;
    @TableField("sort_order")
    private Integer sortOrder;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("is_deleted")
    private Boolean isDeleted;
}
