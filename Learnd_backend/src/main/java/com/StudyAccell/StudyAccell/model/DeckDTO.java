package com.StudyAccell.StudyAccell.model;

public class DeckDTO {
    public int id;
    public String name;
    public String category;
    public DeckDTO(Deck deck) {
        this.id = deck.getId();
        this.name = deck.getName();
        this.category = deck.getCategory();
    }
}
