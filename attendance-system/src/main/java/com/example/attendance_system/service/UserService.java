package com.example.attendance_system.service;

import com.example.attendance_system.dto.PasswordChangeDTO;
import com.example.attendance_system.dto.RegisterDTO;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.exception.DuplicateResourceException;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for user management operations.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with local authentication.
     */
    public UserDTO registerUser(RegisterDTO registerDTO) {
        logger.info("Registering new user: {}", registerDTO.getUsername());

        // Validate passwords match
        if (!registerDTO.isPasswordMatching()) {
            throw new InvalidOperationException("Passwords do not match");
        }

        // Check for existing user
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new DuplicateResourceException("User", "username", registerDTO.getUsername());
        }
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new DuplicateResourceException("User", "email", registerDTO.getEmail());
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setAuthProvider(AuthProvider.LOCAL);

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return UserDTO.fromEntity(savedUser);
    }

    /**
     * Register a local user (internal use).
     */
    public User registerLocalUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Get the current authenticated user entity.
     */
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // No authentication or anonymous user
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            logger.debug("No authenticated user found");
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        // Handle OIDC authentication (Google) - check first since OidcUser extends OAuth2User
        if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            String email = oidcUser.getEmail();
            logger.debug("OIDC user lookup by email: {}", email);
            return userRepository.findByEmail(email).orElse(null);
        }

        // Handle OAuth2 authentication (GitHub, etc.)
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            logger.debug("OAuth2 user lookup by email: {}", email);
            return userRepository.findByEmail(email).orElse(null);
        }

        // Handle form login authentication (UserDetails or String)
        String identifier = authentication.getName();
        logger.debug("Form login user lookup by identifier: {}", identifier);
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElse(null);
    }

    /**
     * Get the current authenticated user as DTO.
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        User user = getCurrentUserEntity();
        return user != null ? UserDTO.fromEntity(user) : null;
    }

    /**
     * Find user by username.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by provider ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDTO.fromEntity(user);
    }

    /**
     * Get all users.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile.
     */
    public UserDTO updateProfile(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDTO.getId()));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(userDTO.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(userDTO.getEmail(), user.getId())) {
                throw new DuplicateResourceException("User", "email", userDTO.getEmail());
            }
            user.setEmail(userDTO.getEmail());
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());

        User savedUser = userRepository.save(user);
        logger.info("User profile updated: {}", savedUser.getUsername());
        return UserDTO.fromEntity(savedUser);
    }

    /**
     * Change user password.
     */
    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // SSO users cannot change password
        if (user.isOAuthUser()) {
            throw new InvalidOperationException("Cannot change password for OAuth users");
        }

        // Validate current password
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        // Validate new passwords match
        if (!passwordChangeDTO.isPasswordMatching()) {
            throw new InvalidOperationException("New passwords do not match");
        }

        // Validate new password is different
        if (!passwordChangeDTO.isNewPasswordDifferent()) {
            throw new InvalidOperationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Update user role (admin only).
     */
    public void updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(role);
        userRepository.save(user);
        logger.info("Role updated for user {}: {}", user.getUsername(), role);
    }

    /**
     * Toggle user enabled status.
     */
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        User currentUser = getCurrentUserEntity();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new InvalidOperationException("Cannot disable your own account");
        }

        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        logger.info("User {} status changed to: {}", user.getUsername(), user.isEnabled() ? "enabled" : "disabled");
    }

    /**
     * Delete user by ID.
     */
    public void deleteUser(Long id) {
        User currentUser = getCurrentUserEntity();
        if (currentUser != null && currentUser.getId().equals(id)) {
            throw new InvalidOperationException("Cannot delete your own account");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted with id: {}", id);
    }

    /**
     * Save user entity.
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Check if username exists.
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists.
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Admin update user (flexible field updates).
     */
    public UserDTO adminUpdateUser(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("email")) {
            String newEmail = (String) updates.get("email");
            if (!user.getEmail().equals(newEmail)) {
                if (userRepository.existsByEmailAndIdNot(newEmail, userId)) {
                    throw new DuplicateResourceException("User", "email", newEmail);
                }
                user.setEmail(newEmail);
            }
        }
        if (updates.containsKey("username")) {
            String newUsername = (String) updates.get("username");
            if (!user.getUsername().equals(newUsername)) {
                if (userRepository.existsByUsernameAndIdNot(newUsername, userId)) {
                    throw new DuplicateResourceException("User", "username", newUsername);
                }
                user.setUsername(newUsername);
            }
        }
        if (updates.containsKey("role")) {
            Role role = Role.valueOf(((String) updates.get("role")).toUpperCase());
            user.setRole(role);
        }
        if (updates.containsKey("enabled")) {
            user.setEnabled(Boolean.parseBoolean(updates.get("enabled").toString()));
        }

        User savedUser = userRepository.save(user);
        logger.info("Admin updated user: {}", savedUser.getUsername());
        return UserDTO.fromEntity(savedUser);
    }

    /**
     * Admin reset password.
     */
    public void adminResetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isOAuthUser()) {
            throw new InvalidOperationException("Cannot set password for OAuth users. User must link account first.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidOperationException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Admin reset password for user: {}", user.getUsername());
    }

    /**
     * Link OAuth account to local account (set password for OAuth user).
     */
    public void linkOAuthAccount(String newPassword, String confirmPassword) {
        User user = getCurrentUserEntity();
        if (user == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        if (!user.isOAuthUser()) {
            throw new InvalidOperationException("Account is already a local account");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidOperationException("Password must be at least 6 characters");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidOperationException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(user);
        logger.info("OAuth user {} linked to local account", user.getUsername());
    }
}
