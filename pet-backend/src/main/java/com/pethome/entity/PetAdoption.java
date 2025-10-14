package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet_adoption")
public class PetAdoption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String petName;
    private String breed;
    private Integer age;
    private String gender;
    private String description;
    private String imageUrl;
    private BigDecimal adoptionFee;
    private String status;
    private String location;
    private String contactInfo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
