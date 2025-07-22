package com.StudyAccell.StudyAccell.service;

import com.StudyAccell.StudyAccell.model.Deck;
import com.StudyAccell.StudyAccell.model.Flashcard;
import com.StudyAccell.StudyAccell.model.FlashcardDTO;
import com.StudyAccell.StudyAccell.repo.DeckRepository;
import com.StudyAccell.StudyAccell.repo.FlashcardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;

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
