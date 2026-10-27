package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.RuleSeverity;
import org.springframework.stereotype.Component;

@Component
public class IncomeRule implements LendingRule {

    private final LendingRulesProperties props;

    public IncomeRule(LendingRulesProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "MIN_ANNUAL_INCOME";
    }

    @Override
    public RuleSeverity severity() {
        return RuleSeverity.REJECT;
    }

    @Override
    public RuleOutcome evaluate(LoanApplication app) {
        if (app.getAnnualIncome() == null
                || app.getAnnualIncome().compareTo(props.minAnnualIncome()) < 0) {
            return RuleOutcome.fail(name(), severity(),
                    "Annual income %s is below minimum %s"
                            .formatted(app.getAnnualIncome(), props.minAnnualIncome()));
        }
        return RuleOutcome.pass(name(), severity(),
                "Annual income %s meets minimum %s"
                        .formatted(app.getAnnualIncome(), props.minAnnualIncome()));
    }
}
