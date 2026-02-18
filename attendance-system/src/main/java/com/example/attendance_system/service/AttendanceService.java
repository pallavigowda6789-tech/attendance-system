package com.example.attendance_system.service;

import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.dto.PagedResponse;
import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.exception.DuplicateResourceException;
import com.example.attendance_system.exception.InvalidOperationException;
import com.example.attendance_system.exception.ResourceNotFoundException;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for attendance management operations.
 */
@Service
@Transactional
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             UserRepository userRepository,
                             UserService userService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Mark attendance for the current user.
     */
    public AttendanceDTO markAttendanceForCurrentUser(boolean present) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        return markAttendanceByUserId(currentUser.getId(), LocalDate.now(), present);
    }

    /**
     * Mark attendance for a specific user.
     */
    public AttendanceDTO markAttendanceByUserId(Long userId, LocalDate date, boolean present) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if attendance already exists
        if (attendanceRepository.existsByUserAndDate(user, date)) {
            throw new DuplicateResourceException("Attendance already marked for this date");
        }

        Attendance attendance = new Attendance(user, date, present);
        attendance.setCheckInTime(LocalDateTime.now());

        Attendance saved = attendanceRepository.save(attendance);
        logger.info("Attendance marked for user {} on {}: {}", user.getUsername(), date, present ? "Present" : "Absent");
        return AttendanceDTO.fromEntity(saved);
    }

    /**
     * Mark attendance (legacy method for backward compatibility).
     */
    @Deprecated
    public Attendance markAttendance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Attendance attendance = new Attendance(user, LocalDate.now(), true);
        return attendanceRepository.save(attendance);
    }

    /**
     * Check out for today's attendance.
     */
    public AttendanceDTO checkOut(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Attendance attendance = attendanceRepository.findByUserAndDate(user, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("No attendance record found for today"));

        if (attendance.getCheckOutTime() != null) {
            throw new InvalidOperationException("Already checked out for today");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        Attendance saved = attendanceRepository.save(attendance);
        logger.info("Check-out recorded for user {} at {}", user.getUsername(), saved.getCheckOutTime());
        return AttendanceDTO.fromEntity(saved);
    }

    /**
     * Get all attendance records.
     */
    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    /**
     * Get attendance for a specific user.
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return attendanceRepository.findByUserOrderByDateDesc(user).stream()
                .map(AttendanceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get current user's attendance.
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getCurrentUserAttendance() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        return getAttendanceByUser(currentUser.getId());
    }

    /**
     * Get attendance by date range.
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return attendanceRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate).stream()
                .map(AttendanceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get current user's attendance by date range.
     */
    @Transactional(readOnly = true)
    public List<AttendanceDTO> getCurrentUserAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        return getAttendanceByDateRange(currentUser.getId(), startDate, endDate);
    }

    /**
     * Get paginated attendance for current user.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceDTO> getCurrentUserAttendancePaginated(int page, int size, 
                                                                           LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }

        List<AttendanceDTO> allRecords;
        if (startDate != null && endDate != null) {
            allRecords = getAttendanceByDateRange(currentUser.getId(), startDate, endDate);
        } else {
            allRecords = getAttendanceByUser(currentUser.getId());
        }

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allRecords.size());
        List<AttendanceDTO> pageRecords = start < allRecords.size()
                ? allRecords.subList(start, end)
                : List.of();

        return PagedResponse.of(pageRecords, page, size, allRecords.size());
    }

    /**
     * Get attendance statistics for a user.
     */
    @Transactional(readOnly = true)
    public AttendanceStatsDTO getAttendanceStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        return calculateStats(user, startOfMonth, endOfMonth);
    }

    /**
     * Get attendance statistics for current user.
     */
    @Transactional(readOnly = true)
    public AttendanceStatsDTO getCurrentUserStats() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new InvalidOperationException("User not authenticated");
        }
        return getAttendanceStats(currentUser.getId());
    }

    /**
     * Get attendance statistics for a date range.
     */
    @Transactional(readOnly = true)
    public AttendanceStatsDTO getAttendanceStats(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return calculateStats(user, startDate, endDate);
    }

    /**
     * Calculate attendance statistics.
     */
    private AttendanceStatsDTO calculateStats(User user, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = attendanceRepository.findByUserAndDateBetween(user, startDate, endDate);

        long totalDays = records.size();
        long presentDays = records.stream().filter(Attendance::isPresent).count();
        long absentDays = totalDays - presentDays;
        double percentage = totalDays > 0 ? (double) presentDays / totalDays * 100 : 0.0;

        return AttendanceStatsDTO.builder()
                .totalDays(totalDays)
                .presentDays(presentDays)
                .absentDays(absentDays)
                .attendancePercentage(percentage)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    /**
     * Get all attendance records (admin).
     */
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceDTO> getAllAttendancePaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Attendance> pageResult = attendanceRepository.findAllOrderByDateDesc(pageable);

        List<AttendanceDTO> content = pageResult.getContent().stream()
                .map(AttendanceDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(content, page, size, pageResult.getTotalElements());
    }

    /**
     * Update attendance notes.
     */
    public AttendanceDTO updateAttendanceNotes(Long attendanceId, String notes) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));

        attendance.setNotes(notes);
        Attendance saved = attendanceRepository.save(attendance);
        return AttendanceDTO.fromEntity(saved);
    }

    /**
     * Delete attendance record.
     */
    public void deleteAttendance(Long attendanceId) {
        if (!attendanceRepository.existsById(attendanceId)) {
            throw new ResourceNotFoundException("Attendance", "id", attendanceId);
        }
        attendanceRepository.deleteById(attendanceId);
        logger.info("Attendance record deleted: {}", attendanceId);
    }

    /**
     * Get today's attendance for a user.
     */
    @Transactional(readOnly = true)
    public AttendanceDTO getTodayAttendance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return attendanceRepository.findTodayAttendanceForUser(user)
                .map(AttendanceDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Check if attendance is marked for today.
     */
    @Transactional(readOnly = true)
    public boolean hasMarkedAttendanceToday(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return attendanceRepository.existsByUserAndDate(user, LocalDate.now());
    }
}
