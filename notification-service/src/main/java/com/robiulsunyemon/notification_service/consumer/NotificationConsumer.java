package com.robiulsunyemon.notification_service.consumer;
import com.robiulsunyemon.notification_service.notification.EmailMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public interface NotificationConsumer{

    void consumeAndSendEmail(EmailMessage message);
}
