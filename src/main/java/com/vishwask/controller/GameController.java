package com.vishwask.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.vishwask.chess.*;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserService userService;

    @GetMapping("/games")
    public String showGames(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("availableGames", gameService.getAvailableGames());
        model.addAttribute("userGames", gameService.getUserGames(user));
        return "games";
    }

    @PostMapping("/games/create")
    public String createGame(@RequestParam(name = "mode", defaultValue = "HUMAN") String mode,
                             @RequestParam(name = "playerColor", defaultValue = "WHITE") String playerColorStr,
                             @RequestParam(name = "timeControl", defaultValue = "NONE") String timeControl,
                             Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(auth.getName());
        Game game;
        if ("AI".equalsIgnoreCase(mode)) {
            Color playerColor = "BLACK".equalsIgnoreCase(playerColorStr) ? Color.BLACK : Color.WHITE;
            game = gameService.createGameVsAi(user, playerColor);
            return "redirect:/game/" + game.getGameUuid();
        } else {
            Integer initialSeconds = parseTimeControl(timeControl);
            game = gameService.createGame(user, initialSeconds);
            return "redirect:/games";
        }
    }

    @PostMapping("/games/{gameUuid}/join")
    public String joinGame(@PathVariable String gameUuid, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        User user = userService.findByUsername(auth.getName());
        gameService.joinGame(gameUuid, user);
        return "redirect:/game/" + gameUuid;
    }

    @GetMapping("/game/{gameUuid}")
    public String showGame(@PathVariable String gameUuid, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }
        Game game = gameService.getGame(gameUuid);
        User user = userService.findByUsername(auth.getName());

        // Check if user is part of this game
        boolean isWhitePlayer = game.getWhitePlayer() != null && game.getWhitePlayer().getId().equals(user.getId());
        boolean isBlackPlayer = game.getBlackPlayer() != null && game.getBlackPlayer().getId().equals(user.getId());
        
        if (!isWhitePlayer && !isBlackPlayer) {
            return "redirect:/games";
        }

        model.addAttribute("game", game);
        model.addAttribute("currentUser", user);
        model.addAttribute("isWhitePlayer", isWhitePlayer);
        return "game";
    }

    private Integer parseTimeControl(String timeControl) {
        if (timeControl == null) {
            return null;
        }
        String normalized = timeControl.trim().toUpperCase();
        if (normalized.isEmpty() || "NONE".equals(normalized)) {
            return null;
        }
        try {
            int seconds = Integer.parseInt(normalized);
            return seconds > 0 ? seconds : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}