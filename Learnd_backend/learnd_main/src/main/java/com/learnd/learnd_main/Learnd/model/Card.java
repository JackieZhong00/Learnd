package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class Card {

    @Column(columnDefinition = "TEXT")
    public String question;

    @Column
    public Instant dateOfNextUsage;

    public Card () {
        this.dateOfNextUsage = Instant.now();
    }

    public Card(String question) {
        this.question = question;
        this.dateOfNextUsage = Instant.now();
    }

    public String getQuestion() {
        return question;
    }

    public Instant getDateOfNextUsage() {
        return dateOfNextUsage;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setDateOfNextUsage(Instant dateOfNextUsage) {
        this.dateOfNextUsage = dateOfNextUsage;
    }

}

