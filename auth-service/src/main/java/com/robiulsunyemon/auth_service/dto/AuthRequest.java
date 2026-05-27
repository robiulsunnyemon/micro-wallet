package com.robiulsunyemon.auth_service.dto;
import com.robiulsunyemon.auth_service.entity.Role;
import lombok.Data;

@Data
public class AuthRequest {
    private String phoneNumber;
    private String email;
    private String password;
    private Role role;
}
