package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.PasswordChangeDTO;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.exception.AuthenticationException;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRestController Tests")
class UserRestControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestController userRestController;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(1L, "testuser", "test@example.com",
                "Test", "User", Role.USER, true, AuthProvider.LOCAL);
    }

    @Nested
    @DisplayName("Get Profile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should get current user profile successfully")
        void getCurrentUserProfile_Success() {
            when(userService.getCurrentUser()).thenReturn(userDTO);

            ResponseEntity<ApiResponse<UserDTO>> response = userRestController.getCurrentUserProfile();

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("testuser", response.getBody().getData().getUsername());
            verify(userService, times(1)).getCurrentUser();
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getCurrentUserProfile_NotAuthenticated() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<ApiResponse<UserDTO>> response = userRestController.getCurrentUserProfile();

            assertEquals(401, response.getStatusCode().value());
            assertFalse(response.getBody().isSuccess());
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void updateProfile_Success() {
            when(userService.getCurrentUser()).thenReturn(userDTO);
            when(userService.updateProfile(any(UserDTO.class))).thenReturn(userDTO);

            ResponseEntity<ApiResponse<UserDTO>> response = userRestController.updateProfile(userDTO);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Profile updated successfully", response.getBody().getMessage());
            verify(userService, times(1)).updateProfile(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateProfile_NotAuthenticated() {
            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<ApiResponse<UserDTO>> response = userRestController.updateProfile(userDTO);

            assertEquals(401, response.getStatusCode().value());
            assertFalse(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("Should handle update for non-existent user")
        void updateProfile_UserNotFound() {
            when(userService.getCurrentUser()).thenReturn(userDTO);
            when(userService.updateProfile(any(UserDTO.class)))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            assertThrows(ResourceNotFoundException.class, () -> {
                userRestController.updateProfile(userDTO);
            });
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
            passwordDTO.setCurrentPassword("oldPass123");
            passwordDTO.setNewPassword("newPass123");
            passwordDTO.setConfirmPassword("newPass123");

            when(userService.getCurrentUser()).thenReturn(userDTO);
            doNothing().when(userService).changePassword(anyLong(), any(PasswordChangeDTO.class));

            ResponseEntity<ApiResponse<Void>> response = userRestController.changePassword(passwordDTO);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Password changed successfully", response.getBody().getMessage());
            verify(userService, times(1)).changePassword(eq(1L), any(PasswordChangeDTO.class));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void changePassword_NotAuthenticated() {
            PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
            passwordDTO.setCurrentPassword("oldPass123");
            passwordDTO.setNewPassword("newPass123");
            passwordDTO.setConfirmPassword("newPass123");

            when(userService.getCurrentUser()).thenReturn(null);

            ResponseEntity<ApiResponse<Void>> response = userRestController.changePassword(passwordDTO);

            assertEquals(401, response.getStatusCode().value());
            assertFalse(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("Should fail with wrong current password")
        void changePassword_WrongCurrentPassword() {
            PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
            passwordDTO.setCurrentPassword("wrongPass");
            passwordDTO.setNewPassword("newPass123");
            passwordDTO.setConfirmPassword("newPass123");

            when(userService.getCurrentUser()).thenReturn(userDTO);
            doThrow(new InvalidOperationException("Current password is incorrect"))
                    .when(userService).changePassword(anyLong(), any(PasswordChangeDTO.class));

            assertThrows(InvalidOperationException.class, () -> {
                userRestController.changePassword(passwordDTO);
            });
        }

        @Test
        @DisplayName("Should fail for OAuth users")
        void changePassword_OAuthUser() {
            PasswordChangeDTO passwordDTO = new PasswordChangeDTO();
            passwordDTO.setCurrentPassword("pass");
            passwordDTO.setNewPassword("newPass123");
            passwordDTO.setConfirmPassword("newPass123");

            when(userService.getCurrentUser()).thenReturn(userDTO);
            doThrow(new InvalidOperationException("Cannot change password for OAuth users"))
                    .when(userService).changePassword(anyLong(), any(PasswordChangeDTO.class));

            assertThrows(InvalidOperationException.class, () -> {
                userRestController.changePassword(passwordDTO);
            });
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void getUserById_Success() {
            when(userService.getUserById(1L)).thenReturn(userDTO);

            ResponseEntity<ApiResponse<UserDTO>> response = userRestController.getUserById(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("testuser", response.getBody().getData().getUsername());
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void getUserById_NotFound() {
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("User not found"));

            assertThrows(ResourceNotFoundException.class, () -> {
                userRestController.getUserById(999L);
            });
        }
    }
}
