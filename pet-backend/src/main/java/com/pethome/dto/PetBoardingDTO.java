package com.pethome.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PetBoardingDTO {
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
}
