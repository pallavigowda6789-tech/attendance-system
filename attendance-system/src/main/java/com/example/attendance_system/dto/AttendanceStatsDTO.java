package com.example.attendance_system.dto;

import java.time.LocalDate;

public class AttendanceStatsDTO {

    private Long totalDays;
    private Long presentDays;
    private Long absentDays;
    private Double attendancePercentage;
    private LocalDate startDate;
    private LocalDate endDate;

    // Constructors
    public AttendanceStatsDTO() {}

    public AttendanceStatsDTO(Long totalDays, Long presentDays, Long absentDays, Double attendancePercentage, LocalDate startDate, LocalDate endDate) {
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.attendancePercentage = attendancePercentage;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public Long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Long totalDays) {
        this.totalDays = totalDays;
    }

    public Long getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(Long presentDays) {
        this.presentDays = presentDays;
    }

    public Long getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(Long absentDays) {
        this.absentDays = absentDays;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
