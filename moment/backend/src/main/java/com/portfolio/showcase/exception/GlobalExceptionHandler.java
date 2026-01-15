package com.portfolio.showcase.exception;

import com.portfolio.showcase.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Global exception handler for consistent error responses across all API endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles IllegalArgumentException (400).
     * Typically thrown for invalid parameter values like invalid category.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.info("Invalid argument: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles MethodArgumentNotValidException (400).
     * Thrown when @Valid validation fails on request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.info("Validation failed: {} - Path: {}", message, request.getRequestURI());
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles ConstraintViolationException (400).
     * Thrown when @Valid validation fails on path variables or query parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        log.info("Constraint violation: {} - Path: {}", message, request.getRequestURI());
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles MethodArgumentTypeMismatchException (400).
     * Thrown when path variable or query parameter type conversion fails.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        log.info("Type mismatch: {} - Path: {}", message, request.getRequestURI());
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other unexpected exceptions (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred - Path: {}", request.getRequestURI(), ex);
        
        ErrorResponseDto error = ErrorResponseDto.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
