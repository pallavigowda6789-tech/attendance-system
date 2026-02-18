package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.LeaveDTO;
import com.example.attendance_system.dto.LeaveRequestDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.entity.LeaveType;
import com.example.attendance_system.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST controller for leave management.
 */
@RestController
@RequestMapping("/api/leaves")
public class LeaveRestController {

    private final LeaveService leaveService;

    public LeaveRestController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    /**
     * Get all leave types.
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<LeaveType>>> getLeaveTypes() {
        return ResponseEntity.ok(ApiResponse.success(Arrays.asList(LeaveType.values())));
    }

    /**
     * Request a new leave.
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<LeaveDTO>> requestLeave(@Valid @RequestBody LeaveRequestDTO request) {
        LeaveDTO leave = leaveService.requestLeave(request);
        return ResponseEntity.ok(ApiResponse.success("Leave request submitted successfully", leave));
    }

    /**
     * Get current user's leaves.
     */
    @GetMapping("/my-leaves")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveDTO>>> getMyLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<LeaveDTO> leaves = leaveService.getCurrentUserLeavesPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * Get current user's leave statistics.
     */
    @GetMapping("/my-stats")
    public ResponseEntity<ApiResponse<LeaveService.LeaveStatsDTO>> getMyLeaveStats() {
        LeaveService.LeaveStatsDTO stats = leaveService.getCurrentUserLeaveStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Cancel a leave request.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveDTO>> cancelLeave(@PathVariable Long id) {
        LeaveDTO leave = leaveService.cancelLeave(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled", leave));
    }

    /**
     * Get leave by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveDTO>> getLeaveById(@PathVariable Long id) {
        LeaveDTO leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.success(leave));
    }

    // ========== Admin Endpoints ==========

    /**
     * Get all leaves (admin).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveDTO>>> getAllLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<LeaveDTO> leaves = leaveService.getAllLeavesPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * Get pending leaves (admin).
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveDTO>>> getPendingLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<LeaveDTO> leaves = leaveService.getPendingLeavesPaginated(page, size);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    /**
     * Get pending leaves count (admin).
     */
    @GetMapping("/admin/pending-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPendingLeavesCount() {
        long count = leaveService.getPendingLeavesCount();
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * Approve a leave request (admin).
     */
    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeaveDTO>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String comment = request != null ? request.get("comment") : null;
        LeaveDTO leave = leaveService.approveLeave(id, comment);
        return ResponseEntity.ok(ApiResponse.success("Leave approved", leave));
    }

    /**
     * Reject a leave request (admin).
     */
    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeaveDTO>> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String comment = request != null ? request.get("comment") : null;
        LeaveDTO leave = leaveService.rejectLeave(id, comment);
        return ResponseEntity.ok(ApiResponse.success("Leave rejected", leave));
    }
}
