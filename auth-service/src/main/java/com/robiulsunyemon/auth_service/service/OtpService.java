package com.robiulsunyemon.auth_service.service;

public interface OtpService {
    void sendAndSaveOtp(String email);
    boolean verifyOtp(String email, String inputOtp);
    String sendForgetPasswordToken(String email);
    boolean verifyForgetPasswordToken(String email,String inputToken);
}
