package com.training.loan.repository;

import com.training.loan.domain.AppUser;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByApplicantOrderByCreatedAtDesc(AppUser applicant);

    @EntityGraph(attributePaths = {"applicant"})
    List<LoanApplication> findByStatusOrderByCreatedAtAsc(ApplicationStatus status);

    @EntityGraph(attributePaths = {"applicant", "documents", "ruleEvaluations"})
    Optional<LoanApplication> findDetailedById(Long id);
}
