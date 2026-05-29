package com.robiulsunyemon.profile_service.profile.dto;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@Data
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String address;
    private String nidNumber;
    private KycStatus kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
