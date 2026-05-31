package com.robiulsunyemon.profile_service.profile.dto;
import com.robiulsunyemon.profile_service.profile.entity.KycStatus;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ProfileRequest {
    private Long userId;
    private String nameEn;
    private String nameBn;
    private String address;
    private String nidNumber;
    private KycStatus kycStatus;
}
