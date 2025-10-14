package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_checkups")
public class PetCheckup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private LocalDate checkupDate;
    private String checkupType;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String recommendations;
    private BigDecimal price;
    private String status;
    private String location;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


