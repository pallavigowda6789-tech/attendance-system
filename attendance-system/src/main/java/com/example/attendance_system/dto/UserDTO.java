package com.example.attendance_system.dto;

import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User entity.
 */
public class UserDTO {

    private Long id;
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
    
    private String fullName;
    private Role role;
    private boolean enabled;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;

    // Constructors
    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String firstName, String lastName, 
                   Role role, boolean enabled, AuthProvider authProvider) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.enabled = enabled;
        this.authProvider = authProvider;
        this.fullName = buildFullName(firstName, lastName, username);
    }

    // Static factory method from entity
    public static UserDTO fromEntity(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setAuthProvider(user.getAuthProvider());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setFullName(user.getFullName());
        return dto;
    }

    private static String buildFullName(String firstName, String lastName, String username) {
        if (firstName == null && lastName == null) {
            return username;
        }
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.fullName = buildFullName(firstName, this.lastName, this.username);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.fullName = buildFullName(this.firstName, lastName, this.username);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
