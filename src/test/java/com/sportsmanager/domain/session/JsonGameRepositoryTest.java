package com.sportsmanager.domain.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonGameRepositoryTest {

    private static final String TEST_FILE = "test_gamesession.json";
    private JsonGameRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JsonGameRepository(TEST_FILE);
    }

    @AfterEach
    void cleanup() {
        new File(TEST_FILE).delete();
    }

    @Test
    void saveAndLoadPreservesCurrentWeek() {
        GameSession session = new GameSession();
        session.setCurrentWeek(5);
        session.setSeason(2026);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();

        assertTrue(loaded.isPresent());
        assertEquals(5, loaded.get().getCurrentWeek());
    }

    @Test
    void saveAndLoadPreservesSeason() {
        GameSession session = new GameSession();
        session.setCurrentWeek(1);
        session.setSeason(2026);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();

        assertTrue(loaded.isPresent());
        assertEquals(2026, loaded.get().getSeason());
    }

    @Test
    void loadReturnsEmptyWhenNoFile() {
        new File(TEST_FILE).delete();
        Optional<GameSession> loaded = repo.load();
        assertFalse(loaded.isPresent());
    }

    @Test
    void saveCreatesFileOnDisk() {
        GameSession session = new GameSession();
        session.setCurrentWeek(1);
        session.setSeason(2025);

        repo.save(session);

        File f = new File(TEST_FILE);
        assertTrue(f.exists(), "JSON file should exist after save");
        assertTrue(f.length() > 0, "JSON file should not be empty");
    }

    @Test
    void savedFileContainsValidJson() throws IOException {
        GameSession session = new GameSession();
        session.setCurrentWeek(10);
        session.setSeason(2030);

        repo.save(session);

        String content = Files.readString(new File(TEST_FILE).toPath());
        assertTrue(content.contains("\"currentWeek\""), "Should contain currentWeek key");
        assertTrue(content.contains("\"season\""), "Should contain season key");
        assertTrue(content.contains("10"), "Should contain week value");
        assertTrue(content.contains("2030"), "Should contain season value");
    }

    @Test
    void defaultConstructorUsesDefaultPath() {
        JsonGameRepository defaultRepo = new JsonGameRepository();
        assertEquals("gamesession.json", defaultRepo.getFilePath());

        // cleanup default file if created
        new File("gamesession.json").delete();
    }

    @Test
    void customPathIsUsedForSaveAndLoad() {
        String customPath = "custom_save.json";
        JsonGameRepository customRepo = new JsonGameRepository(customPath);

        GameSession session = new GameSession();
        session.setCurrentWeek(7);
        session.setSeason(2028);

        customRepo.save(session);
        Optional<GameSession> loaded = customRepo.load();

        assertTrue(loaded.isPresent());
        assertEquals(7, loaded.get().getCurrentWeek());
        assertEquals(2028, loaded.get().getSeason());

        new File(customPath).delete();
    }

    @Test
    void saveOverwritesPreviousFile() {
        GameSession first = new GameSession();
        first.setCurrentWeek(1);
        first.setSeason(2025);
        repo.save(first);

        GameSession second = new GameSession();
        second.setCurrentWeek(20);
        second.setSeason(2030);
        repo.save(second);

        Optional<GameSession> loaded = repo.load();
        assertTrue(loaded.isPresent());
        assertEquals(20, loaded.get().getCurrentWeek());
        assertEquals(2030, loaded.get().getSeason());
    }

    @Test
    void roundTripWithZeroWeekWorks() {
        GameSession session = new GameSession();
        session.setCurrentWeek(0);
        session.setSeason(1);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();

        assertTrue(loaded.isPresent());
        assertEquals(0, loaded.get().getCurrentWeek());
    }
}