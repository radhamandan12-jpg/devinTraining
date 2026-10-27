package com.training.loan.web.dto;

import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationForm {

    @NotNull
    @DecimalMin("1000")
    private BigDecimal requestedAmount;

    @NotNull
    @Min(6)
    @Max(360)
    private Integer termMonths;

    @NotNull
    private LoanPurpose loanPurpose;

    @NotNull
    @Positive
    private BigDecimal annualIncome;

    @NotNull
    @DecimalMin("0")
    private BigDecimal monthlyDebtPayments;

    @NotNull
    private EmploymentStatus employmentStatus;

    @Min(0)
    @Max(80)
    private Integer yearsEmployed;

    @Min(300)
    @Max(850)
    private Integer creditScore;

    @Size(max = 4000)
    private String applicantStatement;
}
