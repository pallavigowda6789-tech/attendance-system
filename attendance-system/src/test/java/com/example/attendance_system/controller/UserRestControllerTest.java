package com.example.attendance_system.controller;

import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void testGetCurrentUserProfile() {
        when(userService.getCurrentUser()).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = userRestController.getCurrentUserProfile();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    void testGetCurrentUserProfile_NotAuthenticated() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseEntity<UserDTO> response = userRestController.getCurrentUserProfile();

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void testUpdateProfile() {
        doNothing().when(userService).updateProfile(any(UserDTO.class));

        ResponseEntity<?> response = userRestController.updateProfile(userDTO);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).updateProfile(any(UserDTO.class));
    }
}
