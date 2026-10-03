package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "hospital_services", autoResultMap = true)
@Schema(name = "HospitalService对象", description = "宠物医院服务")
public class HospitalService {

    @Schema(description = "服务ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务简介")
    private String description;

    @Schema(description = "服务特色介绍")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> introduction;

    @Schema(description = "使用须知")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> instructions;

    @Schema(description = "服务价格")
    private BigDecimal price;

    @Schema(description = "服务时长（分钟/小时）")
    private Integer duration;

    @Schema(description = "服务分类")
    private String category;

    @Schema(description = "封面图片")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "背景颜色")
    @TableField("bg_color")
    private String bgColor;

    @Schema(description = "标签")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @Schema(description = "状态：active/inactive")
    private String status;

    @Schema(description = "是否推荐")
    @TableField("is_recommended")
    private Boolean isRecommended;

    @Schema(description = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @Schema(description = "是否删除")
    @TableField("is_deleted")
    private Boolean isDeleted;

    // 兼容旧字段，便于数据迁移
    private String productIntroduction;
    private String usageInstructions;
    private String precautions;
    private String targetAudience;
    private String serviceDuration;
    private String bookingRequirements;
    private String serviceFeatures;

    /** 显式访问器：避免部分 IDE 未启用 Lombok 时无法解析 {@code getName()} */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
