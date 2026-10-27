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

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LoanApplication application;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] content;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedData;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public Document(LoanApplication application, String filename, String contentType, byte[] content) {
        this.application = application;
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
    }
}
