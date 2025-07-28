package Learnd.service;

import Learnd.model.Flashcard;
import org.springframework.stereotype.Component;
import Learnd.repo.DeckRepository;
import Learnd.repo.FlashcardRepository;

@Component
public class FlashcardService {
    FlashcardRepository flashcardRepository;
    DeckRepository deckRepository;

    public FlashcardService(FlashcardRepository flashcardRepository, DeckRepository deckRepository) {
        this.flashcardRepository = flashcardRepository;
        this.deckRepository = deckRepository;
    }

    public Flashcard save(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);
    }


}
