package com.StudyAccell.StudyAccell.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "category_fk", cascade = CascadeType.ALL)
    private List<Deck> decks = new ArrayList<Deck>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Optional<Category> getParent() {
        return Optional.ofNullable(this.parent);
    }


}
