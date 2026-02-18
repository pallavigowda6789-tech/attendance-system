package com.example.attendance_system.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a duplicate resource already exists.
 */
public class DuplicateResourceException extends AttendanceSystemException {

    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, 4009);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue), 
              HttpStatus.CONFLICT, 4009);
    }
}
