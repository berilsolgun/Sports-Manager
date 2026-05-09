package com.sportsmanager.domain.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.MatchEvent;
import com.sportsmanager.domain.simulation.MatchEventType;
import com.sportsmanager.domain.team.Tactic;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballMatchResult;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballSport;
import com.sportsmanager.football.FootballTeam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void roundTripPreservesCoachesTacticStartingElevenAndFixtureMetadata() {
        FootballSport sport = new FootballSport();
        FootballFactory factory = new FootballFactory();
        FootballTeam t1 = (FootballTeam) factory.createTeam("Alpha FC", "alpha_fc.png");
        var t2 = factory.createTeam("Beta FC", "beta_fc.png");
        t1.setTactic(new Tactic("4-3-3", 1.1, 0.95));
        List<com.sportsmanager.domain.team.IPlayer> starters = validFootballStarters(t1);
        t1.setStartingEleven(starters);

        GameSession session = new GameSession();
        session.setSport(sport);
        session.setSeason(1);
        session.setCurrentWeek(1);
        session.setPlayerTeam(t1);
        session.setLeague(factory.createLeague("Test League", List.of(t1, t2)));

        var league = session.getLeague();
        var fx = league.getWeekFixtures(1).get(0);
        var engine = factory.createMatchEngine();
        IMatchResult result = engine.simulate(fx.getHomeTeam(), fx.getAwayTeam());
        league.recordResult(fx, result);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();
        assertTrue(loaded.isPresent());
        GameSession s2 = loaded.get();
        assertEquals(2, s2.getLeague().getTeams().size());
        var lt = s2.getPlayerTeam();
        assertFalse(lt.getCoaches().isEmpty());
        assertNotNull(lt.getCurrentTactic());
        assertEquals("4-3-3", lt.getCurrentTactic().getName());
        assertEquals(11, lt.getStartingEleven().size());

        var played = s2.getLeague().getFixtures().stream().filter(f -> f.isPlayed()).findFirst();
        assertTrue(played.isPresent());
        var r = played.get().getResult().orElseThrow();
        assertEquals(result.getHomeScore(), r.getHomeScore());
        assertEquals(result.getAwayScore(), r.getAwayScore());
        assertEquals(result.getHomePoints(), r.getHomePoints());
        assertEquals(result.getAwayPoints(), r.getAwayPoints());
        assertEquals(result.getEvents().size(), r.getEvents().size());
    }

    @Test
    void roundTripPreservesMatchEventsAndInjuredPlayers() {
        FootballSport sport = new FootballSport();
        FootballFactory factory = new FootballFactory();
        FootballTeam home = (FootballTeam) factory.createTeam("Home United", "home.png");
        FootballTeam away = (FootballTeam) factory.createTeam("Away City", "away.png");

        IPlayer scorer = home.getSquad().get(5);
        IPlayer injuredHome = home.getSquad().get(7);
        IPlayer injuredAway = away.getSquad().get(3);
        injuredHome.injure(2);
        injuredAway.injure(1);

        List<MatchEvent> events = new ArrayList<>();
        events.add(new MatchEvent(12, 1, MatchEventType.GOAL, scorer.getName() + " scores!", home, scorer));
        events.add(new MatchEvent(34, 1, MatchEventType.YELLOW_CARD, "yellow", away, away.getSquad().get(0)));
        events.add(new MatchEvent(78, 2, MatchEventType.SUBSTITUTION, "sub", home, home.getSquad().get(2)));

        FootballMatchResult result = new FootballMatchResult(2, 1, home, away, events, List.of(injuredHome, injuredAway));

        GameSession session = new GameSession();
        session.setSport(sport);
        session.setSeason(1);
        session.setCurrentWeek(1);
        session.setPlayerTeam(home);
        session.setLeague(factory.createLeague("Test League", List.of(home, away)));

        IFixture fx = session.getLeague().getWeekFixtures(1).get(0);
        session.getLeague().recordResult(fx, result);

        repo.save(session);
        Optional<GameSession> loaded = repo.load();
        assertTrue(loaded.isPresent());

        IFixture playedFx = loaded.get().getLeague().getFixtures().stream()
                .filter(IFixture::isPlayed).findFirst().orElseThrow();
        IMatchResult restored = playedFx.getResult().orElseThrow();

        assertEquals(events.size(), restored.getEvents().size());
        for (int i = 0; i < events.size(); i++) {
            MatchEvent original = events.get(i);
            MatchEvent reloaded = restored.getEvents().get(i);
            assertEquals(original.getMinute(), reloaded.getMinute());
            assertEquals(original.getPhase(), reloaded.getPhase());
            assertEquals(original.getType(), reloaded.getType());
            assertEquals(original.getDescription(), reloaded.getDescription());
        }

        assertEquals(2, restored.getInjuredPlayers().size());
        var injuredNames = restored.getInjuredPlayers().stream().map(IPlayer::getName).toList();
        assertTrue(injuredNames.contains(injuredHome.getName()));
        assertTrue(injuredNames.contains(injuredAway.getName()));
    }

    private static List<IPlayer> validFootballStarters(FootballTeam team) {
        List<IPlayer> squad = new ArrayList<>(team.getSquad());
        IPlayer gk = squad.stream()
                .filter(p -> p instanceof FootballPlayer fp && fp.getFootballPosition() == FootballPosition.GK)
                .findFirst()
                .orElseThrow();
        List<IPlayer> others = squad.stream()
                .filter(p -> !(p instanceof FootballPlayer fp && fp.getFootballPosition() == FootballPosition.GK))
                .limit(10)
                .toList();
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(gk);
        lineup.addAll(others);
        return lineup;
    }
}