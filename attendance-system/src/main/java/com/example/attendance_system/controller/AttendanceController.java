package com.example.attendance_system.controller;


import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.service.AttendanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @PostMapping("/{username}")
    public Attendance markAttendance(@PathVariable String username) {
        return service.markAttendance(username);
    }

    @GetMapping
    public List<Attendance> getAll() {
        return service.getAllAttendance();
    }
}