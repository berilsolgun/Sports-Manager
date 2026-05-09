package com.sportsmanager.application;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.domain.team.Tactic;

import java.util.List;
import java.util.Random;

/**
 * Runs match simulation for fixtures in a given week and records results on the league.
 * Auto-assigns random tactics to AI teams (non-player teams).
 */
public class MatchController {

    private final Random random = new Random();

    public void playWeek(GameSession session, IMatchEngine engine, int week) {
        ILeague league = session.getLeague();
        if (league == null) {
            throw new IllegalStateException("GameSession has no league");
        }

        List<Tactic> availableTactics = session.getSport().createFactory().generateTactics();
        ITeam playerTeam = session.getPlayerTeam();

        List<IFixture> weekFixtures = league.getWeekFixtures(week);
        for (IFixture fixture : weekFixtures) {
            if (fixture.isPlayed()) {
                continue;
            }
            ITeam home = fixture.getHomeTeam();
            ITeam away = fixture.getAwayTeam();

            // Assign random tactic to AI teams (not player team)
            if (home != playerTeam) {
                home.setTactic(availableTactics.get(random.nextInt(availableTactics.size())));
            }
            if (away != playerTeam) {
                away.setTactic(availableTactics.get(random.nextInt(availableTactics.size())));
            }

            IMatchResult result = engine.simulate(home, away);
            league.recordResult(fixture, result);
        }
    }

    public void playCurrentWeek(GameSession session, IMatchEngine engine) {
        playWeek(session, engine, session.getCurrentWeek());
    }
}