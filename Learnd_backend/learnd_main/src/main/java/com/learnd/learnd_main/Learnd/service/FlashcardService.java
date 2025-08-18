package com.learnd.learnd_main.Learnd.service;

import com.learnd.integration.kafka.producer.MessageProducer;
import com.learnd.learnd_main.Learnd.model.Flashcard;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.learnd.learnd_main.Learnd.repo.DeckRepository;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;

@Component
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;

    public FlashcardService(FlashcardRepository flashcardRepository, DeckRepository deckRepository) {
        this.flashcardRepository = flashcardRepository;
    }

    public Flashcard save(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);

    }


}
