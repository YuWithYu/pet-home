package com.pethome.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String name;
    private String title;
    private String department;
    private String specialty;
    private String avatar;
    private String description;
    private String phone;
    private String email;
    private Integer status;
    private Integer sortOrder;
}
