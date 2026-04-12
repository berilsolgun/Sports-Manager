package com.sportsmanager.football;

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

class FootballTest {

    private FootballFactory factory;
    private FootballTeam team;

    @BeforeEach
    void setUp() {
        factory = new FootballFactory();
        team = (FootballTeam) factory.createTeam("Test FC", "logo.png");
    }

    @Test
    void testFootballSportReturnsCorrectName() {
        FootballSport sport = new FootballSport();
        assertEquals("Football", sport.getName());
    }

    @Test
    void testFootballSportMaxSquadSize() {
        FootballSport sport = new FootballSport();
        assertEquals(25, sport.getMaxSquadSize());
    }

    @Test
    void testFootballSportMatchPhaseCount() {
        FootballSport sport = new FootballSport();
        assertEquals(2, sport.getMatchPhaseCount());
    }

    @Test
    void testFootballSportCreatesFactory() {
        FootballSport sport = new FootballSport();
        SportFactory f = sport.createFactory();
        assertInstanceOf(FootballFactory.class, f);
    }

    @Test
    void testFactoryCreatesTeamWithPlayers() {
        assertFalse(team.getSquad().isEmpty());
        assertTrue(team.getSquad().size() >= 11);
    }

    @Test
    void testFactoryTeamHasGoalkeeper() {
        boolean hasGK = team.getSquad().stream()
                .filter(p -> p instanceof FootballPlayer)
                .map(p -> (FootballPlayer) p)
                .anyMatch(p -> p.getFootballPosition() == FootballPosition.GK);
        assertTrue(hasGK);
    }

    @Test
    void testValidateStartingElevenAcceptsValidLineup() {
        List<IPlayer> lineup = buildValidLineup();
        assertDoesNotThrow(() -> team.setStartingEleven(lineup));
        assertEquals(11, team.getStartingEleven().size());
    }

    @Test
    void testValidateStartingElevenRejectsTwoGoalkeepers() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new FootballPlayer("GK1", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 80));
        lineup.add(new FootballPlayer("GK2", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 80));
        for (int i = 0; i < 9; i++) {
            lineup.add(new FootballPlayer("P" + i, 25, FootballPosition.CM, 50, 50, 50, 50, 50, 50, 30));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testValidateStartingElevenRejectsWrongSize() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new FootballPlayer("GK1", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 80));
        for (int i = 0; i < 5; i++) {
            lineup.add(new FootballPlayer("P" + i, 25, FootballPosition.CM, 50, 50, 50, 50, 50, 50, 30));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testValidateStartingElevenRejectsNoGoalkeeper() {
        List<IPlayer> lineup = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            lineup.add(new FootballPlayer("P" + i, 25, FootballPosition.CM, 50, 50, 50, 50, 50, 50, 30));
        }
        assertThrows(IllegalArgumentException.class, () -> team.setStartingEleven(lineup));
    }

    @Test
    void testFootballPlayerOverallRatingGKWeightsGoalkeeping() {
        FootballPlayer gk = new FootballPlayer("GK", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 90);
        FootballPlayer gk2 = new FootballPlayer("GK2", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 40);
        assertTrue(gk.getOverallRating() > gk2.getOverallRating());
    }

    @Test
    void testFootballPlayerOverallRatingSTWeightsShooting() {
        FootballPlayer st1 = new FootballPlayer("ST1", 25, FootballPosition.ST, 50, 90, 50, 50, 50, 50, 50);
        FootballPlayer st2 = new FootballPlayer("ST2", 25, FootballPosition.ST, 50, 40, 50, 50, 50, 50, 50);
        assertTrue(st1.getOverallRating() > st2.getOverallRating());
    }

    @Test
    void testFootballPlayerAttributesMap() {
        FootballPlayer p = new FootballPlayer("Test", 25, FootballPosition.CM, 70, 65, 80, 75, 60, 70, 30);
        Map<String, Integer> attrs = p.getAttributes();
        assertEquals(7, attrs.size());
        assertEquals(70, attrs.get("pace"));
        assertEquals(80, attrs.get("passing"));
    }

    @Test
    void testFootballPositionCategorization() {
        assertTrue(FootballPosition.GK.isGoalkeeper());
        assertTrue(FootballPosition.CB.isDefender());
        assertTrue(FootballPosition.CM.isMidfielder());
        assertTrue(FootballPosition.ST.isAttacker());
        assertFalse(FootballPosition.GK.isDefender());
        assertFalse(FootballPosition.CB.isAttacker());
    }

    @Test
    void testFootballPositionGenericMapping() {
        assertEquals(Position.GK, FootballPosition.GK.getGenericPosition());
        assertEquals(Position.DF, FootballPosition.CB.getGenericPosition());
        assertEquals(Position.DF, FootballPosition.LB.getGenericPosition());
        assertEquals(Position.MF, FootballPosition.CM.getGenericPosition());
        assertEquals(Position.FW, FootballPosition.ST.getGenericPosition());
    }

    @Test
    void testFootballLeagueGeneratesFixtures() {
        List<ITeam> teams = createTeams(4);
        FootballLeague league = new FootballLeague("Test League", teams);
        assertFalse(league.getFixtures().isEmpty());
        assertEquals(12, league.getFixtures().size());
    }

    @Test
    void testFootballLeagueStandingsAfterResults() {
        List<ITeam> teams = createTeams(2);
        FootballLeague league = new FootballLeague("Test League", teams);

        IFixture fixture = league.getFixtures().get(0);
        IMatchResult result = new FootballMatchResult(3, 1,
                fixture.getHomeTeam(), fixture.getAwayTeam(), List.of(), List.of());
        league.recordResult(fixture, result);

        List<StandingEntry> standings = league.getStandings();
        assertEquals(2, standings.size());
        assertEquals(3, standings.get(0).getPoints());
        assertEquals(0, standings.get(1).getPoints());
    }

    @Test
    void testFootballLeagueDrawGivesOnePointEach() {
        List<ITeam> teams = createTeams(2);
        FootballLeague league = new FootballLeague("Test League", teams);

        IFixture fixture = league.getFixtures().get(0);
        IMatchResult result = new FootballMatchResult(1, 1,
                fixture.getHomeTeam(), fixture.getAwayTeam(), List.of(), List.of());
        league.recordResult(fixture, result);

        List<StandingEntry> standings = league.getStandings();
        assertEquals(1, standings.get(0).getPoints());
        assertEquals(1, standings.get(1).getPoints());
    }

    @Test
    void testFootballLeagueIsSeasonOverAllPlayed() {
        List<ITeam> teams = createTeams(2);
        FootballLeague league = new FootballLeague("Test League", teams);
        assertFalse(league.isSeasonOver());

        for (IFixture fixture : league.getFixtures()) {
            IMatchResult result = new FootballMatchResult(1, 0,
                    fixture.getHomeTeam(), fixture.getAwayTeam(), List.of(), List.of());
            league.recordResult(fixture, result);
        }
        assertTrue(league.isSeasonOver());
    }

    @Test
    void testFootballMatchResultWinnerAndDraw() {
        ITeam t1 = factory.createTeam("A", "a.png");
        ITeam t2 = factory.createTeam("B", "b.png");

        FootballMatchResult win = new FootballMatchResult(2, 0, t1, t2, List.of(), List.of());
        assertTrue(win.getWinner().isPresent());
        assertEquals(t1, win.getWinner().get());
        assertFalse(win.isDraw());
        assertEquals(3, win.getHomePoints());
        assertEquals(0, win.getAwayPoints());

        FootballMatchResult draw = new FootballMatchResult(1, 1, t1, t2, List.of(), List.of());
        assertTrue(draw.isDraw());
        assertTrue(draw.getWinner().isEmpty());
        assertEquals(1, draw.getHomePoints());
        assertEquals(1, draw.getAwayPoints());
    }

    @Test
    void testFootballMatchEngineProducesResult() {
        ITeam t1 = factory.createTeam("Home FC", "h.png");
        ITeam t2 = factory.createTeam("Away FC", "a.png");
        FootballMatchEngine engine = new FootballMatchEngine();
        IMatchResult result = engine.simulate(t1, t2);
        assertTrue(result.getHomeScore() >= 0);
        assertTrue(result.getAwayScore() >= 0);
    }

    @Test
    void testFootballCoachConductsTraining() {
        FootballCoach coach = new FootballCoach("Coach Test", "Attacking");
        FootballPlayer player = new FootballPlayer("P1", 25, FootballPosition.ST, 50, 50, 50, 50, 50, 50, 30);
        int shootingBefore = player.getShooting();
        for (int i = 0; i < 50; i++) {
            coach.conductTraining(List.of(player));
        }
        assertTrue(player.getShooting() >= shootingBefore);
    }

    @Test
    void testFootballCoachSkipsInjuredPlayers() {
        FootballCoach coach = new FootballCoach("Coach Test", "Attacking");
        FootballPlayer player = new FootballPlayer("P1", 25, FootballPosition.ST, 50, 50, 50, 50, 50, 50, 30);
        player.injure(3);
        int shootingBefore = player.getShooting();
        coach.conductTraining(List.of(player));
        assertEquals(shootingBefore, player.getShooting());
    }

    @Test
    void testFootballFixtureIsPlayedAfterResult() {
        ITeam t1 = factory.createTeam("A", "a.png");
        ITeam t2 = factory.createTeam("B", "b.png");
        FootballFixture fixture = new FootballFixture(t1, t2, 1);
        assertFalse(fixture.isPlayed());
        assertTrue(fixture.getResult().isEmpty());

        fixture.setResult(new FootballMatchResult(1, 0, t1, t2, List.of(), List.of()));
        assertTrue(fixture.isPlayed());
        assertTrue(fixture.getResult().isPresent());
    }

    @Test
    void testTeamRatingCalculation() {
        FootballTeam t = new FootballTeam("Test", "t.png");
        assertEquals(0, t.getTeamRating());

        FootballPlayer p1 = new FootballPlayer("P1", 25, FootballPosition.CM, 80, 80, 80, 80, 80, 80, 80);
        FootballPlayer p2 = new FootballPlayer("P2", 25, FootballPosition.CM, 60, 60, 60, 60, 60, 60, 60);
        t.addPlayer(p1);
        t.addPlayer(p2);

        int avg = (p1.getOverallRating() + p2.getOverallRating()) / 2;
        assertEquals(avg, t.getTeamRating());
    }

    @Test
    void testFactoryGeneratesTactics() {
        List<Tactic> tactics = factory.generateTactics();
        assertFalse(tactics.isEmpty());
        assertTrue(tactics.stream().anyMatch(t -> t.getName().equals("4-4-2")));
        assertTrue(tactics.stream().anyMatch(t -> t.getName().equals("4-3-3")));
    }

    @Test
    void testLeagueStandingsSortedByGoalDifference() {
        List<ITeam> teams = createTeams(3);
        FootballLeague league = new FootballLeague("Test", teams);

        List<IFixture> fixtures = league.getFixtures();
        league.recordResult(fixtures.get(0),
                new FootballMatchResult(5, 0, fixtures.get(0).getHomeTeam(), fixtures.get(0).getAwayTeam(), List.of(), List.of()));
        league.recordResult(fixtures.get(1),
                new FootballMatchResult(1, 0, fixtures.get(1).getHomeTeam(), fixtures.get(1).getAwayTeam(), List.of(), List.of()));

        List<StandingEntry> standings = league.getStandings();
        int gd0 = standings.get(0).getGoalsFor() - standings.get(0).getGoalsAgainst();
        int gd1 = standings.get(1).getGoalsFor() - standings.get(1).getGoalsAgainst();
        assertTrue(gd0 >= gd1);
    }

    private List<IPlayer> buildValidLineup() {
        List<IPlayer> lineup = new ArrayList<>();
        lineup.add(new FootballPlayer("GK1", 25, FootballPosition.GK, 50, 50, 50, 50, 50, 50, 80));
        lineup.add(new FootballPlayer("CB1", 25, FootballPosition.CB, 50, 50, 50, 50, 70, 60, 30));
        lineup.add(new FootballPlayer("CB2", 25, FootballPosition.CB, 50, 50, 50, 50, 70, 60, 30));
        lineup.add(new FootballPlayer("LB1", 25, FootballPosition.LB, 70, 50, 60, 60, 60, 60, 30));
        lineup.add(new FootballPlayer("RB1", 25, FootballPosition.RB, 70, 50, 60, 60, 60, 60, 30));
        lineup.add(new FootballPlayer("CM1", 25, FootballPosition.CM, 60, 60, 70, 65, 60, 60, 30));
        lineup.add(new FootballPlayer("CM2", 25, FootballPosition.CM, 60, 60, 70, 65, 60, 60, 30));
        lineup.add(new FootballPlayer("CM3", 25, FootballPosition.CDM, 55, 50, 65, 55, 70, 65, 30));
        lineup.add(new FootballPlayer("LW1", 25, FootballPosition.LW, 85, 70, 65, 80, 40, 55, 30));
        lineup.add(new FootballPlayer("RW1", 25, FootballPosition.RW, 85, 70, 65, 80, 40, 55, 30));
        lineup.add(new FootballPlayer("ST1", 25, FootballPosition.ST, 75, 85, 60, 70, 35, 65, 30));
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
