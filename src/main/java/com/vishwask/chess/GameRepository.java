package com.vishwask.chess;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);
    List<Game> findByWhitePlayerOrBlackPlayer(User whitePlayer, User blackPlayer);
    Game findByGameUuid(String gameUuid);
}