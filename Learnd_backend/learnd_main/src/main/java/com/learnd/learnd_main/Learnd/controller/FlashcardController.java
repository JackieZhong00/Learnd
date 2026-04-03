package com.learnd.learnd_main.Learnd.controller;

import com.learnd.integration.grpc.FlashcardGrade;
import com.learnd.integration.grpc.FlashcardToGrade;
import com.learnd.integration.grpc.RagGrpcClient;
import com.learnd.integration.kafka.model.CardUpdateEvent;
import com.learnd.integration.kafka.producer.MessageProducer;
import com.learnd.learnd_main.Learnd.model.*;
import com.learnd.learnd_main.Learnd.repo.CardAccuracyLogRepository;
import com.learnd.learnd_main.Learnd.repo.FlashcardRepository;
import com.learnd.learnd_main.Learnd.service.DeckService;
import com.learnd.learnd_main.Learnd.service.FlashcardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api/flashcard")
public class FlashcardController {
    private final FlashcardService flashcardService;
    private final DeckService deckService;
    private final FlashcardRepository flashcardRepository;
    private final MessageProducer messageProducer;
    private final RagGrpcClient ragGrpcClient;
    private final CardAccuracyLogRepository cardAccuracyLogRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public FlashcardController(FlashcardService flashcardService, DeckService deckService,
                               FlashcardRepository flashcardRepository, MessageProducer messageProducer
                               ,RagGrpcClient ragGrpcClient, CardAccuracyLogRepository cardAccuracyLogRepository) {
        this.flashcardService = flashcardService;
        this.deckService = deckService;
        this.flashcardRepository = flashcardRepository;
        this.messageProducer = messageProducer;
        this.ragGrpcClient = ragGrpcClient;
        this.cardAccuracyLogRepository = cardAccuracyLogRepository;
    }

    private int getUserIdFromPrincipal() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userPrincipal.getUserId();
    }

    @PostMapping("/{deckId}")
    public ResponseEntity<FlashcardDTO> createCard(@PathVariable int deckId, @RequestBody FlashcardSubmitRequest card) {
        //get Deck associated with Deck name and set the card's deck variable to this deck object before saving the card
        Deck deck = deckService.getDeckById(deckId);
        User userRef = entityManager.getReference(User.class, getUserIdFromPrincipal());
        Flashcard flashcard = new Flashcard(card);
        flashcard.setDeck(deck);
        flashcard.setUser(userRef);
        flashcardService.save(flashcard);
        List<String> answers = new ArrayList<>();
        answers.add(flashcard.getAnswer());
        CardUpdateEvent event = new CardUpdateEvent("create", flashcard.getId(), deckId,
                userRef.getId(), false, flashcard.getQuestion(), answers);

        messageProducer.sendCardUpdateMsg("create", event);

        return ResponseEntity.ok().body(new FlashcardDTO(flashcard));
    }

    @PatchMapping("/updateCard/{deckId}/{cardId}")
    public FlashcardDTO updateCard(@PathVariable int deckId, @PathVariable int cardId, @RequestBody FlashcardSubmitRequest card){
        Flashcard fetchedCard = flashcardRepository.findById(cardId);
        fetchedCard.setQuestion(card.getQuestion());
        fetchedCard.setAnswer(card.getAnswer());
        List<String> answers = new ArrayList<>();
        answers.add(fetchedCard.getAnswer());
        User userRef = entityManager.getReference(User.class, getUserIdFromPrincipal());
        CardUpdateEvent event = new CardUpdateEvent("create", fetchedCard.getId(), deckId,
                userRef.getId(), false, fetchedCard.getQuestion(),answers);
        messageProducer.sendCardUpdateMsg("create", event);
        FlashcardDTO flashcardDTO = new FlashcardDTO(fetchedCard);
        flashcardService.save(fetchedCard);
        return flashcardDTO;
    }

    @PatchMapping("/updateCardDate/{cardId}")
    public ResponseEntity<Void> updateCardDate(@PathVariable int cardId, @RequestBody LocalDate date){
        Flashcard fetchedCard = flashcardRepository.findById(cardId);
        fetchedCard.setDateOfNextUsage(date);
        flashcardService.save(fetchedCard);
        return ResponseEntity.ok().build();
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
        return new PagedModel<>(page);
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

    @GetMapping("/getAllDueCards")
    public List<FlashcardDTO> getAllDueCards() {
        return flashcardService.getAllDueCards();
    }

    //makes grpc request to rag -> gets grade -> create card accuracy log for card if successfully graded
    @PostMapping("/grade")
    public ResponseEntity<Void> grade(@RequestBody FlashcardDTO flashcardDTO){
//        System.out.println("flashcardDTO: " + flashcardDTO.getAnswer());
        Flashcard card = flashcardRepository.findById(flashcardDTO.getId());
        DeckAndCategoryNameDTO names = flashcardRepository.findDeckAndCategoryName((long)flashcardDTO.getId());
        FlashcardToGrade grpcRequest = FlashcardToGrade.newBuilder()
                .setCategory(names.getCategoryName())
                .setDeckName(names.getDeckName())
                .setFlashcardId(flashcardDTO.getId())
                .setQuestion(card.getQuestion())
                .setAnswer(flashcardDTO.getAnswer())
                .setUserId(card.getUser().getId())
                .build();
        System.out.println("grpcrequest answer: " + grpcRequest.getAnswer());

        //for full async -> free controller thread to handle other http requests
        //CompletableFuture allows this request to be abandoned momentarily and resumed when the future is resolved
        //so the return isn't made until a response is received
        CompletableFuture.runAsync(() -> {
            FlashcardGrade result = ragGrpcClient.sendCardToGrade(grpcRequest);
            int grade = result.getGrade();
            int userId = getUserIdFromPrincipal();
            User userRef = entityManager.getReference(User.class, userId);
            CardAccuracyLog log = new CardAccuracyLog(card,userRef,grade,LocalDate.now());
            cardAccuracyLogRepository.save(log);
        });
        return ResponseEntity.ok().build();
    }
}
