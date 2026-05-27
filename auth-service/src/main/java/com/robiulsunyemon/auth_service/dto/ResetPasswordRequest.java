package com.robiulsunyemon.auth_service.dto;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String resetToken;
    private String newPassword;
}
