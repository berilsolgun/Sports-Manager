package com.sportsmanager.domain.simulation;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared orchestration for multi-phase matches: clears event log, runs each phase,
 * then delegates to {@link #finishMatch} for sport-specific result packaging.
 */
public abstract class AbstractMatchEngine implements IMatchEngine {

    private final List<MatchEvent> matchEvents = new ArrayList<>();

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
     * Build the final {@link IMatchResult} after all phases (injuries, events snapshot, etc.).
     */
    protected abstract IMatchResult finishMatch(ITeam home, ITeam away, int homeScore, int awayScore);

    @Override
    public final IMatchResult simulate(ITeam home, ITeam away) {
        clearMatchEvents();
        int homeScore = 0;
        int awayScore = 0;
        for (int phase = 1; phase <= getPhaseCount(); phase++) {
            PhaseResult pr = simulatePhase(home, away, phase);
            homeScore += pr.homeScore;
            awayScore += pr.awayScore;
        }
        return finishMatch(home, away, homeScore, awayScore);
    }
}
