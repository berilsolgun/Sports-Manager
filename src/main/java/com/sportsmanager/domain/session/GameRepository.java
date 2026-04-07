package com.sportsmanager.domain.session;

import java.util.Optional;

public interface GameRepository {
    void save(GameSession session);
    Optional<GameSession> load();

}
