package com.learnd.learnd_main.Learnd.model;

public class FlashcardSubmitRequest {
    String question;
    String answer;
    boolean requiresUserInput;
    public FlashcardSubmitRequest () {
        this.question = "";
        this.answer = "";
        this.requiresUserInput = false;
    }

    public FlashcardSubmitRequest (String question, String answer, boolean requiresUserInput) {
        this.question = question;
        this.answer = answer;
        this.requiresUserInput = requiresUserInput;
    }

    public FlashcardSubmitRequest (Flashcard flashcard) {
        this.question = flashcard.getQuestion();
        this.answer = flashcard.getAnswer();
        this.requiresUserInput = flashcard.getRequiresUserInput();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public boolean getRequiresUserInput() {
        return requiresUserInput;
    }
    public void setRequiresUserInput(boolean requiresUserInput) {
        this.requiresUserInput = requiresUserInput;
    }
}
