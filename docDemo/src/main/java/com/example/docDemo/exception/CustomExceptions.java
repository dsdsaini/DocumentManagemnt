package com.example.docDemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

public class CustomExceptions {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class DocumentNotFoundException extends RuntimeException {
        public DocumentNotFoundException(Long id) {
            super("Could not find document with ID: " + id);
        }
    }

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public static class UnsupportedDocumentTypeException extends RuntimeException {
        public UnsupportedDocumentTypeException(String filename, String actualType, List<String> expectedTypes) {
            super(String.format("File '%s' has unsupported content type '%s'. Supported types are: %s",
                    filename, actualType, String.join(", ", expectedTypes)));
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class TextExtractionException extends RuntimeException {
        public TextExtractionException(String message, String filename) {
            super("Extraction error for file '" + filename + "': " + message);
        }
        public TextExtractionException(String message, String filename, Throwable cause) {
            super("Extraction error for file '" + filename + "': " + message, cause);
        }
    }
}
