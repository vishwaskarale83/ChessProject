package com.vishwask.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.vishwask.chess.*;

@Controller
public class GameApiController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @MessageMapping("/game/{gameUuid}/move")
    @SendTo("/topic/game/{gameUuid}")
    public Game handleMove(@DestinationVariable String gameUuid,
                          MoveMessage moveMessage,
                          Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userService.findByUsername(auth.getName());
        return gameService.makeMove(gameUuid, user,
                       moveMessage.getFromRow(),
                       moveMessage.getFromCol(),
                       moveMessage.getToRow(),
                       moveMessage.getToCol(),
                       moveMessage.getPromotion());
    }

    @MessageMapping("/game/{gameUuid}/resign")
    @SendTo("/topic/game/{gameUuid}")
    public Game handleResign(@DestinationVariable String gameUuid,
                             Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userService.findByUsername(auth.getName());
        return gameService.resignGame(gameUuid, user);
    }

    @MessageMapping("/game/{gameUuid}/draw/offer")
    @SendTo("/topic/game/{gameUuid}")
    public Game handleDrawOffer(@DestinationVariable String gameUuid,
                                Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userService.findByUsername(auth.getName());
        return gameService.offerDraw(gameUuid, user);
    }

    @MessageMapping("/game/{gameUuid}/draw/respond")
    @SendTo("/topic/game/{gameUuid}")
    public Game handleDrawResponse(@DestinationVariable String gameUuid,
                                   DrawResponseMessage response,
                                   Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userService.findByUsername(auth.getName());
        boolean accept = response != null && response.isAccept();
        return gameService.respondToDraw(gameUuid, user, accept);
    }

    @MessageMapping("/game/{gameUuid}/timeout")
    @SendTo("/topic/game/{gameUuid}")
    public Game handleTimeoutClaim(@DestinationVariable String gameUuid,
                                   Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Not authenticated");
        }
        User user = userService.findByUsername(auth.getName());
        return gameService.claimTimeout(gameUuid, user);
    }

    @GetMapping("/api/game/{gameUuid}")
    @ResponseBody
    public Game getGame(@PathVariable String gameUuid) {
        return gameService.getGame(gameUuid);
    }

    public static class MoveMessage {
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;
        private String promotion;

        // Getters and setters
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

    public static class DrawResponseMessage {
        private boolean accept;

        public boolean isAccept() { return accept; }
        public void setAccept(boolean accept) { this.accept = accept; }
    }
}