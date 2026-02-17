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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRestControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminRestController adminRestController;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(1L, "testuser", "test@example.com", 
                "Test", "User", Role.USER, true, AuthProvider.LOCAL);
    }

    @Test
    void testGetAllUsers() {
        List<UserDTO> users = Arrays.asList(userDTO);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserDTO>> response = adminRestController.getAllUsers();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(1L)).thenReturn(userDTO);

        ResponseEntity<UserDTO> response = adminRestController.getUserById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("testuser", response.getBody().getUsername());
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<UserDTO> response = adminRestController.getUserById(1L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(anyLong());

        ResponseEntity<?> response = adminRestController.deleteUser(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void testDeleteUser_CannotDeleteSelf() {
        doThrow(new RuntimeException("Cannot delete your own account"))
                .when(userService).deleteUser(anyLong());

        ResponseEntity<?> response = adminRestController.deleteUser(1L);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testUpdateUserRole() {
        doNothing().when(userService).updateUserRole(anyLong(), any(Role.class));

        Map<String, String> request = new HashMap<>();
        request.put("role", "ADMIN");

        ResponseEntity<?> response = adminRestController.updateUserRole(1L, request);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).updateUserRole(1L, Role.ADMIN);
    }

    @Test
    void testToggleUserStatus() {
        doNothing().when(userService).toggleUserStatus(anyLong());

        ResponseEntity<?> response = adminRestController.toggleUserStatus(1L);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).toggleUserStatus(1L);
    }

    @Test
    void testGetAllUsers_Error() {
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<UserDTO>> response = adminRestController.getAllUsers();

        assertEquals(400, response.getStatusCode().value());
    }
}
