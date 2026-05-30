package com.robiulsunyemon.profile_service.profile.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class WalletCreatedMessage implements Serializable {
    private Long userId;
    private Long walletId;
    private String email;
    private String phoneNumber;
}
