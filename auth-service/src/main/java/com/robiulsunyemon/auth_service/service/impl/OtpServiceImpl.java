package com.robiulsunyemon.auth_service.service.impl;
import com.robiulsunyemon.auth_service.entity.OtpToken;
import com.robiulsunyemon.auth_service.repository.OtpRepository;
import com.robiulsunyemon.auth_service.service.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@AllArgsConstructor
@Service
public class OtpServiceImpl implements OtpService {
    private OtpRepository otpRepository;
    private JavaMailSender mailSender;

    @Override
    public void sendAndSaveOtp(String email) {

        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken(email, otp);
        otpRepository.save(otpToken);


        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Micro Wallet - Verification Code");
        message.setText("Your OTP code for verification is: " + otp + ". It is valid for 5 minutes.");

        mailSender.send(message);
    }


    @Override
    public boolean verifyOtp(String email, String inputOtp) {
        return otpRepository.findById(email)
                .map(token -> {
                    if (token.getOtpCode().equals(inputOtp)) {
                        otpRepository.delete(token);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    @Override
    public String sendForgetPasswordToken(String email) {
        String token= UUID.randomUUID().toString();
        OtpToken otpToken = new OtpToken(email, token);
        otpRepository.save(otpToken);
        return token;

    }

    @Override
    public boolean verifyForgetPasswordToken(String email, String inputToken) {
        return otpRepository.findById(email)
                .map(token -> {
                    if (token.getOtpCode().equals(inputToken)) {
                        otpRepository.delete(token);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
