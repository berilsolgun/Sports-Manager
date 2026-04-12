package com.sportsmanager.domain.session;

import java.util.Optional;

/**
 * Abstracts save/load operations for {@link GameSession}.
 * Concrete implementations decide the storage technology (JSON file, database, etc.).
 */
public interface GameRepository {

    /**
     * Persist the given session.
     *
     * @param session the game session to save
     */
    void save(GameSession session);

    /**
     * Load the most recently saved session.
     *
     * @return the session, or empty if none exists
     */
    Optional<GameSession> load();
}