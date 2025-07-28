package Learnd.model;

import java.time.LocalDate;

public class FlashcardDTO {
    int id;
    String question;
    String answer;
    LocalDate dateOfNextUsage;

    public FlashcardDTO(Flashcard flashcard) {
        this.id = flashcard.getId();
        this.question = flashcard.getQuestion();
        this.answer = flashcard.getAnswer();
        this.dateOfNextUsage = flashcard.getDateOfNextUsage();
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

    public void setDateOfNextUsage(LocalDate dateOfNextUsage) {
        this.dateOfNextUsage = dateOfNextUsage;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }


}
