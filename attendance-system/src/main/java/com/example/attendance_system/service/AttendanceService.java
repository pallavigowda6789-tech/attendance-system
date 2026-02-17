package com.example.attendance_system.service;


import com.example.attendance_system.dto.AttendanceDTO;
import com.example.attendance_system.dto.AttendanceStatsDTO;
import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AttendanceService {

    private final AttendanceRepository repo;
    private final UserRepository userRepository;
    private final UserService userService;

    public AttendanceService(AttendanceRepository repo, UserRepository userRepository, UserService userService) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public Attendance markAttendance(String username) {
        // Legacy method - deprecated, kept for backward compatibility
        // Use markAttendanceByUserId instead
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(LocalDate.now());
        attendance.setPresent(true);
        return repo.save(attendance);
    }

    public List<Attendance> getAllAttendance() {
        return repo.findAll();
    }

    public AttendanceDTO markAttendanceByUserId(Long userId, LocalDate date, boolean present) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if attendance already exists
        List<Attendance> existing = repo.findByUserAndDate(user, date);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Attendance already marked for this date");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(date);
        attendance.setPresent(present);
        
        Attendance saved = repo.save(attendance);
        return convertToDTO(saved);
    }

    public AttendanceDTO markAttendanceForCurrentUser(boolean present) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        return markAttendanceByUserId(currentUser.getId(), LocalDate.now(), present);
    }

    public List<AttendanceDTO> getAttendanceByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repo.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AttendanceDTO> getCurrentUserAttendance() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        return getAttendanceByUser(currentUser.getId());
    }

    public List<AttendanceDTO> getAttendanceByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repo.findByUserAndDateBetween(user, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AttendanceStatsDTO getAttendanceStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());

        List<Attendance> records = repo.findByUserAndDateBetween(user, startOfMonth, endOfMonth);
        
        long totalDays = records.size();
        long presentDays = records.stream().filter(Attendance::isPresent).count();
        long absentDays = totalDays - presentDays;
        double percentage = totalDays > 0 ? (double) presentDays / totalDays * 100 : 0.0;

        return new AttendanceStatsDTO(totalDays, presentDays, absentDays, percentage, startOfMonth, endOfMonth);
    }

    public AttendanceStatsDTO getCurrentUserStats() {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        return getAttendanceStats(currentUser.getId());
    }
    
    public List<AttendanceDTO> getCurrentUserAttendanceByDateRange(LocalDate startDate, LocalDate endDate) {
        User currentUser = userService.getCurrentUserEntity();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        return getAttendanceByDateRange(currentUser.getId(), startDate, endDate);
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        return new AttendanceDTO(
                attendance.getId(),
                attendance.getUser().getId(),
                attendance.getUser().getUsername(),
                attendance.getDate(),
                attendance.isPresent(),
                attendance.getTimestamp()
        );
    }
}
