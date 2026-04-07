package com.sportsmanager.football;

import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportFactory;

public class FootballSport implements Sport {

    @Override
    public String getName() {
        return "Football";
    }

    @Override
    public int getMaxSquadSize() {
        return 25;
    }

    @Override
    public int getMaxMatchSquadSize() {
        return 18;
    }

    @Override
    public int getMatchPhaseCount() {
        return 2;
    }

    @Override
    public SportFactory createFactory() {
        return new FootballFactory();
    }
}
