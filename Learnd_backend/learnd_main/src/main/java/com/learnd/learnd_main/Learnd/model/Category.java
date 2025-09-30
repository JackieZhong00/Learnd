package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
        name =  "category",
        indexes = {
            @Index(name="idx_category_userid", columnList="fk_user"),
            @Index(name="idx_category_parentid", columnList="parent_category_id")
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parent;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Deck> decks = new ArrayList<Deck>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
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

    public Category getParent() {
        return this.parent;
    }

    public List<Category> getChildren(){
        return this.children;
    }
    public void setChildren(List<Category> children) {
        this.children = children;
    }


}
