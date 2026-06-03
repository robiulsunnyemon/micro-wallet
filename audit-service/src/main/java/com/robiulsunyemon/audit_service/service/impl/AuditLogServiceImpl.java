package com.robiulsunyemon.audit_service.service.impl;
import com.robiulsunyemon.audit_service.collection.AuditLogCollection;
import com.robiulsunyemon.audit_service.repository.AuditLogRepository;
import com.robiulsunyemon.audit_service.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    public void saveAuditLog(AuditLogCollection auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        auditLogRepository.save(auditLog);
        log.info("Audit log successfully saved in MongoDB");
    }
}
