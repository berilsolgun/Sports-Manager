package com.sportsmanager;

import com.sportsmanager.application.LeagueController;
import com.sportsmanager.application.MatchController;
import com.sportsmanager.application.WeekController;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.session.JsonGameRepository;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballLeague;
import com.sportsmanager.football.FootballMatchEngine;
import com.sportsmanager.football.FootballSport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulationIntegrationTest {

    private static final String TEST_FILE_1 = "it_session_1.json";
    private static final String TEST_FILE_2 = "it_session_2.json";

    @AfterEach
    void cleanup() {
        new File(TEST_FILE_1).delete();
        new File(TEST_FILE_2).delete();
    }

    @Test
    void simulateCurrentWeekEndToEndUpdatesFixturesAndStandings() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = new ArrayList<>();
        teams.add(factory.createTeam("Side A", "a.png"));
        teams.add(factory.createTeam("Side B", "b.png"));

        FootballLeague league = new FootballLeague("Test League", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);
        session.setSeason(2026);

        MatchController matchController = new MatchController();
        LeagueController leagueController = new LeagueController();
        FootballMatchEngine engine = new FootballMatchEngine(new Random(12345L));

        matchController.playCurrentWeek(session, engine);

        long playedCount = league.getFixtures().stream().filter(IFixture::isPlayed).count();
        assertEquals(1, playedCount);

        List<StandingEntry> standings = leagueController.getStandings(session);
        assertEquals(2, standings.size());
        assertEquals(1, standings.get(0).getPlayed());
        assertEquals(1, standings.get(1).getPlayed());
        assertFalse(leagueController.isSeasonComplete(session));
    }

    @Test
    void twoWeekSeasonFlowEndToEndCompletesSeason() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("Home", "h.png"),
                factory.createTeam("Away", "a.png")
        );

        FootballLeague league = new FootballLeague("Mini League", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);
        session.setSeason(2026);

        MatchController matchController = new MatchController();
        WeekController weekController = new WeekController();
        LeagueController leagueController = new LeagueController();
        FootballMatchEngine engine = new FootballMatchEngine(new Random(777L));

        matchController.playWeek(session, engine, 1);
        weekController.advanceWeek(session);
        matchController.playWeek(session, engine, 2);

        assertEquals(2, session.getCurrentWeek());
        assertTrue(leagueController.isSeasonComplete(session));
        assertEquals(2, league.getFixtures().stream().filter(IFixture::isPlayed).count());

        for (StandingEntry row : leagueController.getStandings(session)) {
            assertEquals(2, row.getPlayed());
        }
    }

    @Test
    void simulateThenSaveAndLoadPreservesWeekAndSeason() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("Alpha", "a.png"),
                factory.createTeam("Beta", "b.png")
        );

        FootballLeague league = new FootballLeague("SaveLoad League", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);
        session.setSeason(2030);

        MatchController matchController = new MatchController();
        WeekController weekController = new WeekController();
        FootballMatchEngine engine = new FootballMatchEngine(new Random(99L));

        matchController.playCurrentWeek(session, engine);
        weekController.advanceWeek(session);

        JsonGameRepository repo = new JsonGameRepository(TEST_FILE_1);
        repo.save(session);

        Optional<GameSession> loaded = repo.load();
        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().getCurrentWeek());
        assertEquals(2030, loaded.get().getSeason());
    }

    @Test
    void laterSaveOverwritesEarlierSessionState() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("Gamma", "g.png"),
                factory.createTeam("Delta", "d.png")
        );

        FootballLeague league = new FootballLeague("Overwrite League", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);
        session.setSeason(2028);

        MatchController matchController = new MatchController();
        WeekController weekController = new WeekController();
        FootballMatchEngine engine = new FootballMatchEngine(new Random(2026L));
        JsonGameRepository repo = new JsonGameRepository(TEST_FILE_2);

        repo.save(session);

        matchController.playCurrentWeek(session, engine);
        weekController.advanceWeek(session);
        repo.save(session);

        Optional<GameSession> loaded = repo.load();
        assertTrue(loaded.isPresent());
        assertEquals(2, loaded.get().getCurrentWeek());
        assertEquals(2028, loaded.get().getSeason());
    }
}