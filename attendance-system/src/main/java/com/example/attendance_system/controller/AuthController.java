package com.example.attendance_system.controller;

import com.example.attendance_system.dto.RegisterDTO;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        // Show login page (even though security is disabled, page still accessible)
        return "auth/login";
    }
    
    @PostMapping("/login")
    public String loginPost() {
        // Since security is disabled, just redirect to dashboard
        // In production, Spring Security handles this
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO registerDTO, 
                         BindingResult result, 
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }

        try {
            User user = userService.registerUser(registerDTO);
            redirectAttributes.addFlashAttribute("success", "Registration successful! You can now access the dashboard.");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", registerDTO);
            return "auth/register";
        }
    }
}
