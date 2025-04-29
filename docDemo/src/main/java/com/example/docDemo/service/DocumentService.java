package com.example.docDemo.service;

import com.example.docDemo.dto.DocumentMetadataDTO;
import com.example.docDemo.dto.QAResponseDTO;
import com.example.docDemo.entity.Document;
import com.example.docDemo.exception.CustomExceptions;
import com.example.docDemo.repository.DocumentRepository;
import com.example.docDemo.repository.DocumentSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int SNIPPET_LENGTH = 150;

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private TextExtractionService textExtractionService;


    @Async
    @Transactional
    public CompletableFuture<DocumentMetadataDTO> ingestDocument(MultipartFile file, String author) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file";
        String contentType = file.getContentType();

        log.info("Starting async ingestion for file: {}, Content-Type: {}", filename, contentType);

        try (InputStream inputStream = file.getInputStream()) {
            String content = textExtractionService.extractText(inputStream, filename, contentType);
            if (content == null || content.isBlank()) {
                log.warn("Extracted content is empty for file: {}. Saving document with empty content.", filename);
                content = "";
            }

            Document document = new Document();
            document.setFilename(filename);
            document.setContentType(contentType);
            document.setAuthor(author);
            document.setContent(content);

            Document savedDocument = documentRepository.save(document);
            log.info("Successfully ingested and saved document ID: {}, Filename: {}", savedDocument.getId(), filename);

            return CompletableFuture.completedFuture(mapToMetadataDTO(savedDocument));

        } catch (CustomExceptions.UnsupportedDocumentTypeException | CustomExceptions.TextExtractionException | IOException e) {
            log.error("Ingestion failed for file {}: {}", filename, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Unexpected error during ingestion of file {}: {}", filename, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional(readOnly = true)
    public Page<QAResponseDTO> searchDocumentsByKeyword(String keyword, Pageable pageable) {
        log.debug("Searching for keyword '{}' with pagination: {}", keyword, pageable);
        Page<Document> results = documentRepository.searchByKeyword(keyword, pageable);
        return results.map(doc -> mapToQAResponseDTO(doc, keyword));
    }

    @Transactional(readOnly = true)
    public Page<DocumentMetadataDTO> findDocuments(String author, String contentType, LocalDate uploadTimestamp, Pageable pageable) {
        log.debug("Finding documents with filters - Author: {}, Type: {},uploadTimestamp: {}, Page: {}",
                author, contentType, uploadTimestamp, pageable);
        Specification<Document> spec = Specification.where(DocumentSpecification.hasAuthor(author))
                .and(DocumentSpecification.hasContentType(contentType))
                .and(DocumentSpecification.uploadDate(uploadTimestamp));
        Page<Document> results = documentRepository.findAll(spec, pageable);
        return results.map(this::mapToMetadataDTO);
    }

    // Helper methods
    private DocumentMetadataDTO mapToMetadataDTO(Document doc) {
        return new DocumentMetadataDTO(doc.getId(), doc.getFilename(), doc.getContentType(), doc.getAuthor(), doc.getUploadTimestamp());
    }
    private QAResponseDTO mapToQAResponseDTO(Document doc, String keyword) {
        String snippet = generateSnippet(doc.getContent(), keyword);
        return new QAResponseDTO(doc.getId(), doc.getFilename(), snippet, doc.getAuthor(), doc.getUploadTimestamp());
    }
    private String generateSnippet(String content, String keyword) {

        if (content == null || keyword == null || keyword.isEmpty()) return "";
        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int index = lowerContent.indexOf(lowerKeyword);
        if (index == -1) return content.substring(0, Math.min(content.length(), SNIPPET_LENGTH)) + (content.length() > SNIPPET_LENGTH ? "..." : "");
        int start = Math.max(0, index - (SNIPPET_LENGTH / 2));
        int end = Math.min(content.length(), index + keyword.length() + (SNIPPET_LENGTH / 2));
        String snippet = content.substring(start, end);
        if (start > 0) snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        return snippet;
    }
}
