package com.robiulsunyemon.auth_service.service.impl;
import com.robiulsunyemon.auth_service.config.RabbitMQConfig;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.entity.AccountStatus;
import com.robiulsunyemon.auth_service.entity.UserEntity;
import com.robiulsunyemon.auth_service.exceptions.DuplicateException;
import com.robiulsunyemon.auth_service.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.auth_service.exceptions.BadRequestException;
import com.robiulsunyemon.auth_service.mapper.AuthMapper;
import com.robiulsunyemon.auth_service.repository.AuthRepository;
import com.robiulsunyemon.auth_service.service.AuditPublisherService;
import com.robiulsunyemon.auth_service.service.AuthService;
import com.robiulsunyemon.auth_service.service.OtpService;
import com.robiulsunyemon.auth_service.utils.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitTemplate rabbitTemplate;
    private final AuditPublisherService auditPublisherService;

    @Override
    @Transactional
    public AuthResponse createUser(AuthRequest request, HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");

        Map<String, Object> auditNewValue = Map.of(
                "username", request.getPhoneNumber(),
                "email", request.getEmail(),
                "role", request.getRole() != null ? request.getRole().name() : "USER"
        );

        try {

            if(authRepository.existsByEmail(request.getEmail())){
                auditPublisherService.publishAudit(
                        "USER_SIGNUP", null, null,
                        null, auditNewValue, "FAILED", ipAddress, deviceInfo, "Email already exists"
                );
                throw new DuplicateException("Email already exists",HttpStatus.BAD_REQUEST);
            }
            if(authRepository.existsByPhoneNumber(request.getPhoneNumber())){
                auditPublisherService.publishAudit(
                        "USER_SIGNUP", null, null,
                        null, auditNewValue, "FAILED", ipAddress, deviceInfo, "Phone Number already exists"
                );
                throw new DuplicateException("Phone Number already exists",HttpStatus.BAD_REQUEST);
            }


            UserEntity user = authMapper.requestToUserEntity(request);
            UserEntity entity=authRepository.save(user);
            AuthResponse response = authMapper.entityToResponse(entity);
           // Audit: Successful signup
            auditPublisherService.publishAudit(
                    "USER_SIGNUP", String.valueOf(entity.getId()), String.valueOf(entity.getId()),
                    null, auditNewValue, "SUCCESS", ipAddress, deviceInfo, null
            );

            otpService.sendAndSaveOtp(request.getEmail());
            return response;
        } catch (RuntimeException e) {
            // Audit: failed signup
            auditPublisherService.publishAudit(
                    "USER_SIGNUP", null, null,
                    null, auditNewValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }
    }

    @Override
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("phone_number", request.getPhoneNumber());

        try {
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


            String token = jwtService.generateToken(user.getPhoneNumber(), user.getRole(),user.getId());
            // Audit: Successful Login
            auditPublisherService.publishAudit(
                    "USER_LOGIN", String.valueOf(user.getId()), String.valueOf(user.getId()),
                    null, auditValue, "SUCCESS", ipAddress, deviceInfo, null
            );

            return new LoginResponse("Bearer", token);
        } catch (RuntimeException e) {
            // Audit: Failed Login
            auditPublisherService.publishAudit(
                    "USER_LOGIN", null, null,
                    null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }
    }

    @Override
    @Transactional
    public String verifyOtp(OtpVerifyRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("email", request.getEmail());

        try {
            boolean isVerified = otpService.verifyOtp(request.getEmail(), request.getOtp());
            if (!isVerified) {
                throw new BadRequestException("The OTP provided is invalid or has expired.", HttpStatus.BAD_REQUEST);
            }

            UserEntity user = authRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Account not found with email: %s", request.getEmail()),
                            HttpStatus.NOT_FOUND
                    ));

            UserCreatedMessage walletMessage=new UserCreatedMessage(user.getId(),user.getEmail(),user.getPhoneNumber());
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getExchangeName(),
                    rabbitMQConfig.getRoutingKeyWallet(),
                    walletMessage
            );
            // Audit: Success OTP Verification
            auditPublisherService.publishAudit(
                    "OTP_VERIFY", String.valueOf(user.getId()), String.valueOf(user.getId()),
                    null, auditValue, "SUCCESS", ipAddress, deviceInfo, null
            );
            return "OTP verified successfully. Your account is now active.";

        } catch (RuntimeException e) {
            System.out.println("Error occur from auth service. No message delivery from auth service. because: "+e);
            // Audit: Failed OTP Verification
            auditPublisherService.publishAudit(
                    "OTP_VERIFY", null, null,
                    null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }
    }

    @RabbitListener(queues = "${rabbitmq.messaging.rollback-queue}")
    @Override
    public void handleRegistrationStatusUpdate(RegistrationStatusMessage statusMessage) {
        System.out.println("successfully come registration status update message");

        try {
            Optional<UserEntity> entity=authRepository.findById(statusMessage.getUserId());
            if (!statusMessage.getIsSucceed()){
                entity.ifPresent(authRepository::delete);
            }else {
                entity.ifPresent(user -> {
                    user.setIsVerified(true);
                    user.setAccountStatus(AccountStatus.ACTIVE);
                    authRepository.save(user);
                });
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String resendOtp(EmailRequest request, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("email", request.getEmail());

        try {
            UserEntity user = authRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Account not found with email: %s", request.getEmail()),
                            HttpStatus.NOT_FOUND
                    ));

            otpService.sendAndSaveOtp(request.getEmail());
            // Audit: Resend OTP Success
            auditPublisherService.publishAudit(
                    "RESEND_OTP", String.valueOf(user.getId()), String.valueOf(user.getId()),
                    null, auditValue, "SUCCESS", ipAddress, deviceInfo, null
            );
            return String.format("OTP has been successfully resent to your email: %s", request.getEmail());
        } catch (RuntimeException e) {
            // Audit: Resend OTP Failed
            auditPublisherService.publishAudit(
                    "RESEND_OTP", null, null,
                    null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }
    }

    @Override
    public String forgotPassword(EmailRequest request, HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("email", request.getEmail());

        try {
            UserEntity user = authRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("Account not found with email: %s", request.getEmail()),
                            HttpStatus.NOT_FOUND
                    ));


            if (user.getAccountStatus() != AccountStatus.ACTIVE || !user.getIsVerified()) {
                throw new BadRequestException("Cannot reset password. Your account must be active and verified.", HttpStatus.BAD_REQUEST);
            }

            otpService.sendAndSaveOtp(request.getEmail());
            // Audit: Forgot Password Request Success
            auditPublisherService.publishAudit(
                    "FORGOT_PASSWORD_REQUEST", String.valueOf(user.getId()), String.valueOf(user.getId()),
                    null, auditValue, "SUCCESS", ipAddress, deviceInfo, null
            );
            return String.format("A password reset OTP has been sent to %s. Please verify it to proceed.", request.getEmail());
        } catch (RuntimeException e) {
            // Audit: Forgot Password Request Failed
            auditPublisherService.publishAudit(
                    "FORGOT_PASSWORD_REQUEST", null, null,
                    null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }
    }

    @Override
    public ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request, HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("email", request.getEmail());

        try {
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
           // Audit: Forgot Password OTP Verify Success
           auditPublisherService.publishAudit(
                   "VERIFY_FORGOT_PASSWORD_OTP", String.valueOf(user.getId()), String.valueOf(user.getId()),
                   null, auditValue, "SUCCESS", ipAddress, deviceInfo, null
           );
            return new ForgetPasswordOtpVerifyResponse("OTP verified successfully. You can now reset your password.", token);
       } catch (RuntimeException e) {
           // Audit: Forgot Password OTP Verify Failed
           auditPublisherService.publishAudit(
                   "VERIFY_FORGOT_PASSWORD_OTP", null, null,
                   null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
           );
           throw e;
       }

    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request, HttpServletRequest httpServletRequest) {

        String ipAddress = httpServletRequest.getRemoteAddr();
        String deviceInfo = httpServletRequest.getHeader("User-Agent");
        Map<String, Object> auditValue = Map.of("email", request.getEmail());

        try {
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

            // Audit: Reset Password Success
            auditPublisherService.publishAudit(
                    "PASSWORD_RESET", String.valueOf(user.getId()), String.valueOf(user.getId()),
                    null, Map.of("status", "Password updated successfully"), "SUCCESS", ipAddress, deviceInfo, null
            );
            return "Your password has been successfully reset. Please log in with your new password.";

        } catch (RuntimeException e) {
            // Audit: Reset Password Failed
            auditPublisherService.publishAudit(
                    "PASSWORD_RESET", null, null,
                    null, auditValue, "FAILED", ipAddress, deviceInfo, e.getMessage()
            );
            throw e;
        }

    }

    @Override
    public AuthResponse findById(Long id) {
        return authRepository.findById(id).map(authMapper::entityToResponse).orElseThrow(
                ()->new ResourceNotFoundException(
                        "User not found by userId: "+id,HttpStatus.NOT_FOUND
                )
        );
    }
}