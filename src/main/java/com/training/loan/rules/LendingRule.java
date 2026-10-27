package com.training.loan.rules;

import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.RuleSeverity;

/**
 * A single deterministic, auditable lending policy rule.
 * Implementations must be pure functions of the application's
 * financial attributes and the configured thresholds.
 */
public interface LendingRule {

    String name();

    RuleSeverity severity();

    RuleOutcome evaluate(LoanApplication application);
}
