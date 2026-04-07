import static org.junit.jupiter.api.Assertions.*;

import com.sportsmanager.domain.team.AbstractPlayer;
import com.sportsmanager.domain.team.Position;
import org.junit.jupiter.api.Test;

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
}