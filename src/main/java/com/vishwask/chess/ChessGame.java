package com.vishwask.chess;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class ChessGame {
    private ChessBoard board;
    private Color currentTurn;
    private String lastMessage;
    private Random random;

    public ChessGame() {
        board = new ChessBoard();
        currentTurn = Color.WHITE;
        lastMessage = "";
        random = new Random();
    }

    public String[][] getBoardState() {
        return board.getBoardState();
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (board.isValidMove(fromRow, fromCol, toRow, toCol, currentTurn)) {
            board.movePiece(fromRow, fromCol, toRow, toCol);
            currentTurn = currentTurn == Color.WHITE ? Color.BLACK : Color.WHITE;
            updateGameStatus();
            return true;
        } else {
            lastMessage = "Invalid move. Try again.";
            return false;
        }
    }

    private void updateGameStatus() {
        if (board.isCheckmate(currentTurn)) {
            lastMessage = (currentTurn == Color.WHITE ? "Black" : "White") + " wins by checkmate!";
        } else if (board.isStalemate(currentTurn)) {
            lastMessage = "Stalemate! Game is a draw.";
        } else if (board.isInCheck(currentTurn)) {
            lastMessage = currentTurn + " is in check. " + currentTurn + "'s turn.";
        } else {
            lastMessage = "Move successful. " + currentTurn + "'s turn.";
        }
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void resetGame() {
        board = new ChessBoard();
        currentTurn = Color.WHITE;
        lastMessage = "";
    }

    public void makeAIMove() {
        // Simple AI: make a random valid move
        java.util.List<int[]> validMoves = new java.util.ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.getColor() == currentTurn) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (board.isValidMove(fromRow, fromCol, toRow, toCol, currentTurn)) {
                                validMoves.add(new int[]{fromRow, fromCol, toRow, toCol});
                            }
                        }
                    }
                }
            }
        }
        if (!validMoves.isEmpty()) {
            int[] move = validMoves.get(random.nextInt(validMoves.size()));
            makeMove(move[0], move[1], move[2], move[3]);
        }
    }
}