package com.example.attendance_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDTO {

    private Long id;
    private Long userId;
    private String username;
    private LocalDate date;
    private boolean present;
    private LocalDateTime timestamp;

    // Constructors
    public AttendanceDTO() {}

    public AttendanceDTO(Long id, Long userId, String username, LocalDate date, boolean present, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.date = date;
        this.present = present;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
