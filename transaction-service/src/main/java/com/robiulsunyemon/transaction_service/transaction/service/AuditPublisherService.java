package com.robiulsunyemon.transaction_service.transaction.service;
import java.util.Map;

public interface AuditPublisherService {
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
    );
}
