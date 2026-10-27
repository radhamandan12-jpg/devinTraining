package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.RuleSeverity;
import org.springframework.stereotype.Component;

/**
 * Employed / self-employed applicants need a minimum tenure.
 * Unemployed applicants are a hard reject; retired applicants are
 * referred to manual review (income sources differ).
 */
@Component
public class EmploymentStabilityRule implements LendingRule {

    private final LendingRulesProperties props;

    public EmploymentStabilityRule(LendingRulesProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "EMPLOYMENT_STABILITY";
    }

    @Override
    public RuleSeverity severity() {
        return RuleSeverity.REVIEW;
    }

    @Override
    public RuleOutcome evaluate(LoanApplication app) {
        if (app.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            return new RuleOutcome(name(), RuleSeverity.REJECT, false,
                    "Applicant is currently unemployed");
        }
        if (app.getEmploymentStatus() == EmploymentStatus.RETIRED) {
            return RuleOutcome.fail(name(), severity(),
                    "Retired applicant; income sources require officer review");
        }
        Integer years = app.getYearsEmployed();
        if (years == null || years < props.minYearsEmployed()) {
            return RuleOutcome.fail(name(), severity(),
                    "Employment tenure %s years is below minimum %d"
                            .formatted(years, props.minYearsEmployed()));
        }
        return RuleOutcome.pass(name(), severity(),
                "Employment tenure %d years meets minimum %d"
                        .formatted(years, props.minYearsEmployed()));
    }
}
