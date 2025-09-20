package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.repo.ConsistencyLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ConsistencyService {

    private final ConsistencyLogRepository repo;

    public ConsistencyService(ConsistencyLogRepository repo) {
        this.repo = repo;
    }

//    public List<ConsistencyDayDto> getYearConsistency(Long userId) {
//        return repo.findConsistencyCalendar(userId).stream()
//                .map(row -> new ConsistencyDayDto(
//                        ((Instant) row[0]).toLocalDate(),
//                        row[1] != null ? (Boolean) row[1] : null
//                ))
//                .toList();
//    }
}
