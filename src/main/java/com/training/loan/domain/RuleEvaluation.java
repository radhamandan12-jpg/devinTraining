package com.training.loan.domain;

import com.training.loan.domain.enums.RuleSeverity;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rule_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class RuleEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Column(nullable = false, length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RuleSeverity severity;

    @Column(nullable = false)
    private boolean passed;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public RuleEvaluation(LoanApplication application, String ruleName, RuleSeverity severity,
                          boolean passed, String detail) {
        this.application = application;
        this.ruleName = ruleName;
        this.severity = severity;
        this.passed = passed;
        this.detail = detail;
    }
}
