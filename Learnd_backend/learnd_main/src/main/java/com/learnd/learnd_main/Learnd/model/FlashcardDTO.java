package com.learnd.learnd_main.Learnd.model;

import java.time.LocalDate;

public class FlashcardDTO {
    int id;
    String question;
    String answer;
    LocalDate dateOfNextUsage;
    LocalDate creationDate;
    boolean requiresUserInput;
    int previousTimeInterval;

    public FlashcardDTO(Flashcard flashcard) {
        this.id = flashcard.getId();
        this.question = flashcard.getQuestion();
        this.answer = flashcard.getAnswer();
        this.dateOfNextUsage = flashcard.getDateOfNextUsage();
        this.creationDate = flashcard.getCreationDate();
        this.requiresUserInput = flashcard.getRequiresUserInput();
        this.previousTimeInterval = flashcard.getPreviousTimeInterval();
    }

    public FlashcardDTO() {

    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public LocalDate getDateOfNextUsage() {
        return dateOfNextUsage;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public boolean isRequiresUserInput() {
        return requiresUserInput;
    }

    public int getPreviousTimeInterval() {
        return previousTimeInterval;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setDateOfNextUsage(LocalDate dateOfNextUsage) {
        this.dateOfNextUsage = dateOfNextUsage;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setRequiresUserInput(boolean requiresUserInput) {
        this.requiresUserInput = requiresUserInput;
    }

    public void setPreviousTimeInterval(int previousTimeInterval) {
        this.previousTimeInterval = previousTimeInterval;
    }
}
