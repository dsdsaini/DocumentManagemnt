package com.example.docDemo.controller;

import com.example.docDemo.dto.DocumentMetadataDTO;
import com.example.docDemo.dto.QAResponseDTO;
import com.example.docDemo.exception.CustomExceptions;
import com.example.docDemo.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Management", description = "APIs for document ingestion (PDF, DOCX, TXT), searching, and filtering")
@Validated
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Operation(summary = "Ingest a new document (PDF, DOCX, TXT)", description = "Uploads a document file (PDF, DOCX, or TXT). Extracts text content and stores it asynchronously. Other types are rejected.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentMetadataDTO> ingestDocument(
            @Parameter(description = "Document file to upload (PDF, DOCX, TXT)", required = true) @RequestPart("file") MultipartFile file,
            @Parameter(description = "Author of the document") @RequestParam(value = "author", required = false) String author) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required for ingestion/blank file.");
        }
        log.info("Received ingestion request for file: {}, Author: {}, Content-Type: {}", file.getOriginalFilename(), author, file.getContentType());

        try {
            DocumentMetadataDTO result = documentService.ingestDocument(file, author).get(); // Wait for async completion
            log.info("Async ingestion completed for file: {}. Returning result.", file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Async ingestion failed for file {}: {}", file.getOriginalFilename(), cause != null ? cause.getMessage() : e.getMessage(), cause);

            // Handle specific exceptions from the async task cause
            if (cause instanceof CustomExceptions.UnsupportedDocumentTypeException udte) {
                throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, udte.getMessage(), udte);
            } else if (cause instanceof CustomExceptions.TextExtractionException tee) {
                // Treat extraction failures as internal errors generally
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process document content: " + tee.getMessage(), tee);
            } else if (cause instanceof IOException ioe) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read document file: " + ioe.getMessage(), ioe);
            } else if (cause instanceof ResponseStatusException rse) {
                throw rse; // Re-throw if already a web exception
            } else {
                // General fallback for other execution errors
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ingestion failed: " + (cause != null ? cause.getMessage() : e.getMessage()), e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Ingestion interrupted for file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ingestion process was interrupted.", e);
        }
    }

    @Operation(summary = "Search documents by keyword (Q&A)", description = "Retrieves documents containing the specified keyword in their content. Uses basic case-insensitive matching (LIKE).")
    @GetMapping("/search")
    public ResponseEntity<Page<QAResponseDTO>> searchDocuments(
            @Parameter(description = "Keyword to search", required = true) @RequestParam @NotBlank @Size(min = 1) String query,
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 10, sort = "uploadTimestamp") Pageable pageable) {

        log.debug("Received search request for query: '{}', Pageable: {}", query, pageable);
        Page<QAResponseDTO> results = documentService.searchDocumentsByKeyword(query, pageable);
        return ResponseEntity.ok(results);
    }


    @Operation(summary = "Filter and list documents", description = "Retrieves a paginated list of documents based on optional metadata filters.")
    @GetMapping
    public ResponseEntity<Page<DocumentMetadataDTO>> findDocuments(
            @Parameter(description = "Filter by author") @RequestParam(required = false) String author,
            @Parameter(description = "Filter by content type") @RequestParam(required = false) String contentType,
            @Parameter(description = "Filter by upload date")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadTimestamp,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20, sort = "uploadTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Received find request with filters - Author: {}, Type: {}, uploadTimestamp: {}, Pageable: {}", author, contentType, uploadTimestamp, pageable);
        Page<DocumentMetadataDTO> results = documentService.findDocuments(author, contentType, LocalDate.from(uploadTimestamp.atStartOfDay()), pageable);
        return ResponseEntity.ok(results);
    }
}
