package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.RegisterDTO;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for authentication-related pages and actions.
 */
@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display login page.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    /**
     * Handle login form submission (for development mode).
     */
    @PostMapping("/login")
    public String loginPost() {
        // In development mode, just redirect to dashboard
        // In production, Spring Security handles authentication
        return "redirect:/dashboard";
    }

    /**
     * Display registration page.
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    /**
     * Handle registration form submission.
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO registerDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (result.hasErrors()) {
            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }

        // Validate password match
        if (!registerDTO.isPasswordMatching()) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }

        try {
            UserDTO user = userService.registerUser(registerDTO);
            logger.info("User registered successfully: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("success", 
                    "Registration successful! You can now access the dashboard.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }
    }

    /**
     * API endpoint to check if username is available.
     */
    @GetMapping("/api/auth/check-username")
    @ResponseBody
    public ApiResponse<Boolean> checkUsername(String username) {
        boolean available = !userService.existsByUsername(username);
        return ApiResponse.success(available);
    }

    /**
     * API endpoint to check if email is available.
     */
    @GetMapping("/api/auth/check-email")
    @ResponseBody
    public ApiResponse<Boolean> checkEmail(String email) {
        boolean available = !userService.existsByEmail(email);
        return ApiResponse.success(available);
    }
}
