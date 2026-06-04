package com.robiulsunyemon.profile_service.profile.service.impl;
import com.robiulsunyemon.profile_service.profile.dto.AuditLogEvent;
import com.robiulsunyemon.profile_service.profile.service.AuditPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuditPublisherServiceImpl implements AuditPublisherService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.messaging.audit-exchange}")
    private String exchangeName;

    @Value("${rabbitmq.messaging.audit-routing-key}")
    private String routingKey;


    @Async
    @Override
    public void publishAudit(
            String actionType,
            String actorId,
            String resourceId,
            Map<String, Object> oldValue,
            Map<String, Object> newValue,
            String status,
            String ipAddress,
            String deviceInfo,
            String errorDetails
    ) {
        try {
            AuditLogEvent auditEvent = AuditLogEvent.builder()
                    .serviceName("auth-service")
                    .actionType(actionType)
                    .actorId(actorId != null ? actorId : "ANONYMOUS")
                    .actorRole("USER")
                    .resourceId(resourceId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .status(status)
                    .ipAddress(ipAddress)
                    .deviceInfo(deviceInfo)
                    .errorDetails(errorDetails)
                    .build();

            rabbitTemplate.convertAndSend(exchangeName, routingKey, auditEvent);
            log.info("Async Audit Log sent to RabbitMQ for action: {}", actionType);

        } catch (Exception e) {
            log.error("Failed to publish audit log asynchronously for action: {}", actionType, e);
        }
    }
}
