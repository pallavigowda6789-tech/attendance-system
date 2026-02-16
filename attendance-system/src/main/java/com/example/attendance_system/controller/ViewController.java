package com.example.attendance_system.controller;

import com.example.attendance_system.service.AttendanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    private final AttendanceService attendanceService;

    public ViewController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Home Page
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Dashboard Page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Welcome to Attendance Dashboard");
        model.addAttribute("records", attendanceService.getAllAttendance());
        return "dashboard";
    }

    // Mark Attendance (Form Submission)
    @PostMapping("/mark-attendance")
    public String markAttendance(@RequestParam String username) {
        attendanceService.markAttendance(username);
        return "redirect:/dashboard";
    }
}
