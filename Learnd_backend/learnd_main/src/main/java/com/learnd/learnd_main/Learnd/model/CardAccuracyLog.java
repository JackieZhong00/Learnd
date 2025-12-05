package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class CardAccuracyLog {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="fk_flashcard")
    private Flashcard flashcard;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="fk_userid")
    private User user;
    private int accuracy;
    private LocalDate date;

    public CardAccuracyLog() {}


    public long getId() {
        return id;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public LocalDate getDate() {
        return date;
    }


    public void setFlashcard(Flashcard card) {
        this.flashcard = card;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}


