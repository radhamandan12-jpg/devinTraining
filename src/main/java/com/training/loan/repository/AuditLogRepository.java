package com.training.loan.repository;

import com.training.loan.domain.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}
