package com.sportsmanager.domain.simulation;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.ITeam;

import java.util.List;

public interface IMatchEngine {
    IMatchResult simulate(ITeam home, ITeam away);
    PhaseResult simulatePhase(ITeam home, ITeam away, int phase);
    List<MatchEvent> getMatchEvents();


}
