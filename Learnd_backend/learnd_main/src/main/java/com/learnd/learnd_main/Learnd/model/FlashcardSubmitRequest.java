package com.learnd.learnd_main.Learnd.model;

public class FlashcardSubmitRequest {
    String question;
    String answer;
    public FlashcardSubmitRequest () {
        this.question = "";
        this.answer = "";
    }

    public FlashcardSubmitRequest (String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public FlashcardSubmitRequest (Flashcard flashcard) {
        this.question = flashcard.getQuestion();
        this.answer = flashcard.getAnswer();
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
}
