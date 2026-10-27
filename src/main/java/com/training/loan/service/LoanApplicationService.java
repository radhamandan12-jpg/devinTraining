package com.training.loan.service;

import com.training.loan.domain.AppUser;
import com.training.loan.domain.Communication;
import com.training.loan.domain.LoanApplication;
import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.repository.CommunicationRepository;
import com.training.loan.repository.LoanApplicationRepository;
import com.training.loan.web.dto.ApplicationForm;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final CommunicationRepository communicationRepository;
    private final EvaluationPipeline evaluationPipeline;
    private final AuditService auditService;

    public LoanApplicationService(LoanApplicationRepository applicationRepository,
                                  CommunicationRepository communicationRepository,
                                  EvaluationPipeline evaluationPipeline,
                                  AuditService auditService) {
        this.applicationRepository = applicationRepository;
        this.communicationRepository = communicationRepository;
        this.evaluationPipeline = evaluationPipeline;
        this.auditService = auditService;
    }

    @Transactional
    public LoanApplication submit(AppUser applicant, ApplicationForm form) {
        LoanApplication app = new LoanApplication();
        app.setApplicant(applicant);
        app.setRequestedAmount(form.getRequestedAmount());
        app.setTermMonths(form.getTermMonths());
        app.setLoanPurpose(form.getLoanPurpose());
        app.setAnnualIncome(form.getAnnualIncome());
        app.setMonthlyDebtPayments(form.getMonthlyDebtPayments());
        app.setEmploymentStatus(form.getEmploymentStatus());
        app.setYearsEmployed(form.getYearsEmployed());
        app.setCreditScore(form.getCreditScore());
        app.setApplicantStatement(form.getApplicantStatement());
        app.setStatus(ApplicationStatus.SUBMITTED);
        LoanApplication saved = applicationRepository.save(app);

        auditService.record(saved.getId(), applicant.getUsername(), "APPLICATION_SUBMITTED",
                "amount=%s term=%d purpose=%s".formatted(form.getRequestedAmount(),
                        form.getTermMonths(), form.getLoanPurpose()));

        evaluationPipeline.evaluateAndDecide(saved);
        return saved;
    }

    public List<LoanApplication> forApplicant(AppUser applicant) {
        return applicationRepository.findByApplicantOrderByCreatedAtDesc(applicant);
    }

    public List<LoanApplication> manualReviewQueue() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(ApplicationStatus.MANUAL_REVIEW);
    }

    public LoanApplication getForApplicant(Long id, AppUser applicant) {
        LoanApplication app = get(id);
        if (!app.getApplicant().getId().equals(applicant.getId())) {
            throw new AccessDeniedException("Not your application");
        }
        return app;
    }

    public LoanApplication get(Long id) {
        return applicationRepository.findDetailedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    public List<Communication> communicationsFor(Long applicationId) {
        return communicationRepository.findByApplicationIdOrderByDraftedAtDesc(applicationId);
    }
}
