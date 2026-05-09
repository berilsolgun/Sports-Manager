package com.sportsmanager.domain.simulation;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared multi-phase match orchestration with optional stepwise ({@link #beginMatch} /
 * {@link #playNextPhase} / {@link #endMatch}) playback for live UI.
 */
public abstract class AbstractMatchEngine implements IMatchEngine {

    private final List<MatchEvent> matchEvents = new ArrayList<>();

    protected ITeam ctxHome;
    protected ITeam ctxAway;
    protected boolean matchStarted;
    protected boolean matchFinished;

    protected void recordMatchEvent(MatchEvent event) {
        matchEvents.add(event);
    }

    protected void clearMatchEvents() {
        matchEvents.clear();
    }

    @Override
    public final List<MatchEvent> getMatchEvents() {
        return Collections.unmodifiableList(matchEvents);
    }

    protected List<MatchEvent> copyMatchEvents() {
        return new ArrayList<>(matchEvents);
    }

    protected abstract int getPhaseCount();

    /**
     * Build the final {@link IMatchResult} (injuries, events snapshot, etc.).
     * For football {@code homeScore}/{@code awayScore} are goals; for volleyball they are sets won.
     */
    protected abstract IMatchResult finishMatch(ITeam home, ITeam away, int homeScore, int awayScore);

    @Override
    public final void beginMatch(ITeam home, ITeam away) {
        if (home == null || away == null) {
            throw new IllegalArgumentException("home and away teams required");
        }
        ctxHome = home;
        ctxAway = away;
        matchStarted = true;
        matchFinished = false;
        clearMatchEvents();
        resetStepwiseState();
    }

    /** Reset subclass counters before first {@link #playNextPhase}. */
    protected abstract void resetStepwiseState();

    @Override
    public abstract boolean isMatchComplete();

    @Override
    public abstract PhaseResult playNextPhase();

    @Override
    public final IMatchResult endMatch() {
        if (!matchStarted) {
            throw new IllegalStateException("beginMatch first");
        }
        if (!matchFinished) {
            throw new IllegalStateException("match not complete");
        }
        return buildFinalResult();
    }

    /** Called only when {@link #matchFinished} is true. */
    protected abstract IMatchResult buildFinalResult();

    @Override
    public final IMatchResult simulate(ITeam home, ITeam away) {
        beginMatch(home, away);
        while (!isMatchComplete()) {
            playNextPhase();
        }
        return endMatch();
    }
}
