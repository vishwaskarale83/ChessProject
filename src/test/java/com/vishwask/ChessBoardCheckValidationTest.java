package com.vishwask;

import org.junit.jupiter.api.Test;

import com.vishwask.chess.ChessBoard;
import com.vishwask.chess.Color;
import com.vishwask.chess.Piece;
import com.vishwask.chess.PieceType;

import static org.junit.jupiter.api.Assertions.*;

class ChessBoardCheckValidationTest {

    @Test
    void testBasicQueenMove() {
        String[][] boardState = new String[8][8];
        // Initialize empty board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardState[i][j] = "";
            }
        }
        // White queen at d1
        boardState[7][3] = "QUEEN,WHITE"; // d1

        ChessBoard board = new ChessBoard(boardState);

        // Test if queen can move diagonally (d1 to e2)
        assertTrue(board.isValidMove(7, 3, 6, 4, Color.WHITE),
                  "Queen should be able to move diagonally");
    }

    @Test
    void testCheckValidationWorks() {
        String[][] boardState = new String[8][8];
        // Initialize empty board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardState[i][j] = "";
            }
        }
        // White king at e1, black queen at d2 (checking the king)
        boardState[7][4] = "KING,WHITE"; // e1
        boardState[6][3] = "QUEEN,BLACK"; // d2

        ChessBoard board = new ChessBoard(boardState);

        // Verify king is in check
        assertTrue(board.isInCheck(Color.WHITE), "King should be in check");

        // Try to move the king to a safe square (f1)
        assertTrue(board.isValidMove(7, 4, 7, 5, Color.WHITE), "King should be able to move out of check");

        // Try to move king to a square still in check (e2, still attacked by queen)
        assertFalse(board.isValidMove(7, 4, 6, 4, Color.WHITE), "King should not move into check");
    }

    @Test
    void testCannotMoveIntoCheck() {
        // Set up a position where white king is in check from black queen
        String[][] boardState = new String[8][8];
        // Initialize empty board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardState[i][j] = "";
            }
        }
        // White king at e1, black queen at e8
        boardState[7][4] = "KING,WHITE"; // e1
        boardState[0][4] = "QUEEN,BLACK"; // e8

        ChessBoard board = new ChessBoard(boardState);

        // Try to move the king out of check (to f1)
        assertTrue(board.isValidMove(7, 4, 7, 5, Color.WHITE),
                  "Should allow king to move out of check");

        // Try to move the king into check (to e2, but e8 queen controls e2)
        assertFalse(board.isValidMove(7, 4, 6, 4, Color.WHITE),
                   "Should not allow king to move into check");
    }

    @Test
    void testMustCaptureAttackerWhenInCheck() {
        String[][] boardState = new String[8][8];
        // Initialize empty board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                boardState[i][j] = "";
            }
        }
        // Set up position where white king is checked by black rook on e-file
        boardState[7][4] = "KING,WHITE"; // e1
        boardState[0][4] = "ROOK,BLACK"; // e8
        // White queen at d7 - can capture the rook by moving diagonally to e8
        boardState[1][3] = "QUEEN,WHITE"; // d7

        ChessBoard board = new ChessBoard(boardState);

        // Moving the queen to capture should be valid (queen can move diagonally to e8)
        assertTrue(board.isValidMove(1, 3, 0, 4, Color.WHITE),
                  "Should allow capturing the attacking piece");

        // Moving an irrelevant piece should be invalid
        // Add a white pawn that can't help and test on the same board
        boardState[6][0] = "PAWN,WHITE"; // a2
        ChessBoard boardWithPawn = new ChessBoard(boardState);
        assertFalse(boardWithPawn.isValidMove(6, 0, 5, 0, Color.WHITE),
                   "Should not allow moving pieces that don't resolve check");
    }

    @Test
    void testCanBlockCheck() {
        String[][] boardState = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boardState[row][col] = "";
            }
        }

        // White king on e1, in check from a rook on e8.
        boardState[7][4] = "KING,WHITE"; // e1
        boardState[0][4] = "ROOK,BLACK"; // e8

        // White pawn positioned to capture the attacking rook.
        boardState[1][3] = "PAWN,WHITE"; // d7

        ChessBoard board = new ChessBoard(boardState);

        // Capturing the attacking rook should be legal.
        assertTrue(board.isValidMove(1, 3, 0, 4, Color.WHITE),
                  "Should allow pawn to capture the attacking rook");

        // Advancing the pawn forward leaves the king in check and must be rejected.
        assertFalse(board.isValidMove(1, 3, 0, 3, Color.WHITE),
                   "Should not allow pawn to move without resolving check");
    }

    @Test
    void testNormalMovesAllowedWhenNotInCheck() {
        ChessBoard board = new ChessBoard();

        // Standard starting position - no one in check
        // Should allow normal pawn moves
        assertTrue(board.isValidMove(1, 0, 2, 0, Color.BLACK),
                  "Should allow normal moves when not in check");
        assertTrue(board.isValidMove(6, 0, 5, 0, Color.WHITE),
                  "Should allow normal moves when not in check");
    }
}