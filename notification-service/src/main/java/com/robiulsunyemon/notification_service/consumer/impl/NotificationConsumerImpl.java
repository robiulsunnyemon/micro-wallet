package com.robiulsunyemon.notification_service.consumer.impl;
import com.robiulsunyemon.notification_service.consumer.NotificationConsumer;
import com.robiulsunyemon.notification_service.notification.EmailMessage;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationConsumerImpl implements NotificationConsumer {
    private JavaMailSender mailSender;


    @RabbitListener(queues = "${rabbitmq.messaging.queue}")
    @Override
    public void consumeAndSendEmail(EmailMessage message) {
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
