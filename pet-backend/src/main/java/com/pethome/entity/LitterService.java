package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("litter_services")
public class LitterService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer userId;
    private String serviceType;
    private String frequency;
    private String address;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private BigDecimal price;
    private LocalDateTime nextServiceDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
