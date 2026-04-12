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
    void seededEngineProducesDeterministicScores() {
        FootballFactory factory = new FootballFactory();
        ITeam home = factory.createTeam("H", "h.png");
        ITeam away = factory.createTeam("A", "a.png");

        IMatchResult r1 = new FootballMatchEngine(new Random(2026L)).simulate(home, away);
        ITeam home2 = factory.createTeam("H", "h.png");
        ITeam away2 = factory.createTeam("A", "a.png");
        IMatchResult r2 = new FootballMatchEngine(new Random(2026L)).simulate(home2, away2);

        assertEquals(r1.getHomeScore(), r2.getHomeScore());
        assertEquals(r1.getAwayScore(), r2.getAwayScore());
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