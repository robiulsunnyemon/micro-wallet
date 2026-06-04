package com.robiulsunyemon.audit_service.consumer;
import com.robiulsunyemon.audit_service.collection.AuditLogCollection;
import com.robiulsunyemon.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogLogConsumer {

    private final AuditLogService auditLogService;

    @RabbitListener(queues = "${rabbitmq.messaging.queue}")
    public void consumeAuditMessage(AuditLogCollection auditLog) {
        log.info("Received audit message from service: {} for action: {}",
                auditLog.getServiceName(), auditLog.getActionType());
        try {
            auditLogService.saveAuditLog(auditLog);
        } catch (Exception e) {
            log.error("Failed to process and save audit log", e);
        }
    }
}