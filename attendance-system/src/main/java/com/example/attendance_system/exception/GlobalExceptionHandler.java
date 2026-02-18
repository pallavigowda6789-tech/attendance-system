package com.example.attendance_system.exception;

import com.example.attendance_system.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST API endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AttendanceSystemException.class)
    public ResponseEntity<ApiResponse<Object>> handleAttendanceSystemException(
            AttendanceSystemException ex, WebRequest request) {
        logger.error("AttendanceSystemException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.error("ResourceNotFoundException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        logger.error("DuplicateResourceException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        logger.error("AuthenticationException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOperationException(
            InvalidOperationException ex, WebRequest request) {
        logger.error("InvalidOperationException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.error("Validation errors: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", 4000);
        response.setData(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (existing, replacement) -> existing
                ));
        logger.error("Constraint violations: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.error("Validation failed", 4000);
        response.setData(errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        logger.error("AccessDeniedException: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Access denied", 4003);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(
            NoResourceFoundException ex, WebRequest request) {
        // Silently ignore favicon.ico requests
        if (ex.getResourcePath().contains("favicon.ico")) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.warn("Resource not found: {}", ex.getResourcePath());
        ApiResponse<Object> response = ApiResponse.error("Resource not found: " + ex.getResourcePath(), 4004);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected error: ", ex);
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred", 5000);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
