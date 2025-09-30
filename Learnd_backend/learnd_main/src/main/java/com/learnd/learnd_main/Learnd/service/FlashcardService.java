package com.learnd.learnd_main.Learnd.service;

import com.learnd.integration.kafka.producer.MessageProducer;
import com.learnd.learnd_main.Learnd.model.Flashcard;
import com.learnd.learnd_main.Learnd.model.FlashcardDTO;
import com.learnd.learnd_main.Learnd.model.User;
import com.learnd.learnd_main.Learnd.repo.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.learnd.learnd_main.Learnd.repo.DeckRepository;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;

    public FlashcardService(FlashcardRepository flashcardRepository, DeckRepository deckRepository, UserRepository userRepository) {
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
    }

    public Flashcard save(Flashcard flashcard) {
        return flashcardRepository.save(flashcard);

    }

    public List<FlashcardDTO> getAllDueCards(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        int userId = userRepository.findByEmail(email).orElseThrow().getId();
        List<Flashcard> cards = flashcardRepository.findByUser_IdAndPastDueTrue(userId);
        List<FlashcardDTO> dtoList = new ArrayList<>();
        for (Flashcard flashcard : cards) {
            dtoList.add(new FlashcardDTO(flashcard));
        }
        return dtoList;
    }


}
