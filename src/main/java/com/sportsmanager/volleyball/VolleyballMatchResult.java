package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.MatchEvent;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VolleyballMatchResult implements IMatchResult {
    private final int homeSets;
    private final int awaySets;
    private final ITeam homeTeam;
    private final ITeam awayTeam;
    private final List<MatchEvent> events;
    private final List<IPlayer> injuredPlayers;

    public VolleyballMatchResult(int homeSets, int awaySets, ITeam homeTeam, ITeam awayTeam, List<MatchEvent> events, List<IPlayer> injuredPlayers) {
        this.homeSets = homeSets;
        this.awaySets = awaySets;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.events = events != null ? events : new ArrayList<>();
        this.injuredPlayers = injuredPlayers != null ? injuredPlayers : new ArrayList<>();
    }

    @Override
    public int getHomeScore() {
        return homeSets;
    }

    @Override
    public int getAwayScore() {
        return awaySets;
    }

    @Override
    public Optional<ITeam> getWinner() {
        if (homeSets > awaySets) return Optional.of(homeTeam);
        if (awaySets > homeSets) return Optional.of(awayTeam);
        return Optional.empty();
    }

    @Override
    public boolean isDraw() { // no draws in volleyball
        return false;
    }

    @Override
    public int getHomePoints() {
        if (homeSets == 3 && (awaySets == 0 || awaySets == 1)){
            return 3;
        }
        if (homeSets == 3 && awaySets == 2){
            return 2;
        }
        if (homeSets == 2 && awaySets == 3){
            return 1;
        }
        return 0;
    }

    @Override
    public int getAwayPoints() {
        if (awaySets == 3 && (homeSets == 0 || homeSets == 1)){
            return 3;
        }
        if (awaySets == 3 && homeSets == 2){
            return 2;
        }
        if (awaySets == 2 && homeSets == 3){
            return 1;
        }
        return 0;
    }

    @Override
    public List<MatchEvent> getEvents() {
        return events;
    }

    @Override
    public List<IPlayer> getInjuredPlayers() {
        return injuredPlayers;
    }
}
