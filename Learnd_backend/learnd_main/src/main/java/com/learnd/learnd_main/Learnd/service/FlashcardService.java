package com.learnd.learnd_main.Learnd.service;

import com.learnd.learnd_main.Learnd.model.Flashcard;
import org.springframework.stereotype.Component;
import com.learnd.learnd_main.Learnd.repo.DeckRepository;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;

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
