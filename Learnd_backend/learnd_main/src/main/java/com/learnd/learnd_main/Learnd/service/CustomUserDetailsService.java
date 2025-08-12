package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.model.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.learnd.learnd_main.Learnd.repo.UserRepository;

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
