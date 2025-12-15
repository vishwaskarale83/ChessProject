package com.vishwask.chess;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "puzzle_solves", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "puzzle_id"})
})
public class PuzzleSolve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    private Puzzle puzzle;

    @Column(nullable = false)
    private LocalDateTime solvedAt = LocalDateTime.now();

    @Column(name = "solve_duration_seconds")
    private Long solveDurationSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = puzzle;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }

    public Long getSolveDurationSeconds() {
        return solveDurationSeconds;
    }

    public void setSolveDurationSeconds(Long solveDurationSeconds) {
        this.solveDurationSeconds = solveDurationSeconds;
    }
}
