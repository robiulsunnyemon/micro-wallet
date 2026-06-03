package com.robiulsunyemon.audit_service.repository;
import com.robiulsunyemon.audit_service.collection.AuditLogCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLogCollection,String> {
}
