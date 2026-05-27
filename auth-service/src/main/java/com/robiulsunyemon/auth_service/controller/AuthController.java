package com.robiulsunyemon.auth_service.controller;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<AuthResponse>> createUser(
            @RequestBody AuthRequest authRequest,
            HttpServletRequest req) {

        AuthResponse authResponse = authService.createUser(authRequest);
        return buildSuccessResponse(
                authResponse,
                HttpStatus.CREATED,
                "User registered successfully. Please verify OTP.",
                req.getRequestURI()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<LoginResponse>> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest req) {

        LoginResponse loginResponse = authService.login(loginRequest);
        return buildSuccessResponse(
                loginResponse,
                HttpStatus.OK,
                "Login successful",
                req.getRequestURI()
        );
    }

    @PostMapping("/verify-signup")
    public ResponseEntity<GlobalResponse<String>> verifyOtp(
            @RequestBody OtpVerifyRequest otpVerifyRequest,
            HttpServletRequest req) {

        String result = authService.verifyOtp(otpVerifyRequest);
        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "OTP verified successfully",
                req.getRequestURI()
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<GlobalResponse<String>> resendOtp(
            @RequestBody EmailRequest emailRequest,
            HttpServletRequest req) {

        String result = authService.resendOtp(emailRequest);
        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "OTP resent successfully",
                req.getRequestURI()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GlobalResponse<String>> forgotPassword(
            @RequestBody EmailRequest emailRequest,
            HttpServletRequest req) {

        String result = authService.forgotPassword(emailRequest);
        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "Password reset OTP sent",
                req.getRequestURI()
        );
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<GlobalResponse<ForgetPasswordOtpVerifyResponse>> verifyForgotPasswordOtp(
            @RequestBody OtpVerifyRequest otpVerifyRequest,
            HttpServletRequest req) {

        ForgetPasswordOtpVerifyResponse result = authService.verifyForgotPasswordOtp(otpVerifyRequest);
        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "OTP verified successfully",
                req.getRequestURI()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GlobalResponse<String>> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest,
            HttpServletRequest req) {

        String result = authService.resetPassword(resetPasswordRequest);
        return buildSuccessResponse(
                result,
                HttpStatus.OK,
                "Password reset successful",
                req.getRequestURI()
        );
    }


    private <T> ResponseEntity<GlobalResponse<T>> buildSuccessResponse(T data, HttpStatus status, String message, String path) {
        GlobalResponse<T> response = GlobalResponse.<T>builder()
                .statusCode(status.value())
                .success(true)
                .message(message)
                .path(path)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}