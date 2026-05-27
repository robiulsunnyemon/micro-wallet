package com.robiulsunyemon.auth_service.dto;
import lombok.Data;

@Data
public class LoginRequest {
    private String phoneNumber;
    private String password;
}
