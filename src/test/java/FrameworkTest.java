import static org.junit.jupiter.api.Assertions.*;

import com.sportsmanager.domain.league.AbstractLeague;
import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.league.StandingEntry;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.team.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FrameworkTest {

    class TestPlayer extends AbstractPlayer {
        public TestPlayer(String n, int a, Position p) { super(n, a, p); }
        @Override public int getOverallRating() { return 75; }

        @Override
        public int getInjuryGamesRemaining() {
            return this.injuryGamesRemaining;
        }

        @Override public Map<String, Integer> getAttributes() { return null; }
    }

    class TestTeam extends AbstractTeam {
        public TestTeam(String n, String l) { super(n, l); }

        @Override
        public boolean validateStartingEleven(List<IPlayer> players) {
            return !players.isEmpty();
        }

        @Override public int getTeamRating() { return 100; }
        @Override public List<ICoach> getCoaches() { return new ArrayList<>(); }
    }

    class TestLeague extends AbstractLeague {
        public TestLeague(String n, List<ITeam> t) { super(n, t); }

        @Override
        public void recordResult(IFixture fixture, IMatchResult result) {

        }

        @Override public List<StandingEntry> getStandings() { return null; }
        @Override public boolean isSeasonOver() { return false; }

        @Override
        public List<IFixture> getWeekFixtures(int week) {
            return new ArrayList<>();
        }
    }

    @Test
    void testInjurySystem() {
        TestPlayer p = new TestPlayer("Test", 20, Position.FW);
        p.injure(2);
        assertTrue(p.isInjured());

        p.recoverOneGame();
        assertEquals(1, p.getInjuryGamesRemaining());

        p.recoverOneGame();
        assertFalse(p.isInjured());
    }

    @Test
    void testPlayerIdentity(){
        TestPlayer p = new TestPlayer("Test", 20, Position.FW);
        Assertions.assertEquals("Test", p.getName());
        Assertions.assertEquals(20, p.getAge());
        Assertions.assertEquals(Position.FW, p.getPosition());

    }

    @Test
    void testTeamSquadManagement() {
        TestTeam team = new TestTeam("Lions", "logo.png");
        TestPlayer p1 = new TestPlayer("P1", 20, Position.GK);

        team.addPlayer(p1);
        Assertions.assertEquals(1, team.getSquad().size());
        Assertions.assertTrue(team.getSquad().contains(p1));

    }

    @Test
    void testInvalidStartingElevenThrowsException(){
        TestTeam team = new TestTeam("Seals", "logo.png");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            team.setStartingEleven(new ArrayList<>());
        });
    }

    @Test
    void testLeagueTeamAssignment() {
        List<ITeam> teams = new ArrayList<>();
        teams.add(new TestTeam("Team A", "logoA.png"));

        TestLeague league = new TestLeague("Super Lig", teams);
        Assertions.assertEquals(1, league.getTeams().size());
        Assertions.assertEquals("Team A", league.getTeams().get(0).getName());
    }

    @Test
    void testGameSessionState() {
        GameSession session = new GameSession();
        TestTeam myTeam = new TestTeam("Lions", "logo.png");

        session.setPlayerTeam(myTeam);
        session.setCurrentWeek(5);
        session.setSeason(2026);

        Assertions.assertEquals("Lions", session.getPlayerTeam().getName(), "Team names don't match.");
        Assertions.assertEquals(5, session.getCurrentWeek(), "Week info is wrong.");
        Assertions.assertEquals(2026, session.getSeason(), "Session info is wrong.");
    }

}