package com.vishwask.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.vishwask.chess.ChessGame;

@Controller
@RequestMapping("/chess")
public class ChessController {

    @Autowired
    private ChessGame chessGame;

    @GetMapping("/board")
    @ResponseBody
    public String[][] getBoard() {
        return chessGame.getBoardState();
    }

    @PostMapping("/move")
    @ResponseBody
    public String makeMove(@RequestParam int fromRow, @RequestParam int fromCol,
                           @RequestParam int toRow, @RequestParam int toCol) {
        chessGame.makeMove(fromRow, fromCol, toRow, toCol);
        return chessGame.getLastMessage();
    }

    @PostMapping("/reset")
    @ResponseBody
    public String resetGame() {
        chessGame.resetGame();
        return chessGame.getLastMessage();
    }

    @GetMapping("/turn")
    @ResponseBody
    public String getTurn() {
        return chessGame.getCurrentTurn().toString();
    }

    @GetMapping("/message")
    @ResponseBody
    public String getMessage() {
        return chessGame.getLastMessage();
    }

    @PostMapping("/ai")
    @ResponseBody
    public String makeAIMove() {
        chessGame.makeAIMove();
        return chessGame.getLastMessage();
    }
}