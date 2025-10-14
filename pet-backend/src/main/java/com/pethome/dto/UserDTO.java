package com.pethome.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private Integer status;
}
