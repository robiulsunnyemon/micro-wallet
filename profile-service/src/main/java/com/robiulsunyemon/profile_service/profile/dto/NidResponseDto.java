package com.robiulsunyemon.profile_service.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NidResponseDto {
    private String nameEn;
    private String nameBn;
    private String nidNumber;
    private String dateOfBirth;
    private String address;
}
