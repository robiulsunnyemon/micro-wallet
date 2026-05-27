package com.robiulsunyemon.auth_service.mapper;
import com.robiulsunyemon.auth_service.dto.AuthRequest;
import com.robiulsunyemon.auth_service.dto.AuthResponse;
import com.robiulsunyemon.auth_service.entity.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AuthMapper {
    private PasswordEncoder passwordEncoder;
    public UserEntity requestToUserEntity(AuthRequest request){
        return UserEntity.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
    }

    public AuthResponse entityToResponse(UserEntity entity){
        return AuthResponse.builder()
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .role(entity.getRole())
                .build();
    }
}
