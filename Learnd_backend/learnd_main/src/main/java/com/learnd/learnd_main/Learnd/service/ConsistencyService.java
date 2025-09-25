package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.ConsistencyDayDto;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.model.UserConsistencyLog;
import com.learnd.learnd_main.Learnd.repo.ConsistencyLogRepository;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsistencyService {

    private final UserRepository userRepository;
    private final FlashcardRepository flashcardRepository;
    private final ConsistencyLogRepository consistencyLogRepository;

    public ConsistencyService(UserRepository userRepository, FlashcardRepository flashcardRepository, ConsistencyLogRepository consistencyLogRepository) {
        this.userRepository = userRepository;
        this.flashcardRepository = flashcardRepository;
        this.consistencyLogRepository = consistencyLogRepository;
    }

    void createAndSaveLog(User user, LocalDate today, Boolean isConsistent) {
        UserConsistencyLog log = new UserConsistencyLog();
        log.setUser(user);
        log.setDate(today);
        log.setConsistent(false);
        consistencyLogRepository.save(log);
    }

    // Runs every day at 11:50pm UTC
    @Scheduled(cron = "0 50 23 * * *", zone = "UTC")
    @Transactional
    public void checkUserConsistency() {
        List<User> users = userRepository.findAll();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        for (User user : users) {

            //Step 1: query cards with user id and today's date -> mark as past due
            int numRowsUpdated = flashcardRepository.markPastDueByUserAndDate(user.getId(), today);
            if (numRowsUpdated > 0) {
                createAndSaveLog(user, today, false);
                continue;
            }

            // Step 2: if no cards were due today, check if cards due prior are still past due
            int pastDueCount = flashcardRepository.countByUser_IdAndPastDueTrue(user.getId());

            boolean isConsistent = pastDueCount == 0;

            // Step 3: Create consistency log for today
            createAndSaveLog(user, today, isConsistent);
        }
    }

    public List<ConsistencyDayDto> getCurrentYearConsistency(Long userId) {
        List<Object[]> rows = consistencyLogRepository.findConsistencyCalendar(userId);

        return rows.stream()
                .map(row -> {
                    Date sqlDate = (Date) row[0]; // java.sql.Date
                    Boolean isConsistent = (Boolean) row[1];
                    LocalDate date = sqlDate.toLocalDate();
                    return new ConsistencyDayDto(date, isConsistent != null && isConsistent);
                })
                .collect(Collectors.toList());
    }
}

