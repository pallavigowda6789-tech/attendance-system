package com.example.attendance_system.repository;

import com.example.attendance_system.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUsernameAndDate(String username, java.time.LocalDate date);

}
