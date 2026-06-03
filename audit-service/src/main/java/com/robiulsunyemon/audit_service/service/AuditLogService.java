package com.robiulsunyemon.audit_service.service;
import com.robiulsunyemon.audit_service.collection.AuditLogCollection;

public interface AuditLogService {
    public void saveAuditLog(AuditLogCollection auditLog);
}
