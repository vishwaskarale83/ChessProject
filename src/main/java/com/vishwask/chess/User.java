package com.vishwask.chess;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore // never expose hashed password in JSON responses
    private String password;

    @OneToMany(mappedBy = "whitePlayer")
    @JsonIgnore // prevent recursion when serializing Game -> User -> games
    private Set<Game> whiteGames;

    @OneToMany(mappedBy = "blackPlayer")
    @JsonIgnore // prevent recursion when serializing Game -> User -> games
    private Set<Game> blackGames;

    // Constructors
    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Game> getWhiteGames() { return whiteGames; }
    public void setWhiteGames(Set<Game> whiteGames) { this.whiteGames = whiteGames; }

    public Set<Game> getBlackGames() { return blackGames; }
    public void setBlackGames(Set<Game> blackGames) { this.blackGames = blackGames; }
}