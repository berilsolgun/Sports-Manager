package com.sportsmanager.application;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
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
