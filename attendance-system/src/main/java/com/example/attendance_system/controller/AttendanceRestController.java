package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceRestController {

    private final AttendanceService attendanceService;

    public AttendanceRestController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AttendanceStatsDTO> getMyStats() {
        try {
            AttendanceStatsDTO stats = attendanceService.getCurrentUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-records")
    public ResponseEntity<?> getMyRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceDTO> allRecords;
            
            if (startDate != null && endDate != null) {
                // Get current user ID and fetch filtered records
                allRecords = attendanceService.getCurrentUserAttendanceByDateRange(startDate, endDate);
            } else {
                allRecords = attendanceService.getCurrentUserAttendance();
            }
            
            // Manual pagination
            int start = page * size;
            int end = Math.min(start + size, allRecords.size());
            List<AttendanceDTO> pageRecords = start < allRecords.size() 
                ? allRecords.subList(start, end) 
                : List.of();
            
            Map<String, Object> response = Map.of(
                "content", pageRecords,
                "totalElements", allRecords.size(),
                "totalPages", (int) Math.ceil((double) allRecords.size() / size),
                "currentPage", page,
                "pageSize", size
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestBody Map<String, Boolean> request) {
        try {
            boolean present = request.getOrDefault("present", true);
            AttendanceDTO attendance = attendanceService.markAttendanceForCurrentUser(present);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Attendance marked successfully",
                "data", attendance
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/range")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceDTO> records = attendanceService.getAttendanceByDateRange(userId, startDate, endDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceDTO>> getUserAttendance(@PathVariable Long userId) {
        try {
            List<AttendanceDTO> records = attendanceService.getAttendanceByUser(userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
