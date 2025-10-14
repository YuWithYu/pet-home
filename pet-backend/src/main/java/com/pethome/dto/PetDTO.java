package com.pethome.dto;

import lombok.Data;

@Data
public class PetDTO {
    private Long id;
    private Long userId;
    private String name;
    private String breed;
    private Integer age;
    private String gender;
    private String avatar;
    private String description;
}
