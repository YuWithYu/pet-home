package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_boarding")
public class PetBoarding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private Integer petId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String serviceType;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private BigDecimal price;
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
