package com.StudyAccell.StudyAccell.controller;

import com.StudyAccell.StudyAccell.model.Deck;
import com.StudyAccell.StudyAccell.model.Flashcard;
import com.StudyAccell.StudyAccell.model.FlashcardDTO;
import com.StudyAccell.StudyAccell.model.FlashcardSubmitRequest;
import com.StudyAccell.StudyAccell.repo.FlashcardRepository;
import com.StudyAccell.StudyAccell.service.DeckService;
import com.StudyAccell.StudyAccell.service.FlashcardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/flashcard")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final DeckService deckService;
    private final FlashcardRepository flashcardRepository;

    public FlashcardController(FlashcardService flashcardService, DeckService deckService, FlashcardRepository flashcardRepository) {
        this.flashcardService = flashcardService;
        this.deckService = deckService;
        this.flashcardRepository = flashcardRepository;
    }

    @PostMapping("/{deckId}/createcard")
    public FlashcardDTO createCard(@PathVariable int deckId, @RequestBody FlashcardSubmitRequest card, HttpServletResponse request) {
        //get Deck associated with Deck name and set the card's deck variable to this deck object before saving the card
        Deck deck = deckService.getDeckById(deckId);
        Flashcard flashcard = new Flashcard(card.getQuestion(), card.getAnswer());
        flashcard.setDeck(deck);
        return new FlashcardDTO(flashcardService.save(flashcard));
    }

    @PatchMapping("/updateCard/{cardId}")
    public FlashcardDTO updateCard(@PathVariable int cardId, @RequestBody FlashcardSubmitRequest card){
        Flashcard fetchedCard = flashcardRepository.findById(cardId);
        fetchedCard.setQuestion(card.getQuestion());
        fetchedCard.setAnswer(card.getAnswer());
        FlashcardDTO flashcardDTO = new FlashcardDTO(fetchedCard);
        flashcardService.save(fetchedCard);
        return flashcardDTO;
    }

    @GetMapping("/{cardId}")
    public Flashcard getCardById(@PathVariable int cardId, HttpServletResponse request) {
        return flashcardRepository.findById(cardId);
    }

    @GetMapping("/getDecksCards/{deckId}")
    public PagedModel<FlashcardDTO> getDecksCards(@PathVariable int deckId,
                                                  @RequestParam(value="pageNumber", defaultValue="0") int pageNumber,
                                                  @RequestParam(value="pageSize", defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
        Page<Flashcard> pg = flashcardRepository.findAllByDeckId(deckId, pageable);
        Page<FlashcardDTO> page = pg.map(FlashcardDTO::new);

        PagedModel<FlashcardDTO> finalpg = new PagedModel<>(page);
        for (FlashcardDTO dto : finalpg.getContent()) {
            System.out.println("Card ID: " + dto.getId());
            System.out.println("Question: " + dto.getQuestion());
            System.out.println("Answer: " + dto.getAnswer());
            System.out.println("Next usage: " + dto.getDateOfNextUsage());
            System.out.println("-----------");
        }
        return finalpg;
    }

    @GetMapping("/getCardsDueToday")
    public List<FlashcardDTO> getCardsDueToday(HttpServletResponse request) {
        List<Flashcard> list = flashcardRepository.findAllByDateOfNextUsage(LocalDate.now());
        List<FlashcardDTO> toReturn = new ArrayList<>(list.size());
        for (Flashcard flashcard : list) {
            toReturn.add(new FlashcardDTO(flashcard));
        }
        return toReturn;
    }



    @DeleteMapping("/deleteById/{cardId}")
    public void deleteById(@PathVariable int cardId, HttpServletResponse request) {
        flashcardRepository.deleteById(cardId);
    }

    @GetMapping("/getWithPrefix/{prefix}")
    public List<FlashcardDTO> getCardsWithPrefix(@PathVariable String prefix, HttpServletResponse request) {
        List<Flashcard> list = flashcardRepository.findByQuestionStartingWith(prefix);
        List<FlashcardDTO> toReturn = new ArrayList<>(list.size());
        for (Flashcard flashcard : list) {
            toReturn.add(new FlashcardDTO(flashcard));
        }
        return toReturn;
    }
}
