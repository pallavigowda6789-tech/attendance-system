package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;
    private final AttendanceService attendanceService;

    public AdminRestController(UserService userService, AttendanceService attendanceService) {
        this.userService = userService;
        this.attendanceService = attendanceService;
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
     * Delete user by ID.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
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
        userService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully"));
    }

    /**
     * Toggle user enabled status.
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
    }

    // ========== Attendance Management ==========

    /**
     * Get all attendance records (paginated).
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AttendanceDTO> records = attendanceService.getAllAttendancePaginated(page, size);
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
     * Delete attendance record.
     */
    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance record deleted successfully"));
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

        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "adminCount", adminCount,
                "userCount", totalUsers - adminCount
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
