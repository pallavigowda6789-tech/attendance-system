package com.example.attendance_system.service;

import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service for handling OAuth2 authentication providers.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        logger.info("Processing OAuth2 login from provider: {}", registrationId);

        try {
            return processOAuth2User(registrationId, oauth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 user: {}", ex.getMessage());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("processing_error", ex.getMessage(), null));
        }
    }

    private OAuth2User processOAuth2User(String registrationId, OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String email = extractEmail(attributes);
        String providerId = extractProviderId(attributes);
        String firstName = extractFirstName(attributes);
        String lastName = extractLastName(attributes);
        String name = extractName(attributes);

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found", "Email not found from OAuth2 provider", null));
        }

        AuthProvider authProvider = determineAuthProvider(registrationId);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            updateExistingUser(user, firstName, lastName, providerId, authProvider);
            logger.info("Updated existing user from OAuth2: {}", email);
        } else {
            createNewOAuth2User(email, firstName, lastName, name, providerId, authProvider);
            logger.info("Created new user from OAuth2: {}", email);
        }

        return oauth2User;
    }

    private String extractEmail(Map<String, Object> attributes) {
        return (String) attributes.get("email");
    }

    private String extractProviderId(Map<String, Object> attributes) {
        // Google uses "sub", GitHub uses "id"
        Object sub = attributes.get("sub");
        if (sub != null) return sub.toString();
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    private String extractFirstName(Map<String, Object> attributes) {
        return (String) attributes.get("given_name");
    }

    private String extractLastName(Map<String, Object> attributes) {
        return (String) attributes.get("family_name");
    }

    private String extractName(Map<String, Object> attributes) {
        return (String) attributes.get("name");
    }

    private AuthProvider determineAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> AuthProvider.GOOGLE;
        };
    }

    private void updateExistingUser(User user, String firstName, String lastName, 
                                     String providerId, AuthProvider authProvider) {
        boolean updated = false;

        if (firstName != null && !firstName.equals(user.getFirstName())) {
            user.setFirstName(firstName);
            updated = true;
        }
        if (lastName != null && !lastName.equals(user.getLastName())) {
            user.setLastName(lastName);
            updated = true;
        }
        if (providerId != null && !providerId.equals(user.getProviderId())) {
            user.setProviderId(providerId);
            updated = true;
        }
        if (!authProvider.equals(user.getAuthProvider())) {
            user.setAuthProvider(authProvider);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    private void createNewOAuth2User(String email, String firstName, String lastName,
                                      String name, String providerId, AuthProvider authProvider) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(generateUsername(email));
        
        // Use name components or full name
        if (firstName != null) {
            newUser.setFirstName(firstName);
        } else if (name != null) {
            String[] nameParts = name.split(" ", 2);
            newUser.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                newUser.setLastName(nameParts[1]);
            }
        }
        
        if (lastName != null) {
            newUser.setLastName(lastName);
        }

        newUser.setRole(Role.USER);
        newUser.setEnabled(true);
        newUser.setAuthProvider(authProvider);
        newUser.setProviderId(providerId);

        userRepository.save(newUser);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }
}