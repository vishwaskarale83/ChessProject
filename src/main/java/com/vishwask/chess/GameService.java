package com.vishwask.chess;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class GameService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private GameRepository gameRepository;

    private Map<String, ChessBoard> gameBoards = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    private Random random = new Random();

    private void resetOutcome(Game game) {
        game.setResult(GameOutcome.UNDECIDED);
        game.setEndType(null);
    }

    private void resetOutcomeIfActive(Game game) {
        if (game.getStatus() != GameStatus.FINISHED) {
            resetOutcome(game);
        }
    }

    private void finalizeGame(Game game, GameOutcome outcome, GameEndType endType, String customMessage) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(outcome != null ? outcome : GameOutcome.UNDECIDED);
        game.setEndType(endType);
        game.setTurnStartAt(null);
        game.setDrawOfferBy(null);
        game.setDrawOfferAt(null);
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            game.setLastMessage(customMessage);
        } else {
            game.setLastMessage(buildOutcomeSummary(game));
        }
    }

    private void summarizeFinishedGame(Game game) {
        String current = game.getLastMessage();
        if (current == null || current.trim().isEmpty() || "Game is not active.".equals(current)) {
            game.setLastMessage(buildOutcomeSummary(game));
        }
    }

    private String buildOutcomeSummary(Game game) {
        GameOutcome outcome = game.getResult();
        GameEndType endType = game.getEndType();

        String base;
        if (outcome == GameOutcome.WHITE_WIN) {
            base = "Game over: White wins";
        } else if (outcome == GameOutcome.BLACK_WIN) {
            base = "Game over: Black wins";
        } else if (outcome == GameOutcome.DRAW) {
            base = "Game over: Draw";
        } else {
            base = "Game over";
        }

        String reason = describeEndType(endType, outcome);
        if (!reason.isEmpty()) {
            base = base + " " + reason;
        }
        if (!base.endsWith(".")) {
            base = base + ".";
        }
        return base;
    }

    private String describeEndType(GameEndType endType, GameOutcome outcome) {
        if (endType == null) {
            return "";
        }
        switch (endType) {
            case CHECKMATE:
                return "by checkmate";
            case STALEMATE:
                return "by stalemate";
            case RESIGNATION:
                return "by resignation";
            case TIMEOUT:
                return "by timeout";
            case KING_CAPTURE:
                return outcome == GameOutcome.DRAW ? "after both kings were captured" : "by capturing the king";
            case AGREED_DRAW:
                return "by agreement";
            case OTHER:
            default:
                return "";
        }
    }

    private String formatColorLabel(Color color) {
        if (color == null) {
            return "";
        }
        String lower = color.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public Game createGame(User hostPlayer) {
        return createGame(hostPlayer, null);
    }

    public Game createGame(User hostPlayer, Integer initialTimeSeconds) {
        Game game = new Game();
        game.setGameUuid(java.util.UUID.randomUUID().toString());
        game.setGameName(hostPlayer.getUsername() + "'s Game");

        boolean hostAsWhite = random.nextBoolean();
        if (hostAsWhite) {
            game.setWhitePlayer(hostPlayer);
            game.setLastMessage("Waiting for opponent to join as Black...");
        } else {
            game.setBlackPlayer(hostPlayer);
            game.setLastMessage("Waiting for opponent to join as White...");
        }

        resetOutcomeIfActive(game);
        configureTimeControls(game, initialTimeSeconds);
        ChessBoard board = new ChessBoard();
        gameBoards.put(game.getGameUuid(), board);
        game.setBoardState(serializeBoard(board.getBoardState()));
        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    private PieceType resolvePromotionChoice(String promotion, Piece movingPiece, int destinationRow) {
        if (movingPiece == null || movingPiece.getType() != PieceType.PAWN) {
            return null;
        }
        if (destinationRow != 0 && destinationRow != 7) {
            return null;
        }
        if (promotion == null || promotion.trim().isEmpty()) {
            return PieceType.QUEEN;
        }
        try {
            PieceType choice = PieceType.valueOf(promotion.trim().toUpperCase());
            switch (choice) {
                case QUEEN:
                case ROOK:
                case BISHOP:
                case KNIGHT:
                    return choice;
                default:
                    return PieceType.QUEEN;
            }
        } catch (IllegalArgumentException ex) {
            return PieceType.QUEEN;
        }
    }

    public Game createGameVsAi(User player, Color playerColor) {
        Game game = new Game();
        game.setGameUuid(java.util.UUID.randomUUID().toString());
        game.setGameType(GameType.HUMAN_VS_AI);
        game.setPlayerColor(playerColor);
        game.setStatus(GameStatus.ACTIVE);
        resetOutcomeIfActive(game);
        
        // Assign player to correct side
        if (playerColor == Color.WHITE) {
            game.setWhitePlayer(player);
            game.setGameName(player.getUsername() + " vs AI (Black)");
        } else {
            game.setBlackPlayer(player);
            game.setGameName(player.getUsername() + " vs AI (White)");
        }
        
        ChessBoard board = new ChessBoard();
        gameBoards.put(game.getGameUuid(), board);
        game.setBoardState(serializeBoard(board.getBoardState()));
        game.setLastMoveAt(LocalDateTime.now());
        configureTimeControls(game, null);
        
        // If AI plays white, make first move
        if (playerColor == Color.BLACK) {
            boolean aiMoved = makeAIMove(board, Color.WHITE);
            if (aiMoved) {
                applyLastMoveMetadata(game, board, Color.WHITE);
                game.setCurrentTurn(Color.BLACK);
                game.setBoardState(serializeBoard(board.getBoardState()));
                game.setMoveCount(1);
                game.setLastMessage("AI played White. Your turn.");
            }
        } else {
            game.setLastMessage("");
        }
        
        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        simpMessagingTemplate.convertAndSend("/topic/game/" + savedGame.getGameUuid(), savedGame);
        return savedGame;
    }

    public Game joinGame(String gameUuid, User joiningPlayer) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }

        boolean isReturningWhite = game.getWhitePlayer() != null && game.getWhitePlayer().getId().equals(joiningPlayer.getId());
        boolean isReturningBlack = game.getBlackPlayer() != null && game.getBlackPlayer().getId().equals(joiningPlayer.getId());

        if (isReturningWhite || isReturningBlack) {
            populateValidMoves(game);
            return game;
        }

        if (game.getStatus() == GameStatus.WAITING) {
            if (game.getWhitePlayer() == null && game.getBlackPlayer() == null) {
                game.setWhitePlayer(joiningPlayer);
                game.setLastMessage("Waiting for opponent to join as Black...");
                Game savedLobbyUpdate = gameRepository.save(game);
                populateValidMoves(savedLobbyUpdate);
                return savedLobbyUpdate;
            }

            if (game.getWhitePlayer() == null) {
                game.setWhitePlayer(joiningPlayer);
            } else if (game.getBlackPlayer() == null) {
                game.setBlackPlayer(joiningPlayer);
            } else {
                throw new RuntimeException("Game already has two players");
            }

            game.setStatus(GameStatus.ACTIVE);
            resetOutcome(game);
            game.setLastMessage("");
            game.setLastMoveAt(java.time.LocalDateTime.now());
            if (isTimeControlEnabled(game)) {
                if (game.getWhiteTimeRemainingSeconds() == null || game.getBlackTimeRemainingSeconds() == null) {
                    game.setWhiteTimeRemainingSeconds(game.getInitialTimeSeconds());
                    game.setBlackTimeRemainingSeconds(game.getInitialTimeSeconds());
                }
                game.setTurnStartAt(LocalDateTime.now());
            }
            Game savedGame = gameRepository.save(game);
            populateValidMoves(savedGame);
            simpMessagingTemplate.convertAndSend("/topic/game/" + savedGame.getGameUuid(), savedGame);
            return savedGame;
        }

        throw new RuntimeException("Game is not available to join");
    }

    public Game makeMove(String gameUuid, User player, int fromRow, int fromCol, int toRow, int toCol, String promotion) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }
        resetOutcomeIfActive(game);
        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setLastMessage("Game is not active.");
            if (game.getStatus() == GameStatus.FINISHED) {
                summarizeFinishedGame(game);
            }
            populateValidMoves(game);
            return game;
        }

        ChessBoard board = ensureBoardLoaded(game);

        // Determine player's color and eligibility
        Color playerColor = resolvePlayerColor(game, player);
        if (playerColor == null) {
            game.setLastMessage("You are not part of this game.");
            populateValidMoves(game);
            return game;
        }

        if (game.getGameType() == GameType.HUMAN_VS_AI) {
            Color humanColor = game.getPlayerColor() != null ? game.getPlayerColor() : Color.WHITE;
            Color aiColor = humanColor == Color.WHITE ? Color.BLACK : Color.WHITE; // AI always plays the opposite color selected by the human
            if (game.getCurrentTurn() == aiColor) {
                game.setLastMessage("Wait for AI to move.");
                populateValidMoves(game);
                return game;
            }
        }

        if (game.getCurrentTurn() != playerColor) {
            game.setLastMessage("Not your turn.");
            populateValidMoves(game);
            return game;
        }

        LocalDateTime now = LocalDateTime.now();

        if (isTimeControlEnabled(game)) {
            boolean hasTime = deductTimeForCurrentPlayer(game, now);
            if (!hasTime) {
                handleTimeout(game, playerColor);
                Game timedOutGame = gameRepository.save(game);
                populateValidMoves(timedOutGame);
                return timedOutGame;
            }
            game.setTurnStartAt(now);
        }

        PieceType promotionChoice = resolvePromotionChoice(promotion, board.getPiece(fromRow, fromCol), toRow);
        if (!board.movePiece(fromRow, fromCol, toRow, toCol, promotionChoice)) {
            game.setLastMessage("Invalid move.");
            resetOutcomeIfActive(game);
            Game savedGame = gameRepository.save(game);
            populateValidMoves(savedGame);
            return savedGame;
        }

        applyLastMoveMetadata(game, board, playerColor);
        game.setCurrentTurn(game.getCurrentTurn() == Color.WHITE ? Color.BLACK : Color.WHITE);
        game.setBoardState(serializeBoard(board.getBoardState()));
        game.setLastMoveAt(LocalDateTime.now());
        game.setMoveCount(game.getMoveCount() + 1);
        game.setDrawOfferBy(null);
        game.setDrawOfferAt(null);
        resetOutcomeIfActive(game);
        updateStatus(game, board);

        if (isTimeControlEnabled(game) && game.getStatus() == GameStatus.ACTIVE) {
            game.setTurnStartAt(LocalDateTime.now());
        }

        // Trigger AI move if applicable and game still active
        if (game.getStatus() == GameStatus.ACTIVE
                && game.getGameType() == GameType.HUMAN_VS_AI
                && game.getCurrentTurn() != game.getPlayerColor()) {
            Color aiColor = game.getCurrentTurn();
            boolean aiMoved = makeAIMove(board, aiColor);
            if (aiMoved) {
                applyLastMoveMetadata(game, board, aiColor);
                game.setCurrentTurn(game.getPlayerColor());
                game.setBoardState(serializeBoard(board.getBoardState()));
                game.setLastMoveAt(LocalDateTime.now());
                game.setMoveCount(game.getMoveCount() + 1);
                game.setDrawOfferBy(null);
                game.setDrawOfferAt(null);
                resetOutcomeIfActive(game);
                updateStatus(game, board);

                if (isTimeControlEnabled(game) && game.getStatus() == GameStatus.ACTIVE) {
                    game.setTurnStartAt(LocalDateTime.now());
                }

                Game savedGameAfterAI = gameRepository.save(game);
                populateValidMoves(savedGameAfterAI);
                simpMessagingTemplate.convertAndSend("/topic/game/" + gameUuid, savedGameAfterAI);
                return savedGameAfterAI;

            } else {
                Color humanColor = game.getPlayerColor() != null ? game.getPlayerColor() : Color.WHITE;
                GameOutcome outcome = humanColor == Color.WHITE ? GameOutcome.WHITE_WIN : GameOutcome.BLACK_WIN;
                finalizeGame(game, outcome, GameEndType.OTHER, "AI has no moves. You win!");
            }
        }

        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    public List<Game> getAvailableGames() {
        return gameRepository.findByStatus(GameStatus.WAITING);
    }

    public List<Game> getUserGames(User user) {
        return gameRepository.findByWhitePlayerOrBlackPlayer(user, user);
    }

    public Game getGame(String gameUuid) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }

        ensureBoardLoaded(game);
        populateValidMoves(game);
        return game;
    }

    private Color resolvePlayerColor(Game game, User player) {
        if (game.getWhitePlayer() != null && game.getWhitePlayer().getId().equals(player.getId())) {
            return Color.WHITE;
        }
        if (game.getBlackPlayer() != null && game.getBlackPlayer().getId().equals(player.getId())) {
            return Color.BLACK;
        }
        return null;
    }

    private void populateValidMoves(Game game) {
        ChessBoard board = ensureBoardLoaded(game);
        if (game.getStatus() == GameStatus.ACTIVE || game.getStatus() == GameStatus.WAITING) {
             game.setValidMoves(board.getAllValidMoves(game.getCurrentTurn()));
        }
    }

    public Game resignGame(String gameUuid, User player) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }
        resetOutcomeIfActive(game);
        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setLastMessage("Game is not active.");
            if (game.getStatus() == GameStatus.FINISHED) {
                summarizeFinishedGame(game);
            }
            populateValidMoves(game);
            return game;
        }

        Color resigningColor = resolvePlayerColor(game, player);
        if (resigningColor == null) {
            game.setLastMessage("You are not part of this game.");
            populateValidMoves(game);
            return game;
        }

        Color winner = resigningColor == Color.WHITE ? Color.BLACK : Color.WHITE;
        finalizeGame(
            game,
            winner == Color.WHITE ? GameOutcome.WHITE_WIN : GameOutcome.BLACK_WIN,
            GameEndType.RESIGNATION,
            null
        );

        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    public Game offerDraw(String gameUuid, User player) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }
        resetOutcomeIfActive(game);
        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setLastMessage("Game is not active.");
            if (game.getStatus() == GameStatus.FINISHED) {
                summarizeFinishedGame(game);
            }
            populateValidMoves(game);
            return game;
        }

        if (game.getGameType() == GameType.HUMAN_VS_AI) {
            game.setLastMessage("Draw offers are not available against the computer.");
            populateValidMoves(game);
            return game;
        }

        Color offerColor = resolvePlayerColor(game, player);
        if (offerColor == null) {
            game.setLastMessage("You are not part of this game.");
            populateValidMoves(game);
            return game;
        }

        if (game.getDrawOfferBy() != null) {
            game.setLastMessage("A draw offer is already pending.");
            populateValidMoves(game);
            return game;
        }

        game.setDrawOfferBy(offerColor);
        game.setDrawOfferAt(LocalDateTime.now());
        game.setLastMessage(offerColor + " offers a draw.");

        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    public Game respondToDraw(String gameUuid, User player, boolean accept) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }
        resetOutcomeIfActive(game);
        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setLastMessage("Game is not active.");
            if (game.getStatus() == GameStatus.FINISHED) {
                summarizeFinishedGame(game);
            }
            populateValidMoves(game);
            return game;
        }

        Color drawOfferBy = game.getDrawOfferBy();
        if (drawOfferBy == null) {
            game.setLastMessage("No draw offer to respond to.");
            populateValidMoves(game);
            return game;
        }

        Color responderColor = resolvePlayerColor(game, player);
        if (responderColor == null) {
            game.setLastMessage("You are not part of this game.");
            populateValidMoves(game);
            return game;
        }

        if (responderColor == drawOfferBy) {
            game.setLastMessage("You cannot respond to your own draw offer.");
            populateValidMoves(game);
            return game;
        }

        if (accept) {
            finalizeGame(game, GameOutcome.DRAW, GameEndType.AGREED_DRAW, null);
        } else {
            game.setLastMessage(responderColor + " declined the draw offer.");
        }

        game.setDrawOfferBy(null);
        game.setDrawOfferAt(null);

        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    public Game claimTimeout(String gameUuid, User player) {
        Game game = gameRepository.findByGameUuid(gameUuid);
        if (game == null) {
            throw new RuntimeException("Game not found");
        }
        resetOutcomeIfActive(game);
        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setLastMessage("Game is not active.");
            if (game.getStatus() == GameStatus.FINISHED) {
                summarizeFinishedGame(game);
            }
            populateValidMoves(game);
            return game;
        }
        if (!isTimeControlEnabled(game)) {
            game.setLastMessage("No clock is running for this game.");
            populateValidMoves(game);
            return game;
        }

        Color claimantColor = resolvePlayerColor(game, player);
        if (claimantColor == null) {
            game.setLastMessage("You are not part of this game.");
            populateValidMoves(game);
            return game;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean hasTimeRemaining = deductTimeForCurrentPlayer(game, now);
        if (!hasTimeRemaining) {
            handleTimeout(game, game.getCurrentTurn());
        } else {
            game.setTurnStartAt(now);
            game.setLastMessage(game.getCurrentTurn() + " still has time remaining.");
        }

        Game savedGame = gameRepository.save(game);
        populateValidMoves(savedGame);
        return savedGame;
    }

    private void updateStatus(Game game, ChessBoard board) {
        Color turn = game.getCurrentTurn();
        // End immediately if a king has been captured
        boolean whiteHasKing = board.hasKing(Color.WHITE);
        boolean blackHasKing = board.hasKing(Color.BLACK);
        if (!whiteHasKing || !blackHasKing) {
            if (!whiteHasKing && !blackHasKing) {
                finalizeGame(game, GameOutcome.DRAW, GameEndType.KING_CAPTURE, "Both kings captured. Draw.");
            } else if (!whiteHasKing) {
                finalizeGame(game, GameOutcome.BLACK_WIN, GameEndType.KING_CAPTURE, "Game over: Black wins by capturing the white king.");
            } else {
                finalizeGame(game, GameOutcome.WHITE_WIN, GameEndType.KING_CAPTURE, "Game over: White wins by capturing the black king.");
            }
            return;
        }
        if (board.isCheckmate(turn)) {
            Color winnerColor = turn == Color.WHITE ? Color.BLACK : Color.WHITE;
            String winnerLabel = formatColorLabel(winnerColor);
            finalizeGame(game,
                    winnerColor == Color.WHITE ? GameOutcome.WHITE_WIN : GameOutcome.BLACK_WIN,
                    GameEndType.CHECKMATE,
                    winnerLabel + " wins by checkmate!"
            );
            return;
        }
        if (board.isStalemate(turn)) {
            finalizeGame(game, GameOutcome.DRAW, GameEndType.STALEMATE, "Stalemate! Game is a draw.");
            return;
        }
        if (board.isInCheck(turn)) {
            game.setLastMessage(turn + " is in check. " + turn + "'s turn.");
        } else {
            String turnLabel;
            if (game.getGameType() == GameType.HUMAN_VS_AI) {
                turnLabel = (turn == game.getPlayerColor()) ? turn.toString() : turn + " (AI)";
            } else {
                turnLabel = turn.toString();
            }
            game.setLastMessage("Move successful. " + turnLabel + "'s turn.");
        }
    }

    private ChessBoard ensureBoardLoaded(Game game) {
        ChessBoard board = gameBoards.get(game.getGameUuid());
        if (board == null) {
            if (game.getBoardState() != null && !game.getBoardState().isEmpty()) {
                try {
                    String[][] boardState = objectMapper.readValue(game.getBoardState(), String[][].class);
                    board = new ChessBoard(boardState);
                } catch (Exception e) {
                    board = new ChessBoard();
                    game.setBoardState(serializeBoard(board.getBoardState()));
                }
            } else {
                board = new ChessBoard();
                game.setBoardState(serializeBoard(board.getBoardState()));
            }
            gameBoards.put(game.getGameUuid(), board);
        }
        return board;
    }

    private boolean makeAIMove(ChessBoard board, Color aiColor) {
        List<int[]> validMoves = new ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.getColor() == aiColor) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (board.isValidMove(fromRow, fromCol, toRow, toCol, aiColor)) {
                                validMoves.add(new int[]{fromRow, fromCol, toRow, toCol});
                            }
                        }
                    }
                }
            }
        }
        if (validMoves.isEmpty()) {
            return false;
        }
        int[] move = validMoves.get(random.nextInt(validMoves.size()));
        return board.movePiece(move[0], move[1], move[2], move[3]);
    }

    private void configureTimeControls(Game game, Integer initialTimeSeconds) {
        if (initialTimeSeconds != null && initialTimeSeconds > 0) {
            game.setInitialTimeSeconds(initialTimeSeconds);
            game.setWhiteTimeRemainingSeconds(initialTimeSeconds);
            game.setBlackTimeRemainingSeconds(initialTimeSeconds);
        } else {
            game.setInitialTimeSeconds(null);
            game.setWhiteTimeRemainingSeconds(null);
            game.setBlackTimeRemainingSeconds(null);
        }
        game.setTurnStartAt(null);
    }

    private boolean isTimeControlEnabled(Game game) {
        return game.getInitialTimeSeconds() != null && game.getInitialTimeSeconds() > 0;
    }

    private boolean deductTimeForCurrentPlayer(Game game, LocalDateTime now) {
        if (!isTimeControlEnabled(game)) {
            return true;
        }

        if (game.getTurnStartAt() == null) {
            game.setTurnStartAt(now);
            return true;
        }

        long elapsedSeconds = Duration.between(game.getTurnStartAt(), now).getSeconds();
        if (elapsedSeconds < 0) {
            elapsedSeconds = 0;
        }

        if (game.getCurrentTurn() == Color.WHITE) {
            int remaining = game.getWhiteTimeRemainingSeconds() != null ? game.getWhiteTimeRemainingSeconds() : game.getInitialTimeSeconds();
            remaining -= (int) elapsedSeconds;
            if (remaining < 0) {
                remaining = 0;
            }
            game.setWhiteTimeRemainingSeconds(remaining);
            return remaining > 0;
        } else {
            int remaining = game.getBlackTimeRemainingSeconds() != null ? game.getBlackTimeRemainingSeconds() : game.getInitialTimeSeconds();
            remaining -= (int) elapsedSeconds;
            if (remaining < 0) {
                remaining = 0;
            }
            game.setBlackTimeRemainingSeconds(remaining);
            return remaining > 0;
        }
    }

    private void handleTimeout(Game game, Color playerColor) {
        Color winner = playerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
        String message = formatColorLabel(playerColor) + " ran out of time. "
                + formatColorLabel(winner) + " wins!";
        finalizeGame(
            game,
            winner == Color.WHITE ? GameOutcome.WHITE_WIN : GameOutcome.BLACK_WIN,
            GameEndType.TIMEOUT,
            message
        );
    }

    private String serializeBoard(String[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing board", e);
        }
    }

    private void applyLastMoveMetadata(Game game, ChessBoard board, Color moverColor) {
        if (board == null) {
            game.setLastMoveFrom(null);
            game.setLastMoveTo(null);
            game.setLastMoveColor(null);
            return;
        }

        int[] fromCoords = board.getLastMoveFrom();
        int[] toCoords = board.getLastMoveTo();
        if (fromCoords != null && toCoords != null
                && fromCoords.length == 2 && toCoords.length == 2
                && fromCoords[0] >= 0 && fromCoords[1] >= 0
                && toCoords[0] >= 0 && toCoords[1] >= 0) {
            game.setLastMoveFrom(toAlgebraic(fromCoords[0], fromCoords[1]));
            game.setLastMoveTo(toAlgebraic(toCoords[0], toCoords[1]));
            game.setLastMoveColor(moverColor);
        } else {
            game.setLastMoveFrom(null);
            game.setLastMoveTo(null);
            game.setLastMoveColor(null);
        }
    }

    private String toAlgebraic(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return null;
        }
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return String.valueOf(file) + rank;
    }
}