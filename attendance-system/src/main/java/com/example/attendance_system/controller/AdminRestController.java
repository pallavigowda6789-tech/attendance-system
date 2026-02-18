package com.example.attendance_system.controller;

import com.example.attendance_system.dto.*;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.LeaveService;
import com.example.attendance_system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    private final UserService userService;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;

    public AdminRestController(UserService userService, AttendanceService attendanceService, LeaveService leaveService) {
        this.userService = userService;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
    }

    // ========== User Management ==========

    /**
     * Get all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Create a new user (admin).
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody RegisterDTO registerDTO) {
        logger.info("Admin creating new user: {}", registerDTO.getUsername());
        UserDTO user = userService.registerUser(registerDTO);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", user));
    }

    /**
     * Update user details (admin).
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        logger.info("Admin updating user {}: {}", id, updates.keySet());
        UserDTO user = userService.adminUpdateUser(id, updates);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
    }

    /**
     * Delete user by ID.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        logger.info("Admin deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Update user role.
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String roleStr = request.get("role");
        Role role = Role.valueOf(roleStr.toUpperCase());
        logger.info("Admin updating user {} role to {}", id, role);
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully"));
    }

    /**
     * Toggle user enabled status.
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        logger.info("Admin toggling user {} status", id);
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
    }

    /**
     * Reset user password (admin).
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        logger.info("Admin resetting password for user {}", id);
        userService.adminResetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    // ========== Attendance Management ==========

    /**
     * Get all attendance records (paginated).
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.info("Admin getting attendance - page: {}, userId: {}, dates: {} - {}", 
                    page, userId, startDate, endDate);
        
        PagedResponse<AttendanceDTO> records;
        if (userId != null && startDate != null && endDate != null) {
            List<AttendanceDTO> filtered = attendanceService.getAttendanceByDateRange(userId, startDate, endDate);
            // Manual pagination for filtered results
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<AttendanceDTO> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();
            records = PagedResponse.of(pageContent, page, size, filtered.size());
        } else if (userId != null) {
            List<AttendanceDTO> userRecords = attendanceService.getAttendanceByUser(userId);
            int start = page * size;
            int end = Math.min(start + size, userRecords.size());
            List<AttendanceDTO> pageContent = start < userRecords.size() ? userRecords.subList(start, end) : List.of();
            records = PagedResponse.of(pageContent, page, size, userRecords.size());
        } else {
            records = attendanceService.getAllAttendancePaginated(page, size);
        }
        
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Get attendance for a specific user.
     */
    @GetMapping("/attendance/user/{userId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getUserAttendance(@PathVariable Long userId) {
        List<AttendanceDTO> records = attendanceService.getAttendanceByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Get attendance statistics for a user.
     */
    @GetMapping("/attendance/user/{userId}/stats")
    public ResponseEntity<ApiResponse<AttendanceStatsDTO>> getUserAttendanceStats(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        AttendanceStatsDTO stats;
        if (startDate != null && endDate != null) {
            stats = attendanceService.getAttendanceStats(userId, startDate, endDate);
        } else {
            stats = attendanceService.getAttendanceStats(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Delete attendance record.
     */
    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        logger.info("Admin deleting attendance record: {}", id);
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record deleted successfully"));
    }

    /**
     * Mark attendance for a user (admin).
     */
    @PostMapping("/attendance/mark")
    public ResponseEntity<ApiResponse<AttendanceDTO>> markAttendanceForUser(
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        boolean present = request.get("present") != null ? Boolean.parseBoolean(request.get("present").toString()) : true;
        LocalDate date = request.get("date") != null ? LocalDate.parse(request.get("date").toString()) : LocalDate.now();
        
        logger.info("Admin marking attendance for user {} on {}: {}", userId, date, present);
        AttendanceDTO attendance = attendanceService.markAttendanceByUserId(userId, date, present);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", attendance));
    }

    // ========== Statistics ==========

    /**
     * Get system statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        List<UserDTO> users = userService.getAllUsers();
        long totalUsers = users.size();
        long activeUsers = users.stream().filter(UserDTO::isEnabled).count();
        long adminCount = users.stream().filter(u -> Role.ADMIN.equals(u.getRole())).count();
        long managerCount = users.stream().filter(u -> Role.MANAGER.equals(u.getRole())).count();
        long pendingLeaves = leaveService.getPendingLeavesCount();

        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "adminCount", adminCount,
                "managerCount", managerCount,
                "userCount", totalUsers - adminCount - managerCount,
                "pendingLeaves", pendingLeaves
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get attendance summary for all users.
     */
    @GetMapping("/attendance/summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttendanceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<UserDTO> users = userService.getAllUsers();
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        
        List<Map<String, Object>> summary = users.stream().map(user -> {
            AttendanceStatsDTO stats = attendanceService.getAttendanceStats(user.getId(), start, end);
            return Map.<String, Object>of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "fullName", user.getFullName(),
                    "totalDays", stats.getTotalDays(),
                    "presentDays", stats.getPresentDays(),
                    "absentDays", stats.getAbsentDays(),
                    "attendancePercentage", stats.getAttendancePercentage()
            );
        }).toList();
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
