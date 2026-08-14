package com.cityhub.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String username;
    private String phone;
    private String code;
    private String password;
}
