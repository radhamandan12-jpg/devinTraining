package com.training.loan.service;

import com.training.loan.domain.AuditLog;
import com.training.loan.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long applicationId, String actor, String action, String details) {
        auditLogRepository.save(new AuditLog(applicationId, actor, action, details));
    }
}
