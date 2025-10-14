package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_toys")
public class PetToy {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String material;
    private String suitableFor;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Integer status;
    private Integer sortOrder;
    private String features;
    private String safetyInfo;
    private String usageInstructions;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
