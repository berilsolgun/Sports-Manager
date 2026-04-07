package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;

import java.util.Optional;

public interface IFixture {
    ITeam getHomeTeam();
    ITeam getAwayTeam();
    int getWeek();
    boolean isPlayed();
    Optional<IMatchResult> getResult();

}
