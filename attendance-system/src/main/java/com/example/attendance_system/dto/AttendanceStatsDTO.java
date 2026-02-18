package com.example.attendance_system.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object for attendance statistics.
 */
public class AttendanceStatsDTO {

    private long totalDays;
    private long presentDays;
    private long absentDays;
    private double attendancePercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private long lateArrivals;
    private long earlyDepartures;

    // Constructors
    public AttendanceStatsDTO() {}

    public AttendanceStatsDTO(long totalDays, long presentDays, long absentDays, 
                              double attendancePercentage, LocalDate startDate, LocalDate endDate) {
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.attendancePercentage = Math.round(attendancePercentage * 100.0) / 100.0; // Round to 2 decimal places
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Builder pattern for fluent API
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalDays;
        private long presentDays;
        private long absentDays;
        private double attendancePercentage;
        private LocalDate startDate;
        private LocalDate endDate;
        private long lateArrivals;
        private long earlyDepartures;

        public Builder totalDays(long totalDays) {
            this.totalDays = totalDays;
            return this;
        }

        public Builder presentDays(long presentDays) {
            this.presentDays = presentDays;
            return this;
        }

        public Builder absentDays(long absentDays) {
            this.absentDays = absentDays;
            return this;
        }

        public Builder attendancePercentage(double percentage) {
            this.attendancePercentage = percentage;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder lateArrivals(long lateArrivals) {
            this.lateArrivals = lateArrivals;
            return this;
        }

        public Builder earlyDepartures(long earlyDepartures) {
            this.earlyDepartures = earlyDepartures;
            return this;
        }

        public AttendanceStatsDTO build() {
            AttendanceStatsDTO dto = new AttendanceStatsDTO();
            dto.totalDays = this.totalDays;
            dto.presentDays = this.presentDays;
            dto.absentDays = this.absentDays;
            dto.attendancePercentage = Math.round(this.attendancePercentage * 100.0) / 100.0;
            dto.startDate = this.startDate;
            dto.endDate = this.endDate;
            dto.lateArrivals = this.lateArrivals;
            dto.earlyDepartures = this.earlyDepartures;
            return dto;
        }
    }

    // Getters and Setters
    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }

    public long getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(long presentDays) {
        this.presentDays = presentDays;
    }

    public long getAbsentDays() {
        return absentDays;
    }

    public void setAbsentDays(long absentDays) {
        this.absentDays = absentDays;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
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

    public long getLateArrivals() {
        return lateArrivals;
    }

    public void setLateArrivals(long lateArrivals) {
        this.lateArrivals = lateArrivals;
    }

    public long getEarlyDepartures() {
        return earlyDepartures;
    }

    public void setEarlyDepartures(long earlyDepartures) {
        this.earlyDepartures = earlyDepartures;
    }

    @Override
    public String toString() {
        return "AttendanceStatsDTO{" +
                "totalDays=" + totalDays +
                ", presentDays=" + presentDays +
                ", absentDays=" + absentDays +
                ", attendancePercentage=" + attendancePercentage +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
