package com.learnd.learnd_main.Learnd.model;

import java.time.Instant;
import java.time.LocalDate;

public class UpdateDateRequest {

    private String cardType;
    private int cardId;
    private Instant newDate;

    public String getCardType() {
        return cardType;
    }

    public int getCardId() {
        return cardId;
    }

    public Instant getNewDate() {
        return newDate;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public void setNewDate(Instant newDate) {
        this.newDate = newDate;
    }
}
