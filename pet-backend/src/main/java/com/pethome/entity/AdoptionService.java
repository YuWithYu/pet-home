package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("adoption_services")
@ApiModel(value = "AdoptionService对象", description = "宠物领养服务")
public class AdoptionService {

    @ApiModelProperty(value = "服务ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "服务名称")
    private String name;

    @ApiModelProperty(value = "服务描述")
    private String description;

    @ApiModelProperty(value = "服务分类")
    private String category;

    @ApiModelProperty(value = "服务价格")
    private BigDecimal price;

    @ApiModelProperty(value = "服务时长(分钟)")
    private Integer duration;

    @ApiModelProperty(value = "服务图片URL")
    private String imageUrl;

    @ApiModelProperty(value = "背景颜色")
    private String bgColor;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "是否推荐")
    private Boolean isRecommended;

    @ApiModelProperty(value = "服务标签")
    private String tags;

    @ApiModelProperty(value = "服务状态：active-启用，inactive-禁用")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
