package com.robiulsunyemon.auth_service.service.impl;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import com.robiulsunyemon.auth_service.dto.EmailMessage;
import com.robiulsunyemon.auth_service.service.EmailConsumer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailConsumerImpl implements EmailConsumer {
    private JavaMailSender mailSender;
    @Override
    public void consumeAndSendEmail(EmailMessage message) {
        System.out.println("Received message from RabbitMQ for email: " + message.getToEmail());

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(message.getToEmail());
            mailMessage.setSubject(message.getSubject());
            mailMessage.setText(message.getBody());

            mailSender.send(mailMessage);
            System.out.println("Email successfully sent to " + message.getToEmail());
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}
