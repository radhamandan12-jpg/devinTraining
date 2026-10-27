package com.training.loan.service;

import com.training.loan.ai.AiAssistantService;
import com.training.loan.config.LendingRulesProperties;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import com.training.loan.repository.CommunicationRepository;
import com.training.loan.repository.LoanApplicationRepository;
import com.training.loan.repository.RuleEvaluationRepository;
import com.training.loan.rules.CreditScoreRule;
import com.training.loan.rules.DebtToIncomeRule;
import com.training.loan.rules.EmploymentStabilityRule;
import com.training.loan.rules.EvaluationDecision;
import com.training.loan.rules.IncomeRule;
import com.training.loan.rules.LoanAmountRule;
import com.training.loan.rules.RulesEngine;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationPipelineTest {

    @Mock private AiAssistantService aiService;
    @Mock private LoanApplicationRepository applicationRepository;
    @Mock private RuleEvaluationRepository ruleEvaluationRepository;
    @Mock private CommunicationRepository communicationRepository;
    @Mock private AuditService auditService;

    private EvaluationPipeline pipeline;

    @BeforeEach
    void setUp() {
        LendingRulesProperties props = new LendingRulesProperties(
                640, 580, new BigDecimal("0.36"), new BigDecimal("0.43"),
                new BigDecimal("25000"), 1, new BigDecimal("5"), true);
        RulesEngine engine = new RulesEngine(List.of(
                new CreditScoreRule(props), new DebtToIncomeRule(props),
                new IncomeRule(props), new EmploymentStabilityRule(props),
                new LoanAmountRule(props)), props);
        pipeline = new EvaluationPipeline(aiService, engine, applicationRepository,
                ruleEvaluationRepository, communicationRepository, auditService);

        lenient().when(aiService.summarizeApplication(any())).thenReturn("summary");
        lenient().when(aiService.findMissingOrInconsistentInfo(any())).thenReturn("none");
        lenient().when(aiService.highlightRisks(any())).thenReturn("none");
        lenient().when(aiService.explainDecision(any(), any())).thenReturn("explanation");
        lenient().when(aiService.draftCustomerCommunication(any(), any())).thenReturn("letter");
    }

    private LoanApplication strongApplication() {
        LoanApplication app = new LoanApplication();
        app.setId(1L);
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
    void aiCannotOverrideRulesEngineDecision() {
        // Even if the model "says" the applicant deserves a rejection,
        // a rules-passing application is still approved.
        when(aiService.highlightRisks(any()))
                .thenReturn("This applicant should definitely be rejected.");
        EvaluationDecision decision = pipeline.evaluateAndDecide(strongApplication());
        assertThat(decision.status()).isEqualTo(ApplicationStatus.APPROVED);
    }

    @Test
    void decisionRecordedAsRulesEngineAndCommunicationDrafted() {
        LoanApplication app = strongApplication();
        pipeline.evaluateAndDecide(app);
        assertThat(app.getDecidedBy()).isEqualTo("RULES_ENGINE");
        assertThat(app.getDecidedAt()).isNotNull();
        verify(communicationRepository).save(any());
        verify(auditService).record(any(), any(), any(), any());
    }

    @Test
    void manualReviewApplicationsGetNoDecision() {
        LoanApplication app = strongApplication();
        app.setCreditScore(600); // borderline -> manual review
        pipeline.evaluateAndDecide(app);
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.MANUAL_REVIEW);
        assertThat(app.getDecidedBy()).isNull();
    }

    @Test
    void officerDecisionIsAuditedAndFinalized() {
        LoanApplication app = strongApplication();
        app.setCreditScore(600);
        pipeline.evaluateAndDecide(app);

        pipeline.officerDecision(app, ApplicationStatus.APPROVED, "officer1", "verified income");
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(app.getDecidedBy()).isEqualTo("officer1");
        verify(auditService).record(any(), org.mockito.ArgumentMatchers.eq("officer1"),
                org.mockito.ArgumentMatchers.eq("MANUAL_DECISION"), any());
    }

    @Test
    void officerCannotDecideApplicationNotInReview() {
        LoanApplication app = strongApplication();
        pipeline.evaluateAndDecide(app); // auto-approved
        assertThatThrownBy(() ->
                pipeline.officerDecision(app, ApplicationStatus.REJECTED, "o", "n"))
                .isInstanceOf(IllegalStateException.class);
    }
}
