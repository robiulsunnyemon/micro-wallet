package com.robiulsunyemon.audit_service.collection;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "audit_logs")
public class AuditLogCollection {

    @Id
    private String id;
    private LocalDateTime timestamp;

    private String serviceName;
    private String actionType;
    private String actorId;
    private String actorRole;
    private String resourceId;


    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;

    private String status;
    private String ipAddress;
    private String deviceInfo;
    private String errorDetails;

}