package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.RuleSeverity;
import org.springframework.stereotype.Component;

/**
 * Below the review floor      -> REJECT
 * Between floor and minimum   -> REVIEW (officer discretion)
 * At or above the minimum     -> pass
 */
@Component
public class CreditScoreRule implements LendingRule {

    private final LendingRulesProperties props;

    public CreditScoreRule(LendingRulesProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "MIN_CREDIT_SCORE";
    }

    @Override
    public RuleSeverity severity() {
        return RuleSeverity.REVIEW;
    }

    @Override
    public RuleOutcome evaluate(LoanApplication app) {
        Integer score = app.getCreditScore();
        if (score == null) {
            return new RuleOutcome(name(), RuleSeverity.REVIEW, false,
                    "No credit score on file; requires manual verification");
        }
        if (score < props.reviewCreditScore()) {
            return new RuleOutcome(name(), RuleSeverity.REJECT, false,
                    "Credit score %d is below the review floor %d"
                            .formatted(score, props.reviewCreditScore()));
        }
        if (score < props.minCreditScore()) {
            return RuleOutcome.fail(name(), severity(),
                    "Credit score %d is below preferred minimum %d (manual review allowed from %d)"
                            .formatted(score, props.minCreditScore(), props.reviewCreditScore()));
        }
        return RuleOutcome.pass(name(), severity(),
                "Credit score %d meets minimum %d".formatted(score, props.minCreditScore()));
    }
}
