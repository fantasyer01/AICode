package com.example.aialibaba.exception;

import com.example.aialibaba.model.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.stream.Collectors;

/**
 * Global exception handler for centralized error handling
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ChatResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
                
        logger.warn("Validation error: {}", errorMessage);
        
        ChatResponseDTO response = ChatResponseDTO.error("VALIDATION_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle binding exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ChatResponseDTO> handleBindException(BindException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
                
        logger.warn("Binding error: {}", errorMessage);
        
        ChatResponseDTO response = ChatResponseDTO.error("BINDING_ERROR", errorMessage);
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle service exceptions
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ChatResponseDTO> handleServiceException(ServiceException ex) {
        logger.error("Service exception occurred - Code: {}, Message: {}", 
                    ex.getErrorCode(), ex.getMessage(), ex);
        
        ChatResponseDTO response = ChatResponseDTO.error(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handle HTTP client errors (4xx)
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ChatResponseDTO> handleHttpClientError(HttpClientErrorException ex) {
        logger.error("HTTP client error: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        
        String errorCode = "HTTP_CLIENT_ERROR_" + ex.getStatusCode().value();
        ChatResponseDTO response = ChatResponseDTO.error(errorCode, 
                "External service client error: " + ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    /**
     * Handle HTTP server errors (5xx)
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ChatResponseDTO> handleHttpServerError(HttpServerErrorException ex) {
        logger.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        
        String errorCode = "HTTP_SERVER_ERROR_" + ex.getStatusCode().value();
        ChatResponseDTO response = ChatResponseDTO.error(errorCode, 
                "External service server error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ChatResponseDTO> handleGeneralException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        ChatResponseDTO response = ChatResponseDTO.error("INTERNAL_ERROR", 
                "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}