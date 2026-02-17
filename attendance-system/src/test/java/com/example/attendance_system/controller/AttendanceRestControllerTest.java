package com.example.attendance_system.controller;

import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
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
class AttendanceRestControllerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceRestController attendanceRestController;

    private AttendanceDTO attendanceDTO;
    private AttendanceStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        attendanceDTO = new AttendanceDTO(1L, 1L, "testuser", LocalDate.now(), true, LocalDateTime.now());
        statsDTO = new AttendanceStatsDTO(10L, 8L, 2L, 80.0, 
                LocalDate.now().withDayOfMonth(1), 
                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
    }

    @Test
    void testGetMyStats() {
        when(attendanceService.getCurrentUserStats()).thenReturn(statsDTO);

        ResponseEntity<AttendanceStatsDTO> response = attendanceRestController.getMyStats();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(10L, response.getBody().getTotalDays());
        assertEquals(8L, response.getBody().getPresentDays());
        verify(attendanceService, times(1)).getCurrentUserStats();
    }

    @Test
    void testGetMyRecords() {
        List<AttendanceDTO> records = Arrays.asList(attendanceDTO);
        when(attendanceService.getCurrentUserAttendance()).thenReturn(records);

        ResponseEntity<?> response = attendanceRestController.getMyRecords(0, 10, null, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(attendanceService, times(1)).getCurrentUserAttendance();
    }

    @Test
    void testMarkAttendance() {
        when(attendanceService.markAttendanceForCurrentUser(anyBoolean())).thenReturn(attendanceDTO);

        Map<String, Boolean> request = new HashMap<>();
        request.put("present", true);

        ResponseEntity<?> response = attendanceRestController.markAttendance(request);

        assertEquals(200, response.getStatusCode().value());
        verify(attendanceService, times(1)).markAttendanceForCurrentUser(true);
    }

    @Test
    void testMarkAttendance_AlreadyMarked() {
        when(attendanceService.markAttendanceForCurrentUser(anyBoolean()))
                .thenThrow(new RuntimeException("Attendance already marked for this date"));

        Map<String, Boolean> request = new HashMap<>();
        request.put("present", true);

        ResponseEntity<?> response = attendanceRestController.markAttendance(request);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void testGetMyStats_Error() {
        when(attendanceService.getCurrentUserStats()).thenThrow(new RuntimeException("Error"));

        ResponseEntity<AttendanceStatsDTO> response = attendanceRestController.getMyStats();

        assertEquals(400, response.getStatusCode().value());
    }
}
