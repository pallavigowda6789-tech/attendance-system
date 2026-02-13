package com.example.attendance_system.service;


import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;


@Service
public class AttendanceService {

    private final AttendanceRepository repo;

    public AttendanceService(AttendanceRepository repo) {
        this.repo = repo;
    }

    public Attendance markAttendance(String username) {
        Attendance attendance = new Attendance();
        attendance.setUsername(username);
        attendance.setDate(LocalDate.now());
        attendance.setPresent(true);
        return repo.save(attendance);
    }
}
