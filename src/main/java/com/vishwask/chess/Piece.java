package com.vishwask.chess;

public class Piece {
    private PieceType type;
    private Color color;

    public Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        String symbol = "";
        switch (type) {
            case PAWN: symbol = color == Color.WHITE ? "♙" : "♟"; break;
            case ROOK: symbol = color == Color.WHITE ? "♖" : "♜"; break;
            case KNIGHT: symbol = color == Color.WHITE ? "♘" : "♞"; break;
            case BISHOP: symbol = color == Color.WHITE ? "♗" : "♝"; break;
            case QUEEN: symbol = color == Color.WHITE ? "♕" : "♛"; break;
            case KING: symbol = color == Color.WHITE ? "♔" : "♚"; break;
        }
        return symbol;
    }
}