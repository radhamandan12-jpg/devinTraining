package com.training.loan.repository;

import com.training.loan.domain.RuleEvaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleEvaluationRepository extends JpaRepository<RuleEvaluation, Long> {
    List<RuleEvaluation> findByApplicationId(Long applicationId);
}
