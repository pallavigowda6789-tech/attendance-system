package com.example.attendance_system.controller;

import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    public ViewController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }

    // Home Page
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Dashboard Page
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        
        // Get stats for the dashboard
        var stats = attendanceService.getCurrentUserStats();
        model.addAttribute("stats", stats);
        
        return "dashboard";
    }

    // Attendance Page
    @GetMapping("/attendance")
    public String attendance(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "user/attendance";
    }

    // Profile Page
    @GetMapping("/profile")
    public String profile(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "user/profile";
    }

    // Admin Users Page
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "admin/users";
    }
}
