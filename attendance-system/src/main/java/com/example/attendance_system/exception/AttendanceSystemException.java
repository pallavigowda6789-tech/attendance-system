package com.example.attendance_system.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for the attendance system.
 */
public class AttendanceSystemException extends RuntimeException {

    private final HttpStatus status;
    private final Integer errorCode;

    public AttendanceSystemException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = 5000;
    }

    public AttendanceSystemException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = status.value();
    }

    public AttendanceSystemException(String message, HttpStatus status, Integer errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AttendanceSystemException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = 5000;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
