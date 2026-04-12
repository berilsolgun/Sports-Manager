package com.sportsmanager.football;

import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.simulation.MatchEvent;
import com.sportsmanager.domain.team.IPlayer;
import com.sportsmanager.domain.team.ITeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FootballMatchResult implements IMatchResult {

    private final int homeScore;
    private final int awayScore;
    private final ITeam homeTeam;
    private final ITeam awayTeam;
    private final List<MatchEvent> events;
    private final List<IPlayer> injuredPlayers;

    public FootballMatchResult(int homeScore, int awayScore, ITeam homeTeam, ITeam awayTeam,
                               List<MatchEvent> events, List<IPlayer> injuredPlayers) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.events = events != null ? events : new ArrayList<>();
        this.injuredPlayers = injuredPlayers != null ? injuredPlayers : new ArrayList<>();
    }

    @Override
    public int getHomeScore() {
        return homeScore;
    }

    @Override
    public int getAwayScore() {
        return awayScore;
    }

    @Override
    public Optional<ITeam> getWinner() {
        if (homeScore > awayScore) return Optional.of(homeTeam);
        if (awayScore > homeScore) return Optional.of(awayTeam);
        return Optional.empty();
    }

    @Override
    public boolean isDraw() {
        return homeScore == awayScore;
    }

    @Override
    public int getHomePoints() {
        if (homeScore > awayScore) return 3;
        if (homeScore == awayScore) return 1;
        return 0;
    }

    @Override
    public int getAwayPoints() {
        if (awayScore > homeScore) return 3;
        if (homeScore == awayScore) return 1;
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