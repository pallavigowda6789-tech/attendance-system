package com.example.attendance_system.repository;

import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finders
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);

    // Existence checks
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByUsernameAndIdNot(String username, Long id);

    // List queries
    List<User> findByRole(Role role);

    List<User> findByEnabled(boolean enabled);

    List<User> findByAuthProvider(AuthProvider authProvider);

    // Paginated queries
    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByEnabled(boolean enabled, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count queries
    long countByRole(Role role);

    long countByEnabled(boolean enabled);

    long countByAuthProvider(AuthProvider authProvider);

    // Recent users
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(@Param("since") LocalDateTime since);

    // Update queries
    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    int updateEnabledStatus(@Param("userId") Long userId, @Param("enabled") boolean enabled);

    @Modifying
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    int updateRole(@Param("userId") Long userId, @Param("role") Role role);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);
}
