package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private UserRepository userRepository;

    public CustomOAuth2Service(UserRepository repo) {
        this.userRepository = repo;
    }

    public OAuth2User loadUser (OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Extract user information from the OAuth2User
        String email = oauth2User.getAttribute("email");

        // Check if user already exists in the database
        Optional<User> existingUser = userRepository.findByEmail(email);

        existingUser.ifPresent(fetchedUser -> {
            User newUser = new User();
            newUser.setEmail(email);
            userRepository.save(newUser);
        });

        // Return the OAuth2User (can also add roles or authorities here if needed)
        return oauth2User;
    }
}
