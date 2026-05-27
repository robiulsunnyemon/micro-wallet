package com.robiulsunyemon.auth_service.service;

import com.robiulsunyemon.auth_service.dto.EmailMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public interface EmailConsumer {

    @RabbitListener(queues = "email_queue")
    void consumeAndSendEmail(EmailMessage message);
}
