package com.sportsmanager.football;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.AbstractMatchEngine;
import com.sportsmanager.domain.team.ITeam;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates {@link FootballMatchEngine} behaviour tied to Pool 3 / {@link AbstractMatchEngine}.
 */
class FootballMatchEngineStructureTest {

    @Test
    void engineExtendsAbstractMatchEngine() {
        assertInstanceOf(AbstractMatchEngine.class, new FootballMatchEngine());
    }

    @Test
    void seededEngineProducesDeterministicScoresWhenSquadsAreIdentical() {
        FootballTeam home = deterministicSquadTeam("H");
        FootballTeam away = deterministicSquadTeam("A");

        IMatchResult r1 = new FootballMatchEngine(new Random(2026L)).simulate(home, away);
        IMatchResult r2 = new FootballMatchEngine(new Random(2026L)).simulate(
                deterministicSquadTeam("H"), deterministicSquadTeam("A"));

        assertEquals(r1.getHomeScore(), r2.getHomeScore());
        assertEquals(r1.getAwayScore(), r2.getAwayScore());
    }

    /** Same composition each call so match RNG is the only variable controlled by the engine seed. */
    private static FootballTeam deterministicSquadTeam(String clubName) {
        FootballTeam t = new FootballTeam(clubName, "logo.png");
        t.addPlayer(new FootballPlayer("GK", 25, FootballPosition.GK, 55, 55, 55, 55, 55, 55, 80));
        for (int i = 0; i < 10; i++) {
            t.addPlayer(new FootballPlayer("P" + i, 25, FootballPosition.CM, 60, 60, 60, 60, 60, 60, 30));
        }
        return t;
    }

    @Test
    void getMatchEventsIsUnmodifiableAfterSimulate() {
        FootballFactory factory = new FootballFactory();
        ITeam home = factory.createTeam("H", "h.png");
        ITeam away = factory.createTeam("A", "a.png");
        FootballMatchEngine engine = new FootballMatchEngine(new Random(5L));
        engine.simulate(home, away);
        assertThrows(UnsupportedOperationException.class, () -> engine.getMatchEvents().clear());
    }
}