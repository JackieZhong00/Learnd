package Learnd.repo;

import Learnd.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
