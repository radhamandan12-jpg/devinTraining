package com.training.loan.domain.enums;

/**
 * Severity attached to a lending rule failure.
 * REJECT  - failure auto-rejects the application (hard credit policy).
 * REVIEW  - failure routes the application to a loan officer for manual review.
 */
public enum RuleSeverity {
    REJECT,
    REVIEW
}
