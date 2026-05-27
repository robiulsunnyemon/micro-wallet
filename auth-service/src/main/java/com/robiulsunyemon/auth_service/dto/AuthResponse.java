package com.robiulsunyemon.auth_service.dto;
import com.robiulsunyemon.auth_service.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String phoneNumber;
    private String email;
    private Role role;
}
