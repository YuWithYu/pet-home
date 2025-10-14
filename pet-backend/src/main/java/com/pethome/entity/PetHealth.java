package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_health")
public class PetHealth {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String type;
    private String suitableFor;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private Integer status;
    private Integer sortOrder;
    private String ingredients;
    private String effects;
    private String usageInstructions;
    private String precautions;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


