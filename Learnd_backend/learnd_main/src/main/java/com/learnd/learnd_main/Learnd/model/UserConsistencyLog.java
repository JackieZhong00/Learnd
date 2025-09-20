package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_consistency_log")
public class UserConsistencyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Instant date;

    private boolean isConsistent;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Instant getDate() {
        return date;
    }

    public boolean isConsistent() {
        return isConsistent;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public void setConsistent(boolean consistent) {
        isConsistent = consistent;
    }
}
