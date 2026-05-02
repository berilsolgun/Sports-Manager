package com.sportsmanager.volleyball;

import com.sportsmanager.domain.sport.Sport;
import com.sportsmanager.domain.sport.SportFactory;

public class VolleyballSport implements Sport {
    @Override
    public String getName() {
        return "Volleyball";
    }

    @Override
    public int getMaxSquadSize() { //according to FIVB
        return 14;
    }

    @Override
    public int getMaxMatchSquadSize() {
        return 12;
    }

    @Override
    public int getMatchPhaseCount() {
        return 5;
    }

    @Override
    public SportFactory createFactory() {
        return new VolleyballFactory();
    }
}
