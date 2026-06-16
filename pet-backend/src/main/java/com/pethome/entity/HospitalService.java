package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "hospital_services", autoResultMap = true)
@ApiModel(value = "HospitalService对象", description = "宠物医院服务")
public class HospitalService {

    @ApiModelProperty(value = "服务ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String name;

    @ApiModelProperty(value = "服务简介")
    private String description;

    @ApiModelProperty(value = "服务特色介绍")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> introduction;

    @ApiModelProperty(value = "使用须知")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> instructions;

    @ApiModelProperty(value = "服务价格")
    private BigDecimal price;

    @ApiModelProperty(value = "服务时长（分钟/小时）")
    private Integer duration;

    @ApiModelProperty(value = "服务分类")
    private String category;

    @ApiModelProperty(value = "封面图片")
    @TableField("image_url")
    private String imageUrl;

    @ApiModelProperty(value = "背景颜色")
    @TableField("bg_color")
    private String bgColor;

    @ApiModelProperty(value = "标签")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    @ApiModelProperty(value = "状态：active/inactive")
    private String status;

    @ApiModelProperty(value = "是否推荐")
    @TableField("is_recommended")
    private Boolean isRecommended;

    @ApiModelProperty(value = "排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "是否删除")
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
