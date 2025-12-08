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

    @Column
    private LocalDate creationDate;

    @Column(name="previous_time_interval")
    private int previousTimeInterval = 1;

    public Card () {
        this.dateOfNextUsage = Instant.now()
                .plus(1, ChronoUnit.DAYS)       // add 1 day
                .atZone(ZoneOffset.UTC)          // interpret in UTC
                .toLocalDate();
        this.creationDate = LocalDate.now();
    }

    public Card(String question) {
        this.question = question;
        this.dateOfNextUsage = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();
    }
    public LocalDate getCreationDate() {return creationDate;}
    public String getQuestion() {
        return question;
    }
    public LocalDate getDateOfNextUsage() {
        return dateOfNextUsage;
    }
    public int getPreviousTimeInterval() {return previousTimeInterval;}
    public void setQuestion(String question) {
        this.question = question;
    }
    public void setDateOfNextUsage(LocalDate dateOfNextUsage) {
        this.dateOfNextUsage = dateOfNextUsage;
    }
    public boolean getPastDue() {return pastDue;}
    public void setPastDue(boolean pastDue) {this.pastDue = pastDue;}
    public void setCreationDate(LocalDate creationDate) {this.creationDate = creationDate;}
    public void setPreviousTimeInterval(int previousTimeInterval) {
        if (previousTimeInterval < 1) {this.previousTimeInterval = 1;}
        this.previousTimeInterval = previousTimeInterval;}
}

