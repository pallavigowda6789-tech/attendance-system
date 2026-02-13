package com.example.attendance_system.controller;


import com.example.attendance_system.service.AttendanceService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to Attendance System";
    }

    @PostMapping("/attendance")
    public String markAttendance(@AuthenticationPrincipal OidcUser user) {
        service.markAttendance(user.getEmail());
        return "Attendance marked for " + user.getEmail();
    }
}
