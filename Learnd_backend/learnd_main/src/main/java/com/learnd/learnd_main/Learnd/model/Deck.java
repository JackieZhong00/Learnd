package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.time.Instant;
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
    private String categoryName;

    @Column (name = "accuracy")
    private int accuracy = 100;

    @Column (name = "earliest_due_date")
    private Instant earliestDueDate;

    //deck is the name of member variable in card classes
    //that's used to link a deck to the card
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flashcard> cards;

    @OneToMany(mappedBy = "deck_m", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MultipleChoiceCard> cards_m;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="fk_category")
    private Category category;

    //to use as metadata - creates table with deck_id and list_of_categories columns
    //list of categories column == one long string of all categories that current deck is under
    @ElementCollection
    private Set<String> listOfCategories;

    public void addToListOfCategories(Set<String> categoriesToAdd) {
        listOfCategories.addAll(categoriesToAdd);
    }

    public Set<String> getListOfCategories() {
        return listOfCategories;
    }
    public User getUser() {
        return user;
    }

    public int getId() {
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getCategory(){
        return this.categoryName;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setCategory(String category){
        this.categoryName = category;
    }
    public void setUser(User user){
        this.user = user;
    }

    public void setCategory(Category category){
        this.category = category;
    }

    public int getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public Instant getEarliestDueDate() {
        return earliestDueDate;
    }

    public void setEarliestDueDate(Instant earliestDueDate) {
        this.earliestDueDate = earliestDueDate;
    }

}
