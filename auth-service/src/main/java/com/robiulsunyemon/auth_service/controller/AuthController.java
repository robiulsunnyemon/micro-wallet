package com.robiulsunyemon.auth_service.controller;

import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    ResponseEntity<GlobalResponse<AuthResponse>> createUser(@RequestBody AuthRequest request){
        AuthResponse authResponse=authService.createUser(request);
        GlobalResponse<AuthResponse> response=GlobalResponse.<AuthResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("success")
                .data(authResponse)
                .build();
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @PostMapping("/login")
    ResponseEntity<GlobalResponse<LoginResponse>> loginResponse(@RequestBody LoginRequest request){
        GlobalResponse<LoginResponse> response=GlobalResponse.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.login(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-signup")
    ResponseEntity<GlobalResponse<String>> verifyOtp(@RequestBody OtpVerifyRequest request){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.verifyOtp(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    ResponseEntity<GlobalResponse<String>> resendOtp(@RequestBody EmailRequest request){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.resendOtp(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    ResponseEntity<GlobalResponse<String>> forgotPassword(@RequestBody EmailRequest request){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.forgotPassword(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/verify")
    ResponseEntity<GlobalResponse<ForgetPasswordOtpVerifyResponse>> verifyForgotPasswordOtp(@RequestBody OtpVerifyRequest request){
        GlobalResponse<ForgetPasswordOtpVerifyResponse> response=GlobalResponse.<ForgetPasswordOtpVerifyResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.verifyForgotPasswordOtp(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    ResponseEntity<GlobalResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request){
        GlobalResponse<String> response=GlobalResponse.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("success")
                .data(authService.resetPassword(request))
                .build();
        return ResponseEntity.ok(response);
    }
}
