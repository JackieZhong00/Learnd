package com.learnd.learnd_main.Learnd.controller;


import com.learnd.learnd_main.Learnd.model.ConsistencyDayDto;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import com.learnd.learnd_main.Learnd.service.ConsistencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consistency")
public class ConsistencyController {

    private final ConsistencyService service;
    private final UserRepository userRepository;

    public ConsistencyController(ConsistencyService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping("/year")
    public ResponseEntity<List<ConsistencyDayDto>> getConsistencyYear() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<ConsistencyDayDto> days = service.getCurrentYearConsistency((long)user.getId());
        return ResponseEntity.ok(days);
    }
}

