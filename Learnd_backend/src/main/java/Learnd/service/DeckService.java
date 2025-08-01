package Learnd.service;

import Learnd.model.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import Learnd.repo.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class DeckService {
    private final DeckRepository deckRepository;
    private final MultipleChoiceCardRepository multipleChoiceCardRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public DeckService(DeckRepository deckRepository, MultipleChoiceCardRepository multipleChoiceCardRepository,
                       FlashcardRepository flashcardRepository, UserRepository userRepository,
                       CategoryRepository categoryRepository) {
        this.deckRepository = deckRepository;
        this.multipleChoiceCardRepository = multipleChoiceCardRepository;
        this.flashcardRepository = flashcardRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }


    public Deck save(Deck deck) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresentOrElse(
                fetchedUser -> {
                    deck.setUser(user.get());
                    deckRepository.save(deck);
                },
                () -> {throw new UsernameNotFoundException("user not found, couldn't save deck");}
        );

        return deck;
    }

    public Deck createDeck(DeckRequest request) {
        Deck deck = new Deck();
        deck.setName(request.getName());

        //find user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            Optional<Category> fetchedCategory = categoryRepository.findByNameAndUser(request.getCategoryName(), user.get());

            fetchedCategory.ifPresentOrElse(
                    (category) -> {
                        deck.setCategory_fk(category);
                        deck.setCategory(category.getName());
                        deck.setUser(user.get());
                        deckRepository.save(deck);
                    },
                    () -> {throw new NoSuchElementException("category not found");}
            );
        }
        else {
            throw new UsernameNotFoundException("user not found, couldn't save deck");
        }
        return deck;
    }

    public Deck getDeckByName(String name) {
        return deckRepository.findDeckByName(name);
    }

    public Deck getDeckById(int id) {
        return deckRepository.findById(id).orElse(null);
    }

    public void updateCardDates(int deckId, List<UpdateDateRequest> cards) {
        for (UpdateDateRequest card : cards) {
            if (card.getCardType().equals("flashcard")) {
                //make flashcardrepo request
                updateFlashcard(card.getCardId(), card.getNewDate());
                continue;
            }
            //make multiple_choice_card repo request
            updateMultipleChoiceCard(card.getCardId(), card.getNewDate());
        }
    }

    public void updateFlashcard(int cardId, LocalDate date) {
        Flashcard card = flashcardRepository.findById(cardId);
        card.setDateOfNextUsage(date);
        flashcardRepository.save(card);
    }

    public void updateMultipleChoiceCard(int cardId, LocalDate date) {
        MultipleChoiceCard card = multipleChoiceCardRepository.findById(cardId);
        card.setDateOfNextUsage(date);
        multipleChoiceCardRepository.save(card);
    }

    public List<Deck> getAllDeckNamesWithPrefix(String prefix) {
        return deckRepository.findByNameStartingWith(prefix);
    }

    public List<DeckDTO> getAllDecksByUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        User userObj = user.orElseThrow(()->new UsernameNotFoundException("user not found, couldn't save deck"));
        List<Deck> deckList = deckRepository.findAllByUser(userObj);
        List<DeckDTO> deckDTOList = new ArrayList<>();
        for (Deck deck : deckList) {
            deckDTOList.add(new DeckDTO(deck));
        }
        return deckDTOList;
    }


    public int getDeckIdByName(String name) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByEmail(email);
        User userObj = user.orElseThrow();
        Optional<Integer> fetchedint = deckRepository.findIdByNameAndUser(name,userObj);
        return fetchedint.orElseThrow();
    }
}

