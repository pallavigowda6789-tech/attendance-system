package com.example.attendance_system.dto;

import com.example.attendance_system.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Attendance entity.
 */
public class AttendanceDTO {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private LocalDate date;
    private boolean present;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String notes;
    private LocalDateTime timestamp; // For backward compatibility

    // Constructors
    public AttendanceDTO() {}

    public AttendanceDTO(Long id, Long userId, String username, LocalDate date, 
                         boolean present, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.date = date;
        this.present = present;
        this.timestamp = timestamp;
        this.checkInTime = timestamp;
    }

    // Static factory method from entity
    public static AttendanceDTO fromEntity(Attendance attendance) {
        if (attendance == null) return null;
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setUserId(attendance.getUser().getId());
        dto.setUsername(attendance.getUser().getUsername());
        dto.setFullName(attendance.getUser().getFullName());
        dto.setDate(attendance.getDate());
        dto.setPresent(attendance.isPresent());
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setNotes(attendance.getNotes());
        dto.setTimestamp(attendance.getCheckInTime());
        return dto;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AttendanceDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", date=" + date +
                ", present=" + present +
                '}';
    }
}
