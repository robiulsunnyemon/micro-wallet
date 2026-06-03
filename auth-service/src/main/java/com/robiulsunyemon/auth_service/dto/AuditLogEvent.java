package com.robiulsunyemon.auth_service.dto;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class AuditLogEvent {
    private String serviceName;
    private String actionType;
    private String actorId;
    private String actorRole;
    private String resourceId;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String status;
    private String ipAddress;
    private String errorDetails;
    private String deviceInfo;
}