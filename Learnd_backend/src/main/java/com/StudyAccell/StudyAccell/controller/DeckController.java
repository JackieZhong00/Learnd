package com.StudyAccell.StudyAccell.controller;

import com.StudyAccell.StudyAccell.model.*;
import com.StudyAccell.StudyAccell.repo.DeckRepository;
import com.StudyAccell.StudyAccell.repo.UserRepository;
import com.StudyAccell.StudyAccell.service.DeckService;
import com.StudyAccell.StudyAccell.service.FlashcardService;
import com.StudyAccell.StudyAccell.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/deck")
public class DeckController {
    private final DeckService deckService;
    private final FlashcardService flashcardService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    public DeckController(DeckService deckService, FlashcardService flashcardService, UserService userService, UserRepository userRepository, DeckRepository deckRepository) {
        this.deckService = deckService;
        this.flashcardService = flashcardService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    @GetMapping("/{name}")
    public Deck getDeck(@PathVariable String name) {
        return deckService.getDeckByName(name);
    }

    @PostMapping("/createDeck")
    public ResponseEntity<Deck> createDeck(@RequestBody DeckRequest request) {
        return ResponseEntity.ok(deckService.createDeck(request));
    }

    @PutMapping("/{deckid}/updateDates")
    public void setDateOfNextUsage(@PathVariable int deckId, @RequestBody List<UpdateDateRequest> cards){
        deckService.updateCardDates(deckId, cards);
    }

    //get all decks for the user with this prefix
    @GetMapping("/getDeckNamesWithPrefix/{prefix}")
    public List<Deck> getAllDeckNamesWithPrefix(@PathVariable String prefix) {
        return deckService.getAllDeckNamesWithPrefix(prefix);
    }

    @GetMapping("/getAllDecksByUser")
    public List<DeckDTO> getAllDecks() {
        return deckService.getAllDecksByUser();
    }

    @GetMapping("/getDeckIdByName/{name}")
    public int getDeckIdByName (@PathVariable String name) {
        return deckService.getDeckIdByName(name);
    }

    @PatchMapping("/rename/{deckId}")
    public ResponseEntity<Void> renameDeck(@PathVariable int deckId, @RequestBody DeckRenameRequest name) {
        Deck deck = deckService.getDeckById(deckId);
        deck.setName(name.getName());
        deckRepository.save(deck);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/delete/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable int deckId) {
        deckRepository.deleteById(deckId);
        return ResponseEntity.ok().build();
    }


}
