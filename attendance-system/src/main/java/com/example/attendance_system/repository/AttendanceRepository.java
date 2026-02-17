package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByUser(User user);
    
    List<Attendance> findByUserAndDate(User user, LocalDate date);
    
    List<Attendance> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

}
