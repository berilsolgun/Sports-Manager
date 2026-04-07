package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;

import java.util.List;

public interface ILeague {
    List<ITeam> getTeams();
    List<IFixture> getFixtures();
    void recordResult(IFixture fixture, IMatchResult result);
    List<StandingEntry> getStandings();
    boolean isSeasonOver();
    List<IFixture> getWeekFixtures(int week);

}
