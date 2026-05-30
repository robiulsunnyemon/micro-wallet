package com.robiulsunyemon.profile_service.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationStatusMessage implements Serializable {
    private Boolean isSucceed;
    private Long userId;
}
