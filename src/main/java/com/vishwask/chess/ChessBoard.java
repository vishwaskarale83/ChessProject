package com.vishwask.chess;

import java.util.ArrayList;
import java.util.List;

public class ChessBoard {
    private Piece[][] board;
    private boolean[] hasKingMoved;
    private boolean[][] hasRookMoved;
    private int[] lastPawnMove; // [row, col] of last double pawn move for en passant
    private int[] lastMoveFrom;
    private int[] lastMoveTo;
    private Color lastMoverColor;

    public ChessBoard() {
        board = new Piece[8][8];
        hasKingMoved = new boolean[2]; // 0 white, 1 black
        hasRookMoved = new boolean[2][2]; // [color][side: 0 queenside, 1 kingside]
        lastPawnMove = new int[]{-1, -1};
        lastMoveFrom = new int[]{-1, -1};
        lastMoveTo = new int[]{-1, -1};
        lastMoverColor = null;
        initializeBoard();
    }

    private void initializeBoard() {
        // Place pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Piece(PieceType.PAWN, Color.BLACK);
            board[6][i] = new Piece(PieceType.PAWN, Color.WHITE);
        }

        // Place other pieces
        PieceType[] types = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int i = 0; i < 8; i++) {
            board[0][i] = new Piece(types[i], Color.BLACK);
            board[7][i] = new Piece(types[i], Color.WHITE);
        }
    }

    public Piece getPiece(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) return null;
        return board[row][col];
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, Color currentTurn) {
        if (fromRow < 0 || fromRow > 7 || fromCol < 0 || fromCol > 7 || toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7) {
            return false;
        }
        Piece piece = board[fromRow][fromCol];
        if (piece == null || piece.getColor() != currentTurn) return false;

        Piece target = board[toRow][toCol];
        if (target != null && target.getColor() == piece.getColor()) return false;

        // Check if the basic move is valid (without considering check)
        if (!isBasicMoveValid(fromRow, fromCol, toRow, toCol)) return false;

        // Simulate the move to check if it leaves king in check
        Piece capturedPiece = board[toRow][toCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;

        int deltaRow = toRow - fromRow;
        int deltaCol = toCol - fromCol;

        // Handle special moves
        boolean isEnPassant = false;
        if (piece.getType() == PieceType.PAWN && Math.abs(toCol - fromCol) == 1 && capturedPiece == null) {
            // En passant capture
            int capturedPawnRow = piece.getColor() == Color.WHITE ? toRow + 1 : toRow - 1;
            capturedPiece = board[capturedPawnRow][toCol];
            board[capturedPawnRow][toCol] = null;
            isEnPassant = true;
        }

        // Handle castling
        boolean isCastling = false;
        if (piece.getType() == PieceType.KING && Math.abs(deltaCol) == 2) {
            isCastling = true;
            int rookFromCol = deltaCol > 0 ? 7 : 0;
            int rookToCol = deltaCol > 0 ? 5 : 3;
            board[fromRow][rookToCol] = board[fromRow][rookFromCol];
            board[fromRow][rookFromCol] = null;
        }

        // Check if the move leaves own king in check
        boolean leavesKingInCheck = isInCheck(currentTurn);

        // Undo the move
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = capturedPiece;

        if (isEnPassant) {
            int capturedPawnRow = piece.getColor() == Color.WHITE ? toRow + 1 : toRow - 1;
            board[capturedPawnRow][toCol] = capturedPiece;
        }

        if (isCastling) {
            int rookFromCol = deltaCol > 0 ? 7 : 0;
            int rookToCol = deltaCol > 0 ? 5 : 3;
            board[fromRow][rookFromCol] = board[fromRow][rookToCol];
            board[fromRow][rookToCol] = null;
        }

        return !leavesKingInCheck;
    }

    private boolean isBasicMoveValid(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        if (piece == null) return false;

        int deltaRow = toRow - fromRow;
        int deltaCol = toCol - fromCol;

        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(fromRow, fromCol, toRow, toCol, piece.getColor());
            case ROOK:
                return isValidRookMove(fromRow, fromCol, toRow, toCol);
            case KNIGHT:
                return isValidKnightMove(deltaRow, deltaCol);
            case BISHOP:
                return isValidBishopMove(fromRow, fromCol, toRow, toCol);
            case QUEEN:
                return isValidQueenMove(fromRow, fromCol, toRow, toCol);
            case KING:
                return isValidKingMove(deltaRow, deltaCol) || isValidCastling(fromRow, fromCol, toRow, toCol, piece.getColor());
        }
        return false;
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, Color color) {
        int deltaRow = toRow - fromRow;
        int deltaCol = toCol - fromCol;
        int direction = color == Color.WHITE ? -1 : 1;
        int startRow = color == Color.WHITE ? 6 : 1;

        if (deltaCol == 0 && board[toRow][toCol] == null) {
            if (deltaRow == direction) return true;
            if (fromRow == startRow && deltaRow == 2 * direction && board[fromRow + direction][fromCol] == null) return true;
        } else if (Math.abs(deltaCol) == 1 && deltaRow == direction && board[toRow][toCol] != null && board[toRow][toCol].getColor() != color) {
            return true; // capture
        } else if (Math.abs(deltaCol) == 1 && deltaRow == direction && board[toRow][toCol] == null && lastPawnMove[0] == fromRow && lastPawnMove[1] == toCol) {
            return true; // en passant
        }
        return false;
    }

    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidRookMove(fromRow, fromCol, toRow, toCol) || isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKingMove(int deltaRow, int deltaCol) {
        return Math.abs(deltaRow) <= 1 && Math.abs(deltaCol) <= 1 && (deltaRow != 0 || deltaCol != 0);
    }

    private boolean isValidKnightMove(int deltaRow, int deltaCol) {
        return (Math.abs(deltaRow) == 2 && Math.abs(deltaCol) == 1) || (Math.abs(deltaRow) == 1 && Math.abs(deltaCol) == 2);
    }

    private boolean isValidCastling(int fromRow, int fromCol, int toRow, int toCol, Color color) {
        if (fromRow != toRow || hasKingMoved[color.ordinal()]) return false;
        int row = color == Color.WHITE ? 7 : 0;
        if (fromRow != row || fromCol != 4) return false;

        // Kingside
        if (toCol == 6 && !hasRookMoved[color.ordinal()][1] && board[row][5] == null && board[row][6] == null) {
            return !isSquareUnderAttack(row, 4, color) && !isSquareUnderAttack(row, 5, color) && !isSquareUnderAttack(row, 6, color);
        }
        // Queenside
        if (toCol == 2 && !hasRookMoved[color.ordinal()][0] && board[row][1] == null && board[row][2] == null && board[row][3] == null) {
            return !isSquareUnderAttack(row, 4, color) && !isSquareUnderAttack(row, 3, color) && !isSquareUnderAttack(row, 2, color);
        }
        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int stepRow = Integer.compare(toRow, fromRow);
        int stepCol = Integer.compare(toCol, fromCol);
        int currentRow = fromRow + stepRow;
        int currentCol = fromCol + stepCol;
        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != null) return false;
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        return movePiece(fromRow, fromCol, toRow, toCol, null);
    }

    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol, PieceType promotionChoice) {
        if (fromRow < 0 || fromRow > 7 || fromCol < 0 || fromCol > 7 || toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7) {
            return false;
        }
        Piece piece = board[fromRow][fromCol];
        if (piece == null) {
            return false;
        }
        if (!isValidMove(fromRow, fromCol, toRow, toCol, piece.getColor())) {
            return false;
        }

        Color color = piece.getColor();

        // Handle en passant
        if (piece.getType() == PieceType.PAWN && Math.abs(toCol - fromCol) == 1 && board[toRow][toCol] == null) {
            board[fromRow][toCol] = null;
        }

        // Handle castling
        if (piece.getType() == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            int rookFromCol = toCol == 6 ? 7 : 0;
            int rookToCol = toCol == 6 ? 5 : 3;
            board[toRow][rookToCol] = board[toRow][rookFromCol];
            board[toRow][rookFromCol] = null;
        }

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;

        if (piece.getType() == PieceType.PAWN && (toRow == 0 || toRow == 7)) {
            PieceType promoteTo = determinePromotionChoice(promotionChoice);
            board[toRow][toCol] = new Piece(promoteTo, color);
        }

        if (piece.getType() == PieceType.KING) {
            hasKingMoved[color.ordinal()] = true;
        } else if (piece.getType() == PieceType.ROOK) {
            if (fromCol == 0) {
                hasRookMoved[color.ordinal()][0] = true;
            }
            if (fromCol == 7) {
                hasRookMoved[color.ordinal()][1] = true;
            }
        }

        if (piece.getType() == PieceType.PAWN && Math.abs(toRow - fromRow) == 2) {
            lastPawnMove = new int[]{toRow, toCol};
        } else {
            lastPawnMove = new int[]{-1, -1};
        }

        lastMoveFrom = new int[]{fromRow, fromCol};
        lastMoveTo = new int[]{toRow, toCol};
        lastMoverColor = color;

        return true;
    }

    private PieceType determinePromotionChoice(PieceType requested) {
        if (requested == null) {
            return PieceType.QUEEN;
        }
        switch (requested) {
            case QUEEN:
            case ROOK:
            case BISHOP:
            case KNIGHT:
                return requested;
            default:
                return PieceType.QUEEN;
        }
    }

    public int[] getLastMoveFrom() {
        return lastMoveFrom == null ? null : lastMoveFrom.clone();
    }

    public int[] getLastMoveTo() {
        return lastMoveTo == null ? null : lastMoveTo.clone();
    }

    public Color getLastMoverColor() {
        return lastMoverColor;
    }
    public boolean isInCheck(Color color) {
        int kingRow = -1, kingCol = -1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    kingRow = i;
                    kingCol = j;
                    break;
                }
            }
        }
        if (kingRow == -1 || kingCol == -1) {
            return false;
        }
        return isSquareUnderAttack(kingRow, kingCol, color);
    }

    public boolean hasKing(Color color) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSquareUnderAttack(int row, int col, Color defendingColor) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return false;
        }
        Color attackingColor = defendingColor == Color.WHITE ? Color.BLACK : Color.WHITE;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() == attackingColor) {
                    if (isBasicMoveValid(i, j, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(Color color) {
        if (!isInCheck(color)) return false;
        // Check if any legal move exists (isValidMove now includes check validation)
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board[fromRow][fromCol];
                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol, color)) {
                                return false; // Found a legal move
                            }
                        }
                    }
                }
            }
        }
        return true; // No legal moves found, it's checkmate
    }

    public boolean isStalemate(Color color) {
        if (isInCheck(color)) return false;
        // Check if any legal move exists
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board[fromRow][fromCol];
                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol, color)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    public String[][] getBoardState() {
        String[][] state = new String[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    state[i][j] = getUnicodeSymbol(board[i][j]);
                } else {
                    state[i][j] = "";
                }
            }
        }
        return state;
    }

    private String getUnicodeSymbol(Piece piece) {
        boolean isWhite = piece.getColor() == Color.WHITE;
        switch (piece.getType()) {
            case KING: return isWhite ? "♔" : "♚";
            case QUEEN: return isWhite ? "♕" : "♛";
            case ROOK: return isWhite ? "♖" : "♜";
            case BISHOP: return isWhite ? "♗" : "♝";
            case KNIGHT: return isWhite ? "♘" : "♞";
            case PAWN: return isWhite ? "♙" : "♟";
            default: return "";
        }
    }

    public void setBoardState(String[][] state) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!state[i][j].isEmpty()) {
                    board[i][j] = parseSymbol(state[i][j]);
                } else {
                    board[i][j] = null;
                }
            }
        }
    }

    // Supports both legacy "TYPE,COLOR" strings and current Unicode symbols.
    private Piece parseSymbol(String symbol) {
        // Legacy format: "ROOK,WHITE"
        if (symbol.contains(",")) {
            String[] parts = symbol.split(",");
            if (parts.length == 2) {
                try {
                    PieceType type = PieceType.valueOf(parts[0]);
                    Color color = Color.valueOf(parts[1]);
                    return new Piece(type, color);
                } catch (IllegalArgumentException ex) {
                    // Fallback to Unicode handling below
                }
            }
        }

        switch (symbol) {
            case "♔": return new Piece(PieceType.KING, Color.WHITE);
            case "♚": return new Piece(PieceType.KING, Color.BLACK);
            case "♕": return new Piece(PieceType.QUEEN, Color.WHITE);
            case "♛": return new Piece(PieceType.QUEEN, Color.BLACK);
            case "♖": return new Piece(PieceType.ROOK, Color.WHITE);
            case "♜": return new Piece(PieceType.ROOK, Color.BLACK);
            case "♗": return new Piece(PieceType.BISHOP, Color.WHITE);
            case "♝": return new Piece(PieceType.BISHOP, Color.BLACK);
            case "♘": return new Piece(PieceType.KNIGHT, Color.WHITE);
            case "♞": return new Piece(PieceType.KNIGHT, Color.BLACK);
            case "♙": return new Piece(PieceType.PAWN, Color.WHITE);
            case "♟": return new Piece(PieceType.PAWN, Color.BLACK);
            default: return null;
        }
    }

    // Constructor to restore board from state
    public ChessBoard(String[][] state) {
        board = new Piece[8][8];
        hasKingMoved = new boolean[2];
        hasRookMoved = new boolean[2][2];
        lastPawnMove = new int[]{-1, -1};
        lastMoveFrom = new int[]{-1, -1};
        lastMoveTo = new int[]{-1, -1};
        lastMoverColor = null;
        setBoardState(state);
    }

    public java.util.Map<String, java.util.List<String>> getAllValidMoves(Color currentTurn) {
        java.util.Map<String, java.util.List<String>> validMoves = new java.util.HashMap<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() == currentTurn) {
                    String from = toAlgebraic(i, j);
                    java.util.List<String> moves = new java.util.ArrayList<>();
                    for (int r = 0; r < 8; r++) {
                        for (int c = 0; c < 8; c++) {
                            if (isValidMove(i, j, r, c, currentTurn)) {
                                moves.add(toAlgebraic(r, c));
                            }
                        }
                    }
                    if (!moves.isEmpty()) {
                        validMoves.put(from, moves);
                    }
                }
            }
        }
        return validMoves;
    }

    private String toAlgebraic(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }
}