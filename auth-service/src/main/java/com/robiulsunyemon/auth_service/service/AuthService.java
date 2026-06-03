package com.robiulsunyemon.auth_service.service;

import com.robiulsunyemon.auth_service.dto.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse createUser(AuthRequest request, HttpServletRequest httpServletRequest);
    LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest);
    String verifyOtp(OtpVerifyRequest request, HttpServletRequest httpServletRequest);
    void handleRegistrationStatusUpdate(RegistrationStatusMessage statusMessage);
    String resendOtp(EmailRequest email, HttpServletRequest httpServletRequest);
    String forgotPassword(EmailRequest email, HttpServletRequest httpServletRequest);
    ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request, HttpServletRequest httpServletRequest);
    String resetPassword(ResetPasswordRequest request, HttpServletRequest httpServletRequest);
    AuthResponse findById(Long id);
}
