package com.StudyAccell.StudyAccell.model;

import jakarta.persistence.*;

@Entity
public class Flashcard extends Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(columnDefinition = "TEXT")
    public String answer;

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

    //fk_deck_id = name of column in Flashcard table the holds foreign key refs to decks
    @ManyToOne
    @JoinColumn(name = "fk_deck_id")
    private Deck deck;



}
