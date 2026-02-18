package com.example.attendance_system.config;

import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom authentication success handler for both form and OAuth2 login.
 * Creates new users for OAuth2/OIDC logins if they don't exist.
 * Redirects admins to admin dashboard, others to user dashboard.
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        logger.info("Authentication successful for principal type: {}", 
                authentication.getPrincipal().getClass().getSimpleName());

        User user = extractOrCreateUser(authentication);
        
        if (user != null) {
            setupSession(request.getSession(), user);
            logger.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole());
            
            // Redirect based on role
            String targetUrl = determineTargetUrl(user);
            logger.info("Redirecting to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            logger.warn("Could not find or create user for authenticated principal");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    /**
     * Determine the target URL based on user role.
     */
    private String determineTargetUrl(User user) {
        if (user.getRole() == Role.ADMIN) {
            return "/admin/dashboard";
        }
        return "/dashboard";
    }

    /**
     * Extract User entity from authentication principal, creating if necessary for OAuth2.
     */
    private User extractOrCreateUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // Handle OIDC (Google) - must check before OAuth2User since OidcUser extends OAuth2User
        if (principal instanceof OidcUser oidcUser) {
            return handleOidcUser(oidcUser);
        } else if (principal instanceof OAuth2User oauth2User) {
            return handleOAuth2User(oauth2User);
        } else if (principal instanceof UserDetails userDetails) {
            return handleUserDetails(userDetails);
        } else if (principal instanceof String username) {
            return handleUsername(username);
        }

        return null;
    }

    /**
     * Handle OIDC user authentication (Google).
     */
    private User handleOidcUser(OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        logger.debug("OIDC login with email: {}", email);
        
        if (email == null || email.isBlank()) {
            logger.warn("No email found in OIDC user");
            return null;
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Create new user from OIDC data
        logger.info("Creating new user from OIDC: {}", email);
        return createOAuth2User(email, 
                oidcUser.getGivenName(), 
                oidcUser.getFamilyName(), 
                oidcUser.getFullName(),
                oidcUser.getSubject(),
                AuthProvider.GOOGLE);
    }

    /**
     * Handle OAuth2 user authentication (GitHub, etc.).
     */
    private User handleOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        logger.debug("OAuth2 login with email: {}", email);
        
        if (email == null || email.isBlank()) {
            logger.warn("No email found in OAuth2 user");
            return null;
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Create new user from OAuth2 data
        logger.info("Creating new user from OAuth2: {}", email);
        String providerId = oauth2User.getAttribute("sub");
        if (providerId == null) {
            Object id = oauth2User.getAttribute("id");
            providerId = id != null ? id.toString() : null;
        }
        
        return createOAuth2User(email,
                oauth2User.getAttribute("given_name"),
                oauth2User.getAttribute("family_name"),
                oauth2User.getAttribute("name"),
                providerId,
                AuthProvider.GOOGLE);
    }

    /**
     * Create a new user from OAuth2/OIDC data.
     */
    private User createOAuth2User(String email, String firstName, String lastName, 
                                   String fullName, String providerId, AuthProvider provider) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(generateUsername(email));
        
        if (firstName != null) {
            newUser.setFirstName(firstName);
        } else if (fullName != null) {
            String[] nameParts = fullName.split(" ", 2);
            newUser.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                newUser.setLastName(nameParts[1]);
            }
        }
        
        if (lastName != null) {
            newUser.setLastName(lastName);
        }

        newUser.setRole(Role.USER);
        newUser.setEnabled(true);
        newUser.setAuthProvider(provider);
        newUser.setProviderId(providerId);

        User saved = userRepository.save(newUser);
        logger.info("Created new OAuth2 user: {} ({})", saved.getUsername(), email);
        return saved;
    }

    /**
     * Generate a unique username from email.
     */
    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }

    /**
     * Handle form login authentication.
     */
    private User handleUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        logger.debug("Form login with username: {}", username);
        
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
    }

    /**
     * Handle username string principal.
     */
    private User handleUsername(String username) {
        logger.debug("String principal login: {}", username);
        
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
    }

    /**
     * Setup session attributes for the authenticated user.
     */
    private void setupSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("userFullName", user.getFullName());
        
        logger.debug("Session setup complete for user: {} (ID: {})", 
                user.getUsername(), user.getId());
    }
}
