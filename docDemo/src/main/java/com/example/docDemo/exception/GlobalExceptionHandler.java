package com.example.docDemo.exception;

import com.example.docDemo.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Keep DocumentNotFoundException handler
    @ExceptionHandler(CustomExceptions.DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDocumentNotFound(CustomExceptions.DocumentNotFoundException ex, HttpServletRequest request) {
        log.warn("DocumentNotFoundException: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Keep/Update UnsupportedDocumentTypeException handler
    @ExceptionHandler(CustomExceptions.UnsupportedDocumentTypeException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedDocumentType(CustomExceptions.UnsupportedDocumentTypeException ex, HttpServletRequest request) {
        log.warn("UnsupportedDocumentTypeException: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "Unsupported Media Type", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // Keep/Add TextExtractionException handler
    @ExceptionHandler(CustomExceptions.TextExtractionException.class)
    public ResponseEntity<ErrorResponseDTO> handleTextExtraction(CustomExceptions.TextExtractionException ex, HttpServletRequest request) {
        log.error("TextExtractionException: {}", ex.getMessage(), ex.getCause());
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Document Processing Error", ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Keep handler for IOException (might occur during stream processing)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponseDTO> handleIOException(IOException ex, HttpServletRequest request) {
        log.error("IOException occurred: {}", ex.getMessage(), ex);
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "Error reading or processing file stream.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // Keep other handlers (ResponseStatusException, Validation, Max Upload Size, Generic)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        log.warn("ResponseStatusException: Status {}, Reason {}", ex.getStatusCode(), ex.getReason());
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), status.value(), status.getReasonPhrase(), ex.getReason(), request.getRequestURI());
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.BAD_REQUEST.value(), "Validation Failed", errors, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size limit exceeded: {}", ex.getMessage());
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.PAYLOAD_TOO_LARGE.value(), "Payload Too Large", "Maximum upload size exceeded.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        ErrorResponseDTO error = new ErrorResponseDTO(LocalDate.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred.", request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
