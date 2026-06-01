package com.robiulsunyemon.profile_service.profile.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class KycRequestMessage implements Serializable {
    private Long userId;
    private String selfieUrl;
    private String nidFrontUrl;
}
