package com.training.loan.rules;

import com.training.loan.domain.enums.RuleSeverity;

public record RuleOutcome(String ruleName, RuleSeverity severity, boolean passed, String detail) {

    public static RuleOutcome pass(String ruleName, RuleSeverity severity, String detail) {
        return new RuleOutcome(ruleName, severity, true, detail);
    }

    public static RuleOutcome fail(String ruleName, RuleSeverity severity, String detail) {
        return new RuleOutcome(ruleName, severity, false, detail);
    }
}
