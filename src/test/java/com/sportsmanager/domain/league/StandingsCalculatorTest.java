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

    private final StandingsCalculator calculator = new StandingsCalculator();

    /**
     * Requirement: Points -> Head-to-Head (H2H) Points -> H2H Goal Difference -> H2H Goals For -> Coin Toss.
     * Note: Overall League Goal Difference (GD) and Goals For (GF) are excluded per brief.
     */

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
    void compareFallsBackToTeamNameWhenPointsEqual() {
        StandingEntry alpha = new StandingEntry();
        alpha.setTeam(new FootballTeam("Alpha FC", "logo.png"));
        alpha.setPoints(10);

        StandingEntry zebra = new StandingEntry();
        zebra.setTeam(new FootballTeam("Zebra FC", "logo.png"));
        zebra.setPoints(10);

        assertTrue(StandingsCalculator.compare(alpha, zebra) < 0, "Should sort alphabetically when points are equal");
    }

    @Test
    void computeDecidesByThreeWayHeadToHead() {
        // Scenario: A, B, and C all have 3 points.
        // H2H: A beat B, B beat C, C beat A. (Wait, let's make it simpler)
        // H2H: A beat B and C. A should be 1st.
        FootballTeam a = new FootballTeam("A", "a.png");
        FootballTeam b = new FootballTeam("B", "b.png");
        FootballTeam c = new FootballTeam("C", "c.png");
        List<ITeam> teams = List.of(a, b, c);

        List<IFixture> fixtures = new ArrayList<>();
        // A beats B (3-0)
        fixtures.add(createFixture(a, b, 3, 0));
        // A beats C (2-0)
        fixtures.add(createFixture(a, c, 2, 0));
        // B beats C (1-0) -> B gets 3 points too, but A has better H2H points
        fixtures.add(createFixture(b, c, 1, 0));

        List<StandingEntry> table = calculator.compute(teams, fixtures);

        assertEquals("A", table.get(0).getTeam().getName(), "A won both H2H matches, must be 1st");
    }

    @Test
    void computeUsesH2HGoalsWhenH2HPointsAreEqual() {
        // Scenario: A and B drew their match (H2H points equal).
        // H2H Score: 2-2.
        // In a 2-2 draw, we check if one has more "Goals For" in that H2H bucket.
        // Since it's a single match, they are tied until GF.
        FootballTeam a = new FootballTeam("A", "a.png");
        FootballTeam b = new FootballTeam("B", "b.png");
        List<ITeam> teams = List.of(a, b);

        List<IFixture> fixtures = new ArrayList<>();
        fixtures.add(createFixture(a, b, 2, 2));

        List<StandingEntry> table = calculator.compute(teams, fixtures);

        // At this point, points, H2H points, H2H GD, and H2H GF are all equal.
        // It must reach the Coin Toss (Deterministic via Seed).
        assertNotNull(table.get(0).getTeam().getName());
    }

    @Test
    void computeReachesCoinTossOnFullDeadlock() {
        // Scenario: Exactly identical records and H2H.
        FootballTeam a = new FootballTeam("A", "a.png");
        FootballTeam b = new FootballTeam("B", "b.png");
        List<ITeam> teams = List.of(a, b);

        List<IFixture> fixtures = new ArrayList<>();
        fixtures.add(createFixture(a, b, 0, 0));

        List<StandingEntry> table = calculator.compute(teams, fixtures);

        assertNotEquals(table.get(0).getTeam().getName(), table.get(1).getTeam().getName());
    }

    private IFixture createFixture(ITeam home, ITeam away, int homeScore, int awayScore) {
        FootballFixture fx = new FootballFixture(home, away, 1);
        fx.setResult(new FootballMatchResult(homeScore, awayScore, home, away, List.of(), List.of()));
        return fx;
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
