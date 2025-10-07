package com.brokage.challenge.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuditService {
    
    private final AuditRepository auditRepository;
    
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }
    
    public void logSuccess(String operation, String entityType, Long entityId, String customerId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .operation(operation)
                .entityType(entityType)
                .entityId(entityId)
                .customerId(customerId)
                .details(details)
                .status(AuditStatus.SUCCESS)
                .timestamp(Instant.now())
                .build();
        
        auditRepository.save(auditLog);
    }
    
    public void logFailure(String operation, String entityType, Long entityId, String customerId, String details, String errorMessage) {
        AuditLog auditLog = AuditLog.builder()
                .operation(operation)
                .entityType(entityType)
                .entityId(entityId)
                .customerId(customerId)
                .details(details)
                .status(AuditStatus.FAILURE)
                .timestamp(Instant.now())
                .errorMessage(errorMessage)
                .build();
        
        auditRepository.save(auditLog);
    }
}
