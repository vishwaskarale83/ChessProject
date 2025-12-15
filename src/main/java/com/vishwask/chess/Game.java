package com.vishwask.chess;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String gameUuid;

    @ManyToOne
    @JoinColumn(name = "white_player_id")
    private User whitePlayer;

    @ManyToOne
    @JoinColumn(name = "black_player_id")
    private User blackPlayer;

    @Enumerated(EnumType.STRING)
    private Color currentTurn = Color.WHITE;

    @Column(length = 1000)
    private String boardState; // JSON representation of board

    private String lastMessage;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    private GameType gameType = GameType.HUMAN_VS_HUMAN;

    @Enumerated(EnumType.STRING)
    private Color playerColor; // For AI games: which color the human plays

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastMoveAt;

    private String gameName;

    private Integer moveCount = 0;

    @Transient
    private java.util.Map<String, java.util.List<String>> validMoves;

    private String lastMoveFrom;

    private String lastMoveTo;

    @Enumerated(EnumType.STRING)
    private Color lastMoveColor;

    private Integer initialTimeSeconds;

    private Integer whiteTimeRemainingSeconds;

    private Integer blackTimeRemainingSeconds;

    private LocalDateTime turnStartAt;

    @Enumerated(EnumType.STRING)
    private Color drawOfferBy;

    private LocalDateTime drawOfferAt;

    @Enumerated(EnumType.STRING)
    private GameOutcome result = GameOutcome.UNDECIDED;

    @Enumerated(EnumType.STRING)
    private GameEndType endType;

    // Constructors
    public Game() {
        this.gameUuid = UUID.randomUUID().toString();
    }

    public Game(User whitePlayer) {
        this();
        this.whitePlayer = whitePlayer;
        this.gameName = whitePlayer.getUsername() + "'s Game";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getWhitePlayer() { return whitePlayer; }
    public void setWhitePlayer(User whitePlayer) { this.whitePlayer = whitePlayer; }

    public User getBlackPlayer() { return blackPlayer; }
    public void setBlackPlayer(User blackPlayer) { this.blackPlayer = blackPlayer; }

    public Color getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Color currentTurn) { this.currentTurn = currentTurn; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public GameType getGameType() { return gameType; }
    public void setGameType(GameType gameType) { this.gameType = gameType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGameUuid() { return gameUuid; }
    public void setGameUuid(String gameUuid) { this.gameUuid = gameUuid; }

    public LocalDateTime getLastMoveAt() { return lastMoveAt; }
    public void setLastMoveAt(LocalDateTime lastMoveAt) { this.lastMoveAt = lastMoveAt; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public Integer getMoveCount() { return moveCount; }
    public void setMoveCount(Integer moveCount) { this.moveCount = moveCount; }

    public java.util.Map<String, java.util.List<String>> getValidMoves() { return validMoves; }
    public void setValidMoves(java.util.Map<String, java.util.List<String>> validMoves) { this.validMoves = validMoves; }

    public Color getPlayerColor() { return playerColor; }
    public void setPlayerColor(Color playerColor) { this.playerColor = playerColor; }

    public String getLastMoveFrom() { return lastMoveFrom; }
    public void setLastMoveFrom(String lastMoveFrom) { this.lastMoveFrom = lastMoveFrom; }

    public String getLastMoveTo() { return lastMoveTo; }
    public void setLastMoveTo(String lastMoveTo) { this.lastMoveTo = lastMoveTo; }

    public Color getLastMoveColor() { return lastMoveColor; }
    public void setLastMoveColor(Color lastMoveColor) { this.lastMoveColor = lastMoveColor; }

    public Integer getInitialTimeSeconds() { return initialTimeSeconds; }
    public void setInitialTimeSeconds(Integer initialTimeSeconds) { this.initialTimeSeconds = initialTimeSeconds; }

    public Integer getWhiteTimeRemainingSeconds() { return whiteTimeRemainingSeconds; }
    public void setWhiteTimeRemainingSeconds(Integer whiteTimeRemainingSeconds) { this.whiteTimeRemainingSeconds = whiteTimeRemainingSeconds; }

    public Integer getBlackTimeRemainingSeconds() { return blackTimeRemainingSeconds; }
    public void setBlackTimeRemainingSeconds(Integer blackTimeRemainingSeconds) { this.blackTimeRemainingSeconds = blackTimeRemainingSeconds; }

    public LocalDateTime getTurnStartAt() { return turnStartAt; }
    public void setTurnStartAt(LocalDateTime turnStartAt) { this.turnStartAt = turnStartAt; }

    public Color getDrawOfferBy() { return drawOfferBy; }
    public void setDrawOfferBy(Color drawOfferBy) { this.drawOfferBy = drawOfferBy; }

    public LocalDateTime getDrawOfferAt() { return drawOfferAt; }
    public void setDrawOfferAt(LocalDateTime drawOfferAt) { this.drawOfferAt = drawOfferAt; }

    public GameOutcome getResult() { return result; }
    public void setResult(GameOutcome result) { this.result = result; }

    public GameEndType getEndType() { return endType; }
    public void setEndType(GameEndType endType) { this.endType = endType; }
}