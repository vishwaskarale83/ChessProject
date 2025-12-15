package com.vishwask.chess;

import javax.persistence.*;

@Entity
@Table(name = "puzzles")
public class Puzzle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 5000)
    private String boardState;
    
    private String turn; // WHITE or BLACK
    
    @Column(length = 500)
    private String solution; // JSON array of moves: [{"fromRow":6,"fromCol":4,"toRow":4,"toCol":4}]
    
    private String difficulty; // EASY, MEDIUM, HARD
    
    private Integer rating;

    @Column(name = "rating_low")
    private Integer ratingLow;

    @Column(name = "rating_high")
    private Integer ratingHigh;

    public Puzzle() {}

    public Puzzle(String name, String description, String boardState, String turn, String solution, String difficulty) {
        this(name, description, boardState, turn, solution, difficulty, null, null);
    }

    public Puzzle(String name,
                  String description,
                  String boardState,
                  String turn,
                  String solution,
                  String difficulty,
                  Integer ratingLow,
                  Integer ratingHigh) {
        this.name = name;
        this.description = description;
        this.boardState = boardState;
        this.turn = turn;
        this.solution = solution;
        this.difficulty = difficulty;
        this.ratingLow = ratingLow;
        this.ratingHigh = ratingHigh;
        if (ratingLow != null && ratingHigh != null) {
            this.rating = (ratingLow + ratingHigh) / 2;
        } else {
            this.rating = 1500;
        }
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBoardState() {
        return boardState;
    }

    public void setBoardState(String boardState) {
        this.boardState = boardState;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getRatingLow() {
        return ratingLow;
    }

    public void setRatingLow(Integer ratingLow) {
        this.ratingLow = ratingLow;
    }

    public Integer getRatingHigh() {
        return ratingHigh;
    }

    public void setRatingHigh(Integer ratingHigh) {
        this.ratingHigh = ratingHigh;
    }
}
