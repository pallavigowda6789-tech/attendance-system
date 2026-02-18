package com.example.attendance_system.controller;

import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving Thymeleaf view pages.
 */
@Controller
public class ViewController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    public ViewController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }

    /**
     * Home page.
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    /**
     * Dashboard page with attendance statistics.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        try {
            var stats = attendanceService.getCurrentUserStats();
            model.addAttribute("stats", stats);
        } catch (Exception e) {
            // Provide default stats if not available
            model.addAttribute("stats", new com.example.attendance_system.dto.AttendanceStatsDTO());
        }

        return "dashboard";
    }

    /**
     * Attendance records page.
     */
    @GetMapping("/attendance")
    public String attendance(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "user/attendance";
    }

    /**
     * User profile page.
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "user/profile";
    }

    /**
     * Admin users management page.
     */
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "admin/users";
    }
}
