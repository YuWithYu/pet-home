package com.pethome.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LitterServiceDTO {
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
}
