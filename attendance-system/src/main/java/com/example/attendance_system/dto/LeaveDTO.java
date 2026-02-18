package com.example.attendance_system.dto;

import com.example.attendance_system.entity.Leave;
import com.example.attendance_system.entity.LeaveStatus;
import com.example.attendance_system.entity.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Leave entity.
 */
public class LeaveDTO {

    private Long id;
    private Long userId;
    private String username;
    private String userFullName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private long days;
    private String reason;
    private LeaveStatus status;
    private Long approvedById;
    private String approvedByName;
    private String approvalComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LeaveDTO() {}

    // Static factory method from entity
    public static LeaveDTO fromEntity(Leave leave) {
        if (leave == null) return null;
        LeaveDTO dto = new LeaveDTO();
        dto.setId(leave.getId());
        dto.setUserId(leave.getUser().getId());
        dto.setUsername(leave.getUser().getUsername());
        dto.setUserFullName(leave.getUser().getFullName());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setDays(leave.getDays());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        if (leave.getApprovedBy() != null) {
            dto.setApprovedById(leave.getApprovedBy().getId());
            dto.setApprovedByName(leave.getApprovedBy().getFullName());
        }
        dto.setApprovalComment(leave.getApprovalComment());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());
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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
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

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public Long getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(Long approvedById) {
        this.approvedById = approvedById;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
