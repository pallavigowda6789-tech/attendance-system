package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.dto.UserDTO;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.service.AttendanceService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminRestController Tests")
class AdminRestControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AdminRestController adminRestController;

    private UserDTO userDTO;
    private AttendanceDTO attendanceDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO(1L, "testuser", "test@example.com",
                "Test", "User", Role.USER, true, AuthProvider.LOCAL);
        attendanceDTO = new AttendanceDTO(1L, 1L, "testuser", LocalDate.now(), true, LocalDateTime.now());
    }

    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {

        @Test
        @DisplayName("Should get all users successfully")
        void getAllUsers_Success() {
            List<UserDTO> users = Arrays.asList(userDTO);
            when(userService.getAllUsers()).thenReturn(users);

            ResponseEntity<ApiResponse<List<UserDTO>>> response = adminRestController.getAllUsers();

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
            verify(userService, times(1)).getAllUsers();
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        void getUserById_Success() {
            when(userService.getUserById(1L)).thenReturn(userDTO);

            ResponseEntity<ApiResponse<UserDTO>> response = adminRestController.getUserById(1L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("testuser", response.getBody().getData().getUsername());
            verify(userService, times(1)).getUserById(1L);
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void getUserById_NotFound() {
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

            assertThrows(ResourceNotFoundException.class, () -> {
                adminRestController.getUserById(999L);
            });
        }
    }

    @Nested
    @DisplayName("User Delete Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_Success() {
            doNothing().when(userService).deleteUser(anyLong());

            ResponseEntity<ApiResponse<Void>> response = adminRestController.deleteUser(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("User deleted successfully", response.getBody().getMessage());
            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @DisplayName("Should not allow deleting own account")
        void deleteUser_CannotDeleteSelf() {
            doThrow(new InvalidOperationException("Cannot delete your own account"))
                    .when(userService).deleteUser(anyLong());

            assertThrows(InvalidOperationException.class, () -> {
                adminRestController.deleteUser(1L);
            });
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent user")
        void deleteUser_NotFound() {
            doThrow(new ResourceNotFoundException("User not found"))
                    .when(userService).deleteUser(999L);

            assertThrows(ResourceNotFoundException.class, () -> {
                adminRestController.deleteUser(999L);
            });
        }
    }

    @Nested
    @DisplayName("Role Management Tests")
    class RoleManagementTests {

        @Test
        @DisplayName("Should update user role successfully")
        void updateUserRole_Success() {
            doNothing().when(userService).updateUserRole(anyLong(), any(Role.class));

            Map<String, String> request = new HashMap<>();
            request.put("role", "ADMIN");

            ResponseEntity<ApiResponse<Void>> response = adminRestController.updateUserRole(1L, request);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("User role updated successfully", response.getBody().getMessage());
            verify(userService, times(1)).updateUserRole(1L, Role.ADMIN);
        }

        @Test
        @DisplayName("Should handle invalid role")
        void updateUserRole_InvalidRole() {
            Map<String, String> request = new HashMap<>();
            request.put("role", "INVALID_ROLE");

            assertThrows(IllegalArgumentException.class, () -> {
                adminRestController.updateUserRole(1L, request);
            });
        }
    }

    @Nested
    @DisplayName("User Status Tests")
    class UserStatusTests {

        @Test
        @DisplayName("Should toggle user status successfully")
        void toggleUserStatus_Success() {
            doNothing().when(userService).toggleUserStatus(anyLong());

            ResponseEntity<ApiResponse<Void>> response = adminRestController.toggleUserStatus(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("User status updated successfully", response.getBody().getMessage());
            verify(userService, times(1)).toggleUserStatus(1L);
        }

        @Test
        @DisplayName("Should not allow toggling own status")
        void toggleUserStatus_CannotToggleSelf() {
            doThrow(new InvalidOperationException("Cannot disable your own account"))
                    .when(userService).toggleUserStatus(anyLong());

            assertThrows(InvalidOperationException.class, () -> {
                adminRestController.toggleUserStatus(1L);
            });
        }
    }

    @Nested
    @DisplayName("Attendance Management Tests")
    class AttendanceManagementTests {

        @Test
        @DisplayName("Should get attendance for user")
        void getUserAttendance_Success() {
            List<AttendanceDTO> attendance = Arrays.asList(attendanceDTO);
            when(attendanceService.getAttendanceByUser(1L)).thenReturn(attendance);

            ResponseEntity<ApiResponse<List<AttendanceDTO>>> response =
                    adminRestController.getUserAttendance(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
        }

        @Test
        @DisplayName("Should get all attendance records with pagination")
        void getAllAttendance_Success() {
            List<AttendanceDTO> attendance = Arrays.asList(attendanceDTO);
            PagedResponse<AttendanceDTO> pagedResponse = PagedResponse.of(attendance, 0, 10, 1);
            when(attendanceService.getAllAttendancePaginated(0, 10))
                    .thenReturn(pagedResponse);

            ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> response =
                    adminRestController.getAllAttendance(0, 10, null, null, null);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
        }

        @Test
        @DisplayName("Should delete attendance record")
        void deleteAttendance_Success() {
            doNothing().when(attendanceService).deleteAttendance(1L);

            ResponseEntity<ApiResponse<Void>> response = adminRestController.deleteAttendance(1L);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Attendance record deleted successfully", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("System Stats Tests")
    class SystemStatsTests {

        @Test
        @DisplayName("Should get system statistics")
        void getSystemStats_Success() {
            List<UserDTO> users = Arrays.asList(
                    userDTO,
                    new UserDTO(2L, "admin", "admin@example.com", "Admin", "User", Role.ADMIN, true, AuthProvider.LOCAL)
            );
            when(userService.getAllUsers()).thenReturn(users);

            ResponseEntity<ApiResponse<Map<String, Object>>> response =
                    adminRestController.getSystemStats();

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            Map<String, Object> stats = response.getBody().getData();
            assertEquals(2L, stats.get("totalUsers"));
            assertEquals(2L, stats.get("activeUsers"));
            assertEquals(1L, stats.get("adminCount"));
        }
    }
}
