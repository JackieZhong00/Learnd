package com.learnd.learnd_main.Learnd.controller;

import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.producer.MessageProducer;
import com.learnd.learnd_main.Learnd.model.Deck;
import com.learnd.learnd_main.Learnd.model.Flashcard;
import com.learnd.learnd_main.Learnd.model.FlashcardDTO;
import com.learnd.learnd_main.Learnd.model.FlashcardSubmitRequest;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;
import com.learnd.learnd_main.Learnd.service.DeckService;
import com.learnd.learnd_main.Learnd.service.FlashcardService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/flashcard")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final DeckService deckService;
    private final FlashcardRepository flashcardRepository;
    private final MessageProducer messageProducer;

    public FlashcardController(FlashcardService flashcardService, DeckService deckService,
                               FlashcardRepository flashcardRepository, MessageProducer messageProducer) {
        this.flashcardService = flashcardService;
        this.deckService = deckService;
        this.flashcardRepository = flashcardRepository;
        this.messageProducer = messageProducer;
    }

    @PostMapping("/{deckId}/createcard")
    public FlashcardDTO createCard(@PathVariable int deckId, @RequestBody FlashcardSubmitRequest card) {
        //get Deck associated with Deck name and set the card's deck variable to this deck object before saving the card
        Deck deck = deckService.getDeckById(deckId);
        Flashcard flashcard = new Flashcard(card.getQuestion(), card.getAnswer());
        flashcard.setDeck(deck);
        Flashcard createdCard = flashcardService.save(flashcard);
        List<String> answers = new ArrayList<>();
        answers.add(createdCard.getAnswer());
        CardUpdateEvent event = new CardUpdateEvent("create", createdCard.getId(), deck.getId(),
                deck.getUser().getId(), false, createdCard.getQuestion(), answers);

        messageProducer.sendCardUpdateMsg("create", event);

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
