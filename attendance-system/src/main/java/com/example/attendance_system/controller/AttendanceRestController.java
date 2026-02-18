package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for attendance management endpoints.
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceRestController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceRestController.class);
    
    private final AttendanceService attendanceService;

    public AttendanceRestController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Get attendance statistics for the current user.
     */
    @GetMapping({"/stats", "/my-stats"})
    public ResponseEntity<ApiResponse<AttendanceStatsDTO>> getMyStats() {
        logger.debug("Getting current user stats");
        AttendanceStatsDTO stats = attendanceService.getCurrentUserStats();
        logger.debug("Stats returned: {}", stats);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get attendance records for the current user with pagination.
     */
    @GetMapping("/my-records")
    public ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        logger.info("Getting my records - page: {}, size: {}, startDate: {}, endDate: {}", page, size, startDate, endDate);
        PagedResponse<AttendanceDTO> records = attendanceService.getCurrentUserAttendancePaginated(
                page, size, startDate, endDate);
        logger.info("Returning {} records, total: {}", records.getContent().size(), records.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Mark attendance for the current user.
     */
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<AttendanceDTO>> markAttendance(
            @RequestBody(required = false) Map<String, Object> request) {
        logger.info("Marking attendance with request: {}", request);
        boolean present = request != null ? Boolean.TRUE.equals(request.get("present")) : true;
        AttendanceDTO attendance = attendanceService.markAttendanceForCurrentUser(present);
        logger.info("Attendance marked: {}", attendance);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", attendance));
    }

    /**
     * Check out for today's attendance.
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkOut() {
        var currentUser = attendanceService.getCurrentUserStats();
        // Get user ID from service
        AttendanceDTO attendance = attendanceService.checkOut(
                attendanceService.getCurrentUserAttendance().get(0).getUserId());
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", attendance));
    }

    /**
     * Get attendance by date range for a specific user.
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceDTO> records = attendanceService.getAttendanceByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Get attendance for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getUserAttendance(@PathVariable Long userId) {
        List<AttendanceDTO> records = attendanceService.getAttendanceByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    /**
     * Get today's attendance for current user.
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceDTO>> getTodayAttendance() {
        List<AttendanceDTO> records = attendanceService.getCurrentUserAttendance();
        if (!records.isEmpty()) {
            AttendanceDTO today = records.stream()
                    .filter(a -> a.getDate().equals(LocalDate.now()))
                    .findFirst()
                    .orElse(null);
            return ResponseEntity.ok(ApiResponse.success(today));
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * Check if attendance is already marked for today.
     */
    @GetMapping("/check-today")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkTodayStatus() {
        List<AttendanceDTO> records = attendanceService.getCurrentUserAttendance();
        boolean marked = records.stream()
                .anyMatch(a -> a.getDate().equals(LocalDate.now()));
        return ResponseEntity.ok(ApiResponse.success(Map.of("marked", marked)));
    }

    /**
     * Get today's attendance status for current user (alias for check-today).
     */
    @GetMapping("/today-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayStatus() {
        logger.debug("Getting today's status");
        List<AttendanceDTO> records = attendanceService.getCurrentUserAttendance();
        AttendanceDTO today = records.stream()
                .filter(a -> a.getDate().equals(LocalDate.now()))
                .findFirst()
                .orElse(null);
        
        Map<String, Object> status = Map.of(
                "marked", today != null,
                "present", today != null && today.isPresent(),
                "checkedOut", today != null && today.getCheckOutTime() != null
        );
        logger.debug("Today status: {}", status);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
