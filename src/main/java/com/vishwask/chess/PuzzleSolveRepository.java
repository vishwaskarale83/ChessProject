package com.vishwask.chess;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PuzzleSolveRepository extends JpaRepository<PuzzleSolve, Long> {
    boolean existsByUserAndPuzzle(User user, Puzzle puzzle);
    List<PuzzleSolve> findByUser(User user);
    PuzzleSolve findByUserAndPuzzle(User user, Puzzle puzzle);
}
