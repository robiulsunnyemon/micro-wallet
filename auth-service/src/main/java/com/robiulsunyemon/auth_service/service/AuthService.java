package com.robiulsunyemon.auth_service.service;

import com.robiulsunyemon.auth_service.dto.*;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthResponse createUser(AuthRequest request);
    LoginResponse login(LoginRequest request);
    String verifyOtp(OtpVerifyRequest request);
    String resendOtp(EmailRequest email);
    String forgotPassword(EmailRequest email);
    ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request);
    String resetPassword(ResetPasswordRequest request);
}
