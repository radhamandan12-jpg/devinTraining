package com.training.loan.repository;

import com.training.loan.domain.Communication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationRepository extends JpaRepository<Communication, Long> {
    List<Communication> findByApplicationIdOrderByDraftedAtDesc(Long applicationId);
}
