package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("pet_vaccines")
public class PetVaccine {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private String vaccineName;
    private String vaccineType;
    private LocalDate vaccinationDate;
    private LocalDate nextVaccinationDate;
    private String manufacturer;
    private String batchNumber;
    private BigDecimal price;
    private String status;
    private String doctorName;
    private String location;
    private String notes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


