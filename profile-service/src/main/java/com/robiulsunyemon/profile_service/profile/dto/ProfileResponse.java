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
    private Long walletId;
    private String nameEn;
    private String nameBn;
    private String nidFrontSide;
    private String nidBackSide;
    private LocalDateTime dateOfBirth;
    private String address;
    private String nidNumber;
    private KycStatus kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
