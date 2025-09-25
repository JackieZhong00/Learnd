package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@MappedSuperclass
public abstract class Card {

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column
    private LocalDate dateOfNextUsage;

    @Column (name = "past_due")
    private boolean pastDue;

    public Card () {
        this.dateOfNextUsage = Instant.now()
                .plus(1, ChronoUnit.DAYS)       // add 1 day
                .atZone(ZoneOffset.UTC)          // interpret in UTC
                .toLocalDate();
    }

    public Card(String question) {
        this.question = question;
        this.dateOfNextUsage = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();;
    }

    public String getQuestion() {
        return question;
    }

    public LocalDate getDateOfNextUsage() {
        return dateOfNextUsage;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public void setDateOfNextUsage(LocalDate dateOfNextUsage) {
        this.dateOfNextUsage = dateOfNextUsage;
    }

}

