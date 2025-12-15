package com.vishwask.chess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PuzzleService {

    @Autowired
    private PuzzleRepository puzzleRepository;

    @Autowired
    private PuzzleSolveRepository puzzleSolveRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> ALLOWED_PIECES = setOf("♔", "♕", "♖", "♗", "♘", "♙", "♚", "♛", "♜", "♝", "♞", "♟");
    private static final Set<String> ALLOWED_DIFFICULTIES = setOf("EASY", "MEDIUM", "HARD");
    private static final Set<String> ALLOWED_PROMOTIONS = setOf("QUEEN", "ROOK", "BISHOP", "KNIGHT");

    @SafeVarargs
    private static <T> List<T> listOf(T... items) {
        return Collections.unmodifiableList(Arrays.asList(items));
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... items) {
        LinkedHashSet<T> values = new LinkedHashSet<T>();
        Collections.addAll(values, items);
        return Collections.unmodifiableSet(values);
    }

    @PostConstruct
    public void initPuzzles() {
        if (puzzleRepository.count() == 0) {
            seedPuzzles();
        }
    }

    private void seedPuzzles() {
        List<SeedPuzzle> seeds = listOf(
            new SeedPuzzle(
                "Swift Mate 1",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♝", "", "♚", "♝", "♞", "♜"),
                    row("", "♟", "", "", "♟", "♟", "♟", "♟"),
                    row("", "", "♟", "♟", "", "", "", ""),
                    row("♟", "♛", "", "", "", "", "", ""),
                    row("", "", "", "", "♙", "", "", ""),
                    row("", "", "", "", "", "", "", "♕"),
                    row("♙", "♙", "♙", "♙", "", "♙", "♙", "♙"),
                    row("♖", "♘", "♗", "", "♔", "♗", "♘", "♖")
                ),
                "WHITE",
                listOf(move(5, 7, 0, 2)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 2",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "", "", "♚", "♝", "♞", "♜"),
                    row("♟", "", "♟", "", "♟", "♟", "♟", "♝"),
                    row("", "", "", "", "", "", "♛", ""),
                    row("", "♟", "", "♟", "", "", "", "♟"),
                    row("♙", "", "", "", "", "", "♕", ""),
                    row("", "♙", "", "", "♙", "", "", "♙"),
                    row("", "♗", "♙", "♙", "", "♙", "♙", "♖"),
                    row("♖", "♘", "", "", "♔", "♗", "♘", "")
                ),
                "WHITE",
                listOf(move(4, 6, 0, 2)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 3",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "♝", "♛", "♚", "", "", "♜"),
                    row("♟", "", "♟", "", "", "", "", ""),
                    row("", "", "", "♟", "", "", "♞", ""),
                    row("", "♙", "", "♕", "", "", "♙", "♟"),
                    row("♞", "", "♙", "♙", "♔", "", "", "♙"),
                    row("", "", "", "", "", "♖", "", ""),
                    row("", "♙", "", "", "♙", "", "♙", ""),
                    row("", "", "♗", "", "", "♗", "♘", "♖")
                ),
                "WHITE",
                listOf(move(3, 3, 1, 5)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 4",
                "White to move finishes the attack with the bishop.",
                listOf(
                    row("♜", "", "♝", "♛", "♚", "♝", "", "♜"),
                    row("", "", "", "♟", "♟", "", "", ""),
                    row("", "", "", "♗", "", "", "♟", "♟"),
                    row("♟", "", "♟", "", "", "♗", "", "♞"),
                    row("", "", "", "", "", "♙", "", ""),
                    row("", "♟", "♞", "", "♙", "♘", "", "♙"),
                    row("", "", "♙", "", "", "", "♙", ""),
                    row("♖", "", "", "", "", "♔", "♘", "♖")
                ),
                "WHITE",
                listOf(move(3, 5, 2, 6)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 5",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "", "♛", "", "♜", "♚", ""),
                    row("", "", "", "", "♟", "", "♝", ""),
                    row("", "", "", "♟", "", "", "", "♟"),
                    row("♟", "♟", "♟", "", "♙", "", "♘", ""),
                    row("", "♙", "", "♙", "", "", "♝", "♖"),
                    row("♗", "", "♙", "♕", "", "", "♘", ""),
                    row("♙", "", "", "", "", "♙", "♙", ""),
                    row("", "", "♖", "", "♔", "♗", "", "")
                ),
                "WHITE",
                listOf(move(5, 3, 1, 7)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 6",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("", "", "♜", "", "♚", "♝", "♞", "♜"),
                    row("", "", "♟", "♛", "♟", "", "♟", "♟"),
                    row("", "", "♕", "", "♙", "♟", "", ""),
                    row("♟", "♟", "", "", "", "", "", ""),
                    row("", "", "", "", "", "", "♝", ""),
                    row("", "", "", "", "", "♙", "♙", "♙"),
                    row("♙", "♙", "♞", "♙", "", "♔", "", ""),
                    row("", "♘", "♗", "", "", "♗", "♘", "♖")
                ),
                "WHITE",
                listOf(move(2, 2, 1, 3)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 7",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "♝", "♛", "♚", "♝", "♞", "♜"),
                    row("", "♟", "", "♟", "♟", "", "♟", ""),
                    row("♞", "", "", "", "", "♟", "", "♟"),
                    row("♟", "", "♟", "", "", "", "", ""),
                    row("", "", "", "♙", "", "", "", "♙"),
                    row("♙", "", "", "♕", "", "", "♙", "♖"),
                    row("", "♙", "♙", "", "♙", "♙", "", ""),
                    row("♖", "♘", "♗", "", "♔", "♗", "♘", "")
                ),
                "WHITE",
                listOf(move(5, 3, 2, 6)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 8",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♝", "", "", "♝", "♞", "♜"),
                    row("", "♟", "", "♚", "", "", "♟", ""),
                    row("", "♙", "", "♟", "", "", "", ""),
                    row("♟", "", "♟", "", "", "♟", "", "♙"),
                    row("♙", "", "", "", "", "", "", "♘"),
                    row("♘", "", "♙", "", "", "", "", "♛"),
                    row("♖", "", "♕", "♙", "♙", "♙", "", ""),
                    row("", "", "♗", "", "♔", "", "", "♖")
                ),
                "BLACK",
                listOf(move(5, 7, 7, 7)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 9",
                "White to move finishes the attack with the knight.",
                listOf(
                    row("♜", "♞", "♝", "♛", "", "♝", "♞", ""),
                    row("♟", "", "", "", "♟", "", "♜", ""),
                    row("", "", "", "♚", "", "", "♟", ""),
                    row("", "", "♟", "♟", "♘", "", "♕", ""),
                    row("", "♟", "", "", "", "", "", ""),
                    row("", "", "♘", "", "♙", "", "♙", "♗"),
                    row("♙", "♙", "♙", "♙", "", "♙", "", ""),
                    row("♖", "", "♗", "", "♔", "", "♖", "")
                ),
                "WHITE",
                listOf(move(5, 2, 3, 1)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Swift Mate 10",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "♝", "♛", "♚", "♝", "♞", "♜"),
                    row("♟", "", "♟", "♟", "", "♟", "♟", ""),
                    row("♞", "♟", "", "", "", "", "", ""),
                    row("", "", "♙", "", "♟", "", "", "♟"),
                    row("♙", "", "", "", "", "", "♙", ""),
                    row("", "", "", "", "", "♙", "", "♙"),
                    row("", "♙", "", "♙", "♙", "", "", ""),
                    row("♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(0, 3, 4, 7)),
                "EASY",
                700,
                1100
            ),
            new SeedPuzzle(
                "Tactical Crush 11",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("", "♞", "♝", "", "♚", "♝", "♞", ""),
                    row("♜", "♟", "", "♟", "♟", "", "♟", ""),
                    row("♟", "♛", "♟", "", "", "♟", "", "♜"),
                    row("", "", "", "", "", "", "", ""),
                    row("", "", "", "♙", "", "", "♙", "♟"),
                    row("♙", "♙", "", "", "", "", "", "♙"),
                    row("", "", "♙", "♔", "♙", "♙", "", ""),
                    row("♖", "♘", "♗", "", "♕", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(2, 1, 4, 3)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 12",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♝", "♛", "♚", "♝", "♞", ""),
                    row("", "", "♟", "♟", "", "", "♟", "♜"),
                    row("♟", "", "", "", "♟", "", "", "♟"),
                    row("", "♟", "", "", "", "♟", "", ""),
                    row("", "", "♙", "♘", "", "♙", "♙", ""),
                    row("", "", "", "", "", "", "", ""),
                    row("♙", "♙", "", "♙", "♙", "", "", "♙"),
                    row("", "♖", "♗", "♕", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(0, 3, 4, 7)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 13",
                "White to move finishes the attack with the bishop.",
                listOf(
                    row("♜", "", "", "", "♚", "♝", "♜", ""),
                    row("♟", "", "♟", "", "♟", "♟", "♟", "♞"),
                    row("♝", "", "♞", "", "", "", "", ""),
                    row("", "", "", "", "", "", "", "♙"),
                    row("♙", "", "", "", "", "♙", "♟", ""),
                    row("", "♙", "", "", "♛", "♗", "", ""),
                    row("", "", "♙", "", "♙", "", "", ""),
                    row("", "♖", "♝", "♕", "", "♔", "♘", "♖")
                ),
                "WHITE",
                listOf(move(5, 5, 2, 2)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 14",
                "Black to move finishes the attack with the bishop.",
                listOf(
                    row("♜", "", "♝", "♚", "", "", "", "♜"),
                    row("", "", "♝", "", "", "♟", "", "♟"),
                    row("", "♟", "", "", "♟", "", "♟", "♞"),
                    row("♟", "♙", "♟", "♟", "", "", "♙", ""),
                    row("", "", "", "♞", "", "", "♙", "♙"),
                    row("", "", "♘", "♙", "", "", "", ""),
                    row("♙", "", "♙", "♕", "♙", "", "", ""),
                    row("", "", "", "♖", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(1, 2, 5, 6)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 15",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♝", "", "♚", "", "♞", "♜"),
                    row("♟", "", "", "♟", "", "", "", ""),
                    row("", "", "♟", "", "♟", "♟", "", ""),
                    row("♟", "", "", "", "", "", "♘", "♟"),
                    row("♖", "", "♙", "", "", "♛", "", ""),
                    row("", "", "♝", "♙", "", "", "", ""),
                    row("", "♙", "", "♗", "♙", "♙", "♙", "♙"),
                    row("", "", "", "", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(4, 5, 6, 3)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 16",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("", "", "♝", "", "♚", "", "♞", "♜"),
                    row("", "", "", "♟", "", "", "♝", ""),
                    row("♜", "♟", "", "", "", "♛", "♟", "♟"),
                    row("♟", "", "♙", "♞", "♘", "♙", "", ""),
                    row("♙", "", "", "", "", "", "♙", ""),
                    row("", "", "♙", "", "", "", "", "♙"),
                    row("♖", "", "", "♙", "♙", "", "", ""),
                    row("", "♘", "♗", "♕", "♔", "♗", "", "♖")
                ),
                "BLACK",
                listOf(move(2, 5, 4, 7)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 17",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♝", "", "♚", "", "♞", "♜"),
                    row("", "♟", "♟", "♟", "", "♟", "", ""),
                    row("♟", "", "♗", "", "♟", "", "♟", ""),
                    row("", "", "♝", "", "", "", "", "♟"),
                    row("", "", "", "", "♙", "", "", "♛"),
                    row("♘", "♙", "", "", "", "♙", "", ""),
                    row("♙", "", "♙", "♙", "", "", "♙", ""),
                    row("", "♖", "♗", "♕", "", "♔", "♘", "♖")
                ),
                "BLACK",
                listOf(move(4, 7, 6, 5)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 18",
                "Black to move finishes the attack with the pawn.",
                listOf(
                    row("♜", "", "♝", "", "", "♝", "♞", "♜"),
                    row("♟", "♟", "♟", "♟", "♚", "", "♟", "♟"),
                    row("", "", "", "", "", "", "", ""),
                    row("♞", "", "", "", "♟", "♟", "", ""),
                    row("", "♛", "", "♙", "", "", "", ""),
                    row("♘", "", "", "♔", "♙", "", "♙", "♙"),
                    row("♙", "♙", "♙", "♕", "♗", "♙", "", ""),
                    row("♖", "", "", "", "", "", "♘", "♖")
                ),
                "BLACK",
                listOf(move(3, 4, 4, 4)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 19",
                "Black to move finishes the attack with the bishop.",
                listOf(
                    row("♜", "♞", "♝", "♛", "♚", "", "♜", ""),
                    row("", "", "", "", "♞", "♟", "", ""),
                    row("♟", "", "♟", "", "♟", "", "", ""),
                    row("", "♟", "♙", "", "", "", "♙", "♟"),
                    row("♙", "", "", "", "", "♝", "♙", ""),
                    row("", "", "♘", "♖", "", "♙", "", ""),
                    row("", "♙", "", "♙", "♙", "", "", ""),
                    row("", "", "♗", "♕", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(4, 5, 5, 6)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Tactical Crush 20",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("", "♜", "♝", "", "♜", "", "", ""),
                    row("♟", "♟", "", "", "♚", "♟", "", ""),
                    row("♘", "", "♟", "", "♟", "", "♟", ""),
                    row("♝", "", "", "", "", "", "♙", "♙"),
                    row("♙", "♙", "", "♛", "", "", "", ""),
                    row("", "", "♙", "", "", "", "", ""),
                    row("", "", "", "♙", "♙", "", "", "♙"),
                    row("", "♖", "♗", "♕", "♔", "♗", "♘", "♖")
                ),
                "BLACK",
                listOf(move(4, 3, 4, 7)),
                "MEDIUM",
                1300,
                1700
            ),
            new SeedPuzzle(
                "Master Sequence 21",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "", "♛", "♚", "♝", "♞", "♜"),
                    row("♟", "♟", "♟", "♞", "", "", "♟", ""),
                    row("", "", "", "♟", "", "", "", ""),
                    row("", "", "", "", "♟", "♕", "", "♟"),
                    row("", "", "♗", "♙", "♙", "", "", ""),
                    row("", "", "", "", "", "", "", "♘"),
                    row("♝", "♙", "♙", "", "", "♙", "♙", "♙"),
                    row("♖", "♘", "♗", "", "♔", "♖", "", "")
                ),
                "WHITE",
                listOf(move(3, 5, 1, 5)),
                "HARD",
                1850,
                2250
            ),
            new SeedPuzzle(
                "Master Sequence 22",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "", "♛", "", "♝", "♞", "♜"),
                    row("♞", "", "", "♝", "♟", "", "", ""),
                    row("", "♟", "♟", "", "♙", "", "♟", "♟"),
                    row("", "", "♚", "", "", "♟", "", ""),
                    row("♗", "♟", "♙", "♘", "", "", "♙", ""),
                    row("♙", "", "", "♙", "", "", "", ""),
                    row("", "♗", "", "♔", "", "♙", "", "♙"),
                    row("♖", "♘", "", "", "♕", "", "♖", "")
                ),
                "WHITE",
                listOf(move(7, 4, 3, 4)),
                "HARD",
                1850,
                2250
            ),
            new SeedPuzzle(
                "Master Sequence 23",
                "Black to move finishes the attack with the queen.",
                listOf(
                    row("", "♛", "♝", "", "♚", "♝", "", "♜"),
                    row("♞", "♟", "", "♟", "♟", "♟", "♟", ""),
                    row("", "", "", "", "", "♞", "", ""),
                    row("♙", "", "", "♙", "", "", "♘", "♙"),
                    row("♙", "", "", "", "", "", "", ""),
                    row("", "", "", "", "", "", "", "♜"),
                    row("", "", "♙", "", "♙", "♙", "♗", "♙"),
                    row("♖", "", "♗", "♕", "", "♖", "♔", "")
                ),
                "BLACK",
                listOf(move(0, 1, 6, 7)),
                "HARD",
                1850,
                2250
            ),
            new SeedPuzzle(
                "Master Sequence 24",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "", "♝", "♛", "", "♝", "♞", "♜"),
                    row("♟", "", "", "♟", "♞", "♟", "♟", "♟"),
                    row("", "", "", "", "♚", "", "", ""),
                    row("", "♗", "♟", "", "♟", "", "", "♕"),
                    row("", "", "", "♙", "♙", "", "", ""),
                    row("♙", "", "", "", "", "", "♘", ""),
                    row("♖", "♙", "♙", "", "", "♙", "♙", "♙"),
                    row("", "", "♗", "", "♔", "", "♘", "♖")
                ),
                "WHITE",
                listOf(move(3, 7, 3, 4)),
                "HARD",
                1850,
                2250
            ),
            new SeedPuzzle(
                "Master Sequence 25",
                "White to move finishes the attack with the queen.",
                listOf(
                    row("♜", "♞", "♚", "", "", "", "♜", ""),
                    row("♟", "♝", "♟", "", "", "", "", "♟"),
                    row("", "♟", "", "", "♟", "", "", ""),
                    row("", "♗", "", "♟", "♙", "♟", "♕", ""),
                    row("", "♖", "", "", "", "", "", ""),
                    row("", "♛", "♝", "", "", "", "", ""),
                    row("♙", "", "♙", "♙", "♔", "♙", "♙", ""),
                    row("", "♖", "♗", "", "", "", "♘", "")
                ),
                "WHITE",
                listOf(move(3, 6, 0, 6)),
                "HARD",
                1850,
                2250
            )
        );

        for (SeedPuzzle seed : seeds) {
            try {
                String boardJson = objectMapper.writeValueAsString(seed.board);
                String movesJson = objectMapper.writeValueAsString(seed.moves);
                Puzzle puzzle = new Puzzle();
                puzzle.setName(seed.name);
                puzzle.setDescription(seed.description);
                puzzle.setBoardState(boardJson);
                puzzle.setTurn(seed.turn);
                puzzle.setSolution(movesJson);
                puzzle.setDifficulty(seed.difficulty);
                if (seed.ratingLow != null && seed.ratingHigh != null) {
                    puzzle.setRatingLow(seed.ratingLow);
                    puzzle.setRatingHigh(seed.ratingHigh);
                    puzzle.setRating((seed.ratingLow + seed.ratingHigh) / 2);
                } else {
                    puzzle.setRating(1500);
                    puzzle.setRatingLow(null);
                    puzzle.setRatingHigh(null);
                }
                puzzleRepository.save(puzzle);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to seed default puzzles", e);
            }
        }
    }

    private static List<String> row(String... values) {
        return listOf(values);
    }

    private static MoveData move(int fromRow, int fromCol, int toRow, int toCol) {
        return move(fromRow, fromCol, toRow, toCol, null);
    }

    private static MoveData move(int fromRow, int fromCol, int toRow, int toCol, String promotion) {
        MoveData move = new MoveData();
        move.setFromRow(fromRow);
        move.setFromCol(fromCol);
        move.setToRow(toRow);
        move.setToCol(toCol);
        if (promotion != null && !promotion.isEmpty()) {
            move.setPromotion(promotion);
        }
        return move;
    }

    private static final class SeedPuzzle {
        final String name;
        final String description;
        final List<List<String>> board;
        final String turn;
        final List<MoveData> moves;
        final String difficulty;
        final Integer ratingLow;
        final Integer ratingHigh;

        SeedPuzzle(String name,
                   String description,
                   List<List<String>> board,
                   String turn,
                   List<MoveData> moves,
                   String difficulty,
                   Integer ratingLow,
                   Integer ratingHigh) {
            this.name = name;
            this.description = description;
            this.board = board;
            this.turn = turn;
            this.moves = moves;
            this.difficulty = difficulty;
            this.ratingLow = ratingLow;
            this.ratingHigh = ratingHigh;
        }
    }

    public List<Puzzle> getAllPuzzles() {
        return puzzleRepository.findAll();
    }

    public Puzzle getPuzzle(Long id) {
        return puzzleRepository.findById(id).orElse(null);
    }

    public List<Puzzle> getPuzzlesByDifficulty(String difficulty) {
        return puzzleRepository.findByDifficulty(difficulty);
    }

    public PuzzleEvaluationResult checkSolution(Long puzzleId, List<MoveData> moves, User user, Long elapsedMillis) {
        Puzzle puzzle = getPuzzle(puzzleId);
        if (puzzle == null) {
            return PuzzleEvaluationResult.error("Puzzle not found.");
        }

        if (moves == null || moves.isEmpty()) {
            return PuzzleEvaluationResult.wrong("Make a move first.");
        }

        try {
            List<MoveData> solution = objectMapper.readValue(puzzle.getSolution(), new TypeReference<List<MoveData>>() {});
            if (moves.size() > solution.size()) {
                return PuzzleEvaluationResult.wrong("That move is not part of the solution.");
            }

            for (int i = 0; i < moves.size(); i++) {
                MoveData playerMove = moves.get(i);
                MoveData solutionMove = solution.get(i);
                if (playerMove.getFromRow() != solutionMove.getFromRow() ||
                        playerMove.getFromCol() != solutionMove.getFromCol() ||
                        playerMove.getToRow() != solutionMove.getToRow() ||
                        playerMove.getToCol() != solutionMove.getToCol()) {
                    return PuzzleEvaluationResult.wrong("Wrong move. Try again!");
                }

                String expectedPromotion = normalizePromotion(solutionMove.getPromotion());
                String playerPromotion = normalizePromotion(playerMove.getPromotion());
                if (!Objects.equals(expectedPromotion, playerPromotion)) {
                    return PuzzleEvaluationResult.wrong("Wrong promotion choice. Try again!");
                }
            }

            boolean solved = moves.size() == solution.size();
            if (solved) {
                Long durationSeconds = null;
                if (elapsedMillis != null && elapsedMillis >= 0) {
                    durationSeconds = Math.max(1L, (long) Math.ceil(elapsedMillis / 1000.0));
                }

                if (user != null) {
                    PuzzleSolve solve = puzzleSolveRepository.findByUserAndPuzzle(user, puzzle);
                    if (solve == null) {
                        solve = new PuzzleSolve();
                        solve.setUser(user);
                        solve.setPuzzle(puzzle);
                    }
                    solve.setSolvedAt(LocalDateTime.now());
                    if (durationSeconds != null) {
                        Long existingDuration = solve.getSolveDurationSeconds();
                        if (existingDuration == null || durationSeconds < existingDuration) {
                            solve.setSolveDurationSeconds(durationSeconds);
                        }
                    }
                    puzzleSolveRepository.save(solve);
                }
                return PuzzleEvaluationResult.solved("Correct! Puzzle solved.", durationSeconds);
            }

            return PuzzleEvaluationResult.progress("Correct move! Keep going.");
        } catch (Exception e) {
            return PuzzleEvaluationResult.error("Unable to verify move.");
        }
    }

    public Puzzle createPuzzle(String name,
                               String description,
                               List<List<String>> boardState,
                               String turn,
                               List<MoveData> solutionMoves,
                               String difficulty,
                               Integer ratingLow,
                               Integer ratingHigh) {
        String trimmedName = name != null ? name.trim() : "";
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Puzzle name is required.");
        }

        String normalizedTurn = turn != null ? turn.trim().toUpperCase() : "WHITE";
        if (!"WHITE".equals(normalizedTurn) && !"BLACK".equals(normalizedTurn)) {
            throw new IllegalArgumentException("Turn must be WHITE or BLACK.");
        }

        String normalizedDifficulty = difficulty != null ? difficulty.trim().toUpperCase() : "MEDIUM";
        if (!ALLOWED_DIFFICULTIES.contains(normalizedDifficulty)) {
            normalizedDifficulty = "MEDIUM";
        }

        List<List<String>> normalizedBoard = normalizeBoardState(boardState);
        boolean hasPieces = normalizedBoard.stream()
                .flatMap(List::stream)
                .anyMatch(cell -> cell != null && !cell.isEmpty());
        if (!hasPieces) {
            throw new IllegalArgumentException("Place at least one piece on the board.");
        }

        if (solutionMoves == null || solutionMoves.isEmpty()) {
            throw new IllegalArgumentException("Record the solution sequence before saving.");
        }

        solutionMoves.forEach(this::validateMove);

        Integer normalizedRatingLow = normalizeRatingValue(ratingLow);
        Integer normalizedRatingHigh = normalizeRatingValue(ratingHigh);
        if ((normalizedRatingLow == null) != (normalizedRatingHigh == null)) {
            throw new IllegalArgumentException("Provide both minimum and maximum rating when specifying a range.");
        }
        if (normalizedRatingLow != null && normalizedRatingHigh != null && normalizedRatingLow > normalizedRatingHigh) {
            throw new IllegalArgumentException("Rating minimum cannot exceed rating maximum.");
        }

        try {
            String boardJson = objectMapper.writeValueAsString(normalizedBoard);
            String movesJson = objectMapper.writeValueAsString(solutionMoves);
            Puzzle puzzle = new Puzzle();
            puzzle.setName(trimmedName);
            puzzle.setDescription(description != null ? description.trim() : "");
            puzzle.setBoardState(boardJson);
            puzzle.setTurn(normalizedTurn);
            puzzle.setSolution(movesJson);
            puzzle.setDifficulty(normalizedDifficulty);
            if (normalizedRatingLow != null && normalizedRatingHigh != null) {
                puzzle.setRatingLow(normalizedRatingLow);
                puzzle.setRatingHigh(normalizedRatingHigh);
                puzzle.setRating((normalizedRatingLow + normalizedRatingHigh) / 2);
            } else {
                puzzle.setRating(1500);
                puzzle.setRatingLow(null);
                puzzle.setRatingHigh(null);
            }
            return puzzleRepository.save(puzzle);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to store puzzle data.");
        }
    }

    private List<List<String>> normalizeBoardState(List<List<String>> boardState) {
        List<List<String>> normalized = new ArrayList<>(8);
        for (int row = 0; row < 8; row++) {
            List<String> normalizedRow = new ArrayList<>(8);
            for (int col = 0; col < 8; col++) {
                String value = "";
                if (boardState != null && row < boardState.size()) {
                    List<String> sourceRow = boardState.get(row);
                    if (sourceRow != null && col < sourceRow.size()) {
                        String raw = sourceRow.get(col);
                        if (raw != null) {
                            String trimmed = raw.trim();
                            if (ALLOWED_PIECES.contains(trimmed)) {
                                value = trimmed;
                            }
                        }
                    }
                }
                normalizedRow.add(value);
            }
            normalized.add(normalizedRow);
        }
        return normalized;
    }

    private void validateMove(MoveData move) {
        if (move == null) {
            throw new IllegalArgumentException("Solution contains an invalid move.");
        }
        if (!isOnBoard(move.getFromRow(), move.getFromCol()) || !isOnBoard(move.getToRow(), move.getToCol())) {
            throw new IllegalArgumentException("Solution moves must remain on the board.");
        }
        String normalizedPromotion = normalizePromotion(move.getPromotion());
        if (move.getPromotion() != null && normalizedPromotion == null) {
            throw new IllegalArgumentException("Solution contains an unsupported promotion piece.");
        }
        move.setPromotion(normalizedPromotion);
    }

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private String normalizePromotion(String promotion) {
        if (promotion == null) {
            return null;
        }
        String trimmed = promotion.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String upper = trimmed.toUpperCase();
        return ALLOWED_PROMOTIONS.contains(upper) ? upper : null;
    }

    public Set<Long> getSolvedPuzzleIds(User user) {
        if (user == null) {
            return Collections.emptySet();
        }
        return puzzleSolveRepository.findByUser(user).stream()
                .map(PuzzleSolve::getPuzzle)
                .filter(Objects::nonNull)
                .map(Puzzle::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Integer normalizeRatingValue(Integer rating) {
        if (rating == null) {
            return null;
        }
        if (rating < 100 || rating > 3500) {
            throw new IllegalArgumentException("Rating range must be between 100 and 3500.");
        }
        return rating;
    }

    public enum PuzzleEvaluationStatus {
        WRONG,
        PROGRESS,
        SOLVED,
        ERROR
    }

    public static class PuzzleEvaluationResult {
        private final PuzzleEvaluationStatus status;
        private final String message;
        private final Long recordedDurationSeconds;

        private PuzzleEvaluationResult(PuzzleEvaluationStatus status, String message, Long recordedDurationSeconds) {
            this.status = status;
            this.message = message;
            this.recordedDurationSeconds = recordedDurationSeconds;
        }

        public static PuzzleEvaluationResult wrong(String message) {
            return new PuzzleEvaluationResult(PuzzleEvaluationStatus.WRONG, message, null);
        }

        public static PuzzleEvaluationResult progress(String message) {
            return new PuzzleEvaluationResult(PuzzleEvaluationStatus.PROGRESS, message, null);
        }

        public static PuzzleEvaluationResult solved(String message, Long recordedDurationSeconds) {
            return new PuzzleEvaluationResult(PuzzleEvaluationStatus.SOLVED, message, recordedDurationSeconds);
        }

        public static PuzzleEvaluationResult error(String message) {
            return new PuzzleEvaluationResult(PuzzleEvaluationStatus.ERROR, message, null);
        }

        public PuzzleEvaluationStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Long getRecordedDurationSeconds() {
            return recordedDurationSeconds;
        }

        public boolean isCorrect() {
            return status == PuzzleEvaluationStatus.PROGRESS || status == PuzzleEvaluationStatus.SOLVED;
        }

        public boolean isSolved() {
            return status == PuzzleEvaluationStatus.SOLVED;
        }
    }

    public static class MoveData {
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;
        private String promotion;

        public MoveData() {}

        public int getFromRow() { return fromRow; }
        public void setFromRow(int fromRow) { this.fromRow = fromRow; }
        public int getFromCol() { return fromCol; }
        public void setFromCol(int fromCol) { this.fromCol = fromCol; }
        public int getToRow() { return toRow; }
        public void setToRow(int toRow) { this.toRow = toRow; }
        public int getToCol() { return toCol; }
        public void setToCol(int toCol) { this.toCol = toCol; }
        public String getPromotion() { return promotion; }
        public void setPromotion(String promotion) { this.promotion = promotion; }
    }
}
