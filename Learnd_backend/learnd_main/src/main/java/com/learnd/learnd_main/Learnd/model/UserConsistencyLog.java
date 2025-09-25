package com.learnd.learnd_main.Learnd.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_consistency_log")
public class UserConsistencyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="fk_user")
    private User user;

    @Column(name ="date")
    private LocalDate date;

    @Column(name = "is_consistent")
    private boolean isConsistent;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getDate() {
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

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setConsistent(boolean consistent) {
        isConsistent = consistent;
    }
}
