package com.pethome.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("grooming_services")
public class GroomingService implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("category")
    private String category;

    @TableField("price")
    private BigDecimal price;

    @TableField("duration")
    private Integer duration;

    @TableField("image_url")
    private String imageUrl;

    @TableField("bg_color")
    private String bgColor;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("is_recommended")
    private Boolean isRecommended;

    @TableField("status")
    private String status;

    @TableField("tags")
    private String tags;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    private Boolean isDeleted;
}
