package com.sportsmanager.application;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballLeague;
import com.sportsmanager.football.FootballMatchEngine;
import com.sportsmanager.football.FootballSport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulationControllersTest {

    @Test
    void playWeekThenAdvanceWeekOrchestratesFixtures() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = new ArrayList<>();
        teams.add(factory.createTeam("Side A", "a.png"));
        teams.add(factory.createTeam("Side B", "b.png"));

        FootballLeague league = new FootballLeague("Test League", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);
        session.setSeason(1);

        MatchController matches = new MatchController();
        FootballMatchEngine engine = new FootballMatchEngine(new Random(12345L));
        matches.playCurrentWeek(session, engine);

        long played = league.getFixtures().stream().filter(IFixture::isPlayed).count();
        assertEquals(1, played);

        LeagueController leagueCtl = new LeagueController();
        List<StandingEntry> standings = leagueCtl.getStandings(session);
        assertEquals(2, standings.size());
        assertFalse(leagueCtl.isSeasonComplete(session));

        WeekController weeks = new WeekController();
        weeks.advanceWeek(session);
        assertEquals(2, session.getCurrentWeek());
    }

    @Test
    void prepareNewSeasonIncrementsSeasonResetsWeekAndRebuildsFixtures() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("Home", "h.png"),
                factory.createTeam("Away", "a.png"));
        FootballLeague league = new FootballLeague("Mini", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setSeason(3);
        session.setCurrentWeek(2);

        for (IFixture fx : league.getFixtures()) {
            league.recordResult(fx, new com.sportsmanager.football.FootballMatchResult(
                    1, 0, fx.getHomeTeam(), fx.getAwayTeam(), List.of(), List.of()));
        }
        assertTrue(league.isSeasonOver());

        new LeagueController().prepareNewSeason(session);

        assertEquals(4, session.getSeason());
        assertEquals(1, session.getCurrentWeek());
        assertFalse(league.isSeasonOver());
        assertEquals(0L, league.getFixtures().stream().filter(IFixture::isPlayed).count());
    }

    @Test
    void prepareNewSeasonHealsInjuredAndAgesAllPlayers() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("A", "a.png"),
                factory.createTeam("B", "b.png"));
        FootballLeague league = new FootballLeague("Mini", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setSeason(1);
        session.setCurrentWeek(1);

        List<IPlayer> snapshotAges = new ArrayList<>();
        for (ITeam t : teams) {
            for (IPlayer p : t.getSquad()) {
                snapshotAges.add(p);
                if (snapshotAges.size() <= 5) {
                    p.injure(2);
                }
            }
        }
        int[] before = snapshotAges.stream().mapToInt(IPlayer::getAge).toArray();

        new LeagueController().prepareNewSeason(session);

        for (int i = 0; i < snapshotAges.size(); i++) {
            assertFalse(snapshotAges.get(i).isInjured(), "player " + i + " should be healed");
            assertEquals(before[i] + 1, snapshotAges.get(i).getAge(), "player " + i + " should have aged 1 year");
        }
    }

    @Test
    void prepareNewSeasonClearsStartingElevenForAllTeams() {
        FootballFactory factory = new FootballFactory();
        com.sportsmanager.football.FootballTeam t1 = (com.sportsmanager.football.FootballTeam) factory.createTeam("A", "a.png");
        com.sportsmanager.football.FootballTeam t2 = (com.sportsmanager.football.FootballTeam) factory.createTeam("B", "b.png");
        FootballLeague league = new FootballLeague("Mini", List.of(t1, t2));
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setSeason(1);
        session.setCurrentWeek(1);

        t1.setStartingEleven(footballStarters(t1));
        t2.setStartingEleven(footballStarters(t2));
        assertEquals(11, t1.getStartingEleven().size());

        new LeagueController().prepareNewSeason(session);

        assertTrue(t1.getStartingEleven().isEmpty());
        assertTrue(t2.getStartingEleven().isEmpty());
    }

    private static List<IPlayer> footballStarters(com.sportsmanager.football.FootballTeam team) {
        List<IPlayer> squad = new ArrayList<>(team.getSquad());
        IPlayer gk = squad.stream()
                .filter(p -> p instanceof com.sportsmanager.football.FootballPlayer fp
                        && fp.getFootballPosition() == com.sportsmanager.football.FootballPosition.GK)
                .findFirst()
                .orElseThrow();
        List<IPlayer> others = squad.stream()
                .filter(p -> !(p instanceof com.sportsmanager.football.FootballPlayer fp
                        && fp.getFootballPosition() == com.sportsmanager.football.FootballPosition.GK))
                .limit(10)
                .toList();
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(gk);
        lineup.addAll(others);
        return lineup;
    }

    @Test
    void fullRoundRobinTwoTeamsCompletesSeasonAcrossTwoWeeks() {
        FootballFactory factory = new FootballFactory();
        List<ITeam> teams = List.of(
                factory.createTeam("Home", "h.png"),
                factory.createTeam("Away", "a.png"));
        FootballLeague league = new FootballLeague("Mini", teams);
        GameSession session = new GameSession();
        session.setLeague(league);
        session.setSport(new FootballSport());
        session.setCurrentWeek(1);

        MatchController matches = new MatchController();
        Random deterministic = new Random(777L);
        FootballMatchEngine engine = new FootballMatchEngine(deterministic);

        matches.playWeek(session, engine, 1);
        new WeekController().advanceWeek(session);
        matches.playWeek(session, engine, 2);

        LeagueController leagueCtl = new LeagueController();
        assertTrue(leagueCtl.isSeasonComplete(session));
        assertEquals(2, league.getFixtures().stream().filter(IFixture::isPlayed).count());
        for (StandingEntry row : leagueCtl.getStandings(session)) {
            assertEquals(2, row.getPlayed());
        }
    }
}
