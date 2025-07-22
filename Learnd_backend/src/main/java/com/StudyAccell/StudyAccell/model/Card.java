package com.StudyAccell.StudyAccell.model;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDate;

@MappedSuperclass
public abstract class Card {

    @Column(columnDefinition = "TEXT")
    public String question;

    @Column
    public LocalDate dateOfNextUsage;

    public Card () {
        this.dateOfNextUsage = LocalDate.now();
    }

    public Card(String question) {
        this.question = question;
        this.dateOfNextUsage = LocalDate.now();
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

