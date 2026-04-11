package com.sportsmanager.domain.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.File;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class JsonGameRepositoryTest {

    private final JsonGameRepository repo = new JsonGameRepository();

    @AfterEach
    void cleanup() {
        new File("gamesession.json").delete();
    }

    @Test
    void testSaveAndLoadCurrentWeek() {
        GameSession session = new GameSession();
        session.setCurrentWeek(5);
        session.setSeason(2026);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();

        assertTrue(loaded.isPresent());
        assertEquals(5, loaded.get().getCurrentWeek());
    }

    @Test
    void testSaveAndLoadSeason() {
        GameSession session = new GameSession();
        session.setCurrentWeek(1);
        session.setSeason(2026);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();

        assertTrue(loaded.isPresent());
        assertEquals(2026, loaded.get().getSeason());
    }

    @Test
    void testLoadReturnsEmptyWhenNoFile() {
        new File("gamesession.json").delete();
        Optional<GameSession> loaded = repo.load();
        assertFalse(loaded.isPresent());
    }
}