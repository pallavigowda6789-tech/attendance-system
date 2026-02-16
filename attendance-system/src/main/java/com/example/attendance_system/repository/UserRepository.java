package com.example.attendance_system.repository;

import com.example.attendance_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional <User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User>findByProviderId(String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

}
