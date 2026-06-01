package com.robiulsunyemon.profile_service.profile.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycResultMessage {

    private Long    userId;
    private boolean verified;
    private double  distance;
    private double  threshold;
}