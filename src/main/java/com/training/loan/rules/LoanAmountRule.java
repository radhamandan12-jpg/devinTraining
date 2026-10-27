package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.RuleSeverity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * Requested amount must stay within a multiple of annual income.
 * Exceeding it is not an automatic rejection - it goes to an officer.
 */
@Component
public class LoanAmountRule implements LendingRule {

    private final LendingRulesProperties props;

    public LoanAmountRule(LendingRulesProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "MAX_LOAN_TO_INCOME";
    }

    @Override
    public RuleSeverity severity() {
        return RuleSeverity.REVIEW;
    }

    @Override
    public RuleOutcome evaluate(LoanApplication app) {
        if (app.getAnnualIncome() == null || app.getAnnualIncome().signum() <= 0
                || app.getRequestedAmount() == null) {
            return RuleOutcome.fail(name(), severity(),
                    "Cannot compare requested amount to income; manual review required");
        }
        BigDecimal multiple = app.getRequestedAmount()
                .divide(app.getAnnualIncome(), 4, RoundingMode.HALF_UP);
        if (multiple.compareTo(props.maxLoanToIncomeMultiple()) > 0) {
            return RuleOutcome.fail(name(), severity(),
                    "Requested amount is %.2fx annual income (max %.2fx)"
                            .formatted(multiple, props.maxLoanToIncomeMultiple()));
        }
        return RuleOutcome.pass(name(), severity(),
                "Requested amount is %.2fx annual income (within %.2fx)"
                        .formatted(multiple, props.maxLoanToIncomeMultiple()));
    }
}
