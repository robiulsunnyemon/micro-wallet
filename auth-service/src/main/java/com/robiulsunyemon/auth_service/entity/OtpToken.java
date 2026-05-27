package com.robiulsunyemon.auth_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "EmailOTP", timeToLive = 300)
@Data
@AllArgsConstructor

public class OtpToken implements Serializable {
    @Id
    private String email;
    private String otpCode;

}