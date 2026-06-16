package com.pethome.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

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
@TableName(value = "grooming_services", autoResultMap = true)
public class GroomingService implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField(value = "introduction", typeHandler = JacksonTypeHandler.class)
    private List<String> introduction;

    @TableField(value = "instructions", typeHandler = JacksonTypeHandler.class)
    private List<String> instructions;

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

    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("is_deleted")
    private Boolean isDeleted;
    
    // 手动添加setter方法，确保编译通过
    public String getName() { return name; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setIsRecommended(Boolean isRecommended) { this.isRecommended = isRecommended; }
    public void setStatus(String status) { this.status = status; }
    public void setIntroduction(List<String> introduction) { this.introduction = introduction; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
