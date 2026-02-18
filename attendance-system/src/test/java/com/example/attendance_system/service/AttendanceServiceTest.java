package com.example.attendance_system.service;

import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.exception.DuplicateResourceException;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceService Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AttendanceService attendanceService;

    private User testUser;
    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setAuthProvider(AuthProvider.LOCAL);

        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setUser(testUser);
        testAttendance.setDate(LocalDate.now());
        testAttendance.setPresent(true);
        testAttendance.setCheckInTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Mark Attendance Tests")
    class MarkAttendanceTests {

        @Test
        @DisplayName("Should mark attendance successfully")
        void markAttendanceByUserId_Success() {
            LocalDate date = LocalDate.now();
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.existsByUserAndDate(testUser, date)).thenReturn(false);
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

            AttendanceDTO result = attendanceService.markAttendanceByUserId(1L, date, true);

            assertNotNull(result);
            assertTrue(result.isPresent());
            verify(attendanceRepository, times(1)).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Should throw exception when attendance already marked")
        void markAttendanceByUserId_AlreadyMarked() {
            LocalDate date = LocalDate.now();
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.existsByUserAndDate(testUser, date)).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> {
                attendanceService.markAttendanceByUserId(1L, date, true);
            });

            verify(attendanceRepository, never()).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void markAttendanceByUserId_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                attendanceService.markAttendanceByUserId(1L, LocalDate.now(), true);
            });
        }

        @Test
        @DisplayName("Should mark attendance for current user")
        void markAttendanceForCurrentUser_Success() {
            when(userService.getCurrentUserEntity()).thenReturn(testUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.existsByUserAndDate(testUser, LocalDate.now())).thenReturn(false);
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

            AttendanceDTO result = attendanceService.markAttendanceForCurrentUser(true);

            assertNotNull(result);
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Should throw exception when current user not authenticated")
        void markAttendanceForCurrentUser_NotAuthenticated() {
            when(userService.getCurrentUserEntity()).thenReturn(null);

            assertThrows(InvalidOperationException.class, () -> {
                attendanceService.markAttendanceForCurrentUser(true);
            });
        }
    }

    @Nested
    @DisplayName("Get Attendance Tests")
    class GetAttendanceTests {

        @Test
        @DisplayName("Should get attendance by user")
        void getAttendanceByUser() {
            Attendance attendance2 = new Attendance();
            attendance2.setId(2L);
            attendance2.setUser(testUser);
            attendance2.setDate(LocalDate.now().minusDays(1));
            attendance2.setPresent(false);
            attendance2.setCheckInTime(LocalDateTime.now().minusDays(1));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserOrderByDateDesc(testUser))
                    .thenReturn(Arrays.asList(testAttendance, attendance2));

            List<AttendanceDTO> results = attendanceService.getAttendanceByUser(1L);

            assertNotNull(results);
            assertEquals(2, results.size());
            verify(attendanceRepository, times(1)).findByUserOrderByDateDesc(testUser);
        }

        @Test
        @DisplayName("Should get attendance by date range")
        void getAttendanceByDateRange() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDateBetweenOrderByDateDesc(testUser, startDate, endDate))
                    .thenReturn(Arrays.asList(testAttendance));

            List<AttendanceDTO> results = attendanceService.getAttendanceByDateRange(1L, startDate, endDate);

            assertNotNull(results);
            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("Should get current user attendance")
        void getCurrentUserAttendance() {
            when(userService.getCurrentUserEntity()).thenReturn(testUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserOrderByDateDesc(testUser))
                    .thenReturn(Arrays.asList(testAttendance));

            List<AttendanceDTO> results = attendanceService.getCurrentUserAttendance();

            assertNotNull(results);
            assertEquals(1, results.size());
        }

        @Test
        @DisplayName("Should get paginated attendance for current user")
        void getCurrentUserAttendancePaginated() {
            when(userService.getCurrentUserEntity()).thenReturn(testUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserOrderByDateDesc(testUser))
                    .thenReturn(Arrays.asList(testAttendance));

            PagedResponse<AttendanceDTO> result = attendanceService.getCurrentUserAttendancePaginated(0, 10, null, null);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("Attendance Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should calculate attendance statistics")
        void getAttendanceStats() {
            Attendance attendance2 = new Attendance();
            attendance2.setId(2L);
            attendance2.setUser(testUser);
            attendance2.setDate(LocalDate.now().minusDays(1));
            attendance2.setPresent(true);
            attendance2.setCheckInTime(LocalDateTime.now().minusDays(1));

            Attendance attendance3 = new Attendance();
            attendance3.setId(3L);
            attendance3.setUser(testUser);
            attendance3.setDate(LocalDate.now().minusDays(2));
            attendance3.setPresent(false);
            attendance3.setCheckInTime(LocalDateTime.now().minusDays(2));

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDateBetween(any(), any(), any()))
                    .thenReturn(Arrays.asList(testAttendance, attendance2, attendance3));

            AttendanceStatsDTO stats = attendanceService.getAttendanceStats(1L);

            assertNotNull(stats);
            assertEquals(3, stats.getTotalDays());
            assertEquals(2, stats.getPresentDays());
            assertEquals(1, stats.getAbsentDays());
            assertTrue(stats.getAttendancePercentage() >= 66.0 && stats.getAttendancePercentage() <= 67.0);
        }

        @Test
        @DisplayName("Should handle no attendance records")
        void getAttendanceStats_NoRecords() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDateBetween(any(), any(), any()))
                    .thenReturn(List.of());

            AttendanceStatsDTO stats = attendanceService.getAttendanceStats(1L);

            assertNotNull(stats);
            assertEquals(0, stats.getTotalDays());
            assertEquals(0, stats.getPresentDays());
            assertEquals(0, stats.getAbsentDays());
            assertEquals(0.0, stats.getAttendancePercentage());
        }

        @Test
        @DisplayName("Should get current user stats")
        void getCurrentUserStats() {
            when(userService.getCurrentUserEntity()).thenReturn(testUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDateBetween(any(), any(), any()))
                    .thenReturn(Arrays.asList(testAttendance));

            AttendanceStatsDTO stats = attendanceService.getCurrentUserStats();

            assertNotNull(stats);
            assertEquals(1, stats.getTotalDays());
        }
    }

    @Nested
    @DisplayName("Attendance Deletion Tests")
    class DeletionTests {

        @Test
        @DisplayName("Should delete attendance record")
        void deleteAttendance_Success() {
            when(attendanceRepository.existsById(1L)).thenReturn(true);
            doNothing().when(attendanceRepository).deleteById(1L);

            assertDoesNotThrow(() -> attendanceService.deleteAttendance(1L));
            verify(attendanceRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when attendance not found")
        void deleteAttendance_NotFound() {
            when(attendanceRepository.existsById(99L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> {
                attendanceService.deleteAttendance(99L);
            });

            verify(attendanceRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Check Out Tests")
    class CheckOutTests {

        @Test
        @DisplayName("Should check out successfully")
        void checkOut_Success() {
            testAttendance.setCheckOutTime(null);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDate(testUser, LocalDate.now()))
                    .thenReturn(Optional.of(testAttendance));
            when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

            AttendanceDTO result = attendanceService.checkOut(1L);

            assertNotNull(result);
            verify(attendanceRepository, times(1)).save(any(Attendance.class));
        }

        @Test
        @DisplayName("Should throw exception when already checked out")
        void checkOut_AlreadyCheckedOut() {
            testAttendance.setCheckOutTime(LocalDateTime.now());
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDate(testUser, LocalDate.now()))
                    .thenReturn(Optional.of(testAttendance));

            assertThrows(InvalidOperationException.class, () -> {
                attendanceService.checkOut(1L);
            });
        }

        @Test
        @DisplayName("Should throw exception when no attendance for today")
        void checkOut_NoAttendanceToday() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attendanceRepository.findByUserAndDate(testUser, LocalDate.now()))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                attendanceService.checkOut(1L);
            });
        }
    }
}
