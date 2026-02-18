package com.example.attendance_system.service;

import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom implementation of UserDetailsService for Spring Security authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Attempting to load user: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                });

        if (!user.isEnabled()) {
            logger.warn("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
        }

        logger.debug("User loaded successfully: {}", user.getUsername());

        return buildUserDetails(user);
    }

    /**
     * Build Spring Security UserDetails from our User entity.
     */
    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword() != null ? user.getPassword() : "",
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getAuthority()))
        );
    }

    /**
     * Load user by ID for internal use.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return buildUserDetails(user);
    }
}
