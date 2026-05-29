package com.robiulsunyemon.auth_service.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class UserCreatedMessage implements Serializable {
    private Long userId;
    private String email;
    private String phoneNumber;
}
