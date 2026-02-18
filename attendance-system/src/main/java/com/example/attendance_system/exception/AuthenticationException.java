package com.example.attendance_system.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authentication and authorization errors.
 */
public class AuthenticationException extends AttendanceSystemException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, 4001);
    }

    public AuthenticationException(String message, Integer errorCode) {
        super(message, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
