package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.RuleSeverity;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

/**
 * Debt-to-income bands:
 *   <= maxDebtToIncome    -> pass
 *   <= reviewDebtToIncome -> REVIEW
 *   >  reviewDebtToIncome -> REJECT
 */
@Component
public class DebtToIncomeRule implements LendingRule {

    private final LendingRulesProperties props;

    public DebtToIncomeRule(LendingRulesProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "MAX_DEBT_TO_INCOME";
    }

    @Override
    public RuleSeverity severity() {
        return RuleSeverity.REVIEW;
    }

    @Override
    public RuleOutcome evaluate(LoanApplication app) {
        BigDecimal dti = app.debtToIncomeRatio();
        if (dti == null) {
            return new RuleOutcome(name(), RuleSeverity.REVIEW, false,
                    "Cannot compute DTI (missing income or debt figures); manual review required");
        }
        if (dti.compareTo(props.reviewDebtToIncome()) > 0) {
            return new RuleOutcome(name(), RuleSeverity.REJECT, false,
                    "DTI %.2f exceeds hard ceiling %.2f".formatted(dti, props.reviewDebtToIncome()));
        }
        if (dti.compareTo(props.maxDebtToIncome()) > 0) {
            return RuleOutcome.fail(name(), severity(),
                    "DTI %.2f exceeds preferred maximum %.2f (up to %.2f reviewable)"
                            .formatted(dti, props.maxDebtToIncome(), props.reviewDebtToIncome()));
        }
        return RuleOutcome.pass(name(), severity(),
                "DTI %.2f within maximum %.2f".formatted(dti, props.maxDebtToIncome()));
    }
}
