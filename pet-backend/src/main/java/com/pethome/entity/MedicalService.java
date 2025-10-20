package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("medical_services")
public class MedicalService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    @TableField("image")
    private String imageUrl;
    @TableField(exist = false)
    private Integer duration;
    private String status;
    private Integer sortOrder;
    @TableField("product_introduction")
    private String productIntroduction;
    @TableField("usage_instructions")
    private String usageInstructions;
    @TableField("created_by")
    private Integer createdBy;
    @TableField("updated_by")
    private Integer updatedBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
