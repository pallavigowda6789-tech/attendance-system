package com.example.attendance_system.config;

import com.example.attendance_system.entity.Attendance;
import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.AttendanceRepository;
import com.example.attendance_system.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Database seeder for initial data population.
 */
@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private static final String DEFAULT_PASSWORD = "password123";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final int ATTENDANCE_HISTORY_DAYS = 60;
    private static final double PRESENCE_PROBABILITY = 0.85;

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   AttendanceRepository attendanceRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Fix existing attendance records missing created_at
            fixAttendanceCreatedAt();

            if (userRepository.count() > 0) {
                logger.info("Database already contains data. Skipping seeding.");
                return;
            }

            logger.info("Starting database seeding...");

            // Create admin user
            User admin = createUser("admin", "admin@attendance.com", ADMIN_PASSWORD,
                    "System", "Administrator", Role.ADMIN, passwordEncoder);
            userRepository.save(admin);
            logger.info("✓ Created admin user: admin / {}", ADMIN_PASSWORD);

            // Create regular users
            List<User> users = createRegularUsers(userRepository, passwordEncoder);

            // Generate attendance records
            int recordsCreated = generateAttendanceRecords(attendanceRepository, users);

            logCompletionMessage(users.size(), recordsCreated);
        };
    }

    /**
     * Fix existing attendance records that have NULL created_at values.
     * This handles migration from older schema versions.
     */
    @Transactional
    public void fixAttendanceCreatedAt() {
        try {
            // Use id > 0 to satisfy MySQL safe update mode requirement
            String updateQuery = "UPDATE attendance SET created_at = COALESCE(check_in_time, NOW()) WHERE created_at IS NULL AND id > 0";
            int updated = entityManager.createNativeQuery(updateQuery).executeUpdate();
            if (updated > 0) {
                logger.info("✓ Fixed {} attendance records with missing created_at values", updated);
            }
        } catch (Exception e) {
            logger.debug("Note: Could not update created_at column (may not exist yet): {}", e.getMessage());
        }
    }

    private List<User> createRegularUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
            User user = createUser(userData[0], userData[3], DEFAULT_PASSWORD,
                    userData[1], userData[2], Role.USER, passwordEncoder);
            users.add(userRepository.save(user));
            logger.info("✓ Created user: {} / {}", userData[0], DEFAULT_PASSWORD);
        }

        return users;
    }

    private User createUser(String username, String email, String password,
                            String firstName, String lastName, Role role,
                            PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        user.setAuthProvider(AuthProvider.LOCAL);
        return user;
    }

    private int generateAttendanceRecords(AttendanceRepository attendanceRepository, List<User> users) {
        Random random = new Random();
        int recordsCreated = 0;
        LocalDate today = LocalDate.now();

        for (User user : users) {
            for (int i = 0; i < ATTENDANCE_HISTORY_DAYS; i++) {
                LocalDate date = today.minusDays(i);

                // Skip weekends
                if (isWeekend(date)) {
                    continue;
                }

                boolean isPresent = random.nextDouble() < PRESENCE_PROBABILITY;
                Attendance attendance = createAttendanceRecord(user, date, isPresent, random);
                attendanceRepository.save(attendance);
                recordsCreated++;
            }
        }

        return recordsCreated;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private Attendance createAttendanceRecord(User user, LocalDate date, boolean present, Random random) {
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(date);
        attendance.setPresent(present);

        if (present) {
            // Random check-in time between 8:00 and 10:00
            int hour = 8 + random.nextInt(2);
            int minute = random.nextInt(60);
            LocalDateTime checkIn = LocalDateTime.of(date, LocalTime.of(hour, minute));
            attendance.setCheckInTime(checkIn);

            // Random check-out time between 17:00 and 19:00
            int checkOutHour = 17 + random.nextInt(2);
            int checkOutMinute = random.nextInt(60);
            LocalDateTime checkOut = LocalDateTime.of(date, LocalTime.of(checkOutHour, checkOutMinute));
            attendance.setCheckOutTime(checkOut);
        }

        return attendance;
    }

    private void logCompletionMessage(int userCount, int recordsCreated) {
        logger.info("✓ Created {} attendance records for {} users", recordsCreated, userCount);
        logger.info("=".repeat(60));
        logger.info("DATABASE SEEDING COMPLETED SUCCESSFULLY!");
        logger.info("=".repeat(60));
        logger.info("Login Credentials:");
        logger.info("  Admin:  admin / {}", ADMIN_PASSWORD);
        logger.info("  Users:  any username above / {}", DEFAULT_PASSWORD);
        logger.info("=".repeat(60));
    }
}
