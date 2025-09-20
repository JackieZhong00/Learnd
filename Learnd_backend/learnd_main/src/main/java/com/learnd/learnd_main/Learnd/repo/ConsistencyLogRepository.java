package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.UserConsistencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsistencyLogRepository extends JpaRepository<UserConsistencyLog, Long> {

    @Query(value = """
        WITH calendar AS (
          SELECT d::date AS day
          FROM generate_series(
            date_trunc('year', CURRENT_DATE),
            date_trunc('year', CURRENT_DATE) + interval '1 year - 1 day',
            interval '1 day'
          ) d
        )
        SELECT c.day, l.is_consistent
        FROM calendar c
        LEFT JOIN user_consistency_log l
               ON l.date = c.day
              AND l.user_id = :userId
        WHERE c.day >= (SELECT created_at::date FROM users WHERE id = :userId)
        ORDER BY c.day
        """, nativeQuery = true)
    List<Object[]> findConsistencyCalendar(@Param("userId") Long userId);
}
