package com.sportsmanager.volleyball;

import com.sportsmanager.domain.league.IFixture;
import com.sportsmanager.domain.league.IMatchResult;
import com.sportsmanager.domain.team.ITeam;

import java.util.Optional;

public class VolleyballFixture implements IFixture {

    private final ITeam homeTeam;
    private final ITeam awayTeam;
    private final int week;
    private IMatchResult result;

    public VolleyballFixture(ITeam homeTeam, ITeam awayTeam, int week) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.week = week;
    }

    @Override
    public ITeam getHomeTeam() {
        return homeTeam;
    }

    @Override
    public ITeam getAwayTeam() {
        return awayTeam;
    }

    @Override
    public int getWeek() {
        return week;
    }

    @Override
    public boolean isPlayed() {
        return result != null;
    }

    @Override
    public Optional<IMatchResult> getResult() {
        return Optional.ofNullable(result);
    }

    public void setResult(IMatchResult result) {
        this.result = result;
    }
}