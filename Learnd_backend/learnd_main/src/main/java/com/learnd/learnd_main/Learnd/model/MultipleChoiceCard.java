package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity
public class MultipleChoiceCard extends Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @ManyToOne
    @JoinColumn(name = "fk_deck")
    private Deck deck_m;

    @ElementCollection
    @CollectionTable(name = "answer_choices", joinColumns = @JoinColumn(name = "card_id"))
    ArrayList<String> answers;

    @Column(name = "index_correct_ans")
    int correctAns;

    public MultipleChoiceCard() {
        super("");
        this.answers = new ArrayList<>();
        this.correctAns = 0;
    }
    public MultipleChoiceCard(String q, ArrayList<String> ans, int correctAns){
        super(q);
        this.answers = ans;
        this.correctAns = correctAns;

    }

}
