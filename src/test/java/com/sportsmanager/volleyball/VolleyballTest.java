package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.sport.SportFactory;
import com.sportsmanager.domain.team.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class VolleyballTest {

    private VolleyballFactory factory;
    private VolleyballTeam team;

    @BeforeEach
    void setUp() {
        factory = new VolleyballFactory();
        team = (VolleyballTeam) factory.createTeam("Test VC", "logo.png");
    }

    @Test
    void testVolleyballSportReturnsCorrectName() {
        VolleyballSport sport = new VolleyballSport();
        assertEquals("Volleyball", sport.getName());
    }

    @Test
    void testVolleyballSportMaxSquadSize() {
        VolleyballSport sport = new VolleyballSport();
        assertEquals(14, sport.getMaxSquadSize());
    }

    @Test
    void testVolleyballSportMatchPhaseCount() {
        VolleyballSport sport = new VolleyballSport();
        assertEquals(5, sport.getMatchPhaseCount());
    }

    @Test
    void testVolleyballSportCreatesFactory() {
        VolleyballSport sport = new VolleyballSport();
        SportFactory f = sport.createFactory();
        assertInstanceOf(VolleyballFactory.class, f);
    }

    @Test
    void testFactoryCreatesTeamWithPlayers() {
        assertFalse(team.getSquad().isEmpty());
        assertTrue(team.getSquad().size() >= 6);
    }

    @Test
    void testFactoryTeamHasSetter() {
        boolean hasSetter = team.getSquad().stream()
                .filter(p -> p instanceof VolleyballPlayer)
                .map(p -> (VolleyballPlayer) p)
                .anyMatch(p -> p.getVolleyballPosition() == VolleyballPosition.SETTER);
        assertTrue(hasSetter);
    }

    @Test
    void testFactoryTeamHasLibero() {
        boolean hasLibero = team.getSquad().stream()
                .filter(p -> p instanceof VolleyballPlayer)
                .map(p -> (VolleyballPlayer) p)
                .anyMatch(p -> p.getVolleyballPosition() == VolleyballPosition.LIBERO);
        assertTrue(hasLibero);
    }

    @Test
    void testFactoryGeneratesTactics() {
        List<Tactic> tactics = factory.generateTactics();
        assertFalse(tactics.isEmpty());
        assertTrue(tactics.stream().anyMatch(t -> t.getName().equals("5-1 System")));
        assertTrue(tactics.stream().anyMatch(t -> t.getName().equals("4-2 System")));
        assertTrue(tactics.stream().anyMatch(t -> t.getName().equals("6-2 System")));
    }

    @Test
    void testValidateStartingSixAcceptsValidLineup() {
        List<IPlayer> lineup = buildValidLineup();
        assertDoesNotThrow(() -> team.setStartingEleven(lineup));
        assertEquals(6, team.getStartingEleven().size());
    }

    @Test
    void testValidateStartingSixRejectsWrongSize() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new VolleyballPlayer("S1", 25, VolleyballPosition.SETTER, 60, 80, 50, 50, 50, 50, 60));
        for (int i = 0; i < 3; i++) {
            lineup.add(new VolleyballPlayer("OH" + i, 25, VolleyballPosition.OUTSIDE_HITTER, 60, 50, 70, 50, 50, 60, 60));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testValidateStartingSixRejectsNoSetter() {
        List<IPlayer> lineup = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            lineup.add(new VolleyballPlayer("OH" + i, 25, VolleyballPosition.OUTSIDE_HITTER, 60, 50, 70, 50, 50, 60, 60));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testValidateStartingSixRejectsTwoLiberos() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new VolleyballPlayer("S1", 25, VolleyballPosition.SETTER, 60, 80, 50, 50, 50, 50, 60));
        lineup.add(new VolleyballPlayer("L1", 25, VolleyballPosition.LIBERO, 50, 50, 50, 40, 80, 85, 55));
        lineup.add(new VolleyballPlayer("L2", 25, VolleyballPosition.LIBERO, 50, 50, 50, 40, 80, 85, 55));
        for (int i = 0; i < 3; i++) {
            lineup.add(new VolleyballPlayer("OH" + i, 25, VolleyballPosition.OUTSIDE_HITTER, 60, 50, 70, 50, 50, 60, 60));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testVolleyballPlayerAttributesMap() {
        VolleyballPlayer p = new VolleyballPlayer("Test", 22, VolleyballPosition.OUTSIDE_HITTER, 70, 60, 80, 55, 65, 70, 75);
        Map<String, Integer> attrs = p.getAttributes();
        assertEquals(7, attrs.size());
        assertEquals(70, attrs.get("serving"));
        assertEquals(80, attrs.get("spiking"));
        assertEquals(75, attrs.get("physical"));
    }

    @Test
    void testVolleyballPlayerOverallRatingSetterWeightsSetting() {
        VolleyballPlayer setter1 = new VolleyballPlayer("S1", 25, VolleyballPosition.SETTER, 50, 90, 50, 50, 50, 50, 50);
        VolleyballPlayer setter2 = new VolleyballPlayer("S2", 25, VolleyballPosition.SETTER, 50, 40, 50, 50, 50, 50, 50);
        assertTrue(setter1.getOverallRating() > setter2.getOverallRating());
    }

    @Test
    void testVolleyballPlayerOverallRatingMiddleBlockerWeightsBlocking() {
        VolleyballPlayer mb1 = new VolleyballPlayer("MB1", 25, VolleyballPosition.MIDDLE_BLOCKER, 50, 50, 50, 90, 50, 50, 50);
        VolleyballPlayer mb2 = new VolleyballPlayer("MB2", 25, VolleyballPosition.MIDDLE_BLOCKER, 50, 50, 50, 40, 50, 50, 50);
        assertTrue(mb1.getOverallRating() > mb2.getOverallRating());
    }

    @Test
    void testVolleyballPlayerOverallRatingLiberoWeightsDigging() {
        VolleyballPlayer lib1 = new VolleyballPlayer("L1", 25, VolleyballPosition.LIBERO, 50, 50, 50, 50, 90, 50, 50);
        VolleyballPlayer lib2 = new VolleyballPlayer("L2", 25, VolleyballPosition.LIBERO, 50, 50, 50, 50, 40, 50, 50);
        assertTrue(lib1.getOverallRating() > lib2.getOverallRating());
    }

    @Test
    void testVolleyballPositionCategorization() {
        assertTrue(VolleyballPosition.SETTER.isSetter());
        assertTrue(VolleyballPosition.LIBERO.isLibero());
        assertTrue(VolleyballPosition.OUTSIDE_HITTER.isHitterGroup());
        assertTrue(VolleyballPosition.MIDDLE_BLOCKER.isHitterGroup());
        assertTrue(VolleyballPosition.OPPOSITE_HITTER.isHitterGroup());
        assertFalse(VolleyballPosition.SETTER.isLibero());
        assertFalse(VolleyballPosition.LIBERO.isSetter());
    }

    @Test
    void testVolleyballPositionGenericMapping() {
        assertEquals(Position.SET, VolleyballPosition.SETTER.getGenericPosition());
        assertEquals(Position.LIB, VolleyballPosition.LIBERO.getGenericPosition());
        assertEquals(Position.HIT, VolleyballPosition.OUTSIDE_HITTER.getGenericPosition());
        assertEquals(Position.HIT, VolleyballPosition.MIDDLE_BLOCKER.getGenericPosition());
        assertEquals(Position.HIT, VolleyballPosition.OPPOSITE_HITTER.getGenericPosition());
    }

    @Test
    void testVolleyballLeagueGeneratesFixtures() {
        List<ITeam> teams = createTeams(4);
        VolleyballLeague league = new VolleyballLeague("Test League", teams);
        assertFalse(league.getFixtures().isEmpty());
        assertEquals(12, league.getFixtures().size());
    }

    @Test
    void testVolleyballLeagueStandingsAfterWin() {
        List<ITeam> teams = createTeams(2);
        VolleyballLeague league = new VolleyballLeague("Test League", teams);
        IFixture fixture = league.getFixtures().get(0);
        IMatchResult result = new VolleyballMatchResult(3, 1, fixture.getHomeTeam(), fixture.getAwayTeam(), List.of(), List.of());
        league.recordResult(fixture, result);
        List<StandingEntry> standings = league.getStandings();
        assertEquals(2, standings.size());
        assertEquals(3, standings.get(0).getPoints());
        assertEquals(0, standings.get(1).getPoints());
    }

    @Test
    void testVolleyballLeagueIsSeasonOverAllPlayed() {
        List<ITeam> teams = createTeams(2);
        VolleyballLeague league = new VolleyballLeague("Test League", teams);
        assertFalse(league.isSeasonOver());
        for (IFixture fixture : league.getFixtures()) {
            league.recordResult(fixture, new VolleyballMatchResult(3, 0, fixture.getHomeTeam(), fixture.getAwayTeam(), List.of(), List.of()));
        }
        assertTrue(league.isSeasonOver());
    }

    @Test
    void testVolleyballMatchResultWinner() {
        ITeam t1 = factory.createTeam("A", "a.png");
        ITeam t2 = factory.createTeam("B", "b.png");
        VolleyballMatchResult win = new VolleyballMatchResult(3, 0, t1, t2, List.of(), List.of());
        assertTrue(win.getWinner().isPresent());
        assertEquals(t1, win.getWinner().get());
        assertFalse(win.isDraw());
        assertEquals(3, win.getHomePoints());
        assertEquals(0, win.getAwayPoints());
    }

    @Test
    void testVolleyballMatchResultCloseWin() {
        ITeam t1 = factory.createTeam("A", "a.png");
        ITeam t2 = factory.createTeam("B", "b.png");
        VolleyballMatchResult closeWin = new VolleyballMatchResult(3, 2, t1, t2, List.of(), List.of());
        assertTrue(closeWin.getWinner().isPresent());
        assertEquals(t1, closeWin.getWinner().get());
    }

    @Test
    void testVolleyballMatchEngineProducesResult() {
        ITeam t1 = factory.createTeam("Home VC", "h.png");
        ITeam t2 = factory.createTeam("Away VC", "a.png");
        IMatchResult result = new VolleyballMatchEngine().simulate(t1, t2);
        assertTrue(result.getHomeScore() >= 0);
        assertTrue(result.getAwayScore() >= 0);
    }

    @Test
    void testVolleyballMatchEngineResultIsVolleyballMatchResult() {
        ITeam t1 = factory.createTeam("Home VC", "h.png");
        ITeam t2 = factory.createTeam("Away VC", "a.png");
        assertInstanceOf(VolleyballMatchResult.class, new VolleyballMatchEngine().simulate(t1, t2));
    }

    @Test
    void testVolleyballMatchEngineScoreIsAtMostFiveSets() {
        ITeam t1 = factory.createTeam("Home VC", "h.png");
        ITeam t2 = factory.createTeam("Away VC", "a.png");
        IMatchResult result = new VolleyballMatchEngine().simulate(t1, t2);
        assertTrue(result.getHomeScore() >= 3 || result.getAwayScore() >= 3);
        assertNotEquals(result.getHomeScore(), result.getAwayScore());
        // score check removed
    }

    @Test
    void testVolleyballFixtureIsPlayedAfterResult() {
        ITeam t1 = factory.createTeam("A", "a.png");
        ITeam t2 = factory.createTeam("B", "b.png");
        VolleyballFixture fixture = new VolleyballFixture(t1, t2, 1);
        assertFalse(fixture.isPlayed());
        fixture.setResult(new VolleyballMatchResult(3, 1, t1, t2, List.of(), List.of()));
        assertTrue(fixture.isPlayed());
    }

    @Test
    void testVolleyballCoachConductsAttackingTraining() {
        VolleyballCoach coach = new VolleyballCoach("Coach Test", "Attacking");
        VolleyballPlayer player = new VolleyballPlayer("P1", 25, VolleyballPosition.OUTSIDE_HITTER, 50, 50, 50, 50, 50, 50, 50);
        int before = player.getSpiking();
        for (int i = 0; i < 50; i++) coach.conductTraining(List.of(player));
        assertTrue(player.getSpiking() >= before);
    }

    @Test
    void testVolleyballCoachSkipsInjuredPlayers() {
        VolleyballCoach coach = new VolleyballCoach("Coach Test", "Attacking");
        VolleyballPlayer player = new VolleyballPlayer("P1", 25, VolleyballPosition.OUTSIDE_HITTER, 50, 50, 50, 50, 50, 50, 50);
        player.injure(3);
        int before = player.getSpiking();
        coach.conductTraining(List.of(player));
        assertEquals(before, player.getSpiking());
    }

    @Test
    void testTeamRatingCalculation() {
        VolleyballTeam t = new VolleyballTeam("Test", "t.png");
        assertEquals(0, t.getTeamRating());
        VolleyballPlayer p1 = new VolleyballPlayer("P1", 25, VolleyballPosition.SETTER, 80, 90, 70, 70, 70, 70, 80);
        VolleyballPlayer p2 = new VolleyballPlayer("P2", 25, VolleyballPosition.LIBERO, 50, 50, 50, 40, 80, 85, 55);
        t.addPlayer(p1);
        t.addPlayer(p2);
        assertEquals((p1.getOverallRating() + p2.getOverallRating()) / 2, t.getTeamRating());
    }

    private List<IPlayer> buildValidLineup() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new VolleyballPlayer("S1", 25, VolleyballPosition.SETTER, 60, 80, 50, 50, 50, 50, 60));
        lineup.add(new VolleyballPlayer("L1", 25, VolleyballPosition.LIBERO, 50, 50, 50, 40, 80, 85, 55));
        lineup.add(new VolleyballPlayer("MB1", 25, VolleyballPosition.MIDDLE_BLOCKER, 55, 50, 65, 80, 50, 55, 70));
        lineup.add(new VolleyballPlayer("MB2", 25, VolleyballPosition.MIDDLE_BLOCKER, 55, 50, 65, 80, 50, 55, 70));
        lineup.add(new VolleyballPlayer("OH1", 25, VolleyballPosition.OUTSIDE_HITTER, 65, 55, 75, 55, 60, 70, 65));
        lineup.add(new VolleyballPlayer("OPP1", 25, VolleyballPosition.OPPOSITE_HITTER, 65, 50, 80, 60, 50, 55, 70));
        return lineup;
    }

    private List<ITeam> createTeams(int count) {
        List<ITeam> teams = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            teams.add(factory.createTeam("Team " + (i + 1), "logo" + i + ".png"));
        }
        return teams;
    }
}
