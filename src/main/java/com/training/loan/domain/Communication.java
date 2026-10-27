package com.training.loan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * AI-drafted customer communication. Always a draft created after the
 * deterministic decision is made; never carries a decision of its own.
 */
@Entity
@Table(name = "communications")
@Getter
@Setter
@NoArgsConstructor
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Column(nullable = false, length = 50)
    private String channel;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String subject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private LocalDateTime draftedAt = LocalDateTime.now();

    public Communication(LoanApplication application, String channel, String subject, String body) {
        this.application = application;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
    }
}
