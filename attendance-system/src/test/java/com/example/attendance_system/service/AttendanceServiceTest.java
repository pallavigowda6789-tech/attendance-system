package com.example.attendance_system.service;

import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    }

    @Test
    void testMarkAttendanceByUserId_Success() {
        LocalDate date = LocalDate.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserAndDate(testUser, date)).thenReturn(new ArrayList<>());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        AttendanceDTO result = attendanceService.markAttendanceByUserId(1L, date, true);

        assertNotNull(result);
        assertTrue(result.isPresent());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendanceByUserId_AlreadyMarked() {
        LocalDate date = LocalDate.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserAndDate(testUser, date))
                .thenReturn(Arrays.asList(testAttendance));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.markAttendanceByUserId(1L, date, true);
        });

        assertEquals("Attendance already marked for this date", exception.getMessage());
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void testMarkAttendanceByUserId_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attendanceService.markAttendanceByUserId(1L, LocalDate.now(), true);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAttendanceByUser() {
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setUser(testUser);
        attendance2.setDate(LocalDate.now().minusDays(1));
        attendance2.setPresent(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUser(testUser))
                .thenReturn(Arrays.asList(testAttendance, attendance2));

        List<AttendanceDTO> results = attendanceService.getAttendanceByUser(1L);

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(attendanceRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testGetAttendanceByDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserAndDateBetween(testUser, startDate, endDate))
                .thenReturn(Arrays.asList(testAttendance));

        List<AttendanceDTO> results = attendanceService.getAttendanceByDateRange(1L, startDate, endDate);

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(attendanceRepository, times(1)).findByUserAndDateBetween(testUser, startDate, endDate);
    }

    @Test
    void testGetAttendanceStats() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setUser(testUser);
        attendance2.setDate(LocalDate.now().minusDays(1));
        attendance2.setPresent(true);

        Attendance attendance3 = new Attendance();
        attendance3.setId(3L);
        attendance3.setUser(testUser);
        attendance3.setDate(LocalDate.now().minusDays(2));
        attendance3.setPresent(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testAttendance, attendance2, attendance3));

        AttendanceStatsDTO stats = attendanceService.getAttendanceStats(1L);

        assertNotNull(stats);
        assertEquals(3, stats.getTotalDays());
        assertEquals(2, stats.getPresentDays());
        assertEquals(1, stats.getAbsentDays());
        assertTrue(stats.getAttendancePercentage() > 66.0 && stats.getAttendancePercentage() < 67.0);
    }

    @Test
    void testGetAttendanceStats_NoRecords() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(attendanceRepository.findByUserAndDateBetween(any(), any(), any()))
                .thenReturn(new ArrayList<>());

        AttendanceStatsDTO stats = attendanceService.getAttendanceStats(1L);

        assertNotNull(stats);
        assertEquals(0, stats.getTotalDays());
        assertEquals(0, stats.getPresentDays());
        assertEquals(0, stats.getAbsentDays());
        assertEquals(0.0, stats.getAttendancePercentage());
    }
}
