package com.learnd.learnd_main.Learnd.model;


import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "refreshTokenVersion")
    private int refreshTokenVersion;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Deck> decks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Category> categories = new ArrayList<Category>();

    public User(String email, String password){
        this.email = email;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.password = encoder.encode(password);
    }

    public User() {

    }


    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getRefreshTokenVersion() {
        return refreshTokenVersion;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRefreshTokenVersion() {
        this.refreshTokenVersion = 1;
    }

    public void setRefreshTokenVersion(int versionNumber) {
        this.refreshTokenVersion = versionNumber;
    }

    public void incrementRefreshTokenVersion(){
        this.refreshTokenVersion+=1;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", decks=" + decks +
                '}';
    }
}
