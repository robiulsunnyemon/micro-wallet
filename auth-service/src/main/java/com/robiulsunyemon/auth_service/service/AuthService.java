package com.robiulsunyemon.auth_service.service;

import com.robiulsunyemon.auth_service.dto.*;

public interface AuthService {
    AuthResponse createUser(AuthRequest request);
    LoginResponse login(LoginRequest request);
    String verifyOtp(OtpVerifyRequest request);
    void handleRegistrationStatusUpdate(RegistrationStatusMessage statusMessage);
    String resendOtp(EmailRequest email);
    String forgotPassword(EmailRequest email);
    ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request);
    String resetPassword(ResetPasswordRequest request);
    AuthResponse findById(Long id);
}
