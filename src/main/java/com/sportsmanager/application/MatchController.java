package com.sportsmanager.application;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.team.ITeam;

import java.util.List;

/**
 * Runs match simulation for fixtures in a given week and records results on the league.
 */
public class MatchController {

    public void playWeek(GameSession session, IMatchEngine engine, int week) {
        ILeague league = session.getLeague();
        if (league == null) {
            throw new IllegalStateException("GameSession has no league");
        }
        List<IFixture> weekFixtures = league.getWeekFixtures(week);
        for (IFixture fixture : weekFixtures) {
            if (fixture.isPlayed()) {
                continue;
            }
            ITeam home = fixture.getHomeTeam();
            ITeam away = fixture.getAwayTeam();
            IMatchResult result = engine.simulate(home, away);
            league.recordResult(fixture, result);
        }
    }

    /**
     * Simulates every fixture scheduled for {@link GameSession#getCurrentWeek()}.
     */
    public void playCurrentWeek(GameSession session, IMatchEngine engine) {
        playWeek(session, engine, session.getCurrentWeek());
    }
}
