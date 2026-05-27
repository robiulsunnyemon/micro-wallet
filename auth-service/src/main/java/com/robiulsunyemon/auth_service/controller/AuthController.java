package com.robiulsunyemon.auth_service.controller;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    ResponseEntity<GlobalResponse<AuthResponse>> createUser(@RequestBody AuthRequest authRequest,HttpServletRequest req){
        AuthResponse authResponse=authService.createUser(authRequest);
        GlobalResponse<AuthResponse> response=GlobalResponse.<AuthResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("success")
                .path(req.getRequestURI())
                .data(authResponse)
                .build();
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @PostMapping("/login")
    ResponseEntity<GlobalResponse<LoginResponse>> loginResponse(@RequestBody LoginRequest loginRequest,HttpServletRequest req){
        GlobalResponse<LoginResponse> response=GlobalResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.login(loginRequest))
                .path(req.getRequestURI())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-signup")
    ResponseEntity<GlobalResponse<String>> verifyOtp(@RequestBody OtpVerifyRequest otpVerifyRequest,HttpServletRequest req){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .path(req.getRequestURI())
                .data(authService.verifyOtp(otpVerifyRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<GlobalResponse<Object>> resendOtp(@RequestBody EmailRequest emailRequest, HttpServletRequest servletRequest) {
        String resultMessage = authService.resendOtp(emailRequest);

        Map<String, String> dataBody = new HashMap<>();
        dataBody.put("message", resultMessage);

        GlobalResponse<Object> response = GlobalResponse.<Object>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .path(servletRequest.getRequestURI())
                .data(dataBody)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    ResponseEntity<GlobalResponse<String>> forgotPassword(@RequestBody EmailRequest emailRequest,HttpServletRequest req){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .path(req.getRequestURI())
                .data(authService.forgotPassword(emailRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/verify")
    ResponseEntity<GlobalResponse<ForgetPasswordOtpVerifyResponse>> verifyForgotPasswordOtp(@RequestBody OtpVerifyRequest otpVerifyRequest,HttpServletRequest req){
        GlobalResponse<ForgetPasswordOtpVerifyResponse> response=GlobalResponse.<ForgetPasswordOtpVerifyResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .path(req.getRequestURI())
                .data(authService.verifyForgotPasswordOtp(otpVerifyRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    ResponseEntity<GlobalResponse<String>> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest,HttpServletRequest req){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .path(req.getRequestURI())
                .data(authService.resetPassword(resetPasswordRequest))
                .build();
        return ResponseEntity.ok(response);
    }
}
