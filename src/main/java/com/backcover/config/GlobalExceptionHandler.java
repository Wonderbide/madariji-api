package com.backcover.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import java.io.IOException;

import com.backcover.exception.WordAnalysisException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        
        // Log only if it's not a 403 (normal behavior)
        if (ex.getStatusCode().value() != 403) {
            log.error("ResponseStatusException: {}", ex.getMessage());
        }
        
        Map<String, Object> response = createErrorResponse(
            ex.getStatusCode().toString(), 
            ex.getReason(), 
            HttpStatus.valueOf(ex.getStatusCode().value()),
            null
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "VALIDATION_ERROR", 
            "Request validation failed", 
            HttpStatus.BAD_REQUEST,
            ex.getBindingResult().getAllErrors()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        log.error("Constraint violation: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "CONSTRAINT_VIOLATION", 
            "Database constraint violation", 
            HttpStatus.CONFLICT,
            ex.getConstraintViolations()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(
            DataAccessException ex, WebRequest request) {
        
        log.error("Database error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = createErrorResponse(
            "DATABASE_ERROR", 
            "Database operation failed", 
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "ACCESS_DENIED", 
            "Insufficient permissions", 
            HttpStatus.FORBIDDEN,
            null
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(
            IOException ex, WebRequest request) {
        
        log.error("I/O error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = createErrorResponse(
            "IO_ERROR", 
            "File or network operation failed", 
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        log.error("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            "INVALID_ARGUMENT", 
            "Invalid argument provided", 
            HttpStatus.BAD_REQUEST,
            null
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(WordAnalysisException.class)
    public ResponseEntity<Map<String, Object>> handleWordAnalysisException(
            WordAnalysisException ex, WebRequest request) {

        WordAnalysisException.ErrorType errorType = ex.getErrorType();

        log.error("Word analysis error [{}]: {}", errorType.name(), ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("error", "WORD_ANALYSIS_ERROR");
        response.put("errorType", errorType.name());
        response.put("message", errorType.getUserMessage());
        response.put("status", errorType.getHttpStatus());
        response.put("retryable", errorType.isRetryable());
        response.put("timestamp", System.currentTimeMillis());
        response.put("severity", errorType.isRetryable() ? "LOW" : "MEDIUM");
        response.put("category", "ANALYSIS");

        return ResponseEntity.status(errorType.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> response = createErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR,
            null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private Map<String, Object> createErrorResponse(String errorCode, String message, 
                                                   HttpStatus status, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", errorCode);
        response.put("message", message);
        response.put("status", status.value());
        response.put("timestamp", System.currentTimeMillis());
        
        if (details != null) {
            response.put("details", details);
        }
        
        response.put("severity", calculateSeverity(status));
        response.put("category", categorizeByStatus(status));
        
        return response;
    }
    
    private String calculateSeverity(HttpStatus status) {
        if (status.is5xxServerError()) {
            return "HIGH";
        } else if (status == HttpStatus.FORBIDDEN || status == HttpStatus.UNAUTHORIZED) {
            return "LOW"; // Changed from CRITICAL - these are normal
        } else if (status.is4xxClientError()) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    private String categorizeByStatus(HttpStatus status) {
        return switch (status.value()) {
            case 400, 422 -> "VALIDATION";
            case 401, 403 -> "SECURITY";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 500, 502, 503 -> "SYSTEM";
            default -> "GENERAL";
        };
    }
}