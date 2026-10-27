package com.training.loan.service;

import com.training.loan.ai.AiAssistantService;
import com.training.loan.domain.Document;
import com.training.loan.domain.LoanApplication;
import com.training.loan.repository.DocumentRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AiAssistantService aiService;
    private final AuditService auditService;

    public DocumentService(DocumentRepository documentRepository, AiAssistantService aiService,
                           AuditService auditService) {
        this.documentRepository = documentRepository;
        this.aiService = aiService;
        this.auditService = auditService;
    }

    @Transactional
    public Document attach(LoanApplication application, MultipartFile file) throws IOException {
        Document doc = new Document(application, file.getOriginalFilename(),
                file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
                file.getBytes());

        String text = extractText(doc);
        if (!text.isBlank()) {
            doc.setExtractedData(aiService.extractDocumentData(doc.getFilename(), text));
        }
        Document saved = documentRepository.save(doc);
        auditService.record(application.getId(), application.getApplicant().getUsername(),
                "DOCUMENT_UPLOADED", "file=" + doc.getFilename());
        return saved;
    }

    private String extractText(Document doc) {
        if (doc.getContent() == null || doc.getContentType() == null) {
            return "";
        }
        if (doc.getContentType().startsWith("text/") || doc.getContentType().contains("json")) {
            return new String(doc.getContent(), StandardCharsets.UTF_8);
        }
        return "";
    }
}
