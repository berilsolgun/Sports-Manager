package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.football.FootballFixture;
import com.sportsmanager.football.FootballMatchResult;
import com.sportsmanager.football.FootballTeam;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StandingsCalculatorTest {

    private final StandingsCalculator calculator = new StandingsCalculator(3, 1);

    private static StandingEntry entry(String name, int points, int gf, int ga) {
        StandingEntry e = new StandingEntry();
        e.setTeam(new FootballTeam(name, "logo.png"));
        e.setPoints(points);
        e.setGoalsFor(gf);
        e.setGoalsAgainst(ga);
        return e;
    }

    @Test
    void compareSortsHigherPointsFirst() {
        StandingEntry leader = entry("A", 9, 0, 0);
        StandingEntry trailer = entry("B", 3, 0, 0);
        assertTrue(StandingsCalculator.compare(leader, trailer) < 0);
        assertTrue(StandingsCalculator.compare(trailer, leader) > 0);
    }

    @Test
    void compareUsesGoalDifferenceWhenPointsEqual() {
        StandingEntry betterGd = entry("A", 3, 5, 1);
        StandingEntry worseGd = entry("B", 3, 2, 2);
        assertTrue(StandingsCalculator.compare(betterGd, worseGd) < 0);
    }

    @Test
    void compareUsesGoalsForWhenPointsAndGoalDifferenceEqual() {
        StandingEntry moreGf = entry("A", 3, 4, 1);
        StandingEntry fewerGf = entry("B", 3, 3, 0);
        assertTrue(StandingsCalculator.compare(moreGf, fewerGf) < 0);
    }

    @Test
    void compareFallsBackToTeamNameWhenFullyTied() {
        StandingEntry alpha = entry("Alpha FC", 3, 2, 1);
        StandingEntry zebra = entry("Zebra FC", 3, 2, 1);
        assertTrue(StandingsCalculator.compare(alpha, zebra) < 0);
        assertTrue(StandingsCalculator.compare(zebra, alpha) > 0);
    }

    @Test
    void computeRecordsWinDrawLossAndPoints() {
        FootballTeam home = new FootballTeam("Home", "h.png");
        FootballTeam away = new FootballTeam("Away", "a.png");
        List<ITeam> teams = List.of(home, away);
        FootballFixture fx = new FootballFixture(home, away, 1);
        fx.setResult(new FootballMatchResult(2, 1, home, away, List.of(), List.of()));

        List<StandingEntry> table = calculator.compute(teams, List.of(fx));

        assertEquals(2, table.size());
        StandingEntry top = table.get(0);
        StandingEntry bottom = table.get(1);
        assertEquals(3, top.getPoints());
        assertEquals(0, bottom.getPoints());
        assertEquals(1, top.getWon());
        assertEquals(1, bottom.getLost());
        assertEquals(2, top.getGoalsFor());
        assertEquals(1, bottom.getGoalsFor());
    }

    @Test
    void computeSortsTableUsingSameRulesAsCompare() {
        FootballTeam a = new FootballTeam("A", "a.png");
        FootballTeam b = new FootballTeam("B", "b.png");
        FootballTeam c = new FootballTeam("C", "c.png");
        List<ITeam> teams = new ArrayList<>(List.of(a, b, c));

        FootballFixture f0 = new FootballFixture(a, b, 1);
        f0.setResult(new FootballMatchResult(1, 1, a, b, List.of(), List.of()));
        FootballFixture f1 = new FootballFixture(a, c, 2);
        f1.setResult(new FootballMatchResult(0, 3, a, c, List.of(), List.of()));
        FootballFixture f2 = new FootballFixture(b, c, 3);
        f2.setResult(new FootballMatchResult(2, 0, b, c, List.of(), List.of()));

        List<StandingEntry> table = calculator.compute(teams, List.of(f0, f1, f2));

        assertEquals(b.getName(), table.get(0).getTeam().getName());
        assertEquals(c.getName(), table.get(1).getTeam().getName());
        assertEquals(a.getName(), table.get(2).getTeam().getName());
    }
}
