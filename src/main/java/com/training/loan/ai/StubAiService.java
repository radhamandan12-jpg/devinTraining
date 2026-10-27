package com.training.loan.ai;

import com.training.loan.domain.LoanApplication;
import com.training.loan.rules.EvaluationDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deterministic local fallback used when no Anthropic API key is
 * configured, so the whole pipeline runs end-to-end offline.
 */
public class StubAiService implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(StubAiService.class);

    public StubAiService() {
        log.info("No anthropic.api-key configured - using local stub AI service");
    }

    @Override
    public String extractDocumentData(String filename, String documentText) {
        return "[stub] Document '%s' received (%d chars). Configure anthropic.api-key "
                .formatted(filename, documentText == null ? 0 : documentText.length())
                + "for real extraction.";
    }

    @Override
    public String summarizeApplication(LoanApplication app) {
        return "[stub] Applicant requests %s over %d months for %s. Annual income %s, "
                .formatted(app.getRequestedAmount(), app.getTermMonths(), app.getLoanPurpose(),
                        app.getAnnualIncome())
                + "monthly debt %s, credit score %s, %s employed for %s years."
                .formatted(app.getMonthlyDebtPayments(), app.getCreditScore(),
                        app.getEmploymentStatus(), app.getYearsEmployed());
    }

    @Override
    public String findMissingOrInconsistentInfo(LoanApplication app) {
        StringBuilder flags = new StringBuilder();
        if (app.getCreditScore() == null) flags.append("- Credit score not provided\n");
        if (app.getYearsEmployed() == null) flags.append("- Employment tenure not provided\n");
        if (app.getAnnualIncome() == null || app.getAnnualIncome().signum() <= 0) {
            flags.append("- Annual income missing\n");
        }
        return flags.isEmpty() ? "[stub] No obvious gaps detected." : flags.toString();
    }

    @Override
    public String highlightRisks(LoanApplication app) {
        StringBuilder flags = new StringBuilder();
        var dti = app.debtToIncomeRatio();
        if (dti != null && dti.doubleValue() > 0.36) {
            flags.append("- Elevated debt-to-income ratio (").append(dti).append(")\n");
        }
        if (app.getCreditScore() != null && app.getCreditScore() < 700) {
            flags.append("- Credit score below 700\n");
        }
        return flags.isEmpty() ? "[stub] No elevated risk factors detected." : flags.toString();
    }

    @Override
    public String explainDecision(LoanApplication app, EvaluationDecision decision) {
        if (decision.failures().isEmpty()) {
            return "[stub] Your application met all lending criteria and was "
                    + decision.status() + ".";
        }
        StringBuilder sb = new StringBuilder("[stub] Your application was ")
                .append(decision.status()).append(" based on: ");
        decision.failures().forEach(f -> sb.append(f.ruleName()).append(" (")
                .append(f.detail()).append("); "));
        return sb.toString();
    }

    @Override
    public String draftCustomerCommunication(LoanApplication app, EvaluationDecision decision) {
        return "Dear applicant,\n\nYour loan application for %s has been %s.\n\n%s\n\n"
                .formatted(app.getRequestedAmount(), decision.status(),
                        explainDecision(app, decision))
                + "Please contact your loan officer with any questions.";
    }
}
