package com.learnd.learnd_main.Learnd.controller;

import com.learnd.learnd_main.Learnd.model.*;
import com.learnd.learnd_main.Learnd.model.*;
import com.learnd.learnd_main.Learnd.service.DeckService;
import com.learnd.learnd_main.Learnd.service.FlashcardService;
import com.learnd.learnd_main.Learnd.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learnd.learnd_main.Learnd.repo.DeckRepository;
import com.learnd.learnd_main.Learnd.repo.UserRepository;

import java.util.List;

@RestController
@RequestMapping(path = "/api/deck")
public class DeckController {
    private final DeckService deckService;

    private final DeckRepository deckRepository;

    public DeckController(DeckService deckService, DeckRepository deckRepository) {
        this.deckService = deckService;
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
    public List<DeckDTO> getAllDeckNamesWithPrefix(@PathVariable String prefix) {
        return deckService.getAllDeckNamesWithPrefix(prefix);
    }

    @GetMapping("/getAllDecksByUser")
    public List<DeckDTO> getAllDecks() {
        return deckService.getAllDecksByUser();
    }

    @GetMapping("/getByCategory/{categoryId}")
    public List<DeckDTO> getDecksByCategory(@PathVariable int categoryId) {
        return deckService.getDecksByCategory(categoryId);
    }
    @GetMapping("/getDeckIdByName/{name}")
    public int getDeckIdByName (@PathVariable String name) {
        return deckService.getDeckIdByName(name);
    }

    @PatchMapping("/rename/{deckId}")
    public ResponseEntity<Void> renameDeck(@PathVariable int deckId, @RequestBody RenameRequest name) {
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

//    @GetMapping("/getDueDecks")
//    public List<DeckDTO> getDueDecks(){
//        //get user id from security context - use it to get all decks with due date == today
//    }


}
