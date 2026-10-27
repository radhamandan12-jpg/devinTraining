package com.training.loan.rules;

import com.training.loan.domain.enums.ApplicationStatus;
import java.util.List;

/**
 * The deterministic result of running all lending rules.
 *
 * APPROVED      - every rule passed and auto-approval is enabled.
 * REJECTED      - at least one REJECT-severity rule failed.
 * MANUAL_REVIEW - no hard failure, but a REVIEW rule failed, or
 *                 auto-approval is disabled and human sign-off is required.
 */
public record EvaluationDecision(ApplicationStatus status, List<RuleOutcome> outcomes) {

    public List<RuleOutcome> failures() {
        return outcomes.stream().filter(o -> !o.passed()).toList();
    }
}
