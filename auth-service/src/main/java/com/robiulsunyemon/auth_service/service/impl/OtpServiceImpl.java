package com.robiulsunyemon.auth_service.service.impl;
import com.robiulsunyemon.auth_service.config.RabbitMQConfig;
import com.robiulsunyemon.auth_service.dto.EmailMessage;
import com.robiulsunyemon.auth_service.entity.OtpToken;
import com.robiulsunyemon.auth_service.repository.OtpRepository;
import com.robiulsunyemon.auth_service.service.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Random;
import java.util.UUID;



@AllArgsConstructor
@Service
public class OtpServiceImpl implements OtpService {
    private OtpRepository otpRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendAndSaveOtp(String email) {

        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken = new OtpToken(email, otp);
        otpRepository.save(otpToken);

        String subject = "Micro Wallet - Verification Code";
        String body = "Your OTP code for verification is: " + otp + ". It is valid for 5 minutes.";

        EmailMessage emailMessage = new EmailMessage(email, subject, body);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                emailMessage
        );

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
