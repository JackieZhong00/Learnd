package com.learnd.integration.kafka.model;

import java.time.LocalDateTime;
import java.util.List;

public class RecommendFeedbackEvent {
    public boolean wasAccepted;
    public int userId;
    public int deckId;
    public String question;
    public List<String> answer;
    public boolean isMultipleChoice;



    public RecommendFeedbackEvent() {

    }

    public boolean getIsMultipleChoice() {
        return isMultipleChoice;
    }

    public boolean getWasAccepted() {
        return wasAccepted;
    }

    public int getUserId() {
        return userId;
    }

    public int getDeckId() {
        return deckId;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setIsMultipleChoice(boolean isMultipleChoice) {
        this.isMultipleChoice = isMultipleChoice;
    }

    public void setWasAccepted(boolean wasAccepted) {
        this.wasAccepted = wasAccepted;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setDeckId(int deckId) {
        this.deckId = deckId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }


    @Override
    public String toString() {
        return "RecommendFeedbackEvent{" +
                "wasAccepted=" + wasAccepted +
                ", userId=" + userId +
                ", deckId=" + deckId +
                ", question='" + question + '\'' +
                ", answer=" + answer +
                '}';
    }

    public boolean isWasAccepted() {
        return wasAccepted;
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }
}
