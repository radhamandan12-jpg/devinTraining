package com.training.loan.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized, version-controllable lending policy thresholds.
 * Every rule evaluation records the threshold applied, so decisions
 * are auditable against the configuration in effect at the time.
 */
@ConfigurationProperties(prefix = "lending.rules")
public record LendingRulesProperties(
        int minCreditScore,
        int reviewCreditScore,
        BigDecimal maxDebtToIncome,
        BigDecimal reviewDebtToIncome,
        BigDecimal minAnnualIncome,
        int minYearsEmployed,
        BigDecimal maxLoanToIncomeMultiple,
        boolean autoApproveEnabled
) {
    public LendingRulesProperties {
        if (minCreditScore <= 0) minCreditScore = 640;
        if (reviewCreditScore <= 0) reviewCreditScore = 580;
        if (maxDebtToIncome == null) maxDebtToIncome = new BigDecimal("0.36");
        if (reviewDebtToIncome == null) reviewDebtToIncome = new BigDecimal("0.43");
        if (minAnnualIncome == null) minAnnualIncome = new BigDecimal("25000");
        if (minYearsEmployed < 0) minYearsEmployed = 1;
        if (maxLoanToIncomeMultiple == null) maxLoanToIncomeMultiple = new BigDecimal("5");
    }
}
