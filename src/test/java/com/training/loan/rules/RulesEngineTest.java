package com.training.loan.rules;

import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RulesEngineTest {

    private RulesEngine engine;

    @BeforeEach
    void setUp() {
        LendingRulesProperties props = new LendingRulesProperties(
                640, 580,
                new BigDecimal("0.36"), new BigDecimal("0.43"),
                new BigDecimal("25000"), 1,
                new BigDecimal("5"), true);
        engine = new RulesEngine(List.of(
                new CreditScoreRule(props),
                new DebtToIncomeRule(props),
                new IncomeRule(props),
                new EmploymentStabilityRule(props),
                new LoanAmountRule(props)), props);
    }

    private LoanApplication strongApplication() {
        LoanApplication app = new LoanApplication();
        app.setRequestedAmount(new BigDecimal("50000"));
        app.setTermMonths(60);
        app.setLoanPurpose(LoanPurpose.HOME);
        app.setAnnualIncome(new BigDecimal("120000"));
        app.setMonthlyDebtPayments(new BigDecimal("1500"));
        app.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        app.setYearsEmployed(5);
        app.setCreditScore(780);
        return app;
    }

    @Test
    void approvesStrongApplication() {
        EvaluationDecision decision = engine.evaluate(strongApplication());
        assertThat(decision.status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(decision.failures()).isEmpty();
    }

    @Test
    void rejectsWhenIncomeBelowMinimum() {
        LoanApplication app = strongApplication();
        app.setAnnualIncome(new BigDecimal("10000"));
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(decision.failures()).extracting(RuleOutcome::ruleName)
                .contains("MIN_ANNUAL_INCOME");
    }

    @Test
    void rejectsWhenCreditScoreBelowFloor() {
        LoanApplication app = strongApplication();
        app.setCreditScore(500);
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void routesBorderlineCreditToManualReview() {
        LoanApplication app = strongApplication();
        app.setCreditScore(600);
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.MANUAL_REVIEW);
        assertThat(decision.failures()).extracting(RuleOutcome::ruleName)
                .contains("MIN_CREDIT_SCORE");
    }

    @Test
    void routesHighButNotExcessiveDtiToManualReview() {
        LoanApplication app = strongApplication();
        app.setMonthlyDebtPayments(new BigDecimal("4000")); // 40% DTI
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.MANUAL_REVIEW);
        assertThat(decision.failures()).extracting(RuleOutcome::ruleName)
                .contains("MAX_DEBT_TO_INCOME");
    }

    @Test
    void rejectsExcessiveDti() {
        LoanApplication app = strongApplication();
        app.setMonthlyDebtPayments(new BigDecimal("5000")); // 50% DTI
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void rejectsUnemployedApplicant() {
        LoanApplication app = strongApplication();
        app.setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        EvaluationDecision decision = engine.evaluate(app);
        assertThat(decision.status()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void manualReviewWhenAutoApproveDisabled() {
        LendingRulesProperties props = new LendingRulesProperties(
                640, 580, new BigDecimal("0.36"), new BigDecimal("0.43"),
                new BigDecimal("25000"), 1, new BigDecimal("5"), false);
        RulesEngine noAuto = new RulesEngine(List.of(
                new CreditScoreRule(props), new DebtToIncomeRule(props),
                new IncomeRule(props), new EmploymentStabilityRule(props),
                new LoanAmountRule(props)), props);
        assertThat(noAuto.evaluate(strongApplication()).status())
                .isEqualTo(ApplicationStatus.MANUAL_REVIEW);
    }
}
