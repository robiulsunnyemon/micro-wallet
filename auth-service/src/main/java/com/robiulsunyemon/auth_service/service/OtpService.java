package com.robiulsunyemon.auth_service.service;

import org.springframework.stereotype.Service;

@Service
public interface OtpService {
    void sendAndSaveOtp(String email);
    boolean verifyOtp(String email, String inputOtp);
    String sendForgetPasswordToken(String email);
    boolean verifyForgetPasswordToken(String email,String inputToken);
}
