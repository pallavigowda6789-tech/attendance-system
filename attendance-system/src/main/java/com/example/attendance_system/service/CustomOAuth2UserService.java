package com.example.attendance_system.service;

import com.example.attendance_system.entity.AuthProvider;
import com.example.attendance_system.entity.Role;
import com.example.attendance_system.entity.User;
import com.example.attendance_system.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauth2User =new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String providerId = oauth2User.getAttribute("sub");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        if( email==null){
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        Optional<User> existingUser = userRepository.findByEmail(email);

       if (existingUser.isEmpty() ){
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setRole(Role.USER);
            newUser.setEnabled(true);
            newUser.setAuthProvider(AuthProvider.SSO);
            newUser.setProviderId(providerId);
            newUser.setRole(Role.USER);
            newUser.setCreatedAt(LocalDateTime.now());
            userRepository.save(newUser);

        }
        System.out.println("OAuth2 user loaded: " + email);

        return oauth2User;

    }
}