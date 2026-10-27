package com.training.loan.ai;

import com.training.loan.domain.LoanApplication;
import com.training.loan.rules.EvaluationDecision;

/**
 * Claude-powered advisory functions ONLY. Implementations of this
 * interface never return a decision - they return analysis that a
 * human or the deterministic rules engine consumes.
 */
public interface AiAssistantService {

    /** Extract structured financial facts from uploaded document text. */
    String extractDocumentData(String filename, String documentText);

    /** Produce a neutral summary of the application for the officer. */
    String summarizeApplication(LoanApplication application);

    /** Flag missing or internally inconsistent information. */
    String findMissingOrInconsistentInfo(LoanApplication application);

    /** Highlight potential risk factors for the reviewer. */
    String highlightRisks(LoanApplication application);

    /** Plain-language explanation of an already-made decision. */
    String explainDecision(LoanApplication application, EvaluationDecision decision);

    /** Draft the customer-facing letter for an already-made decision. */
    String draftCustomerCommunication(LoanApplication application, EvaluationDecision decision);
}
