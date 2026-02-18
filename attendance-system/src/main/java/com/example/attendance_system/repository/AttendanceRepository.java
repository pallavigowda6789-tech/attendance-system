package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Attendance entity operations.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Basic finders
    List<Attendance> findByUser(User user);

    List<Attendance> findByUserOrderByDateDesc(User user);

    Optional<Attendance> findByUserAndDate(User user, LocalDate date);

    List<Attendance> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Attendance> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    List<Attendance> findByDate(LocalDate date);

    // Paginated queries
    Page<Attendance> findByUser(User user, Pageable pageable);

    Page<Attendance> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Attendance> findByDate(LocalDate date, Pageable pageable);

    @Query("SELECT a FROM Attendance a ORDER BY a.date DESC, a.checkInTime DESC")
    Page<Attendance> findAllOrderByDateDesc(Pageable pageable);

    // Existence checks
    boolean existsByUserAndDate(User user, LocalDate date);

    // Count queries
    long countByUser(User user);

    long countByUserAndPresent(User user, boolean present);

    long countByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    long countByUserAndDateBetweenAndPresent(User user, LocalDate startDate, LocalDate endDate, boolean present);

    long countByDate(LocalDate date);

    long countByDateAndPresent(LocalDate date, boolean present);

    // Statistics queries
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user AND a.present = true AND a.date BETWEEN :startDate AND :endDate")
    long countPresentDays(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user = :user AND a.present = false AND a.date BETWEEN :startDate AND :endDate")
    long countAbsentDays(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // User attendance by ID
    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId ORDER BY a.date DESC")
    List<Attendance> findByUserIdOrderByDateDesc(@Param("userId") Long userId);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId")
    Page<Attendance> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<Attendance> findByUserIdAndDateBetween(@Param("userId") Long userId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);

    // Delete queries
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.user = :user")
    int deleteByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    // Recent attendance
    @Query("SELECT a FROM Attendance a WHERE a.date >= :since ORDER BY a.date DESC, a.checkInTime DESC")
    List<Attendance> findRecentAttendance(@Param("since") LocalDate since);

    // Attendance for today
    @Query("SELECT a FROM Attendance a WHERE a.date = CURRENT_DATE ORDER BY a.checkInTime DESC")
    List<Attendance> findTodayAttendance();

    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.date = CURRENT_DATE")
    Optional<Attendance> findTodayAttendanceForUser(@Param("user") User user);
}
