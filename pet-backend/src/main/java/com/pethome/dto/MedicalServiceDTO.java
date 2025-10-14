package com.pethome.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MedicalServiceDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String imageUrl;
    private Integer duration;
    private String status;
    private Integer sortOrder;
}
