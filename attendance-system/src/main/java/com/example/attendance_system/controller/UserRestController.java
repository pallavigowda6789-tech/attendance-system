package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.PasswordChangeDTO;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user's profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile() {
        UserDTO user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated", 4001));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update current user's profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        UserDTO currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated", 4001));
        }
        
        // Ensure user can only update their own profile
        userDTO.setId(currentUser.getId());
        UserDTO updatedUser = userService.updateProfile(userDTO);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }

    /**
     * Change current user's password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        UserDTO currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("User not authenticated", 4001));
        }
        
        userService.changePassword(currentUser.getId(), passwordChangeDTO);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
