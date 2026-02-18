package com.example.attendance_system.controller;

import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.service.AttendanceService;
import com.example.attendance_system.service.LeaveService;
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
    private final LeaveService leaveService;

    public ViewController(AttendanceService attendanceService, UserService userService, LeaveService leaveService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.leaveService = leaveService;
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
     * Leave management page.
     */
    @GetMapping("/leaves")
    public String leaves(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        try {
            var leaveStats = leaveService.getCurrentUserLeaveStats();
            model.addAttribute("leaveStats", leaveStats);
        } catch (Exception e) {
            model.addAttribute("leaveStats", null);
        }
        return "user/leaves";
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

    /**
     * Admin dashboard page.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "admin/dashboard";
    }

    /**
     * Admin attendance management page.
     */
    @GetMapping("/admin/attendance")
    public String adminAttendance(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        return "admin/attendance";
    }

    /**
     * Admin leave management page.
     */
    @GetMapping("/admin/leaves")
    public String adminLeaves(Model model) {
        UserDTO currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        try {
            long pendingCount = leaveService.getPendingLeavesCount();
            model.addAttribute("pendingCount", pendingCount);
        } catch (Exception e) {
            model.addAttribute("pendingCount", 0);
        }
        return "admin/leaves";
    }
}
