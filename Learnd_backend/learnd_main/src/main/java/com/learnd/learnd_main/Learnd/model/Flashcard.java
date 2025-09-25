package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

@Entity
public class Flashcard extends Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(columnDefinition = "TEXT")
    public String answer;

    //fk_deck_id = name of column in Flashcard table the holds foreign key refs to decks
    @ManyToOne
    @JoinColumn(name = "fk_deck_id")
    private Deck deck;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    public Flashcard(){
        super("");
        this.answer = "";
    }

    public Flashcard(String q, String ans) {
        super(q);
        this.answer = ans;
    }
    public String getAnswer() {
        return this.answer;
    }
    public User getUser() {
        return this.user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setDeck(Deck deck) {
        this.deck = deck;
    }
    public Deck getDeck(){
        return this.deck;
    }


}
