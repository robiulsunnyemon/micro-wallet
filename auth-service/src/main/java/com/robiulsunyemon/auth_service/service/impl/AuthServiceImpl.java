package com.robiulsunyemon.auth_service.service.impl;
import com.robiulsunyemon.auth_service.dto.*;
import com.robiulsunyemon.auth_service.entity.AccountStatus;
import com.robiulsunyemon.auth_service.entity.UserEntity;
import com.robiulsunyemon.auth_service.exceptions.ResourceNotFoundException;
import com.robiulsunyemon.auth_service.mapper.AuthMapper;
import com.robiulsunyemon.auth_service.repository.AuthRepository;
import com.robiulsunyemon.auth_service.service.AuthService;
import com.robiulsunyemon.auth_service.service.OtpService;
import com.robiulsunyemon.auth_service.utils.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private AuthRepository authRepository;
    private AuthMapper authMapper;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private OtpService otpService;
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse createUser(AuthRequest request) {
        UserEntity user=authMapper.requestToUserEntity(request);
        AuthResponse response=authMapper.entityToResponse(authRepository.save(user));
        otpService.sendAndSaveOtp(request.getEmail());
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(),request.getPassword())

        );
        // Get the authenticated user details
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Fetch the user entity to get the role
        UserEntity user = authRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account is not found", HttpStatus.NOT_FOUND));

        if(user.getIsVerified()==false){
            throw new ResourceNotFoundException("Account is not varified", HttpStatus.NOT_FOUND);
        }
        if(user.getAccountStatus()!=AccountStatus.ACTIVE){
            throw new ResourceNotFoundException("Account is not active", HttpStatus.NOT_FOUND);
        }

        // Generate token with phone number and role
        String token = jwtService.generateToken(user.getPhoneNumber(), user.getRole());
        return new LoginResponse("Bearer",token);
    }

    @Override
    public String verifyOtp(OtpVerifyRequest request) {
        boolean isVerified= otpService.verifyOtp(request.getEmail(),request.getOtp());
        if(isVerified){
            UserEntity user = authRepository.findByEmail(request.getEmail())
                    .orElseThrow(() ->new ResourceNotFoundException("Account is not found by "+request.getEmail(), HttpStatus.NOT_FOUND));
            user.setIsVerified(true);
            user.setAccountStatus(AccountStatus.ACTIVE);
            authRepository.save(user);
            return "Otp verify successfully";
        }else {
            return "Invalid Otp";
        }
    }

    @Override
    public String resendOtp(EmailRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new ResourceNotFoundException("Account is not found by "+request.getEmail(), HttpStatus.NOT_FOUND));
        otpService.sendAndSaveOtp(request.getEmail());
        return "Successfully resend otp in you email: "+request.getEmail();
    }

    @Override
    public String forgotPassword(EmailRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new ResourceNotFoundException("Account is not found by "+request.getEmail(), HttpStatus.NOT_FOUND));
        if(user.getAccountStatus()!= AccountStatus.ACTIVE && !user.getIsVerified()){
            throw new ResourceNotFoundException("Account is active or not varified",HttpStatus.BAD_REQUEST);
        }
        otpService.sendAndSaveOtp(request.getEmail());
        return "Successfully send otp in your email "+request.getEmail() +" .Please verify otp";
    }

    @Override
    public ForgetPasswordOtpVerifyResponse verifyForgotPasswordOtp(OtpVerifyRequest request) {

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new ResourceNotFoundException("Account is not found by "+request.getEmail(), HttpStatus.NOT_FOUND));

        boolean isVerified= otpService.verifyOtp(request.getEmail(),request.getOtp());
        if(!isVerified){
           throw new ResourceNotFoundException("Invalid Otp",HttpStatus.BAD_REQUEST);
        }


        if(user.getAccountStatus()!= AccountStatus.ACTIVE && !user.getIsVerified()){
            throw new ResourceNotFoundException("Account is active or not varified",HttpStatus.BAD_REQUEST);
        }

        String token=otpService.sendForgetPasswordToken(request.getEmail());
        return new ForgetPasswordOtpVerifyResponse("Successfully verify otp",token);
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->new ResourceNotFoundException("Account is not found by "+request.getEmail(), HttpStatus.NOT_FOUND));

        boolean isVerified= otpService.verifyOtp(request.getEmail(),request.getResetToken());
        if(!isVerified){
            throw new ResourceNotFoundException("Invalid Token",HttpStatus.BAD_REQUEST);
        }
        if(user.getAccountStatus()!= AccountStatus.ACTIVE && !user.getIsVerified()){
            throw new ResourceNotFoundException("Account is active or not varified",HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepository.save(user);
        return "Successfully reset your password";
    }

}
