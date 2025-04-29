package com.example.docDemo.service;

import com.example.docDemo.exception.CustomExceptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TextExtractionService {

    private static final Logger log = LoggerFactory.getLogger(TextExtractionService.class);

    private static final String PDF_TYPE = "application/pdf";
    private static final String DOCX_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String TXT_TYPE = "text/plain";

    public String extractText(InputStream inputStream, String filename, String contentType)
            throws IOException, CustomExceptions.TextExtractionException, CustomExceptions.UnsupportedDocumentTypeException {

        log.debug("Attempting text extraction for file: {}, Content-Type: {}", filename, contentType);

        if (contentType == null || contentType.isBlank()) {
            log.warn("Missing content type for file: {}. Cannot determine extraction method.", filename);
            throw new CustomExceptions.UnsupportedDocumentTypeException(filename, "Unknown/Missing", List.of(PDF_TYPE, DOCX_TYPE, TXT_TYPE));
        }
        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase();

        try {
            switch (normalizedContentType) {
                case PDF_TYPE:
                    log.info("Extracting text from PDF: {}", filename);
                    return extractTextFromPdf(inputStream);
                case DOCX_TYPE:
                    log.info("Extracting text from DOCX: {}", filename);
                    return extractTextFromDocx(inputStream);
                case TXT_TYPE:
                    log.info("Reading text from TXT: {}", filename);
                    return extractTextFromTxt(inputStream);
                default:
                    log.warn("Unsupported content type '{}' for file: {}", contentType, filename);
                    throw new CustomExceptions.UnsupportedDocumentTypeException(filename, contentType, List.of(PDF_TYPE, DOCX_TYPE, TXT_TYPE));
            }
        } catch (IOException ioe) {
            log.error("IO error during text extraction for file {}: {}", filename, ioe.getMessage());
            throw ioe;
        } catch (CustomExceptions.UnsupportedDocumentTypeException e){
            throw e;
        } catch (Exception e) {
            log.error("Extraction failed for file {}: {}", filename, e.getMessage(), e);
            throw new CustomExceptions.TextExtractionException("Failed to extract text from " + filename + " (Type: " + contentType + ")", filename, e);
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            if (document.isEncrypted()) {
                log.warn("PDF document is encrypted. Text extraction might fail or be incomplete.");
            }
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
        // Catch specific PDFBox exceptions if needed for finer control
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        }
        // Catch specific POI exceptions like InvalidFormatException if needed
    }

    private String extractTextFromTxt(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}