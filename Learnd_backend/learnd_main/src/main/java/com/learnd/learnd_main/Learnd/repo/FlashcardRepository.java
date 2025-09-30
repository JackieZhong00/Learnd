package com.learnd.learnd_main.Learnd.repo;

import com.learnd.learnd_main.Learnd.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Integer> {

    Flashcard findById(int id);
    Optional<Flashcard> findByQuestion(String question);
    Page<Flashcard> findAllByDeckId(int deckId, Pageable pageable);
    List<Flashcard> findAllByDateOfNextUsage(LocalDate now);


    List<Flashcard> findByQuestionStartingWith(String prefix);
    List<Flashcard> findByUser_IdAndPastDueTrue(int userId);

    int countByUser_IdAndDateOfNextUsage(int userId, LocalDate date);

    int countByUser_IdAndPastDueTrue(int userId);

    @Modifying
    @Query("UPDATE Flashcard f SET f.pastDue = true WHERE f.user = :userId AND f.dateOfNextUsage = :date")
    int markPastDueByUserAndDate(@Param("userId") int userId, @Param("date") LocalDate date);
}
