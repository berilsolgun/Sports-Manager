package com.sportsmanager.domain.league;

import com.sportsmanager.domain.team.ITeam;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLeague implements ILeague {
    protected String name;
    protected List<ITeam> teams;
    protected List<IFixture> fixtures = new ArrayList<>();

    public AbstractLeague(String name, List<ITeam> teams) {
        this.name = name;
        this.teams = teams;
    }

    @Override
    public List<ITeam> getTeams() { return teams; }

    @Override
    public List<IFixture> getFixtures() { return fixtures; }

    @Override
    public abstract List<StandingEntry> getStandings();

    @Override
    public abstract boolean isSeasonOver();

}