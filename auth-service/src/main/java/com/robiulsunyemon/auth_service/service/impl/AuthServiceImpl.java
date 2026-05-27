package com.robiulsunyemon.auth_service.service.impl;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.entity.AccountStatus;
import com.robiulsunyemon.auth_service.entity.UserEntity;
import com.robiulsunyemon.auth_service.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.auth_service.exceptions.BadRequestException;
import com.robiulsunyemon.auth_service.mapper.AuthMapper;
import com.robiulsunyemon.auth_service.repository.AuthRepository;
import com.robiulsunyemon.auth_service.service.AuthService;
import com.robiulsunyemon.auth_service.service.OtpService;
import com.robiulsunyemon.auth_service.utils.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse createUser(AuthRequest request) {
        UserEntity user = authMapper.requestToUserEntity(request);
        AuthResponse response = authMapper.entityToResponse(authRepository.save(user));
        otpService.sendAndSaveOtp(request.getEmail());
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        UserEntity user = authRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User not found with phone number: %s", request.getPhoneNumber()),
                        HttpStatus.NOT_FOUND
                ));


        if (!user.getIsVerified()) {
            throw new DisabledException("Your account is not verified. Please verify your OTP first.");
        }
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new DisabledException("Your account is currently inactive or suspended.");
        }


        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid phone number or password.");
        }


        String token = jwtService.generateToken(user.getPhoneNumber(), user.getRole());
        return new LoginResponse("Bearer", token);
    }

    @Override
    @Transactional
    public String verifyOtp(OtpVerifyRequest request) {
        boolean isVerified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isVerified) {
            throw new BadRequestException("The OTP provided is invalid or has expired.", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Account not found with email: %s", request.getEmail()),
                        HttpStatus.NOT_FOUND
                ));

        user.setIsVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        authRepository.save(user);

        return "OTP verified successfully. Your account is now active.";
    }

    @Override
    public String resendOtp(EmailRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Account not found with email: %s", request.getEmail()),
                        HttpStatus.NOT_FOUND
                ));

        otpService.sendAndSaveOtp(request.getEmail());
        return String.format("OTP has been successfully resent to your email: %s", request.getEmail());
    }

    @Override
    public String forgotPassword(EmailRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Account not found with email: %s", request.getEmail()),
                        HttpStatus.NOT_FOUND
                ));


        if (user.getAccountStatus() != AccountStatus.ACTIVE || !user.getIsVerified()) {
            throw new BadRequestException("Cannot reset password. Your account must be active and verified.", HttpStatus.BAD_REQUEST);
        }

        otpService.sendAndSaveOtp(request.getEmail());
        return String.format("A password reset OTP has been sent to %s. Please verify it to proceed.", request.getEmail());
    }

    @Override
    public ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Account not found with email: %s", request.getEmail()),
                        HttpStatus.NOT_FOUND
                ));

        boolean isVerified = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isVerified) {
            throw new BadRequestException("The OTP provided is invalid or has expired.", HttpStatus.BAD_REQUEST);
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE || !user.getIsVerified()) {
            throw new BadRequestException("Account must be active and verified to verify OTP.", HttpStatus.BAD_REQUEST);
        }

        String token = otpService.sendForgetPasswordToken(request.getEmail());
        return new ForgetPasswordOtpVerifyResponse("OTP verified successfully. You can now reset your password.", token);
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Account not found with email: %s", request.getEmail()),
                        HttpStatus.NOT_FOUND
                ));


        boolean isTokenValid = otpService.verifyOtp(request.getEmail(), request.getResetToken());
        if (!isTokenValid) {
            throw new BadRequestException("Invalid or expired password reset token.", HttpStatus.BAD_REQUEST);
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE || !user.getIsVerified()) {
            throw new BadRequestException("Account must be active and verified to reset password.", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);

        return "Your password has been successfully reset. Please log in with your new password.";
    }
}