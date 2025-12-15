package com.vishwask.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vishwask.chess.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class PuzzleController {

    @Autowired
    private PuzzleService puzzleService;

    @Autowired
    private UserService userService;

    @GetMapping("/puzzles")
    public String puzzles(@RequestParam(required = false) String difficulty, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(auth.getName());
        model.addAttribute("currentUser", currentUser);

        List<Puzzle> puzzles;
        if (difficulty != null && !difficulty.isEmpty()) {
            puzzles = puzzleService.getPuzzlesByDifficulty(difficulty.toUpperCase());
        } else {
            puzzles = puzzleService.getAllPuzzles();
        }
        
        model.addAttribute("puzzles", puzzles);
        model.addAttribute("solvedPuzzleIds", puzzleService.getSolvedPuzzleIds(currentUser));
        model.addAttribute("selectedDifficulty", difficulty != null ? difficulty.toUpperCase() : "ALL");
        return "puzzles";
    }

    @GetMapping("/puzzle/{id}")
    public String puzzle(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        User currentUser = userService.findByUsername(auth.getName());
        Puzzle puzzle = puzzleService.getPuzzle(id);
        
        if (puzzle == null) {
            return "redirect:/puzzles";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("puzzle", puzzle);
        return "puzzle";
    }

    @GetMapping("/puzzles/create")
    public String createPuzzle(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        return "puzzle-create";
    }

    @PostMapping("/api/puzzle/{id}/check")
    @ResponseBody
    public CheckResult checkPuzzle(@PathVariable Long id,
                                   @RequestBody PuzzleCheckRequest request,
                                   Authentication auth) {
        User currentUser = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            currentUser = userService.findByUsername(auth.getName());
        }
        List<PuzzleService.MoveData> moves = request != null ? request.getMoves() : null;
        Long elapsedMillis = request != null ? request.getElapsedMillis() : null;
        PuzzleService.PuzzleEvaluationResult result = puzzleService.checkSolution(id, moves, currentUser, elapsedMillis);
        return new CheckResult(
                result.isCorrect(),
                result.isSolved(),
                result.getStatus().name(),
                result.getMessage(),
                result.getRecordedDurationSeconds()
        );
    }

    @PostMapping("/api/puzzles")
    @ResponseBody
    public ResponseEntity<CreatePuzzleResponse> createPuzzle(@RequestBody PuzzleCreationRequest request,
                                                             Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CreatePuzzleResponse(false, null, "Please log in to create puzzles."));
        }
        try {
            Puzzle puzzle = puzzleService.createPuzzle(
                    request != null ? request.getName() : null,
                    request != null ? request.getDescription() : null,
                    request != null ? request.getBoardState() : null,
                    request != null ? request.getTurn() : null,
                    request != null ? request.getMoves() : null,
                    request != null ? request.getDifficulty() : null,
                    request != null ? request.getRatingLow() : null,
                    request != null ? request.getRatingHigh() : null
            );
            return ResponseEntity.ok(new CreatePuzzleResponse(true, puzzle.getId(), "Puzzle created successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new CreatePuzzleResponse(false, null, ex.getMessage()));
        }
    }

    public static class PuzzleCheckRequest {
        private List<PuzzleService.MoveData> moves;
        private Long elapsedMillis;

        public List<PuzzleService.MoveData> getMoves() {
            return moves;
        }

        public void setMoves(List<PuzzleService.MoveData> moves) {
            this.moves = moves;
        }

        public Long getElapsedMillis() {
            return elapsedMillis;
        }

        public void setElapsedMillis(Long elapsedMillis) {
            this.elapsedMillis = elapsedMillis;
        }
    }

    public static class PuzzleCreationRequest {
        private String name;
        private String description;
        private List<List<String>> boardState;
        private String turn;
        private List<PuzzleService.MoveData> moves;
        private String difficulty;
        private List<PuzzleService.MoveData> solution;
        @JsonProperty("ratingLow")
        private Integer ratingLow;
        @JsonProperty("ratingHigh")
        private Integer ratingHigh;

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

        public List<List<String>> getBoardState() {
            return boardState;
        }

        public void setBoardState(List<List<String>> boardState) {
            this.boardState = boardState;
        }

        public String getTurn() {
            return turn;
        }

        public void setTurn(String turn) {
            this.turn = turn;
        }

        public List<PuzzleService.MoveData> getMoves() {
            return moves;
        }

        public void setMoves(List<PuzzleService.MoveData> moves) {
            this.moves = moves;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public List<PuzzleService.MoveData> getSolution() {
            return solution;
        }

        public void setSolution(List<PuzzleService.MoveData> solution) {
            this.solution = solution;
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

    public static class CreatePuzzleResponse {
        private boolean success;
        private Long puzzleId;
        private String message;

        public CreatePuzzleResponse(boolean success, Long puzzleId, String message) {
            this.success = success;
            this.puzzleId = puzzleId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Long getPuzzleId() {
            return puzzleId;
        }

        public void setPuzzleId(Long puzzleId) {
            this.puzzleId = puzzleId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class CheckResult {
        private boolean correct;
        private boolean solved;
        private String status;
        private String message;
        private Long recordedDurationSeconds;

        public CheckResult(boolean correct, boolean solved, String status, String message, Long recordedDurationSeconds) {
            this.correct = correct;
            this.solved = solved;
            this.status = status;
            this.message = message;
            this.recordedDurationSeconds = recordedDurationSeconds;
        }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
        public boolean isSolved() { return solved; }
        public void setSolved(boolean solved) { this.solved = solved; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getRecordedDurationSeconds() { return recordedDurationSeconds; }
        public void setRecordedDurationSeconds(Long recordedDurationSeconds) { this.recordedDurationSeconds = recordedDurationSeconds; }
    }
}
