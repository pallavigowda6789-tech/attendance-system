package com.example.attendance_system.controller;

import com.example.attendance_system.dto.ApiResponse;
import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.exception.DuplicateResourceException;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.service.AttendanceService;
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
@DisplayName("AttendanceRestController Tests")
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

    @Nested
    @DisplayName("Get Stats Tests")
    class GetStatsTests {

        @Test
        @DisplayName("Should get my stats successfully")
        void getMyStats_Success() {
            when(attendanceService.getCurrentUserStats()).thenReturn(statsDTO);

            ResponseEntity<ApiResponse<AttendanceStatsDTO>> response = attendanceRestController.getMyStats();

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals(10L, response.getBody().getData().getTotalDays());
            assertEquals(8L, response.getBody().getData().getPresentDays());
            verify(attendanceService, times(1)).getCurrentUserStats();
        }
    }

    @Nested
    @DisplayName("Get Records Tests")
    class GetRecordsTests {

        @Test
        @DisplayName("Should get my records with pagination")
        void getMyRecords_Success() {
            List<AttendanceDTO> records = Arrays.asList(attendanceDTO);
            PagedResponse<AttendanceDTO> pagedResponse = PagedResponse.of(records, 0, 10, 1);
            when(attendanceService.getCurrentUserAttendancePaginated(0, 10, null, null))
                    .thenReturn(pagedResponse);

            ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> response =
                    attendanceRestController.getMyRecords(0, 10, null, null);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            verify(attendanceService, times(1)).getCurrentUserAttendancePaginated(0, 10, null, null);
        }

        @Test
        @DisplayName("Should get my records with date filter")
        void getMyRecords_WithDateFilter() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            List<AttendanceDTO> records = Arrays.asList(attendanceDTO);
            PagedResponse<AttendanceDTO> pagedResponse = PagedResponse.of(records, 0, 10, 1);

            when(attendanceService.getCurrentUserAttendancePaginated(0, 10, startDate, endDate))
                    .thenReturn(pagedResponse);

            ResponseEntity<ApiResponse<PagedResponse<AttendanceDTO>>> response =
                    attendanceRestController.getMyRecords(0, 10, startDate, endDate);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
        }
    }

    @Nested
    @DisplayName("Mark Attendance Tests")
    class MarkAttendanceTests {

        @Test
        @DisplayName("Should mark attendance successfully")
        void markAttendance_Success() {
            when(attendanceService.markAttendanceForCurrentUser(anyBoolean())).thenReturn(attendanceDTO);

            Map<String, Object> request = new HashMap<>();
            request.put("present", true);

            ResponseEntity<ApiResponse<AttendanceDTO>> response = attendanceRestController.markAttendance(request);

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals("Attendance marked successfully", response.getBody().getMessage());
            verify(attendanceService, times(1)).markAttendanceForCurrentUser(true);
        }

        @Test
        @DisplayName("Should mark attendance as absent")
        void markAttendance_Absent() {
            when(attendanceService.markAttendanceForCurrentUser(false)).thenReturn(attendanceDTO);

            Map<String, Object> request = new HashMap<>();
            request.put("present", false);

            ResponseEntity<ApiResponse<AttendanceDTO>> response = attendanceRestController.markAttendance(request);

            assertEquals(200, response.getStatusCode().value());
            verify(attendanceService, times(1)).markAttendanceForCurrentUser(false);
        }

        @Test
        @DisplayName("Should default to present when no body provided")
        void markAttendance_DefaultToPresent() {
            when(attendanceService.markAttendanceForCurrentUser(true)).thenReturn(attendanceDTO);

            ResponseEntity<ApiResponse<AttendanceDTO>> response = attendanceRestController.markAttendance(null);

            assertEquals(200, response.getStatusCode().value());
            verify(attendanceService, times(1)).markAttendanceForCurrentUser(true);
        }
    }

    @Nested
    @DisplayName("Get User Attendance Tests")
    class GetUserAttendanceTests {

        @Test
        @DisplayName("Should get attendance for specific user")
        void getUserAttendance_Success() {
            List<AttendanceDTO> records = Arrays.asList(attendanceDTO);
            when(attendanceService.getAttendanceByUser(1L)).thenReturn(records);

            ResponseEntity<ApiResponse<List<AttendanceDTO>>> response =
                    attendanceRestController.getUserAttendance(1L);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().size());
        }

        @Test
        @DisplayName("Should get attendance by date range")
        void getAttendanceByRange_Success() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            List<AttendanceDTO> records = Arrays.asList(attendanceDTO);

            when(attendanceService.getAttendanceByDateRange(1L, startDate, endDate)).thenReturn(records);

            ResponseEntity<ApiResponse<List<AttendanceDTO>>> response =
                    attendanceRestController.getAttendanceByRange(1L, startDate, endDate);

            assertNotNull(response);
            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().isSuccess());
        }
    }

    @Nested
    @DisplayName("Today's Attendance Tests")
    class TodayAttendanceTests {

        @Test
        @DisplayName("Should check today's attendance status")
        void checkTodayStatus_NotMarked() {
            when(attendanceService.getCurrentUserAttendance()).thenReturn(List.of());

            ResponseEntity<ApiResponse<Map<String, Boolean>>> response =
                    attendanceRestController.checkTodayStatus();

            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().getData().get("marked"));
        }

        @Test
        @DisplayName("Should return marked=true when attendance exists for today")
        void checkTodayStatus_Marked() {
            when(attendanceService.getCurrentUserAttendance()).thenReturn(Arrays.asList(attendanceDTO));

            ResponseEntity<ApiResponse<Map<String, Boolean>>> response =
                    attendanceRestController.checkTodayStatus();

            assertEquals(200, response.getStatusCode().value());
            assertTrue(response.getBody().getData().get("marked"));
        }
    }
}
