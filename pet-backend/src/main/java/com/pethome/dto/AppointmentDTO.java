package com.pethome.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AppointmentDTO {
    private Long id;
    private Integer userId;
    private Integer petId;
    private String serviceType;
    private String date;
    private String timeSlot;
    private String status;
    private String remark;
    private String contactName;
    private String contactPhone;
    private BigDecimal price;
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
