package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.domain.enums.RuleSeverity;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Deterministic decision authority. Runs every registered LendingRule
 * and reduces the outcomes to a final status. The AI assistant has no
 * input here - this is the guarantee that the model never decides.
 */
@Service
public class RulesEngine {

    private final List<LendingRule> rules;
    private final LendingRulesProperties props;

    public RulesEngine(List<LendingRule> rules, LendingRulesProperties props) {
        this.rules = rules;
        this.props = props;
    }

    public EvaluationDecision evaluate(LoanApplication application) {
        List<RuleOutcome> outcomes = rules.stream()
                .map(rule -> rule.evaluate(application))
                .toList();

        boolean hardFailure = outcomes.stream()
                .anyMatch(o -> !o.passed() && o.severity() == RuleSeverity.REJECT);
        boolean reviewNeeded = outcomes.stream().anyMatch(o -> !o.passed());

        ApplicationStatus status;
        if (hardFailure) {
            status = ApplicationStatus.REJECTED;
        } else if (reviewNeeded || !props.autoApproveEnabled()) {
            status = ApplicationStatus.MANUAL_REVIEW;
        } else {
            status = ApplicationStatus.APPROVED;
        }
        return new EvaluationDecision(status, outcomes);
    }
}
