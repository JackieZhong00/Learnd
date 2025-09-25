package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.UserConsistencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsistencyLogRepository extends JpaRepository<UserConsistencyLog, Long> {

    @Query(value = """
        WITH calendar AS (
          SELECT (generate_series(
            date_trunc('year', CURRENT_DATE)::date,
            (date_trunc('year', CURRENT_DATE) + interval '1 year - 1 day')::date,
            interval '1 day'
          ))::date AS day
        )
        SELECT c.day AS day, COALESCE(l.is_consistent, false) AS is_consistent
        FROM calendar c
        LEFT JOIN user_consistency_log l
          ON l.date = c.day
         AND l.fk_user = :userId
        ORDER BY c.day
        """, nativeQuery = true)
    List<Object[]> findConsistencyCalendar(@Param("userId") Long userId);
}
