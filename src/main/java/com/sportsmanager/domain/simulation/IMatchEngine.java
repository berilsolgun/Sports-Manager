package com.sportsmanager.domain.simulation;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.ITeam;

import java.util.List;

public interface IMatchEngine {

    /**
     * Full automatic simulation (compat): begins match, plays all phases, returns final result.
     */
    IMatchResult simulate(ITeam home, ITeam away);

    /**
     * Legacy hook: simulate one phase/set by index (still used by tests / direct calls).
     */
    PhaseResult simulatePhase(ITeam home, ITeam away, int phase);

    List<MatchEvent> getMatchEvents();

    /**
     * Stepwise match flow for live UI: {@link #beginMatch}, loop {@link #playNextPhase} until
     * {@link #isMatchComplete()}, then {@link #endMatch}.
     */
    void beginMatch(ITeam home, ITeam away);

    boolean isMatchComplete();

    PhaseResult playNextPhase();

    IMatchResult endMatch();
}
