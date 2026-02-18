package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Leave;
import com.example.attendance_system.entity.LeaveStatus;
import com.example.attendance_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Leave entity.
 */
@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // Find by user
    List<Leave> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Leave> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find by status
    List<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status);
    
    Page<Leave> findByStatusOrderByCreatedAtDesc(LeaveStatus status, Pageable pageable);

    // Find by user and status
    List<Leave> findByUserAndStatusOrderByCreatedAtDesc(User user, LeaveStatus status);

    // Find pending leaves for approval
    @Query("SELECT l FROM Leave l WHERE l.status = :status ORDER BY l.createdAt DESC")
    Page<Leave> findPendingLeaves(@Param("status") LeaveStatus status, Pageable pageable);

    // Find leaves by date range
    @Query("SELECT l FROM Leave l WHERE l.user = :user AND " +
           "((l.startDate BETWEEN :start AND :end) OR (l.endDate BETWEEN :start AND :end) OR " +
           "(l.startDate <= :start AND l.endDate >= :end))")
    List<Leave> findByUserAndDateRange(@Param("user") User user, 
                                       @Param("start") LocalDate start, 
                                       @Param("end") LocalDate end);

    // Check for overlapping leaves
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Leave l WHERE l.user = :user AND " +
           "l.status != 'REJECTED' AND l.status != 'CANCELLED' AND " +
           "((l.startDate BETWEEN :start AND :end) OR (l.endDate BETWEEN :start AND :end) OR " +
           "(l.startDate <= :start AND l.endDate >= :end))")
    boolean hasOverlappingLeave(@Param("user") User user, 
                                @Param("start") LocalDate start, 
                                @Param("end") LocalDate end);

    // Count leaves by user and status
    long countByUserAndStatus(User user, LeaveStatus status);

    // Count pending leaves (for badge)
    long countByStatus(LeaveStatus status);

    // Find all with pagination
    @Query("SELECT l FROM Leave l ORDER BY l.createdAt DESC")
    Page<Leave> findAllOrderByCreatedAtDesc(Pageable pageable);

    // Count total leave days for user in a year
    @Query("SELECT COALESCE(SUM(DATEDIFF(l.endDate, l.startDate) + 1), 0) FROM Leave l " +
           "WHERE l.user = :user AND l.status = 'APPROVED' AND YEAR(l.startDate) = :year")
    long countApprovedLeaveDaysForYear(@Param("user") User user, @Param("year") int year);
}
