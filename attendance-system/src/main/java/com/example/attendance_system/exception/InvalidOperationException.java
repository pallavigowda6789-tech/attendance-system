package com.example.attendance_system.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for invalid operations or business rule violations.
 */
public class InvalidOperationException extends AttendanceSystemException {

    public InvalidOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, 4000);
    }

    public InvalidOperationException(String message, Integer errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}
