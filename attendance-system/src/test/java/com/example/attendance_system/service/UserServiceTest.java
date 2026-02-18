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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setAuthProvider(AuthProvider.LOCAL);
        testUser.setCreatedAt(LocalDateTime.now());

        registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setEmail("newuser@example.com");
        registerDTO.setPassword("password123");
        registerDTO.setConfirmPassword("password123");
        registerDTO.setFirstName("New");
        registerDTO.setLastName("User");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void registerUser_Success() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDTO result = userService.registerUser(registerDTO);

            assertNotNull(result);
            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode("password123");
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void registerUser_EmailAlreadyExists() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> {
                userService.registerUser(registerDTO);
            });

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void registerUser_UsernameAlreadyExists() {
            when(userRepository.existsByUsername(anyString())).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> {
                userService.registerUser(registerDTO);
            });

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when passwords don't match")
        void registerUser_PasswordMismatch() {
            registerDTO.setConfirmPassword("differentPassword");

            assertThrows(InvalidOperationException.class, () -> {
                userService.registerUser(registerDTO);
            });

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class RetrievalTests {

        @Test
        @DisplayName("Should get all users")
        void getAllUsers() {
            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");
            user2.setEmail("user2@example.com");
            user2.setFirstName("User");
            user2.setLastName("Two");
            user2.setRole(Role.USER);
            user2.setEnabled(true);
            user2.setAuthProvider(AuthProvider.LOCAL);

            when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

            List<UserDTO> users = userService.getAllUsers();

            assertNotNull(users);
            assertEquals(2, users.size());
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        void getUserById_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserById(1L);

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            assertEquals("test@example.com", result.getEmail());
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found by ID")
        void getUserById_NotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                userService.getUserById(1L);
            });
        }

        @Test
        @DisplayName("Should find user by username")
        void findByUsername() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByUsername("testuser");

            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
        }

        @Test
        @DisplayName("Should find user by email")
        void findByEmail() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.findByEmail("test@example.com");

            assertTrue(result.isPresent());
            assertEquals("test@example.com", result.get().getEmail());
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update user role")
        void updateUserRole() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.updateUserRole(1L, Role.ADMIN);

            verify(userRepository, times(1)).save(any(User.class));
            assertEquals(Role.ADMIN, testUser.getRole());
        }

        @Test
        @DisplayName("Should toggle user status")
        void toggleUserStatus() {
            testUser.setEnabled(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.toggleUserStatus(1L);

            verify(userRepository, times(1)).save(any(User.class));
            assertFalse(testUser.isEnabled());
        }

        @Test
        @DisplayName("Should update profile successfully")
        void updateProfile_Success() {
            UserDTO updateDTO = new UserDTO();
            updateDTO.setId(1L);
            updateDTO.setFirstName("Updated");
            updateDTO.setLastName("Name");
            updateDTO.setEmail("test@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            UserDTO result = userService.updateProfile(updateDTO);

            assertNotNull(result);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when updating email to existing one")
        void updateProfile_EmailExists() {
            UserDTO updateDTO = new UserDTO();
            updateDTO.setId(1L);
            updateDTO.setEmail("existing@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.existsByEmailAndIdNot("existing@example.com", 1L)).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> {
                userService.updateProfile(updateDTO);
            });
        }
    }

    @Nested
    @DisplayName("Password Change Tests")
    class PasswordChangeTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            PasswordChangeDTO dto = new PasswordChangeDTO("oldPassword", "newPassword123", "newPassword123");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "encoded_password")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("new_encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> userService.changePassword(1L, dto));
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void changePassword_WrongCurrentPassword() {
            PasswordChangeDTO dto = new PasswordChangeDTO("wrongPassword", "newPassword123", "newPassword123");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "encoded_password")).thenReturn(false);

            assertThrows(InvalidOperationException.class, () -> {
                userService.changePassword(1L, dto);
            });

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception for OAuth user password change")
        void changePassword_OAuthUser() {
            testUser.setAuthProvider(AuthProvider.GOOGLE);
            PasswordChangeDTO dto = new PasswordChangeDTO("oldPassword", "newPassword123", "newPassword123");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThrows(InvalidOperationException.class, () -> {
                userService.changePassword(1L, dto);
            });
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_Success() {
            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(2L);

            assertDoesNotThrow(() -> userService.deleteUser(2L));
            verify(userRepository, times(1)).deleteById(2L);
        }

        @Test
        @DisplayName("Should throw exception when user not found for deletion")
        void deleteUser_NotFound() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> {
                userService.deleteUser(99L);
            });

            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Existence Check Tests")
    class ExistenceTests {

        @Test
        @DisplayName("Should check if username exists")
        void existsByUsername() {
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            assertTrue(userService.existsByUsername("testuser"));
        }

        @Test
        @DisplayName("Should check if email exists")
        void existsByEmail() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            assertTrue(userService.existsByEmail("test@example.com"));
        }
    }
}
