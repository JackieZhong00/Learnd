package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
public class Deck {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Column (name = "category")
    private String category;

    //deck is the name of member variable in card classes
    //that's used to link a deck to the card
    @OneToMany(mappedBy = "deck")
    private List<Flashcard> cards;

    @OneToMany(mappedBy = "deck_m")
    private List<MultipleChoiceCard> cards_m;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne
    @JoinColumn(name="fk_category")
    private Category category_fk;

    @ElementCollection
    private Set<String> listOfCategories;

    public void addToListOfCategories(Set<String> categoriesToAdd) {
        listOfCategories.addAll(categoriesToAdd);
    }

    public Set<String> getListOfCategories() {
        return listOfCategories;
    }

    public int getId() {
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getCategory(){
        return this.category;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setCategory(String category){
        this.category = category;
    }
    public void setUser(User user){
        this.user = user;
    }

    public void setCategory_fk(Category category){
        this.category_fk = category;
    }

}
