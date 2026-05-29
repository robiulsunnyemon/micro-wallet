package com.robiulsunyemon.wallet_service.wallet.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.lang.Long;


@AllArgsConstructor
@Data
public class UserCreatedMessage implements Serializable {
    private Long userId;
    private String email;
    private String phoneNumber;
}
