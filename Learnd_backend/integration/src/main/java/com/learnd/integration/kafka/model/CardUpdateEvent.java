package com.learnd.integration.kafka.model;

import java.time.LocalDateTime;
import java.util.List;

public class CardUpdateEvent {
    public String updateType; //"create", "update", "delete"
    public int cardId;
    public int deckId;
    public int userId;
    public boolean isMultipleChoice;


    //these two are empty strings when updateType = "delete"
    public String question;
    public List<String> answer;

    public CardUpdateEvent() {

    }


    public boolean getIsMultipleChoice() {
        return isMultipleChoice;
    }
    public String getUpdateType() {
        return updateType;
    }

    public int getCardId() {
        return cardId;
    }

    public int getDeckId() {
        return deckId;
    }

    public int getUserId() {
        return userId;
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
    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public void setDeckId(int deckId) {
        this.deckId = deckId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "CardUpdateEvent{" +
                "updateType='" + updateType + '\'' +
                ", cardId=" + cardId +
                ", deckId=" + deckId +
                ", userId=" + userId +
                ", question='" + question + '\'' +
                ", answer=" + answer +
                '}';
    }

    public boolean isMultipleChoice() {
        return isMultipleChoice;
    }
}
