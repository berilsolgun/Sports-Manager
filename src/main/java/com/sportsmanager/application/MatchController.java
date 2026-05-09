package com.sportsmanager.application;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.ILeague;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.session.GameSession;
import com.sportsmanager.domain.simulation.IMatchEngine;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;
import com.sportsmanager.domain.team.Tactic;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Runs match simulation for fixtures in a given week and records results on the league.
 * Auto-assigns random tactics to AI teams (non-player teams).
 */
public class MatchController {

    private final Random random = new Random();

    /** Simulates every fixture in the week (including the player's match). */
    public void playWeek(GameSession session, IMatchEngine engine, int week) {
        playWeek(session, engine, week, true);
    }

    /**
     * @param autoResolvePlayerMatches when {@code false}, fixtures involving {@link GameSession#getPlayerTeam()}
     *                                 stay unplayed so the UI can drive {@link IMatchEngine} step-by-step.
     */
    public void playWeek(GameSession session, IMatchEngine engine, int week, boolean autoResolvePlayerMatches) {
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

            boolean involvesPlayer = playerTeam != null
                    && (fixture.getHomeTeam() == playerTeam || fixture.getAwayTeam() == playerTeam);

            if (!autoResolvePlayerMatches && involvesPlayer) {
                continue;
            }

            ITeam home = fixture.getHomeTeam();
            ITeam away = fixture.getAwayTeam();

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

    public void playCurrentWeek(GameSession session, IMatchEngine engine, boolean autoResolvePlayerMatches) {
        playWeek(session, engine, session.getCurrentWeek(), autoResolvePlayerMatches);
    }

    public Optional<IFixture> findPlayerFixtureThisWeek(GameSession session, int week) {
        ITeam playerTeam = session.getPlayerTeam();
        if (playerTeam == null || session.getLeague() == null) {
            return Optional.empty();
        }
        for (IFixture fx : session.getLeague().getWeekFixtures(week)) {
            if (fx.isPlayed()) {
                continue;
            }
            if (fx.getHomeTeam() == playerTeam || fx.getAwayTeam() == playerTeam) {
                return Optional.of(fx);
            }
        }
        return Optional.empty();
    }

    public void recordResult(GameSession session, IFixture fixture, IMatchResult result) {
        session.getLeague().recordResult(fixture, result);
    }

    /** One injury recovery tick per injured player after all fixtures for this gameweek are recorded. */
    public void applyGameweekInjuryRecovery(GameSession session) {
        if (session.getLeague() == null) {
            return;
        }
        for (ITeam team : session.getLeague().getTeams()) {
            for (IPlayer p : team.getSquad()) {
                if (p.isInjured()) {
                    p.recoverOneGame();
                }
            }
        }
    }
}
