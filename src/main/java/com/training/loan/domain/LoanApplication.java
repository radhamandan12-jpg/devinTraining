package com.training.loan.domain;

import com.training.loan.domain.enums.ApplicationStatus;
import com.training.loan.domain.enums.EmploymentStatus;
import com.training.loan.domain.enums.LoanPurpose;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deliberately models only financial and employment attributes.
 * Protected characteristics (race, religion, gender, disability, marital
 * status, etc.) are intentionally not represented anywhere in this model.
 */
@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private AppUser applicant;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer termMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanPurpose loanPurpose;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal annualIncome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyDebtPayments;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmploymentStatus employmentStatus;

    @Column
    private Integer yearsEmployed;

    @Column
    private Integer creditScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String applicantStatement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiMissingInfo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String aiRiskNotes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String decisionExplanation;

    @Column
    private String decidedBy;

    @Column
    private LocalDateTime decidedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String decisionNotes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Document> documents = new LinkedHashSet<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RuleEvaluation> ruleEvaluations = new LinkedHashSet<>();

    public BigDecimal debtToIncomeRatio() {
        if (annualIncome == null || annualIncome.signum() <= 0 || monthlyDebtPayments == null) {
            return null;
        }
        return monthlyDebtPayments
                .multiply(BigDecimal.valueOf(12))
                .divide(annualIncome, 4, java.math.RoundingMode.HALF_UP);
    }
}
