package com.StudyAccell.StudyAccell.service;

import com.StudyAccell.StudyAccell.model.User;
import com.StudyAccell.StudyAccell.model.UserPrincipal;
import com.StudyAccell.StudyAccell.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Email: " + email);
        Optional<User> user = userRepository.findByEmail(email);
        return new UserPrincipal(user.orElseThrow(() -> new UsernameNotFoundException(email)));
    }
}
