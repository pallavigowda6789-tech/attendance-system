package com.example.attendance_system.service;

import com.example.attendance_system.dto.LeaveDTO;
import com.example.attendance_system.dto.LeaveRequestDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.entity.*;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.repository.LeaveRepository;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for leave management.
 */
@Service
@Transactional
public class LeaveService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveService.class);

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public LeaveService(LeaveRepository leaveRepository, UserRepository userRepository, UserService userService) {
        this.leaveRepository = leaveRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Request a new leave.
     */
    public LeaveDTO requestLeave(LeaveRequestDTO request) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        // Validate date range
        if (!request.isValidDateRange()) {
            throw new InvalidOperationException("End date must be on or after start date");
        }

        // Check for overlapping leaves
        if (leaveRepository.hasOverlappingLeave(currentUser, request.getStartDate(), request.getEndDate())) {
            throw new InvalidOperationException("You already have a leave request for these dates");
        }

        Leave leave = new Leave(
                currentUser,
                request.getLeaveType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getReason()
        );

        Leave saved = leaveRepository.save(leave);
        logger.info("Leave requested by {} from {} to {}", currentUser.getUsername(), 
                    request.getStartDate(), request.getEndDate());
        return LeaveDTO.fromEntity(saved);
    }

    /**
     * Get current user's leaves.
     */
    @Transactional(readOnly = true)
    public List<LeaveDTO> getCurrentUserLeaves() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        return leaveRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(LeaveDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get current user's leaves (paginated).
     */
    @Transactional(readOnly = true)
    public PagedResponse<LeaveDTO> getCurrentUserLeavesPaginated(int page, int size) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Leave> pageResult = leaveRepository.findByUserOrderByCreatedAtDesc(currentUser, pageable);
        
        List<LeaveDTO> content = pageResult.getContent().stream()
                .map(LeaveDTO::fromEntity)
                .collect(Collectors.toList());
        
        return PagedResponse.of(content, page, size, pageResult.getTotalElements());
    }

    /**
     * Cancel a leave request.
     */
    public LeaveDTO cancelLeave(Long leaveId) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        // Can only cancel own leaves
        if (!leave.getUser().getId().equals(currentUser.getId())) {
            throw new InvalidOperationException("You can only cancel your own leave requests");
        }

        // Can only cancel pending leaves
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leaves can be cancelled");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        Leave saved = leaveRepository.save(leave);
        logger.info("Leave {} cancelled by {}", leaveId, currentUser.getUsername());
        return LeaveDTO.fromEntity(saved);
    }

    /**
     * Approve a leave request (admin/manager).
     */
    public LeaveDTO approveLeave(Long leaveId, String comment) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leaves can be approved");
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(currentUser);
        leave.setApprovalComment(comment);

        Leave saved = leaveRepository.save(leave);
        logger.info("Leave {} approved by {}", leaveId, currentUser.getUsername());
        return LeaveDTO.fromEntity(saved);
    }

    /**
     * Reject a leave request (admin/manager).
     */
    public LeaveDTO rejectLeave(Long leaveId, String comment) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidOperationException("Only pending leaves can be rejected");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(currentUser);
        leave.setApprovalComment(comment);

        Leave saved = leaveRepository.save(leave);
        logger.info("Leave {} rejected by {}", leaveId, currentUser.getUsername());
        return LeaveDTO.fromEntity(saved);
    }

    /**
     * Get all leaves (admin).
     */
    @Transactional(readOnly = true)
    public PagedResponse<LeaveDTO> getAllLeavesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Leave> pageResult = leaveRepository.findAllOrderByCreatedAtDesc(pageable);

        List<LeaveDTO> content = pageResult.getContent().stream()
                .map(LeaveDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(content, page, size, pageResult.getTotalElements());
    }

    /**
     * Get pending leaves (admin).
     */
    @Transactional(readOnly = true)
    public PagedResponse<LeaveDTO> getPendingLeavesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Leave> pageResult = leaveRepository.findPendingLeaves(LeaveStatus.PENDING, pageable);

        List<LeaveDTO> content = pageResult.getContent().stream()
                .map(LeaveDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(content, page, size, pageResult.getTotalElements());
    }

    /**
     * Get leave by ID.
     */
    @Transactional(readOnly = true)
    public LeaveDTO getLeaveById(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave", "id", id));
        return LeaveDTO.fromEntity(leave);
    }

    /**
     * Get count of pending leaves.
     */
    @Transactional(readOnly = true)
    public long getPendingLeavesCount() {
        return leaveRepository.countByStatus(LeaveStatus.PENDING);
    }

    /**
     * Get leave statistics for current user.
     */
    @Transactional(readOnly = true)
    public LeaveStatsDTO getCurrentUserLeaveStats() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        int currentYear = LocalDate.now().getYear();
        long totalUsed = leaveRepository.countApprovedLeaveDaysForYear(currentUser, currentYear);
        long pending = leaveRepository.countByUserAndStatus(currentUser, LeaveStatus.PENDING);
        long approved = leaveRepository.countByUserAndStatus(currentUser, LeaveStatus.APPROVED);
        long rejected = leaveRepository.countByUserAndStatus(currentUser, LeaveStatus.REJECTED);

        return new LeaveStatsDTO(totalUsed, pending, approved, rejected);
    }

    /**
     * Inner class for leave statistics.
     */
    public static class LeaveStatsDTO {
        private final long totalDaysUsed;
        private final long pendingRequests;
        private final long approvedRequests;
        private final long rejectedRequests;

        public LeaveStatsDTO(long totalDaysUsed, long pendingRequests, long approvedRequests, long rejectedRequests) {
            this.totalDaysUsed = totalDaysUsed;
            this.pendingRequests = pendingRequests;
            this.approvedRequests = approvedRequests;
            this.rejectedRequests = rejectedRequests;
        }

        public long getTotalDaysUsed() { return totalDaysUsed; }
        public long getPendingRequests() { return pendingRequests; }
        public long getApprovedRequests() { return approvedRequests; }
        public long getRejectedRequests() { return rejectedRequests; }
    }
}
