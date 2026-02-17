package com.example.attendance_system.config;

import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DataSeeder {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   AttendanceRepository attendanceRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if data already exists
            if (userRepository.count() > 0) {
                logger.info("Database already contains data. Skipping seeding.");
                return;
            }
            
            logger.info("Seeding database with initial data...");
            
            // Create Admin User
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@attendance.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setAuthProvider(AuthProvider.LOCAL);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            logger.info("✓ Created admin user: admin / admin123");
            
            // Create Regular Users
            List<User> users = new ArrayList<>();
            
            String[][] usersData = {
                {"john.doe", "John", "Doe", "john.doe@company.com"},
                {"jane.smith", "Jane", "Smith", "jane.smith@company.com"},
                {"mike.wilson", "Mike", "Wilson", "mike.wilson@company.com"},
                {"sarah.jones", "Sarah", "Jones", "sarah.jones@company.com"},
                {"david.brown", "David", "Brown", "david.brown@company.com"},
                {"emma.davis", "Emma", "Davis", "emma.davis@company.com"},
                {"james.miller", "James", "Miller", "james.miller@company.com"},
                {"olivia.garcia", "Olivia", "Garcia", "olivia.garcia@company.com"}
            };
            
            for (String[] userData : usersData) {
                User user = new User();
                user.setUsername(userData[0]);
                user.setFirstName(userData[1]);
                user.setLastName(userData[2]);
                user.setEmail(userData[3]);
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole(Role.USER);
                user.setEnabled(true);
                user.setAuthProvider(AuthProvider.LOCAL);
                user.setCreatedAt(LocalDateTime.now());
                users.add(userRepository.save(user));
                logger.info("✓ Created user: {} / password123", userData[0]);
            }
            
            // Generate attendance records for the last 60 days
            Random random = new Random();
            int recordsCreated = 0;
            
            for (User user : users) {
                LocalDate currentDate = LocalDate.now();
                
                // Create attendance for last 60 days
                for (int i = 0; i < 60; i++) {
                    LocalDate date = currentDate.minusDays(i);
                    
                    // Skip weekends (Saturday = 6, Sunday = 7)
                    int dayOfWeek = date.getDayOfWeek().getValue();
                    if (dayOfWeek == 6 || dayOfWeek == 7) {
                        continue;
                    }
                    
                    // 85% chance of being present
                    boolean isPresent = random.nextDouble() < 0.85;
                    
                    Attendance attendance = new Attendance();
                    attendance.setUser(user);
                    attendance.setDate(date);
                    attendance.setPresent(isPresent);
                    
                    // Set realistic timestamp (between 8 AM and 10 AM)
                    LocalDateTime timestamp = date.atTime(8 + random.nextInt(2), random.nextInt(60));
                    attendance.setTimestamp(timestamp);
                    
                    attendanceRepository.save(attendance);
                    recordsCreated++;
                }
            }
            
            logger.info("✓ Created {} attendance records for {} users", recordsCreated, users.size());
            logger.info("=".repeat(60));
            logger.info("DATABASE SEEDING COMPLETED SUCCESSFULLY!");
            logger.info("=".repeat(60));
            logger.info("Login Credentials:");
            logger.info("  Admin:  admin / admin123");
            logger.info("  Users:  any username above / password123");
            logger.info("=".repeat(60));
        };
    }
}
