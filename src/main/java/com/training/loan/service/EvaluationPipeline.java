package com.training.loan.service;

import com.training.loan.ai.AiAssistantService;
import com.training.loan.domain.Communication;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.RuleEvaluation;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.repository.CommunicationRepository;
import com.training.loan.repository.LoanApplicationRepository;
import com.training.loan.repository.RuleEvaluationRepository;
import com.training.loan.rules.EvaluationDecision;
import com.training.loan.rules.RulesEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates one evaluation cycle:
 *   1. AI advisory analysis (summary, gaps, risks) - stored for the officer
 *   2. Deterministic rules engine - sole source of the automated decision
 *   3. AI explanation + communication draft - generated AFTER the decision
 *
 * Order matters: the model analyzes first but decides never.
 */
@Service
public class EvaluationPipeline {

    private final AiAssistantService aiService;
    private final RulesEngine rulesEngine;
    private final LoanApplicationRepository applicationRepository;
    private final RuleEvaluationRepository ruleEvaluationRepository;
    private final CommunicationRepository communicationRepository;
    private final AuditService auditService;

    public EvaluationPipeline(AiAssistantService aiService, RulesEngine rulesEngine,
                              LoanApplicationRepository applicationRepository,
                              RuleEvaluationRepository ruleEvaluationRepository,
                              CommunicationRepository communicationRepository,
                              AuditService auditService) {
        this.aiService = aiService;
        this.rulesEngine = rulesEngine;
        this.applicationRepository = applicationRepository;
        this.ruleEvaluationRepository = ruleEvaluationRepository;
        this.communicationRepository = communicationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public EvaluationDecision evaluateAndDecide(LoanApplication application) {
        application.setAiSummary(aiService.summarizeApplication(application));
        application.setAiMissingInfo(aiService.findMissingOrInconsistentInfo(application));
        application.setAiRiskNotes(aiService.highlightRisks(application));

        EvaluationDecision decision = rulesEngine.evaluate(application);

        ruleEvaluationRepository.saveAll(decision.outcomes().stream()
                .map(o -> new RuleEvaluation(application, o.ruleName(), o.severity(),
                        o.passed(), o.detail()))
                .toList());

        application.setStatus(decision.status());
        if (decision.status() != ApplicationStatus.MANUAL_REVIEW) {
            application.setDecidedBy("RULES_ENGINE");
            application.setDecidedAt(java.time.LocalDateTime.now());
            finalizeDecision(application, decision);
        }
        application.setUpdatedAt(java.time.LocalDateTime.now());
        applicationRepository.save(application);

        auditService.record(application.getId(), "SYSTEM", "EVALUATED",
                "status=%s rules=%d failures=%d".formatted(decision.status(),
                        decision.outcomes().size(), decision.failures().size()));
        return decision;
    }

    /**
     * Authorized loan officer makes the binding decision when the rules
     * engine routed the application to manual review.
     */
    @Transactional
    public void officerDecision(LoanApplication application, ApplicationStatus decision,
                                String officerUsername, String notes) {
        if (application.getStatus() != ApplicationStatus.MANUAL_REVIEW) {
            throw new IllegalStateException(
                    "Application is not awaiting manual review (status=" + application.getStatus() + ")");
        }
        if (decision != ApplicationStatus.APPROVED && decision != ApplicationStatus.REJECTED) {
            throw new IllegalArgumentException("Officer decision must be APPROVED or REJECTED");
        }

        application.setStatus(decision);
        application.setDecidedBy(officerUsername);
        application.setDecidedAt(java.time.LocalDateTime.now());
        application.setDecisionNotes(notes);
        application.setUpdatedAt(java.time.LocalDateTime.now());

        EvaluationDecision synthetic = new EvaluationDecision(decision,
                application.getRuleEvaluations().stream()
                        .map(e -> new com.training.loan.rules.RuleOutcome(
                                e.getRuleName(), e.getSeverity(), e.isPassed(), e.getDetail()))
                        .toList());
        finalizeDecision(application, synthetic);
        applicationRepository.save(application);

        auditService.record(application.getId(), officerUsername, "MANUAL_DECISION",
                "decision=%s notes=%s".formatted(decision, notes));
    }

    private void finalizeDecision(LoanApplication application, EvaluationDecision decision) {
        application.setDecisionExplanation(aiService.explainDecision(application, decision));
        String letter = aiService.draftCustomerCommunication(application, decision);
        communicationRepository.save(new Communication(application, "EMAIL",
                "Loan application " + decision.status(), letter));
    }
}
