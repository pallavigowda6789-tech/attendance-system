package com.example.attendance_system.config;

import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        logger.info("Authentication successful for: {}", authentication.getName());
        
        // Set session attribute for easier access in controllers
        String username = null;
        
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            logger.info("OAuth2 login with email: {}", email);
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                username = user.getUsername();
                request.getSession().setAttribute("userId", user.getId());
                request.getSession().setAttribute("username", user.getUsername());
                logger.info("OAuth2 user found: {} (ID: {})", username, user.getId());
            }
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            username = userDetails.getUsername();
            logger.info("Form login with username: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                request.getSession().setAttribute("userId", user.getId());
                request.getSession().setAttribute("username", user.getUsername());
                logger.info("Form user found: {} (ID: {})", username, user.getId());
            }
        }
        
        logger.info("Redirecting to dashboard...");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
